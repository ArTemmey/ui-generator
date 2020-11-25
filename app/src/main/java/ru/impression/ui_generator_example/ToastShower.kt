package ru.impression.ui_generator_example

import android.view.View
import android.widget.Toast
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel

@MakeComponent
class ToastShower : ComponentScheme<View, ToastShowerViewModel>({ viewModel ->
    viewModel.message?.let {
        viewModel.message = null
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    }
    null
})

class ToastShowerViewModel : ComponentViewModel() {

    @Prop(twoWay = true)
    var message by state<String?>(null)
}