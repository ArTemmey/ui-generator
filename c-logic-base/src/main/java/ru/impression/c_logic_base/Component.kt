package ru.impression.c_logic_base

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
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
        viewModel.sharedProperties.forEach { sharedPropertiesOfCertainViewModel ->
            if (sharedPropertiesOfCertainViewModel.key.findAnnotation<SharedViewModel>() == null)
                throw UnsupportedOperationException("Cannot share properties of viewModel " +
                        "${sharedPropertiesOfCertainViewModel.key}, that is not shared view model.")
            val sourceViewModel = createViewModel(sharedPropertiesOfCertainViewModel.key)
            sourceViewModel.stateChange.observe(
                lifecycleOwner,
                Observer {
                    sharedPropertiesOfCertainViewModel.value.forEach { sharedProperty ->
                        (sharedProperty.value as ComponentViewModel.StateImpl<Any?>)
                            .setValue(sharedProperty.key.get(sourceViewModel))
                    }
                }
            )
        }
    }
}