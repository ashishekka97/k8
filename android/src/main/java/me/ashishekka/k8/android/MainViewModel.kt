package me.ashishekka.k8.android

import android.content.Context
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ashishekka.k8.core.Chip8Impl
import me.ashishekka.k8.core.KeyEventType

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

    private var composeCoroutineScope: CoroutineScope? = null
    private var snackbarHostState: SnackbarHostState? = null

    fun readRomFile(context: Context, filePath: String = uiState.value.currentRom) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val romFile = context.assets.open(filePath)
            val romData = romFile.readBytes()
            chip8.loadRom(romData)
            chip8.start()
            _uiState.value = UiState(
                currentRom = filePath,
                snackMessage = SnackMessage(MessageType.SUCCESS, "Loaded $filePath")
            )
            composeCoroutineScope?.let {
                withContext(it.coroutineContext) {
                    snackbarHostState?.showSnackbar(
                        _uiState.value.snackMessage.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    fun observeUiState(
        composeCoroutineScope: CoroutineScope,
        snackbarHostState: SnackbarHostState
    ) {
        this.composeCoroutineScope = composeCoroutineScope
        this.snackbarHostState = snackbarHostState
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
        readRomFile(context)
    }
}

data class UiState(
    val currentRom: String = "chip8-test-suite.ch8",
    val snackMessage: SnackMessage = SnackMessage(MessageType.SUCCESS, "Loaded $currentRom")
)

data class SnackMessage(
    val type: MessageType,
    val message: String
)

enum class MessageType {
    ERROR,
    SUCCESS
}