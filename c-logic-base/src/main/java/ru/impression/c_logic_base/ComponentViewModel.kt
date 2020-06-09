package ru.impression.c_logic_base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

abstract class ComponentViewModel : ViewModel(), LifecycleEventObserver {

    @PublishedApi
    internal val render = LiveCommand()

    @PublishedApi
    internal val sharedProperties =
        HashMap<KClass<out ComponentViewModel>, HashMap<KMutableProperty<*>, ArrayList<KMutableProperty<*>>>>()

    private val onPropertyChangedListeners =
        HashMap<LifecycleOwner, (property: KMutableProperty<*>, value: Any?) -> Unit>()

    protected fun <T> state(
        initialValue: T,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ComponentViewModel, T> =
        object : ObservableImpl<T>(initialValue, onChanged) {
            override fun notifyPropertyChanged(property: KMutableProperty<*>, value: T) {
                render()
                callOnPropertyChangedListeners(property, value)
            }
        }

    protected fun <T> observable(
        initialValue: T,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ComponentViewModel, T> =
        object : ObservableImpl<T>(initialValue, onChanged) {
            override fun notifyPropertyChanged(property: KMutableProperty<*>, value: T) {
                callOnPropertyChangedListeners(property, value)
            }
        }

    protected inline fun <reified VM : ComponentViewModel, T> KProperty<*>.isMutableBy(
        vararg properties: KMutableProperty1<VM, T>
    ) {
        val map = sharedProperties[VM::class]
            ?: HashMap<KMutableProperty<*>, ArrayList<KMutableProperty<*>>>()
                .also { sharedProperties[VM::class] = it }
        properties.forEach { property ->
            val list = map[property]
                ?: ArrayList<KMutableProperty<*>>().also { map[property] = it }
            list.add(
                this as? KMutableProperty<*>
                    ?: throw UnsupportedOperationException("Property must be mutable")
            )
        }
    }

    fun addOnPropertyChangedListener(
        owner: LifecycleOwner,
        listener: (property: KProperty<*>, value: Any?) -> Unit
    ) {
        onPropertyChangedListeners[owner] = listener
        owner.lifecycle.addObserver(this)
    }

    private fun callOnPropertyChangedListeners(property: KMutableProperty<*>, value: Any?) {
        onPropertyChangedListeners.values.forEach { it(property, value) }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            onPropertyChangedListeners.remove(source)
            source.lifecycle.removeObserver(this)
        }
    }

    internal abstract class ObservableImpl<T>(
        initialValue: T,
        private val onChanged: ((T) -> Unit)? = null
    ) : ReadWriteProperty<ComponentViewModel, T> {

        private var value = initialValue

        override fun getValue(thisRef: ComponentViewModel, property: KProperty<*>) = value

        override fun setValue(thisRef: ComponentViewModel, property: KProperty<*>, value: T) {
            if (this.value == value) return
            this.value = value
            onChanged?.invoke(value)
            notifyPropertyChanged(property as KMutableProperty<*>, value)
        }

        abstract fun notifyPropertyChanged(property: KMutableProperty<*>, value: T)
    }
}