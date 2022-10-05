package me.ashishekka.k8.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val AVERAGE_KEY_RESET_DELAY_MILLIS = 85L

interface Keypad {
    var currentKey: ByteStore?

    var isKeyPressed: Boolean

    fun onKeyDown(key: Int)

    fun onKeyUp(key: Int)
}

class KeypadImpl(private val coroutineScope: CoroutineScope) : Keypad {

    private var resetKeyPressJob: Job? = null

    override var currentKey: ByteStore? = null

    override var isKeyPressed: Boolean = false

    override fun onKeyDown(key: Int) {
        isKeyPressed = true
        currentKey = checkIfValidKey(key)
        resetKeyPressJob?.cancel()
        resetKeyPressJob = coroutineScope.launch(Dispatchers.Default) {
            delay(AVERAGE_KEY_RESET_DELAY_MILLIS)
            currentKey = null
        }
    }

    override fun onKeyUp(key: Int) {
        isKeyPressed = false
    }

    private fun checkIfValidKey(key: Int): ByteStore? {
        return key.takeIf { it in 0..15 }?.toUByte()
    }
}

enum class KeyEventType { DOWN, UP }