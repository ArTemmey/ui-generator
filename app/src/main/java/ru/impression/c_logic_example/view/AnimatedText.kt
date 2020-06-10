@file:SuppressLint("RestrictedApi")

package ru.impression.c_logic_example.view

import android.annotation.SuppressLint
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.adapters.TextViewBindingAdapter
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_annotations.Prop
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.fadeIn
import ru.impression.c_logic_example.fadeOut
import ru.impression.c_logic_example.translateLeft
import ru.impression.c_logic_example.translateRight

@MakeComponent
class AnimatedText : ComponentScheme<AppCompatTextView, AnimatedTextViewModel>({ viewModel ->
    // Use binding adapters as they are safe if you re-set the same value
    TextViewBindingAdapter.setText(this, viewModel.text)
    clearAnimation()
    when (viewModel.animation) {
        Animation.FADE_IN -> fadeIn(1000) { viewModel.onAnimationCompleted() }
        Animation.FADE_OUT -> fadeOut(1000) { viewModel.onAnimationCompleted() }
        Animation.TRANSLATE_LEFT -> translateLeft(1000) { viewModel.onAnimationCompleted() }
        Animation.TRANSLATE_RIGHT -> translateRight(1000) { viewModel.onAnimationCompleted() }
    }
    null
}) {
    enum class Animation { FADE_IN, FADE_OUT, TRANSLATE_LEFT, TRANSLATE_RIGHT }
}

class AnimatedTextViewModel : ComponentViewModel() {

    val text = "Animated text"

    @Prop(twoWay = true)
    var animation by state<AnimatedText.Animation?>(null)

    fun onAnimationCompleted() {
        animation = null
    }
}