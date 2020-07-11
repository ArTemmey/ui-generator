package ru.impression.ui_generator_base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

open class ObservableImpl<R : Any, T>(
    parent: R,
    initialValue: T,
    initialValueDeferred: Deferred<T>?,
    private val immediatelyBindChanges: Boolean?,
    private val onChanged: ((T) -> Unit)?
) : ReadWriteProperty<R, T> {

    open val viewModel = parent as? ComponentViewModel

    @Volatile
    var property: KMutableProperty<*>? = null

    @Volatile
    var value = initialValue

    @Volatile
    var isInitializing = initialValueDeferred != null

    init {
        initialValueDeferred?.let { deferred ->
            (parent as? CoroutineScope)?.launch {
                val result = deferred.await()
                isInitializing = false
                (parent::class.members.firstOrNull {
                    it.isAccessible = true
                    it is KMutableProperty1<*, *> && (it as KMutableProperty1<R, *>)
                        .getDelegate(parent) === this@ObservableImpl
                } as KMutableProperty1<R, T>?)?.set(parent, result)
            }
        }
    }

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>): T {
        this.property ?: run { this.property = property as? KMutableProperty<*> }
        return value
    }

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        this.property ?: run { this.property = property as? KMutableProperty<*> }
        this.value = value
        notifyListeners(value)
    }

    open fun notifyListeners(value: T) {
        immediatelyBindChanges?.let { viewModel?.onStateChanged(it) }
        property?.let { viewModel?.callOnPropertyChangedListeners(it, value) }
        viewModel?.callOnPropertyChangedListeners(property as KMutableProperty<*>, value)
        onChanged?.invoke(value)
    }
}