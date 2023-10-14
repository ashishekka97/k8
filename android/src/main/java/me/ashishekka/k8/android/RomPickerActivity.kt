@file:OptIn(ExperimentalMaterialApi::class)

package me.ashishekka.k8.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val PICKED_ROM_PATH = "PICKED_ROM_PATH"

class RomPickerActivity : AppCompatActivity() {

    private val romFileList: MutableState<List<RomFile>> = mutableStateOf(emptyList())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RomPickerScreen(romFileList.value, ::onRomFileClicked) {
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
                } else null
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
}

@Composable
fun RomPickerScreen(
    files: List<RomFile>,
    onRomFileClicked: (RomFile) -> Unit,
    onBackClicked: () -> Unit
) {
    MaterialTheme { // or AppCompatTheme
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Load ROM") },
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
            RomFileList(it, files, onRomFileClicked)
        }
    }
}

@Composable
fun RomFileList(
    paddingValues: PaddingValues,
    files: List<RomFile>,
    onRomFileClicked: (RomFile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues).fillMaxWidth()
    ) {
        items(files) { romFile ->
            Surface(
                onClick = { onRomFileClicked(romFile) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    romFile.romName,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

data class RomFile(
    val romName: String,
    val romPath: String
)