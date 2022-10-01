package me.ashishekka.k8.core

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
     * Expose the VRAM updates as State
     */
    fun getVideoMemoryState(): State<VideoMemory>

    /**
     * Expose running state
     */
    fun isRunning(): Boolean
}

class Chip8Impl(romBytes: ByteArray? = null) : Chip8 {

    private val cpuClockHz: Long = 1000

    private val scope = MainScope()
    private var cpuClockJob: Job? = null

    private val memory = Memory(4096) { 0u }
    private val videoMemory = VideoMemory(32) { BooleanArray(64) }
    private val cpu: Cpu

    private val videoMemoryState = mutableStateOf(VideoMemory(32) { BooleanArray(64) })

    init {
        cpu = Cpu(memory, videoMemory) {
            videoMemoryState.value = it
        }
        if (romBytes != null) {
            loadRom(romBytes)
        }
    }


    override fun loadRom(romBytes: ByteArray) {
        romBytes.forEachIndexed { index, byte ->
            memory[index + 0x200] = byte.toUByte()
        }
        println(memory.print())
    }

    override fun start() {
        cpuClockJob = scope.launch {
            while (true) {
                delay(1000 / cpuClockHz)
                cpu.tick()
            }
        }
    }

    override fun getVideoMemoryState(): State<VideoMemory> {
        return videoMemoryState
    }

    override fun isRunning(): Boolean {
        return cpuClockJob?.isActive == true
    }
}