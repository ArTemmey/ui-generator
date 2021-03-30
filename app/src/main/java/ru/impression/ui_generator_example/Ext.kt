package ru.impression.ui_generator_example

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import ru.impression.ui_generator_base.ComponentViewModel

fun ComponentViewModel.showToast(toastMessage: String) {
    getSharedViewModel<ToastShowerViewModel>().toastMessage = toastMessage
}

object DataBindingExt {

    @JvmStatic
    @BindingAdapter("isInvisible")
    fun setIsInvisible(view: View, value: Boolean) {
        view.isInvisible = value
    }

    @JvmStatic
    @BindingAdapter("isVisible")
    fun setIsVisible(view: View, value: Boolean) {
        view.isVisible = value
    }

    @JvmStatic
    @BindingAdapter("isGone")
    fun setIsGone(view: View, value: Boolean) {
        view.isGone = value
    }
}