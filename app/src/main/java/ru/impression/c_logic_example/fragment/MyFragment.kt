package ru.impression.c_logic_example.fragment

import androidx.fragment.app.Fragment
import ru.impression.c_logic_annotations.Bindable
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.databinding.MyFragmentBinding

@MakeComponent
class MyFragment : ComponentScheme<Fragment, MyFragmentViewModel>({ MyFragmentBinding::class })

class MyFragmentViewModel : ComponentViewModel() {

    @Bindable
    var text by state<String?>(null)
}