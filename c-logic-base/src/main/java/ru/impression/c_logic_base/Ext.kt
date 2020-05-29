package ru.impression.c_logic_base

import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

val View.activity: AppCompatActivity
    get() {
        var contextWrapper = (context as ContextWrapper)
        while (contextWrapper !is AppCompatActivity) {
            contextWrapper =
                contextWrapper.baseContext as ContextWrapper? ?: throw NoSuchFieldException()
        }
        return contextWrapper
    }