package com.enjoy.shopmobile

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.enjoy.shopmobile.ui.screens.CartScreen
import com.enjoy.shopmobile.ui.screens.CheckoutScreen
import com.enjoy.shopmobile.ui.screens.ProductDetailsScreen
import com.enjoy.shopmobile.ui.screens.ProductListScreen
import com.enjoy.shopmobile.ui.theme.ShopTheme
import com.enjoy.shopmobile.ui.theme.ShopThemeDefaults
import com.enjoy.shopmobile.viewmodel.AuthState
import com.enjoy.shopmobile.viewmodel.AuthViewModel
import com.enjoy.shopmobile.viewmodel.CartViewModel
import com.enjoy.shopmobile.viewmodel.CheckoutViewModel
import com.enjoy.shopmobile.viewmodel.ProductDetailsState
import com.enjoy.shopmobile.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent { ShopTheme { ShopApp() } }
    }
}

private enum class ShopScreen { PRODUCTS, DETAILS, CART, CHECKOUT }

@Composable
private fun ShopApp() {
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val checkoutViewModel: CheckoutViewModel = viewModel()

    val authState by authViewModel.state.collectAsState()
    val productState by productViewModel.state.collectAsState()
    val detailsState by productViewModel.detailsState.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val checkoutState by checkoutViewModel.state.collectAsState()

    var screen by rememberSaveable { mutableStateOf(ShopScreen.PRODUCTS) }
    var showRegister by rememberSaveable { mutableStateOf(false) }
    var showAuth by rememberSaveable { mutableStateOf(false) }
    var checkoutAfterLogin by rememberSaveable { mutableStateOf(false) }

    val authenticated = authState is AuthState.Authenticated
    val user = (authState as? AuthState.Authenticated)?.user

    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
        cartViewModel.loadCart(authenticated = false)
    }

    LaunchedEffect(authState) {
        val isAuthenticated = authState is AuthState.Authenticated
        cartViewModel.loadCart(authenticated = isAuthenticated)
        if (isAuthenticated) {
            showAuth = false
            showRegister = false
            if (checkoutAfterLogin) {
                checkoutAfterLogin = false
                screen = ShopScreen.CHECKOUT
            }
        }
    }

    when {
        showAuth && !authenticated -> {
            if (showRegister) {
                RegisterScreen(authState.let { (it as? AuthState.Unauthenticated)?.message }, authViewModel) {
                    showRegister = false
                    authViewModel.clearMessage()
                }
            } else {
                LoginScreen(authState.let { (it as? AuthState.Unauthenticated)?.message }, authViewModel) {
                    showRegister = true
                    authViewModel.clearMessage()
                }
            }
        }

        screen == ShopScreen.CHECKOUT && authenticated && user != null -> {
            CheckoutScreen(
                userName = user.name,
                userEmail = user.email,
                state = checkoutState,
                onBack = { screen = ShopScreen.CART },
                onSubmit = { checkoutViewModel.submit(it) }
            )
        }

        else -> when (screen) {
            ShopScreen.PRODUCTS -> ProductListScreen(
                state = productState,
                cart = cartState.cart,
                userName = user?.name ?: "there",
                authenticated = authenticated,
                onProductClick = { productViewModel.openProduct(it); screen = ShopScreen.DETAILS },
                onRefresh = {
                    productViewModel.loadProducts()
                    cartViewModel.loadCart(authenticated)
                },
                onCartClick = {
                    cartViewModel.loadCart(authenticated)
                    screen = ShopScreen.CART
                },
                onAuthAction = {
                    if (authenticated) authViewModel.logout()
                    else {
                        checkoutAfterLogin = false
                        showAuth = true
                    }
                }
            )

            ShopScreen.DETAILS -> {
                val detail = detailsState
                ProductDetailsScreen(
                    product = (detail as? ProductDetailsState.Success)?.product,
                    loading = detail is ProductDetailsState.Loading,
                    error = (detail as? ProductDetailsState.Error)?.message,
                    onBack = { productViewModel.closeProduct(); screen = ShopScreen.PRODUCTS },
                    onRetry = { if (detail is ProductDetailsState.Error) productViewModel.openProduct(detail.productId) },
                    onAddToCart = { cartViewModel.addItem(it, authenticated) },
                    adding = cartState.operationItemId == "add",
                    cartItemCount = cartState.cart?.itemCount ?: 0
                ) {
                    cartViewModel.loadCart(authenticated)
                    screen = ShopScreen.CART
                }
            }

            ShopScreen.CART -> CartScreen(
                state = cartState,
                authenticated = authenticated,
                onBack = { screen = ShopScreen.PRODUCTS },
                onRefresh = { cartViewModel.loadCart(authenticated) },
                onIncrease = { cartViewModel.increase(it, authenticated) },
                onDecrease = { cartViewModel.decrease(it, authenticated) },
                onRemove = { cartViewModel.remove(it, authenticated) },
                onClear = { cartViewModel.clear(authenticated) },
                onCheckout = {
                    if (authenticated) {
                        screen = ShopScreen.CHECKOUT
                    } else {
                        checkoutAfterLogin = true
                        showRegister = false
                        showAuth = true
                    }
                }
            )

            ShopScreen.CHECKOUT -> Unit
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun AuthHeader(eyebrow: String, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Text(
                "enj0y Solution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LoginScreen(message: String?, viewModel: AuthViewModel, onCreateAccount: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    AuthSurface {
        AuthHeader("Welcome back", "Sign in", "Access your cart and orders.")
        message?.let { AuthError(it) }
        OutlinedTextField(
            email, { email = it }, Modifier.fillMaxWidth(),
            label = { Text("Email") }, singleLine = true,
            colors = ShopThemeDefaults.textFieldColors()
        )
        OutlinedTextField(
            password, { password = it }, Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            colors = ShopThemeDefaults.textFieldColors()
        )
        Button(
            onClick = { viewModel.login(email, password) },
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ShopThemeDefaults.buttonColors()
        ) { Text("Sign in") }
        TextButton(onClick = onCreateAccount, Modifier.fillMaxWidth()) {
            Text("New here? Create an account")
        }
    }
}

@Composable
private fun RegisterScreen(message: String?, viewModel: AuthViewModel, onBackToLogin: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    AuthSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackToLogin) { Icon(Icons.Default.ArrowBack, "Back") }
            Text("Back to sign in", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AuthHeader("Create your account", "Join enj0y Solution", "One account for your cart and orders.")
        message?.let { AuthError(it) }
        OutlinedTextField(
            name, { name = it }, Modifier.fillMaxWidth(),
            label = { Text("Full name") }, singleLine = true,
            colors = ShopThemeDefaults.textFieldColors()
        )
        OutlinedTextField(
            email, { email = it }, Modifier.fillMaxWidth(),
            label = { Text("Email") }, singleLine = true,
            colors = ShopThemeDefaults.textFieldColors()
        )
        OutlinedTextField(
            password, { password = it }, Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            colors = ShopThemeDefaults.textFieldColors()
        )
        Button(
            onClick = { viewModel.register(name, email, password) },
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ShopThemeDefaults.buttonColors()
        ) { Text("Create account") }
    }
}

@Composable
private fun AuthSurface(content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            Modifier.fillMaxWidth().widthIn(max = 460.dp),
            colors = ShopThemeDefaults.cardColors()
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

@Composable
private fun AuthError(message: String) {
    Surface(
        Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}
