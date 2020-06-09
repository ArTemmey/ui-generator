package ru.impression.c_logic_example

import android.app.Application
import android.content.Context

lateinit var context: Context

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}