package ru.impression.ui_generator_base

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers

abstract class CoroutineViewModel(attrs: IntArray? = null) : ComponentViewModel(attrs),
    ClearableCoroutineScope by ClearableCoroutineScopeImpl(Dispatchers.IO) {

    protected fun <T> state(getInitialValue: suspend () -> T, onChanged: ((T?) -> Unit)? = null) =
        StateDelegate(this, null, getInitialValue, onChanged)

    @CallSuper
    override fun onCleared() {
        clear()
    }
}