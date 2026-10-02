package com.enjoy.shopmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enjoy.shopmobile.ui.screens.ProductDetailsScreen
import com.enjoy.shopmobile.ui.screens.ProductListScreen
import com.enjoy.shopmobile.viewmodel.AuthState
import com.enjoy.shopmobile.viewmodel.AuthViewModel
import com.enjoy.shopmobile.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { ShopApp() } }
    }
}

@Composable
private fun ShopApp() {
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()
    val productState by productViewModel.state.collectAsState()
    val selectedProduct by productViewModel.selectedProduct.collectAsState()

    when (val auth = authState) {
        AuthState.Unknown, AuthState.Loading -> LoadingScreen()
        is AuthState.Unauthenticated -> LoginScreen(auth.message, authViewModel)
        is AuthState.Authenticated -> {
            if (selectedProduct != null) {
                ProductDetailsScreen(selectedProduct, false, productViewModel::closeProduct)
            } else {
                ProductListScreen(productState, productViewModel::openProduct, productViewModel::loadProducts)
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        CircularProgressIndicator()
    }
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
