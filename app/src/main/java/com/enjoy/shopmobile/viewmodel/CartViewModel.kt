package com.enjoy.shopmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enjoy.shopmobile.data.api.ApiClient
import com.enjoy.shopmobile.data.model.Cart
import com.enjoy.shopmobile.data.model.CartItem
import com.enjoy.shopmobile.data.model.CartProduct
import com.enjoy.shopmobile.data.repository.CartRepository
import com.enjoy.shopmobile.data.repository.ProductRepository
import com.enjoy.shopmobile.data.session.VisitorCartStore
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
    private val api = ApiClient.service(application)
    private val repository = CartRepository(api)
    private val productRepository = ProductRepository(api)
    private val visitorCart = VisitorCartStore(application)

    private val _state = MutableStateFlow(CartState(loading = true))
    val state: StateFlow<CartState> = _state.asStateFlow()

    fun loadCart(authenticated: Boolean, showLoading: Boolean = true) {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(loading = showLoading, error = null)

            if (!authenticated) {
                loadVisitorCart()
                return@launch
            }

            val localItems = visitorCart.getItems()
            if (localItems.isNotEmpty()) {
                repository.merge(localItems)
                    .onSuccess {
                        visitorCart.clear()
                        _state.value = CartState(cart = it)
                    }
                    .onFailure { error ->
                        _state.value = CartState(
                            cart = current.cart,
                            error = error.message ?: "Could not sync your cart."
                        )
                    }
            } else {
                repository.getCart()
                    .onSuccess { _state.value = CartState(cart = it) }
                    .onFailure { error ->
                        _state.value = CartState(
                            cart = current.cart,
                            error = error.message ?: "Could not load your cart."
                        )
                    }
            }
        }
    }

    fun addItem(productId: String, authenticated: Boolean, quantity: Int = 1) {
        if (quantity < 1) return
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null, operationItemId = "add")
            if (!authenticated) {
                visitorCart.add(productId, quantity)
                loadVisitorCart()
                return@launch
            }

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

    fun increase(item: CartItem, authenticated: Boolean) {
        if (item.quantity >= item.product.stock) {
            _state.value = _state.value.copy(error = "Only ${item.product.stock} available in stock.")
            return
        }
        if (!authenticated) {
            viewModelScope.launch {
                visitorCart.update(item.product.id, item.quantity + 1)
                loadVisitorCart()
            }
        } else {
            mutate(item.id) { repository.updateItem(item.id, item.quantity + 1) }
        }
    }

    fun decrease(item: CartItem, authenticated: Boolean) {
        if (item.quantity <= 1) {
            remove(item, authenticated)
        } else if (!authenticated) {
            viewModelScope.launch {
                visitorCart.update(item.product.id, item.quantity - 1)
                loadVisitorCart()
            }
        } else {
            mutate(item.id) { repository.updateItem(item.id, item.quantity - 1) }
        }
    }

    fun remove(item: CartItem, authenticated: Boolean) {
        if (!authenticated) {
            viewModelScope.launch {
                visitorCart.remove(item.product.id)
                loadVisitorCart()
            }
        } else {
            mutate(item.id) { repository.removeItem(item.id) }
        }
    }

    fun clear(authenticated: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            if (!authenticated) {
                visitorCart.clear()
                loadVisitorCart()
                return@launch
            }

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

    private suspend fun loadVisitorCart() {
        val items = visitorCart.getItems()
        if (items.isEmpty()) {
            _state.value = CartState(cart = Cart("visitor", emptyList(), 0, 0))
            return
        }

        val hydrated = items.mapNotNull { item ->
            productRepository.getProduct(item.productId).getOrNull()?.let { product ->
                CartItem(
                    id = product.id,
                    product = CartProduct(
                        id = product.id,
                        name = product.name,
                        price = product.price,
                        imageUrl = product.imageUrl,
                        category = product.category,
                        stock = product.stock,
                        inStock = product.inStock,
                        variants = product.variants
                    ),
                    quantity = item.quantity,
                    subtotal = product.price * item.quantity
                )
            }
        }

        val cart = Cart(
            id = "visitor",
            items = hydrated,
            total = hydrated.sumOf { it.subtotal },
            itemCount = hydrated.sumOf { it.quantity }
        )
        _state.value = CartState(cart = cart)
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
