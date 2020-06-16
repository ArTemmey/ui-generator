package ru.impression.ui_generator_example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.impression.ui_generator_example.fragment.MainFragmentComponent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container)
        supportFragmentManager.findFragmentByTag(MainFragmentComponent::class.qualifiedName)
            ?: supportFragmentManager.beginTransaction().replace(
                R.id.container,
                MainFragmentComponent().apply { defaultWelcomeText = "Hello world!" },
                MainFragmentComponent::class.qualifiedName
            ).commit()
    }
}