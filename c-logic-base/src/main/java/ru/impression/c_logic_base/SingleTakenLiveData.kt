package ru.impression.c_logic_base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class SingleTakenLiveData<T> : MutableLiveData<T>() {

    private var lastTakenValue: T? = null

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer {
            if (lastTakenValue == it) return@Observer
            lastTakenValue = it
            observer.onChanged(it)
        })
    }
}