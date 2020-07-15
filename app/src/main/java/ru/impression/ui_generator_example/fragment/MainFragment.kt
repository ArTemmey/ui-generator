package ru.impression.ui_generator_example.fragment

import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.CoroutineViewModel
import ru.impression.ui_generator_base.isLoading
import ru.impression.ui_generator_base.reload
import ru.impression.ui_generator_example.databinding.MainFragmentBinding
import ru.impression.ui_generator_example.view.AnimatedText
import ru.impression.ui_generator_example.view.TextEditorViewModel
import kotlin.random.Random
import kotlin.random.nextInt

@MakeComponent
class MainFragment :
    ComponentScheme<Fragment, MainFragmentViewModel>({ viewModel ->
        viewModel.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.toastMessage = null
        }
        MainFragmentBinding::class
    })

class MainFragmentViewModel : CoroutineViewModel() {

    @Prop
    var welcomeText by state<String?>(null)

    init {
        ::welcomeText.isMutableBy(TextEditorViewModel::customWelcomeText)
    }

    var welcomeTextIsVisible by state(true)

    fun toggleVisibility() {
        welcomeTextIsVisible = !welcomeTextIsVisible
    }


    var textAnimation by state<AnimatedText.Animation?>(null) {
        toastMessage = "Current animation in ${it?.name}"
    }

    fun animate() {
        textAnimation =
            AnimatedText.Animation.values()[Random.nextInt(AnimatedText.Animation.values().indices)]
    }


    var currentTime by state({
        delay(3000)
        System.currentTimeMillis().toString()
    })

    val currentTimeIsLoading get() = ::currentTime.isLoading

    fun reloadCurrentTime() {
        ::currentTime.reload()
    }


    var toastMessage by state<String?>(null)
}