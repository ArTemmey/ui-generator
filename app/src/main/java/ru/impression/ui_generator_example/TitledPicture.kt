package ru.impression.ui_generator_example

import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel

@MakeComponent
class TitledPicture : ComponentScheme<FrameLayout, TitledPictureViewModel>({ viewModel ->
    when (viewModel.titlePosition) {
        TitlePosition.BELOW_PICTURE -> R.layout.titled_picture_title_below
        TitlePosition.ABOVE_PICTURE -> R.layout.titled_picture_title_above
    }
}) {

    enum class TitlePosition {
        BELOW_PICTURE,
        ABOVE_PICTURE,
    }
}

class TitledPictureViewModel : ComponentViewModel(attrs = R.styleable.TextAndImageBlockComponent) {

    var title by state<String?>(null, attr = R.styleable.TextAndImageBlockComponent_title)

    var titlePosition by state(
        TitledPicture.TitlePosition.BELOW_PICTURE,
        attr = R.styleable.TextAndImageBlockComponent_titlePosition
    )

    var picture by state<Drawable?>(null, attr = R.styleable.TextAndImageBlockComponent_picture)

    fun showInfoToast() {
        showToast("Clicked on $title!")
    }
}