package ru.impression.ui_generator_base

import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

abstract class ComponentViewModel(val attrs: IntArray? = null) : ViewModel(),
    StateParent, LifecycleEventObserver {

    private var boundLifecycleOwner: LifecycleOwner? = null

    private val handler = Handler(Looper.getMainLooper())

    private var onStateChanged: (() -> Unit)? = null

    private var onTwoWayPropChanged: ((propertyName: String) -> Unit)? = null

    private val onStateChangedListenerCaller = Runnable {
        callOnStateChangedListener(true)
    }

    internal var hasMissedStateChange = false

    protected fun <T> state(initialValue: T, onChanged: ((T) -> Unit)? = null) =
        StateDelegate(this, initialValue, null, onChanged)

    @CallSuper
    override fun onStateChanged(renderImmediately: Boolean) {
        callOnStateChangedListener(renderImmediately)
    }


    fun setListeners(
        owner: LifecycleOwner,
        onStateChanged: () -> Unit,
        onTwoWayPropChanged: (propertyName: String) -> Unit
    ) {
        this.onStateChanged = onStateChanged
        this.onTwoWayPropChanged = onTwoWayPropChanged
        boundLifecycleOwner = owner
        owner.lifecycle.addObserver(this)
        if (hasMissedStateChange) {
            hasMissedStateChange = false
            onStateChanged()
        }
    }

    private fun callOnStateChangedListener(renderImmediately: Boolean) {
        onStateChanged?.let {
            handler.removeCallbacks(onStateChangedListenerCaller)
            if (renderImmediately && Thread.currentThread() === Looper.getMainLooper().thread)
                it()
            else
                handler.post(onStateChangedListenerCaller)
        } ?: run { hasMissedStateChange = true }
    }

    fun callOnTwoWayPropChangedListener(propertyName: String) {
        onTwoWayPropChanged?.invoke(propertyName)
    }

    private fun removeListeners() {
        onStateChanged = null
        onTwoWayPropChanged = null
        handler.removeCallbacks(onStateChangedListenerCaller)
        boundLifecycleOwner?.lifecycle?.removeObserver(this)
        boundLifecycleOwner = null
    }

    final override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val boundLifecycleOwner = boundLifecycleOwner
        if (source !== boundLifecycleOwner) return
        onLifecycleEvent(event)
        if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) removeListeners()
    }

    open fun onLifecycleEvent(event: Lifecycle.Event) = Unit

    public override fun onCleared() = Unit
}