package com.example.blablacat2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterDetailsActivity : BasicActivity() {

    private lateinit var lastNameInputLayout: TextInputLayout
    private lateinit var firstNameInputLayout: TextInputLayout
    private lateinit var dateOfBirthInputLayout: TextInputLayout
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var buttonNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_naming)

        lastNameInputLayout = findViewById(R.id.textInputLayoutLastName)
        firstNameInputLayout = findViewById(R.id.textInputLayoutFirstName)
        dateOfBirthInputLayout = findViewById(R.id.textInputLayoutDateOfBirth)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        val lastNameInput = findViewById<TextInputEditText>(R.id.editTextLastName)
        val firstNameInput = findViewById<TextInputEditText>(R.id.editTextFirstName)
        val dateOfBirthInput = findViewById<TextInputEditText>(R.id.editTextDateOfBirth)
        buttonNext = findViewById(R.id.buttonNext)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)

        // TextWatcher для Last Name
        lastNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateLastName(s.toString().trim())
                updateNextButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // TextWatcher для First Name
        firstNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateFirstName(s.toString().trim())
                updateNextButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // TextWatcher для Date of Birth
        dateOfBirthInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateDateOfBirth(s.toString().trim())
                updateNextButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Обработчик для RadioGroup
        radioGroupGender.setOnCheckedChangeListener { _, _ ->
            updateNextButtonState()
        }

        // Обработчик для Date of Birth
        dateOfBirthInput.setOnClickListener {
            showDatePickerDialog(dateOfBirthInput)
        }

        // Обработчик для кнопки "Далее"
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
                }

                // Успешная регистрация
                startActivity(Intent(this, UploadDocumentsActivity::class.java))
                finish()
            }
        }

        // Обработчик для кнопки "Назад"
        buttonBack.setOnClickListener {
            if (!isInternetAvailable()) {
                startActivity(Intent(this, NoConnectionActivity::class.java))
                finish()
            }

            finish() // Вернуться на предыдущий экран
        }
    }

    private fun validateInput(lastName: String, firstName: String, dateOfBirth: String, gender: String): Boolean {
        var isValid = true

        if (lastName.isEmpty()) {
            showToast(getString(R.string.empty_all))
            isValid = false
        }

        if (firstName.isEmpty()) {
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
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            dateFormat.isLenient = false
            dateFormat.parse(dateOfBirth)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isUserAtLeast18(dateOfBirth: String): Boolean {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val birthDate = dateFormat.parse(dateOfBirth)
        val calendar = Calendar.getInstance()
        calendar.time = birthDate

        val currentCalendar = Calendar.getInstance()
        val currentYear = currentCalendar.get(Calendar.YEAR)
        val currentMonth = currentCalendar.get(Calendar.MONTH)
        val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)

        val birthYear = calendar.get(Calendar.YEAR)
        val birthMonth = calendar.get(Calendar.MONTH)
        val birthDay = calendar.get(Calendar.DAY_OF_MONTH)

        return (currentYear - birthYear > 18) ||
                (currentYear - birthYear == 18 && currentMonth > birthMonth) ||
                (currentYear - birthYear == 18 && currentMonth == birthMonth && currentDay >= birthDay)
    }

    private fun validateLastName(lastName: String) {
        if (lastName.isEmpty()) {
            lastNameInputLayout.error = getString(R.string.empty)
        } else {
            lastNameInputLayout.error = null
        }
    }

    private fun validateFirstName(firstName: String) {
        if (firstName.isEmpty()) {
            firstNameInputLayout.error = getString(R.string.empty)
        } else {
            firstNameInputLayout.error = null
        }
    }

    private fun validateDateOfBirth(dateOfBirth: String) {
        if (dateOfBirth.isEmpty()) {
            dateOfBirthInputLayout.error = getString(R.string.empty)
        } else if (!isValidDateOfBirth(dateOfBirth)) {
            dateOfBirthInputLayout.error = getString(R.string.correct_birth)
        } else if (!isUserAtLeast18(dateOfBirth)) {
            dateOfBirthInputLayout.error = getString(R.string.y_old)
        } else {
            dateOfBirthInputLayout.error = null
        }
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
                val selectedDate = String.format(Locale.US, "%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear)
                dateOfBirthInput.setText(selectedDate)
                validateDateOfBirth(selectedDate)
                updateNextButtonState()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}