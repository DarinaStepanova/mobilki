package com.example.blablacat2


import HomeFragment
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.blablacat2.data.SharedPreferencesHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : BasicActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isOnboardingCompleted = sharedPreferences.getBoolean("onboarding_completed", false)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val theme = SharedPreferencesHelper.getTheme(this)
        if (theme == "dark") {
            setTheme(R.style.Night_Theme_BlaBlaCat2)
        } else {
            setTheme(R.style.Base_Theme_BlaBlaCat2)
        }
        if (isOnboardingCompleted) {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }
            // Проверяем, вошёл ли пользователь в профиль
            if (!SharedPreferencesHelper.isLoggedIn(this)) {
                // Если пользователь не вошёл, переходим на LoginingActivity
                startActivity(Intent(this, LoginingActivity::class.java))
                finish() // Завершаем текущую активность
                return
            }
            else {
                val homeFragment = HomeFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, homeFragment) // R.id.fragmentContainer — это ID контейнера в разметке
                    .addToBackStack(null) // Добавляем транзакцию в стек, чтобы можно было вернуться назад
                    .commit()
            }
        } else {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_favorites -> {
                    loadFragment(FavoritesFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

