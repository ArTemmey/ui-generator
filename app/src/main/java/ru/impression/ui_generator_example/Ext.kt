package ru.impression.ui_generator_example

import android.view.View
import androidx.core.view.isInvisible
import androidx.databinding.BindingAdapter

object DataBindingExt {

    @JvmStatic
    @BindingAdapter("isInvisible")
    fun setIsVisible(view: View, value: Boolean) {
        view.isInvisible = value
    }
}