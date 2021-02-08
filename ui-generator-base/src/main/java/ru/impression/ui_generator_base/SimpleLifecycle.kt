package ru.impression.ui_generator_base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

class SimpleLifecycle(private val owner: LifecycleOwner) : Lifecycle() {

    @Volatile
    private var state = State.INITIALIZED

    private val observers = HashSet<LifecycleEventObserver>()

    private val lock = Any()

    override fun addObserver(observer: LifecycleObserver) {
        synchronized(lock) { observers.add(observer as? LifecycleEventObserver ?: return) }
    }

    override fun removeObserver(observer: LifecycleObserver) {
        synchronized(lock) { observers.remove(observer as? LifecycleEventObserver ?: return) }
    }

    override fun getCurrentState() = state

    fun handleLifecycleEvent(event: Event) {
        state = event.targetState
        synchronized(lock) { observers.forEach { it.onStateChanged(owner, event) } }
    }
}