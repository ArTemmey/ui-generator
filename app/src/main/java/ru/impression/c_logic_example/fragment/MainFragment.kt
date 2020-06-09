package ru.impression.c_logic_example.fragment

import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_annotations.Prop
import ru.impression.c_logic_base.ComponentScheme
import ru.impression.c_logic_base.ComponentViewModel
import ru.impression.c_logic_example.context
import ru.impression.c_logic_example.databinding.MainFragmentBinding
import ru.impression.c_logic_example.view.WelcomeText
import kotlin.random.Random
import kotlin.random.nextInt

@MakeComponent
class MainFragment :
    ComponentScheme<Fragment, MainFragmentViewModel>({ MainFragmentBinding::class })

class MainFragmentViewModel : ComponentViewModel() {

    @Prop
    var welcomeText by state<String?>(null)

    var welcomeTextVisibility by state(VISIBLE)

    var welcomeTextAnimation by state<WelcomeText.Animation?>(null) {
        Toast.makeText(context, "Current animation in ${it?.name}", Toast.LENGTH_SHORT).show()
    }

    fun toggleWelcomeTextVisibility() {
        welcomeTextVisibility = if (welcomeTextVisibility == VISIBLE) INVISIBLE else VISIBLE
    }

    fun animateWelcomeText() {
        welcomeTextAnimation =
            WelcomeText.Animation.values()[Random.nextInt(WelcomeText.Animation.values().indices)]
    }
}