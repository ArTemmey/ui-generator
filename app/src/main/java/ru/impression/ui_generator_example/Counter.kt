package ru.impression.ui_generator_example

import android.widget.FrameLayout
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel
import ru.impression.ui_generator_example.databinding.CounterBinding

@MakeComponent
class Counter : ComponentScheme<FrameLayout, CounterViewModel>({ CounterBinding::class })

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