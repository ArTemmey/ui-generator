package ru.impression.ui_generator_base

import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.lifecycle.*
import kotlin.reflect.KMutableProperty0

abstract class ComponentViewModel(val attrs: IntArray? = null) : ViewModel(), StateOwner,
    LifecycleEventObserver {

    internal val delegateToAttrs = HashMap<StateDelegate<*, *>, Int>()

    private var component: Component<*, *>? = null

    internal var componentHasMissedStateChange = false

    private val stateObservers = HashMap<LifecycleOwner, HashSet<() -> Unit>>()

    private val handler = Handler(Looper.getMainLooper())

    private val stateObserversNotifier = Runnable { notifyStateObservers(true) }

    private val subscriptionsInitializers = ArrayList<(() -> Unit)>()

    protected fun <T> state(initialValue: T, attr: Int? = null, onChanged: ((T) -> Unit)? = null) =
        StateDelegate(this, initialValue, onChanged)
            .also { delegate -> attr?.let { delegateToAttrs[delegate] = it } }

    @CallSuper
    override fun onStateChanged(renderImmediately: Boolean) {
        notifyStateObservers(renderImmediately)
    }

    fun setComponent(component: Component<*, *>) {
        this.component = component
        component.boundLifecycleOwner.lifecycle.addObserver(this)
        if (componentHasMissedStateChange) {
            componentHasMissedStateChange = false
            onStateChanged()
        }
    }

    private fun unsetComponent() {
        component?.boundLifecycleOwner?.lifecycle?.removeObserver(this)
        component = null
    }

    fun initSubscriptions(block: () -> Unit) {
        block()
        subscriptionsInitializers.add(block)
    }

    fun restoreSubscriptions() {
        subscriptionsInitializers.forEach { it() }
    }

    fun addStateObserver(lifecycleOwner: LifecycleOwner, observer: () -> Unit) {
        fun addActual() {
            val set = stateObservers[lifecycleOwner]
                ?: HashSet<() -> Unit>().also { stateObservers[lifecycleOwner] = it }
            set.add(observer)
            lifecycleOwner.lifecycle.addObserver(this)
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED))
            addActual()
        else
            lifecycleOwner.lifecycle.addObserver(
                object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (source == lifecycleOwner && event == Lifecycle.Event.ON_CREATE) {
                            addActual()
                            lifecycleOwner.lifecycle.removeObserver(this)
                        }
                    }
                }
            )

    }

    private fun removeStateObservers(lifecycleOwner: LifecycleOwner) {
        stateObservers.remove(lifecycleOwner)
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    private fun notifyStateObservers(immediately: Boolean) {
        handler.removeCallbacks(stateObserversNotifier)
        if (immediately && Thread.currentThread() === Looper.getMainLooper().thread) {
            component?.render(true) ?: run { componentHasMissedStateChange = true }
            stateObservers.values.forEach { set -> set.forEach { it() } }
        } else {
            handler.post(stateObserversNotifier)
        }
    }

    internal fun notifyTwoWayPropChanged(propertyName: String) {
        component?.onTwoWayPropChanged(propertyName)
    }

    final override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (source === component?.boundLifecycleOwner) onLifecycleEvent(event)
        if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            if (source === component?.boundLifecycleOwner) unsetComponent()
            removeStateObservers(source)
        }
    }

    protected open fun onLifecycleEvent(event: Lifecycle.Event) = Unit

    open fun onSaveInstanceState(): Parcelable? = null

    open fun onRestoreInstanceState(savedInstanceState: Parcelable?) = Unit

    public override fun onCleared() = Unit

    protected fun <T> KMutableProperty0<T>.set(value: T, renderImmediately: Boolean = false) {
        set(value)
        onStateChanged(renderImmediately)
    }
}