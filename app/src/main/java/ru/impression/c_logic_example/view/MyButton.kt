package ru.impression.c_logic_example.view

import android.widget.FrameLayout
import ru.impression.c_logic_annotations.Bindable
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.databinding.MyButtonBinding

@MakeComponent
class MyButton : ComponentScheme<FrameLayout, MyButtonBinding, MyButtonViewModel>()

class MyButtonViewModel : ComponentViewModel() {

    @Bindable(true)
    val text = Data<String>()
        .mutableBy(MyLayoutViewModel::text)
        .changesAffect(MyLayoutViewModel::text)

    fun onMyButtonClick() {
        text.set("HELLO WORLD!")
    }
}