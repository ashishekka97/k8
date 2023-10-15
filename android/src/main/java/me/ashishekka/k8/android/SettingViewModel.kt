package me.ashishekka.k8.android

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SettingViewModel : ViewModel() {
    private val _uiState: MutableState<SettingUiState> = mutableStateOf(SettingUiState())
    val uiState: State<SettingUiState>
        get() = _uiState


    fun loadSettings() {
        val initialSettings = listOf(
            Setting.ToggleSetting(
                title = "Sound",
                description = "Enables sound emulation",
                isEnabled = false
            ),

            Setting.ToggleSetting(
                title = "Haptic Feedback",
                description = "Enables haptic feedback on key press",
                isEnabled = false
            ),

            Setting.MultiOptionSetting(
                title = "Theme",
                description = "Change the color scheme of the app and chip8 screen",
                options = listOf("Default", "Terminal", "Gameboy"),
                optionSelected = 0
            ),

            Setting.MultiOptionSetting(
                title = "Emulation Speed",
                description = "Change emulation speed",
                options = listOf("0.5X", "1.0x", "1.5X", "2.0X"),
                optionSelected = 1
            ),

            Setting.TextSetting(
                title = "Version",
                description = "v1.0.1"
            )
        )
        _uiState.value = SettingUiState(settings = initialSettings)
    }

    fun onSettingClicked(setting: Setting) {
        when (setting) {
            is Setting.ToggleSetting -> {
                mutateSetting(setting.copy(isEnabled = !setting.isEnabled))
            }

            is Setting.MultiOptionSetting -> {
                _uiState.value = _uiState.value.copy(
                    optionDialog = setting,
                    showOptionDialog = true
                )
            }

            is Setting.TextSetting -> Unit
        }
    }

    fun onDialogOptionSelected(setting: Setting.MultiOptionSetting, optionIndex: Int) {
        mutateSetting(setting.copy(optionSelected = optionIndex))
        cancelModel()
    }

    fun cancelModel() {
        _uiState.value = _uiState.value.copy(showOptionDialog = false)
    }

    private fun mutateSetting(setting: Setting) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.map {
                if (it.title == setting.title) setting
                else it
            }
        )
    }
}

data class SettingUiState(
    val settings: List<Setting> = emptyList(),
    val optionDialog: Setting.MultiOptionSetting = Setting.MultiOptionSetting(
        "",
        "",
        emptyList(),
        -1
    ),
    val showOptionDialog: Boolean = false
)