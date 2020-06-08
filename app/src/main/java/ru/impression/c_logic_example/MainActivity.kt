package ru.impression.c_logic_example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.impression.c_logic_example.fragment.MyFragmentComponent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MyFragmentComponent().apply { text = "Hello world!" })
            .commit()
    }
}