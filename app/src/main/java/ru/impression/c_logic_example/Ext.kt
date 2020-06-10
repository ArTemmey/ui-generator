package ru.impression.c_logic_example

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

fun View.fadeIn(duration: Long, callback: (() -> Unit)? = null) {
    startAnimation(AlphaAnimation(0f, 1f).apply {
        this.duration = duration
        setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    callback?.invoke()
                }

                override fun onAnimationStart(animation: Animation?) {
                }

            }
        )
    })
}

fun View.fadeOut(duration: Long, callback: (() -> Unit)? = null) {
    startAnimation(AlphaAnimation(1f, 0f).apply {
        this.duration = duration
        setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    callback?.invoke()
                }

                override fun onAnimationStart(animation: Animation?) {
                }

            }
        )
    })
}

fun View.translateLeft(duration: Long, callback: (() -> Unit)? = null) {
    startAnimation(TranslateAnimation(0f, -100f, 0f, 0f).apply {
        this.duration = duration
        setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    callback?.invoke()
                }

                override fun onAnimationStart(animation: Animation?) {
                }

            }
        )
    })
}

fun View.translateRight(duration: Long, callback: (() -> Unit)? = null) {
    startAnimation(TranslateAnimation(0f, 100f, 0f, 0f).apply {
        this.duration = duration
        setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    callback?.invoke()
                }

                override fun onAnimationStart(animation: Animation?) {
                }

            }
        )
    })
}