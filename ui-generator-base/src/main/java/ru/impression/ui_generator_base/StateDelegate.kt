package ru.impression.ui_generator_base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import ru.impression.ui_generator_annotations.Prop
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

open class StateDelegate<R : StateParent, T>(
    val parent: R,
    initialValue: T,
    val getInitialValue: (suspend () -> T)?,
    val onChanged: ((T) -> Unit)?
) : ReadWriteProperty<R, T> {

    @Volatile
    private var value = initialValue

    @Volatile
    internal var isLoading = false

    @Volatile
    private var loadJob: Job? = null

    init {
        load(false)
    }

    @Synchronized
    internal fun load(notifyStateChangedBeforeLoading: Boolean) {
        getInitialValue ?: return
        if (parent !is CoroutineScope) return
        loadJob?.cancel()
        isLoading = true
        if (notifyStateChangedBeforeLoading) parent.onStateChanged()
        loadJob = parent.launch {
            val result = getInitialValue.invoke()
            isLoading = false
            setValueToProperty(result)
            loadJob = null
        }
    }

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>) = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        setValue(property, value)
    }

    @Synchronized
    internal fun setValue(
        property: KProperty<*>,
        value: T,
        renderImmediately: Boolean = false
    ) {
        this.value = value
        parent.onStateChanged(renderImmediately)
        if (property.findAnnotation<Prop>()?.twoWay == true)
            (parent as? ComponentViewModel)?.callOnTwoWayPropChangedListener(property.name)
        onChanged?.invoke(value)
    }

    @Synchronized
    private fun setValueToProperty(value: T) {
        (parent::class.declaredMemberProperties.firstOrNull {
            (it as? KProperty1<R, *>)
                ?.getDelegateFromSum<R, StateDelegate<*, *>>(parent) == this
        } as KMutableProperty1<R, T>?)?.set(parent, value)
    }
}