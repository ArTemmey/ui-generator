package ru.impression.c_logic_base

import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding

val View.activity: AppCompatActivity?
    get() {
        var contextWrapper = (context as? ContextWrapper)
        while (contextWrapper !is AppCompatActivity) {
            contextWrapper =
                contextWrapper?.baseContext as ContextWrapper? ?: return null
        }
        return contextWrapper
    }

fun ViewDataBinding.setViewModel(viewModel: ComponentViewModel) {
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
}
