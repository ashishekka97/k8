@file:OptIn(ExperimentalMaterialApi::class)

package me.ashishekka.k8.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.Colors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import me.ashishekka.k8.android.theming.fullScaffoldBackground
import me.ashishekka.k8.android.theming.getThemeColors
import me.ashishekka.k8.android.util.capitalize
import me.ashishekka.k8.configs.ThemeColor
import me.ashishekka.k8.storage.K8Settings
import me.ashishekka.k8.storage.KEY_THEME

class SettingsActivity : AppCompatActivity() {
    private val viewModel by lazy { SettingViewModel() }
    private val settings by lazy { K8Settings() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val uiState = viewModel.uiState
            val themeState = settings.getIntFlowSetting(KEY_THEME).collectAsState(0)
            val theme = ThemeColor.getThemeFromIndex(themeState.value)
            SettingScreen(
                getThemeColors(theme),
                uiState.value,
                viewModel::onSettingClicked,
                viewModel::onDialogOptionSelected,
                viewModel::cancelModel,
                onBackPressedDispatcher::onBackPressed
            )
        }
        viewModel.loadSettings()
    }
}

@Composable
fun SettingScreen(
    themeColors: Colors,
    uiState: SettingUiState,
    onSettingClicked: (Setting) -> Unit,
    onDialogOptionSelected: (Setting.MultiOptionSetting, Int) -> Unit,
    onDialogCancelled: () -> Unit,
    onBackClicked: () -> Unit
) {
    MaterialTheme(colors = themeColors) { // or AppCompatTheme
        Scaffold(
            modifier = Modifier.background(
                fullScaffoldBackground(
                    color = MaterialTheme.colors.primarySurface,
                    elevationOverlay = LocalElevationOverlay.current
                )
            ).safeDrawingPadding(),
            topBar = {
                TopAppBar(
                    title = { Text("Settings", color = MaterialTheme.colors.primary) },
                    navigationIcon = {
                        IconButton(onBackClicked) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_back),
                                "Back to previous screen",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                )
            }
        ) {
            SettingList(it, uiState.settings, onSettingClicked)
            if (uiState.showOptionDialog) {
                OptionSelectionUi(
                    uiState.optionDialog,
                    { newOptionIndex ->
                        onDialogOptionSelected(uiState.optionDialog, newOptionIndex)
                    }
                ) {
                    onDialogCancelled()
                }
            }
        }
    }
}

@Composable
fun OptionSelectionUi(
    setting: Setting.MultiOptionSetting,
    onOptionChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismiss, DialogProperties()) {
        Card {
            Column {
                Text(
                    "Choose ${setting.title.lowercase()}",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn {
                    itemsIndexed(setting.options) { optionIndex, optionName ->
                        OptionUi(
                            optionName = optionName.capitalize(),
                            optionIndex = optionIndex,
                            isSelected = setting.optionSelected == optionIndex
                        ) { newSelectedIndex -> onOptionChanged(newSelectedIndex) }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    TextButton(onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun OptionUi(
    optionName: String,
    optionIndex: Int,
    isSelected: Boolean,
    onOptionSelected: (Int) -> Unit
) {
    Surface(
        onClick = { onOptionSelected(optionIndex) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Text(
                optionName,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.primary
            )
            RadioButton(
                selected = isSelected,
                colors = RadioButtonDefaults.colors(
                    unselectedColor = MaterialTheme.colors.secondary
                ),
                onClick = { onOptionSelected(optionIndex) }
            )
        }
    }
}

@Composable
fun SettingList(
    paddingValues: PaddingValues,
    settings: List<Setting>,
    onSettingClicked: (Setting) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues).fillMaxWidth()
    ) {
        items(settings) { setting ->
            SettingUi(setting, onSettingClicked)
        }
    }
}

@Composable
fun SettingUi(
    setting: Setting,
    onSettingClicked: (Setting) -> Unit
) {
    Surface(
        onClick = { onSettingClicked(setting) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(
                    setting.title,
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    setting.description,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(0.4f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (setting) {
                    is Setting.ToggleSetting -> Switch(
                        checked = setting.isEnabled,
                        onCheckedChange = { _ -> onSettingClicked(setting) }
                    )

                    is Setting.MultiOptionSetting -> {
                        val text = with(setting) {
                            if (optionSelected > -1 && optionSelected < options.size) {
                                options[optionSelected].capitalize()
                            } else {
                                "Invalid"
                            }
                        }
                        Text(
                            text,
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.primary
                        )
                    }

                    is Setting.TextSetting -> Unit
                }
            }
        }
    }
}

sealed class Setting(
    open val key: String,
    open val title: String,
    open val description: String
) {
    data class ToggleSetting(
        override val key: String,
        override val title: String,
        override val description: String,
        val isEnabled: Boolean
    ) : Setting(key, title, description)

    data class MultiOptionSetting(
        override val key: String,
        override val title: String,
        override val description: String,
        val options: List<String>,
        val optionSelected: Int
    ) : Setting(key, title, description)

    data class TextSetting(
        override val key: String,
        override val title: String,
        override val description: String
    ) : Setting(key, title, description)
}
