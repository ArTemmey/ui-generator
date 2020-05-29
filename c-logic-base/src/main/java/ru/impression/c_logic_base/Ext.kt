package ru.impression.c_logic_base

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import kotlin.reflect.KClass

val View.activity: AppCompatActivity
    get() {
        var contextWrapper = (context as ContextWrapper)
        while (contextWrapper !is AppCompatActivity) {
            contextWrapper =
                contextWrapper.baseContext as ContextWrapper? ?: throw NoSuchFieldException()
        }
        return contextWrapper
    }

fun KClass<out ViewDataBinding>.inflate(
    context: Context,
    viewModel: ComponentViewModel,
    lifecycleOwner: LifecycleOwner,
    root: ViewGroup?,
    attachToRoot: Boolean
) = (java.getMethod(
    "inflate",
    LayoutInflater::class.java,
    ViewGroup::class.java,
    Boolean::class.javaPrimitiveType
).invoke(
    null,
    LayoutInflater.from(context),
    root,
    attachToRoot
) as ViewDataBinding).apply {
    this.lifecycleOwner = lifecycleOwner
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
