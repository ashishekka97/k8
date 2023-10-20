package me.ashishekka.k8.android.theming

import androidx.compose.material.Colors
import androidx.compose.material.ElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

@Composable
fun fullScaffoldBackground(
    color: Color,
    elevationOverlay: ElevationOverlay?
): Color {
    return if (color == MaterialTheme.colors.surface && elevationOverlay != null) {
        elevationOverlay.apply(color, 4.dp)
    } else {
        color
    }
}
