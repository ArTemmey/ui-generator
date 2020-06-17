package ru.impression.ui_generator_base

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

class Renderer(private val component: Component<*, *>) {

    var currentBinding: ViewDataBinding? = null

    var currentBindingClass: KClass<out ViewDataBinding>? = null

    fun render(newBindingClass: KClass<out ViewDataBinding>?, attachToContainer: Boolean) {
        currentBinding?.let {
            if (newBindingClass != null && newBindingClass == currentBindingClass) {
                it.setViewModel(component.viewModel)
                it.executePendingBindings()
                return
            }
            it.unbind()
            (component.container as? ViewGroup)?.removeAllViews()
        }
        currentBinding = newBindingClass?.inflate(
            component.container as? ViewGroup
                ?: throw UnsupportedOperationException("Component must be ViewGroup"),
            component.viewModel,
            component.boundLifecycleOwner,
            attachToContainer
        )?.apply { executePendingBindings() }
        currentBindingClass = newBindingClass
    }
}