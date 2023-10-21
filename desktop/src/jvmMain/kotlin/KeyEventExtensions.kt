import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key

fun KeyEvent.mapKeyboardToChip8KeyPad(): Int {
    return when (key) {
        Key.One -> 1
        Key.Two -> 2
        Key.Three -> 3
        Key.Four -> 12
        Key.Q -> 4
        Key.W -> 5
        Key.E -> 6
        Key.R -> 13
        Key.K -> 7
        Key.S -> 8
        Key.D -> 9
        Key.F -> 14
        Key.Z -> 10
        Key.X -> 0
        Key.C -> 11
        Key.V -> 15
        else -> -1
    }
}
