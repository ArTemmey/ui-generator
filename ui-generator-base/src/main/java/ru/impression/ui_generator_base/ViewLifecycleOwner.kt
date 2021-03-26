package ru.impression.ui_generator_base

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class ViewLifecycleOwner(private val parent: View) : LifecycleOwner {

    private val lifecycle = SimpleLifecycle(this)

    private val onAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        override fun onViewDetachedFromWindow(v: View?) {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    init {
        parent.addOnAttachStateChangeListener(onAttachStateChangeListener)
    }

    override fun getLifecycle() = lifecycle

    fun destroy() {
        parent.removeOnAttachStateChangeListener(onAttachStateChangeListener)
    }
}