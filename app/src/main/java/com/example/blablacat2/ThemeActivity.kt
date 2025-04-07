package com.example.blablacat2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.blablacat2.data.SharedPreferencesHelper

class ThemeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Применяем тему перед загрузкой разметки
        when (SharedPreferencesHelper.getTheme(this)) {
            "light" -> setTheme(R.style.Base_Theme_BlaBlaCat2)
            "dark" -> setTheme(R.style.Night_Theme_BlaBlaCat2)
        }

        setContentView(R.layout.activity_theme)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupTheme)
        val radioLight = findViewById<RadioButton>(R.id.radioLight)
        val radioDark = findViewById<RadioButton>(R.id.radioDark)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)

        // Устанавливаем текущую тему
        when (SharedPreferencesHelper.getTheme(this)) {
            "light" -> radioLight.isChecked = true
            "dark" -> radioDark.isChecked = true
        }

        // Обработка изменения выбранной темы
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = if (checkedId == R.id.radioLight) "light" else "dark"
            SharedPreferencesHelper.saveTheme(this, selectedTheme)

            // Применяем тему и пересоздаем активность
            recreate()
        }

        // Обработка нажатия на кнопку "Назад"
        buttonBack.setOnClickListener {
            finish() // Закрываем текущую активность и возвращаемся на предыдущую
        }
    }
}