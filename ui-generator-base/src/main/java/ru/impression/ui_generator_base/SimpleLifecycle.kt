package ru.impression.ui_generator_base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentSkipListSet

class SimpleLifecycle(private val owner: LifecycleOwner) : Lifecycle() {

    @Volatile
    private var state = State.INITIALIZED

    private val observers = ConcurrentSkipListSet<LifecycleEventObserver>()

    override fun addObserver(observer: LifecycleObserver) {
        observers.add(observer as? LifecycleEventObserver ?: return)
    }

    override fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer as? LifecycleEventObserver ?: return)
    }

    override fun getCurrentState() = state

    fun handleLifecycleEvent(event: Event) {
        state = event.targetState
        observers.forEach { it.onStateChanged(owner, event) }
    }
}