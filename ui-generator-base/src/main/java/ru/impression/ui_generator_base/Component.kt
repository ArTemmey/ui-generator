package ru.impression.ui_generator_base

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import ru.impression.ui_generator_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation


interface Component<C, VM : ComponentViewModel> {

    val scheme: ComponentScheme<C, VM>

    val viewModel: VM

    val container: View?

    val boundLifecycleOwner: LifecycleOwner

    val renderer: Renderer

    fun <T : ComponentViewModel> createViewModel(viewModelClass: KClass<T>): T {
        val activity = when (this) {
            is View -> activity
            is Fragment -> activity
            else -> null
        }
        val result = when {
            activity != null && viewModelClass.findAnnotation<SharedViewModel>() != null ->
                ViewModelProvider(activity)[viewModelClass.java]
            activity != null && this is ViewModelStoreOwner ->
                ViewModelProvider(this)[viewModelClass.java]
            else -> viewModelClass.createInstance()
        }
        return result.apply { owner = boundLifecycleOwner }
    }

    fun startObservations() {
        viewModel.onStateChangedListener = { render() }
        for (viewModelAndProperties in viewModel.sharedProperties) {
            if (viewModelAndProperties.key.findAnnotation<SharedViewModel>() == null) continue
            val sourceViewModel = createViewModel(viewModelAndProperties.key)
            sourceViewModel.addOnPropertyChangedListener(
                boundLifecycleOwner,
                { property, value ->
                    viewModelAndProperties.value[property]?.forEach { it.set(viewModel, value) }
                }
            )
        }
    }

    fun render(attachToContainer: Boolean = true) {
        renderer.render(scheme.getBindingClass?.invoke(this as C, viewModel), attachToContainer)
    }
}