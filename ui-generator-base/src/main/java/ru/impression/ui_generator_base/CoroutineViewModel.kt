package ru.impression.ui_generator_base

import androidx.annotation.CallSuper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.properties.ReadWriteProperty

abstract class CoroutineViewModel : ComponentViewModel(),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {

    protected fun <T> state(
        initialValue: Deferred<T>,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T?) -> Unit)? = null
    ): ReadWriteProperty<CoroutineViewModel, T?> =
        CoroutineObservableImpl(this, initialValue, immediatelyBindChanges, onChanged)

    protected fun <T> observable(
        initialValue: Deferred<T>,
        onChanged: ((T?) -> Unit)? = null
    ): ReadWriteProperty<CoroutineViewModel, T?> =
        CoroutineObservableImpl(this, initialValue, null, onChanged)


    @CallSuper
    override fun onCleared() {
        cancel()
    }
}