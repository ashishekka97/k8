import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.awt.Toolkit
import me.ashishekka.k8.core.Chip8
import me.ashishekka.k8.core.VideoMemory

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
    val foreground = MaterialTheme.colors.primary
    val background = MaterialTheme.colors.background
    BoxWithConstraints {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val blockSize = size.width / 128
            videoMemory.forEachIndexed { row, rowData ->
                rowData.forEachIndexed { col, _ ->
                    val xx = blockSize * col.toFloat()
                    val yy = blockSize * row.toFloat()
                    val color = if (videoMemory[row][col]) foreground else background
                    drawRect(color, topLeft = Offset(xx, yy), Size(blockSize, blockSize))
                }
            }
        }
    }
}

@Composable
fun PlaySound(play: Boolean) {
    if (play) Toolkit.getDefaultToolkit().beep()
}
