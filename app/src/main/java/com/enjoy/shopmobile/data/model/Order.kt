package com.enjoy.shopmobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    @SerialName("order_number") val orderNumber: String,
    val status: String,
    @SerialName("total_amount") val totalAmount: Long,
    val customer: OrderCustomer,
    @SerialName("created_at") val createdAt: String,
    val items: List<OrderItem>
)

@Serializable
data class OrderCustomer(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String
)

@Serializable
data class OrderItem(
    val id: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_name") val productName: String,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Long,
    val subtotal: Long
)
