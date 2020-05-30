package ru.impression.c_logic_base

import androidx.lifecycle.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class ComponentViewModel : ViewModel() {

    @PublishedApi
    internal val dataRelations = ArrayList<DataRelation>()

    inline fun <reified T : ViewModel, R> Data<R>.mutableBy(
        vararg properties: KProperty1<T, LiveData<R>>
    ) = apply {
        properties.forEach {
            dataRelations.add(DataRelation(DataRelation.Type.MUTABILITY, this, T::class, it))
        }
    }

    inline fun <reified T : ViewModel, R> Data<R>.changesAffect(
        vararg properties: KProperty1<T, LiveData<R>>
    ) = apply {
        properties.forEach {
            dataRelations.add(DataRelation(DataRelation.Type.AFFECTION, this, T::class, it))
        }
    }

    companion object

    class Data<T>(value: T? = null, onChange: ((T) -> Unit)? = null) : MutableLiveData<T>() {

        init {
            set(value)
            onChange?.let { observeForever { it(it) } }
        }

        fun get() = value

        fun set(value: T?) = setValue(value)

        override fun setValue(value: T?) {
            if (value != this.value) super.setValue(value)
        }
    }
}