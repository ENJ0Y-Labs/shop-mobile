package com.enjoy.shopmobile.data.repository

import com.enjoy.shopmobile.data.api.ApiService
import com.enjoy.shopmobile.data.model.CheckoutRequest
import com.enjoy.shopmobile.data.model.Order

class OrderRepository(private val api: ApiService) {
    suspend fun createOrder(request: CheckoutRequest): Result<Order> =
        execute { api.createOrder(request).requireBody().order }
    suspend fun getOrders(): Result<List<Order>> =
        execute { api.getOrders().requireBody().orders }
    suspend fun getOrder(orderId: String): Result<Order> =
        execute { api.getOrder(orderId).requireBody().order }
}
