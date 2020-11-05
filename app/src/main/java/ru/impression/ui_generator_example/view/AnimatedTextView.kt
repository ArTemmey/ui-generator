@file:SuppressLint("RestrictedApi")

package ru.impression.ui_generator_example.view

import android.annotation.SuppressLint
import androidx.appcompat.widget.AppCompatTextView
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel
import ru.impression.ui_generator_base.bindText
import ru.impression.ui_generator_example.*

@MakeComponent
class AnimatedTextView : ComponentScheme<AppCompatTextView, AnimatedTextViewModel>({ viewModel ->
    viewModel.animation?.let {
        clearAnimation()
        when (it) {
            Animation.FADE_IN -> fadeIn(1000) { viewModel.onAnimationCompleted() }
            Animation.FADE_OUT -> fadeOut(1000) { viewModel.onAnimationCompleted() }
            Animation.TRANSLATE_LEFT -> translateLeft(1000) { viewModel.onAnimationCompleted() }
            Animation.TRANSLATE_RIGHT -> translateRight(1000) { viewModel.onAnimationCompleted() }
        }
    }
    bindText(viewModel.animatedText)
    null
}) {
    enum class Animation { FADE_IN, FADE_OUT, TRANSLATE_LEFT, TRANSLATE_RIGHT }
}

class AnimatedTextViewModel : ComponentViewModel(attrs = R.styleable.AnimatedTextViewComponent) {

    @Prop(twoWay = true)
    var animation by state<AnimatedTextView.Animation?>(null)

    @Prop(attr = R.styleable.AnimatedTextViewComponent_animatedText)
    var animatedText by state<String?>(null)

    fun onAnimationCompleted() {
        animation = null
    }
}