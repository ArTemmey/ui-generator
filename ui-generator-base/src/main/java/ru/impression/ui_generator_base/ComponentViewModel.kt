package ru.impression.ui_generator_base

import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityParent
import ru.impression.ui_generator_annotations.SharedViewModel
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.full.hasAnnotation


abstract class ComponentViewModel(val attrs: IntArray? = null) : ViewModel(), StateOwner,
    LifecycleEventObserver {

    internal val delegates = ArrayList<StateDelegate<*, *>>()

    internal val delegateToAttrs = HashMap<StateDelegate<*, *>, Int>()

    @PublishedApi
    internal var _component: Component<*, *>? = null

    internal var componentHasMissedStateChange = false

    private val stateObservers = HashMap<LifecycleOwner, HashSet<() -> Unit>>()

    private val handler = Handler(Looper.getMainLooper())

    private val stateObserversNotifier = Runnable { notifyStateObservers(true) }

    private val subscriptionsInitializers = ArrayList<(() -> Unit)>()

    internal val singletonEntityParent: SingletonEntityParent = object : SingletonEntityParent {
        override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
            delegates.forEach {
                if (it.value === oldEntity)
                    (it as StateDelegate<*, SingletonEntity>).setValue(newEntity)
            }
        }
    }

    protected fun <T> state(initialValue: T, attr: Int? = null, onChanged: ((T) -> Unit)? = null) =
        StateDelegate(this, singletonEntityParent, initialValue, onChanged).also { delegate ->
            delegates.add(delegate)
            attr?.let { delegateToAttrs[delegate] = it }
        }

    @CallSuper
    override fun onStateChanged(renderImmediately: Boolean) {
        notifyStateObservers(renderImmediately)
    }

    fun setComponent(component: Component<*, *>) {
        this._component = component
        component.boundLifecycleOwner.lifecycle.addObserver(this)
        if (componentHasMissedStateChange) {
            componentHasMissedStateChange = false
            onStateChanged()
        }
    }

    private fun unsetComponent() {
        _component?.boundLifecycleOwner?.lifecycle?.removeObserver(this)
        _component = null
    }

    fun initSubscriptions(block: () -> Unit) {
        block()
        subscriptionsInitializers.add(block)
    }

    fun restoreSubscriptions() {
        delegates.forEach { it.observeValue() }
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
            _component?.render(executeBindingsImmediately = true)
                ?: run { componentHasMissedStateChange = true }
            stateObservers.values.forEach { set -> set.forEach { it() } }
        } else {
            handler.post(stateObserversNotifier)
        }
    }

    internal fun notifyTwoWayPropChanged(propertyName: String) {
        _component?.onTwoWayPropChanged(propertyName)
    }

    inline fun <reified T : ViewModel> getSharedViewModel(): T {
        val viewModelClass = T::class
        val component = _component
        return when {
            !viewModelClass.hasAnnotation<SharedViewModel>() ->
                throw IllegalArgumentException("ViewModel must have SharedViewModel annotation")
            component == null ->
                throw IllegalStateException("Cannot get ViewModel when detached from component")
            else -> component.createViewModel(viewModelClass)
        }
    }

    final override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (source === _component?.boundLifecycleOwner) onLifecycleEvent(event)
        if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            if (source === _component?.boundLifecycleOwner) unsetComponent()
            removeStateObservers(source)
        }
    }

    open fun beforeRender() = Unit

    protected open fun onLifecycleEvent(event: Lifecycle.Event) = Unit

    open fun onSaveInstanceState(): Parcelable? = null

    open fun onRestoreInstanceState(savedInstanceState: Parcelable?) = Unit

    @CallSuper
    public override fun onCleared() {
        delegates.forEach { it.stopObserveValue() }
    }

    protected fun <T> KMutableProperty0<T>.set(value: T, renderImmediately: Boolean = false) {
        set(value)
        onStateChanged(renderImmediately)
    }
}