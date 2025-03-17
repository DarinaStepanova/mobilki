package com.example.blablacat2

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NoConnectionActivity : BasicActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 3000 // Проверка сети каждые 3 секунды
    private var lastScreen: String? = null // Последний экран перед обрывом сети

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_connection)

        val retryButton = findViewById<Button>(R.id.button_retry)

        // Получаем экран, на котором пропал интернет
        lastScreen = intent.getStringExtra("LAST_SCREEN")

        retryButton.setOnClickListener {
            checkInternetAndProceed()
        }

        // Запускаем автоматическую проверку сети
        startCheckingInternet()
    }

    private fun startCheckingInternet() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isInternetAvailable()) {
                    navigateToLastScreen()
                } else {
                    handler.postDelayed(this, checkInterval)
                }
            }
        }, checkInterval)
    }

    private fun checkInternetAndProceed() {
        if (isInternetAvailable()) {
            navigateToLastScreen()
        }
    }

    private fun navigateToLastScreen() {
        val intent = when (lastScreen) {
            "CongratsActivity" -> Intent(this, CongratsActivity::class.java)
            "UploadDocumentsActivity" -> Intent(this, UploadDocumentsActivity::class.java)
            "RegisterDetailsActivity" -> Intent(this, RegisterDetailsActivity::class.java)
            "RegisterActivity" -> Intent(this, RegisterActivity::class.java)
            "LoginActivity" -> Intent(this, LoginActivity::class.java)
            "LoginingActivity" -> Intent(this, LoginingActivity::class.java)
            "OnboardingActivity" -> Intent(this, OnboardingActivity::class.java)
            "MainActivity" -> Intent(this, MainActivity::class.java)
            else -> Intent(this, MainActivity::class.java) // По умолчанию на главный экран
        }
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Останавливаем проверки при закрытии экрана
    }
}
