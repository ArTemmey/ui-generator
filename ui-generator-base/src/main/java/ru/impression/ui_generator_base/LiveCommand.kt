package ru.impression.ui_generator_base

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class LiveCommand : MutableLiveData<Unit>() {

    @Volatile
    var takeValue = false

    override fun observe(owner: LifecycleOwner, observer: Observer<in Unit>) {
        super.observe(
            owner,
            Observer {
                if (takeValue) observer.onChanged(it)
                takeValue = false
            }
        )
    }

    override fun setValue(value: Unit?) {
        takeValue = true
        super.setValue(value)
    }

    override fun postValue(value: Unit?) {
        takeValue = true
        super.postValue(value)
    }

    operator fun invoke() {
        if (Thread.currentThread() == Looper.getMainLooper().thread)
            value = Unit
        else
            postValue(Unit)
    }
}