package ru.impression.ui_generator_example

import androidx.fragment.app.Fragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.CoroutineViewModel
import ru.impression.ui_generator_base.isLoading
import ru.impression.ui_generator_base.reload

@MakeComponent
class MainFragment :
    ComponentScheme<Fragment, MainFragmentViewModel>({ R.layout.main_fragment })

class MainFragmentViewModel : CoroutineViewModel() {

    var countDown by state(flow {
        delay(1000)
        emit(3)
        delay(1000)
        emit(2)
        delay(1000)
        emit(1)
        delay(1000)
        emit(0)
    })

    val countDownIsLoading get() = ::countDown.isLoading


    @Prop
    var welcomeText by state<String?>(null)


    var count by state(0) { showToast("Count is $it!") }


    var currentTime by state({
        delay(2000)
        System.currentTimeMillis().toString()
    })

    val currentTimeIsLoading get() = ::currentTime.isLoading

    fun reloadCurrentTime() {
        ::currentTime.reload()
    }
}