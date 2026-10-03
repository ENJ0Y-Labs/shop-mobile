package com.enjoy.shopmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enjoy.shopmobile.data.api.ApiClient
import com.enjoy.shopmobile.data.model.User
import com.enjoy.shopmobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Unknown : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: User) : AuthState
    data class Unauthenticated(val message: String? = null) : AuthState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(ApiClient.service(application), application)
    private val _state = MutableStateFlow<AuthState>(AuthState.Unknown)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init { checkSession() }

    fun clearMessage() {
        if (_state.value is AuthState.Unauthenticated) _state.value = AuthState.Unauthenticated()
    }

    fun checkSession() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.currentUser()
                .onSuccess { _state.value = AuthState.Authenticated(it) }
                .onFailure { _state.value = AuthState.Unauthenticated(it.message ?: "Your session could not be restored. Please log in again.") }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Unauthenticated("Enter your email and password.")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.login(email.trim(), password)
                .onSuccess { _state.value = AuthState.Authenticated(it) }
                .onFailure { _state.value = AuthState.Unauthenticated(it.message ?: "Login failed.") }
        }
    }

    fun register(name: String, email: String, password: String) {
        when {
            name.isBlank() -> {
                _state.value = AuthState.Unauthenticated("Enter your full name.")
                return
            }
            email.isBlank() || password.isBlank() -> {
                _state.value = AuthState.Unauthenticated("Enter your email and password.")
                return
            }
            password.length < 8 -> {
                _state.value = AuthState.Unauthenticated("Password must be at least 8 characters.")
                return
            }
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.register(email.trim(), password, name.trim())
                .onSuccess { _state.value = AuthState.Authenticated(it) }
                .onFailure { _state.value = AuthState.Unauthenticated(it.message ?: "Account creation failed.") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.logout()
                .onSuccess { _state.value = AuthState.Unauthenticated("Logged out") }
                .onFailure { _state.value = AuthState.Unauthenticated(it.message ?: "Logout failed.") }
        }
    }
}