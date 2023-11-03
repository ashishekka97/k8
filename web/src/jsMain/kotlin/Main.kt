import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ashishekka.k8.configs.EmulatorSpeed
import me.ashishekka.k8.configs.ThemeColor
import me.ashishekka.k8.core.Chip8
import me.ashishekka.k8.core.Chip8Impl
import me.ashishekka.k8.core.KeyEventType.DOWN
import me.ashishekka.k8.core.KeyEventType.UP
import me.ashishekka.k8.storage.K8Settings
import me.ashishekka.k8.storage.KEY_SOUND
import me.ashishekka.k8.storage.KEY_SPEED
import me.ashishekka.k8.storage.KEY_THEME
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.CSSUnitValueTyped
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.gridTemplateRows
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.CheckboxInput
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.events.KeyboardEvent
import org.w3c.files.File
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.FileReader.Companion.DONE
import org.w3c.files.get

fun main() {
    val scope = CoroutineScope(Dispatchers.Unconfined)
    val chip8 = Chip8Impl(scope)
    window.onkeydown = { handleKey(it, chip8) }
    window.onkeypress = { handleKey(it, chip8) }
    window.onkeyup = { handleKey(it, chip8) }

    val gridColumnTemplate = columnTemplate()
    val gridRowTemplate = rowTemplate()

    // Need to find why we can't nest composables within renderComposable
    renderComposable(rootElementId = "root") {
        val soundState = chip8.getSoundState()
        val videoMemory = chip8.getVideoMemoryState()
        val settings = K8Settings()
        val storedTheme = settings.getInt(KEY_THEME)
        val storedSpeed = settings.getInt(KEY_SPEED)
        val storedSound = settings.getBoolean(KEY_SOUND)

        var sound by remember { mutableStateOf(storedSound) }
        var theme by remember {
            mutableStateOf(ThemeColor.getThemeFromIndex(storedTheme))
        }
        var speed by remember {
            mutableStateOf(EmulatorSpeed.getSpeedFromIndex(storedSpeed))
        }
        chip8.emulationSpeedFactor(speed.speedFactor)
        Div({
            style {
                textAlign("center")
                padding(1.em)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                justifyContent(JustifyContent.FlexStart)
                backgroundColor(theme.background.mapToCssColor())
                width(CSSUnitValueTyped(100f, CSSUnit.vw))
                height(CSSUnitValueTyped(100f, CSSUnit.vh))
            }
        }) {
            Div({
                style {
                    property("margin-left", "auto")
                    property("margin-right", "auto")
                }
            }) {
                H1({
                    style {
                        color(theme.foreground.mapToCssColor())
                    }
                }) {
                    Text("K8 (Kate) - Chip 8 emulator")
                }
            }
            Div({
                style {
                    width(64.vw)
                    property("margin-left", "auto")
                    property("margin-right", "auto")
                }
            }) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.SpaceBetween)
                        flexWrap(FlexWrap.Wrap)
                    }
                }) {
                    Div {
                        Button({
                            onClick {
                                val fileInput = document.getElementById("file")
                                val files = fileInput.asDynamic().files.unsafeCast<FileList>()
                                console.log(files)
                                if (files.length != 0) {
                                    files[0]?.let { readRomFile(it, chip8) }
                                }
                            }
                        }) {
                            Text("Reset")
                        }
                    }
                    Div {
                        Span({
                            style {
                                color(theme.foreground.mapToCssColor())
                            }
                        }) {
                            Text("ROM: ")
                        }
                        Input(InputType.File) {
                            style {
                                color(theme.foreground.mapToCssColor())
                            }
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
                    Div {
                        Span({
                            style {
                                color(theme.foreground.mapToCssColor())
                            }
                        }) {
                            Text("Emulate Sound: ")
                        }
                        CheckboxInput(checked = sound) {
                            onChange {
                                scope.launch {
                                    settings.setBooleanSetting(KEY_SOUND, it.value)
                                    sound = it.value
                                }
                            }
                        }
                    }
                    Div {
                        Span({
                            style {
                                color(theme.foreground.mapToCssColor())
                            }
                        }) {
                            Text("Theme: ")
                        }
                        Select({
                            onChange {
                                it.value?.let {
                                    val selectedTheme = ThemeColor.getThemeFromKey(it)
                                    scope.launch {
                                        settings.setIntSetting(
                                            KEY_THEME,
                                            selectedTheme.ordinal
                                        )
                                        theme = selectedTheme
                                    }
                                }
                            }
                        }, multiple = false) {
                            ThemeColor.values().mapIndexed { index, colorScheme ->
                                Option(colorScheme.schemeName, {
                                    if (index == theme.ordinal) selected()
                                }) {
                                    Text(colorScheme.schemeName)
                                }
                            }
                        }
                    }
                    Div {
                        Span({
                            style {
                                color(theme.foreground.mapToCssColor())
                            }
                        }) {
                            Text("Emulation Speed: ")
                        }
                        Select({
                            onChange {
                                it.value?.let { option ->
                                    val selectedSpeed = EmulatorSpeed.getSpeedFromKey(
                                        option.toFloat()
                                    )
                                    scope.launch {
                                        settings.setIntSetting(
                                            KEY_SPEED,
                                            selectedSpeed.ordinal
                                        )
                                        speed = selectedSpeed
                                    }
                                }
                            }
                        }, multiple = false) {
                            EmulatorSpeed.values().mapIndexed { index, emulatorSpeed ->
                                Option("${emulatorSpeed.speedFactor}", {
                                    if (index == speed.ordinal) selected()
                                }) {
                                    Text("${emulatorSpeed.speedFactor}x")
                                }
                            }
                        }
                    }
                }
            }
            Div({
                style {
                    property("margin-left", "auto")
                    marginTop(1.em)
                    property("margin-right", "auto")
                    marginBottom(1.em)
                    textAlign("center")
                    width(64.vw)
                    height(32.vw)
                    display(DisplayStyle.Grid)
                    gridTemplateColumns(gridColumnTemplate)
                    gridTemplateRows(gridRowTemplate)
                    border {
                        style = LineStyle.Solid
                        color = theme.foreground.mapToCssColor()
                        width = 2.px
                    }
                }
            }) {
                videoMemory.value.forEachIndexed { _, rowData ->
                    rowData.forEachIndexed { _, isOn ->
                        val color = if (isOn) {
                            theme.foreground.mapToCssColor()
                        } else {
                            theme.background.mapToCssColor()
                        }

                        val width = CSSUnitValueTyped(1f, CSSUnit.vw)
                        val height = CSSUnitValueTyped(1f, CSSUnit.vw)
                        Div({
                            style {
                                backgroundColor(color)
                                width(width)
                                height(height)
                            }
                        }) {}
                    }
                }
            }
            // TODO Add sound
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

fun handleKey(keyboardEvent: KeyboardEvent, chip8: Chip8) {
    console.log(keyboardEvent)
    val chip8Key = keyboardEvent.mapToChip8KeyPad()
    if (chip8Key !in 0..15) return
    when (keyboardEvent.type) {
        "keydown" -> chip8.onKey(chip8Key, DOWN)
        "keyup" -> chip8.onKey(chip8Key, UP)
        else -> Unit
    }
}

fun KeyboardEvent.mapToChip8KeyPad(): Int {
    return when (code) {
        "Digit1" -> 1
        "Digit2" -> 2
        "Digit3" -> 3
        "Digit4" -> 12
        "KeyQ" -> 4
        "KeyW" -> 5
        "KeyE" -> 6
        "KeyR" -> 13
        "KeyA" -> 7
        "KeyS" -> 8
        "KeyD" -> 9
        "KeyF" -> 14
        "KeyZ" -> 10
        "KeyX" -> 0
        "KeyC" -> 11
        "KeyV" -> 15
        else -> -1
    }
}

private fun columnTemplate(): String {
    val columnBuilder = StringBuilder()
    for (i in 0 until 64) {
        columnBuilder.append("1vw")
        if (i < 63) columnBuilder.append(" ")
    }
    return columnBuilder.toString()
}

private fun rowTemplate(): String {
    val rowBuilder = StringBuilder()
    for (i in 0 until 32) {
        rowBuilder.append("1vw")
        if (i < 31) rowBuilder.append(" ")
    }
    return rowBuilder.toString()
}

private fun Long.mapToCssColor(): CSSColorValue {
    val alpha = (this and 0xFF000000) shr 24
    val red = (this and 0x00FF0000) shr 16
    val green = (this and 0x0000FF00) shr 8
    val blue = this and 0x000000FF

    return rgba(red, green, blue, alpha)
}
