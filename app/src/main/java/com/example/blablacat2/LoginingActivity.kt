package com.example.blablacat2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginingActivity : BasicActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logining)

        val buttonSignIn: Button = findViewById(R.id.button_sign_in)
        val buttonSignUp: Button = findViewById(R.id.button_sign_up)

        buttonSignIn.setOnClickListener {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        buttonSignUp.setOnClickListener {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}