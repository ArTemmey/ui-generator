package ru.impression.c_logic_base

import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

val View.activity: AppCompatActivity?
    get() {
        var contextWrapper = (context as? ContextWrapper)
        while (contextWrapper !is AppCompatActivity) {
            contextWrapper =
                contextWrapper?.baseContext as ContextWrapper? ?: return null
        }
        return contextWrapper
    }

inline fun <reified T : ViewModel> Any.obtainViewModel(): T {
    val activity =
        (this as? View)?.activity ?: (this as? Fragment)?.activity
        ?: return T::class.createInstance()
    return when {
        T::class.findAnnotation<SharedViewModel>() != null ->
            ViewModelProvider(activity)[T::class.java]
        this is ViewModelStoreOwner -> ViewModelProvider(this)[T::class.java]
        else -> T::class.createInstance()
    }
}