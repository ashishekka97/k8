package me.ashishekka.k8.android.theming

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

fun defaultColors(
    primary: Color = Color(0xFFe3e8ea),
    primaryVariant: Color = Color(0xFFe3e8ea),
    secondary: Color = Color(0xFFe3e8ea),
    secondaryVariant: Color = Color(0xFFe3e8ea),
    background: Color = Color(0xFF495054),
    surface: Color = Color(0xFF495054),
    error: Color = Color(0xFFB00020),
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.Black,
    onBackground: Color = Color.Black,
    onSurface: Color = Color.Black,
    onError: Color = Color.White
): Colors = Colors(
    primary,
    primaryVariant,
    secondary,
    secondaryVariant,
    background,
    surface,
    error,
    onPrimary,
    onSecondary,
    onBackground,
    onSurface,
    onError,
    false
)

fun terminalColors(
    primary: Color = Color(0xFF00ff00),
    primaryVariant: Color = Color(0xFF00ff00),
    secondary: Color = Color(0xFF00ff00),
    secondaryVariant: Color = Color(0xFF00ff00),
    background: Color = Color(0xFF00270d),
    surface: Color = Color(0xFF00270d),
    error: Color = Color(0xFFB00020),
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.Black,
    onBackground: Color = Color.Black,
    onSurface: Color = Color.Black,
    onError: Color = Color.White
): Colors = Colors(
    primary,
    primaryVariant,
    secondary,
    secondaryVariant,
    background,
    surface,
    error,
    onPrimary,
    onSecondary,
    onBackground,
    onSurface,
    onError,
    false
)

fun gameboyColors(
    primary: Color = Color(0xFF0f380f),
    primaryVariant: Color = Color(0xFF0f380f),
    secondary: Color = Color(0xFF0f380f),
    secondaryVariant: Color = Color(0xFF0f380f),
    background: Color = Color(0xff9bbc0f),
    surface: Color = Color(0xff9bbc0f),
    error: Color = Color(0xFFB00020),
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.Black,
    onBackground: Color = Color.Black,
    onSurface: Color = Color.Black,
    onError: Color = Color.White
): Colors = Colors(
    primary,
    primaryVariant,
    secondary,
    secondaryVariant,
    background,
    surface,
    error,
    onPrimary,
    onSecondary,
    onBackground,
    onSurface,
    onError,
    false
)

fun iceColors(
    primary: Color = Color(0xFFcfe2f3),
    primaryVariant: Color = Color(0xFFcfe2f3),
    secondary: Color = Color(0xFFcfe2f3),
    secondaryVariant: Color = Color(0xFFcfe2f3),
    background: Color = Color(0xff365673),
    surface: Color = Color(0xff365673),
    error: Color = Color(0xFFB00020),
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.Black,
    onBackground: Color = Color.Black,
    onSurface: Color = Color.Black,
    onError: Color = Color.White
): Colors = Colors(
    primary,
    primaryVariant,
    secondary,
    secondaryVariant,
    background,
    surface,
    error,
    onPrimary,
    onSecondary,
    onBackground,
    onSurface,
    onError,
    false
)

fun khakiColors(
    primary: Color = Color(0xFFd5ddcf),
    primaryVariant: Color = Color(0xFFd5ddcf),
    secondary: Color = Color(0xFFd5ddcf),
    secondaryVariant: Color = Color(0xFFd5ddcf),
    background: Color = Color(0xff6f7769),
    surface: Color = Color(0xff6f7769),
    error: Color = Color(0xFFB00020),
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.Black,
    onBackground: Color = Color.Black,
    onSurface: Color = Color.Black,
    onError: Color = Color.White
): Colors = Colors(
    primary,
    primaryVariant,
    secondary,
    secondaryVariant,
    background,
    surface,
    error,
    onPrimary,
    onSecondary,
    onBackground,
    onSurface,
    onError,
    false
)

fun c64Colors(
    primary: Color = Color(0xFF7664d9),
    primaryVariant: Color = Color(0xFF7664d9),
    secondary: Color = Color(0xFF7664d9),
    secondaryVariant: Color = Color(0xFF7664d9),
    background: Color = Color(0xff36209b),
    surface: Color = Color(0xff36209b),
    error: Color = Color(0xFFB00020),
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.Black,
    onBackground: Color = Color.Black,
    onSurface: Color = Color.Black,
    onError: Color = Color.White
): Colors = Colors(
    primary,
    primaryVariant,
    secondary,
    secondaryVariant,
    background,
    surface,
    error,
    onPrimary,
    onSecondary,
    onBackground,
    onSurface,
    onError,
    false
)

fun getThemeColors(scheme: ColorScheme = ColorScheme.GAMEBOY): Colors {
    return when (scheme) {
        ColorScheme.DEFAULT -> defaultColors()
        ColorScheme.TERMINAL -> terminalColors()
        ColorScheme.GAMEBOY -> gameboyColors()
        ColorScheme.ICE -> iceColors()
        ColorScheme.KHAKI -> khakiColors()
        ColorScheme.C64 -> c64Colors()
    }
}

enum class ColorScheme(private val theme: String) {
    DEFAULT("default"),
    TERMINAL("terminal"),
    GAMEBOY("gameboy"),
    ICE("ice"),
    KHAKI("khaki"),
    C64("c64");

    companion object {
        fun getAllThemes() = values().map { it.theme }
        fun getThemeFromIndex(index: Int) = try {
            values()[index]
        } catch (ex: Throwable) {
            DEFAULT
        }
    }
}