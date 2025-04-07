package com.example.blablacat2

import android.content.Intent
import android.os.Bundle
import android.widget.Button

class CongratsActivity : BasicActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congrats)

        val buttonNext = findViewById<Button>(R.id.buttonNext)

        buttonNext.setOnClickListener {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }

            // Перейти на главный экран приложения
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}