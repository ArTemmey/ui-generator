package ru.impression.ui_generator_base

import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityDelegate

interface SingletonEntityParentSource {
    fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity)
    fun <T : SingletonEntity?> singletonEntity(initialValue: T): SingletonEntityDelegate<T>
    fun detachFromEntities()
}

interface StateOwner : SingletonEntityParentSource {
    fun onStateChanged(renderImmediately: Boolean = false)
}