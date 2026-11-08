package com.example.apptiket.data

import android.content.Context
import androidx.core.content.edit

/** Guarda el token y los datos básicos del técnico para mantener la sesión abierta. */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("session", Context.MODE_PRIVATE)

    val token: String? get() = prefs.getString(KEY_TOKEN, null)
    val userName: String? get() = prefs.getString(KEY_NAME, null)
    val isLoggedIn: Boolean get() = !token.isNullOrBlank()

    fun save(token: String, user: UserDto) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putString(KEY_NAME, user.name)
            putString(KEY_EMPLOYEE, user.employeeNumber)
            putString(KEY_ROLE, user.role)
        }
    }

    fun clear() = prefs.edit { clear() }

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_NAME = "name"
        const val KEY_EMPLOYEE = "employee_number"
        const val KEY_ROLE = "role"
    }
}