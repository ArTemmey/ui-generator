@file:SuppressLint("RestrictedApi")

package ru.impression.ui_generator_base

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.databinding.adapters.TextViewBindingAdapter

fun TextView.bindText(text: CharSequence?) = TextViewBindingAdapter.setText(this, text)

fun View.dp(value: Int) = value * context.resources.displayMetrics.density

fun View.updateLayoutParams(
    width: Int? = null,
    height: Int? = null,
    marginStart: Int? = null,
    marginTop: Int? = null,
    marginEnd: Int? = null,
    marginBottom: Int? = null
) {
    layoutParams?.let {
        if ((width == null || width == it.width)
            && (height == null || height == it.height)
            && (marginStart == null || marginStart == this.marginStart)
            && (marginTop == null || marginTop == this.marginTop)
            && (marginEnd == null || marginEnd == this.marginEnd)
            && (marginBottom == null || marginBottom == this.marginBottom)
        ) return
    }
    layoutParams =
        if (marginStart != null || marginTop != null || marginEnd != null || marginBottom != null)
            (layoutParams as? ViewGroup.MarginLayoutParams
                ?: ViewGroup.MarginLayoutParams(width ?: WRAP_CONTENT, height ?: WRAP_CONTENT))
                .apply {
                    marginStart?.let { leftMargin = it }
                    marginTop?.let { topMargin = it }
                    marginEnd?.let { rightMargin = it }
                    marginBottom?.let { bottomMargin = it }
                }
        else
            layoutParams ?: ViewGroup.LayoutParams(width ?: WRAP_CONTENT, height ?: WRAP_CONTENT)
}