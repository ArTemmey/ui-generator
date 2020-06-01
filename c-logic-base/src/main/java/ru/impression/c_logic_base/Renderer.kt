package ru.impression.c_logic_base

import android.view.View.GONE
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

class Renderer(private val component: Component<*, *>) {

    private var binding: ViewDataBinding? = null

    fun render(bindingClass: KClass<out ViewDataBinding>?) {
        if (bindingClass != null) {
            if (bindingClass == binding?.let { it::class }) {
                binding?.setViewModel(component.viewModel)
            }
            (component.container as? ViewGroup)?.let {
                it.removeAllViews()
                binding = bindingClass.inflate(it, component.viewModel, component.lifecycleOwner)
            } ?: throw UnsupportedOperationException("")
        } else {
            binding = null
            (component.container as? ViewGroup)?.let {
                it.removeAllViews()
                if (it.visibility != GONE) it.visibility = GONE
            }
        }
    }
}