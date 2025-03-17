package com.example.blablacat2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity


class MainActivity : BasicActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Пауза 2 секунды, затем переход на главный экран
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }
            startActivity(Intent(this, BaseActivity::class.java))
            finish()
        }, 2000)
    }
}