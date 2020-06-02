package ru.impression.c_logic_base

import android.os.Looper
import androidx.lifecycle.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class ComponentViewModel : ViewModel() {

    val observables = HashMap<String, MutableLiveData<*>>()

    @PublishedApi
    internal val stateChange = SingleTakenLiveData<Unit>()

    @PublishedApi
    internal val sharedProperties =
        HashMap<KClass<out ComponentViewModel>, HashMap<KProperty1<ComponentViewModel, *>, StateImpl<*>>>()

    fun <T> state(initialValue: T, onChanged: ((T) -> Unit)? = null) =
        object : StateImpl<T>(initialValue, onChanged) {
            override fun notifyStateChanged() {
                if (Thread.currentThread() == Looper.getMainLooper().thread)
                    stateChange.value = Unit
                else
                    stateChange.postValue(Unit)
            }
        }

    inline fun <reified VM : ComponentViewModel, T> StateImpl<T>.mutableBy(
        vararg properties: KProperty1<VM, T>
    ) = apply {
        properties.forEach { property ->
            val map = sharedProperties[VM::class]
                ?: HashMap<KProperty1<ComponentViewModel, *>, StateImpl<*>>().also {
                    sharedProperties[VM::class] = it
                }
            map[property as KProperty1<ComponentViewModel, *>] = this
        }
    }

    abstract class StateImpl<T> internal constructor(
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
            notifyStateChanged()
        }

        abstract fun notifyStateChanged()
    }

    companion object
}