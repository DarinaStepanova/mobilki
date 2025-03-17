package com.example.blablacat2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UploadDocumentsActivity : BasicActivity() {

    private lateinit var driversLicenseInputLayout: TextInputLayout
    private lateinit var issuanceDateInputLayout: TextInputLayout
    private lateinit var imageViewProfilePhoto: ImageView
    private lateinit var buttonNext: Button
    private var profilePhotoUri: Uri? = null
    private var driversLicensePhotoUri: Uri? = null
    private var passportPhotoUri: Uri? = null
    private lateinit var buttonUploadDriversLicensePhoto : MaterialButton
    private lateinit var buttonUploadPassportPhoto: MaterialButton


    private val cameraPermissionCode = 100
    private val galleryPermissionCode = 101
    private var currentImageUri: Uri? = null
    private var currentImageViewId: Int = 0 // Переменная для отслеживания текущего виджета

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_documents)

        driversLicenseInputLayout = findViewById(R.id.textInputLayoutDriversLicense)
        issuanceDateInputLayout = findViewById(R.id.textInputLayoutIssuanceDate)
        imageViewProfilePhoto = findViewById(R.id.imageViewProfilePhoto)
        val driversLicenseInput = findViewById<TextInputEditText>(R.id.editTextDriversLicense)
        val issuanceDateInput = findViewById<TextInputEditText>(R.id.editTextIssuanceDate)
        buttonNext = findViewById(R.id.buttonNext)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        buttonUploadDriversLicensePhoto = findViewById(R.id.buttonUploadDriversLicensePhoto)
        buttonUploadPassportPhoto = findViewById(R.id.buttonUploadPassportPhoto)



        // Проверка разрешений
        checkPermissions()

        // TextWatcher для Driver's License Number
        driversLicenseInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateDriversLicenseNumber(s.toString().trim())
                updateNextButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // TextWatcher для Issuance Date
        issuanceDateInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateIssuanceDate(s.toString().trim())
                updateNextButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Обработчик для Issuance Date
        issuanceDateInput.setOnClickListener {
            showDatePickerDialog(issuanceDateInput)
        }

        // Обработчик для Profile Photo
        imageViewProfilePhoto.setOnClickListener {
            currentImageViewId = R.id.imageViewProfilePhoto // Устанавливаем текущий виджет
            openImagePickerOrCamera(it)
        }

        // Обработчик для кнопки загрузки фото водительского удостоверения
        buttonUploadDriversLicensePhoto.setOnClickListener {
            currentImageViewId = R.id.buttonUploadDriversLicensePhoto // Устанавливаем текущий виджет
            openImagePickerOrCamera(it)
        }

        // Обработчик для кнопки загрузки фото паспорта
        buttonUploadPassportPhoto.setOnClickListener {
            currentImageViewId = R.id.buttonUploadPassportPhoto // Устанавливаем текущий виджет
            openImagePickerOrCamera(it)
        }

        // Обработчик для кнопки "Далее"
        buttonNext.setOnClickListener {
            val driversLicenseNumber = driversLicenseInput.text.toString().trim()
            val issuanceDate = issuanceDateInput.text.toString().trim()

            if (validateInput(driversLicenseNumber, issuanceDate)) {
                if (!isInternetAvailable()) {
                    startActivity(Intent(this, NoConnectionActivity::class.java))
                    finish()
                }

                // Успешная регистрация
                startActivity(Intent(this, CongratsActivity::class.java))
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

    private fun getDrawableFromUri(uri: Uri): Drawable? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            BitmapDrawable(resources, bitmap)
        } catch (e: Exception) {
            null
        }
    }


    private fun validateInput(driversLicenseNumber: String, issuanceDate: String): Boolean {
        var isValid = true

        if (driversLicenseNumber.isEmpty()) {
            showToast("Пожалуйста, заполните все обязательные поля.")
            isValid = false
        }

        if (issuanceDate.isEmpty() || !isValidIssuanceDate(issuanceDate)) {
            showToast("Введите корректную дату выдачи.")
            isValid = false
        }

        if (profilePhotoUri == null) {
            showToast("Пожалуйста, загрузите фото профиля.")
            isValid = false
        }

        if (driversLicensePhotoUri == null) {
            showToast("Пожалуйста, загрузите фото водительского удостоверения.")
            isValid = false
        }

        if (passportPhotoUri == null) {
            showToast("Пожалуйста, загрузите фото паспорта.")
            isValid = false
        }

        return isValid
    }

    private fun isValidDriversLicenseNumber(driversLicenseNumber: String): Boolean {
        return driversLicenseNumber.matches(Regex("\\d{10}"))
    }

    private fun isValidIssuanceDate(issuanceDate: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            dateFormat.isLenient = false
            val issuanceCalendar = Calendar.getInstance()
            issuanceCalendar.time = dateFormat.parse(issuanceDate)

            val currentCalendar = Calendar.getInstance()
            val tenYearsAgo = Calendar.getInstance()
            tenYearsAgo.add(Calendar.YEAR, -10)

            issuanceCalendar.timeInMillis >= tenYearsAgo.timeInMillis &&
                    issuanceCalendar.timeInMillis <= currentCalendar.timeInMillis
        } catch (e: Exception) {
            false
        }
    }

    private fun validateDriversLicenseNumber(driversLicenseNumber: String) {
        if (driversLicenseNumber.isEmpty()) {
            driversLicenseInputLayout.error = "Это поле не может быть пустым"
        } else if (!isValidDriversLicenseNumber(driversLicenseNumber)) {
            driversLicenseInputLayout.error = "Номер водительского удостоверения должен содержать 10 цифр"
        } else {
            driversLicenseInputLayout.error = null
        }
    }

    private fun validateIssuanceDate(issuanceDate: String) {
        if (issuanceDate.isEmpty()) {
            issuanceDateInputLayout.error = "Это поле не может быть пустым"
        } else if (!isValidIssuanceDate(issuanceDate)) {
            issuanceDateInputLayout.error = "Введите корректную дату выдачи"
        } else {
            issuanceDateInputLayout.error = null
        }
    }

    private fun updateNextButtonState() {
        val driversLicenseNumber = driversLicenseInputLayout.editText?.text.toString().trim()
        val issuanceDate = issuanceDateInputLayout.editText?.text.toString().trim()

        buttonNext.isEnabled = driversLicenseNumber.isNotEmpty() &&
                issuanceDate.isNotEmpty() &&
                isValidDriversLicenseNumber(driversLicenseNumber) &&
                isValidIssuanceDate(issuanceDate) &&
                profilePhotoUri != null &&
                driversLicensePhotoUri != null &&
                passportPhotoUri != null
    }

    private fun showDatePickerDialog(issuanceDateInput: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format(Locale.US, "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                issuanceDateInput.setText(selectedDate)
                validateIssuanceDate(selectedDate)
                updateNextButtonState()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun openImagePickerOrCamera(view: View) {
        val options = arrayOf("Камера", "Галерея")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Выберите источник")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera(view)
                1 -> openGallery(view)
            }
        }
        builder.show()
    }

    private fun openCamera(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File = createImageFile()
            val photoUri: Uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                photoFile
            )
            currentImageUri = photoUri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    private fun openGallery(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            val selectedImageUri = data.data ?: currentImageUri
//            when (currentImageViewId) {
//                R.id.imageViewProfilePhoto -> {
//                    profilePhotoUri = selectedImageUri
//                    imageViewProfilePhoto.setImageURI(selectedImageUri)
//                }
//                R.id.buttonUploadDriversLicensePhoto -> {
//                    driversLicensePhotoUri = selectedImageUri
//                }
//                R.id.buttonUploadPassportPhoto -> {
//                    passportPhotoUri = selectedImageUri
//                }
//            }
//            updateNextButtonState()
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data ?: currentImageUri

            if (selectedImageUri != null) {
                when (currentImageViewId) {
                    R.id.imageViewProfilePhoto -> {
                        profilePhotoUri = selectedImageUri
                        imageViewProfilePhoto.setImageURI(selectedImageUri)
                    }
                    R.id.buttonUploadDriversLicensePhoto -> {
                        driversLicensePhotoUri = selectedImageUri
                        buttonUploadDriversLicensePhoto.icon = getDrawableFromUri(selectedImageUri)
                    }
                    R.id.buttonUploadPassportPhoto -> {
                        passportPhotoUri = selectedImageUri
                        buttonUploadPassportPhoto.icon = getDrawableFromUri(selectedImageUri)
                    }
                }
                updateNextButtonState()
            } else {
                showToast("Ошибка загрузки изображения")
            }
        }
    }


    private fun createImageFile(): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
        }

        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), galleryPermissionCode)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PROFILE_PHOTO_REQUEST_CODE = 1
        private const val DRIVERS_LICENSE_PHOTO_REQUEST_CODE = 2
        private const val PASSPORT_PHOTO_REQUEST_CODE = 3
        private const val CAMERA_REQUEST_CODE = 4
        private const val GALLERY_REQUEST_CODE = 5
    }
}