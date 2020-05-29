package ru.impression.c_logic_example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.impression.c_logic_example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ActivityMainBinding.inflate(layoutInflater).apply { text = "HELLO WORLD" }.root
        )
    }
}