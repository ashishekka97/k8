package me.ashishekka.k8.core

import kotlin.random.Random

const val PC_START = 0x200
const val FONT_START = 0x050

class Cpu(
    private val memory: Memory = Memory(4096) { 0u },
    private val videoMemory: VideoMemory = VideoMemory(32) { BooleanArray(64) },
    private val isModern: Boolean = true,
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

    private var lastKey: ByteStore? = null
    private val key: ByteStore? = null

    private val instructionSet: InstructionSet

    init {
        // Load the FONT_SPRITE into the memory (anywhere b/w 000 to 1FF, popularly 050-09f)
        FONT_SPRITES.forEachIndexed { index, byte ->
            memory[FONT_START + index] = byte
        }
        instructionSet = loadInstructions()
    }

    fun tick() {
        val (firstByte, secondByte) = fetch()
        val operation = decode(firstByte)
        operation?.execute(firstByte, secondByte)
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
        invoke(Operands(firstByte.lowerNibble(), secondByte.upperNibble(), secondByte.lowerNibble()))
    }

    private fun zero(operands: Operands) {
        when (operands.toByteStore().toInt()) {
            0xE0 -> cls(operands)
            0xEE -> ret(operands)
            else -> bin(operands)
        }
    }

    fun cls(operands: Operands) {
        videoMemory.forEachIndexed { y, row ->
            row.forEachIndexed { x, _ ->
                videoMemory[y][x] = false
            }
        }
        onDrawn?.invoke(videoMemory)
    }

    fun ret(operands: Operands) {
        if (stack.isNotEmpty()) {
            PC = stack.removeLast().toInt()
        }
    }

    fun bin(operands: Operands) {
        // Execute machine code. No need to emulate for now.
        PC -= 2
    }

    fun jmp(operands: Operands) {
        PC = operands.toInt()
    }

    fun func(operands: Operands) {
        stack.add(PC.toUShort())
        PC = operands.toInt() - 2
    }

    fun skipXN(operands: Operands) {
        if (V[operands.x().toInt()] == operands.toByteStore()) {
            PC += 2
        }
    }

    fun skipXnotN(operands: Operands) {
        if (V[operands.x().toInt()] != operands.toByteStore()) {
            PC += 2
        }
    }

    fun skipXY(operands: Operands) {
        if (V[operands.x().toInt()] == V[operands.y().toInt()]) {
            PC += 2
        }
    }

    fun setVx(operands: Operands) {
        val registerIndex = operands.x().toInt()
        V[registerIndex] = operands.toByteStore()
    }

    fun addVx(operands: Operands) {
        val registerIndex = operands.x().toInt()
        V[registerIndex] = (V[registerIndex] + operands.toByteStore()).toByteStore()

    }

    fun arithmetics(operands: Operands) {
        val (x, y, n) = Triple(operands.x().toInt(), operands.y().toInt(), operands.n().toInt())
        when (n) {
            0 -> V[x] = V[y]
            1 -> V[x] = V[x] or V[y]
            2 -> V[x] = V[x] and V[y]
            3 -> V[x] = V[x] xor V[y]
            4 -> {
                val result = V[x].toInt() + V[y].toInt()
                V[0xF] = if (result > 0xFF) 1u else 0u
                V[x] = result.toUByte()
            }
            5 -> {
                val result = V[x].toInt() - V[y].toInt()
                V[0xF] = if (result < 0) 0u else 1u
                V[x] = result.toUByte()
            }
            6 -> {
                if (!isModern) {
                    V[x] = V[y]
                }
                V[0xF] = (V[x] % 2u).toByteStore()
                V[x] = (V[x].toInt() shr 1).toUByte()
            }
            7 -> {
                val result = V[y].toInt() - V[x].toInt()
                V[0xF] = if (result < 0) 0u else 1u
                V[x] = result.toUByte()
            }
            0xE -> {
                if (!isModern) {
                    V[x] = V[y]
                }
                V[0xF] = (V[x] % 2u).toByteStore()
                V[x] = (V[x].toInt() shl 1).toUByte()
            }
        }
    }

    fun skipXnotY(operands: Operands) {
        if (V[operands.x().toInt()] != V[operands.y().toInt()]) {
            PC += 2
        }
    }

    fun setI(operands: Operands) {
        I = operands.toInt()
    }

    fun jmpOff(operands: Operands) {
        val plusHend = if (isModern) V[operands.x().toInt()] else V[0]
        PC = operands.toInt() + plusHend.toInt()
    }

    fun rnd(operands: Operands) {
        val num = Random.nextInt()
        V[operands.x().toInt()] = num.toUByte() and operands.toByteStore()
    }

    fun draw(operands: Operands) {
        val n = operands.n().toInt()
        V[0xF] = 0u
        repeat(n) { row ->
            val y = ((V[operands.y().toInt()] + row.toUByte()) % 32u).toInt()
            val sprite = memory[I + row]
            // 8 is the sprite's width
            repeat(8) { col ->
                val x = ((V[operands.x().toInt()] + col.toUByte()) % 64u).toInt()
                val spritePixelState = ((sprite.toInt() shr (7 - col)) and 1) == 1
                val oldState = videoMemory[y][x]
                val newState = oldState xor spritePixelState
                V[0xF] = V[0xF] or (spritePixelState and oldState).toUByte()
                videoMemory[y][x] = newState
            }
        }
        onDrawn?.invoke(videoMemory)
    }

    fun skipKey(operands: Operands) {
        val x = operands.x().toInt()
        val result = when (operands.toWordStore().toInt()) {
            0x9E -> V[x] == key
            0xA1 -> V[x] != key
            else -> false
        }
        if (result) PC += 2
    }

    fun others(operands: Operands) {
        val x = operands.x().toInt()
        when (operands.toByteStore().toInt()) {
            0x07 -> V[x] = DT
            0x0A -> {
                // TODO Handle OG (non-modern) case
                if (key == null) {
                    // Halt fetch-decode-execute to wait for the key press
                    PC -= 2
                } else {
                    V[x] = key
                }
            }
            0x15 -> DT = V[x]
            0x18 -> ST = V[x]
            0x1E -> {
                val result = V[x].toInt() + I
                if (isModern && result > 0x0FFF) {
                    V[0xF] = 1u
                } else {
                    V[0xF] = 0u
                }
                I = result
            }
            0x29 -> {
                val character = V[x]
                I = memory[FONT_START + (character.toInt() * 5)].toInt()
            }
            0x33 -> {
                val hexNum = V[x].toInt()
                memory[I] = (hexNum / 100).toUByte()
                memory[I + 1] = ((hexNum / 10) % 10).toUByte()
                memory[I + 2] = ((hexNum % 100) % 10).toUByte()

            }
            0x55 -> {
                if (isModern) {
                    for (i in 0..x) {
                        memory[I + i] = V[i]
                    }
                    I += x + 1
                } else {
                    repeat(x) {
                        memory[I] = V[I]
                        I++
                    }
                }
            }
            0x65 -> {
                if (isModern) {
                    for (i in 0..x) {
                        V[i] = memory[I + i]
                    }
                    I += x + 1
                } else {
                    repeat(x) {
                        V[I] = memory[I]
                        I++
                    }
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