package ru.impression.ui_generator_base

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

class DataBindingManager(private val component: Component<*, *>) {

    private val context by lazy {
        (component as? Fragment)?.context ?: (component as? View)?.context
    }

    private var currentBinding: ViewDataBinding? = null

    private var currentLayoutResId: Int? = null

    private val handler = Handler()

    internal fun updateBinding(
        newLayoutResId: Int?,
        attachToContainer: Boolean,
        executeBindingsImmediately: Boolean
    ): ViewDataBinding? {
        currentBinding?.let {
            if (newLayoutResId != null && newLayoutResId == currentLayoutResId) {
                it.setViewModel(component.viewModel)
                if (executeBindingsImmediately) it.executePendingBindings()
                return it
            }
            (component.container as? ViewGroup)?.removeAllViews()
        }
        newLayoutResId?.let { inflateBinding(it, attachToContainer) }
            ?.apply { prepare(executeBindingsImmediately) }
            ?.also {
                currentBinding = it
                currentLayoutResId = newLayoutResId
                onBindingCreated(attachToContainer, executeBindingsImmediately)
            }
        return currentBinding
    }


    private fun inflateBinding(layoutResId: Int, attachToContainer: Boolean): ViewDataBinding? {
        return DataBindingUtil.inflate(
            LayoutInflater.from(context ?: return null),
            layoutResId,
            component.container as? ViewGroup,
            attachToContainer
        )
    }

    private fun ViewDataBinding.prepare(executeBindings: Boolean) {
        this.lifecycleOwner = component.boundLifecycleOwner
        setViewModel(component.viewModel)
        setVariable("component", component)
        setVariable("context", context)
        if (executeBindings) executePendingBindings()
    }

    private fun onBindingCreated(
        attachedToContainer: Boolean,
        executeBindingsImmediately: Boolean
    ) {
        if (attachedToContainer)
            component.render(executeBindingsImmediately)
        else
            handler.post { component.render(true) }
    }

    fun releaseBinding() {
        currentBinding = null
        currentLayoutResId = null
    }
}
