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

    val viewModelClass: KClass<VM>

    val viewModel: VM

    val container: View

    val lifecycleOwner: LifecycleOwner

    val renderer: Renderer

    fun render() {
        renderer.render(scheme.getBindingClass?.invoke(this as C, viewModel))
    }

    fun startObservations() {
        viewModel.stateChange.observe(lifecycleOwner, Observer { render() })
        bindSharedProperties()
    }

    private fun bindSharedProperties() {
        val sharedPropertiesOfChildViews =
            viewModel.sharedProperties.filterTo(HashMap()) { sharedPropertiesOfCertainViewModel ->
                if (sharedPropertiesOfCertainViewModel.key.findAnnotation<SharedViewModel>() == null)
                    return@filterTo true
                val sourceViewModel = createViewModel(sharedPropertiesOfCertainViewModel.key)
                sourceViewModel.stateChange.observe(
                    lifecycleOwner,
                    Observer {
                        sharedPropertiesOfCertainViewModel.value.forEach { sharedProperty ->
                            (sharedProperty.value as ComponentViewModel.StateImpl<Any?>)
                                .setValue(null, sharedProperty.key.get(sourceViewModel))
                        }
                    }
                )
                false
            }
        if (sharedPropertiesOfChildViews.isNotEmpty())
            container.bindSharedProperties(sharedPropertiesOfChildViews)
    }

    private fun View.bindSharedProperties(
        sharedProperties: HashMap<KClass<out ComponentViewModel>, HashMap<KProperty1<ComponentViewModel, *>, ComponentViewModel.StateImpl<*>>>
    ) {
        if (this is Component<*, *>) {
            sharedProperties.remove(viewModel::class)?.let { sharedPropertiesOfCertainViewModel ->
                viewModel.addOnPropertyChangeListener(this@Component) { property, value ->
                    (sharedPropertiesOfCertainViewModel[property]
                            as ComponentViewModel.StateImpl<Any?>?)?.setValue(null, value)
                }
            }
            if (sharedProperties.isEmpty()) return
        }
        if (this is ViewGroup) children.forEach { it.bindSharedProperties(sharedProperties) }
    }
}