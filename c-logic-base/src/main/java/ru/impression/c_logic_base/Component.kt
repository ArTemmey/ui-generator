package ru.impression.c_logic_base

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.full.findAnnotation


interface Component<C, VM : ComponentViewModel> {

    val scheme: ComponentScheme<C, VM>

    val viewModel: VM

    val container: View

    val lifecycleOwner: LifecycleOwner

    val renderer: Renderer

    fun render() {
        renderer.render(scheme.getBindingClass?.invoke(this as C, viewModel))
    }

    fun startObservations() {
        viewModel.render.observe(lifecycleOwner, Observer { render() })
        for (viewModelAndProperties in viewModel.sharedProperties) {
            if (viewModelAndProperties.key.findAnnotation<SharedViewModel>() == null) continue
            val sourceViewModel = createViewModel(viewModelAndProperties.key)
            sourceViewModel.addOnPropertyChangedListener(
                lifecycleOwner,
                { property, value ->
                    viewModelAndProperties.value[property]?.forEach { it.set(viewModel, value) }
                }
            )
        }
    }
}