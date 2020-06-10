package ru.impression.c_logic_example.view

import android.widget.FrameLayout
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_annotations.SharedViewModel
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.databinding.TextEditorBinding

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