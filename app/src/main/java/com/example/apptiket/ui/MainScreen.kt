    package com.example.apptiket.ui

    import android.widget.Toast
    import androidx.activity.compose.BackHandler
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.List
    import androidx.compose.material.icons.filled.Build
    import androidx.compose.material.icons.filled.CheckCircle
    import androidx.compose.material3.Badge
    import androidx.compose.material3.BadgedBox
    import androidx.compose.material3.Icon
    import androidx.compose.material3.NavigationBar
    import androidx.compose.material3.NavigationBarItem
    import androidx.compose.material3.NavigationBarItemDefaults
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.saveable.rememberSaveable
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.platform.LocalContext
    import com.example.apptiket.data.DatosDemo
    import com.example.apptiket.data.DocumentoPdf
    import com.example.apptiket.data.Estatus
    import com.example.apptiket.data.Tecnico
    import com.example.apptiket.data.Ticket
    import com.example.apptiket.ui.theme.TiketColors
    import com.example.apptiket.data.SessionManager

    enum class Pestana(val titulo: String, val icono: ImageVector) {
        ASIGNACIONES("Asignaciones", Icons.AutoMirrored.Filled.List),
        EN_CAMPO("En Campo", Icons.Filled.Build),
        FINALIZADAS("Finalizadas", Icons.Filled.CheckCircle)
    }

    /**
     * Pantalla principal tras el login: encabezado del técnico + 3 pestañas
     *  - Asignaciones (RF-MAN-02)
     *  - En Campo: detalle, estatus y reasignación (RF-MAN-03 y RF-MAN-04)
     *  - Finalizadas: consulta y documentación (RF-MAN-05)
     */
    @Composable
    fun MainScreen(
        userName: String,
        employeeNumber: String?,
        viewModel: TicketsViewModel,
        onLogout: () -> Unit
    ) {
        val context = LocalContext.current

        var pestana by rememberSaveable { mutableStateOf(Pestana.ASIGNACIONES) }
        var seleccionadoId by rememberSaveable { mutableStateOf<Int?>(null) }
        var documentandoId by rememberSaveable { mutableStateOf<Int?>(null) }
        var reasignandoId by rememberSaveable { mutableStateOf<Int?>(null) }

        val session = remember { SessionManager(context) }

        val yo = remember(userName, employeeNumber) {
            Tecnico(
                id = session.userId,
                nombre = userName,
                codigo = employeeNumber ?: "—",
                cargo = "Soporte en Campo"
            )
        }

        // Muestra tanto los tickets asignados a este técnico como los pendientes sin asignar (0)
        val misTickets = viewModel.tickets.filter { it.asignadoId == yo.id || it.asignadoId == 0 }
        val abiertos = misTickets.filter { it.estatus != Estatus.CERRADO }
        val finalizados = misTickets.filter { it.estatus == Estatus.CERRADO }
        val enProceso = abiertos.count { it.estatus == Estatus.EN_PROCESO }

        fun aviso(mensaje: String) =
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

        fun generarPdf(ticket: Ticket, compartir: Boolean) {
            try {
                val archivo = DocumentoPdf.generar(context, ticket, userName)
                if (compartir) {
                    DocumentoPdf.compartir(context, archivo)
                } else {
                    aviso("Documento generado: ${archivo.name}")
                }
            } catch (e: Exception) {
                aviso("No se pudo generar el documento.")
            }
        }

        BackHandler(enabled = documentandoId != null || pestana != Pestana.ASIGNACIONES) {
            if (documentandoId != null) documentandoId = null else pestana = Pestana.ASIGNACIONES
        }

        Scaffold(
            containerColor = TiketColors.Background,
            topBar = { EncabezadoTecnico(tecnico = yo, onSalir = onLogout) },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    Pestana.entries.forEach { p ->
                        val cantidad = when (p) {
                            Pestana.ASIGNACIONES -> abiertos.size
                            Pestana.EN_CAMPO -> enProceso
                            Pestana.FINALIZADAS -> finalizados.size
                        }
                        NavigationBarItem(
                            selected = pestana == p,
                            onClick = { pestana = p },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (cantidad > 0) {
                                            Badge(containerColor = TiketColors.Blue) {
                                                Text("$cantidad")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(p.icono, contentDescription = p.titulo)
                                }
                            },
                            label = { Text(p.titulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TiketColors.Blue,
                                selectedTextColor = TiketColors.Blue,
                                indicatorColor = TiketColors.BlueSoft,
                                unselectedIconColor = TiketColors.TextMuted,
                                unselectedTextColor = TiketColors.TextMuted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (pestana) {
                    Pestana.ASIGNACIONES -> AsignacionesScreen(
                        tickets = abiertos,
                        onGestionar = { ticket ->
                            seleccionadoId = ticket.id
                            pestana = Pestana.EN_CAMPO
                        }
                    )

                    Pestana.EN_CAMPO -> {
                        val ticket = abiertos.firstOrNull { it.id == seleccionadoId }
                        if (ticket == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                EstadoVacio(
                                    mensaje = "Selecciona un ticket en Asignaciones para gestionarlo.",
                                    textoBoton = "Ir a Asignaciones",
                                    onClick = { pestana = Pestana.ASIGNACIONES }
                                )
                            }
                        } else {
                            DetalleTicketScreen(
                                ticket = ticket,
                                onVolver = { pestana = Pestana.ASIGNACIONES },
                                onReasignar = { reasignandoId = ticket.id },
                                onGuardar = { actualizado ->
                                    viewModel.guardarAvance(actualizado)
                                    if (actualizado.estatus == Estatus.CERRADO) {
                                        seleccionadoId = null
                                        documentandoId = actualizado.id
                                        pestana = Pestana.FINALIZADAS
                                        aviso("Ticket cerrado. Completa la documentación.")
                                    } else {
                                        aviso("Avance guardado")
                                    }
                                }
                            )
                        }
                    }

                    Pestana.FINALIZADAS -> {
                        val ticket = finalizados.firstOrNull { it.id == documentandoId }
                        if (ticket == null) {
                            FinalizadosScreen(
                                tickets = finalizados,
                                onVerFormulario = { documentandoId = it.id },
                                onGenerarDocumento = { generarPdf(it, compartir = true) }
                            )
                        } else {
                            DocumentacionScreen(
                                ticket = ticket,
                                tecnico = userName,
                                onVolver = { documentandoId = null },
                                onGuardar = { formulario ->
                                    viewModel.guardarFormulario(ticket.id, formulario)
                                    aviso("Formulario guardado")
                                },
                                onGenerarPdf = { formulario ->
                                    viewModel.guardarFormulario(ticket.id, formulario)
                                    generarPdf(ticket.copy(formulario = formulario), compartir = false)
                                },
                                onCompartir = { formulario ->
                                    viewModel.guardarFormulario(ticket.id, formulario)
                                    generarPdf(ticket.copy(formulario = formulario), compartir = true)
                                }
                            )
                        }
                    }
                }
            }
        }

        // RF-MAN-03: hoja de reasignación
        val ticketReasignando = viewModel.tickets.firstOrNull { it.id == reasignandoId }
        if (ticketReasignando != null) {
            val miembros = viewModel.equipo.map { miembro ->
                miembro.copy(
                    ticketsActivos = viewModel.tickets.count {
                        it.asignadoId == miembro.id && it.estatus != Estatus.CERRADO
                    }
                )
            }
            ReasignarSheet(
                ticket = ticketReasignando,
                equipo = miembros,
                onDismiss = { reasignandoId = null },
                onConfirmar = { destino, motivo ->
                    viewModel.reasignar(ticketReasignando.id, destino, motivo)
                    reasignandoId = null
                    seleccionadoId = null
                    pestana = Pestana.ASIGNACIONES
                    aviso("Ticket reasignado a ${destino.nombre}")
                }
            )
        }
    }

