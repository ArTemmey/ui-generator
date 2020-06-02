package ru.impression.c_logic_example.view

import android.widget.FrameLayout
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.databinding.MyButtonBinding

@MakeComponent
class MyButton : ComponentScheme<FrameLayout, MyButtonViewModel>({ viewModel ->
    if (viewModel.isVisible)
        MyButtonBinding::class
    else
        null
})

class MyButtonViewModel : ComponentViewModel() {

    var isVisible by state(false)

    var text by state<String?>(null){

    }.mutableBy(MyLayoutViewModel::image)
}