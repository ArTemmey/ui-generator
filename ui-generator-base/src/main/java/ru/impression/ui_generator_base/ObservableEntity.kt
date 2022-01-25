package ru.impression.ui_generator_base

import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityDelegate
import ru.impression.singleton_entity.SingletonEntityParent

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
    internal val singletonEntityParent: SingletonEntityParent = object : SingletonEntityParent {
        override fun detachFromEntities() {
            this@ObservableEntityImpl.detachFromEntities()
        }

        override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
            this@ObservableEntityImpl.replace(oldEntity, newEntity)
        }

        override fun <T : SingletonEntity?> singletonEntity(initialValue: T) =
            this@ObservableEntityImpl.singletonEntity(initialValue)
    }

    override fun <T> state(initialValue: T, onChanged: ((T) -> Unit)?) =
        StateDelegate(this, singletonEntityParent, initialValue, onChanged)
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
}