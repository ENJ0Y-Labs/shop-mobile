package com.enjoy.shopmobile.data.repository

import com.enjoy.shopmobile.data.api.ApiService
import com.enjoy.shopmobile.data.model.*

class AuthRepository(private val api: ApiService) {
    suspend fun login(email: String, password: String): Result<User> =
        execute { api.login(LoginRequest(email, password)).requireBody().user }
    suspend fun register(email: String, password: String, name: String): Result<User> =
        execute { api.register(RegisterRequest(email, password, name)).requireBody().user }
    suspend fun currentUser(): Result<User> =
        execute { api.me().requireBody().user }
    suspend fun logout(): Result<Unit> = execute { api.logout(); Unit }
}
