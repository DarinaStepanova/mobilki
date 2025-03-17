package com.example.blablacat2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : BasicActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var checkBoxTerms: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailInputLayout = findViewById(R.id.textInputLayoutEmail)
        passwordInputLayout = findViewById(R.id.textInputLayoutPassword)
        confirmPasswordInputLayout = findViewById(R.id.textInputLayoutConfirmPassword)
        checkBoxTerms = findViewById(R.id.checkBoxTerms)
        val emailInput = findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.editTextPassword)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.editTextConfirmPassword)
        val buttonNext = findViewById<Button>(R.id.buttonNext)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)

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

        // TextWatcher для Confirm Password
        confirmPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateConfirmPassword(s.toString().trim())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        buttonNext.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (validateInput(email, password, confirmPassword)) {
                if (!isInternetAvailable()) {
                    startActivity(Intent(this, NoConnectionActivity::class.java))
                    finish()
                }

                // Перейти на следующий экран регистрации
                startActivity(Intent(this, RegisterDetailsActivity::class.java))
            }
        }

        buttonBack.setOnClickListener {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }

            finish() // Вернуться на предыдущий экран
        }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Проверка email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = getString(R.string.valid_email)
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        // Проверка пароля
        if (password.isEmpty() || password.length < 6) {
            passwordInputLayout.error = getString(R.string.minimum_six)
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        // Проверка подтверждения пароля
        if (confirmPassword.isEmpty() || confirmPassword != password) {
            confirmPasswordInputLayout.error = getString(R.string.valid_password)
            isValid = false
        } else {
            confirmPasswordInputLayout.error = null
        }

        // Проверка CheckBox
        if (!checkBoxTerms.isChecked) {
            Toast.makeText(this, getString(R.string.confid), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
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

    private fun validateConfirmPassword(confirmPassword: String) {
        val password = passwordInputLayout.editText?.text.toString().trim()
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = getString(R.string.empty)
        } else if (confirmPassword != password) {
            confirmPasswordInputLayout.error = getString(R.string.valid_password)
        } else {
            confirmPasswordInputLayout.error = null // Сбросить ошибку
        }
    }
}