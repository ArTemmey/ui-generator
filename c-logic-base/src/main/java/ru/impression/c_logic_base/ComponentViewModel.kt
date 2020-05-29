package ru.impression.c_logic_base

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

abstract class ComponentViewModel : ViewModel() {

    @PublishedApi
    internal val propertyDuplicates = ArrayList<PropertyDuplicate>()

    inline fun <reified T : ViewModel, R> duplicate(
        property: KProperty1<T, LiveData<R>>,
        noinline observer: ((R) -> Unit) = {}
    ) = Data<R>(null, observer).apply {
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
    ) = Data<R>(null, observer).apply {
        propertyDuplicates.add(
            PropertyDuplicate(
                T::class,
                property as KProperty1<Any?, LiveData<out Any?>>,
                this,
                true
            )
        )
    }

    class PropertyDuplicate(
        val viewModelClass: KClass<out ViewModel>,
        val property: KProperty1<Any?, LiveData<out Any?>>,
        val target: Data<out Any?>,
        val isMutable: Boolean
    )

    companion object {

        fun <T : ComponentViewModel> create(viewModelClass: KClass<T>, owner: Any): T {
            val activity =
                (owner as? View)?.activity ?: (owner as? Fragment)?.activity
                ?: return viewModelClass.createInstance()
            return when {
                viewModelClass.findAnnotation<SharedViewModel>() != null ->
                    ViewModelProvider(activity)[viewModelClass.java]
                owner is ViewModelStoreOwner -> ViewModelProvider(owner)[viewModelClass.java]
                else -> viewModelClass.createInstance()
            }
        }
    }

    class Data<T>(value: T? = null, onChange: ((T) -> Unit)? = null) :
        MutableLiveData<T>() {

        init {
            onChange?.let { observeForever { it(it) } }
        }

        fun get() = value

        fun set(value: T?) = setValue(value)

        override fun setValue(value: T?) {
            if (value != this.value) super.setValue(value)
        }
    }
}