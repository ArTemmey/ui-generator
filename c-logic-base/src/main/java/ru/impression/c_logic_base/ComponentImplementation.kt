package ru.impression.c_logic_base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.impression.c_logic_annotations.Bindable
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

class ComponentImplementation private constructor(
    private val fragment: Fragment?,
    private val view: ViewGroup?,
    private val bindingClass: Class<out ViewDataBinding>,
    private val viewModelClass: Class<out ComponentViewModel>
) {

    lateinit var viewModel: ComponentViewModel

    constructor(
        view: ViewGroup,
        bindingClass: Class<out ViewDataBinding>,
        viewModelClass: Class<out ComponentViewModel>
    ) : this(null, view, bindingClass, viewModelClass)

    constructor(
        fragment: Fragment,
        bindingClass: Class<out ViewDataBinding>,
        viewModelClass: Class<out ComponentViewModel>
    ) : this(fragment, null, bindingClass, viewModelClass)

    fun getRootView(container: ViewGroup?) =
        (bindingClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        ).invoke(
            null,
            LayoutInflater.from(fragment?.context ?: view!!.context),
            container,
            fragment?.let { false } ?: true
        ) as ViewDataBinding).apply {
            lifecycleOwner = fragment?.activity ?: view!!.activity
            val viewModel = createViewModel()
            var packageName = javaClass.`package`!!.name
            var br: Class<*>? = null
            while (true) {
                try {
                    br = Class.forName("$packageName.BR")
                    break
                } catch (e: ClassNotFoundException) {
                    val nextPackageName = packageName.substringBeforeLast('.')
                    if (nextPackageName == packageName) break
                    packageName = nextPackageName
                }
            }
            setVariable(br!!.getField("viewModel").getInt(null), viewModel)
        }.root

    private fun createViewModel(): ComponentViewModel {
        viewModel =
            fragment?.let {
                ViewModelProvider(
                    if (viewModelClass.annotations.firstOrNull { it is SharedViewModel } != null)
                        it.activity!!
                    else
                        it
                )[viewModelClass]
            } ?: viewModelClass.newInstance()
        viewModel::class.members.forEach { member ->
            if (member.findAnnotation<Bindable>()?.inverse == true) {
                outerObservables[member.name] = null
                (member as KProperty1<Any, ComponentViewModel.Observable<Any>>).get(viewModel)
                    .observeForever { outerObservables[member.name]?.value = it }
            }
        }
        return viewModel
    }

    private val outerObservables = HashMap<String, ComponentViewModel.Observable<Any>?>()

    fun onValueBinded(name: String, value: ComponentViewModel.Observable<Any>) {
        if (outerObservables.containsKey(name))
            outerObservables[name] = null
        (viewModel::class.members.first { it.name == name }
                as KProperty1<Any, ComponentViewModel.Observable<Any>>).get(viewModel).value =
            value.value
        if (outerObservables.containsKey(name))
            outerObservables[name] = value
    }
}