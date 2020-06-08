package ru.impression.c_logic_example.view

import android.widget.Button
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_annotations.Prop
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel

@MakeComponent
class MyButton : ComponentScheme<Button, MyButtonViewModel>({ viewModel ->
    if (text != viewModel.twoWayText) text = viewModel.twoWayText
    if (!hasOnClickListeners()) setOnClickListener { viewModel.onClick() }
    null
})

class MyButtonViewModel : ComponentViewModel() {

    @Prop(true)
    var twoWayText by state("default text")

    fun onClick() {
        twoWayText = "default text"
    }

    init {
        ::twoWayText.isMutableBy(MyLayoutViewModel::text)
    }
}