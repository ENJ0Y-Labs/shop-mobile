package com.enjoy.shopmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enjoy.shopmobile.ui.screens.CartScreen
import com.enjoy.shopmobile.ui.screens.ProductDetailsScreen
import com.enjoy.shopmobile.ui.screens.ProductListScreen
import com.enjoy.shopmobile.viewmodel.AuthState
import com.enjoy.shopmobile.viewmodel.AuthViewModel
import com.enjoy.shopmobile.viewmodel.CartViewModel
import com.enjoy.shopmobile.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { ShopApp() } }
    }
}

private enum class ShopScreen { PRODUCTS, DETAILS, CART }

@Composable
private fun ShopApp() {
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()
    val productState by productViewModel.state.collectAsState()
    val selectedProduct by productViewModel.selectedProduct.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    var screen by rememberSaveable { mutableStateOf(ShopScreen.PRODUCTS) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            productViewModel.loadProducts()
            cartViewModel.loadCart()
            screen = ShopScreen.PRODUCTS
        }
    }

    when (val auth = authState) {
        AuthState.Unknown, AuthState.Loading -> LoadingScreen()
        is AuthState.Unauthenticated -> LoginScreen(auth.message, authViewModel)
        is AuthState.Authenticated -> when (screen) {
            ShopScreen.PRODUCTS -> ProductListScreen(
                productState, cartState.cart, {
                    productViewModel.openProduct(it)
                    screen = ShopScreen.DETAILS
                }, {
                    productViewModel.loadProducts()
                    cartViewModel.loadCart()
                }, {
                    cartViewModel.loadCart()
                    screen = ShopScreen.CART
                }
            )
            ShopScreen.DETAILS -> ProductDetailsScreen(
                selectedProduct, selectedProduct == null, productViewModel::closeProduct,
                { cartViewModel.addItem(it) },
                cartState.operationItemId == "add",
                cartState.cart?.itemCount ?: 0
            ) {
                cartViewModel.loadCart()
                screen = ShopScreen.CART
            }
            ShopScreen.CART -> CartScreen(
                cartState,
                { screen = ShopScreen.PRODUCTS },
                { cartViewModel.loadCart() },
                { cartViewModel.increase(it.id, it.quantity, it.product.stock) },
                { cartViewModel.decrease(it.id, it.quantity) },
                { cartViewModel.remove(it.id) },
                { cartViewModel.clear() }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun LoginScreen(message: String?, viewModel: AuthViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("ENJ0Y Shop", style = MaterialTheme.typography.headlineMedium)
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") })
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { viewModel.login(email, password) }, modifier = Modifier.fillMaxWidth()) { Text("Log in") }
    }
}
