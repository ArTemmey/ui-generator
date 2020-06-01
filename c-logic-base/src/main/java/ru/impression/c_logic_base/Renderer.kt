package ru.impression.c_logic_base

import android.view.View.GONE
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

internal class Renderer(private val component: Component) {

    private var currentBindingClass: KClass<out ViewDataBinding>? = null

    fun render(bindingClass: KClass<out ViewDataBinding>?) {
        if (bindingClass != null) {
            if (bindingClass == currentBindingClass) return
            currentBindingClass = bindingClass
            (component.container as? ViewGroup)?.let {
                it.removeAllViews()
                bindingClass.inflate(it, component.viewModel, component.lifecycleOwner)
            } ?: throw UnsupportedOperationException("")
        } else {
            (component.container as? ViewGroup)?.let {
                it.removeAllViews()
                if (it.visibility != GONE) it.visibility = GONE
            }
        }
    }
}