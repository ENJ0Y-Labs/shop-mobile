package com.enjoy.shopmobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Long,
    @SerialName("image_url") val imageUrl: String? = null,
    val category: String,
    val stock: Int,
    @SerialName("in_stock") val inStock: Boolean,
    val variants: JsonElement? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)
