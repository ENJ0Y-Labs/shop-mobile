package com.enjoy.shopmobile.data.repository

import com.enjoy.shopmobile.data.api.ApiService
import com.enjoy.shopmobile.data.model.Product

class ProductRepository(private val api: ApiService) {
    suspend fun getProducts(search: String? = null, category: String? = null, sort: String = "name", page: Int = 1, perPage: Int = 12): Result<List<Product>> =
        execute { api.getProducts(search, category, sort, page, perPage).requireBody().products }
    suspend fun getProduct(productId: String): Result<Product> =
        execute { api.getProduct(productId).requireBody().product }
}
