import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import java.awt.FileDialog
import java.awt.Frame
import me.ashishekka.k8.configs.EmulatorSpeed
import me.ashishekka.k8.configs.ThemeColor

@Composable
fun FrameWindowScope.K8MenuBar(
    isFileChooserOpen: Boolean,
    soundEnabled: Boolean,
    currentTheme: ThemeColor,
    currentSpeed: EmulatorSpeed,
    onFileOpenClick: () -> Unit,
    onResetClick: () -> Unit,
    onNewFileChosen: (newFile: String) -> Unit,
    onSoundToggled: (Boolean) -> Unit,
    onThemeChanged: (Int) -> Unit,
    onSpeedChanged: (Int) -> Unit
) {
    if (isFileChooserOpen) {
        FileDialog(
            onCloseRequest = { result -> result?.let { onNewFileChosen(it) } }
        )
    }
    MenuBar {
        Menu("File", mnemonic = 'F') {
            Item(
                text = "Open New Rom",
                onClick = onFileOpenClick
            )
            Item(
                text = "Reset Rom",
                mnemonic = 'O',
                shortcut = KeyShortcut(Key.O, meta = true),
                onClick = onResetClick
            )
        }
        Menu("Settings") {
            CheckboxItem(
                text = "Sound emulation",
                mnemonic = 'S',
                shortcut = KeyShortcut(Key.S, meta = true),
                checked = soundEnabled,
                onCheckedChange = { onSoundToggled(!soundEnabled) }
            )
            Menu("Theme") {
                ThemeColor.values().forEachIndexed { index, colorScheme ->
                    RadioButtonItem(
                        text = colorScheme.schemeName.capitalize(),
                        selected = currentTheme.ordinal == index,
                        onClick = { onThemeChanged(index) }
                    )
                }
            }
            Menu("Speed") {
                EmulatorSpeed.values().forEachIndexed { index, emulatorSpeed ->
                    RadioButtonItem(
                        text = "${emulatorSpeed.speedFactor}X",
                        selected = currentSpeed.ordinal == index,
                        onClick = { onSpeedChanged(index) }
                    )
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
