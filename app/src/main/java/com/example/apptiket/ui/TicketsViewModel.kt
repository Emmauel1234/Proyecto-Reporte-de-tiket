package com.example.apptiket.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptiket.data.ApiClient
import com.example.apptiket.data.ApiError
import com.example.apptiket.data.DatosDemo
import com.example.apptiket.data.Estatus
import com.example.apptiket.data.FormularioTrabajo
import com.example.apptiket.data.SessionManager
import com.example.apptiket.data.Tecnico
import com.example.apptiket.data.Ticket
import com.example.apptiket.data.TomarTicketRequest
import com.example.apptiket.data.fechaHoraActual
import com.example.apptiket.data.toTicket
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class TicketsViewModel(app: Application) : AndroidViewModel(app) {

    private val session = SessionManager(app)
    private val api = ApiClient.createTickets(session)

    val tickets = mutableStateListOf<Ticket>()
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    /** Integrantes del equipo a quienes se puede reasignar (RF-MAN-03). */
    val equipo: List<Tecnico> = DatosDemo.equipo

    init {
        cargarTickets()
    }

    fun cargarTickets() {
        if (!session.isLoggedIn) return
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = api.getTickets()
                if (response.isSuccessful) {
                    val lista = response.body()?.data?.data.orEmpty().map { it.toTicket() }
                    tickets.clear()
                    tickets.addAll(lista)
                } else {
                    errorMessage.value = extraerError(response)
                }
            } catch (e: Exception) {
                errorMessage.value = "Error de conexión al cargar tickets."
            } finally {
                isLoading.value = false
            }
        }
    }

    fun reiniciar() {
        tickets.clear()
        errorMessage.value = null
    }

    /** RF-MAN-04: toma el ticket (PENDIENTE -> EN_PROCESO) o lo cierra con evidencia (CERRADO). */
    fun guardarAvance(ticket: Ticket) {
        val anterior = tickets.firstOrNull { it.id == ticket.id }
        reemplazar(ticket)

        // 1. El técnico toma un ticket que estaba pendiente/en fallo
        if (anterior != null && anterior.estatus == Estatus.PENDIENTE && ticket.estatus == Estatus.EN_PROCESO) {
            viewModelScope.launch {
                try {
                    val comentario = ticket.notas.ifBlank { null }
                    val response = api.tomarTicket(ticket.id, TomarTicketRequest(comentario))
                    val actualizado = response.body()?.data?.toTicket()

                    if (response.isSuccessful && actualizado != null) {
                        reemplazar(
                            actualizado.copy(
                                checklist = ticket.checklist,
                                notas = ticket.notas,
                                evidencias = ticket.evidencias,
                                formulario = ticket.formulario
                            )
                        )
                    } else {
                        reemplazar(anterior)
                        errorMessage.value = extraerError(response)
                    }
                } catch (e: Exception) {
                    reemplazar(anterior)
                    errorMessage.value = "No se pudo tomar el ticket."
                }
            }
            return
        }

        // 2. El técnico finaliza el ticket (Laravel exige foto en el campo "evidencia")
        if (ticket.estatus == Estatus.CERRADO && anterior?.estatus != Estatus.CERRADO) {
            val bitmapEvidencia = ticket.evidencias.lastOrNull { it.imagen != null }?.imagen

            if (bitmapEvidencia == null) {
                reemplazar(ticket.copy(estatus = Estatus.EN_PROCESO))
                errorMessage.value = "Adjunta una foto de evidencia antes de cerrar el ticket."
                return
            }

            cerrarTicketEnApi(ticket, anterior, bitmapEvidencia)
        }
    }

    private fun cerrarTicketEnApi(ticket: Ticket, anterior: Ticket?, bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val archivo = bitmapAArchivo(bitmap)
                val reqFile = archivo.asRequestBody("image/jpeg".toMediaType())
                val evidenciaPart = MultipartBody.Part.createFormData("evidencia", archivo.name, reqFile)

                val methodPart = "PATCH".toRequestBody("text/plain".toMediaType())
                val estadoPart = "finalizado".toRequestBody("text/plain".toMediaType())
                val textoComentario = construirComentarioCierre(ticket)
                val comentarioPart = textoComentario?.toRequestBody("text/plain".toMediaType())

                val response = api.cambiarEstado(
                    id = ticket.id,
                    method = methodPart,
                    estado = estadoPart,
                    comentario = comentarioPart,
                    evidencia = evidenciaPart
                )

                archivo.delete()

                val ticketCerrado = response.body()?.data?.ticket?.toTicket()
                if (response.isSuccessful && ticketCerrado != null) {
                    reemplazar(
                        ticketCerrado.copy(
                            checklist = ticket.checklist,
                            notas = ticket.notas,
                            evidencias = ticket.evidencias,
                            formulario = ticket.formulario,
                            fechaCierre = ticketCerrado.fechaCierre ?: fechaHoraActual()
                        )
                    )
                } else {
                    if (anterior != null) reemplazar(anterior)
                    errorMessage.value = extraerError(response)
                }
            } catch (e: Exception) {
                if (anterior != null) reemplazar(anterior)
                errorMessage.value = "Error al subir la evidencia y cerrar el ticket."
            }
        }
    }

    /** RF-MAN-03: deriva el ticket a otro integrante del equipo. */
    @Suppress("UNUSED_PARAMETER")
    fun reasignar(ticketId: Int, destino: Tecnico, motivo: String) {
        tickets.firstOrNull { it.id == ticketId }?.let {
            reemplazar(it.copy(asignadoId = destino.id))
        }
    }

    /** RF-MAN-05: guarda el formulario de cierre. */
    fun guardarFormulario(ticketId: Int, formulario: FormularioTrabajo) {
        tickets.firstOrNull { it.id == ticketId }?.let { actual ->
            val conFormulario = actual.copy(formulario = formulario)
            reemplazar(conFormulario)

            val bitmap = conFormulario.evidencias.lastOrNull { it.imagen != null }?.imagen
            if (bitmap != null && conFormulario.estatus == Estatus.EN_PROCESO) {
                cerrarTicketEnApi(conFormulario.copy(estatus = Estatus.CERRADO), actual, bitmap)
            }
        }
    }

    private fun construirComentarioCierre(ticket: Ticket): String? {
        val partes = mutableListOf<String>()
        if (ticket.notas.isNotBlank()) partes.add("Notas: ${ticket.notas}")
        ticket.formulario?.let { f ->
            if (f.trabajoRealizado.isNotBlank()) partes.add("Trabajo: ${f.trabajoRealizado}")
            if (f.materiales.isNotBlank()) partes.add("Materiales: ${f.materiales}")
            if (f.observaciones.isNotBlank()) partes.add("Obs: ${f.observaciones}")
        }
        return partes.joinToString(" | ").ifBlank { null }
    }

    private fun bitmapAArchivo(bitmap: Bitmap): File {
        val file = File(getApplication<Application>().cacheDir, "evidencia_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file
    }

    private fun extraerError(response: Response<*>): String {
        return try {
            Gson().fromJson(response.errorBody()?.string(), ApiError::class.java)?.message
        } catch (_: Exception) {
            null
        } ?: "Error del servidor (${response.code()})"
    }

    private fun reemplazar(ticket: Ticket) {
        val i = tickets.indexOfFirst { it.id == ticket.id }
        if (i >= 0) tickets[i] = ticket
    }
}