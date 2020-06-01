package ru.impression.c_logic_base

import androidx.lifecycle.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class ComponentViewModel : ViewModel() {

    @PublishedApi
    internal val changedProperty = SingleTakenLiveData<Pair<String, *>>()

    @PublishedApi
    internal val dataDuplicates = ArrayList<DataDependency>()

    inline fun <reified VM : ViewModel, T> ReadWriteProperty<ComponentViewModel, T>.mutableBy(
        vararg properties: KProperty1<VM, T>
    ) = apply {
        properties.forEach {
            dataDuplicates.add(DataDependency(VM::class, it, this))
        }
    }

    fun <T> data(
        initialValue: T,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ComponentViewModel, T> = object : DataImpl<T>(initialValue, onChanged) {
        override fun notifyComponent(propertyName: String, propertyValue: T) {
            changedProperty.value = propertyName to propertyValue
        }
    }

    companion object

    @PublishedApi
    internal abstract class DataImpl<T>(
        initialValue: T,
        private val onChanged: ((T) -> Unit)? = null
    ) : ReadWriteProperty<ComponentViewModel, T> {

        private var value = initialValue

        override fun getValue(thisRef: ComponentViewModel, property: KProperty<*>) = value

        override fun setValue(thisRef: ComponentViewModel, property: KProperty<*>, value: T) {
            if (this.value == value) return
            this.value = value
            onChanged?.invoke(value)
            notifyComponent(property.name, value)
        }

        abstract fun notifyComponent(propertyName: String, propertyValue: T)
    }
}