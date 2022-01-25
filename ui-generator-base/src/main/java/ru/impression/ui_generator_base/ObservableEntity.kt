package ru.impression.ui_generator_base

import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityDelegate

interface ObservableEntity : StateOwner {

    fun <T> state(
        initialValue: T,
        onChanged: ((T) -> Unit)? = null
    ): StateDelegate<ObservableEntity, T>

    fun addStateOwner(stateOwner: StateOwner)

    fun removeStateOwner(stateOwner: StateOwner)
}

class ObservableEntityImpl : ObservableEntity {
    private val stateOwners = ArrayList<StateOwner>()
    private val delegates = ArrayList<StateDelegate<*, *>>()
    private val singletonEntityDelegates = ArrayList<SingletonEntityDelegate<*>>()

    override fun <T> state(initialValue: T, onChanged: ((T) -> Unit)?) =
        StateDelegate(this, initialValue, onChanged)
            .also { delegates.add(it) } as StateDelegate<ObservableEntity, T>

    override fun addStateOwner(stateOwner: StateOwner) {
        stateOwners.add(stateOwner)
    }

    override fun removeStateOwner(stateOwner: StateOwner) {
        stateOwners.remove(stateOwner)
    }

    override fun onStateChanged(renderImmediately: Boolean) {
        stateOwners.forEach { it.onStateChanged(renderImmediately) }
    }


    override fun <T : SingletonEntity?> singletonEntity(initialValue: T) =
        SingletonEntityDelegate(this, initialValue).also { singletonEntityDelegates.add(it) }

    override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
        singletonEntityDelegates.forEach {
            if (it.value === oldEntity)
                (it as SingletonEntityDelegate<SingletonEntity>).setValue(newEntity)
        }
        delegates.forEach {
            if (it.value === oldEntity)
                (it as StateDelegate<*, SingletonEntity>).setValue(newEntity)
        }
    }

    override fun detachFromEntities() {
        singletonEntityDelegates.forEach { it.value?.removeParent(this) }
        delegates.forEach { it.stopObserveValue() }
    }
}