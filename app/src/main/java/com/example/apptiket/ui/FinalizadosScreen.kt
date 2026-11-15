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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptiket.data.Ticket
import com.example.apptiket.ui.theme.TiketColors

/** RF-MAN-05: consulta de trabajos finalizados, con acceso a su formulario y documento. */
@Composable
fun FinalizadosScreen(
    tickets: List<Ticket>,
    onVerFormulario: (Ticket) -> Unit,
    onGenerarDocumento: (Ticket) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    "Trabajos Finalizados",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TiketColors.TextMain
                )
                Text(
                    "Consulta formularios y genera documentación",
                    fontSize = 12.sp,
                    color = TiketColors.TextMuted
                )
            }
        }

        if (tickets.isEmpty()) {
            item { EstadoVacio("Aún no hay trabajos finalizados") }
        }

        items(tickets, key = { it.id }) { ticket ->
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiketColors.TextMain
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Finalizado: ${ticket.fechaCierre ?: "—"}",
                        fontSize = 12.sp,
                        color = TiketColors.TextMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onVerFormulario(ticket) }) {
                            Text("Ver formulario")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = { onGenerarDocumento(ticket) },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Documento PDF")
                        }
                    }
                }
            }
        }
    }
}