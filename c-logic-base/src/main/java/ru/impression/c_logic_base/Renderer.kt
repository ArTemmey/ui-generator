package ru.impression.c_logic_base

import android.view.View.GONE
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

class Renderer(private val component: Component<*, *>) {

    var binding: ViewDataBinding? = null

    fun render(bindingClass: KClass<out ViewDataBinding>?) {
        if (bindingClass != null)
            binding?.let {
                if (it::class == bindingClass) {
                    it.setViewModel(component.viewModel)
                    it.executePendingBindings()
                    return
                }
            }
        replaceBinding(
            bindingClass?.inflate(
                component.container as? ViewGroup
                    ?: throw UnsupportedOperationException("Component must be ViewGroup"),
                component.viewModel,
                component.lifecycleOwner
            )?.apply { executePendingBindings() }
        )
    }

    private fun replaceBinding(newBinding: ViewDataBinding?) {
        binding?.let { oldBinding ->
            oldBinding.unbind()
            (component.container as? ViewGroup)?.removeAllViews()
        }
        binding = newBinding
        newBinding ?: run { component.container.visibility = GONE }
    }
}