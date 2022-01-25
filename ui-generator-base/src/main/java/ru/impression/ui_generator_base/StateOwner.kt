package ru.impression.ui_generator_base

import ru.impression.singleton_entity.SingletonEntityParent

interface StateOwner : SingletonEntityParent {
    fun onStateChanged(renderImmediately: Boolean = false)
}