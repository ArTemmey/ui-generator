package ru.impression.ui_generator_base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.impression.ui_generator_annotations.Prop
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

open class StateDelegate<R : StateOwner, T>(
    val parent: R,
    initialValue: T,
    val onChanged: ((T) -> Unit)?,
    val loadValue: (suspend () -> T)? = null,
    val valueFlow: Flow<T>? = null
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
        when {
            loadValue != null -> load(false)
            valueFlow != null -> collectValueFlow()
        }
    }

    @Synchronized
    fun load(notifyStateChangedBeforeLoading: Boolean): Job {
        loadJob?.cancel()
        isLoading = true
        if (notifyStateChangedBeforeLoading) parent.onStateChanged()
        return (parent as CoroutineScope).launch {
            val result = loadValue!!.invoke()
            isLoading = false
            setValue(result)
            loadJob = null
        }.also { loadJob = it }
    }

    private fun collectValueFlow() {
        isLoading = true
        fun collect() = (parent as CoroutineScope).launch {
            valueFlow!!.collect {
                isLoading = false
                setValue(it)
            }
        }
        (parent as? ComponentViewModel)?.initSubscriptions(::collect) ?: collect()
    }

    override fun getValue(thisRef: R, property: KProperty<*>) = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        setValue(value, property)
    }

    @Synchronized
    fun setValue(
        value: T,
        property: KProperty<*>? = getProperty()
    ) {
        this.value = value
        parent.onStateChanged()
        if (property?.findAnnotation<Prop>()?.twoWay == true)
            (parent as? ComponentViewModel)?.notifyTwoWayPropChanged(property.name)
        onChanged?.invoke(value)
    }
}