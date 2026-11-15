package com.example.apptiket.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptiket.data.Estatus
import com.example.apptiket.data.Prioridad
import com.example.apptiket.data.Tecnico
import com.example.apptiket.ui.theme.TiketColors

// ---------- Colores / iconos por prioridad y estatus ----------

fun Prioridad.color(): Color = when (this) {
    Prioridad.ALTA -> TiketColors.Red
    Prioridad.MEDIA -> TiketColors.Amber
    Prioridad.BAJA -> TiketColors.Green
}

fun Prioridad.colorSuave(): Color = when (this) {
    Prioridad.ALTA -> TiketColors.RedSoft
    Prioridad.MEDIA -> TiketColors.AmberSoft
    Prioridad.BAJA -> TiketColors.GreenSoft
}

fun Estatus.color(): Color = when (this) {
    Estatus.PENDIENTE -> TiketColors.Slate
    Estatus.EN_PROCESO -> TiketColors.Blue
    Estatus.CERRADO -> TiketColors.Green
}

fun Estatus.colorSuave(): Color = when (this) {
    Estatus.PENDIENTE -> TiketColors.SlateSoft
    Estatus.EN_PROCESO -> TiketColors.BlueSoft
    Estatus.CERRADO -> TiketColors.GreenSoft
}

fun Estatus.icono(): ImageVector = when (this) {
    Estatus.PENDIENTE -> Icons.Filled.DateRange
    Estatus.EN_PROCESO -> Icons.Filled.PlayArrow
    Estatus.CERRADO -> Icons.Filled.CheckCircle
}

/** "Ing. Carlos Mendoza" -> "CM" */
fun String.iniciales(): String =
    split(" ")
        .filter { it.isNotBlank() && !it.endsWith(".") }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

// ---------- Componentes reutilizables ----------

@Composable
fun EtiquetaChip(texto: String, color: Color, fondo: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(50), color = fondo, modifier = modifier) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).background(color, CircleShape))
            Spacer(Modifier.width(5.dp))
            Text(texto, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PrioridadChip(prioridad: Prioridad) =
    EtiquetaChip(prioridad.etiqueta, prioridad.color(), prioridad.colorSuave())

@Composable
fun EstatusChip(estatus: Estatus) =
    EtiquetaChip(estatus.etiqueta, estatus.color(), estatus.colorSuave())

@Composable
fun FolioChip(folio: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = TiketColors.BlueSoft) {
        Text(
            text = "#$folio",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = TiketColors.Blue,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun Tarjeta(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TiketColors.Border),
        content = content
    )
}

@Composable
fun EncabezadoTecnico(tecnico: Tecnico, onSalir: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TiketColors.Navy)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(TiketColors.Blue, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                tecnico.nombre.iniciales(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                tecnico.nombre,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1
            )
            Text(
                "${tecnico.codigo} • ${tecnico.cargo}",
                color = TiketColors.BlueLight,
                fontSize = 12.sp
            )
        }
        TextButton(onClick = onSalir) {
            Text("Salir", color = TiketColors.BlueLight, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Fila de filtros tipo "pill" con etiqueta a la izquierda. */
@Composable
fun <T> FiltroFila(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T,
    nombre: (T) -> String,
    colorActivo: Color,
    onSeleccion: (T) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            etiqueta,
            modifier = Modifier.width(72.dp),
            fontSize = 12.sp,
            color = TiketColors.TextMuted
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            opciones.forEach { opcion ->
                val activa = opcion == seleccionada
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSeleccion(opcion) },
                    shape = RoundedCornerShape(50),
                    color = if (activa) colorActivo else Color.White,
                    border = if (activa) null else BorderStroke(1.dp, TiketColors.Border)
                ) {
                    Text(
                        nombre(opcion),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (activa) Color.White else TiketColors.TextMain
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoVacio(
    mensaje: String,
    modifier: Modifier = Modifier,
    textoBoton: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(mensaje, color = TiketColors.TextMuted, textAlign = TextAlign.Center)
        if (textoBoton != null && onClick != null) {
            Button(onClick = onClick) { Text(textoBoton) }
        }
    }
}