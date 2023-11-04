@file:OptIn(ExperimentalMaterialApi::class)

package me.ashishekka.k8.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ashishekka.k8.android.theming.fullScaffoldBackground
import me.ashishekka.k8.android.theming.getThemeColors
import me.ashishekka.k8.configs.ThemeColor
import me.ashishekka.k8.storage.K8Settings
import me.ashishekka.k8.storage.KEY_THEME

const val PICKED_ROM_PATH = "PICKED_ROM_PATH"
const val CUSTOM_ROM_URI = "CUSTOM_ROM_URI"

class RomPickerActivity : AppCompatActivity() {

    private val setting by lazy { K8Settings() }

    private val romFileList: MutableState<List<RomFile>> = mutableStateOf(emptyList())

    val documentOpener =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                val returnIntent = Intent().apply {
                    putExtra(CUSTOM_ROM_URI, uri.toString())
                }
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val themeState = setting.getIntFlowSetting(KEY_THEME).collectAsState(0)
            val theme = ThemeColor.getThemeFromIndex(themeState.value)
            RomPickerScreen(
                getThemeColors(theme),
                romFileList.value,
                ::onRomFileClicked,
                ::onFilePickerClicked
            ) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        loadRomFiles()
    }

    override fun onDestroy() {
        val returnIntent = Intent()
        setResult(RESULT_CANCELED, returnIntent)
        super.onDestroy()
    }

    private fun loadRomFiles() {
        lifecycleScope.launch(Dispatchers.IO) {
            val fileNames = assets.list("c8games")
            val romFiles = fileNames?.mapNotNull { fileName ->
                if (fileName != null) {
                    val filePath = "c8games/$fileName"
                    RomFile(
                        romName = fileName,
                        romPath = filePath
                    )
                } else {
                    null
                }
            } ?: emptyList()
            romFileList.value = romFiles
        }
    }

    private fun onRomFileClicked(romFile: RomFile) {
        val returnIntent = Intent().apply {
            putExtra(PICKED_ROM_PATH, romFile.romPath)
        }
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun onFilePickerClicked() {
        documentOpener.launch(arrayOf("application/octet-stream"))
    }
}

@Composable
fun RomPickerScreen(
    themeColors: Colors,
    files: List<RomFile>,
    onRomFileClicked: (RomFile) -> Unit,
    onFilePickerClicked: () -> Unit,
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
                    title = { Text("Load ROM", color = MaterialTheme.colors.primary) },
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
            Column(
                modifier = Modifier.padding(it),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colors.primary.copy(alpha = 0.6f)
                    ),
                    onClick = onFilePickerClicked,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Pick Custom ROM",
                        style = MaterialTheme.typography.button,
                        color = MaterialTheme.colors.primary
                    )
                }
                Text(
                    text = "Inbuilt ROMS:",
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.primary
                )
                RomFileList(files, onRomFileClicked)
            }
        }
    }
}

@Composable
fun RomFileList(
    files: List<RomFile>,
    onRomFileClicked: (RomFile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(files) { romFile ->
            Surface(
                onClick = { onRomFileClicked(romFile) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    romFile.romName,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

data class RomFile(
    val romName: String,
    val romPath: String
)
