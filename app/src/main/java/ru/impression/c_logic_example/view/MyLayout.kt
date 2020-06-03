package ru.impression.c_logic_example.view

import android.widget.FrameLayout
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_annotations.SharedViewModel
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.databinding.MyLayoutBinding

@MakeComponent
class MyLayout : ComponentScheme<FrameLayout, MyLayoutViewModel>({
    if (it.isVisible)
        MyLayoutBinding::class
    else
        null
})

@SharedViewModel
class MyLayoutViewModel : ComponentViewModel() {

    var isVisible by state(true)

    var text by state<String?>(null)


    init {
        text = "123"
    }
}
