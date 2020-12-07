package ru.impression.ui_generator_base

interface StateOwner {
    fun onStateChanged(renderImmediately: Boolean = false)
}