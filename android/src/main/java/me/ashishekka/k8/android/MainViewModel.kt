package me.ashishekka.k8.android

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ashishekka.k8.configs.EmulatorSpeed
import me.ashishekka.k8.core.Chip8Impl
import me.ashishekka.k8.core.KeyEventType
import me.ashishekka.k8.storage.K8Settings
import me.ashishekka.k8.storage.KEY_SOUND
import me.ashishekka.k8.storage.KEY_SPEED

class MainViewModel : ViewModel() {

    private val chip8 = Chip8Impl(viewModelScope)

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
        _uiState.value = UiState(
            currentRom = "",
            snackMessage = SnackMessage(
                type = MessageType.ERROR,
                message = "Error loading file. Please select a new one"
            )
        )
    }

    private val _uiState = mutableStateOf(UiState())
    val uiState: State<UiState>
        get() = _uiState

    val videoMemory = chip8.getVideoMemoryState()
    val soundState = chip8.getSoundState()

    val settings by lazy { K8Settings() }

    fun readRomFile(context: Context, filePath: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val romFile = context.assets.open(filePath)
            val romData = romFile.readBytes()
            Log.d("Bytes", "$romData.")
            chip8.loadRom(romData)
            chip8.start()
            _uiState.value = UiState(
                currentRom = filePath,
                snackMessage = SnackMessage(MessageType.SUCCESS, "Loaded $filePath")
            )
            delay(100)
            _uiState.value = UiState(
                currentRom = filePath,
                snackMessage = null
            )
        }
    }

    fun observeUiState() {
        viewModelScope.launch {
            settings.getIntFlowSetting(KEY_SPEED).collectLatest { speedIndex ->
                val speed = EmulatorSpeed.getSpeedFromIndex(speedIndex)
                chip8.emulationSpeedFactor(speed.speedFactor)
            }
        }
        viewModelScope.launch {
            settings.getBooleanFlowSetting(KEY_SOUND).collectLatest { soundEnabled ->
                chip8.toggleSound(soundEnabled)
            }
        }
    }

    fun onGameKeyDown(key: Int) {
        chip8.onKey(key, KeyEventType.DOWN)
    }

    fun onGameKeyUp(key: Int) {
        chip8.onKey(key, KeyEventType.UP)
    }

    fun onPause() {
        chip8.pause()
    }

    fun onResume() {
        chip8.resume()
    }

    fun resetRom(context: Context) {
        chip8.reset()
        readRomFile(context, _uiState.value.currentRom)
    }
}

data class UiState(
    val currentRom: String = "",
    val snackMessage: SnackMessage? = null
)

data class SnackMessage(
    val type: MessageType,
    val message: String
)

enum class MessageType {
    ERROR,
    SUCCESS
}
