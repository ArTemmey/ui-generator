package ru.impression.ui_generator_base

class Hooks {

    private val initBlocks = ArrayList<() -> Unit>()

    private var initBlocksCalled = false

    fun addInitBlock(block: () -> Unit) {
        if (initBlocksCalled) return
        initBlocks.add(block)
    }

    fun callInitBlocks() {
        initBlocksCalled = true
        initBlocks.forEach { it() }
        initBlocks.clear()
    }
}

class LifecycleScope {

    private val onCreateBlocks = ArrayList<() -> Unit>()
    private val onStartBlocks = ArrayList<() -> Unit>()
    private val onResumeBlocks = ArrayList<() -> Unit>()
    private val onPauseBlocks = ArrayList<() -> Unit>()
    private val onStopBlocks = ArrayList<() -> Unit>()
    private val onDestroyBlocks = ArrayList<() -> Unit>()

    fun onCreate(block: () -> Unit) {
        onCreateBlocks.add(block)
    }

    internal fun callOnCreateBlocks() {
        onCreateBlocks.forEach { it() }
    }

    fun onStart(block: () -> Unit) {
        onStartBlocks.add(block)
    }

    internal fun callOnStartBlocks() {
        onStartBlocks.forEach { it() }
    }

    fun onResume(block: () -> Unit) {
        onResumeBlocks.add(block)
    }

    internal fun callOnResumeBlocks() {
        onResumeBlocks.forEach { it() }
    }

    fun onPause(block: () -> Unit) {
        onPauseBlocks.add(block)
    }

    internal fun callOnPauseBlocks() {
        onPauseBlocks.forEach { it() }
    }

    fun onStop(block: () -> Unit) {
        onStopBlocks.add(block)
    }

    internal fun callOnStopBlocks() {
        onStopBlocks.forEach { it() }
    }

    fun onDestroy(block: () -> Unit) {
        onDestroyBlocks.add(block)
    }

    internal fun callOnDestroyBlocks() {
        onDestroyBlocks.forEach { it() }
    }
}