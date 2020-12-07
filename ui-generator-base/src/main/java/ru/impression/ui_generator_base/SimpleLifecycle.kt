package ru.impression.ui_generator_base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

class SimpleLifecycle(private val owner: LifecycleOwner) : Lifecycle() {

    private var observer: LifecycleEventObserver? = null

    private var state = State.INITIALIZED

    override fun addObserver(observer: LifecycleObserver) {
        if (this.observer == null && observer is LifecycleEventObserver) this.observer = observer
    }

    override fun removeObserver(observer: LifecycleObserver) {
        if (this.observer === observer) this.observer = null
    }

    override fun getCurrentState() = state

    fun handleLifecycleEvent(event: Event) {
        state = event.targetState
        observer?.onStateChanged(owner, event)
    }
}