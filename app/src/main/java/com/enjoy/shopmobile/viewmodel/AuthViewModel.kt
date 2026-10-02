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
    private val repository = AuthRepository(
        api = ApiClient.service(application),
        context = application
    )

    private val _state = MutableStateFlow<AuthState>(AuthState.Unknown)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.currentUser()
                .onSuccess { user ->
                    _state.value = AuthState.Authenticated(user)
                }
                .onFailure {
                    _state.value = AuthState.Unauthenticated("No active session")
                }
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
                .onSuccess { user ->
                    _state.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _state.value = AuthState.Unauthenticated(
                        error.message ?: "Login failed."
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.logout()
                .onSuccess {
                    _state.value = AuthState.Unauthenticated("Logged out")
                }
                .onFailure { error ->
                    _state.value = AuthState.Unauthenticated(
                        error.message ?: "Logout failed."
                    )
                }
        }
    }
}
