package ca.tradejmark.newspeak

import ca.tradejmark.newspeak.error.IndexError
import java.lang.StringBuilder
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

sealed class NSValue(val typeName: String) {
    open fun show(): String = toString()
    sealed class NSNum(typeName: String, private val numVal: Number): NSValue(typeName) {
        abstract operator fun plus(other: NSNum): NSNum
        abstract operator fun minus(other: NSNum): NSNum
        abstract operator fun times(other: NSNum): NSNum
        operator fun div(other: NSNum): NSReal = NSReal(numVal.toDouble() / other.numVal.toDouble())
        abstract fun pow(other: NSNum): NSNum
        operator fun compareTo(other: NSNum): Int = if (this == other) 0 else (this.numVal.toDouble() - other.numVal.toDouble()).sign.toInt()
        override fun equals(other: Any?): Boolean = when (other) {
            is NSNum -> (numVal.toDouble() - other.numVal.toDouble()).absoluteValue < 10E-10
            else -> false
        }
        override fun hashCode(): Int {
            return numVal.hashCode()
        }
        override fun toString(): String = numVal.toString()
        class NSInt(val value: Int): NSNum("integer", value) {
            override operator fun plus(other: NSNum) = when (other) {
                is NSReal -> NSReal(value + other.value)
                is NSInt -> NSInt(value + other.value)
            }
            override operator fun minus(other: NSNum): NSNum = when (other) {
                is NSReal -> NSReal(value - other.value)
                is NSInt -> NSInt(value - other.value)
            }
            override operator fun times(other: NSNum): NSNum = when (other) {
                is NSReal -> NSReal(value * other.value)
                is NSInt -> NSInt(value * other.value)
            }
            override fun pow(other: NSNum): NSNum = when (other) {
                is NSReal -> NSReal(value.toDouble().pow(other.value))
                is NSInt -> NSInt(value.toDouble().pow(other.value).toInt())
            }
        }
        class NSReal(val value: Double): NSNum("real number", value) {
            override operator fun plus(other: NSNum): NSReal = NSReal(value + other.numVal.toDouble())
            override operator fun minus(other: NSNum): NSReal = NSReal(value - other.numVal.toDouble())
            override operator fun times(other: NSNum): NSReal = NSReal(value * other.numVal.toDouble())
            override fun pow(other: NSNum): NSReal = NSReal(value.pow(other.numVal.toDouble()))
        }
    }
    data class NSString(val value: String): NSValue("string") {
        override fun toString(): String = value
        override fun show(): String = "\"$value\""
    }
    sealed class NSBool(private val value: Boolean): NSValue("boolean") {
        override fun toString(): String = value.toString()
        object NSTrue: NSBool(true)
        object NSFalse: NSBool(false)
        companion object {
            fun fromBool(v: Boolean): NSBool {
                return if (v) NSTrue else NSFalse
            }
        }
    }
    class NSList: NSValue("list") {
        private class Node(var data: NSValue, var next: Node? = null, var prev: Node? = null)
        private var head: Node? = null
        private var tail: Node? = null
        private var size: Int = 0

        fun empty(): Boolean = size == 0

        fun add(v: NSValue) {
            if (head == null) {
                val n = Node(v)
                head = n
                tail = n
            }
            else {
                val n = Node(v, prev = tail)
                tail!!.next = n
                tail = n
            }
            size += 1
        }

        fun remove(i: Int) {
            if (empty()) throw IndexError(i, "remove")
            val n = nodeAtI(i, "remove")
            if (n.prev != null) n.prev!!.next = n.next
            else head = n.next
            if (n.next != null) n.next!!.prev = n.prev
            else tail = n.prev
            size -= 1
        }

        operator fun set(i: Int, v: NSValue) {
            if (i >= size) throw IndexError(i, "insert")
            val n = nodeAtI(i, "update")
            n.data = v
        }

        operator fun get(i: Int): NSValue {
            if (empty()) throw IndexError(i, "get")
            return nodeAtI(i, "get").data
        }

        fun forEach(f: (NSValue) -> Unit) {
            var p = head
            while (p != null) {
                f(p.data)
                p = p.next
            }
        }

        fun <T> map(m: (NSValue) -> T): List<T> {
            val l = mutableListOf<T>()
            for (i in 0 until size) {
                l.add(m(nodeAtI(i, "map").data))
            }
            return l
        }

        private fun nodeAtI(i: Int, op: String): Node {
            val dir = i < size/2
            fun Node.a() = if (dir) next else prev
            var p = (if (dir) head else tail)!!
            for (x in 1..(if (dir) i else size - i - 1)) {
                p = p.a() ?: throw IndexError(i, op)
            }
            return p
        }

        override fun equals(other: Any?): Boolean =
            if (other is NSList) equals(head, other.head)
            else false

        override fun hashCode(): Int = map { it.hashCode() }.sum()

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append('[')
            forEach { sb.append("$it, ") }
            sb.replace(sb.length - 2, sb.length, "]")
            return sb.toString()
        }
        companion object {
            @JvmStatic
            private fun equals(a: Node?, b: Node?): Boolean = a?.data == b?.data && a?.let { equals(it.next, b?.next) } ?: true
        }

        operator fun iterator(): NSListIterator = NSListIterator()

        inner class NSListIterator: Iterator<NSValue> {
            private var state = head
            override fun hasNext(): Boolean = state != null
            override fun next(): NSValue {
                val now = state ?: throw NoSuchElementException("Reached end of list.")
                state = now.next
                return now.data
            }
        }
    }

    class NSFunc(val params: List<String>, val block: NewspeakParser.BlockContext): NSValue("function") {
        override fun toString(): String = "<function>"
    }
}