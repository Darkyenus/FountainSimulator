package com.darkyen

/**
 *
 */
import com.badlogic.gdx.math.MathUtils
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

fun normalizeAngleDeg(degrees:Float):Float {
    val result = (degrees % 360f)
    return if (result < 0f) {
        result + 360f
    } else {
        result
    }
}

fun normalizeAngleRad(radians:Float):Float {
    val result = (radians % MathUtils.PI2)
    return if (result < 0f) {
        result + MathUtils.PI2
    } else {
        result
    }
}

fun normalizeAngleOffsetDeg(degrees:Float):Float {
    val result = (degrees % 360)
    return when {
        result < -180 -> result + 360
        result > 180 -> result - 180
        else -> result
    }
}

fun normalizeAngleOffsetRad(radians:Float):Float {
    val result = (radians % MathUtils.PI2)
    return when {
        result < -MathUtils.PI -> result + MathUtils.PI2
        result > MathUtils.PI -> result - MathUtils.PI2
        else -> result
    }
}

/** Returns a re-mapped float value from inRange to outRange.  */
fun map(value: Float, inRangeStart: Float, inRangeEnd: Float, outRangeStart: Float, outRangeEnd: Float): Float {
    return outRangeStart + (outRangeEnd - outRangeStart) * ((value - inRangeStart) / (inRangeEnd - inRangeStart))
}