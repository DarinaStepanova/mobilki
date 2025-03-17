package com.example.blablacat2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : BasicActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInputLayout = findViewById(R.id.textInputLayoutEmail)
        passwordInputLayout = findViewById(R.id.textInputLayoutPassword)
        val emailInput = findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val googleLoginButton = findViewById<MaterialButton>(R.id.googleButton)
        val registerText = findViewById<TextView>(R.id.textRegister)
        progressBar = findViewById(R.id.progressBar)

        // TextWatcher для Email
        emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString().trim())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // TextWatcher для Password
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validatePassword(s.toString().trim())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInput(email, password)) {
                progressBar.visibility = ProgressBar.VISIBLE
                loginUser(email, password)
            }
        }

        googleLoginButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.google_sign_in), Toast.LENGTH_SHORT).show()
        }

        registerText.setOnClickListener {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }

            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast(getString(R.string.valid_email))
                false
            }
            password.isEmpty() || password.length < 6 -> {
                showToast(getString(R.string.minimum_six))
                false
            }
            else -> true
        }
    }

    private fun validateEmail(email: String) {
        if (email.isEmpty()) {
            emailInputLayout.error = getString(R.string.empty)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = getString(R.string.valid_email)
        } else {
            emailInputLayout.error = null // Сбросить ошибку
        }
    }

    private fun validatePassword(password: String) {
        if (password.isEmpty()) {
            passwordInputLayout.error = getString(R.string.empty)
        } else if (password.length < 6) {
            passwordInputLayout.error = getString(R.string.minimum_six)
        } else {
            passwordInputLayout.error = null // Сбросить ошибку
        }
    }

    private fun loginUser(email: String, password: String) {
        if (!isInternetAvailable()) {
            startActivity(Intent(this, NoConnectionActivity::class.java))
            finish()
        }

        // Здесь должен быть запрос к серверу (имитация)
        Toast.makeText(this, getString(R.string.lucky), Toast.LENGTH_SHORT).show()
        progressBar.visibility = ProgressBar.GONE
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}