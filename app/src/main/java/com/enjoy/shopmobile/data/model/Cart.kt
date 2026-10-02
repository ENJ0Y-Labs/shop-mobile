package com.enjoy.shopmobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Cart(
    val id: String,
    val items: List<CartItem>,
    val total: Long,
    @kotlinx.serialization.SerialName("item_count") val itemCount: Int
)

@Serializable
data class CartItem(
    val id: String,
    val product: CartProduct,
    val quantity: Int,
    val subtotal: Long
)

@Serializable
data class CartProduct(
    val id: String,
    val name: String,
    val price: Long,
    @kotlinx.serialization.SerialName("image_url") val imageUrl: String? = null,
    val category: String,
    val stock: Int,
    @kotlinx.serialization.SerialName("in_stock") val inStock: Boolean,
    val variants: JsonElement? = null
)
