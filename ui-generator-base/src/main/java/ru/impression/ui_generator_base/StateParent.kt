package ru.impression.ui_generator_base

interface StateParent {
    fun onStateChanged(renderImmediately: Boolean = false)
}