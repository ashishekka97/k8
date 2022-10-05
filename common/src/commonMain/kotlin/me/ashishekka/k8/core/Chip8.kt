package me.ashishekka.k8.core

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface Chip8 {
    /**
     * Loads the ROM into the memory starting at 0x200
     */
    fun loadRom(romBytes: ByteArray)

    /**
     * Starts emulation.
     */
    fun start()

    /**
     * Resets the system.
     */
    fun reset()

    /**
     * Expose the VRAM updates as State
     */
    fun getVideoMemoryState(): State<VideoMemory>

    /**
     * Expose the sound updates as State
     */
    fun getSoundState(): State<Boolean>

    /**
     * Expose running state
     */
    fun isRunning(): Boolean

    /**
     * Register keypad input
     */
    fun onKey(key: Int, type: KeyEventType)
}

class Chip8Impl(private val scope: CoroutineScope, romBytes: ByteArray? = null) : Chip8 {

    private val cpuClockHz: Long = 1000
    private val timerClockHz: Long = 60
    private var cpuClockJob: Job? = null
    private var timerJob: Job? = null

    private val memory = Memory(4096) { 0u }
    private val videoMemory = VideoMemory(32) { BooleanArray(64) }
    private val cpu: Cpu

    private val videoMemoryState = mutableStateOf(VideoMemory(32) { BooleanArray(64) }, neverEqualPolicy())
    private val soundState = mutableStateOf(false, neverEqualPolicy())

    private val keypad = KeypadImpl(scope)

    init {
        cpu = Cpu(memory, videoMemory, keypad) {
            videoMemoryState.value = it
        }
        if (romBytes != null) {
            loadRom(romBytes)
        }
    }


    override fun loadRom(romBytes: ByteArray) {
        reset()
        loadFontIntoMemory()
        romBytes.forEachIndexed { index, byte ->
            memory[index + 0x200] = byte.toUByte()
        }
        //println(memory.print())
    }

    override fun start() {
        cpuClockJob = scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000 / cpuClockHz)
                cpu.tick()
            }
        }

        timerJob = scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000 / timerClockHz)
                if (cpu.DT > 0u) {
                    cpu.DT--
                }
                if (cpu.ST > 0u)  {
                    cpu.ST--
                    if (!soundState.value) soundState.value = true
                } else {
                    soundState.value = false
                }
            }
        }
    }

    override fun reset() {
        cpuClockJob?.cancel()
        timerJob?.cancel()
        cpu.reset()
        memory.clear()
        videoMemory.clear()
        videoMemoryState.value = videoMemory
        soundState.value = false
    }

    override fun getVideoMemoryState(): State<Array<BooleanArray>> {
        return videoMemoryState
    }

    override fun getSoundState(): State<Boolean> {
        return soundState
    }

    override fun isRunning(): Boolean {
        return cpuClockJob?.isActive == true
    }

    override fun onKey(key: Int, type: KeyEventType) {
        when (type) {
            KeyEventType.DOWN -> keypad.onKeyDown(key)
            KeyEventType.UP -> keypad.onKeyUp(key)
        }
    }

    private fun loadFontIntoMemory() {
        // Load the FONT_SPRITE into the memory (anywhere b/w 000 to 1FF, popularly 050-09f)
        FONT_SPRITES.forEachIndexed { index, byte ->
            memory[FONT_START + index] = byte
        }
    }
}