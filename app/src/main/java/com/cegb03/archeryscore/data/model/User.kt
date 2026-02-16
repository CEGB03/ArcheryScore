package com.cegb03.archeryscore.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val id: Int? = null,
    val username: String,
    val password: String? = null,
    val role: String = "client", // Valor predeterminado
    val email: String,
    val tel: String = ""
): Parcelable