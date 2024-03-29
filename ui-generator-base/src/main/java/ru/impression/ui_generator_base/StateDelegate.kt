package ru.impression.ui_generator_base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityParent
import ru.impression.ui_generator_annotations.Prop
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

class StateDelegate<R : StateOwner, T>(
    val parent: R,
    private val singletonEntityParent: SingletonEntityParent,
    initialValue: T,
    private val onChanged: ((T) -> Unit)?,
    val loadValue: (suspend () -> T)? = null,
    val valueFlow: Flow<T>? = null,
    val property: KProperty<*>
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
        observeValue()
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
        fun dpCollect() = (parent as CoroutineScope).launch {
            valueFlow!!.collect {
                if (it === value) return@collect
                setValue(it)
            }
        }
        (parent as? ComponentViewModel)?.initSubscriptions(::dpCollect) ?: dpCollect()
    }

    override fun getValue(thisRef: R, property: KProperty<*>) = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        setValue(value)
    }

    @Synchronized
    fun setValue(
        value: T
    ) {
        stopObserveValue()
        this.value = value
        observeValue()
        parent.onStateChanged()
        if (property.findAnnotation<Prop>()?.twoWay == true)
            (parent as? ComponentViewModel)?.notifyTwoWayPropChanged(property.name)
        onChanged?.invoke(value)
    }

    fun observeValue() {
        (this.value as? ObservableEntity)?.addStateOwner(parent)
        (this.value as? SingletonEntity)?.addParent(singletonEntityParent)
    }

    fun stopObserveValue() {
        (this.value as? ObservableEntity)?.removeStateOwner(parent)
        (this.value as? SingletonEntity)?.removeParent(singletonEntityParent)
    }
}