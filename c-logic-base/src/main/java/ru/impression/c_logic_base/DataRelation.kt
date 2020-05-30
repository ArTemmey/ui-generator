package ru.impression.c_logic_base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class DataRelation(
    val type: Type,
    val target: ComponentViewModel.Data<*>,
    val sourceViewModelClass: KClass<out ViewModel>,
    val sourceProperty: KProperty1<*, LiveData<*>>
) {

    enum class Type {
        MUTABILITY,
        AFFECTION
    }
}