package com.enjoy.shopmobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    @SerialName("profile_picture_url") val profilePictureUrl: String? = null
)
