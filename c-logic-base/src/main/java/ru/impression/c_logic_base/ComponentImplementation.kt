package ru.impression.c_logic_base

import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.impression.c_logic_annotations.Bindable
import ru.impression.c_logic_annotations.SharedViewModel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

class BindingManager(
    private val owner: View,
    binding: ViewDataBinding,
    private val viewModel: ComponentViewModel
) {

    private val twoWayBindingObservables =
        HashMap<String, ComponentViewModel.Observable<out Any?>>()

    private val duplicatedObservables = HashMap<LiveData<out Any?>, ArrayList<Observer<out Any?>>>()

    init {
        owner.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                duplicatedObservables.forEach { entry ->
                    entry.value.forEach { entry.key.removeObserver(it as Observer<Any?>) }
                }
                duplicatedObservables.clear()
            }

            override fun onViewAttachedToWindow(v: View?) {
                viewModel.propertyDuplicates.forEach { propertyDuplicate ->
                    propertyDuplicate.property.get(ViewModelProvider(owner.activity!!)[propertyDuplicate.viewModelClass.java])
                        .observe { propertyDuplicate.target.value = it }
                }
            }

        })
        viewModel.apply {
            viewModel::class.members.forEach { member ->
                if (member.findAnnotation<Bindable>()?.twoWay == true) {
                    (member as KProperty1<Any?, ComponentViewModel.Observable<Any?>>).get(this)
                        .observeForever { twoWayBindingObservables[member.name]?.value = it }
                }
            }
            propertyDuplicates.forEach { propertyDuplicate ->
                if (propertyDuplicate.isMutable) {
                    propertyDuplicate.target.observeForever {
                        (propertyDuplicate.property.get(
                            ViewModelProvider(owner.activity!!)[propertyDuplicate.viewModelClass.java]
                        ) as MutableLiveData<out Any?>).value = it
                    }
                }
            }
        }
        binding.setViewModel(viewModel)
        binding.lifecycleOwner = owner.activity
    }

    fun onValueBound(name: String, value: ComponentViewModel.Observable<out Any>) {
        twoWayBindingObservables.remove(name)
        notifyBindableObservable(name, value.value)
        twoWayBindingObservables[name] = value
    }

    fun onValueBound(name: String, value: Any) {
        notifyBindableObservable(name, value)
    }

    private fun notifyBindableObservable(name: String, value: Any?) {
        (viewModel!!::class.members.first { it.name == name }
                as KProperty1<Any?, ComponentViewModel.Observable<Any?>>).get(viewModel).value =
            value
    }

    fun <T> LiveData<T>.observe(onChanged: (T) -> Unit) {
        observe(owner.activity!!, Observer(onChanged).also {
            val list = duplicatedObservables[this]
                ?: ArrayList<Observer<out Any?>>().also { duplicatedObservables[this] = it }
            list.add(it)
        })
    }
}

class FragmentComponentImplementation(
    private val fragment: Fragment,
    private val binding: ViewDataBinding,
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

    fun onCreateView(container: ViewGroup?) = binding.apply {
        lifecycleOwner = fragment
        setViewModel(viewModel)
    }.root
}