package com.example.apptiket.data

/**
 * Datos de ejemplo. Tu API (AuthApi) solo tiene login/me/logout, así que mientras no existan
 * endpoints de tickets, la app trabaja con estos datos en memoria.
 * Cuando tengas la API, reemplaza el contenido de TicketsViewModel por llamadas Retrofit.
 */
object DatosDemo {

    /** Id del técnico que inició sesión (los tickets demo están asignados a este id). */
    const val YO_ID = 1

    /** Otros integrantes del equipo, destino posible de una reasignación. */
    val equipo = listOf(
        Tecnico(2, "Ing. Laura Ríos", "EMP-102", "Soporte en Campo"),
        Tecnico(3, "Téc. Miguel Torres", "EMP-103", "Mantenimiento"),
        Tecnico(4, "Ing. Ana Beltrán", "EMP-104", "Soporte en Campo")
    )

    private fun checklistBase(hechos: Int = 0) = listOf(
        "Inspección visual y bloqueo de seguridad (LOTO)",
        "Medición de amperaje y voltaje de alimentación",
        "Limpieza / calibración de componentes críticos",
        "Prueba operativa bajo carga nominal"
    ).mapIndexed { i, t -> ChecklistItem(t, i < hechos) }

    fun tickets(): List<Ticket> = listOf(
        Ticket(
            id = 1,
            folio = "TK-2041",
            titulo = "Falla en Unidad Manejadora de Aire (UMA-03)",
            descripcion = "Alarma por baja presión de retorno y variación térmica en sala de " +
                    "recuperación. Requiere revisión de filtros HEPA y actuador de compuerta.",
            equipo = "Carrier 30XA-120 / Sensor Presión Diferencial",
            prioridad = Prioridad.ALTA,
            estatus = Estatus.EN_PROCESO,
            horaProgramada = "09:30 AM",
            asignadoId = YO_ID,
            checklist = checklistBase(hechos = 2),
            notas = "Se detectó obstrucción parcial en pre-filtro y descalibración en actuador " +
                    "Belimo de 24V. Procediendo con limpieza y ajuste de torque."
        ),
        Ticket(
            id = 2,
            folio = "TK-2042",
            titulo = "Mantenimiento Preventivo Servidor Rack B2",
            descripcion = "Mantenimiento trimestral: limpieza de ventiladores, revisión de " +
                    "temperaturas y verificación de respaldos de energía.",
            equipo = "Rack B2 / Servidor Dell R740",
            prioridad = Prioridad.MEDIA,
            estatus = Estatus.PENDIENTE,
            horaProgramada = "11:45 AM",
            asignadoId = YO_ID,
            checklist = checklistBase()
        ),
        Ticket(
            id = 3,
            folio = "TK-2043",
            titulo = "Calibración Sensores Temperatura Cuarto Frío",
            descripcion = "Los sensores reportan desviaciones mayores a 1.5 °C respecto al " +
                    "termómetro patrón. Calibrar y registrar valores.",
            equipo = "Sensores PT100 / Controlador CF-02",
            prioridad = Prioridad.ALTA,
            estatus = Estatus.PENDIENTE,
            horaProgramada = "01:15 PM",
            asignadoId = YO_ID,
            checklist = checklistBase()
        ),
        Ticket(
            id = 4,
            folio = "TK-2044",
            titulo = "Revisión de UPS y banco de baterías",
            descripcion = "Revisión mensual de carga, voltaje por celda y alarmas del UPS.",
            equipo = "UPS APC Smart-UPS 10kVA",
            prioridad = Prioridad.BAJA,
            estatus = Estatus.PENDIENTE,
            horaProgramada = "03:30 PM",
            asignadoId = YO_ID,
            checklist = checklistBase()
        ),
        // Asignados a otros integrantes (cuentan como "tickets activos" en la reasignación)
        Ticket(
            id = 7,
            folio = "TK-2045",
            titulo = "Cambio de lámparas en pasillo técnico",
            descripcion = "Reemplazo de luminarias fundidas.",
            equipo = "Luminarias LED 40W",
            prioridad = Prioridad.BAJA,
            estatus = Estatus.PENDIENTE,
            horaProgramada = "10:00 AM",
            asignadoId = 2,
            checklist = checklistBase()
        ),
        Ticket(
            id = 8,
            folio = "TK-2046",
            titulo = "Ajuste de presión en bomba de agua helada",
            descripcion = "Presión fluctuante en la descarga.",
            equipo = "Bomba B-01",
            prioridad = Prioridad.MEDIA,
            estatus = Estatus.EN_PROCESO,
            horaProgramada = "12:00 PM",
            asignadoId = 3,
            checklist = checklistBase(hechos = 1)
        ),
        // Trabajos finalizados del técnico (RF-MAN-05)
        Ticket(
            id = 5,
            folio = "TK-2038",
            titulo = "Cambio de contactor en bomba de recirculación",
            descripcion = "Contactor con contactos quemados; la bomba no arrancaba.",
            equipo = "Bomba B-02 / Contactor 40A",
            prioridad = Prioridad.MEDIA,
            estatus = Estatus.CERRADO,
            horaProgramada = "08:00 AM",
            asignadoId = YO_ID,
            checklist = checklistBase(hechos = 4),
            notas = "Se reemplazó contactor y se apretaron bornes. Arranque normal.",
            fechaCierre = "13/11/2026 04:40 PM",
            formulario = FormularioTrabajo(
                trabajoRealizado = "Reemplazo de contactor de 40A y reapriete de bornes.",
                materiales = "Contactor 40A (1 pza), terminales (4 pzas)",
                horas = "1.5",
                observaciones = "Se recomienda revisar el térmico en el próximo mantenimiento.",
                conformidad = true,
                receptor = "Roberto Salas"
            )
        ),
        Ticket(
            id = 6,
            folio = "TK-2039",
            titulo = "Reemplazo de filtros en manejadora UMA-01",
            descripcion = "Filtros saturados por mantenimiento vencido.",
            equipo = "Manejadora UMA-01",
            prioridad = Prioridad.BAJA,
            estatus = Estatus.CERRADO,
            horaProgramada = "09:00 AM",
            asignadoId = YO_ID,
            checklist = checklistBase(hechos = 4),
            notas = "Filtros reemplazados; presión diferencial normalizada.",
            fechaCierre = "14/11/2026 11:20 AM",
            formulario = FormularioTrabajo(
                trabajoRealizado = "Cambio de 6 filtros G4 y 6 filtros F7.",
                materiales = "Filtros G4 (6), filtros F7 (6)",
                horas = "2",
                observaciones = "",
                conformidad = true,
                receptor = "María López"
            )
        )
    )
}