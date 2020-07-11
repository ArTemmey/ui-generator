package ru.impression.ui_generator_base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

open class ObservableImpl<R, T>(
    parent: R,
    initialValue: T,
    private val immediatelyBindChanges: Boolean?,
    private val onChanged: ((T) -> Unit)?
) : ReadWriteProperty<R, T> {

    val viewModel = parent as? ComponentViewModel

    @Volatile
    var value = initialValue

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>): T = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        this.value = value
        immediatelyBindChanges?.let { viewModel?.onStateChanged(it) }
        viewModel?.callOnPropertyChangedListeners(property as KMutableProperty<*>, value)
        onChanged?.invoke(value)
    }
}

open class CoroutineObservableImpl<R : CoroutineScope, T>(
    parent: R,
    initialValue: Deferred<T>,
    immediatelyBindChanges: Boolean?,
    onChanged: ((T?) -> Unit)?
) : ObservableImpl<R, T?>(parent, null, immediatelyBindChanges, onChanged) {

    @Volatile
    open var isInitializing = true

    init {
        parent.launch {
            val result = initialValue.await()
            (parent::class.members.firstOrNull {
                it.isAccessible = true
                it is KMutableProperty1<*, *> && (it as KMutableProperty1<R, *>)
                    .getDelegate(parent) === this@CoroutineObservableImpl
            } as KMutableProperty1<CoroutineViewModel, T>?)
                ?.set(parent, result)
            isInitializing = false
            immediatelyBindChanges?.let { viewModel?.onStateChanged(it) }
        }
    }
}