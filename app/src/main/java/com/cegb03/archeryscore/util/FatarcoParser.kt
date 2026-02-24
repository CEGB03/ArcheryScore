package com.cegb03.archeryscore.util

import android.util.Log
import com.cegb03.archeryscore.data.model.FatarcoArcherData
import org.jsoup.Jsoup

/**
 * Parser para extraer datos de arqueros del HTML de FATARCO
 */
object FatarcoParser {

    /**
     * Parsea el HTML de la tabla de FATARCO y extrae los datos de arqueros
     * @param htmlContent HTML con las filas <tr>
     * @return Lista de arqueros encontrados
     */
    fun parseArchersTable(htmlContent: String): List<FatarcoArcherData> {
        val archers = mutableListOf<FatarcoArcherData>()
        
        try {
            Log.d("ArcheryScore_Debug", "🔍 HTML original length: ${htmlContent.length}")
            Log.d("ArcheryScore_Debug", "🔍 HTML sample: ${htmlContent.take(300)}")
            
            // Extraer todas las filas usando regex
            val trPattern = Regex("<tr[^>]*>.*?</tr>", RegexOption.DOT_MATCHES_ALL)
            val tdPattern = Regex("<td[^>]*>(.*?)</td>", RegexOption.DOT_MATCHES_ALL)
            
            val rowMatches = trPattern.findAll(htmlContent)
            Log.d("ArcheryScore_Debug", "📋 Filas encontradas por regex: ${rowMatches.count()}")
            
            if (rowMatches.count() == 0) {
                Log.w("ArcheryScore_Debug", "⚠️ No se encontraron filas <tr> con regex")
                return archers
            }
            
            for ((index, rowMatch) in rowMatches.withIndex()) {
                try {
                    val rowHtml = rowMatch.value
                    val cellMatches = tdPattern.findAll(rowHtml).map { it.groupValues[1].trim() }.toList()
                    
                    Log.d("ArcheryScore_Debug", "📄 Fila $index: ${cellMatches.size} celdas")
                    
                    if (cellMatches.size < 5) {
                        Log.d("ArcheryScore_Debug", "⏭️ Fila $index: saltando (${cellMatches.size} celdas)")
                        continue
                    }
                    
                    val dni = cellMatches[0].trim()
                    val nombre = cellMatches[1].trim()
                    val fechaNacimiento = cellMatches[2].trim()
                    val club = cellMatches[3].trim()
                    
                    // Extraer estados del botón (última celda contiene el botón)
                    val estadosCell = cellMatches[4]
                    val estados = extractEstados(estadosCell)
                    
                    Log.d("ArcheryScore_Debug", "✅ Fila $index: DNI=$dni, Nombre=$nombre, Estados=$estados")
                    
                    if (dni.isNotBlank() && nombre.isNotBlank()) {
                        archers.add(
                            FatarcoArcherData(
                                dni = dni,
                                nombre = nombre,
                                fechaNacimiento = fechaNacimiento,
                                club = club,
                                estados = estados
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ArcheryScore_Debug", "❌ Error parseando fila $index: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ Error parseando HTML: ${e.message}")
        }
        
        Log.d("ArcheryScore_Debug", "✅ Total arqueros parseados: ${archers.size}")
        return archers
    }
    
    /**
     * Extrae los estados/roles de los botones de colores
     * btn-success (verde) + letra A = arquero
     * btn-warning (amarillo) = entrenador  
     * btn-primary (azul) = juez
     * btn-info (celeste) = infantil
     * btn-secondary (gris) = escuela
     */
    private fun extractEstados(html: String): List<String> {
        val estados = mutableListOf<String>()
        
        // Arquero: btn-success con letra "A"
        if (html.contains("btn-success") && html.contains(">A<")) {
            estados.add("arquero")
        }
        
        // Entrenador: btn-warning
        if (html.contains("btn-warning")) {
            estados.add("entrenador")
        }
        
        // Juez: btn-primary
        if (html.contains("btn-primary")) {
            estados.add("juez")
        }
        
        // Infantil: btn-info
        if (html.contains("btn-info")) {
            estados.add("infantil")
        }
        
        // Escuela: btn-secondary
        if (html.contains("btn-secondary")) {
            estados.add("escuela")
        }
        
        return estados
    }
}
