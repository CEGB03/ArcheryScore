package com.cegb03.archeryscore.data.model

data class ParticipantSummaryItem(
    val label: String,
    val count: Int
)

data class ParticipantRow(
    val number: String,
    val dni: String,
    val name: String,
    val club: String,
    val birthDate: String,
    val category: String,
    val entryDate: String
)

data class ParticipantsPage(
    val clubName: String,
    val totalCount: Int?,
    val byClub: List<ParticipantSummaryItem>,
    val byCategory: List<ParticipantSummaryItem>,
    val rows: List<ParticipantRow>
)

sealed interface InvitationBlock {
    data class Heading(val text: String) : InvitationBlock
    data class Paragraph(val text: String) : InvitationBlock
    data class Table(val rows: List<List<String>>) : InvitationBlock
}

data class InvitationHtml(
    val title: String,
    val blocks: List<InvitationBlock>
)

sealed interface InvitationContent {
    data class Html(val data: InvitationHtml) : InvitationContent
    data class Pdf(val bytes: ByteArray) : InvitationContent
}

/**
 * Respuesta del endpoint AJAX de FATARCO
 */
data class FatarcoSearchResponse(
    val table_data: String,  // HTML con las filas de la tabla
    val total_data: Int      // Total de resultados
)

/**
 * Datos de un arquero extraídos del HTML de FATARCO
 */
data class FatarcoArcherData(
    val dni: String,
    val nombre: String,
    val fechaNacimiento: String,
    val club: String,
    val estados: List<String>  // Lista de estados: "arquero", "entrenador", "juez", "infantil", "escuela"
)

/**
 * Resultado de la verificación FATARCO
 */
sealed class FatarcoVerificationResult {
    data class Success(val data: FatarcoArcherData) : FatarcoVerificationResult()
    object NotFound : FatarcoVerificationResult()
    data class Error(val message: String) : FatarcoVerificationResult()
}
