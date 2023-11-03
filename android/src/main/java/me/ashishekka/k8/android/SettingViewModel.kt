package me.ashishekka.k8.android

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.ashishekka.k8.configs.EmulatorSpeed
import me.ashishekka.k8.configs.ThemeColor
import me.ashishekka.k8.storage.K8Settings
import me.ashishekka.k8.storage.KEY_HAPTICS
import me.ashishekka.k8.storage.KEY_SOUND
import me.ashishekka.k8.storage.KEY_SPEED
import me.ashishekka.k8.storage.KEY_THEME
import me.ashishekka.k8.storage.KEY_VERSION

class SettingViewModel : ViewModel() {
    private val _uiState: MutableState<SettingUiState> = mutableStateOf(SettingUiState())
    val uiState: State<SettingUiState>
        get() = _uiState

    private val settings = K8Settings()

    fun loadSettings() {
        viewModelScope.launch {
            combine(
                settings.getBooleanFlowSetting(KEY_SOUND),
                settings.getBooleanFlowSetting(KEY_HAPTICS),
                settings.getIntFlowSetting(KEY_THEME),
                settings.getIntFlowSetting(KEY_SPEED)
            ) { sound, haptics, theme, speed ->
                listOf(
                    Setting.ToggleSetting(
                        key = KEY_SOUND,
                        title = "Sound",
                        description = "Enables sound emulation",
                        isEnabled = sound
                    ),

                    Setting.ToggleSetting(
                        key = KEY_HAPTICS,
                        title = "Haptic Feedback",
                        description = "Enables haptic feedback on key press",
                        isEnabled = haptics
                    ),

                    Setting.MultiOptionSetting(
                        key = KEY_THEME,
                        title = "Theme",
                        description = "Change the color scheme of the app and chip8 screen",
                        options = ThemeColor.getAllThemes(),
                        optionSelected = theme
                    ),

                    Setting.MultiOptionSetting(
                        key = KEY_SPEED,
                        title = "Emulation Speed",
                        description = "Change emulation speed",
                        options = EmulatorSpeed.getAllSpeeds(),
                        optionSelected = speed
                    ),

                    Setting.TextSetting(
                        key = KEY_VERSION,
                        title = "Version",
                        description = "${BuildConfig.BUILD_TYPE} ${BuildConfig.VERSION_NAME}"
                    )
                )
            }.collectLatest {
                _uiState.value = SettingUiState(settings = it)
            }
        }
    }

    fun onSettingClicked(setting: Setting) {
        when (setting) {
            is Setting.ToggleSetting -> {
                changeSetting(setting.copy(isEnabled = !setting.isEnabled))
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
        changeSetting(setting.copy(optionSelected = optionIndex))
        cancelModel()
    }

    fun cancelModel() {
        _uiState.value = _uiState.value.copy(showOptionDialog = false)
    }

    private fun changeSetting(setting: Setting) {
        viewModelScope.launch {
            when (setting) {
                is Setting.ToggleSetting -> {
                    settings.setBooleanSetting(setting.key, setting.isEnabled)
                }

                is Setting.MultiOptionSetting -> {
                    settings.setIntSetting(setting.key, setting.optionSelected)
                }

                is Setting.TextSetting -> Unit
            }
        }
    }
}

data class SettingUiState(
    val settings: List<Setting> = emptyList(),
    val optionDialog: Setting.MultiOptionSetting = Setting.MultiOptionSetting(
        "",
        "",
        "",
        emptyList(),
        -1
    ),
    val showOptionDialog: Boolean = false
)
