package com.cegb03.archeryscore.data.model

data class Tournament(
    val clubName: String,
    val tournamentType: String,
    val region: String,
    val date: String,
    val modality: String, // Aire Libre, Campo, Sala, 3D
    val status: String, // Estado de inscripción
    val participants: Int,
    val capacity: Int,
    val imageUrl: String?,
    val participantsUrl: String?,
    val invitationUrl: String?,
    val isSuspended: Boolean = false
)
