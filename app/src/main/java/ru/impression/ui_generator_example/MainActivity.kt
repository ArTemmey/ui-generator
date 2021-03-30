package ru.impression.ui_generator_example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        supportFragmentManager.findFragmentByTag(MainFragmentComponent::class.qualifiedName)
            ?: supportFragmentManager.beginTransaction().replace(
                R.id.container,
                MainFragmentComponent().apply { welcomeText = "Hello world!" },
                MainFragmentComponent::class.qualifiedName
            ).commit()
    }
}