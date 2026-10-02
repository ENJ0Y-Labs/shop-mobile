package com.enjoy.shopmobile.data.api

import com.enjoy.shopmobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("health") suspend fun health(): Response<HealthResponse>
    @POST("auth/register") suspend fun register(@Body request: RegisterRequest): Response<UserResponse>
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): Response<UserResponse>
    @POST("auth/logout") suspend fun logout(): Response<Unit>
    @GET("auth/me") suspend fun me(): Response<UserResponse>

    @GET("products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("sort") sort: String = "name",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 12
    ): Response<ProductListResponse>

    @GET("products/{id}") suspend fun getProduct(@Path("id") productId: String): Response<ProductResponse>
    @GET("cart") suspend fun getCart(): Response<CartResponse>
    @POST("cart/items") suspend fun addCartItem(@Body request: AddCartItemRequest): Response<CartResponse>
    @PATCH("cart/items/{id}") suspend fun updateCartItem(@Path("id") itemId: String, @Body request: UpdateCartItemRequest): Response<CartResponse>
    @DELETE("cart/items/{id}") suspend fun removeCartItem(@Path("id") itemId: String): Response<CartResponse>
    @DELETE("cart") suspend fun clearCart(): Response<CartResponse>
    @POST("cart/merge") suspend fun mergeCart(@Body request: MergeCartRequest): Response<CartResponse>
    @POST("orders") suspend fun createOrder(@Body request: CheckoutRequest): Response<OrderResponse>
    @GET("orders") suspend fun getOrders(): Response<OrderListResponse>
    @GET("orders/{id}") suspend fun getOrder(@Path("id") orderId: String): Response<OrderResponse>
}
