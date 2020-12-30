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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Job
import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

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
            for (delegateToAttr in viewModel.delegateToAttrs) {
                val property = delegateToAttr.key.getProperty()
                (property as KMutableProperty1<Any?, Any?>).set(
                    viewModel,
                    when (property.returnType.classifier) {
                        Boolean::class -> getBoolean(
                            delegateToAttr.value,
                            property.get(viewModel) as Boolean? ?: false
                        )
                        Int::class -> getInt(
                            delegateToAttr.value,
                            property.get(viewModel) as Int? ?: 0
                        )
                        Float::class -> getFloat(
                            delegateToAttr.value,
                            property.get(viewModel) as Float? ?: 0f
                        )
                        String::class -> getString(delegateToAttr.value)
                        Drawable::class -> getDrawable(delegateToAttr.value)
                        else -> continue
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

fun <R : StateOwner, T> StateDelegate<R, T>.getProperty() =
    (parent::class.declaredMemberProperties.firstOrNull {
        (it as? KProperty1<R, *>)
            ?.getDelegateFromSum<R, StateDelegate<*, *>>(parent) == this
    } as KMutableProperty1<R, T>?)

fun <T> KMutableProperty0<T>.nullSafetySet(value: T?) {
    if (!this.returnType.isMarkedNullable && value == null) return
    set(value as T)
}

val KMutableProperty0<*>.isLoading: Boolean
    get() = getDelegateFromSum<StateDelegate<*, *>>()?.isLoading == true

fun KMutableProperty0<*>.reload(): Job =
    getDelegateFromSum<StateDelegate<*, *>>()!!.load(true)

fun ViewDataBinding.bindViewModel(viewModel: ComponentViewModel) {
    setViewModel(viewModel)
    viewModel.addStateObserver(lifecycleOwner ?: return) {
        setViewModel(viewModel)
        executePendingBindings()
    }
}

fun View.toLifecycleOwner() = object : LifecycleOwner {

    private val lifecycle = SimpleLifecycle(this)

    init {
        addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {
                    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                }

                override fun onViewDetachedFromWindow(v: View?) {
                    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

                }
            }
        )
    }

    override fun getLifecycle() = lifecycle
}