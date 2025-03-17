package com.example.blablacat2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class OnboardingActivity : BasicActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val context: Context = this
        val items = listOf(
            OnboardingItem(R.drawable.arenda, getString(R.string.slide1_title), getString(R.string.slide1_desc)),
            OnboardingItem(R.drawable.safe, getString(R.string.slide2_title), getString(R.string.slide2_desc)),
            OnboardingItem(R.drawable.best, getString(R.string.slide3_title), getString(R.string.slide3_desc))
        )

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = OnboardingAdapter(items)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        val btnSkip = findViewById<Button>(R.id.btnSkip)
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnSkip.setOnClickListener {
            completeOnboarding()
        }

        btnNext.setOnClickListener {
            if (viewPager.currentItem < items.size - 1) {
                viewPager.currentItem += 1
            } else {
                completeOnboarding()
            }
        }
    }


    private fun completeOnboarding() {
        sharedPreferences.edit().putBoolean("onboarding_completed", true).apply()
        if (!isInternetAvailable()) {
            startActivity(Intent(this, NoConnectionActivity::class.java))
            finish()
        }

        startActivity(Intent(this, LoginingActivity::class.java))
        if (!isFinishing) {
            finish()
        }
    }
}
