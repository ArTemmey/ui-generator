package ru.impression.c_logic_base

import androidx.lifecycle.ViewModel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class DataDependency(
    val sourceViewModelClass: KClass<out ViewModel>,
    val sourceProperty: KProperty1<*, *>,
    val targetProperty: ReadWriteProperty<*, *>
)