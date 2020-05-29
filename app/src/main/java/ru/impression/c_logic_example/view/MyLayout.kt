package ru.impression.c_logic_example.view

import android.widget.FrameLayout
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel

class MyLayout : ComponentScheme<FrameLayout, MyLayoutBinding>

class MyLayoutViewModel : ComponentViewModel() {

    val text = Observable<String>()
}