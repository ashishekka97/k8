package me.ashishekka.k8.configs

enum class ColorScheme(
    val schemeName: String,
    val background: Long,
    val foreground: Long
) {
    DEFAULT("default", background = 0xFF495054, foreground = 0xFFe3e8ea),
    TERMINAL("terminal", background = 0xFF00270d, foreground = 0xFF00ff00),
    GAMEBOY("gameboy", background = 0xff9bbc0f, foreground = 0xFF0f380f),
    ICE("ice", background = 0xff365673, foreground = 0xFFcfe2f3),
    KHAKI("khaki", background = 0xff6f7769, foreground = 0xFFd5ddcf),
    C64("c64", background = 0xff36209b, foreground = 0xFF7664d9);

    companion object {
        fun getAllThemes() = values().map { it.schemeName }
        fun getThemeFromIndex(index: Int) = try {
            values()[index]
        } catch (ex: Throwable) {
            DEFAULT
        }
    }
}
