package com.cegb03.archeryscore.data.repository

import android.util.Log
import com.cegb03.archeryscore.data.model.Tournament
import com.cegb03.archeryscore.data.model.InvitationBlock
import com.cegb03.archeryscore.data.model.InvitationContent
import com.cegb03.archeryscore.data.model.InvitationHtml
import com.cegb03.archeryscore.data.model.ParticipantRow
import com.cegb03.archeryscore.data.model.ParticipantSummaryItem
import com.cegb03.archeryscore.data.model.ParticipantsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TournamentRepository @Inject constructor() {
    
    private val baseUrl = "https://arquerosonline.com.ar/"
    
    suspend fun fetchTournaments(): Result<List<Tournament>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ArcheryScore_Debug", "🌐 TournamentRepository.fetchTournaments() - Iniciando descarga desde $baseUrl")
            val doc = Jsoup.connect(baseUrl).get()
            Log.d("ArcheryScore_Debug", "✅ TournamentRepository - HTML descargado exitosamente")
            val tournaments = mutableListOf<Tournament>()
            
            // Seleccionar todos los elementos de torneos (cada torneo está en div.col-md-6)
            val tournamentElements = doc.select("div.col-md-6")
            Log.d("ArcheryScore_Debug", "🔍 TournamentRepository - Encontrados ${tournamentElements.size} elementos de torneos")
            
            tournamentElements.forEach { element ->
                try {
                    // Extraer nombre del club (h6.mb-0)
                    val clubName = element.select("h6.mb-0").text()
                    
                    // Si no tiene nombre de club, no es un torneo válido
                    if (clubName.isEmpty()) return@forEach
                    
                    // Extraer fecha y modalidad del strong (ej: "15/02/2026 - Campo")
                    val dateAndModality = element.select("strong.text-primary").text()
                    val (date, modality) = parseDateAndModality(dateAndModality)
                    
                    // Extraer tipo y región del primer p.card-text.mb-auto (ej: "Homologatorio - Patagónica")
                    val typeAndRegion = element.select("p.card-text.mb-auto").firstOrNull()?.text() ?: ""
                    val (type, region) = parseTypeAndRegion(typeAndRegion)
                    
                    // Extraer estado de inscripción
                    val status = extractStatus(element)
                    
                    // Extraer participantes y cupo
                    val (participants, capacity) = extractParticipantsAndCapacity(element)
                    
                    // Extraer imagen del club (img dentro de div.col-2)
                    val imageUrl = element.select("img").attr("src")

                    // Extraer links de participantes e invitación
                    val participantsUrl = extractLink(element, "Participantes")
                    val invitationUrl = extractLink(element, "Invitación")
                    
                    // Verificar si está suspendido (badge con clase badge-danger)
                    val isSuspended = element.select(".badge-danger").text().contains("Suspendido")
                    
                    tournaments.add(
                        Tournament(
                            clubName = clubName,
                            tournamentType = type,
                            region = region,
                            date = date,
                            modality = modality,
                            status = status,
                            participants = participants,
                            capacity = capacity,
                            imageUrl = imageUrl.takeIf { it.isNotEmpty() },
                            participantsUrl = participantsUrl,
                            invitationUrl = invitationUrl,
                            isSuspended = isSuspended
                        )
                    )
                } catch (e: Exception) {
                    // Si falla parsear un torneo individual, continuar con el siguiente
                    Log.w("ArcheryScore_Debug", "⚠️ TournamentRepository - Error parseando un torneo: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            Log.d("ArcheryScore_Debug", "✅ TournamentRepository - fetchTournaments() completado: ${tournaments.size} torneos parseados")
            Result.success(tournaments)
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ ERROR en TournamentRepository.fetchTournaments(): ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun parseTypeAndRegion(text: String): Pair<String, String> {
        // Ejemplo: "Homologatorio - Patagónica"
        val parts = text.split(" - ")
        return if (parts.size >= 2) {
            Pair(parts[0].trim(), parts[1].trim())
        } else {
            Pair(text.trim(), "")
        }
    }
    
    private fun parseDateAndModality(text: String): Pair<String, String> {
        // Ejemplo: "21/02/2026 - Aire Libre"
        val parts = text.split(" - ")
        return if (parts.size >= 2) {
            Pair(parts[0].trim(), parts[1].trim())
        } else {
            Pair(text.trim(), "")
        }
    }
    
    private fun extractStatus(element: org.jsoup.nodes.Element): String {
        // Buscar primero en párrafos con clases específicas
        val dangerText = element.select("p.text-danger").text()
        if (dangerText.contains("Inscripciones Cerradas")) {
            return "Inscripciones Cerradas"
        }
        
        val successText = element.select("p.text-success").text()
        if (successText.contains("Cierre de Inscripción:")) {
            // Extraer la fecha completa (ej: "13/02 23:59")
            val pattern = "Cierre de Inscripción:\\s*(.+)".toRegex()
            val match = pattern.find(successText)
            return match?.groupValues?.getOrNull(1)?.trim() ?: "Inscripciones Abiertas"
        }
        
        val primaryText = element.select("p.text-primary").text()
        if (primaryText.contains("Apertura de Inscripción:")) {
            // Extraer la fecha completa (ej: "01/03 20:00")
            val pattern = "Apertura de Inscripción:\\s*(.+)".toRegex()
            val match = pattern.find(primaryText)
            return "Abre: ${match?.groupValues?.getOrNull(1)?.trim() ?: "Próximamente"}"
        }
        
        val allText = element.text()
        if (allText.contains("Sin información")) {
            return "Sin información"
        }
        
        return "Sin información"
    }
    
    private fun extractParticipantsAndCapacity(element: org.jsoup.nodes.Element): Pair<Int, Int> {
        // Extraer participantes del badge (span.badge.rounded-pill)
        val participantsBadge = element.select(".badge.rounded-pill").text()
        val participants = participantsBadge.toIntOrNull() ?: 0
        
        // Extraer cupo del texto que contiene "Cupo:"
        val text = element.text()
        val capacityPattern = "Cupo:\\s*(\\d+)".toRegex()
        val capacity = capacityPattern.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
        
        return Pair(participants, capacity)
    }

    private fun extractLink(element: org.jsoup.nodes.Element, label: String): String? {
        val link = element.select("a").firstOrNull {
            it.text().contains(label, ignoreCase = true)
        }?.attr("href").orEmpty()

        return link.takeIf { it.isNotEmpty() }?.let { normalizeUrl(it) }
    }

    private fun normalizeUrl(url: String): String {
        return if (url.startsWith("http")) {
            url
        } else {
            baseUrl.trimEnd('/') + "/" + url.trimStart('/')
        }
    }

    suspend fun fetchParticipants(url: String): Result<ParticipantsPage> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url).get()
            val clubName = extractParticipantsClubName(doc)
            val (byClub, totalCount) = extractSummaryTable(doc, "Denominación")
            val (byCategory, _) = extractSummaryTable(doc, "Categoría")
            val rows = extractParticipantsRows(doc)

            Result.success(
                ParticipantsPage(
                    clubName = clubName,
                    totalCount = totalCount,
                    byClub = byClub,
                    byCategory = byCategory,
                    rows = rows
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchInvitation(url: String): Result<InvitationContent> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val contentType = connection.contentType.orEmpty()
            val bytes = connection.inputStream.use { it.readBytes() }

            val isPdf = contentType.contains("pdf", ignoreCase = true) || url.endsWith(".pdf", true)
            if (isPdf) {
                Result.success(InvitationContent.Pdf(bytes))
            } else {
                val doc = Jsoup.parse(ByteArrayInputStream(bytes), "UTF-8", url)
                val html = parseInvitationHtml(doc)
                Result.success(InvitationContent.Html(html))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractParticipantsClubName(doc: Document): String {
        val titles = doc.select("table.table h5").map { it.text().trim() }
        return titles.firstOrNull { it.isNotEmpty() && !it.contains("LISTADO", true) }
            ?: "Inscriptos"
    }

    private fun extractSummaryTable(
        doc: Document,
        headerText: String
    ): Pair<List<ParticipantSummaryItem>, Int?> {
        val table = doc.select("table").firstOrNull { table ->
            table.select("th").any { it.text().contains(headerText, true) }
        } ?: return Pair(emptyList(), null)

        val items = mutableListOf<ParticipantSummaryItem>()
        var total: Int? = null

        table.select("tbody tr").forEach { row ->
            val cells = row.select("td")
            if (cells.size >= 2) {
                val label = cells[0].text().trim()
                val count = cells[1].text().trim().toIntOrNull() ?: 0
                if (label.contains("Cantidad de Inscriptos", true)) {
                    total = count
                } else if (label.isNotEmpty()) {
                    items.add(ParticipantSummaryItem(label, count))
                }
            }
        }

        return Pair(items, total)
    }

    private fun extractParticipantsRows(doc: Document): List<ParticipantRow> {
        val table = doc.select("table").firstOrNull { table ->
            val headers = table.select("th").map { it.text() }
            headers.any { it.contains("Arquero", true) } && headers.any { it.contains("DNI", true) }
        } ?: return emptyList()

        return table.select("tbody tr").mapNotNull { row ->
            val cells = row.select("td")
            if (cells.size < 7) return@mapNotNull null
            ParticipantRow(
                number = cells[0].text().trim(),
                dni = cells[1].text().trim(),
                name = cells[2].text().trim(),
                club = cells[3].text().trim(),
                birthDate = cells[4].text().trim(),
                category = cells[5].text().trim(),
                entryDate = cells[6].text().trim()
            )
        }
    }

    private fun parseInvitationHtml(doc: Document): InvitationHtml {
        val container = doc.selectFirst("div#app") ?: doc.body()
        val title = container.select("h1, h2, h3").firstOrNull()?.text().orEmpty()
        val blocks = mutableListOf<InvitationBlock>()

        container.select("h1, h2, h3, p, li, table").forEach { element ->
            when (element.tagName()) {
                "h1", "h2", "h3" -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty()) blocks.add(InvitationBlock.Heading(text))
                }
                "p", "li" -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty()) blocks.add(InvitationBlock.Paragraph(text))
                }
                "table" -> {
                    val rows = element.select("tr").map { row ->
                        row.select("th, td").map { it.text().trim() }
                            .filter { it.isNotEmpty() }
                    }.filter { it.isNotEmpty() }
                    if (rows.isNotEmpty()) blocks.add(InvitationBlock.Table(rows))
                }
            }
        }

        return InvitationHtml(
            title = title.ifEmpty { "Invitación" },
            blocks = blocks
        )
    }
}
