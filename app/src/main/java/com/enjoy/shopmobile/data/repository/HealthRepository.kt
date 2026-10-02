package com.enjoy.shopmobile.data.repository

import com.enjoy.shopmobile.data.api.ApiService

class HealthRepository(private val api: ApiService) {
    suspend fun check(): Result<String> = execute { api.health().requireBody().status }
}
