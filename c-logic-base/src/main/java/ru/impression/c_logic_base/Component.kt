package ru.impression.c_logic_base

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation


interface Component<C, VM : ComponentViewModel> {

    val scheme: ComponentScheme<C, VM>

    val viewModel: VM?

    val container: View

    val lifecycleOwner: LifecycleOwner

    val renderer: Renderer

    fun render() {
        renderer.render(
            viewModel?.let { scheme.getBindingClass?.invoke(this as C, it) }
                ?: renderer.binding?.let { it::class }
        )
    }

    fun startObservations() {
        val viewModel = viewModel ?: return
        viewModel.stateChange.observe(lifecycleOwner, Observer { render() })
        viewModel.sharedProperties.forEach { viewModelAndProperties ->
            if (viewModelAndProperties.key.findAnnotation<SharedViewModel>() == null)
                throw UnsupportedOperationException(
                    "Cannot share properties of viewModel " +
                            "${viewModelAndProperties.key}, that is not shared view model."
                )
            val sourceViewModel = createViewModel(viewModelAndProperties.key)
            sourceViewModel.addOnStatePropertyChangedListener(
                lifecycleOwner,
                { property, value ->
                    viewModelAndProperties.value[property]?.forEach {
                        (it as KMutableProperty1<ComponentViewModel, Any?>).set(viewModel, value)
                    }
                }
            )
        }
    }
}