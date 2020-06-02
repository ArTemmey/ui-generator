package ru.impression.c_logic_example.view

import android.widget.FrameLayout
import androidx.lifecycle.MutableLiveData
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

    var image by state<String?>(null).mutableBy(MyButtonViewModel::text)


    init {
        image = "123"
    }
}
