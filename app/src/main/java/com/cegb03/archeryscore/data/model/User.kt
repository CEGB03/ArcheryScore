package com.cegb03.archeryscore.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val id: Int? = null,
    val username: String,
    val password: String? = null,
    val email: String,
    val tel: String = "",
    val documento: String? = null,
    val club: String? = null,
    val fechaNacimiento: String? = null,
    val roles: List<String> = listOf("arquero")
): Parcelable