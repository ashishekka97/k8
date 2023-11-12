package me.ashishekka.k8.core

import kotlin.random.Random

const val PC_START = 0x200
const val FONT_START = 0x000

class Cpu(
    private val memory: Memory = Memory(4096) { 0u },
    private val videoMemory: VideoMemory = VideoMemory(32) { BooleanArray(64) },
    private val keypad: Keypad,
    private val system: System = System.CHIP8,
    private val onDrawn: ((VideoMemory) -> Unit)? = null
) {
    // Program counter, starts at 0x200
    var PC = PC_START

    // 16 Bit Index register
    var I = 0

    // Stack
    val stack = Stack()

    // 8-bit Delay timer
    var DT: ByteStore = 0U

    // 8-bit Sound timer
    var ST: ByteStore = 0u

    // 16 8-bit General purpose variable registers
    val V = Array<ByteStore>(16) { 0u }

    private val instructionSet: InstructionSet

    init {
        instructionSet = loadInstructions()
    }

    fun tick() {
        val (firstByte, secondByte) = fetch()
        val operation = decode(firstByte)
        operation?.execute(firstByte, secondByte)
    }

    fun reset() {
        PC = PC_START
        I = 0
        stack.clear()
        DT = 0u
        ST = 0u
        V.forEachIndexed { index, _ -> V[index] = 0u }
    }

    private fun fetch(): Pair<ByteStore, ByteStore> {
        val firstByte = memory[PC++]
        val secondByte = memory[PC++]
        return Pair(firstByte, secondByte)
    }

    private fun decode(firstByte: ByteStore): Operation? {
        return instructionSet[firstByte.upperNibble()]
    }

    private fun Operation.execute(firstByte: ByteStore, secondByte: ByteStore) {
        invoke(
            Operands(firstByte.lowerNibble(), secondByte.upperNibble(), secondByte.lowerNibble())
        )
    }

    private fun zero(operands: Operands) {
        when (operands.toByteStore().toInt()) {
            0xE0 -> cls(operands)
            0xEE -> ret(operands)
            else -> bin(operands)
        }
    }

    private fun cls(operands: Operands) {
        videoMemory.clear()
        onDrawn?.invoke(videoMemory)
    }

    private fun ret(operands: Operands) {
        if (stack.isNotEmpty()) {
            PC = stack.removeLast().toInt()
        }
    }

    private fun bin(operands: Operands) {
        // Execute machine code. No need to emulate for now.
        PC -= 2
    }

    private fun jmp(operands: Operands) {
        PC = operands.toInt()
    }

    private fun func(operands: Operands) {
        stack.add(PC.toUShort())
        PC = operands.toInt()
    }

    private fun skipXN(operands: Operands) {
        if (V[operands.x().toInt()] == operands.toByteStore()) {
            PC += 2
        }
    }

    private fun skipXnotN(operands: Operands) {
        if (V[operands.x().toInt()] != operands.toByteStore()) {
            PC += 2
        }
    }

    private fun skipXY(operands: Operands) {
        if (V[operands.x().toInt()] == V[operands.y().toInt()]) {
            PC += 2
        }
    }

    private fun setVx(operands: Operands) {
        val registerIndex = operands.x().toInt()
        V[registerIndex] = operands.toByteStore()
    }

    private fun addVx(operands: Operands) {
        val registerIndex = operands.x().toInt()
        V[registerIndex] = (V[registerIndex] + operands.toByteStore()).toByteStore()
    }

    private fun arithmetics(operands: Operands) {
        val (x, y, n) = Triple(operands.x().toInt(), operands.y().toInt(), operands.n().toInt())
        when (n) {
            0x0 -> V[x] = V[y]
            0x1 -> {
                V[x] = V[x] or V[y]
                if (system.has(Quirk.VF_RESET)) {
                    V[0xF] = 0u
                }
            }
            0x2 -> {
                V[x] = V[x] and V[y]
                if (system.has(Quirk.VF_RESET)) {
                    V[0xF] = 0u
                }
            }
            0x3 -> {
                V[x] = V[x] xor V[y]
                if (system.has(Quirk.VF_RESET)) {
                    V[0xF] = 0u
                }
            }
            0x4 -> {
                val result = V[x].toInt() + V[y].toInt()
                V[0xF] = if (result > 0xFF) 1u else 0u
                V[x] = result.toUByte()
            }
            0x5 -> {
                val result = V[x].toInt() - V[y].toInt()
                V[0xF] = if (result < 0) 0u else 1u
                V[x] = result.toUByte()
            }
            0x6 -> {
                if (!system.has(Quirk.SHIFTING)) {
                    V[x] = V[y]
                }
                val shiftedBit = (V[x] and 0x01u)
                V[x] = (V[x].toInt() shr 1).toUByte()
                V[0xF] = shiftedBit
            }
            0x7 -> {
                val result = V[y].toInt() - V[x].toInt()
                V[0xF] = if (result < 0) 0u else 1u
                V[x] = result.toUByte()
            }
            0xE -> {
                if (!system.has(Quirk.SHIFTING)) {
                    V[x] = V[y]
                }
                V[x] = (V[x].toInt() shl 1).toUByte()
                V[0xF] = (V[x].toInt() shr 7).toUByte()
            }
        }
    }

    private fun skipXnotY(operands: Operands) {
        if (V[operands.x().toInt()] != V[operands.y().toInt()]) {
            PC += 2
        }
    }

    private fun setI(operands: Operands) {
        I = operands.toInt()
    }

    private fun jmpOff(operands: Operands) {
        val plusHend = if (system.has(Quirk.JUMPING)) V[operands.x().toInt()] else V[0]
        PC = operands.toInt() + plusHend.toInt()
    }

    private fun rnd(operands: Operands) {
        val num = Random.nextInt()
        V[operands.x().toInt()] = num.toUByte() and operands.toByteStore()
    }

    private fun draw(operands: Operands) {
        val x = V[operands.x().toInt()].toInt() % 64
        val y = V[operands.y().toInt()].toInt() % 32
        val n = operands.n().toInt()
        val shouldClip = system.has(Quirk.CLIPPING)
        V[0xF] = 0u
        for (row in 0 until n) {
            val yCoordinate = if (shouldClip) { y + row } else { (y + row) % 32 }
            if (yCoordinate >= 32 && shouldClip) break
            val sprite = memory[I + row]

            // 8 is the sprite's width
            for (col in 0 until 8) {
                val xCoordinate = if (shouldClip) { x + col } else { (x + col) % 64 }
                if (xCoordinate >= 64 && shouldClip) break
                val spritePixelState = ((sprite.toInt() shr (7 - col)) and 1) == 1
                val oldState = videoMemory[yCoordinate][xCoordinate]
                val newState = oldState xor spritePixelState
                V[0xF] = V[0xF] or (spritePixelState and oldState).toUByte()
                videoMemory[yCoordinate][xCoordinate] = newState
            }
        }
        onDrawn?.invoke(videoMemory)
    }

    private fun skipKey(operands: Operands) {
        val x = operands.x().toInt()
        val mode = operands.toByteStore().toInt()
        val key = keypad.currentKey
        val result = when (mode) {
            0x9E -> V[x] == key
            0xA1 -> V[x] != key
            else -> false
        }
        if (result) PC += 2
    }

    private fun others(operands: Operands) {
        val x = operands.x().toInt()
        when (operands.toByteStore().toInt()) {
            0x07 -> V[x] = DT
            0x0A -> {
                // TODO Handle OG (non-modern) case
                val key = keypad.currentKey
                val keyPressed = keypad.isKeyPressed
                if (key != null) {
                    if (!keyPressed) V[x] = key
                } else {
                    PC -= 2
                }
            }
            0x15 -> DT = V[x]
            0x18 -> ST = V[x]
            0x1E -> {
                val result = V[x].toInt() + I
                if (result > 0x0FFF) {
                    V[0xF] = 1u
                }
                I = result
            }
            0x29 -> {
                I = (V[x].toInt() * 5)
            }
            0x33 -> {
                val hexNum = V[x].toInt()
                memory[I] = (hexNum / 100).toUByte()
                memory[I + 1] = ((hexNum / 10) % 10).toUByte()
                memory[I + 2] = ((hexNum % 100) % 10).toUByte()
            }
            0x55 -> {
                for (i in 0..x) {
                    memory[I + i] = V[i]
                }
                if (system.has(Quirk.MEMORY)) {
                    I += x + 1
                }
            }
            0x65 -> {
                for (i in 0..x) {
                    V[i] = memory[I + i]
                }
                if (system.has(Quirk.MEMORY)) {
                    I += x + 1
                }
            }
        }
    }

    private fun loadInstructions(): HashMap<ByteStore, Operation> {
        return hashMapOf(
            Pair(0x0u, ::zero),
            Pair(0x1u, ::jmp),
            Pair(0x2u, ::func),
            Pair(0x3u, ::skipXN),
            Pair(0x4u, ::skipXnotN),
            Pair(0x5u, ::skipXY),
            Pair(0x6u, ::setVx),
            Pair(0x7u, ::addVx),
            Pair(0x8u, ::arithmetics),
            Pair(0x9u, ::skipXnotY),
            Pair(0xAu, ::setI),
            Pair(0xBu, ::jmpOff),
            Pair(0xCu, ::rnd),
            Pair(0xDu, ::draw),
            Pair(0xEu, ::skipKey),
            Pair(0xFu, ::others)
        )
    }
}

enum class Quirk {
    VF_RESET,
    MEMORY,
    DISPLAY_WAIT, // Not implemented
    CLIPPING,
    SHIFTING,
    JUMPING
}
