package me.ashishekka.k8.core

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlin.math.roundToLong
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

interface Chip8 {
    /**
     * Loads the ROM into the memory starting at 0x200
     */
    fun loadRom(romBytes: ByteArray): Boolean

    /**
     * Starts emulation.
     */
    fun start()

    fun pause()

    fun resume()

    /**
     * Resets the system.
     */
    fun reset()

    /**
     * Expose the VRAM updates as State
     */
    fun getVideoMemoryState(): State<VideoMemory>

    /**
     * Expose the VRAM updates as flow
     */
    @NativeCoroutines
    fun getVideoMemoryFlow(): Flow<VideoMemory>

    /**
     * Expose the sound updates as State
     */
    fun getSoundState(): State<Boolean>

    /**
     * Expose the sound updates sas flow
     */
    @NativeCoroutines
    fun getSoundFlow(): Flow<Boolean>

    /**
     * Expose running state
     */
    fun isRunning(): Boolean

    /**
     * Register keypad input
     */
    fun onKey(key: Int, type: KeyEventType)

    fun emulationSpeedFactor(factor: Float)

    fun toggleSound(isEnabled: Boolean)
}

class Chip8Impl(
    @NativeCoroutineScope val scope: CoroutineScope,
    romBytes: ByteArray? = null
) : Chip8 {

    constructor() : this(scope = CoroutineScope(SupervisorJob() + Dispatchers.Default))

    private val cpuClockHz: Long = 960
    private var speedFactor: Float = 1.0f
    private var soundEnabled: Boolean = true
    private val timerClockHz: Long = 60
    private var cpuClockJob: Job? = null
    private var timerJob: Job? = null

    private val atomicScreenUpdate: AtomicBoolean = atomic(false)
    private val atomicSoundUpdate: AtomicBoolean = atomic(false)

    private val memory = Memory(4096) { 0u }
    private val videoMemory = VideoMemory(64) { BooleanArray(128) }
    private val cpu: Cpu

    private val videoMemoryState =
        mutableStateOf(VideoMemory(64) { BooleanArray(128) }, neverEqualPolicy())
    private val soundState = mutableStateOf(false, neverEqualPolicy())

    private val keypad = KeypadImpl()

    private var isPaused: Boolean = false

    init {
        cpu = Cpu(memory, videoMemory, keypad) {
            videoMemoryState.value = it
            atomicScreenUpdate.compareAndSet(expect = false, update = true)
        }
        if (romBytes != null) {
            loadRom(romBytes)
        }
    }

    override fun loadRom(romBytes: ByteArray): Boolean {
        return try {
            reset()
            loadFontIntoMemory()
            romBytes.forEachIndexed { index, byte ->
                memory[index + 0x200] = byte.toUByte()
            }
            true
        } catch (ex: Throwable) {
            false
        }
    }

    override fun start() {
        cpuClockJob = scope.launch(Dispatchers.Default) {
            while (true) {
                val delayInterval = ((960 / speedFactor) / cpuClockHz).roundToLong()
                delay(delayInterval)
                if (!isPaused) cpu.tick()
            }
        }

        timerJob = scope.launch(Dispatchers.Default) {
            while (true) {
                val delayInterval = ((960 / speedFactor) / timerClockHz).roundToLong()
                cpu.releaseInterrupts()
                delay(delayInterval)
                if (cpu.DT > 0u) {
                    cpu.DT--
                }
                if (cpu.ST > 0u) {
                    cpu.ST--
                    soundState.value = true
                } else {
                    soundState.value = false
                }
                atomicSoundUpdate.compareAndSet(expect = false, update = true)
            }
        }
    }

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }

    override fun reset() {
        cpuClockJob?.cancel()
        timerJob?.cancel()
        cpu.reset()
        memory.clear()
        videoMemory.clear()
    }

    override fun getVideoMemoryState(): State<Array<BooleanArray>> {
        return videoMemoryState
    }

    override fun getVideoMemoryFlow(): Flow<VideoMemory> {
        return flow {
            while (true) {
                if (atomicScreenUpdate.value) {
                    emit(videoMemory)
                    atomicScreenUpdate.compareAndSet(expect = true, update = false)
                } else {
                    delay(16)
                }
            }
        }
    }

    override fun getSoundState(): State<Boolean> {
        return derivedStateOf { soundState.value && soundEnabled }
    }

    override fun getSoundFlow(): Flow<Boolean> {
        return flow {
            while (true) {
                if (atomicSoundUpdate.value) {
                    emit(value = soundState.value && soundEnabled)
                    atomicSoundUpdate.compareAndSet(expect = true, update = false)
                } else {
                    delay(16)
                }
            }
        }
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

    override fun emulationSpeedFactor(factor: Float) {
        speedFactor = factor
    }

    override fun toggleSound(isEnabled: Boolean) {
        soundEnabled = isEnabled
    }

    private fun loadFontIntoMemory() {
        // Load the FONT_SPRITE into the memory (anywhere b/w 000 to 1FF, popularly 050-09f)
        FONT_SPRITES.forEachIndexed { index, byte ->
            memory[FONT_START + index] = byte
        }
    }
}

// TODO -> Quirks are to be separated from platform, and tied to individual games.
//  More info here: https://github.com/chip-8/chip-8-database
enum class System(val quirks: Set<Quirk>) {
    CHIP8(setOf(Quirk.VF_RESET, Quirk.MEMORY, Quirk.DISPLAY_WAIT, Quirk.CLIPPING)),
    SUPERCHIP_LEGACY(setOf(Quirk.DISPLAY_WAIT, Quirk.CLIPPING, Quirk.SHIFTING, Quirk.JUMPING)),
    SUPERCHIP_MODERN(setOf(Quirk.CLIPPING, Quirk.SHIFTING, Quirk.JUMPING)),
    XOCHIP(setOf(Quirk.MEMORY));

    fun has(quirk: Quirk): Boolean {
        return quirks.contains(quirk)
    }
}
