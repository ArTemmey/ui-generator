package ru.impression.ui_generator_base

public interface StateParent {
    fun onStateChanged(renderImmediately: Boolean = false)
}