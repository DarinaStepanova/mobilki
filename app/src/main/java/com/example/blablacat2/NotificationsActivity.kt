package com.example.blablacat2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.blablacat2.data.SharedPreferencesHelper

class NotificationsActivity : AppCompatActivity() {
    private lateinit var notificationManager: NotificationManager
    private val channelId = "com.example.blablacat2.notifications"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Получаем NotificationManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений (для Android 8.0+)
        createNotificationChannel()

        val switchNotifications = findViewById<SwitchCompat>(R.id.switchNotifications)
        val buttonTestNotification = findViewById<Button>(R.id.buttonTestNotification)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)

        // Устанавливаем сохраненное состояние уведомлений
        switchNotifications.isChecked = SharedPreferencesHelper.isNotificationsEnabled(this)

        // Устанавливаем обработчик изменения состояния Switch
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Сохраняем состояние в SharedPreferences
            SharedPreferencesHelper.saveNotificationsEnabled(this, isChecked)

            // Включаем или выключаем уведомления
            if (isChecked) {
                enableNotifications()
            } else {
                disableNotifications()
            }
        }

        // Обработка нажатия на кнопку "Отправить тестовое уведомление"
        buttonTestNotification.setOnClickListener {
            sendTestNotification()
        }

        // Обработка нажатия на кнопку "Назад"
        buttonBack.setOnClickListener {
            finish() // Закрываем текущую активность и возвращаемся на предыдущую
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BlaBlaCat2 Notifications"
            val descriptionText = "Уведомления от приложения BlaBlaCat2"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendTestNotification() {
        if (SharedPreferencesHelper.isNotificationsEnabled(this)) {
            // Создаем уведомление
            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Тестовое уведомление")
                .setContentText("Это тестовое уведомление для проверки работы системы.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Отправляем уведомление
            with(NotificationManagerCompat.from(this)) {
                notify(1, builder.build())
            }
        } else {
            // Если уведомления выключены, показываем сообщение пользователю
            Toast.makeText(this, "Уведомления выключены.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableNotifications() {
        SharedPreferencesHelper.saveNotificationsEnabled(this, true)
        Toast.makeText(this, "Уведомления включены.", Toast.LENGTH_SHORT).show()
    }

    private fun disableNotifications() {
        SharedPreferencesHelper.saveNotificationsEnabled(this, false)
        Toast.makeText(this, "Уведомления выключены.", Toast.LENGTH_SHORT).show()
    }
}