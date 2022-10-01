package me.ashishekka.k8.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import me.ashishekka.k8.core.Chip8
import me.ashishekka.k8.core.Chip8Impl
import me.ashishekka.k8.core.VideoMemory

class MainActivity : AppCompatActivity() {

    private val chip8: Chip8 by lazy { Chip8Impl() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val romFile = assets.open("IBM_logo.ch8")
        val romData = romFile.readBytes()
        val emulator = findViewById<ComposeView>(R.id.emulator)
        chip8.loadRom(romData)
        emulator?.setContent {
            MaterialTheme { // or AppCompatTheme
                MainLayout(chip8)
            }
        }
        chip8.start()
    }
}

@Composable
fun MainLayout(chip8: Chip8) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("K8 (Kate)") }) },
    ) {
        Column(
            modifier = Modifier.padding(it).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val videoMemory = chip8.getVideoMemoryState()
            Screen(videoMemory.value)
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