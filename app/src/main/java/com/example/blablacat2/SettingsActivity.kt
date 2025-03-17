package com.example.blablacat2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : BasicActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchTheme: SwitchMaterial
    private lateinit var switchNotifications: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("UserSettings", Context.MODE_PRIVATE)

        val avatarImageView = findViewById<ImageView>(R.id.imageViewAvatar)
        val userNameTextView = findViewById<TextView>(R.id.textViewUserName)
        val userEmailTextView = findViewById<TextView>(R.id.textViewUserEmail)
        val profileSection = findViewById<LinearLayout>(R.id.profileSection)

        switchTheme = findViewById(R.id.switchTheme)
        switchNotifications = findViewById(R.id.switchNotifications)

        val bookingsSection = findViewById<LinearLayout>(R.id.bookingsSection)
        val addCarSection = findViewById<LinearLayout>(R.id.addCarSection)
        val helpSection = findViewById<LinearLayout>(R.id.helpSection)
        val inviteFriendSection = findViewById<LinearLayout>(R.id.inviteFriendSection)

        // Загружаем сохраненные настройки
        loadSettings()

        // Переход на экран профиля
        profileSection.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Переход на экран "Мои бронирования"
        bookingsSection.setOnClickListener {
            startActivity(Intent(this, BookingsActivity::class.java))
        }

        // Подключить свой автомобиль
        addCarSection.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }

        // Помощь
        helpSection.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // Пригласить друга
        inviteFriendSection.setOnClickListener {
            shareApp()
        }

        // Переключатель темы
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            saveThemeSetting(isChecked)
        }

        // Переключатель уведомлений
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationSetting(isChecked)
        }
    }

    private fun loadSettings() {
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications", true)

        switchTheme.isChecked = isDarkMode
        switchNotifications.isChecked = isNotificationsEnabled
    }

    private fun saveThemeSetting(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", isDarkMode).apply()
        // Тут можно добавить логику смены темы
    }

    private fun saveNotificationSetting(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications", isEnabled).apply()
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Попробуй наше приложение! Скачай его по ссылке...")
        startActivity(Intent.createChooser(intent, "Пригласить друга"))
    }
}
