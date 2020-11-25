package ru.impression.ui_generator_base

import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import ru.impression.ui_generator_annotations.Prop
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

val View.activity: AppCompatActivity?
    get() {
        var contextWrapper = (context as? ContextWrapper)
        while (contextWrapper !is AppCompatActivity) {
            contextWrapper =
                contextWrapper?.baseContext as ContextWrapper? ?: return null
        }
        return contextWrapper
    }

fun <T, VM : ComponentViewModel> T.resolveAttrs(attrs: AttributeSet?) where T : Component<*, VM>, T : View {
    with(context.theme.obtainStyledAttributes(attrs, viewModel.attrs ?: return, 0, 0)) {
        try {
            propertyLoop@ for (property in viewModel::class.declaredMemberProperties) {
                val propAnnotation = property.findAnnotation<Prop>() ?: continue
                if (property is KMutableProperty1<*, *> && propAnnotation.attr != -1)
                    (property as KMutableProperty1<Any?, Any?>).set(
                        viewModel,
                        when (property.returnType.classifier) {
                            Boolean::class -> getBoolean(
                                propAnnotation.attr,
                                property.get(viewModel) as Boolean? ?: false
                            )
                            Int::class -> getInt(
                                propAnnotation.attr,
                                property.get(viewModel) as Int? ?: 0
                            )
                            Float::class -> getFloat(
                                propAnnotation.attr,
                                property.get(viewModel) as Float? ?: 0f
                            )
                            String::class -> getString(propAnnotation.attr)
                            Drawable::class -> getDrawable(propAnnotation.attr)
                            else -> continue@propertyLoop
                        }
                    )
            }
        } finally {
            recycle()
        }
    }
}

internal fun KClass<out ViewDataBinding>.inflate(
    component: Component<*, *>,
    attachToRoot: Boolean
) = (component.container as? ViewGroup).let {
    val context =
        (component as? Fragment)?.context ?: (component as? View)?.context ?: return@let null
    try {
        (java.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        ).invoke(null, LayoutInflater.from(context), it, attachToRoot) as ViewDataBinding).apply {
            this.lifecycleOwner = component.boundLifecycleOwner
            setViewModel(component.viewModel)
            safeCallSetter("setComponent", component)
            safeCallSetter("setContext", context)
        }
    } catch (e: NoSuchMethodException) {
        null
    }
}

internal fun ViewDataBinding.setViewModel(viewModel: ComponentViewModel) {
    try {
        this::class.java.getDeclaredMethod("setViewModel", viewModel::class.java)
            .invoke(this, viewModel)
    } catch (e: NoSuchMethodException) {
    }
}

internal fun ViewDataBinding.safeCallSetter(setterName: String, data: Any) {
    this::class.java.declaredMethods.firstOrNull {
        val parameterTypes = it.parameterTypes
        it.name == setterName
                && parameterTypes.size == 1
                && parameterTypes[0].isAssignableFrom(data::class.java)
    }?.invoke(this, data)
}

fun <T> KMutableProperty0<T>.safeSetProp(value: T?) {
    if (!this.returnType.isMarkedNullable && value == null) return
    set(value as T, true)
}

fun <T> KMutableProperty0<T>.set(value: T, renderImmediately: Boolean = false) {
    getDelegateFromSum<StateDelegate<StateParent, T>>()
        ?.setValue(this, value, renderImmediately)
}

val KMutableProperty0<*>.isLoading: Boolean
    get() = getDelegateFromSum<StateDelegate<*, *>>()?.isLoading == true

fun KMutableProperty0<*>.reload() {
    getDelegateFromSum<StateDelegate<*, *>>()?.load(true)
}