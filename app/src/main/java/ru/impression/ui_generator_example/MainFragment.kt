package ru.impression.ui_generator_example

import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.*
import kotlin.random.Random
import kotlin.random.nextInt

@MakeComponent
class MainFragment :
    ComponentScheme<Fragment, MainFragmentViewModel>({
        onInit {
            Log.v(MainFragment::class.simpleName, "onInit")
        }
        withLifecycle {
            onCreate {
                Log.v(MainFragment::class.simpleName, "onCreate")
            }
            onStart {
                Log.v(MainFragment::class.simpleName, "onStart")
            }
            onResume {
                Log.v(MainFragment::class.simpleName, "onResume")
            }
            onDestroy {
                Log.v(MainFragment::class.simpleName, "onDestroy")
            }
        }
        R.layout.main_fragment
    })

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

    @Prop
    var greetingStructure by state<GreetingStructure?>(null)

    fun changeGreetingAddressee() {
        greetingStructure?.greetingAddresseeState =
            arrayOf("world", "my friend", "universe").random()
    }


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