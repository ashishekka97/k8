import androidx.compose.runtime.Composable
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.ashishekka.k8.core.Chip8
import me.ashishekka.k8.core.Chip8Impl
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Canvas
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.files.File
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.FileReader.Companion.DONE
import org.w3c.files.get

fun main() {
    val scope = CoroutineScope(Dispatchers.Main)
    val chip8 = Chip8Impl(scope)
    renderComposable(rootElementId = "root") {
        MainLayout(chip8)
    }
}

@Composable
fun MainLayout(chip8: Chip8) {
    val sound = chip8.getSoundState()
    PlaySound(sound.value)
    Div({
        style {
            property("text-align", "center")
            padding(1.em)
        }
    }) {
        H1 {
            Text("K8 (Kate) - Chip 8 emulator")
        }
        Div({
            style {
                property("text-align", "center")
                padding(1.em)
            }
        }) {
            Input(InputType.File) {
                id("file")
                onChange {
                    val fileInput = document.getElementById("file")
                    val files = fileInput.asDynamic().files.unsafeCast<FileList>()
                    console.log(files)
                    if (files.length != 0) {
                        files[0]?.let { readRomFile(it, chip8) }
                    }
                }
            }
        }
        Br { }
        Div {
            Screen(chip8)
        }
    }
}

fun Uint8Array.toByteArray(): ByteArray {
    val byteArray = ByteArray(length)
    for (i in 0 until length) {
        byteArray[i] = this[i]
    }
    return byteArray
}

fun readRomFile(file: File, chip8: Chip8) {
    val reader = FileReader()
    reader.readAsArrayBuffer(file)
    reader.onloadend = {
        if (it.target.asDynamic().readyState == DONE) {
            val arrayBuffer = it.target.asDynamic().result.unsafeCast<ArrayBuffer>()
            val byteArray = Uint8Array(arrayBuffer).toByteArray()
            console.log(byteArray)
            chip8.loadRom(byteArray)
            chip8.start()
        }
    }
}

@Composable
fun Screen(chip8: Chip8) {
    Canvas(
        attrs = {
            attr("id", "screen")
            attr("width", "640")
            attr("height", "320")
        }
    ) {
        val videoMemory = chip8.getVideoMemoryState()
        this.DomSideEffect {
            val blockSize = 10.0
            val ctx = it.getContext("2d").unsafeCast<CanvasRenderingContext2D>()
            with(ctx) {
                videoMemory.value.forEachIndexed { row, rowData ->
                    rowData.forEachIndexed { col, _ ->
                        beginPath()
                        val xx = blockSize * col.toDouble()
                        val yy = blockSize * row.toDouble()
                        fillStyle = if (videoMemory.value[row][col]) Color.white else Color.black
                        rect(xx, yy, blockSize, blockSize)
                        fill()
                        closePath()
                    }
                }
            }
        }
    }
}


@Composable
fun PlaySound(play: Boolean) {

}