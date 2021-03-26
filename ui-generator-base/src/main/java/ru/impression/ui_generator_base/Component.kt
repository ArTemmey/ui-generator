package ru.impression.ui_generator_base

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import ru.impression.ui_generator_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation


interface Component<C, VM : ComponentViewModel> {

    val scheme: ComponentScheme<C, VM>

    val viewModel: VM

    val container: View?

    val boundLifecycleOwner: LifecycleOwner

    val dataBindingManager: DataBindingManager

    fun <T : ComponentViewModel> createViewModel(viewModelClass: KClass<T>): T {
        val activity = when (this) {
            is View -> activity
            is Fragment -> activity
            else -> null
        }
        return when {
            activity != null && viewModelClass.findAnnotation<SharedViewModel>() != null ->
                ViewModelProvider(activity)[viewModelClass.java]
            activity != null && this is ViewModelStoreOwner ->
                ViewModelProvider(this)[viewModelClass.java]
            else -> viewModelClass.createInstance()
        }
    }

    fun onTwoWayPropChanged(propertyName: String) = Unit

    fun render(
        executeBindingsImmediately: Boolean,
        attachToContainer: Boolean = true
    ): ViewDataBinding? {
        viewModel.componentHasMissedStateChange = false
        return dataBindingManager.updateBinding(
            scheme.render?.invoke(this as C, viewModel),
            executeBindingsImmediately,
            attachToContainer
        )
    }
}