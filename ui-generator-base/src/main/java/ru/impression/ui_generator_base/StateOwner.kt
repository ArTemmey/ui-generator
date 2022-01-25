package ru.impression.ui_generator_base

import ru.impression.syncable_entity.SingletonEntityParent

interface StateOwner : SingletonEntityParent {
    fun onStateChanged(renderImmediately: Boolean = false)
}