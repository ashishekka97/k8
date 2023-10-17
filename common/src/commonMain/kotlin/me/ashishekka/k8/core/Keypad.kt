package me.ashishekka.k8.core

interface Keypad {
    var currentKey: ByteStore?

    var isKeyPressed: Boolean

    fun onKeyDown(key: Int)

    fun onKeyUp(key: Int)
}

class KeypadImpl : Keypad {

    private val keyMap = (0..15).associateWith { it.toUByte() }

    override var currentKey: ByteStore? = null

    override var isKeyPressed: Boolean = false

    override fun onKeyDown(key: Int) {
        isKeyPressed = true
        currentKey = checkIfValidKey(key)
    }

    override fun onKeyUp(key: Int) {
        isKeyPressed = false
        currentKey = null
    }

    private fun checkIfValidKey(key: Int): ByteStore? {
        return keyMap[key]
    }
}

enum class KeyEventType { DOWN, UP }
