package ru.impression.ui_generator_base

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

abstract class CoroutineViewModel(attrs: IntArray? = null) : ComponentViewModel(attrs),
    ClearableCoroutineScope by ClearableCoroutineScopeImpl(Dispatchers.IO) {

    protected fun <T> state(loadValue: suspend () -> T, onChanged: ((T?) -> Unit)? = null) =
        StateDelegate(this, null, onChanged, loadValue = loadValue)
            .also { delegates.add(it) }

    protected fun <T> state(valueFlow: Flow<T>, onChanged: ((T?) -> Unit)? = null) =
        StateDelegate(this, null, onChanged, valueFlow = valueFlow)
            .also { delegates.add(it) }

    protected fun <T> state(valueFlow: StateFlow<T>, onChanged: ((T) -> Unit)? = null) =
        StateDelegate(this, valueFlow.value, onChanged, valueFlow = valueFlow)
            .also { delegates.add(it) }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        clear()
    }
}