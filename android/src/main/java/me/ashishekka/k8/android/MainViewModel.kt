package me.ashishekka.k8.android

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.InputStream
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

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, th ->
        Log.d("MainViewModel", "Error happened: $th")
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

    fun readRomFromAssets(context: Context, filePath: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val romFile = context.assets.open(filePath)
            loadRomFileFromInputStream(romFile)
            _uiState.value = UiState(
                currentRom = filePath,
                currentRomType = RomType.INBUILT,
                snackMessage = SnackMessage(MessageType.SUCCESS, "Loaded $filePath")
            )
            delay(100)
            _uiState.value = UiState(
                currentRom = filePath,
                currentRomType = RomType.INBUILT,
                snackMessage = null
            )
        }
    }

    fun readCustomRomFromUri(context: Context, uri: Uri) {
        Log.d("ASHISH_", "Reading from uri: $uri")
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val inputStream = context.contentResolver.openInputStream(uri)
            try {
                if (inputStream != null) {
                    loadRomFileFromInputStream(inputStream)
                }
                val fileName = getFileNameFromUri(context, uri)
                _uiState.value = UiState(
                    currentRom = uri.toString(),
                    currentRomType = RomType.CUSTOM,
                    snackMessage = SnackMessage(MessageType.SUCCESS, "Loaded $fileName")
                )
            } catch (_: Throwable) {
            } finally {
                inputStream?.close()
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0) {
                        result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (cut != null) {
                    result = result?.substring(cut + 1)
                }
            }
        }
        return result ?: ""
    }

    private fun loadRomFileFromInputStream(inputStream: InputStream) {
        val romData = inputStream.readBytes()
        Log.d("Bytes", "$romData")
        chip8.loadRom(romData)
        chip8.start()
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
        val uiState = _uiState.value
        when (uiState.currentRomType) {
            RomType.INBUILT -> readRomFromAssets(context, uiState.currentRom)
            RomType.CUSTOM -> readCustomRomFromUri(context, uiState.currentRom.toUri())
        }
    }
}

data class UiState(
    val currentRom: String = "",
    val currentRomType: RomType = RomType.INBUILT,
    val snackMessage: SnackMessage? = null
)

enum class RomType {
    INBUILT, CUSTOM
}

data class SnackMessage(
    val type: MessageType,
    val message: String
)

enum class MessageType {
    ERROR,
    SUCCESS
}
