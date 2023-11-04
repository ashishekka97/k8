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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
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
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ashishekka.k8.android.theming.fullScaffoldBackground
import me.ashishekka.k8.android.theming.getThemeColors
import me.ashishekka.k8.configs.ThemeColor
import me.ashishekka.k8.core.VideoMemory
import me.ashishekka.k8.storage.KEY_HAPTICS
import me.ashishekka.k8.storage.KEY_THEME

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy { MainViewModel() }

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
            val customRomUri = result.data?.getStringExtra(CUSTOM_ROM_URI)?.toUri()
            if (romPath != null) {
                viewModel.readRomFromAssets(this, romPath)
            } else if (customRomUri != null) {
                viewModel.readCustomRomFromUri(this, customRomUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        viewModel.observeUiState()
        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            val themeState = viewModel.settings.getIntFlowSetting(KEY_THEME).collectAsState(
                initial = 0
            )
            val theme = ThemeColor.getThemeFromIndex(themeState.value)
            val hapticState = viewModel.settings.getBooleanFlowSetting(KEY_HAPTICS).collectAsState(
                initial = false
            )
            maybeShowSnackbar(viewModel.uiState.value.snackMessage) {
                scope.launch {
                    snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                }
            }
            MaterialTheme(colors = getThemeColors(theme)) { // or AppCompatTheme
                MainLayout(
                    videoMemory = viewModel.videoMemory,
                    soundState = viewModel.soundState,
                    toneGenerator = toneGenerator,
                    hapticsState = hapticState,
                    snackbarHostState = snackbarHostState,
                    onGameKeyDown = viewModel::onGameKeyDown,
                    onGameKeyUp = viewModel::onGameKeyUp,
                    onLoadGameClick = ::launchRomPicker,
                    onGameResetClick = { viewModel.resetRom(this) },
                    onSettingsClick = ::launchSettings
                )
            }
        }
        viewModel.readRomFromAssets(this, "chip8-test-suite.ch8")
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
fun maybeShowSnackbar(
    snackMessage: SnackMessage?,
    show: (message: String) -> Unit
) {
    if (snackMessage != null) {
        show(snackMessage.message)
    }
}

@Composable
fun MainLayout(
    videoMemory: State<VideoMemory>,
    soundState: State<Boolean>,
    toneGenerator: ToneGenerator,
    hapticsState: State<Boolean>,
    snackbarHostState: SnackbarHostState,
    onGameKeyDown: (Int) -> Unit,
    onGameKeyUp: (Int) -> Unit,
    onLoadGameClick: () -> Unit,
    onGameResetClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.background(
            fullScaffoldBackground(
                color = MaterialTheme.colors.primarySurface,
                elevationOverlay = LocalElevationOverlay.current
            )
        ).safeDrawingPadding(),
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    data,
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.background
                )
            }
        }
    ) {
        PlaySound(toneGenerator, soundState.value)
        Column(
            modifier = Modifier.fillMaxHeight().padding(it),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row { Screen(videoMemory.value) }
            Row { Keypad(hapticsState, onGameKeyDown, onGameKeyUp) }
        }
    }
}

@Composable
fun Screen(videoMemory: VideoMemory) {
    val pixelOffColor = MaterialTheme.colors.background
    val pixelOnColor = MaterialTheme.colors.primary
    BoxWithConstraints(
        modifier = Modifier.padding(top = 16.dp).border(
            BorderStroke(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.6f))
        ).padding(4.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 320.dp, height = 160.dp)) {
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
fun Keypad(hapticsState: State<Boolean>, onKeyDown: (Int) -> Unit, onKeyUp: (Int) -> Unit) {
    val keys = listOf(1, 2, 3, 12, 4, 5, 6, 13, 7, 8, 9, 14, 10, 0, 11, 15)
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        items(keys) { key ->
            Key(key, hapticsState, onKeyDown, onKeyUp)
        }
    }
}

@Composable
fun Key(
    number: Int,
    hapticsState: State<Boolean>,
    onKeyDown: (Int) -> Unit,
    onKeyUp: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    if (hapticsState.value) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onKeyDown(number)
                }

                is PressInteraction.Release -> onKeyUp(number)
            }
        }
    }
    OutlinedButton(
        interactionSource = interactionSource,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colors.primary.copy(alpha = 0.6f)
        ),
        onClick = { },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = number.toUInt().toString(16).uppercase(Locale.ROOT),
            style = MaterialTheme.typography.h4
        )
    }
}

@Composable
fun PlaySound(toneGenerator: ToneGenerator, play: Boolean) {
    if (play) toneGenerator.startTone(ToneGenerator.TONE_SUP_RADIO_ACK)
}
