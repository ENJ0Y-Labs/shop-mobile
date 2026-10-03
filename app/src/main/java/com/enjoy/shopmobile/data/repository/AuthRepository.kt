package com.enjoy.shopmobile.data.repository

import android.content.Context
import com.enjoy.shopmobile.data.api.ApiClient
import com.enjoy.shopmobile.data.api.ApiService
import com.enjoy.shopmobile.data.model.*

class AuthRepository(
    private val api: ApiService,
    private val context: Context
) {
    suspend fun login(email: String, password: String): Result<User> =
        execute { api.login(LoginRequest(email, password)).requireBody().user }

    suspend fun register(email: String, password: String, name: String): Result<User> =
        execute { api.register(RegisterRequest(email, password, name)).requireBody().user }

    suspend fun currentUser(): Result<User> =
        execute {
            val response = api.me()
            if (response.code() == 401) {
                ApiClient.clearSession(context)
                throw ApiError.Http(
                    statusCode = 401,
                    message = "Your session has expired. Please log in again."
                )
            }
            response.requireBody().user
        }

    suspend fun logout(): Result<Unit> =
        execute {
            try {
                api.logout().requireBodyIfPresent()
            } finally {
                ApiClient.clearSession(context)
            }
            Unit
        }
}
