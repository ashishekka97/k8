@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.A
import androidx.compose.ui.input.key.Key.Companion.C
import androidx.compose.ui.input.key.Key.Companion.D
import androidx.compose.ui.input.key.Key.Companion.E
import androidx.compose.ui.input.key.Key.Companion.F
import androidx.compose.ui.input.key.Key.Companion.Four
import androidx.compose.ui.input.key.Key.Companion.One
import androidx.compose.ui.input.key.Key.Companion.Q
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.input.key.Key.Companion.S
import androidx.compose.ui.input.key.Key.Companion.Three
import androidx.compose.ui.input.key.Key.Companion.Two
import androidx.compose.ui.input.key.Key.Companion.V
import androidx.compose.ui.input.key.Key.Companion.W
import androidx.compose.ui.input.key.Key.Companion.X
import androidx.compose.ui.input.key.Key.Companion.Z
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ashishekka.k8.core.Chip8
import me.ashishekka.k8.core.Chip8Impl
import me.ashishekka.k8.core.KeyEventType
import me.ashishekka.k8.core.VideoMemory
import java.awt.Toolkit
import java.io.File
import java.nio.file.Paths
import java.util.*

fun main() = application {
    val scope = CoroutineScope(Dispatchers.Main)
    val chip8 = Chip8Impl(scope)

    readRomFile(scope, chip8)
    Window(
        onCloseRequest = ::exitApplication,
        title = "K8 (Kate) - Chip 8 Emulator",
        onKeyEvent = {
            val chip8Key = it.mapKeyboardToChip8KeyPad()
            if (chip8Key > -1) {
                when (it.type) {
                    KeyDown -> chip8.onKey(chip8Key, KeyEventType.DOWN)
                    KeyUp -> chip8.onKey(chip8Key, KeyEventType.UP)
                    else -> return@Window false
                }
                true
            } else false
        }
    ) {
        MaterialTheme {
            MainLayout(chip8)
        }
    }
    chip8.start()
}

fun readRomFile(scope: CoroutineScope, chip8: Chip8) {
    scope.launch(Dispatchers.IO) {
        val path = Paths.get("android/src/main/assets/c8games/INVADERS").toAbsolutePath().toString()
        val romFile = File(path)
        val romData = romFile.readBytes()
        chip8.loadRom(romData)
    }
}

@Composable
fun MainLayout(chip8: Chip8) {
    Scaffold {
        val sound = chip8.getSoundState()
        PlaySound(sound.value)
        Column(
            modifier = Modifier.fillMaxHeight().padding(it),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val videoMemory = chip8.getVideoMemoryState()
            Row { Screen(videoMemory.value) }
        }
    }
}

@Composable
fun Screen(videoMemory: VideoMemory) {
    BoxWithConstraints {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val blockSize = size.width / 64
            videoMemory.forEachIndexed { row, rowData ->
                rowData.forEachIndexed { col, _ ->
                    val xx = blockSize * col.toFloat()
                    val yy = blockSize * row.toFloat()
                    val color = if (videoMemory[row][col]) Color.White else Color.Black
                    drawRect(color, topLeft = Offset(xx, yy), Size(blockSize, blockSize))
                }
            }
        }
    }
}

fun KeyEvent.mapKeyboardToChip8KeyPad(): Int {
    return when (key) {
        One -> 1
        Two -> 2
        Three -> 3
        Four -> 12
        Q -> 4
        W -> 5
        E -> 6
        R -> 13
        A -> 7
        S -> 8
        D -> 9
        F -> 14
        Z -> 10
        X -> 0
        C -> 11
        V -> 15
        else -> -1
    }
}

@Composable
fun PlaySound(play: Boolean) {
    if (play) Toolkit.getDefaultToolkit().beep();
}