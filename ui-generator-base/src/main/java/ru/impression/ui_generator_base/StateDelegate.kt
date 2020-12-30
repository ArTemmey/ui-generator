package ru.impression.ui_generator_base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.impression.ui_generator_annotations.Prop
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

open class StateDelegate<R : StateOwner, T>(
    val parent: R,
    initialValue: T,
    val getInitialValue: (suspend () -> T)?,
    val onChanged: ((T) -> Unit)?
) : ReadWriteProperty<R, T> {

    @Volatile
    var value = initialValue
        private set

    @Volatile
    var isLoading = false
        private set

    @Volatile
    private var loadJob: Job? = null

    init {
        if (getInitialValue != null) load(false)
    }

    @Synchronized
    fun load(notifyStateChangedBeforeLoading: Boolean): Job {
        loadJob?.cancel()
        isLoading = true
        if (notifyStateChangedBeforeLoading) parent.onStateChanged()
        return (parent as CoroutineScope).launch {
            val result = getInitialValue!!.invoke()
            isLoading = false
            setValueToProperty(result)
            loadJob = null
        }.also { loadJob = it }
    }

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>) = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        setValue(property, value)
    }

    @Synchronized
    fun setValue(
        property: KProperty<*>,
        value: T,
        renderImmediately: Boolean = false
    ) {
        this.value = value
        parent.onStateChanged(renderImmediately)
        if (property.findAnnotation<Prop>()?.twoWay == true)
            (parent as? ComponentViewModel)?.notifyTwoWayPropChanged(property.name)
        onChanged?.invoke(value)
    }

    @Synchronized
    fun setValueToProperty(value: T) {
        getProperty()?.set(parent, value)
    }
}