package com.example.apptiket.data

import android.graphics.Bitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Prioridad(val etiqueta: String) {
    ALTA("Alta"),
    MEDIA("Media"),
    BAJA("Baja")
}

/** RF-MAN-04: estatus que el técnico puede asignar en campo. */
enum class Estatus(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En Proceso"),
    CERRADO("Cerrado")
}

data class ChecklistItem(val texto: String, val hecho: Boolean = false)

data class Evidencia(
    val id: Long,
    val descripcion: String,
    val hora: String,
    val imagen: Bitmap? = null
)

/** Integrante del equipo (RF-MAN-03: destino de una reasignación). */
data class Tecnico(
    val id: Int,
    val nombre: String,
    val codigo: String,
    val cargo: String,
    val ticketsActivos: Int = 0
)

/** RF-MAN-05: formulario de cierre / documentación del trabajo. */
data class FormularioTrabajo(
    val trabajoRealizado: String = "",
    val materiales: String = "",
    val horas: String = "",
    val observaciones: String = "",
    val conformidad: Boolean = false,
    val receptor: String = ""
)

data class Ticket(
    val id: Int,
    val folio: String,
    val titulo: String,
    val descripcion: String,
    val equipo: String,
    val prioridad: Prioridad,
    val estatus: Estatus,
    val horaProgramada: String,
    val asignadoId: Int,
    val checklist: List<ChecklistItem> = emptyList(),
    val notas: String = "",
    val evidencias: List<Evidencia> = emptyList(),
    val fechaCierre: String? = null,
    val formulario: FormularioTrabajo? = null
)

fun horaActual(): String =
    SimpleDateFormat("hh:mm a", Locale("es", "MX")).format(Date())

fun fechaHoraActual(): String =
    SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale("es", "MX")).format(Date())