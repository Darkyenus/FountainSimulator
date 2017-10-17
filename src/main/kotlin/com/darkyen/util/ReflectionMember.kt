package com.darkyen.util

import java.lang.reflect.Field
import kotlin.reflect.KProperty

/**
 *
 */
class ReflectionMember<T>(onClass:Class<*>, name:String) {

    val field: Field = onClass.getDeclaredField(name).apply {
        isAccessible = true
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return field.get(thisRef) as T
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        field.set(thisRef, value)
    }
}