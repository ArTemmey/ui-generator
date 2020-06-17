package ru.impression.ui_generator_base

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

class Renderer(private val component: Component<*, *>) {

    private var currentBinding: ViewDataBinding? = null

    private var currentBindingClass: KClass<out ViewDataBinding>? = null

    fun render(
        newBindingClass: KClass<out ViewDataBinding>?,
        attachToContainer: Boolean
    ): ViewDataBinding? {
        currentBinding?.let {
            if (newBindingClass != null && newBindingClass == currentBindingClass) {
                it.setViewModel(component.viewModel)
                return it
            }
            (component.container as? ViewGroup)?.removeAllViews()
        }
        currentBinding = newBindingClass?.inflate(
            component.container as? ViewGroup
                ?: throw UnsupportedOperationException("Component must be ViewGroup"),
            component.viewModel,
            component.boundLifecycleOwner,
            attachToContainer
        )
        currentBindingClass = newBindingClass
        return currentBinding
    }

    fun release() {
        currentBinding = null
        currentBindingClass = null
    }
}