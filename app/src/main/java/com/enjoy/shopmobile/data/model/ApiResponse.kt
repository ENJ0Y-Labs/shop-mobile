package com.enjoy.shopmobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class UserResponse(val user: User)
@Serializable data class ProductResponse(val product: Product)

@Serializable
data class ProductListResponse(val products: List<Product>, val pagination: Pagination)

@Serializable
data class Pagination(
    val page: Int,
    @SerialName("per_page") val perPage: Int,
    val total: Int,
    val pages: Int,
    @SerialName("has_next") val hasNext: Boolean,
    @SerialName("has_previous") val hasPrevious: Boolean
)

@Serializable data class CartResponse(val cart: Cart)
@Serializable data class OrderResponse(val order: Order)
@Serializable data class OrderListResponse(val orders: List<Order>)
@Serializable data class HealthResponse(val status: String)

@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class RegisterRequest(val email: String, val password: String, val name: String)

@Serializable
data class AddCartItemRequest(
    @SerialName("product_id") val productId: String,
    val quantity: Int = 1
)

@Serializable data class UpdateCartItemRequest(val quantity: Int)

@Serializable
data class MergeCartItemRequest(
    @SerialName("product_id") val productId: String,
    val quantity: Int
)

@Serializable data class MergeCartRequest(val items: List<MergeCartItemRequest>)

@Serializable
data class CheckoutRequest(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String
)
