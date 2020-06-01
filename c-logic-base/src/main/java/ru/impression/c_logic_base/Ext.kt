package ru.impression.c_logic_base

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
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

fun KClass<out ViewDataBinding>.inflate(
    container: ViewGroup,
    viewModel: ComponentViewModel,
    lifecycleOwner: LifecycleOwner
) = (java.getMethod(
    "inflate",
    LayoutInflater::class.java,
    ViewGroup::class.java,
    Boolean::class.javaPrimitiveType
).invoke(
    null,
    LayoutInflater.from(container.context),
    container,
    true
) as ViewDataBinding).apply {
    this.lifecycleOwner = lifecycleOwner
    setViewModel(viewModel)
}

fun ViewDataBinding.setViewModel(viewModel: ComponentViewModel) {
    var packageName = viewModel::class.java.`package`!!.name
    var br: Class<*>? = null
    while (true) {
        try {
            br = Class.forName("$packageName.BR")
            break
        } catch (e: ClassNotFoundException) {
            val nextPackageName = packageName.substringBeforeLast('.')
            if (nextPackageName == packageName) break
            packageName = nextPackageName
        }
    }
    setVariable(br!!.getField("viewModel").getInt(null), viewModel)
}

fun <T : ComponentViewModel> Any.createViewModel(viewModelClass: KClass<T>): T {
    val activity =
        (this as? View)?.activity ?: (this as? Fragment)?.activity
        ?: return viewModelClass.createInstance()
    return when {
        viewModelClass.findAnnotation<SharedViewModel>() != null ->
            ViewModelProvider(activity)[viewModelClass.java]
        this is ViewModelStoreOwner -> ViewModelProvider(this)[viewModelClass.java]
        else -> viewModelClass.createInstance()
    }
}

//private class ViewModelFactory(vararg arguments: Any?) : ViewModelProvider.Factory {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//    }
//}
