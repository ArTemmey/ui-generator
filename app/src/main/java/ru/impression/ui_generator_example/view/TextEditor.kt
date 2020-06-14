package ru.impression.ui_generator_example.view

import android.widget.FrameLayout
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.SharedViewModel
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.ComponentViewModel
import ru.impression.ui_generator_example.databinding.TextEditorBinding

@MakeComponent
class TextEditor : ComponentScheme<FrameLayout, TextEditorViewModel>({ TextEditorBinding::class })

@SharedViewModel
class TextEditorViewModel : ComponentViewModel() {

    var editedText by state<String?>(null)

    var customWelcomeText by observable<String?>(null)

    fun updateText() {
        customWelcomeText = editedText
    }
}