package ru.impression.ui_generator_example

import android.util.Log
import android.widget.FrameLayout
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel
import ru.impression.ui_generator_base.onInit
import ru.impression.ui_generator_base.withLifecycle
import kotlin.reflect.KClass

@MakeComponent
class Counter : ComponentScheme<FrameLayout, CounterViewModel>({
    onInit {
        Log.v(Counter::class.simpleName, "onInit")
    }
    withLifecycle {
        onCreate {
            Log.v(Counter::class.simpleName, "onCreate")
        }
        onDestroy {
            Log.v(Counter::class.simpleName, "onDestroy")
        }
    }
    R.layout.counter
})

class CounterViewModel : ComponentViewModel() {

    @Prop(twoWay = true)
    var count by state(0)

    @Prop
    var clazz by state<KClass<*>?>(null)

    fun increment() {
        count++
    }

    fun decrement() {
        count--
    }
}