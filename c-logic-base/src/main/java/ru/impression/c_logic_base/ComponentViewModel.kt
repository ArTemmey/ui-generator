package ru.impression.c_logic_base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.reflect.KProperty1

abstract class ComponentViewModel : ViewModel() {

    fun <T> observable(value: T? = null, observer: (Observable<T>.(T) -> Unit)? = null) =
        Observable(value, observer)

    inline fun <reified T, R> duplicate(
        property: KProperty1<T, MutableLiveData<R>>,
        noinline observer: (MutableLiveData<R>.(R) -> Unit) = {}
    ) = Observable(observer)

    class Observable<T>(value: T? = null, observer: (Observable<T>.(T) -> Unit)? = null) :
        MutableLiveData<T>() {

        init {
            observer?.let { observeForever { it(this, it) } }
        }

        override fun setValue(value: T?) {
            if (value != this.value) super.setValue(value)
        }
    }
}