package me.ashishekka.k8.core

const val PC_START = 0x200

class Cpu(
    private val memory: Memory = Memory(4096) { 0u },
    private val videoMemory: VideoMemory = VideoMemory(32) { BooleanArray(64) },
    private val onDrawn: (VideoMemory) -> Unit
) {
    // Program counter, starts at 0x200
    var PC = PC_START

    // 16 Bit Index register
    var I = 0

    // Stack
    val stack = Stack()

    // 8-bit Delay timer
    var DT = ByteStore

    // 8-bit Sound timer
    var ST = ByteStore

    // 16 8-bit General purpose variable registers
    val V = Array<ByteStore>(16) { 0u }

    private val instructionSet: InstructionSet

    init {
        // Load the FONT_SPRITE into the memory (anywhere b/w 000 to 1FF, popularly 050-09f)
        FONT_SPRITES.forEachIndexed { index, byte ->
            memory[0x050 + index] = byte
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

    fun cls(operands: Operands) {
        videoMemory.forEachIndexed { y, row ->
            row.forEachIndexed { x, _ ->
                videoMemory[y][x] = false
            }
        }
        onDrawn.invoke(videoMemory)
    }

    fun jmp(operands: Operands) {
        PC = operands.toInt()
    }

    fun setVx(operands: Operands) {
        val registerIndex = operands.x().toInt()
        V[registerIndex] = operands.toByteStore()
    }

    fun addVx(operands: Operands) {
        val registerIndex = operands.x().toInt()
        V[registerIndex] = (V[registerIndex] + operands.toByteStore()).toByteStore()

    }

    fun setI(operands: Operands) {
        I = operands.toInt()
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
        onDrawn.invoke(videoMemory)
    }

    private fun loadInstructions(): HashMap<ByteStore, Operation> {
        return hashMapOf(
            Pair(0x0u, ::cls),
            Pair(0x1u, ::jmp),
            Pair(0x6u, ::setVx),
            Pair(0x7u, ::addVx),
            Pair(0xAu, ::setI),
            Pair(0xDu, ::draw)
        )
    }
}