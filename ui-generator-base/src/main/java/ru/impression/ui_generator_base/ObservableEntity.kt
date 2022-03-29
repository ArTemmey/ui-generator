package ru.impression.ui_generator_base

import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityDelegate
import ru.impression.singleton_entity.SingletonEntityParent
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty


interface ObservableEntity : StateOwner {

    fun <T> state(
        initialValue: T,
        onChanged: ((T) -> Unit)? = null
    ): PropertyDelegateProvider<ObservableEntity, StateDelegate<ObservableEntity, T>>

    fun addStateOwner(stateOwner: StateOwner)

    fun removeStateOwner(stateOwner: StateOwner)
}

class ObservableEntityImpl : ObservableEntity {
    private val stateOwners = CopyOnWriteArrayList<StateOwner>()
    private val delegates = ArrayList<StateDelegate<*, *>>()
    private val singletonEntityParent: SingletonEntityParent = object : SingletonEntityParent {
        override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
            delegates.forEach {
                if (it.value === oldEntity)
                    (it as StateDelegate<*, SingletonEntity>).setValue(newEntity)
            }
        }
    }

    override fun <T> state(initialValue: T, onChanged: ((T) -> Unit)?) =
        PropertyDelegateProvider<ObservableEntity, StateDelegate<ObservableEntity, T>> { _, property ->
            StateDelegate(
                parent = this@ObservableEntityImpl,
                singletonEntityParent = singletonEntityParent,
                initialValue = initialValue,
                onChanged = onChanged,
                property = property
            )
                .also { delegates.add(it) } as StateDelegate<ObservableEntity, T>
        }

    override fun addStateOwner(stateOwner: StateOwner) {

        stateOwners.add(stateOwner)
    }

    override fun removeStateOwner(stateOwner: StateOwner) {
        stateOwners.remove(stateOwner)
    }

    override fun onStateChanged(renderImmediately: Boolean) {
        stateOwners.forEach { it?.onStateChanged(renderImmediately) }
    }
}