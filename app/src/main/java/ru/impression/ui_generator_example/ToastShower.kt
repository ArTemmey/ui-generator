package ru.impression.ui_generator_example

import android.view.View
import android.widget.Toast
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_annotations.SharedViewModel
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel

@MakeComponent
class ToastShower : ComponentScheme<View, ToastShowerViewModel>({ viewModel ->
    viewModel.toastMessage?.let {
        viewModel.toastMessage = null
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    }
    null
})

@SharedViewModel
class ToastShowerViewModel : ComponentViewModel() {

    var toastMessage by state<String?>(null)
}