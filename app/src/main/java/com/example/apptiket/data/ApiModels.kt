package com.example.apptiket.data


import com.google.gson.annotations.SerializedName

/** Cuerpo de POST /api/auth/login */
data class LoginRequest(
    @SerializedName("employee_number") val employeeNumber: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String? = "app-tecnico"
)

/** Todas las respuestas de la API vienen con este formato: message / data / status */
data class ApiResponse<T>(
    val message: String?,
    val data: T?,
    val status: Int?
)

data class LoginData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String?,
    val user: UserDto
)

data class UserDto(
    val id: Int,
    @SerializedName("employee_number") val employeeNumber: String,
    val name: String,
    val role: String
)

/** Errores de Laravel: 422 trae "errors", 401/403 solo "message". */
data class ApiError(
    val message: String?,
    val errors: Map<String, List<String>>?
)

object Roles {
    // Verifica que sea exactamente el valor que guarda tu API en la columna "role"
    const val TECNICO = "tecnico"
}