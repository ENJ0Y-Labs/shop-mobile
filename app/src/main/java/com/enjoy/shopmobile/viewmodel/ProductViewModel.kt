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

sealed interface ProductDetailsState {
    data object Idle : ProductDetailsState
    data object Loading : ProductDetailsState
    data class Success(val product: Product) : ProductDetailsState
    data class Error(val productId: String, val message: String) : ProductDetailsState
}

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository(ApiClient.service(application))
    private val _state = MutableStateFlow<ProductListState>(ProductListState.Loading)
    val state: StateFlow<ProductListState> = _state.asStateFlow()
    private val _detailsState = MutableStateFlow<ProductDetailsState>(ProductDetailsState.Idle)
    val detailsState: StateFlow<ProductDetailsState> = _detailsState.asStateFlow()

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
            _detailsState.value = ProductDetailsState.Loading
            repository.getProduct(productId)
                .onSuccess { _detailsState.value = ProductDetailsState.Success(it) }
                .onFailure { _detailsState.value = ProductDetailsState.Error(productId, it.message ?: "Could not load this product.") }
        }
    }

    fun closeProduct() { _detailsState.value = ProductDetailsState.Idle }
}
