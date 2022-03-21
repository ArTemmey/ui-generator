package ru.impression.ui_generator_base

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.PropertyDelegateProvider

abstract class CoroutineViewModel(attrs: IntArray? = null) : ComponentViewModel(attrs),
    ClearableCoroutineScope by ClearableCoroutineScopeImpl(Dispatchers.IO) {

    protected fun <T> state(loadValue: suspend () -> T, onChanged: ((T?) -> Unit)? = null) =
        PropertyDelegateProvider<CoroutineViewModel, StateDelegate<CoroutineViewModel, T?>> { _, property ->
            StateDelegate(
                parent = this,
                singletonEntityParent = singletonEntityParent,
                initialValue = null,
                onChanged = onChanged,
                loadValue = loadValue,
                property = property
            )
                .also { delegates.add(it) }
        }


    protected fun <T> state(flow: Flow<T>, onChanged: ((T?) -> Unit)? = null) =
        PropertyDelegateProvider<CoroutineViewModel, StateDelegate<CoroutineViewModel, T?>> { _, property ->
            StateDelegate(
                parent = this,
                singletonEntityParent = singletonEntityParent,
                initialValue = null,
                onChanged = onChanged,
                valueFlow = flow,
                property = property
            )
                .also { delegates.add(it) }
        }


    protected fun <T> state(stateFlow: StateFlow<T>, onChanged: ((T) -> Unit)? = null) =
        PropertyDelegateProvider<CoroutineViewModel, StateDelegate<CoroutineViewModel, T>> { _, property ->
            StateDelegate(
                parent = this,
                singletonEntityParent = singletonEntityParent,
                initialValue = stateFlow.value,
                onChanged = onChanged,
                valueFlow = stateFlow,
                property = property
            ).also { delegates.add(it) }
        }


    @CallSuper
    override fun onCleared() {
        clear()
        super.onCleared()
    }
}