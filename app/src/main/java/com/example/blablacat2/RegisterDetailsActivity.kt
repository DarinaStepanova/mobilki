package com.example.blablacat2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import com.example.blablacat2.data.database.AppDatabase
import com.example.blablacat2.data.dao.UserDao
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class RegisterDetailsActivity : BasicActivity() {

    private lateinit var lastNameInputLayout: TextInputLayout
    private lateinit var firstNameInputLayout: TextInputLayout
    private lateinit var dateOfBirthInputLayout: TextInputLayout
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var buttonNext: Button
    private lateinit var userDao: UserDao
    private var email: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_naming)

        // Получаем email и пароль из RegisterActivity
        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")

        val database = AppDatabase.getDatabase(this)
        userDao = database.userDao()

        lastNameInputLayout = findViewById(R.id.textInputLayoutLastName)
        firstNameInputLayout = findViewById(R.id.textInputLayoutFirstName)
        dateOfBirthInputLayout = findViewById(R.id.textInputLayoutDateOfBirth)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        val lastNameInput = findViewById<TextInputEditText>(R.id.editTextLastName)
        val firstNameInput = findViewById<TextInputEditText>(R.id.editTextFirstName)
        val dateOfBirthInput = findViewById<TextInputEditText>(R.id.editTextDateOfBirth)
        buttonNext = findViewById(R.id.buttonNext)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)

        // Поля ввода
        lastNameInput.addTextChangedListener(createTextWatcher())
        firstNameInput.addTextChangedListener(createTextWatcher())
        dateOfBirthInput.addTextChangedListener(createTextWatcher())

        // Обработчик выбора пола
        radioGroupGender.setOnCheckedChangeListener { _, _ -> updateNextButtonState() }

        // Обработчик для Date of Birth
        dateOfBirthInput.setOnClickListener {
            showDatePickerDialog(dateOfBirthInput)
        }

        // Кнопка "Далее"
        buttonNext.setOnClickListener {
            val lastName = lastNameInput.text.toString().trim()
            val firstName = firstNameInput.text.toString().trim()
            val dateOfBirth = dateOfBirthInput.text.toString().trim()
            val gender = when (radioGroupGender.checkedRadioButtonId) {
                R.id.radioButtonMale -> getString(R.string.male)
                R.id.radioButtonFemale -> getString(R.string.female)
                else -> ""
            }

            if (validateInput(lastName, firstName, dateOfBirth, gender)) {
                if (!isInternetAvailable()) {
                    startActivity(Intent(this, NoConnectionActivity::class.java))
                    finish()
                } else {
                    saveUserDetailsAndProceed(lastName, firstName, dateOfBirth, gender)
                }
            }
        }

        // Кнопка "Назад"
        buttonBack.setOnClickListener {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }
            finish()
        }
    }

    private fun saveUserDetailsAndProceed(lastName: String, firstName: String, dateOfBirth: String, gender: String) {
        if (email == null || password == null) {
            showToast("Ошибка: отсутствуют email или пароль")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val user = userDao.getUserByEmail(email!!)
            if (user != null) {
                userDao.insertUser(user.copy(
                    lastName = lastName,
                    firstName = firstName,
                    birthDate = dateOfBirth,
                    gender = gender
                ))

                withContext(Dispatchers.Main) {
                    val intent = Intent(this@RegisterDetailsActivity, UploadDocumentsActivity::class.java)
                    intent.putExtra("email", email) // Передаем email дальше
                    startActivity(intent)
                    finish()
                }
            } else {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка: пользователь не найден")
                }
            }
        }
    }

    private fun validateInput(lastName: String, firstName: String, dateOfBirth: String, gender: String): Boolean {
        var isValid = true

        if (lastName.isEmpty() || firstName.isEmpty()) {
            showToast(getString(R.string.empty_all))
            isValid = false
        }

        if (dateOfBirth.isEmpty() || !isValidDateOfBirth(dateOfBirth)) {
            showToast(getString(R.string.correct_birth))
            isValid = false
        } else if (!isUserAtLeast18(dateOfBirth)) {
            showToast(getString(R.string.y_old))
            isValid = false
        }

        if (gender.isEmpty()) {
            showToast(getString(R.string.y_sex))
            isValid = false
        }

        return isValid
    }

    private fun isValidDateOfBirth(dateOfBirth: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            dateFormat.isLenient = false
            dateFormat.parse(dateOfBirth)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isUserAtLeast18(dateOfBirth: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val birthDate = dateFormat.parse(dateOfBirth)
        val calendar = Calendar.getInstance()
        calendar.time = birthDate

        val currentCalendar = Calendar.getInstance()
        return currentCalendar.get(Calendar.YEAR) - calendar.get(Calendar.YEAR) >= 18
    }

    private fun updateNextButtonState() {
        val lastName = lastNameInputLayout.editText?.text.toString().trim()
        val firstName = firstNameInputLayout.editText?.text.toString().trim()
        val dateOfBirth = dateOfBirthInputLayout.editText?.text.toString().trim()
        val gender = when (radioGroupGender.checkedRadioButtonId) {
            R.id.radioButtonMale -> "Мужской"
            R.id.radioButtonFemale -> "Женский"
            else -> ""
        }

        buttonNext.isEnabled = lastName.isNotEmpty() &&
                firstName.isNotEmpty() &&
                dateOfBirth.isNotEmpty() &&
                gender.isNotEmpty() &&
                isValidDateOfBirth(dateOfBirth) &&
                isUserAtLeast18(dateOfBirth)
    }

    private fun showDatePickerDialog(dateOfBirthInput: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format(Locale.US, "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                dateOfBirthInput.setText(selectedDate)
                updateNextButtonState()
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun createTextWatcher() = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateNextButtonState()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
