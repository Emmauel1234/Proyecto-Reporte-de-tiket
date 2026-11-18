package com.example.apptiket.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptiket.data.Estatus
import com.example.apptiket.data.Evidencia
import com.example.apptiket.data.Ticket
import com.example.apptiket.data.horaActual
import com.example.apptiket.ui.theme.TiketColors

/**
 * RF-MAN-04: el técnico cambia el estatus del ticket en campo (Pendiente / En Proceso / Cerrado),
 * marca el checklist, escribe notas y adjunta evidencia fotográfica.
 * Desde aquí también se abre la reasignación (RF-MAN-03).
 */
@Composable
fun DetalleTicketScreen(
    ticket: Ticket,
    onVolver: () -> Unit,
    onReasignar: () -> Unit,
    onGuardar: (Ticket) -> Unit,
    modifier: Modifier = Modifier
) {
    var estatus by rememberSaveable(ticket.id) { mutableStateOf(ticket.estatus) }
    var hechos by rememberSaveable(ticket.id) { mutableStateOf(ticket.checklist.map { it.hecho }) }
    var notas by rememberSaveable(ticket.id) { mutableStateOf(ticket.notas) }
    var error by rememberSaveable(ticket.id) { mutableStateOf<String?>(null) }
    val evidencias = remember(ticket.id) {
        mutableStateListOf<Evidencia>().apply { addAll(ticket.evidencias) }
    }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) {
            evidencias.add(
                Evidencia(
                    id = System.currentTimeMillis(),
                    descripcion = "Evidencia ${evidencias.size + 1}",
                    hora = horaActual(),
                    imagen = foto
                )
            )
            error = null
        }
    }

    fun guardar() {
        val checklistFinal = ticket.checklist.mapIndexed { i, item ->
            item.copy(hecho = hechos.getOrElse(i) { item.hecho })
        }
        when {
            estatus == Estatus.CERRADO && checklistFinal.any { !it.hecho } -> {
                error = "Completa todo el checklist antes de cerrar el ticket."
            }
            ticket.estatus == Estatus.PENDIENTE && estatus == Estatus.CERRADO -> {
                error = "Primero cambia el estatus a En Proceso y guarda para tomar el ticket."
            }
            estatus == Estatus.CERRADO && evidencias.none { it.imagen != null } -> {
                error = "Adjunta al menos una foto de evidencia antes de cerrar el ticket."
            }
            else -> {
                error = null
                onGuardar(
                    ticket.copy(
                        estatus = estatus,
                        checklist = checklistFinal,
                        notas = notas.trim(),
                        evidencias = evidencias.toList()
                    )
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Barra de acciones: volver / reasignar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onVolver,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Volver", fontSize = 12.sp)
            }
            FilledTonalButton(
                onClick = onReasignar,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Reasignar Ticket", fontSize = 12.sp)
            }
        }

        // Información del ticket
        Tarjeta(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FolioChip(ticket.folio)
                    Spacer(Modifier.weight(1f))
                    PrioridadChip(ticket.prioridad)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    ticket.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TiketColors.TextMain
                )
                Spacer(Modifier.height(6.dp))
                Text(ticket.descripcion, fontSize = 13.sp, color = TiketColors.TextMuted)
                if (ticket.equipo.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TiketColors.Background)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Build,
                            contentDescription = null,
                            tint = TiketColors.Blue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(ticket.equipo, fontSize = 12.sp, color = TiketColors.TextMain)
                    }
                }
            }
        }

        // Estatus operativo (RF-MAN-04)
        Tarjeta(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "ESTATUS OPERATIVO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TiketColors.Blue
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Estatus.entries.forEach { opcion ->
                        val activo = estatus == opcion
                        val contenido = if (activo) Color.White else opcion.color()
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (activo) opcion.color() else opcion.colorSuave())
                                .clickable {
                                    estatus = opcion
                                    error = null
                                }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                opcion.icono(),
                                contentDescription = null,
                                tint = contenido,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                opcion.etiqueta,
                                color = contenido,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Checklist + notas + evidencia
        Tarjeta(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Checklist de Inspección",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${hechos.count { it }}/${ticket.checklist.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiketColors.TextMuted
                    )
                }

                ticket.checklist.forEachIndexed { i, item ->
                    val hecho = hechos.getOrElse(i) { item.hecho }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (hecho) TiketColors.GreenSoft else TiketColors.Background)
                            .border(
                                1.dp,
                                if (hecho) TiketColors.Green.copy(alpha = 0.3f) else TiketColors.Border,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                hechos = hechos.toMutableList().also { l ->
                                    if (i < l.size) l[i] = !hecho
                                }
                                error = null
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hecho,
                            onCheckedChange = null,
                            modifier = Modifier.padding(8.dp),
                            colors = CheckboxDefaults.colors(checkedColor = TiketColors.Green)
                        )
                        Text(
                            item.texto,
                            fontSize = 13.sp,
                            color = if (hecho) TiketColors.TextMuted else TiketColors.TextMain,
                            textDecoration = if (hecho) TextDecoration.LineThrough else null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                HorizontalDivider(color = TiketColors.Border)

                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    label = { Text("Notas técnicas de diagnóstico y solución") },
                    minLines = 3
                )

                HorizontalDivider(color = TiketColors.Border)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Evidencia Fotográfica (${evidencias.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { camara.launch(null) },
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Adjuntar Foto", fontSize = 12.sp)
                    }
                }

                if (evidencias.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        evidencias.forEach { ev ->
                            EvidenciaItem(ev = ev, onEliminar = { evidencias.remove(ev) })
                        }
                    }
                }
            }
        }

        if (error != null) {
            Text(
                error.orEmpty(),
                color = TiketColors.Red,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = { guardar() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TiketColors.Blue)
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Guardar Avance en Campo", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EvidenciaItem(ev: Evidencia, onEliminar: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TiketColors.Border, RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(TiketColors.Navy)
        ) {
            ev.imagen?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = ev.descripcion,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            IconButton(
                onClick = onEliminar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar foto",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(Modifier.padding(8.dp)) {
            Text(
                ev.descripcion,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(ev.hora, fontSize = 10.sp, color = TiketColors.TextMuted)
        }
    }
}