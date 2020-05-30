package ru.impression.c_logic_example.view

import android.widget.FrameLayout
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.databinding.MyTextBinding

@MakeComponent
class MyText : ComponentScheme<FrameLayout, MyTextBinding, MyTextViewModel>()

class MyTextViewModel : ComponentViewModel() {


}