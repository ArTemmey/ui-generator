package ru.impression.ui_generator_base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.impression.kotlin_delegate_concatenator.DelegateSum
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

open class StateDelegate<R : Any, T>(
    val parent: R,
    initialValue: T,
    val getInitialValue: (suspend () -> T)?,
    val immediatelyBindChanges: Boolean?,
    val onChanged: ((T) -> Unit)?
) : ReadWriteProperty<R, T> {

    val viewModel = parent as? ComponentViewModel

    @Volatile
    var value = initialValue

    @Volatile
    var isLoading = false

    @Volatile
    var loadJob: Job? = null

    init {
        load(false)
    }

    fun load(notifyStateChangedBeforeLoading: Boolean) {
        getInitialValue ?: return
        if (parent !is CoroutineScope) return
        loadJob?.cancel()
        isLoading = true
        if (notifyStateChangedBeforeLoading) notifyStateChanged()
        loadJob = parent.launch {
            val result = getInitialValue.invoke()
            isLoading = false
            (parent::class.members.firstOrNull {
                it.isAccessible = true
                val targetDelegate = (it as? KMutableProperty1<R, *>)?.getDelegate(parent)
                if (targetDelegate is DelegateSum<*, *>)
                    this@StateDelegate == targetDelegate.delegate1 || this@StateDelegate == targetDelegate.delegate2
                else
                    this@StateDelegate == targetDelegate
            } as KMutableProperty1<R, T>?)?.set(parent, result)
            loadJob = null
        }
    }

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>) = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        this.value = value
        notifyStateChanged()
        (property as? KMutableProperty<*>)
            ?.let { viewModel?.callOnPropertyChangedListeners(it, value) }
        onChanged?.invoke(value)
    }

    open fun notifyStateChanged() {
        immediatelyBindChanges?.let { viewModel?.onStateChanged(it) }
    }
}