package ru.impression.ui_generator_example

import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel
import ru.impression.ui_generator_example.databinding.TitledPictureBinding

@MakeComponent
class TitledPicture :
    ComponentScheme<FrameLayout, TitledPictureViewModel>({ TitledPictureBinding::class })

class TitledPictureViewModel : ComponentViewModel(R.styleable.TextAndImageBlockComponent) {

    @Prop(attr = R.styleable.TextAndImageBlockComponent_title)
    var title by state<String?>(null)

    @Prop(attr = R.styleable.TextAndImageBlockComponent_picture)
    var picture by state<Drawable?>(null)

    var toastMessage by state<String?>(null)

    fun showToast() {
        toastMessage = "Clicked on $title!"
    }
}