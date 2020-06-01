package ru.impression.c_logic_example.view

import android.widget.FrameLayout
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.databinding.MyButtonBinding

@MakeComponent
class MyButton : ComponentScheme<FrameLayout, MyButtonViewModel>({ MyButtonBinding::class })

class MyButtonViewModel : ComponentViewModel() {

    var text by data<String?>(null).mutableBy(MyLayoutViewModel::text)

}