package ru.impression.c_logic_base

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
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
    viewModel: ComponentViewModel?,
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

fun ViewDataBinding.setViewModel(viewModel: ComponentViewModel?) {
    this::class.java.getMethod("setViewModel", ComponentViewModel::class.java)
        .invoke(this, viewModel)
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

fun <R, T> R.argument(name: String): ReadWriteProperty<R, T?> where R : Fragment, R : Component<*, *> =
    ArgumentImpl(this, name)

private class ArgumentImpl<R, T>(private val fragment: R, private val name: String) :
    ReadWriteProperty<R, T?> where R : Fragment, R : Component<*, *> {

    override fun getValue(thisRef: R, property: KProperty<*>): T? = fragment.view
        ?.let {
            fragment.viewModel?.let { viewModel ->
                (viewModel::class.members.firstOrNull { it.name == name }
                        as? KProperty1<ComponentViewModel, T>)?.get(viewModel)
            }
        }
        ?: fragment.arguments?.get(name) as? T

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        fragment.arguments?.putAll(bundleOf(name to value))
        fragment.view?.let {
            fragment.viewModel?.let { viewModel ->
                (viewModel::class.members.firstOrNull { it.name == name }
                        as? KMutableProperty1<ComponentViewModel, T?>)?.apply {
                    if (value != null || returnType.isMarkedNullable) set(viewModel, value)
                }
            }
        }
    }
}