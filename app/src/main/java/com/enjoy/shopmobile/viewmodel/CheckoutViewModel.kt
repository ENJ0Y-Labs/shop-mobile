package com.enjoy.shopmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enjoy.shopmobile.data.api.ApiClient
import com.enjoy.shopmobile.data.model.CheckoutRequest
import com.enjoy.shopmobile.data.model.Order
import com.enjoy.shopmobile.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckoutState(
    val submitting: Boolean = false,
    val order: Order? = null,
    val error: String? = null
)

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OrderRepository(ApiClient.service(application))
    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    fun submit(request: CheckoutRequest) {
        viewModelScope.launch {
            _state.value = CheckoutState(submitting = true)
            repository.createOrder(request)
                .onSuccess { _state.value = CheckoutState(order = it) }
                .onFailure { _state.value = CheckoutState(error = it.message ?: "Could not place your order.") }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
