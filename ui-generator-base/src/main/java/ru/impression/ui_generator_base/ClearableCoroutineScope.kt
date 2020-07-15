package ru.impression.ui_generator_base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

interface ClearableCoroutineScope : CoroutineScope {
    fun clear()
}

class ClearableCoroutineScopeImpl(coroutineContext: CoroutineContext) :
    ClearableCoroutineScope {

    override var coroutineContext: CoroutineContext = coroutineContext + Job()

    override fun clear() {
        coroutineContext[Job]?.cancel()
            ?.also { coroutineContext = coroutineContext.minusKey(Job) + Job() }
    }
}