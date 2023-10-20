import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color
import me.ashishekka.k8.configs.ColorScheme

fun getThemeColors(scheme: ColorScheme = ColorScheme.GAMEBOY): Colors {
    return scheme.mapToColors()
}

private fun ColorScheme.mapToColors() = Colors(
    primary = Color(foreground),
    primaryVariant = Color(foreground),
    secondary = Color(foreground),
    secondaryVariant = Color(foreground),
    background = Color(background),
    surface = Color(background),
    error = Color(0xFFB00020),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White,
    isLight = false
)
