package ru.impression.c_logic_base

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KMutableProperty1
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
        viewModel.stateChange.observe(lifecycleOwner, Observer { render() })
        viewModel.sharedProperties.forEach { viewModelAndProperties ->
            if (viewModelAndProperties.key.findAnnotation<SharedViewModel>() == null)
                throw UnsupportedOperationException(
                    "Cannot share properties of viewModel " +
                            "${viewModelAndProperties.key}, that is not shared view model."
                )
            val sourceViewModel = createViewModel(viewModelAndProperties.key)
            sourceViewModel.addOnPropertyChangedListener(
                lifecycleOwner,
                { property, value ->
                    viewModelAndProperties.value[property]
                        ?.forEach { it.setter.call(viewModel, value) }
                }
            )
        }
    }
}