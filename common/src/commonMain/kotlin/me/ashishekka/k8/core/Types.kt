package me.ashishekka.k8.core


typealias ByteStore = UByte
typealias WordStore = UShort
typealias Memory = Array<ByteStore>
typealias VideoMemory = Array<BooleanArray>
typealias Stack = ArrayDeque<WordStore>
typealias Operands = Triple<ByteStore, ByteStore, ByteStore>
typealias Operation = (Operands) -> Unit
typealias InstructionSet = HashMap<ByteStore, Operation>

fun Triple<ByteStore, ByteStore, ByteStore>.x() = first

fun Triple<ByteStore, ByteStore, ByteStore>.y() = second

fun Triple<ByteStore, ByteStore, ByteStore>.n() = third

fun Triple<ByteStore, ByteStore, ByteStore>.toByteStore() = ((y().toUInt() shl 4) or n().toUInt()).toUByte()

fun Triple<ByteStore, ByteStore, ByteStore>.toInt() = (x().toInt() shl 8) or (y().toInt() shl 4) or n().toInt()

fun Triple<ByteStore, ByteStore, ByteStore>.toWordStore() = toInt().toUShort()

fun ByteStore.lowerNibble(): ByteStore {
    return (this and 0x0Fu)
}

fun ByteStore.upperNibble(): ByteStore {
    return ((this.toInt() shr 4) and 0x0F).toUByte()
}

fun UInt.toByteStore(): ByteStore = this.toUByte()

fun UInt.toWordStore(): WordStore = this.toUShort()

fun Boolean.toUByte() = (if (this) 0x1u else 0u).toUByte()

fun VideoMemory.print(): String {
    val sb = StringBuilder()
    forEach { rowData ->
        sb.append("\n")
        rowData.forEach { colData ->
            if (colData) {
                sb.append("■■")
            } else {
                sb.append("  ")
            }
        }
    }
    return sb.toString()
}

fun VideoMemory.clear() {
    forEachIndexed { y, row ->
        row.forEachIndexed { x, _ ->
            this[y][x] = false
        }
    }
}

fun Memory.print(): String {
    val sb = StringBuilder()
    forEachIndexed { index, uByte ->
        val hexValue = uByte.toString(16)
        if (index % 8 == 0) {
            sb.append("\n$hexValue")
        } else {
            sb.append(hexValue)
        }
    }
    return sb.toString()
}

fun Memory.clear() {
    indices.forEach { if (it >= 0x200) this[it] = 0u }
}
