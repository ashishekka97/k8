@file:OptIn(ExperimentalMaterialApi::class)

package me.ashishekka.k8.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

class SettingsActivity : AppCompatActivity() {

    private val viewModel by lazy { SettingViewModel() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState = viewModel.uiState
            SettingScreen(
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
    uiState: SettingUiState,
    onSettingClicked: (Setting) -> Unit,
    onDialogOptionSelected: (Setting.MultiOptionSetting, Int) -> Unit,
    onDialogCancelled: () -> Unit,
    onBackClicked: () -> Unit
) {
    MaterialTheme { // or AppCompatTheme
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onBackClicked) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_back),
                                "Back to previous screen",
                                tint = Color.White
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
                    }) {
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
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn {
                    itemsIndexed(setting.options) { optionIndex, optionName ->
                        OptionUi(
                            optionName = optionName,
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
            Text(optionName, style = MaterialTheme.typography.subtitle1)
            RadioButton(
                selected = isSelected,
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
                Text(setting.title, style = MaterialTheme.typography.subtitle1)
                Text(setting.description, style = MaterialTheme.typography.caption)
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
                                options[optionSelected]
                            } else "Invalid"
                        }
                        Text(text, style = MaterialTheme.typography.subtitle2)
                    }

                    is Setting.TextSetting -> Unit
                }
            }
        }
    }
}

sealed class Setting(
    open val title: String,
    open val description: String
) {
    data class ToggleSetting(
        override val title: String,
        override val description: String,
        val isEnabled: Boolean
    ) : Setting(title, description)

    data class MultiOptionSetting(
        override val title: String,
        override val description: String,
        val options: List<String>,
        val optionSelected: Int
    ) : Setting(title, description)

    data class TextSetting(
        override val title: String,
        override val description: String
    ) : Setting(title, description)
}