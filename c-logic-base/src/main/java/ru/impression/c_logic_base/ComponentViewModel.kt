package ru.impression.c_logic_base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class ComponentViewModel : ViewModel() {

    @PublishedApi
    internal val propertyDuplicates = ArrayList<PropertyDuplicate>()

    inline fun <reified T : ViewModel, R> duplicate(
        property: KProperty1<T, LiveData<R>>,
        noinline observer: ((R) -> Unit) = {}
    ) = Observable<R>(null, observer).apply {
        propertyDuplicates.add(
            PropertyDuplicate(
                T::class,
                property as KProperty1<Any?, LiveData<out Any?>>,
                this,
                false
            )
        )
    }

    inline fun <reified T : ViewModel, R> mutableDuplicate(
        property: KProperty1<T, MutableLiveData<R>>,
        noinline observer: ((R) -> Unit) = {}
    ) = Observable<R>(null, observer)

    class PropertyDuplicate(
        val viewModelClass: KClass<out ViewModel>,
        val property: KProperty1<Any?, LiveData<out Any?>>,
        val target: Observable<out Any?>,
        val isMutable: Boolean
    )

    class Observable<T>(value: T? = null, observer: ((T) -> Unit)? = null) :
        MutableLiveData<T>() {

        init {
            observer?.let { observeForever { it(it) } }
        }

        override fun setValue(value: T?) {
            if (value != this.value) super.setValue(value)
        }
    }
}