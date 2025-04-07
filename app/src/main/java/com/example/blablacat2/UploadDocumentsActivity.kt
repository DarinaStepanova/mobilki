package com.example.blablacat2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.blablacat2.data.database.AppDatabase
import com.example.blablacat2.data.dao.UserDao
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UploadDocumentsActivity : BasicActivity() {

    private lateinit var driversLicenseInputLayout: TextInputLayout
    private lateinit var issuanceDateInputLayout: TextInputLayout
    private lateinit var imageViewProfilePhoto: ImageView
    private lateinit var buttonNext: Button
    private lateinit var buttonUploadDriversLicensePhoto: MaterialButton
    private lateinit var buttonUploadPassportPhoto: MaterialButton
    private lateinit var userDao: UserDao

    private var profilePhotoUri: Uri? = null
    private var driversLicensePhotoUri: Uri? = null
    private var passportPhotoUri: Uri? = null
    private var email: String? = null
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_documents)

        email = intent.getStringExtra("email")
        val database = AppDatabase.getDatabase(this)
        userDao = database.userDao()

        driversLicenseInputLayout = findViewById(R.id.textInputLayoutDriversLicense)
        issuanceDateInputLayout = findViewById(R.id.textInputLayoutIssuanceDate)
        imageViewProfilePhoto = findViewById(R.id.imageViewProfilePhoto)
        val driversLicenseInput = findViewById<TextInputEditText>(R.id.editTextDriversLicense)
        val issuanceDateInput = findViewById<TextInputEditText>(R.id.editTextIssuanceDate)
        buttonNext = findViewById(R.id.buttonNext)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        buttonUploadDriversLicensePhoto = findViewById(R.id.buttonUploadDriversLicensePhoto)
        buttonUploadPassportPhoto = findViewById(R.id.buttonUploadPassportPhoto)

        checkPermissions()

        issuanceDateInput.setOnClickListener {
            showDatePickerDialog(issuanceDateInput)
        }

        imageViewProfilePhoto.setOnClickListener {
            openImagePickerOrCamera(R.id.imageViewProfilePhoto)
        }

        buttonUploadDriversLicensePhoto.setOnClickListener {
            openImagePickerOrCamera(R.id.buttonUploadDriversLicensePhoto)
        }

        buttonUploadPassportPhoto.setOnClickListener {
            openImagePickerOrCamera(R.id.buttonUploadPassportPhoto)
        }

        // Добавляем обработчики для активации кнопки
        driversLicenseInput.addTextChangedListener(createTextWatcher())
        issuanceDateInput.addTextChangedListener(createTextWatcher())

        buttonNext.setOnClickListener {
            saveUserDocuments()
        }

        buttonBack.setOnClickListener {
            finish()
        }

        updateNextButtonState() // Проверяем состояние кнопки при запуске
    }

    private fun saveUserDocuments() {
        val driversLicenseNumber = driversLicenseInputLayout.editText?.text.toString().trim()
        val issuanceDate = issuanceDateInputLayout.editText?.text.toString().trim()

        if (validateInput(driversLicenseNumber, issuanceDate)) {
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUserByEmail(email!!)
                if (user != null) {
                    userDao.insertUser(user.copy(
                        licenseNumber = driversLicenseNumber,
                        licenseIssueDate = issuanceDate,
                        profilePhotoUri = profilePhotoUri?.toString(),
                        licensePhotoUri = driversLicensePhotoUri?.toString(),
                        passportPhotoUri = passportPhotoUri?.toString()
                    ))

                    withContext(Dispatchers.Main) {
                        startActivity(Intent(this@UploadDocumentsActivity, CongratsActivity::class.java))
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Ошибка: пользователь не найден")
                    }
                }
            }
        }
    }

    private fun validateInput(driversLicenseNumber: String, issuanceDate: String): Boolean {
        return driversLicenseNumber.matches(Regex("\\d{10}")) &&
                issuanceDate.isNotEmpty() &&
                driversLicensePhotoUri != null &&
                passportPhotoUri != null
    }

    private fun updateNextButtonState() {
        val driversLicenseNumber = driversLicenseInputLayout.editText?.text.toString().trim()
        val issuanceDate = issuanceDateInputLayout.editText?.text.toString().trim()

        buttonNext.isEnabled = validateInput(driversLicenseNumber, issuanceDate)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val granted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!granted) {
            ActivityCompat.requestPermissions(this, permissions, 102)
        }
    }

    private fun openCamera(viewId: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File = createImageFile()
            val photoUri: Uri = FileProvider.getUriForFile(
                this, "$packageName.fileprovider", photoFile
            )
            currentImageUri = photoUri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(intent, viewId)
        }
    }

    private fun openGallery(viewId: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        startActivityForResult(intent, viewId)
    }



    private fun createImageFile(): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
    }


    private fun openImagePickerOrCamera(viewId: Int) {
        val options = arrayOf("Камера", "Галерея")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Выберите источник")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera(viewId)
                1 -> openGallery(viewId)
            }
        }
        builder.show()
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateNextButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data ?: currentImageUri

            if (selectedImageUri != null) {
                try {
                    // Проверяем, можно ли взять persistable permission
                    contentResolver.takePersistableUriPermission(
                        selectedImageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

                when (requestCode) {
                    R.id.imageViewProfilePhoto -> {
                        profilePhotoUri = selectedImageUri
                        imageViewProfilePhoto.setImageURI(selectedImageUri)
                    }
                    R.id.buttonUploadDriversLicensePhoto -> {
                        driversLicensePhotoUri = selectedImageUri
                        buttonUploadDriversLicensePhoto.background = getDrawableFromUri(selectedImageUri)
                        buttonUploadDriversLicensePhoto.backgroundTintMode = PorterDuff.Mode.ADD
                        val layoutParams = buttonUploadDriversLicensePhoto.layoutParams
                        layoutParams.width = 200 // Устанавливаем ширину в пикселях
                        layoutParams.height = 200 // Устанавливаем высоту в пикселях
                        buttonUploadDriversLicensePhoto.layoutParams = layoutParams
                        buttonUploadDriversLicensePhoto.icon = null
                        buttonUploadDriversLicensePhoto.text = null

                    }
                    R.id.buttonUploadPassportPhoto -> {
                        passportPhotoUri = selectedImageUri
                        buttonUploadPassportPhoto.background = getDrawableFromUri(selectedImageUri)
                        buttonUploadPassportPhoto.backgroundTintMode = PorterDuff.Mode.ADD
                        val layoutParams = buttonUploadPassportPhoto.layoutParams
                        layoutParams.width = 200 // Устанавливаем ширину в пикселях
                        layoutParams.height = 200 // Устанавливаем высоту в пикселях
                        buttonUploadPassportPhoto.layoutParams = layoutParams
                        buttonUploadPassportPhoto.icon = null
                        buttonUploadPassportPhoto.text = null
                    }
                }
                updateNextButtonState()
            } else {
                showToast("Ошибка загрузки изображения")
            }
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

    private fun showDatePickerDialog(inputField: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format(Locale.US, "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                inputField.setText(selectedDate)
                updateNextButtonState()
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
