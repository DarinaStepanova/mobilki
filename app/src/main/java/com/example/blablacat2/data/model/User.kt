package com.example.blablacat2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val birthDate: String,
    val gender: String,
    val profilePhotoUri: String?,
    val licenseNumber: String,
    val licenseIssueDate: String,
    val passportPhotoUri: String?,
    val licensePhotoUri: String?,
    val isLoggedIn: Boolean = false
)
