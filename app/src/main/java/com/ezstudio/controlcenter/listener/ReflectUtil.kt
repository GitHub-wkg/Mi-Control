package com.ezstudio.controlcenter.listener

import java.lang.reflect.Field
import java.lang.reflect.Modifier

object ReflectUtil {
    fun setFieldValue(obj: Any, fieldName: String, value: Any?) {
        val field = getAccessibleField(obj, fieldName)
            ?: throw IllegalArgumentException("Could not find field [$fieldName] on target [$obj]")
        try {
            field[obj] = value
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    fun getAccessibleField(obj: Any?, fieldName: String?): Field? {
        requireNotNull(obj) { "object can't be null" }
        require(!(fieldName == null || fieldName.isEmpty())) { "fieldName can't be blank" }
        var superClass: Class<*> = obj.javaClass
        while (superClass != Any::class.java) {
            return try {
                val field: Field = superClass.getDeclaredField(fieldName)
                makeAccessible(field)
                field
            } catch (e: NoSuchFieldException) {
                superClass = superClass.superclass
                continue
            }
            superClass = superClass.superclass
        }
        return null
    }

    fun makeAccessible(field: Field) {
        if ((!Modifier.isPublic(field.modifiers) || !Modifier.isPublic(
                field.declaringClass.modifiers
            ) || Modifier.isFinal(field.modifiers)) && !field.isAccessible
        ) {
            field.isAccessible = true
        }
    }
}