package ru.impression.ui_generator_base

import androidx.annotation.CallSuper
import kotlinx.coroutines.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.isAccessible

abstract class CoroutineViewModel : ComponentViewModel(),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {

    protected fun <T> state(
        initialValue: Deferred<T>,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T?) -> Unit)? = null
    ): ReadWriteProperty<ComponentViewModel, T?> =
        CoroutineObservableImpl(this, initialValue, immediatelyBindChanges, onChanged)

    protected fun <T> observable(
        initialValue: Deferred<T>,
        onChanged: ((T?) -> Unit)? = null
    ): ReadWriteProperty<ComponentViewModel, T?> =
        CoroutineObservableImpl(this, initialValue, null, onChanged)

    internal class CoroutineObservableImpl<T>(
        parent: CoroutineViewModel,
        initialValue: Deferred<T>,
        immediatelyBindChanges: Boolean?,
        onChanged: ((T?) -> Unit)?
    ) : ObservableImpl<T?>(parent, null, immediatelyBindChanges, onChanged) {

        @Volatile
        internal var isInitializing = true

        init {
            parent.launch {
                val result = initialValue.await()
                (parent::class.members.firstOrNull {
                    it.isAccessible = true
                    it is KMutableProperty1<*, *> && (it as KMutableProperty1<CoroutineViewModel, *>)
                        .getDelegate(parent) === this@CoroutineObservableImpl
                } as KMutableProperty1<CoroutineViewModel, T>?)
                    ?.set(parent, result)
                isInitializing = false
                immediatelyBindChanges?.let { parent.onStateChanged(it) }
            }
        }
    }

    @CallSuper
    override fun onCleared() {
        cancel()
    }
}