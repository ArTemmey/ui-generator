package ru.impression.c_logic_base

import android.view.View
import androidx.lifecycle.LifecycleOwner

interface Component {

    val container: View

    val viewModel: ComponentViewModel

    val lifecycleOwner: LifecycleOwner
}