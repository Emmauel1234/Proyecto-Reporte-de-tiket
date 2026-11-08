package com.example.apptiket.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptiket.data.ApiClient
import com.example.apptiket.data.ApiError
import com.example.apptiket.data.LoginRequest
import com.example.apptiket.data.Roles
import com.example.apptiket.data.SessionManager
import com.example.apptiket.data.UserDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val userName: String? = null
)

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val session = SessionManager(app)
    private val api = ApiClient.create(session)

    private val _state = MutableStateFlow(
        LoginUiState(isLoggedIn = session.isLoggedIn, userName = session.userName)
    )
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(employeeNumber: String, password: String) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(employeeNumber, password))
                val data = response.body()?.data

                if (response.isSuccessful && data != null) {
                    if (data.user.role.equals(Roles.TECNICO, ignoreCase = true)) {
                        session.save(data.accessToken, data.user)
                        _state.update {
                            LoginUiState(isLoggedIn = true, userName = data.user.name)
                        }
                    } else {
                        // No es técnico: se revoca el token recién creado y se bloquea el acceso
                        revokeToken(data.accessToken)
                        fail("Esta aplicación es solo para técnicos.")
                    }
                } else {
                    fail(parseError(response))
                }
            } catch (e: IOException) {
                fail("No se pudo conectar con el servidor. Revisa tu conexión.")
            } catch (e: Exception) {
                fail("Ocurrió un error inesperado.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { api.logout() } catch (_: Exception) { /* sin red: cerramos localmente */ }
            session.clear()
            _state.value = LoginUiState()
        }
    }

    private suspend fun revokeToken(token: String) {
        try {
            session.save(token, UserDto(0, "", "", ""))
            api.logout()
        } catch (_: Exception) {
        } finally {
            session.clear()
        }
    }

    private fun fail(message: String) {
        _state.update { it.copy(isLoading = false, errorMessage = message) }
    }

    private fun parseError(response: Response<*>): String {
        val apiMessage = try {
            Gson().fromJson(response.errorBody()?.string(), ApiError::class.java)?.message
        } catch (_: Exception) {
            null
        }
        return when (response.code()) {
            401 -> apiMessage ?: "Número de empleado o contraseña incorrectos."
            403 -> apiMessage ?: "La cuenta está inactiva. Contacta al administrador."
            422 -> apiMessage ?: "Revisa los datos ingresados."
            429 -> "Demasiados intentos. Espera un minuto e inténtalo de nuevo."
            in 500..599 -> "Error del servidor. Inténtalo más tarde."
            else -> apiMessage ?: "No se pudo iniciar sesión (código ${response.code()})."
        }
    }
}