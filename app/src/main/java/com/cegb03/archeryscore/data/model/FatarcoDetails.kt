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
