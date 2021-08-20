package ru.impression.ui_generator_base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CopyOnWriteArraySet

class SimpleLifecycle(private val owner: LifecycleOwner) : Lifecycle() {

    @Volatile
    private var state = State.INITIALIZED

    private val observers = CopyOnWriteArraySet<LifecycleEventObserver>()

    override fun addObserver(observer: LifecycleObserver) {
        if (observer !is LifecycleEventObserver) return
        observers.add(observer)
        if (state > State.INITIALIZED)
            repeat(state.ordinal - 1) {
                observer.onStateChanged(owner, Event.upTo(State.values()[it + 1]) ?: return)
            }
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