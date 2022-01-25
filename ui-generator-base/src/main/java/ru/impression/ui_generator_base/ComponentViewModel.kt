package ru.impression.ui_generator_base

import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.lifecycle.*
import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityDelegate
import ru.impression.singleton_entity.SingletonEntityParent
import ru.impression.ui_generator_annotations.SharedViewModel
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.full.hasAnnotation


abstract class ComponentViewModel(val attrs: IntArray? = null) : ViewModel(), StateOwner,
    LifecycleEventObserver {

    internal val delegates = ArrayList<StateDelegate<*, *>>()

    internal val delegateToAttrs = HashMap<StateDelegate<*, *>, Int>()

    private val singletonEntityDelegates = ArrayList<SingletonEntityDelegate<*>>()

    @PublishedApi
    internal var _component: Component<*, *>? = null

    internal var componentHasMissedStateChange = false

    private val stateObservers = HashMap<LifecycleOwner, HashSet<() -> Unit>>()

    private val handler = Handler(Looper.getMainLooper())

    private val stateObserversNotifier = Runnable { notifyStateObservers(true) }

    private val subscriptionsInitializers = ArrayList<(() -> Unit)>()

    internal val singletonEntityParent: SingletonEntityParent = object : SingletonEntityParent {
        override fun detachFromEntities() {
            this@ComponentViewModel.detachFromEntities()
        }

        override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
            this@ComponentViewModel.replace(oldEntity, newEntity)
        }

        override fun <T : SingletonEntity?> singletonEntity(initialValue: T) =
            this@ComponentViewModel.singletonEntity(initialValue)
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


    private fun <T : SingletonEntity?> singletonEntity(initialValue: T) =
        SingletonEntityDelegate(singletonEntityParent, initialValue)
            .also { singletonEntityDelegates.add(it) }

    private fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
        singletonEntityDelegates.forEach {
            if (it.value === oldEntity)
                (it as SingletonEntityDelegate<SingletonEntity>).setValue(newEntity)
        }
        delegates.forEach {
            if (it.value === oldEntity)
                (it as StateDelegate<*, SingletonEntity>).setValue(newEntity)
        }
    }

    private fun detachFromEntities() {
        singletonEntityDelegates.forEach { it.value?.removeParent(singletonEntityParent) }
        delegates.forEach { it.stopObserveValue() }
    }


    @CallSuper
    public override fun onCleared() {
        detachFromEntities()
    }

    protected fun <T> KMutableProperty0<T>.set(value: T, renderImmediately: Boolean = false) {
        set(value)
        onStateChanged(renderImmediately)
    }
}