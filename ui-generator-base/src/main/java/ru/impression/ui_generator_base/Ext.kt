package ru.impression.ui_generator_base

import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.reflect.*

val View.activity: AppCompatActivity?
    get() {
        var contextWrapper = (context as? ContextWrapper)
        while (contextWrapper !is AppCompatActivity) {
            contextWrapper =
                contextWrapper?.baseContext as ContextWrapper? ?: return null
        }
        return contextWrapper
    }

internal fun KClass<out ViewDataBinding>.inflate(
    component: Component<*, *>,
    attachToRoot: Boolean
) = (component.container as? ViewGroup).let {
    (java.getDeclaredMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.javaPrimitiveType
    ).invoke(
        null,
        LayoutInflater.from((component as? Fragment)?.context ?: (component as? View)?.context),
        it,
        attachToRoot
    ) as ViewDataBinding).apply {
        this.lifecycleOwner = component.boundLifecycleOwner
        setComponent(component)
        setViewModel(component.viewModel)
    }
}

internal fun ViewDataBinding.setViewModel(viewModel: ComponentViewModel) {
    this::class.java.getDeclaredMethod("setViewModel", viewModel::class.java)
        .invoke(this, viewModel)
}

internal fun ViewDataBinding.setComponent(component: Component<*, *>) {
    this::class.java.declaredMethods.firstOrNull {
        val parameterTypes = it.parameterTypes
        it.name == "setComponent"
                && parameterTypes.size == 1
                && parameterTypes[0].isAssignableFrom(component::class.java)
    }?.invoke(this, component)
}

fun KProperty<*>.get(receiver: Any?) =
    when (this) {
        is KProperty0<*> -> get()
        is KProperty1<*, *> -> (this as KProperty1<Any?, Any?>).get(receiver)
        else -> throw UnsupportedOperationException("Unsupported property")
    }

fun KMutableProperty<*>.set(receiver: Any?, value: Any?) {
    if (!this.returnType.isMarkedNullable && value == null) return
    when (this) {
        is KMutableProperty0<*> -> (this as KMutableProperty0<Any?>).set(value)
        is KMutableProperty1<*, *> -> (this as KMutableProperty1<Any?, Any?>).set(receiver, value)
    }
}

val KMutableProperty0<*>.isLoading: Boolean
    get() = getDelegateFromSum<StateDelegate<*, *>>()?.isLoading == true


fun KMutableProperty0<*>.reload() {
    getDelegateFromSum<StateDelegate<*, *>>()?.load(true)
}