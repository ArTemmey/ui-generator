package ru.impression.ui_generator_base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

class DataBindingManager(private val parentComponent: Component<*, *>) {

    private val context by lazy {
        (parentComponent as? Fragment)?.context ?: (parentComponent as? View)?.context
    }

    private var currentBinding: ViewDataBinding? = null

    private var currentLayoutResId: Int? = null

    internal fun updateBinding(
        newLayoutResId: Int?,
        immediately: Boolean,
        attachToRoot: Boolean
    ): ViewDataBinding? {
        currentBinding?.let {
            if (newLayoutResId != null && newLayoutResId == currentLayoutResId) {
                it.setViewModel(parentComponent.viewModel)
                if (immediately) it.executePendingBindings()
                return it
            }
            (parentComponent.container as? ViewGroup)?.removeAllViews()
        }
        currentBinding = newLayoutResId
            ?.let { inflateBinding(it, attachToRoot) }
            ?.apply {
                prepare()
                if (immediately) executePendingBindings()
            }
        currentLayoutResId = newLayoutResId
        return currentBinding
    }


    private fun inflateBinding(layoutResId: Int, attachToRoot: Boolean): ViewDataBinding? {
        return DataBindingUtil.inflate(
            LayoutInflater.from(context ?: return null),
            layoutResId,
            parentComponent.container as? ViewGroup,
            attachToRoot
        )
    }

    private fun ViewDataBinding.prepare() {
        this.lifecycleOwner = parentComponent.boundLifecycleOwner
        setViewModel(parentComponent.viewModel)
        setVariable("component", parentComponent)
        setVariable("context", context)
    }

    fun releaseBinding() {
        currentBinding = null
        currentLayoutResId = null
    }
}
