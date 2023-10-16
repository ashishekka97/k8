package me.ashishekka.k8.android

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import me.ashishekka.k8.android.data.KEY_HAPTICS
import me.ashishekka.k8.android.data.KEY_THEME
import me.ashishekka.k8.android.data.KateDataStoreImpl
import me.ashishekka.k8.android.theming.ColorScheme
import me.ashishekka.k8.android.theming.getThemeColors
import me.ashishekka.k8.core.VideoMemory
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private val viewModel by lazy { MainViewModel(application) }
    private val dataStore by lazy { KateDataStoreImpl(application) }

    private val toneGenerator by lazy {
        ToneGenerator(
            AudioManager.STREAM_MUSIC,
            ToneGenerator.MAX_VOLUME
        )
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val romPath = result.data?.getStringExtra(PICKED_ROM_PATH)
            if (romPath != null) {
                viewModel.readRomFile(this, romPath)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val themePreferenceState = dataStore.getIntPreference(KEY_THEME).collectAsState(0)
            val theme = ColorScheme.getThemeFromIndex(themePreferenceState.value)
            val hapticFeedbackEnabled = dataStore.getBooleanPreference(KEY_HAPTICS).collectAsState(false)
            MaterialTheme(colors = getThemeColors(theme)) { // or AppCompatTheme
                MainLayout(
                    videoMemory = viewModel.videoMemory,
                    soundState = viewModel.soundState,
                    toneGenerator = toneGenerator,
                    enableHaptics = hapticFeedbackEnabled.value,
                    snackbarHostState = snackbarHostState,
                    onGameKeyDown = viewModel::onGameKeyDown,
                    onGameKeyUp = viewModel::onGameKeyUp,
                    onLoadGameClick = ::launchRomPicker,
                    onGameResetClick = { viewModel.resetRom(this) },
                    onSettingsClick = ::launchSettings
                )
            }
            with(viewModel) {
                observeUiState(scope, snackbarHostState)
                readRomFile(this@MainActivity)
            }
        }
    }

    override fun onResume() {
        viewModel.onResume()
        super.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
    }

    private fun launchRomPicker() {
        val intent = Intent(this, RomPickerActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun launchSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun MainLayout(
    videoMemory: State<VideoMemory>,
    soundState: State<Boolean>,
    toneGenerator: ToneGenerator,
    enableHaptics: Boolean,
    snackbarHostState: SnackbarHostState,
    onGameKeyDown: (Int) -> Unit,
    onGameKeyUp: (Int) -> Unit,
    onLoadGameClick: () -> Unit,
    onGameResetClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K8 (Kate)", color = MaterialTheme.colors.primary) },
                actions = {
                    IconButton(onLoadGameClick) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_file_open),
                            "Load ROM",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    IconButton(onGameResetClick) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_reset),
                            "Load ROM",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    IconButton(onSettingsClick) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_settings),
                            "Load ROM",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        val sound = soundState
        PlaySound(toneGenerator, sound.value)
        Column(
            modifier = Modifier.fillMaxHeight().padding(it),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row { Screen(videoMemory.value) }
            Row { Keypad(enableHaptics, onGameKeyDown, onGameKeyUp) }
        }
    }
}

@Composable
fun Screen(videoMemory: VideoMemory) {
    val pixelOffColor = MaterialTheme.colors.background
    val pixelOnColor = MaterialTheme.colors.primary
    BoxWithConstraints {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val blockSize = size.width / 64
            videoMemory.forEachIndexed { row, rowData ->
                rowData.forEachIndexed { col, _ ->
                    val xx = blockSize * col.toFloat()
                    val yy = blockSize * row.toFloat()
                    val color = if (videoMemory[row][col]) pixelOnColor else pixelOffColor
                    drawRect(color, topLeft = Offset(xx, yy), Size(blockSize, blockSize))
                }
            }
        }
    }
}

@Composable
fun Keypad(enableHaptics: Boolean, onKeyDown: (Int) -> Unit, onKeyUp: (Int) -> Unit) {
    val keys = listOf(1, 2, 3, 12, 4, 5, 6, 13, 7, 8, 9, 14, 10, 0, 11, 15)
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 42.dp)
    ) {
        items(keys) { key ->
            Key(key, enableHaptics, onKeyDown, onKeyUp)
        }
    }
}

@Composable
fun Key(number: Int, enableHaptics: Boolean, onKeyDown: (Int) -> Unit, onKeyUp: (Int) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onKeyDown(number)
                }

                is PressInteraction.Release -> onKeyUp(number)
            }
        }
    }
    OutlinedButton(
        interactionSource = interactionSource,
        border = BorderStroke(
            ButtonDefaults.OutlinedBorderSize,
            MaterialTheme.colors.primary.copy(alpha = ButtonDefaults.OutlinedBorderOpacity)
        ),
        onClick = { },
        shape = CircleShape,
        modifier = Modifier.size(94.dp).padding(8.dp)
    ) {
        Text(
            text = number.toUInt().toString(16).uppercase(Locale.ROOT),
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun PlaySound(toneGenerator: ToneGenerator, play: Boolean) {
    if (play) toneGenerator.startTone(ToneGenerator.TONE_SUP_RADIO_ACK)
}