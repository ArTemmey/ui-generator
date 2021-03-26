package ru.impression.ui_generator_example

import android.widget.FrameLayout
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel

@MakeComponent
class Counter : ComponentScheme<FrameLayout, CounterViewModel>({ R.layout.counter })

class CounterViewModel : ComponentViewModel() {

    @Prop(twoWay = true)
    var count by state(0)

    fun increment() {
        count++
    }

    fun decrement() {
        count--
    }
}