package com.example.apptiket.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.apptiket.data.DatosDemo
import com.example.apptiket.data.Estatus
import com.example.apptiket.data.FormularioTrabajo
import com.example.apptiket.data.Tecnico
import com.example.apptiket.data.Ticket
import com.example.apptiket.data.fechaHoraActual

/**
 * Estado de tickets de la sesión. Hoy usa datos de ejemplo en memoria.
 * Cuando tu API tenga endpoints de tickets, cada función de aquí es el lugar
 * donde llamar a Retrofit (listar asignados, cambiar estatus, reasignar, guardar formulario).
 */
class TicketsViewModel : ViewModel() {

    val tickets = mutableStateListOf<Ticket>().apply { addAll(DatosDemo.tickets()) }

    /** Integrantes del equipo a quienes se puede reasignar (RF-MAN-03). */
    val equipo: List<Tecnico> = DatosDemo.equipo

    /** Se llama al cerrar sesión para que otro técnico no vea datos del anterior. */
    fun reiniciar() {
        tickets.clear()
        tickets.addAll(DatosDemo.tickets())
    }

    /** RF-MAN-04: guarda estatus, checklist, notas y evidencias del ticket. */
    fun guardarAvance(ticket: Ticket) {
        val actualizado = if (ticket.estatus == Estatus.CERRADO && ticket.fechaCierre == null) {
            ticket.copy(fechaCierre = fechaHoraActual())
        } else {
            ticket
        }
        reemplazar(actualizado)
    }

    /** RF-MAN-03: deriva el ticket a otro integrante del equipo. */
    @Suppress("UNUSED_PARAMETER")
    fun reasignar(ticketId: Int, destino: Tecnico, motivo: String) {
        // TODO API: enviar también el motivo de la reasignación
        tickets.firstOrNull { it.id == ticketId }?.let {
            reemplazar(it.copy(asignadoId = destino.id))
        }
    }

    /** RF-MAN-05: guarda el formulario de cierre. */
    fun guardarFormulario(ticketId: Int, formulario: FormularioTrabajo) {
        tickets.firstOrNull { it.id == ticketId }?.let {
            reemplazar(it.copy(formulario = formulario))
        }
    }

    private fun reemplazar(ticket: Ticket) {
        val i = tickets.indexOfFirst { it.id == ticket.id }
        if (i >= 0) tickets[i] = ticket
    }
}