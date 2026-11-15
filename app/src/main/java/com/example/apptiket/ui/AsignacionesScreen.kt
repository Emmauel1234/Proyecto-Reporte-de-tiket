package com.example.apptiket.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptiket.data.Estatus
import com.example.apptiket.data.Prioridad
import com.example.apptiket.data.Ticket
import com.example.apptiket.ui.theme.TiketColors


@Composable

fun AsignacionesScreen(
    tickets: List<Ticket>,
    onGestionar: (Ticket) -> Unit,
    modifier: Modifier = Modifier
) {
    var prioridad by rememberSaveable { mutableStateOf<Prioridad?>(null) }
    var estatus by rememberSaveable { mutableStateOf<Estatus?>(null) }

    val filtrados = tickets.filter {
        (prioridad == null || it.prioridad == prioridad) &&
                (estatus == null || it.estatus == estatus)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Mis Asignaciones",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiketColors.TextMain
                    )
                    Text(
                        "Órdenes programadas para la jornada actual",
                        fontSize = 12.sp,
                        color = TiketColors.TextMuted
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = TiketColors.BlueSoft) {
                    Text(
                        "${tickets.size} tareas",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = TiketColors.Blue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroFila(
                    etiqueta = "Prioridad:",
                    opciones = listOf<Prioridad?>(null) + Prioridad.entries,
                    seleccionada = prioridad,
                    nombre = { it?.etiqueta ?: "Todos" },
                    colorActivo = TiketColors.Navy,
                    onSeleccion = { prioridad = it }
                )
                FiltroFila(
                    etiqueta = "Estatus:",
                    opciones = listOf<Estatus?>(null, Estatus.PENDIENTE, Estatus.EN_PROCESO),
                    seleccionada = estatus,
                    nombre = { it?.etiqueta ?: "Todos" },
                    colorActivo = TiketColors.Blue,
                    onSeleccion = { estatus = it }
                )
            }
        }

        if (filtrados.isEmpty()) {
            item {
                EstadoVacio(
                    if (tickets.isEmpty()) "No tienes tickets asignados"
                    else "Ningún ticket coincide con los filtros"
                )
            }
        }

        items(filtrados, key = { it.id }) { ticket ->
            TicketCard(ticket = ticket, onGestionar = { onGestionar(ticket) })
        }
    }
}

@Composable
private fun TicketCard(ticket: Ticket, onGestionar: () -> Unit) {
    Tarjeta(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onGestionar)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(ticket.prioridad.color())
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FolioChip(ticket.folio)
                    Spacer(Modifier.weight(1f))
                    PrioridadChip(ticket.prioridad)
                    Spacer(Modifier.width(6.dp))
                    EstatusChip(ticket.estatus)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    ticket.titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TiketColors.TextMain
                )
                if (ticket.equipo.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Build,
                            contentDescription = null,
                            tint = TiketColors.TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            ticket.equipo,
                            fontSize = 12.sp,
                            color = TiketColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = TiketColors.Border)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = TiketColors.Blue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Programado: ${ticket.horaProgramada}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TiketColors.TextMain
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Gestionar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiketColors.Blue
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TiketColors.Blue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

