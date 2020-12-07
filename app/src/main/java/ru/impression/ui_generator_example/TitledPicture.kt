package ru.impression.ui_generator_example

import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel
import ru.impression.ui_generator_example.databinding.TitledPictureBinding

@MakeComponent
class TitledPicture :
    ComponentScheme<FrameLayout, TitledPictureViewModel>({ TitledPictureBinding::class })

class TitledPictureViewModel : ComponentViewModel(attrs = R.styleable.TextAndImageBlockComponent) {

    var title by state<String?>(null, attr = R.styleable.TextAndImageBlockComponent_title)

    var picture by state<Drawable?>(null, attr = R.styleable.TextAndImageBlockComponent_picture)

    var toastMessage by state<String?>(null)

    fun showToast() {
        toastMessage = "Clicked on $title!"
    }
}