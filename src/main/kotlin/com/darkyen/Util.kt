@file:Suppress("unused")

package com.darkyen

/**
 *
 */
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.ByteArray as Bytes
import com.badlogic.gdx.utils.IntArray as Ints

typealias Objects<T> = com.badlogic.gdx.utils.Array<T>
typealias Floats = com.badlogic.gdx.utils.FloatArray
typealias Bytes = com.badlogic.gdx.utils.ByteArray
typealias Ints = com.badlogic.gdx.utils.IntArray
typealias Shorts = com.badlogic.gdx.utils.ShortArray // (not Oxhorn's)

typealias Index = Int

inline fun <T> Objects<T>.forEach(action:(T) -> Unit) {
    val items = this.items
    for (i in 0 until this.size) {
        action(items[i])
    }
}

inline fun Ints.forEach(action:(Int) -> Unit) {
    val items = this.items
    for (i in 0 until this.size) {
        action(items[i])
    }
}

inline fun Floats.forEach(action:(Float) -> Unit) {
    val items = this.items
    for (i in 0 until this.size) {
        action(items[i])
    }
}

inline fun Bytes.forEach(action:(Byte) -> Unit) {
    val items = this.items
    for (i in 0 until this.size) {
        action(items[i])
    }
}

inline fun Ints.forEachIndexed(action:(Int, Index) -> Unit) {
    val items = this.items
    for (i in 0 until this.size) {
        action(items[i], i)
    }
}

inline fun Floats.forEachIndexed(action:(Float, Index) -> Unit) {
    val items = this.items
    for (i in 0 until this.size) {
        action(items[i], i)
    }
}

inline fun Bytes.forEachIndexed(action:(Byte, Index) -> Unit) {
    val items = this.items
    for (i in 0 until this.size) {
        action(items[i], i)
    }
}

/** Returns a re-mapped float value from inRange to outRange.  */
fun map(value: Float, inRangeStart: Float, inRangeEnd: Float, outRangeStart: Float, outRangeEnd: Float): Float {
    return outRangeStart + (outRangeEnd - outRangeStart) * ((value - inRangeStart) / (inRangeEnd - inRangeStart))
}

fun toString(p:Pixmap):CharSequence {
    return "Pixmap "+p.width+" x "+p.height+" in "+p.format
}

fun toString(t: Texture):CharSequence {
    return "Texture "+ t.width+" x "+ t.height+" with "+ t.textureData?.type+" data"
}