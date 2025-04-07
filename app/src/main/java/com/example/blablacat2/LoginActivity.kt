package com.example.blablacat2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.blablacat2.data.SharedPreferencesHelper
import com.example.blablacat2.data.database.AppDatabase
import com.example.blablacat2.data.dao.UserDao
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*

class LoginActivity : BasicActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализация БД
        val database = AppDatabase.getDatabase(this)
        userDao = database.userDao()

        emailInputLayout = findViewById(R.id.textInputLayoutEmail)
        passwordInputLayout = findViewById(R.id.textInputLayoutPassword)
        val emailInput = findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val googleLoginButton = findViewById<MaterialButton>(R.id.googleButton)
        val registerText = findViewById<TextView>(R.id.textRegister)
        progressBar = findViewById(R.id.progressBar)

        // Валидация email
        emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Валидация пароля
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
            emailInputLayout.error = null
        }
    }

    private fun validatePassword(password: String) {
        if (password.isEmpty()) {
            passwordInputLayout.error = getString(R.string.empty)
        } else if (password.length < 6) {
            passwordInputLayout.error = getString(R.string.minimum_six)
        } else {
            passwordInputLayout.error = null
        }
    }

    private fun loginUser(email: String, password: String) {
        if (!isInternetAvailable()) {
            startActivity(Intent(this, NoConnectionActivity::class.java))
            finish()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.password == password) {
                userDao.logoutAllUsers() // Сбрасываем всех авторизованных
                userDao.insertUser(user.copy(isLoggedIn = true)) // Авторизуем текущего пользователя

                withContext(Dispatchers.Main) {
                    SharedPreferencesHelper.saveIsLoggedIn(this@LoginActivity, true)
                    Toast.makeText(this@LoginActivity, getString(R.string.lucky), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, BaseActivity::class.java))
                    finish()
                }
            } else {
                withContext(Dispatchers.Main) {
                    showToast("Неверный email или пароль")
                }
            }
        }
    }


    private fun saveCurrentUserEmail(email: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("current_user_email", email)
            apply()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
