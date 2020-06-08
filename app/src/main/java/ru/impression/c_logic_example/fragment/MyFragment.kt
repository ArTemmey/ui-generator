package ru.impression.c_logic_example.fragment

import androidx.fragment.app.Fragment
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_annotations.Prop
import ru.impression.c_logic_annotations.SharedViewModel
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.databinding.MyFragmentBinding

@MakeComponent
class MyFragment : ComponentScheme<Fragment, MyFragmentViewModel>({ MyFragmentBinding::class })

@SharedViewModel
class MyFragmentViewModel : ComponentViewModel() {

    @Prop
    var text by state<String?>(null)
}