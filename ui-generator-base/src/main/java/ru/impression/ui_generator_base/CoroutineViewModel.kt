package ru.impression.ui_generator_base

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers

abstract class CoroutineViewModel : ComponentViewModel(),
    ClearableCoroutineScope by ClearableCoroutineScopeImpl(Dispatchers.IO) {

    protected fun <T> state(
        getInitialValue: suspend () -> T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T?) -> Unit)? = null
    ) = StateDelegate(this, null, getInitialValue, immediatelyBindChanges, onChanged)

    protected fun <T> observable(
        getInitialValue: suspend () -> T,
        onChanged: ((T?) -> Unit)? = null
    ) = StateDelegate(this, null, getInitialValue, null, onChanged)

    @CallSuper
    override fun onCleared() {
        clear()
    }
}