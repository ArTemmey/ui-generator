package ru.impression.c_logic_base

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

sealed class ObservingHelper {
    abstract fun <T> observe(data: LiveData<T>, onChange: (T) -> Unit)
}

class ViewObservingHelper(private val view: View) : ObservingHelper() {

    private val observables = HashMap<LiveData<*>, ArrayList<Observer<*>>>()

    override fun <T> observe(data: LiveData<T>, onChange: (T) -> Unit) {
        data.observe(view.activity!!, Observer(onChange).also { observer ->
            val list = observables[data] ?: ArrayList<Observer<*>>().also { observables[data] = it }
            list.add(observer)
        })
    }

    fun stopAllObservations() {
        observables.forEach { entry ->
            entry.value.forEach { entry.key.removeObserver(it as Observer<in Any>) }
        }
        observables.clear()
    }
}

class FragmentObservingHelper(private val fragment: Fragment) : ObservingHelper() {

    override fun <T> observe(data: LiveData<T>, onChange: (T) -> Unit) {
        data.observe(fragment, Observer(onChange))
    }
}