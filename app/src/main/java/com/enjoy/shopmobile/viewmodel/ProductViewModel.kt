package com.enjoy.shopmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enjoy.shopmobile.data.api.ApiClient
import com.enjoy.shopmobile.data.model.Product
import com.enjoy.shopmobile.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductListState {
    data object Loading : ProductListState
    data class Success(val products: List<Product>) : ProductListState
    data object Empty : ProductListState
    data class Error(val message: String) : ProductListState
}

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository(ApiClient.service(application))
    private val _state = MutableStateFlow<ProductListState>(ProductListState.Loading)
    val state: StateFlow<ProductListState> = _state.asStateFlow()
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _state.value = ProductListState.Loading
            repository.getProducts()
                .onSuccess { products ->
                    _state.value = if (products.isEmpty()) ProductListState.Empty else ProductListState.Success(products)
                }
                .onFailure { _state.value = ProductListState.Error(it.message ?: "Could not load products.") }
        }
    }

    fun openProduct(productId: String) {
        viewModelScope.launch {
            _selectedProduct.value = null
            repository.getProduct(productId)
                .onSuccess { _selectedProduct.value = it }
                .onFailure { _state.value = ProductListState.Error(it.message ?: "Could not load product.") }
        }
    }

    fun closeProduct() { _selectedProduct.value = null }
}
