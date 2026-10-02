package com.enjoy.shopmobile.data.repository

import com.enjoy.shopmobile.data.api.ApiService
import com.enjoy.shopmobile.data.model.*

class CartRepository(private val api: ApiService) {
    suspend fun getCart(): Result<Cart> = execute { api.getCart().requireBody().cart }
    suspend fun addItem(productId: String, quantity: Int = 1): Result<Cart> =
        execute { api.addCartItem(AddCartItemRequest(productId, quantity)).requireBody().cart }
    suspend fun updateItem(itemId: String, quantity: Int): Result<Cart> =
        execute { api.updateCartItem(itemId, UpdateCartItemRequest(quantity)).requireBody().cart }
    suspend fun removeItem(itemId: String): Result<Cart> =
        execute { api.removeCartItem(itemId).requireBody().cart }
    suspend fun clear(): Result<Cart> = execute { api.clearCart().requireBody().cart }
    suspend fun merge(items: List<MergeCartItemRequest>): Result<Cart> =
        execute { api.mergeCart(MergeCartRequest(items)).requireBody().cart }
}
