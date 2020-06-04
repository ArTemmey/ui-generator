package ru.impression.c_logic_base

import android.view.View.GONE
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

class Renderer(private val component: Component<*, *>) {

    var binding: ViewDataBinding? = null

    fun render(newBindingClass: KClass<out ViewDataBinding>?) {
        if (newBindingClass != null)
            binding?.let {
                if (it::class == newBindingClass) {
                    it.setViewModel(component.viewModel)
                    it.executePendingBindings()
                    return
                }
            }
        val newBinding = newBindingClass?.inflate(
            component.container as? ViewGroup
                ?: throw UnsupportedOperationException("Component must be ViewGroup"),
            component.viewModel,
            component.lifecycleOwner
        )
        binding?.let {
            it.unbind()
            (component.container as? ViewGroup)?.removeAllViews()
        }
        binding = newBinding
        newBinding?.executePendingBindings() ?: run { component.container.visibility = GONE }
    }
}