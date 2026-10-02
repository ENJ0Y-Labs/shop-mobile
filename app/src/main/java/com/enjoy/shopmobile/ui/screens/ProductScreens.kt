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
import com.enjoy.shopmobile.data.model.Product
import com.enjoy.shopmobile.viewmodel.ProductListState

private fun formatPrice(price: Long): String = "₦" + "%,d".format(price)

@Composable
fun ProductListScreen(state: ProductListState, onProductClick: (String) -> Unit, onRefresh: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Shop", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onRefresh) { Text("Refresh") }
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
fun ProductDetailsScreen(product: Product?, loading: Boolean, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TextButton(onClick = onBack) { Text("Back") }
        if (loading) Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        else if (product == null) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Product could not be loaded.") }
        else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(model = product.imageUrl, contentDescription = product.name, modifier = Modifier.fillMaxWidth().height(240.dp), contentScale = ContentScale.Crop)
            Text(product.name, style = MaterialTheme.typography.headlineSmall)
            Text(formatPrice(product.price), style = MaterialTheme.typography.headlineMedium)
            Text(product.description ?: "No description available.")
            Text(if (product.inStock) "In stock: " + product.stock else "Out of stock", color = if (product.inStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }
}