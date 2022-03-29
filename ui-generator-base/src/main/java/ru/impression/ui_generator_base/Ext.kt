@file:Suppress("NonExhaustiveWhenStatementMigration")

package ru.impression.ui_generator_base

import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Job
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.isSubclassOf

val View.activity: AppCompatActivity?
    get() {
        var contextWrapper = (context as? ContextWrapper)
        while (contextWrapper !is AppCompatActivity) {
            contextWrapper =
                contextWrapper?.baseContext as ContextWrapper? ?: return null
        }
        return contextWrapper
    }

@PublishedApi
internal const val KEY_JSON_ARGS = "JSON_ARGS"

inline fun <reified T> Fragment.putArgument(key: String, value: T) {
    val arguments = arguments ?: Bundle().also { arguments = it }
    try {
        arguments.putAll(bundleOf(key to value))
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        try {
            arguments.putString(key, Json.encodeToString(value))
            arguments.putStringArray(
                KEY_JSON_ARGS,
                (arguments.getStringArray(KEY_JSON_ARGS) ?: emptyArray<String>()) + arrayOf(key)
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
        }
    }
}


inline fun <reified T> Fragment.getArgument(key: String): T? = arguments?.get(key)?.let {
    when {
        arguments?.getStringArray(KEY_JSON_ARGS)?.contains(key) == true -> try {
            Json.decodeFromString<T>(it as? String ?: return@let null)
        } catch (e: SerializationException) {
            null
        }
        it is T -> it
        else -> null
    }
}


fun <T, VM : ComponentViewModel> T.resolveAttrs(attrs: AttributeSet?) where T : Component<*, VM>, T : View {
    with(context.theme.obtainStyledAttributes(attrs, viewModel.attrs ?: return, 0, 0)) {
        try {
            for (delegateToAttr in viewModel.delegateToAttrs) {
                val property = delegateToAttr.key.property
                val classifier = property.returnType.classifier as? KClass<*> ?: continue
                property as KMutableProperty1<Any?, Any?>
                val value = when (classifier) {
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

                    else -> when {
                        classifier.isSubclassOf(Enum::class) ->
                            getInt(delegateToAttr.value, -1)
                                .takeIf { it != -1 }
                                ?.let { classifier.java.enumConstants?.get(it) }
                                ?: continue

                        else -> continue
                    }
                }
                property.set(viewModel, value)
            }
        } finally {
            recycle()
        }
    }
}

val KMutableProperty0<*>.isLoading: Boolean
    get() = getDelegateFromSum<StateDelegate<*, *>>()?.isLoading == true


fun KMutableProperty0<*>.reload(): Job =
    getDelegateFromSum<StateDelegate<*, *>>()!!.load(true)

fun View.asLifecycleOwner() = ViewLifecycleOwner(this)


fun View.onInit(block: () -> Unit) {
    (this as? Component<*, *>)?.onInit(block)
}

fun Fragment.onInit(block: () -> Unit) {
    (this as? Component<*, *>)?.onInit(block)
}

private fun Component<*, *>.onInit(block: () -> Unit) {
    hooks.addInitBlock(block)
}

fun View.withLifecycle(block: LifecycleScope.() -> Unit) {
    onInit {
        (this as? Component<*, *>)?.withLifecycle(block)
    }
}

fun Fragment.withLifecycle(block: LifecycleScope.() -> Unit) {
    onInit {
        Handler(Looper.getMainLooper()).post {
            (this as? Component<*, *>)?.withLifecycle(block)
        }
    }
}

private fun Component<*, *>.withLifecycle(block: LifecycleScope.() -> Unit) {
    val scope = LifecycleScope()
    block(scope)
    boundLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_CREATE -> scope.callOnCreateBlocks()
                Lifecycle.Event.ON_START -> scope.callOnStartBlocks()
                Lifecycle.Event.ON_RESUME -> scope.callOnResumeBlocks()
                Lifecycle.Event.ON_PAUSE -> scope.callOnPauseBlocks()
                Lifecycle.Event.ON_STOP -> scope.callOnStopBlocks()
                Lifecycle.Event.ON_DESTROY -> scope.callOnDestroyBlocks()
            }
        }

    })
}