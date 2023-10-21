import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ashishekka.k8.configs.ColorScheme
import me.ashishekka.k8.configs.ColorScheme.DEFAULT
import me.ashishekka.k8.configs.EmulatorSpeed
import me.ashishekka.k8.configs.EmulatorSpeed.FULL
import me.ashishekka.k8.core.Chip8
import me.ashishekka.k8.core.Chip8Impl
import me.ashishekka.k8.core.KeyEventType
import me.ashishekka.k8.storage.K8Settings
import me.ashishekka.k8.storage.KEY_SOUND
import me.ashishekka.k8.storage.KEY_SPEED
import me.ashishekka.k8.storage.KEY_THEME

const val DEFAULT_ROM = "android/src/main/assets/c8games/INVADERS"

fun main() = application {
    val scope = CoroutineScope(Dispatchers.Main)
    val chip8 = Chip8Impl(scope)
    val settings = K8Settings()

    readRomFile(scope, chip8)
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(640.dp, 348.dp)),
        title = "K8 (Kate) - Chip 8 Emulator",
        resizable = false,
        onKeyEvent = { keyEvent -> onKeyEvent(keyEvent, chip8) }
    ) {
        val themeState = settings.getIntSetting(KEY_THEME).collectAsState(DEFAULT.ordinal)
        val speedState = settings.getIntSetting(KEY_SPEED).collectAsState(FULL.ordinal)
        val soundState = settings.getBooleanSetting(KEY_SOUND).collectAsState(false)

        val theme = ColorScheme.getThemeFromIndex(themeState.value)
        val speed = EmulatorSpeed.getSpeedFromIndex(speedState.value)
        chip8.emulationSpeedFactor(speed.speedFactor)

        MaterialTheme(getThemeColors(theme)) {
            val fileChooserState = remember { mutableStateOf(false) }
            val currentFileState = remember { mutableStateOf(DEFAULT_ROM) }
            K8MenuBar(
                isFileChooserOpen = fileChooserState.value,
                soundEnabled = soundState.value,
                currentTheme = theme,
                currentSpeed = speed,
                onFileOpenClick = { fileChooserState.value = true },
                onResetClick = { onResetClick(scope, chip8, currentFileState.value) },
                onNewFileChosen = { newFile ->
                    onNewFileChosen(
                        scope,
                        chip8,
                        currentFileState,
                        fileChooserState,
                        newFile
                    )
                },
                onSoundToggled = { soundEnabled -> onSoundChanged(scope, settings, soundEnabled) },
                onThemeChanged = { themeIndex -> onThemeChanged(scope, settings, themeIndex) },
                onSpeedChanged = { speedIndex -> onSpeedChanged(scope, settings, speedIndex) }
            )
            MainLayout(chip8)
        }
    }
    chip8.start()
}

private fun onKeyEvent(keyEvent: KeyEvent, chip8: Chip8): Boolean {
    val chip8Key = keyEvent.mapKeyboardToChip8KeyPad()
    return if (chip8Key > -1) {
        when (keyEvent.type) {
            KeyDown -> {
                chip8.onKey(chip8Key, KeyEventType.DOWN)
                true
            }

            KeyUp -> {
                chip8.onKey(chip8Key, KeyEventType.UP)
                true
            }

            else -> false
        }
    } else {
        false
    }
}

private fun readRomFile(
    scope: CoroutineScope,
    chip8: Chip8,
    file: String = Paths.get("android/src/main/assets/c8games/INVADERS").toAbsolutePath().toString()
) {
    scope.launch(Dispatchers.IO) {
        val romFile = File(file)
        val romData = romFile.readBytes()
        chip8.loadRom(romData)
    }
}

private fun onResetClick(scope: CoroutineScope, chip8: Chip8, currentFile: String) {
    readRomFile(scope, chip8, currentFile)
    chip8.start()
}

private fun onNewFileChosen(
    scope: CoroutineScope,
    chip8: Chip8,
    currentFileState: MutableState<String>,
    isFileChooserOpenState: MutableState<Boolean>,
    newFile: String
) {
    isFileChooserOpenState.value = false
    readRomFile(scope, chip8, newFile)
    chip8.start()
    currentFileState.value = newFile
}

private fun onSoundChanged(scope: CoroutineScope, settings: K8Settings, soundEnabled: Boolean) {
    scope.launch {
        withContext(Dispatchers.IO) {
            settings.setBooleanSetting(KEY_SOUND, soundEnabled)
        }
    }
}

private fun onThemeChanged(scope: CoroutineScope, settings: K8Settings, themeIndex: Int) {
    scope.launch {
        withContext(Dispatchers.IO) {
            settings.setIntSetting(KEY_THEME, themeIndex)
        }
    }
}

private fun onSpeedChanged(scope: CoroutineScope, settings: K8Settings, speedIndex: Int) {
    scope.launch {
        withContext(Dispatchers.IO) {
            settings.setIntSetting(KEY_SPEED, speedIndex)
        }
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(
                Locale.getDefault()
            )
        } else it.toString()
    }
}
