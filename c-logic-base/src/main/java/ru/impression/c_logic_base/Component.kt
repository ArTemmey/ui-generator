package ru.impression.c_logic_base

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
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
        viewModel.propertyChange.observe(lifecycleOwner, Observer { render() })
        val viewPropertyShares =
            HashMap<KClass<out ComponentViewModel>, List<PropertyShare>>()
        viewModel.propertyShares.forEach {
            if (it.key.findAnnotation<SharedViewModel>() != null)
                performPropertyShares(createViewModel(it.key), it.value)
            else
                viewPropertyShares[it.key] = it.value

        }
        container.bindSharedProperties(viewPropertyShares)
    }

    private fun View.bindSharedProperties(
        propertyShares: MutableMap<KClass<out ComponentViewModel>, List<PropertyShare>>
    ) {
        if (this is Component<*, *>) {
            propertyShares.remove(viewModel::class)?.let { performPropertyShares(viewModel, it) }
            if (propertyShares.isEmpty()) return
        }
        if (this is ViewGroup) children.forEach { it.bindSharedProperties(propertyShares) }
    }

    private fun performPropertyShares(
        sourceViewModel: ComponentViewModel,
        propertyShares: List<PropertyShare>
    ) {
        sourceViewModel.propertyChange.observe(
            this@Component.lifecycleOwner,
            Observer {
                propertyShares.forEach { propertyShare ->
                    val value = (sourceViewModel::class.declaredMemberProperties
                        .find { it == propertyShare.sourceProperty }
                            as? KProperty1<ComponentViewModel, *>)
                        ?.get(sourceViewModel)
                    (propertyShare.targetProperty as? ComponentViewModel.DataImpl<Any?>)
                        ?.setValue(value)
                }
            }
        )
    }
}