package com.example.apptiket.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptiket.data.FormularioTrabajo
import com.example.apptiket.data.Ticket
import com.example.apptiket.ui.theme.TiketColors

/**
 * RF-MAN-05: formulario de estatus / cierre del trabajo y generación de la documentación
 * virtual (PDF) para guardar o compartir.
 */
@Composable
fun DocumentacionScreen(
    ticket: Ticket,
    tecnico: String,
    onVolver: () -> Unit,
    onGuardar: (FormularioTrabajo) -> Unit,
    onGenerarPdf: (FormularioTrabajo) -> Unit,
    onCompartir: (FormularioTrabajo) -> Unit,
    modifier: Modifier = Modifier
) {
    val inicial = ticket.formulario ?: FormularioTrabajo(trabajoRealizado = ticket.notas)

    var trabajo by rememberSaveable(ticket.id) { mutableStateOf(inicial.trabajoRealizado) }
    var materiales by rememberSaveable(ticket.id) { mutableStateOf(inicial.materiales) }
    var horas by rememberSaveable(ticket.id) { mutableStateOf(inicial.horas) }
    var observaciones by rememberSaveable(ticket.id) { mutableStateOf(inicial.observaciones) }
    var conformidad by rememberSaveable(ticket.id) { mutableStateOf(inicial.conformidad) }
    var receptor by rememberSaveable(ticket.id) { mutableStateOf(inicial.receptor) }

    fun formulario() = FormularioTrabajo(
        trabajoRealizado = trabajo.trim(),
        materiales = materiales.trim(),
        horas = horas.trim(),
        observaciones = observaciones.trim(),
        conformidad = conformidad,
        receptor = receptor.trim()
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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

        Tarjeta(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FolioChip(ticket.folio)
                    Spacer(Modifier.weight(1f))
                    EstatusChip(ticket.estatus)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    ticket.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TiketColors.TextMain
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Técnico: $tecnico · Cierre: ${ticket.fechaCierre ?: "—"}",
                    fontSize = 12.sp,
                    color = TiketColors.TextMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Checklist: ${ticket.checklist.count { it.hecho }}/${ticket.checklist.size} · " +
                            "Fotos adjuntas: ${ticket.evidencias.size}",
                    fontSize = 12.sp,
                    color = TiketColors.TextMuted
                )
            }
        }

        Text(
            "Formulario de cierre",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TiketColors.TextMain
        )

        OutlinedTextField(
            value = trabajo,
            onValueChange = { trabajo = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            label = { Text("Trabajo realizado") },
            minLines = 3
        )
        OutlinedTextField(
            value = materiales,
            onValueChange = { materiales = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            label = { Text("Materiales / refacciones utilizados") },
            minLines = 2
        )
        OutlinedTextField(
            value = horas,
            onValueChange = { horas = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            label = { Text("Tiempo invertido (horas)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            label = { Text("Observaciones") },
            minLines = 2
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = conformidad, onCheckedChange = { conformidad = it })
            Text("El solicitante está conforme con el trabajo", fontSize = 13.sp)
        }

        OutlinedTextField(
            value = receptor,
            onValueChange = { receptor = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            label = { Text("Nombre de quien recibe") },
            singleLine = true
        )

        Button(
            onClick = { onGuardar(formulario()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TiketColors.Blue)
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Guardar formulario", fontWeight = FontWeight.Bold)
        }

        FilledTonalButton(
            onClick = { onGenerarPdf(formulario()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Generar documento (PDF)", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = { onCompartir(formulario()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Compartir", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))
    }
}