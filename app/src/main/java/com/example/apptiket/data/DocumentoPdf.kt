package com.example.apptiket.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File

/** RF-MAN-05: genera la documentación virtual (PDF) de un trabajo finalizado. */
object DocumentoPdf {

    /** Crea el PDF en el almacenamiento privado de la app y devuelve el archivo. */
    fun generar(context: Context, ticket: Ticket, tecnico: String): File {
        val carpeta = File(context.filesDir, "documentos").apply { mkdirs() }
        val archivo = File(carpeta, "${ticket.folio}.pdf")
        val doc = PdfDocument()
        try {
            val f = ticket.formulario ?: FormularioTrabajo()
            val w = PdfWriter(doc)

            w.titulo("Documento de cierre de servicio")
            w.subtitulo("Folio ${ticket.folio}")

            w.seccion("Datos del ticket")
            w.campo("Título", ticket.titulo)
            w.campo("Descripción", ticket.descripcion)
            w.campo("Equipo", ticket.equipo)
            w.campo("Prioridad", ticket.prioridad.etiqueta)
            w.campo("Estatus", ticket.estatus.etiqueta)
            w.campo("Técnico responsable", tecnico)
            w.campo("Fecha de cierre", ticket.fechaCierre ?: "—")

            w.seccion("Checklist de inspección")
            if (ticket.checklist.isEmpty()) {
                w.texto("Sin checklist")
            } else {
                ticket.checklist.forEach { w.texto((if (it.hecho) "[x] " else "[  ] ") + it.texto) }
            }

            w.seccion("Trabajo realizado")
            w.campo("Descripción del trabajo", f.trabajoRealizado)
            w.campo("Notas técnicas", ticket.notas)
            w.campo("Materiales / refacciones", f.materiales)
            w.campo("Tiempo invertido (horas)", f.horas)
            w.campo("Observaciones", f.observaciones)

            w.seccion("Conformidad")
            w.campo("Solicitante conforme", if (f.conformidad) "Sí" else "No")
            w.campo("Recibe", f.receptor)

            val fotos = ticket.evidencias.mapNotNull { it.imagen }
            if (fotos.isNotEmpty()) {
                w.seccion("Evidencia fotográfica")
                fotos.forEach { w.imagen(it) }
            }

            w.cerrar()
            archivo.outputStream().use { doc.writeTo(it) }
        } finally {
            doc.close()
        }
        return archivo
    }

    /** Abre el selector del sistema para compartir el PDF (WhatsApp, correo, Drive, etc.). */
    fun compartir(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val selector = Intent.createChooser(intent, "Compartir documento")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(selector)
    }
}

/** Escritor sencillo de texto e imágenes con salto de línea y de página automáticos. */
private class PdfWriter(private val doc: PdfDocument) {

    private val ancho = 595
    private val alto = 842
    private val margen = 40f

    private var numero = 1
    private var page = doc.startPage(PdfDocument.PageInfo.Builder(ancho, alto, numero).create())
    private var y = margen

    private val normal = Paint().apply {
        textSize = 11f
        color = Color.DKGRAY
        isAntiAlias = true
    }
    private val negrita = Paint(normal).apply {
        isFakeBoldText = true
        color = Color.BLACK
    }
    private val grande = Paint(negrita).apply { textSize = 20f }
    private val seccionPaint = Paint(negrita).apply {
        textSize = 13f
        color = Color.rgb(37, 99, 235)
    }
    private val linea = Paint().apply {
        color = Color.rgb(226, 232, 240)
        strokeWidth = 1f
    }

    private fun asegurar(espacio: Float) {
        if (y + espacio > alto - margen) {
            doc.finishPage(page)
            numero++
            page = doc.startPage(PdfDocument.PageInfo.Builder(ancho, alto, numero).create())
            y = margen
        }
    }

    fun titulo(texto: String) {
        asegurar(30f)
        page.canvas.drawText(texto, margen, y + 18f, grande)
        y += 28f
    }

    fun subtitulo(texto: String) {
        asegurar(20f)
        page.canvas.drawText(texto, margen, y + 11f, normal)
        y += 24f
    }

    fun seccion(texto: String) {
        asegurar(34f)
        y += 8f
        page.canvas.drawText(texto, margen, y + 13f, seccionPaint)
        y += 20f
        page.canvas.drawLine(margen, y, ancho - margen, y, linea)
        y += 8f
    }

    fun campo(etiqueta: String, valor: String) {
        asegurar(32f)
        page.canvas.drawText(etiqueta, margen, y + 11f, negrita)
        y += 16f
        texto(valor.ifBlank { "—" })
        y += 4f
    }

    fun texto(contenido: String) {
        lineas(contenido).forEach { l ->
            asegurar(15f)
            page.canvas.drawText(l, margen, y + 11f, normal)
            y += 15f
        }
    }

    fun imagen(bmp: Bitmap) {
        val maxAncho = 220f
        val maxAlto = 300f
        val escala = minOf(maxAncho / bmp.width, maxAlto / bmp.height)
        val w = bmp.width * escala
        val h = bmp.height * escala
        asegurar(h + 10f)
        page.canvas.drawBitmap(bmp, null, RectF(margen, y, margen + w, y + h), null)
        y += h + 10f
    }

    fun cerrar() {
        doc.finishPage(page)
    }

    private fun lineas(texto: String): List<String> {
        val disponible = ancho - 2 * margen
        val resultado = mutableListOf<String>()
        texto.split("\n").forEach { parrafo ->
            var resto = parrafo
            if (resto.isEmpty()) {
                resultado.add("")
                return@forEach
            }
            while (resto.isNotEmpty()) {
                var n = normal.breakText(resto, true, disponible, null)
                if (n < resto.length) {
                    val corte = resto.lastIndexOf(' ', n)
                    if (corte > 0) n = corte
                }
                n = maxOf(1, n)
                resultado.add(resto.substring(0, n).trimEnd())
                resto = resto.substring(n).trimStart()
            }
        }
        return resultado
    }
}