package ru.impression.c_logic_base

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.impression.c_logic_annotations.Bindable
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation


class ViewComponentImplementation(
    view: ViewGroup,
    bindingClass: KClass<out ViewDataBinding>,
    viewModelClass: KClass<out ComponentViewModel>
) {

    private val outerObservables = HashMap<String, ComponentViewModel.Observable<out Any>?>()

    private val viewModel = if (viewModelClass.findAnnotation<SharedViewModel>() != null)
        ViewModelProvider(view.activity)[viewModelClass.java]
    else
        viewModelClass.createInstance()
            .apply {
                viewModelClass.members.forEach { member ->
                    if (member.findAnnotation<Bindable>()?.inverse == true) {
                        (member as KProperty1<Any, ComponentViewModel.Observable<Any>>).get(this)
                            .observeForever { outerObservables[member.name]?.value = it }
                    }
                }
            }

    init {
        bindingClass.inflate(view.context, viewModel, view.activity, view, true)
    }

    fun onValueBound(name: String, value: ComponentViewModel.Observable<out Any>) {
        outerObservables[name] = null
        notifyBindableObservable(name, value.value)
        outerObservables[name] = value
    }

    fun onValueBound(name: String, value: Any) {
        notifyBindableObservable(name, value)
    }

    private fun notifyBindableObservable(name: String, value: Any?) {
        (viewModel::class.members.first { it.name == name }
                as KProperty1<Any, ComponentViewModel.Observable<Any>>).get(viewModel).value =
            value
    }
}

class FragmentComponentImplementation(
    private val fragment: Fragment,
    private val bindingClass: KClass<out ViewDataBinding>,
    private val viewModelClass: KClass<out ComponentViewModel>
) {

    private val viewModel by lazy {
        ViewModelProvider(
            if (viewModelClass.annotations.firstOrNull { it is SharedViewModel } != null)
                fragment.activity!!
            else
                fragment
        )[viewModelClass.java]
    }

    fun createView(container: ViewGroup?) =
        bindingClass.inflate(fragment.activity!!, viewModel, fragment, container, false).root
}