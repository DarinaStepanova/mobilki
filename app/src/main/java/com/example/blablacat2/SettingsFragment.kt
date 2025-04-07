package com.example.blablacat2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.blablacat2.data.database.AppDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*

class SettingsFragment : Fragment() {

    private lateinit var userAvatar: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var menuBookings: MaterialButton
    private lateinit var menuTheme: MaterialButton
    private lateinit var menuNotifications: MaterialButton
    private lateinit var menuConnectCar: MaterialButton
    private lateinit var menuHelp: MaterialButton
    private lateinit var menuInviteFriend: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация элементов
        userAvatar = view.findViewById(R.id.userAvatar)
        userName = view.findViewById(R.id.userName)
        userEmail = view.findViewById(R.id.userEmail)
        menuBookings = view.findViewById(R.id.menuBookings)
        menuTheme = view.findViewById(R.id.menuTheme)
        menuNotifications = view.findViewById(R.id.menuNotifications)
        menuConnectCar = view.findViewById(R.id.menuConnectCar)
        menuHelp = view.findViewById(R.id.menuHelp)
        menuInviteFriend = view.findViewById(R.id.menuInviteFriend)

        // Загружаем профиль пользователя
        loadUserProfile()

        // Обработчики нажатий
        view.findViewById<View>(R.id.profileSection).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment()) // Открываем фрагмент профиля
                .addToBackStack(null) // Позволяет вернуться назад
                .commit()
        }

        menuBookings.setOnClickListener {
            startActivity(Intent(activity, BookingsActivity::class.java))
        }
        menuTheme.setOnClickListener {
            startActivity(Intent(activity, ThemeActivity::class.java))
        }
        menuNotifications.setOnClickListener {
            startActivity(Intent(activity, NotificationsActivity::class.java))
        }
        menuConnectCar.setOnClickListener {
            startActivity(Intent(activity, MainActivity::class.java))
        }
        menuHelp.setOnClickListener {
            startActivity(Intent(activity, MainActivity::class.java))
        }
        menuInviteFriend.setOnClickListener {
            shareInvitation()
        }
    }

    private fun loadUserProfile() {
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = AppDatabase.getDatabase(context ?: return@launch).userDao()
            val user = userDao.getCurrentUser()

            withContext(Dispatchers.Main) {
                if (user != null) {
                    userName.text = user.firstName
                    userEmail.text = user.email

                    user.profilePhotoUri?.let { uriString ->
                        val uri = Uri.parse(uriString)
                        try {
                            val resolver = context?.contentResolver ?: return@let

                            // Проверяем, доступен ли файл
                            context?.contentResolver?.openInputStream(uri)?.close()

                            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            resolver.takePersistableUriPermission(uri, takeFlags)
                            userAvatar.setImageURI(uri)
                        } catch (e: SecurityException) {
                            showToast("Нет доступа к изображению. Выберите новое фото.")
                        } catch (e: Exception) {
                            showToast("Файл не найден. Выберите новое фото.")
                        }
                    }
                }
            }
        }
    }
    private fun shareInvitation() {
        val inviteText = "🚀 Привет! Попробуй это крутое приложение! Скачай его по ссылке: https://catcar.com/app"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Приглашение в приложение BlaBlaCat")
            putExtra(Intent.EXTRA_TEXT, inviteText)
        }

        startActivity(Intent.createChooser(shareIntent, "Поделиться с другом"))
    }


    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
