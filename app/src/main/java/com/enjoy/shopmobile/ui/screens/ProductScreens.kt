@file:OptIn(ExperimentalMaterial3Api::class)

package com.enjoy.shopmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.enjoy.shopmobile.data.model.Cart
import com.enjoy.shopmobile.data.model.CartItem
import com.enjoy.shopmobile.data.model.Product
import com.enjoy.shopmobile.data.model.CheckoutRequest
import com.enjoy.shopmobile.viewmodel.CheckoutState
import com.enjoy.shopmobile.viewmodel.CartState
import com.enjoy.shopmobile.viewmodel.ProductListState
import com.enjoy.shopmobile.ui.theme.ShopThemeDefaults
import java.util.Locale

private fun formatPrice(price: Long): String {
    val whole = price / 100
    val cents = price % 100
    return String.format(Locale.US, "₦%,d.%02d", whole, cents)
}

@Composable
private fun ProductImage(model: String?, name: String, modifier: Modifier) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = name,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        },
        error = {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
                Text(name.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ProductListScreen(state: ProductListState, cart: Cart?, userName: String, authenticated: Boolean, onProductClick: (String) -> Unit, onRefresh: () -> Unit, onCartClick: () -> Unit, onAuthAction: () -> Unit) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("enj0y", fontWeight = FontWeight.Bold); Text("SOLUTION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) } },
            actions = { CartButton(cart?.itemCount ?: 0, onCartClick); IconButton(onClick = onAuthAction) { Icon(Icons.AutoMirrored.Filled.Logout, if (authenticated) "Log out" else "Sign in") } }
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text("Hi, ${userName.substringBefore(" ").ifBlank { "there" }}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp))
            when (state) {
                ProductListState.Loading -> LoadingState()
                ProductListState.Empty -> EmptyState("No products yet", "There are no products available right now.")
                is ProductListState.Error -> ErrorState(state.message, onRefresh)
                is ProductListState.Success -> LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(state.products, key = { it.id }) { product -> ProductCard(product, onProductClick) }
                }
            }
        }
    }
}
@Composable
private fun CartButton(itemCount: Int, onClick: () -> Unit) {
    BadgedBox(modifier = Modifier.padding(end = 4.dp), badge = { if (itemCount > 0) Badge { Text(itemCount.coerceAtMost(99).toString()) } }) {
        IconButton(onClick = onClick) { Icon(Icons.Default.ShoppingCart, "Cart") }
    }
}
@Composable
private fun ProductCard(product: Product, onClick: (String) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick(product.id) }, shape = RoundedCornerShape(14.dp)) {
        Column {
            Box(Modifier.fillMaxWidth().height(210.dp).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                ProductImage(product.imageUrl, product.name, Modifier.fillMaxSize())
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(product.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(product.name, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatPrice(product.price), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if (product.inStock) "In stock" else "Sold out", color = if (product.inStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
@Composable private fun LoadingState() { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() } }
@Composable private fun EmptyState(title: String, message: String) { Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun ErrorState(message: String, retry: () -> Unit) { Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Something went wrong", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = retry) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text("Try again") } } } }

@Composable
fun ProductDetailsScreen(product: Product?, loading: Boolean, error: String?, onBack: () -> Unit, onRetry: () -> Unit, onAddToCart: (String) -> Unit, adding: Boolean, cartItemCount: Int, onCartClick: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Product") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, actions = { CartButton(cartItemCount, onCartClick) }) }) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            error != null -> ErrorState(error, onRetry)
            product == null -> EmptyState("Product unavailable", "We could not find that product.")
            else -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ProductImage(product.imageUrl, product.name, Modifier.fillMaxWidth().height(320.dp))
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(product.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(formatPrice(product.price), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(product.description ?: "No description available.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    Text(if (product.inStock) "${product.stock} available" else "Sold out", color = if (product.inStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                    Button(onClick = { onAddToCart(product.id) }, enabled = product.inStock && !adding, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 14.dp)) {
                        if (adding) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text(if (product.inStock) "Add to cart" else "Sold out")
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(state: CartState, authenticated: Boolean, onBack: () -> Unit, onRefresh: () -> Unit, onIncrease: (CartItem) -> Unit, onDecrease: (CartItem) -> Unit, onRemove: (CartItem) -> Unit, onClear: () -> Unit, onCheckout: () -> Unit) {
    val cart = state.cart
    Scaffold(topBar = { TopAppBar(title = { Text("Your cart") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, actions = { IconButton(onClick = onRefresh, enabled = !state.loading) { Icon(Icons.Default.Refresh, "Refresh cart") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.error?.let { Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer) { Text(it, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer) } }
            if (state.loading && cart == null) LoadingState()
            else if (cart == null || cart.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Your cart is empty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Add something you like and it will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = onBack) { Text("Continue shopping") } } }
            } else {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cart.items, key = { it.id }) { item -> CartItemRow(item, state.operationItemId == item.id, { onIncrease(item) }, { onDecrease(item) }, { onRemove(item) }) }
                }
                Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 4.dp) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Items", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(cart.itemCount.toString(), fontWeight = FontWeight.SemiBold) }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total", style = MaterialTheme.typography.titleMedium); Text(formatPrice(cart.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                        OutlinedButton(onClick = onClear, enabled = state.operationItemId == null && !state.loading, modifier = Modifier.fillMaxWidth()) { Text("Clear cart") }
                        Button(onClick = onCheckout, enabled = state.operationItemId == null && !state.loading, modifier = Modifier.fillMaxWidth()) { Text(if (authenticated) "Checkout" else "Sign in to checkout") }
                    }
                }
            }
        }
    }
}
@Composable private fun CartItemRow(item: CartItem, busy: Boolean, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Card(shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProductImage(item.product.imageUrl, item.product.name, Modifier.size(84.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold)
                Text(formatPrice(item.product.price), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Subtotal: ${formatPrice(item.subtotal)}", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onDecrease, enabled = !busy) { Text("−") }
                    Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 14.dp), fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = onIncrease, enabled = !busy && item.quantity < item.product.stock) { Text("+") }
                }
                TextButton(onClick = onRemove, enabled = !busy) { Text("Remove") }
                if (item.quantity >= item.product.stock && item.product.stock > 0) Text("Maximum available stock reached.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@Composable
fun CheckoutScreen(
    userName: String,
    userEmail: String,
    state: CheckoutState,
    onBack: () -> Unit,
    onSubmit: (CheckoutRequest) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(userName) }
    var email by rememberSaveable { mutableStateOf(userEmail) }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (state.order != null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("Order placed", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Order #${state.order.orderNumber}")
                    Button(onClick = onBack) { Text("Continue shopping") }
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Complete your order", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("You need an account to checkout. Your cart stays available before sign in.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.error?.let {
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                CheckoutField("Full name", name) { name = it }
                CheckoutField("Email", email) { email = it }
                CheckoutField("Phone", phone) { phone = it }
                CheckoutField("Address", address) { address = it }
                CheckoutField("City", city) { city = it }
                CheckoutField("State", region) { region = it }
                CheckoutField("Country", "Nigeria") { }

                Button(
                    onClick = {
                        onSubmit(
                            CheckoutRequest(
                                name = name.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                city = city.trim(),
                                state = region.trim(),
                                country = "Nigeria"
                            )
                        )
                    },
                    enabled = !state.submitting && name.isNotBlank() && email.isNotBlank() &&
                        phone.isNotBlank() && address.isNotBlank() && city.isNotBlank() && region.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.submitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Place order")
                }
            }
        }
    }
}

@Composable
private fun CheckoutField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = ShopThemeDefaults.textFieldColors()
    )
}
