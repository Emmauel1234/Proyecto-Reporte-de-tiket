package com.example.apptiket.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptiket.data.Tecnico
import com.example.apptiket.data.Ticket
import com.example.apptiket.ui.theme.TiketColors

/** RF-MAN-03: derivar o asignar el ticket a un integrante específico del equipo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReasignarSheet(
    ticket: Ticket,
    equipo: List<Tecnico>,
    onDismiss: () -> Unit,
    onConfirmar: (destino: Tecnico, motivo: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var seleccionado by remember { mutableStateOf<Tecnico?>(null) }
    var motivo by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Reasignar ticket",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TiketColors.TextMain
            )
            Text(
                "#${ticket.folio} · ${ticket.titulo}",
                fontSize = 13.sp,
                color = TiketColors.TextMuted
            )

            Spacer(Modifier.height(4.dp))
            Text(
                "Selecciona al integrante del equipo",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            equipo.forEach { miembro ->
                val activo = seleccionado?.id == miembro.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activo) TiketColors.BlueSoft else Color.White)
                        .border(
                            1.dp,
                            if (activo) TiketColors.Blue else TiketColors.Border,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { seleccionado = miembro }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(TiketColors.Blue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            miembro.nombre.iniciales(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(miembro.nombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${miembro.cargo} · ${miembro.ticketsActivos} tickets activos",
                            fontSize = 12.sp,
                            color = TiketColors.TextMuted
                        )
                    }
                    RadioButton(selected = activo, onClick = null)
                }
            }

            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                label = { Text("Motivo de la reasignación") },
                minLines = 2
            )

            Button(
                onClick = { seleccionado?.let { onConfirmar(it, motivo.trim()) } },
                enabled = seleccionado != null && motivo.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TiketColors.Blue)
            ) {
                Text("Confirmar reasignación", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}