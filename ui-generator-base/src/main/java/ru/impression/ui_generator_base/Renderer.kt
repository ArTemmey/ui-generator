package ru.impression.ui_generator_base

import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

class Renderer(private val component: Component<*, *>) {

    private val context by lazy {
        (component as? Fragment)?.context ?: (component as? View)?.context
    }

    private var currentBinding: ViewDataBinding? = null

    private var currentBindingClass: KClass<out ViewDataBinding>? = null

    internal fun render(
        newBindingClass: KClass<out ViewDataBinding>?,
        immediately: Boolean,
        attachToRoot: Boolean
    ): ViewDataBinding? {
        currentBinding?.let {
            if (newBindingClass != null && newBindingClass == currentBindingClass) {
                it.setViewModel(component.viewModel)
                if (immediately) it.executePendingBindings()
                return it
            }
            (component.container as? ViewGroup)?.removeAllViews()
        }
        currentBinding = newBindingClass?.inflate(attachToRoot)?.apply {
            prepare()
            if (immediately) executePendingBindings()
        }
        currentBindingClass = newBindingClass
        return currentBinding
    }

    private fun KClass<out ViewDataBinding>.inflate(attachToRoot: Boolean): ViewDataBinding? {
        return inflate(context ?: return null, component.container as? ViewGroup, attachToRoot)
    }

    private fun ViewDataBinding.prepare() {
        this.lifecycleOwner = component.boundLifecycleOwner
        setViewModel(component.viewModel)
        safeCallSetter("setComponent", component)
        safeCallSetter("setContext", context)
    }

    fun release() {
        currentBinding = null
        currentBindingClass = null
    }
}