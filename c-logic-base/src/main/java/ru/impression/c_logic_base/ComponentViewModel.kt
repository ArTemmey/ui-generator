package ru.impression.c_logic_base

import androidx.lifecycle.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class ComponentViewModel : ViewModel() {

    @PublishedApi
    internal val propertyChange = SingleTakenLiveData<Unit>()

    @PublishedApi
    internal val propertyShares =
        HashMap<KClass<out ComponentViewModel>, ArrayList<PropertyShare>>()

    fun <T> state(
        initialValue: T,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ComponentViewModel, T> = object : DataImpl<T>(initialValue, onChanged) {
        override fun notifyComponent() {
            propertyChange.value = Unit
        }
    }

    inline fun <reified VM : ComponentViewModel, T> ReadWriteProperty<ComponentViewModel, T>.mutableBy(
        vararg properties: KProperty1<VM, T>
    ) = apply {
        properties.forEach { property ->
            val list = propertyShares[VM::class]
                ?: ArrayList<PropertyShare>().also { propertyShares[VM::class] = it }
            list.add(
                PropertyShare(
                    property as KProperty1<ComponentViewModel, *>,
                    this as DataImpl<T>
                )
            )
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
            setValue(value)
        }

        fun setValue(value: T) {
            if (this.value == value) return
            this.value = value
            onChanged?.invoke(value)
            notifyComponent()
        }

        abstract fun notifyComponent()
    }
}