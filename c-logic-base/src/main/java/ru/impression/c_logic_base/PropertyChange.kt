package ru.impression.c_logic_base

import kotlin.reflect.KProperty1

class PropertyChange(
    val property: KProperty1<*, *>?,
    val value: Any?,
    val needRender: Boolean
)