@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ashishekka.k8.configs.ColorScheme
import me.ashishekka.k8.configs.EmulatorSpeed
import me.ashishekka.k8.core.Chip8
import me.ashishekka.k8.core.Chip8Impl
import me.ashishekka.k8.core.KeyEventType
import me.ashishekka.k8.core.VideoMemory

fun main() = application {
    val scope = CoroutineScope(Dispatchers.Main)
    val chip8 = Chip8Impl(scope)

    readRomFile(scope, chip8)
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(640.dp, 340.dp)),
        title = "K8 (Kate) - Chip 8 Emulator",
        resizable = false,
        onKeyEvent = {
            val chip8Key = it.mapKeyboardToChip8KeyPad()
            if (chip8Key > -1) {
                when (it.type) {
                    KeyDown -> chip8.onKey(chip8Key, KeyEventType.DOWN)
                    KeyUp -> chip8.onKey(chip8Key, KeyEventType.UP)
                    else -> return@Window false
                }
                true
            } else {
                false
            }
        }
    ) {
        val isFileChooserOpen = remember { mutableStateOf(false) }
        val currentFile = remember { mutableStateOf("android/src/main/assets/c8games/INVADERS") }
        val theme = remember { mutableStateOf(ColorScheme.DEFAULT) }
        val speed = remember { mutableStateOf(EmulatorSpeed.FULL) }
        val sound = remember { mutableStateOf(false) }
        chip8.emulationSpeedFactor(speed.value.speedFactor)

        MaterialTheme(getThemeColors(theme.value)) {
            if (isFileChooserOpen.value) {
                FileDialog(
                    onCloseRequest = {
                        isFileChooserOpen.value = false
                        println("Result $it")
                        if (it != null) {
                            readRomFile(scope, chip8, it)
                            chip8.start()
                            currentFile.value = it
                        }
                    }
                )
            }
            MenuBar {
                Menu("File", mnemonic = 'F') {
                    Item(
                        text = "Open New Rom",
                        onClick = { isFileChooserOpen.value = true }
                    )
                    Item(
                        text = "Reset Rom",
                        mnemonic = 'O',
                        shortcut = KeyShortcut(Key.O, meta = true),
                        onClick = {
                            readRomFile(scope, chip8, currentFile.value)
                            chip8.start()
                        }
                    )
                }
                Menu("Settings") {
                    CheckboxItem(
                        text = "Sound emulation",
                        mnemonic = 'S',
                        shortcut = KeyShortcut(Key.S, meta = true),
                        checked = sound.value,
                        onCheckedChange = { sound.value = !sound.value }
                    )
                    Menu("Theme") {
                        ColorScheme.values().forEach {
                            RadioButtonItem(
                                text = it.schemeName.capitalize(),
                                selected = theme.value == it,
                                onClick = { theme.value = it }
                            )
                        }
                    }
                    Menu("Speed") {
                        EmulatorSpeed.values().forEach {
                            RadioButtonItem(
                                text = "${it.speedFactor}X",
                                selected = speed.value == it,
                                onClick = { speed.value = it }
                            )
                        }
                    }
                }
            }
            MainLayout(chip8)
        }
    }
    chip8.start()
}

fun readRomFile(
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

@Composable
fun MainLayout(chip8: Chip8) {
    Scaffold {
        val sound = chip8.getSoundState()
        PlaySound(sound.value)
        Column(
            modifier = Modifier.fillMaxHeight().padding(it),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val videoMemory = chip8.getVideoMemoryState()
            Row { Screen(videoMemory.value) }
        }
    }
}

@Composable
fun Screen(videoMemory: VideoMemory) {
    val foreground = MaterialTheme.colors.primary
    val background = MaterialTheme.colors.background
    BoxWithConstraints {
        Canvas(modifier = Modifier.size(width = 640.dp, height = 320.dp)) {
            val blockSize = size.width / 64
            videoMemory.forEachIndexed { row, rowData ->
                rowData.forEachIndexed { col, _ ->
                    val xx = blockSize * col.toFloat()
                    val yy = blockSize * row.toFloat()
                    val color = if (videoMemory[row][col]) background else foreground
                    drawRect(color, topLeft = Offset(xx, yy), Size(blockSize, blockSize))
                }
            }
        }
    }
}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest("$directory$file")
                }
            }
        }
    },
    dispose = FileDialog::dispose
)

fun KeyEvent.mapKeyboardToChip8KeyPad(): Int {
    return when (key) {
        Key.One -> 1
        Key.Two -> 2
        Key.Three -> 3
        Key.Four -> 12
        Key.Q -> 4
        Key.W -> 5
        Key.E -> 6
        Key.R -> 13
        Key.K -> 7
        Key.S -> 8
        Key.D -> 9
        Key.F -> 14
        Key.Z -> 10
        Key.X -> 0
        Key.C -> 11
        Key.V -> 15
        else -> -1
    }
}

@Composable
fun PlaySound(play: Boolean) {
    if (play) Toolkit.getDefaultToolkit().beep()
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
