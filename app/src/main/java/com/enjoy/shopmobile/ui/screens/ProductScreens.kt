package com.enjoy.shopmobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enjoy.shopmobile.data.model.Cart
import com.enjoy.shopmobile.data.model.CartItem
import com.enjoy.shopmobile.data.model.Product
import com.enjoy.shopmobile.viewmodel.CartState
import com.enjoy.shopmobile.viewmodel.ProductListState

private fun formatPrice(price: Long): String = "₦" + "%,d".format(price)

@Composable
fun ProductListScreen(
    state: ProductListState,
    cart: Cart?,
    onProductClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onCartClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Shop", style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onCartClick) {
                    Text("Cart" + if (cart != null && cart.itemCount > 0) " (" + cart.itemCount + ")" else "")
                }
                TextButton(onClick = onRefresh) { Text("Refresh") }
            }
        }
        when (state) {
            ProductListState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            ProductListState.Empty -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No products found.") }
            is ProductListState.Error -> ErrorProducts(state.message, onRefresh)
            is ProductListState.Success -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.products, key = { it.id }) { product -> ProductCard(product, onProductClick) }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: (String) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick(product.id) }) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(model = product.imageUrl, contentDescription = product.name, modifier = Modifier.size(96.dp), contentScale = ContentScale.Crop)
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold)
                Text(formatPrice(product.price), style = MaterialTheme.typography.titleMedium)
                Text(if (product.inStock) "In stock (" + product.stock + ")" else "Out of stock", color = if (product.inStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ErrorProducts(message: String, retry: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(message); Button(onClick = retry) { Text("Try again") } } }
}

@Composable
fun ProductDetailsScreen(
    product: Product?,
    loading: Boolean,
    onBack: () -> Unit,
    onAddToCart: (String) -> Unit,
    adding: Boolean,
    cartItemCount: Int,
    onCartClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Back") }
            TextButton(onClick = onCartClick) { Text("Cart" + if (cartItemCount > 0) " (" + cartItemCount + ")" else "") }
        }
        if (loading) Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        else if (product == null) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Product could not be loaded.") }
        else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(model = product.imageUrl, contentDescription = product.name, modifier = Modifier.fillMaxWidth().height(240.dp), contentScale = ContentScale.Crop)
            Text(product.name, style = MaterialTheme.typography.headlineSmall)
            Text(formatPrice(product.price), style = MaterialTheme.typography.headlineMedium)
            Text(product.description ?: "No description available.")
            Text(if (product.inStock) "In stock: " + product.stock else "Out of stock", color = if (product.inStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Button(onClick = { onAddToCart(product.id) }, enabled = product.inStock && !adding, modifier = Modifier.fillMaxWidth()) {
                if (adding) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(if (product.inStock) "Add to Cart" else "Out of Stock")
            }
        }
    }
}

@Composable
fun CartScreen(
    state: CartState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit,
    onClear: () -> Unit
) {
    val cart = state.cart
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Back to Shop") }
            TextButton(onClick = onRefresh, enabled = !state.loading) { Text("Refresh") }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp)) }
        if (state.loading && cart == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else if (cart == null || cart.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your cart is empty.", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onBack) { Text("Continue Shopping") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cart.items, key = { it.id }) { item ->
                    CartItemRow(item, state.operationItemId == item.id, { onIncrease(item) }, { onDecrease(item) }, { onRemove(item) })
                }
            }
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Items: " + cart.itemCount)
                Text("Total: " + formatPrice(cart.total), style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = onClear, enabled = state.operationItemId == null && !state.loading, modifier = Modifier.fillMaxWidth()) { Text("Clear Cart") }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, busy: Boolean, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(model = item.product.imageUrl, contentDescription = item.product.name, modifier = Modifier.size(88.dp), contentScale = ContentScale.Crop)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold)
                Text(formatPrice(item.product.price))
                Text("Subtotal: " + formatPrice(item.subtotal))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onDecrease, enabled = !busy) { Text("−") }
                    Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 12.dp))
                    OutlinedButton(onClick = onIncrease, enabled = !busy && item.quantity < item.product.stock) { Text("+") }
                }
                TextButton(onClick = onRemove, enabled = !busy) { Text("Remove") }
                if (item.quantity >= item.product.stock && item.product.stock > 0) Text("Maximum available stock reached.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
