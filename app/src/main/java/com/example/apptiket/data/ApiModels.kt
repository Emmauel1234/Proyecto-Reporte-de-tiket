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
    const val TECNICO = "tecnico"
}

// --- Modelos de Tickets (Laravel -> AppTiket) ---

data class PaginatedData<T>(
    @SerializedName("current_page") val currentPage: Int?,
    val data: List<T> = emptyList()
)

data class EquipoDto(
    val id: Int,
    val codigo: String?,
    val nombre: String?,
    val ubicacion: String?,
    val estado: String?
)

data class TicketUserDto(
    val id: Int,
    @SerializedName("employee_number") val employeeNumber: String?,
    val name: String?
)

data class TicketDto(
    val id: Int,
    @SerializedName("numero_ticket") val numeroTicket: String,
    @SerializedName("equipo_id") val equipoId: Int,
    @SerializedName("tecnico_id") val tecnicoId: Int?,
    val titulo: String,
    val descripcion: String,
    val estado: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("fecha_cierre") val fechaCierre: String?,
    val equipo: EquipoDto?,
    val tecnico: TicketUserDto?
)

data class TomarTicketRequest(
    val comentario: String? = null
)

data class CambiarEstadoData(
    val ticket: TicketDto,
    @SerializedName("tiempo_resolucion_segundos") val tiempoResolucionSegundos: Int?
)

/** Convierte el JSON de Laravel al modelo Ticket que esperan tus pantallas de Compose */
fun TicketDto.toTicket(): Ticket {
    val nombreEquipo = listOfNotNull(equipo?.nombre, equipo?.ubicacion)
        .filter { it.isNotBlank() }
        .joinToString(" - ")
        .ifBlank { "Equipo #$equipoId" }

    val estatusMapeado = when (estado.lowercase()) {
        "fallo" -> Estatus.PENDIENTE
        "en_revision" -> Estatus.EN_PROCESO
        "finalizado", "inhabilitado", "cerrado" -> Estatus.CERRADO
        else -> Estatus.PENDIENTE
    }

    val fechaFormateada = createdAt
        ?.take(16)
        ?.replace("T", " ")
        ?: horaActual()

    return Ticket(
        id = id,
        folio = numeroTicket,
        titulo = titulo,
        descripcion = descripcion,
        equipo = nombreEquipo,
        prioridad = Prioridad.MEDIA,
        estatus = estatusMapeado,
        horaProgramada = fechaFormateada,
        asignadoId = tecnicoId ?: 0,
        fechaCierre = fechaCierre
    )
}