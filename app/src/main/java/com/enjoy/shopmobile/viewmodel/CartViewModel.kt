package com.enjoy.shopmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enjoy.shopmobile.data.api.ApiClient
import com.enjoy.shopmobile.data.model.Cart
import com.enjoy.shopmobile.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartState(
    val loading: Boolean = false,
    val cart: Cart? = null,
    val error: String? = null,
    val operationItemId: String? = null
)

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CartRepository(ApiClient.service(application))
    private val _state = MutableStateFlow(CartState(loading = true))
    val state: StateFlow<CartState> = _state.asStateFlow()

    fun loadCart(showLoading: Boolean = true) {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(loading = showLoading, error = null)
            repository.getCart()
                .onSuccess { cart -> _state.value = CartState(cart = cart) }
                .onFailure { error ->
                    _state.value = CartState(
                        cart = current.cart,
                        error = error.message ?: "Could not load your cart."
                    )
                }
        }
    }

    fun addItem(productId: String, quantity: Int = 1) {
        if (quantity < 1) return
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null, operationItemId = "add")
            repository.addItem(productId, quantity)
                .onSuccess { cart -> _state.value = CartState(cart = cart) }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Could not add item to cart.",
                        operationItemId = null
                    )
                }
        }
    }

    fun increase(itemId: String, currentQuantity: Int, stock: Int) {
        if (currentQuantity >= stock) {
            _state.value = _state.value.copy(error = "Only " + stock + " available in stock.")
            return
        }
        mutate(itemId) { repository.updateItem(itemId, currentQuantity + 1) }
    }

    fun decrease(itemId: String, currentQuantity: Int) {
        if (currentQuantity <= 1) remove(itemId)
        else mutate(itemId) { repository.updateItem(itemId, currentQuantity - 1) }
    }

    fun remove(itemId: String) {
        mutate(itemId) { repository.removeItem(itemId) }
    }

    fun clear() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            repository.clear()
                .onSuccess { cart -> _state.value = CartState(cart = cart) }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = error.message ?: "Could not clear your cart."
                    )
                }
        }
    }

    private fun mutate(itemId: String, operation: suspend () -> Result<Cart>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null, operationItemId = itemId)
            operation()
                .onSuccess { cart -> _state.value = CartState(cart = cart) }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Cart update failed.",
                        operationItemId = null
                    )
                }
        }
    }
}
