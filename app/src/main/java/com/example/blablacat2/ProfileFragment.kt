package com.example.blablacat2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.net.toUri
import com.example.blablacat2.data.database.AppDatabase
import kotlinx.coroutines.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileFragment : Fragment() {

    private lateinit var userAvatar: ImageView
    private lateinit var userFullName: TextView
    private lateinit var userEmail: TextView
    private lateinit var logoutButton: Button

    private lateinit var oldPasswordInput: TextInputEditText
    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var changePasswordButton: Button

    private var avatarUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userAvatar = view.findViewById(R.id.userAvatar)
        userFullName = view.findViewById(R.id.userFullName)
        userEmail = view.findViewById(R.id.userEmail)
        logoutButton = view.findViewById(R.id.logoutButton)

        oldPasswordInput = view.findViewById(R.id.oldPassword)
        newPasswordInput = view.findViewById(R.id.newPassword)
        confirmPasswordInput = view.findViewById(R.id.confirmPassword)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)

        loadUserProfile()

        userAvatar.setOnClickListener {
            openGallery()
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }

        changePasswordButton.setOnClickListener {
            updatePassword()
        }
    }

    private fun loadUserProfile() {
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = AppDatabase.getDatabase(requireContext()).userDao()
            val user = userDao.getCurrentUser()

            withContext(Dispatchers.Main) {
                if (user != null) {
                    userFullName.text = "${user.lastName} ${user.firstName} ${user.middleName ?: ""}".trim()
                    userEmail.text = user.email
                    user.profilePhotoUri?.let {
                        avatarUri = it.toUri()
                        userAvatar.setImageURI(avatarUri)
                    }
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                avatarUri = uri
                userAvatar.setImageURI(uri)

                CoroutineScope(Dispatchers.IO).launch {
                    val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                    val user = userDao.getCurrentUser()
                    if (user != null) {
                        userDao.insertUser(user.copy(profilePhotoUri = uri.toString()))
                    }
                }
            }
        }
    }

    private fun updatePassword() {
        val oldPassword = oldPasswordInput.text.toString().trim()
        val newPassword = newPasswordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (newPassword.length < 6) {
            showToast("Пароль должен содержать не менее 6 символов")
            return
        }

        if (newPassword != confirmPassword) {
            showToast("Пароли не совпадают")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val userDao = AppDatabase.getDatabase(requireContext()).userDao()
            val user = userDao.getCurrentUser()

            if (user != null && user.password == oldPassword) {
                userDao.insertUser(user.copy(password = newPassword))
                withContext(Dispatchers.Main) {
                    showToast("Пароль успешно изменён")
                    oldPasswordInput.text?.clear()
                    newPasswordInput.text?.clear()
                    confirmPasswordInput.text?.clear()
                }
            } else {
                withContext(Dispatchers.Main) {
                    showToast("Неверный текущий пароль")
                }
            }
        }
    }

    private fun logoutUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = AppDatabase.getDatabase(requireContext()).userDao()
            userDao.logoutAllUsers()

            withContext(Dispatchers.Main) {
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
