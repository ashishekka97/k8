package me.ashishekka.k8.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface Keypad {
    var currentKey: ByteStore?

    var lastKey: ByteStore?

    fun onKeyClick(key: Int)

    fun onKeyLongPress(key: Int)
}

class KeypadImpl(private val coroutineScope: CoroutineScope) : Keypad {
    private var keyResetJob: Job? = null

    override var currentKey: ByteStore? = null

    override var lastKey: ByteStore? = null

    override fun onKeyClick(key: Int) {
        keyResetJob?.cancel()
        this.currentKey = checkIfValidKey(key)
        keyResetJob = coroutineScope.launch {
            delay(100)
            lastKey = currentKey
            currentKey = null
        }
    }

    override fun onKeyLongPress(key: Int) {
        keyResetJob?.cancel()
        this.currentKey = checkIfValidKey(key)
        keyResetJob = coroutineScope.launch {
            delay(500)
            lastKey = currentKey
            currentKey = null
        }
    }

    private fun checkIfValidKey(key: Int): ByteStore? {
        return key.takeIf { it in 0..15 }?.toUByte()
    }
}

enum class KeyEventType { CLICK, LONG }