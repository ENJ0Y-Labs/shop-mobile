package com.enjoy.shopmobile.data.session

import android.content.Context
import com.enjoy.shopmobile.data.model.MergeCartItemRequest
import org.json.JSONArray
import org.json.JSONObject

class VisitorCartStore(context: Context) {
    private val preferences = context.getSharedPreferences("visitor_cart", Context.MODE_PRIVATE)
    private val key = "items"

    fun getItems(): List<MergeCartItemRequest> {
        val raw = preferences.getString(key, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val productId = item.optString("product_id")
                    val quantity = item.optInt("quantity")
                    if (productId.isNotBlank() && quantity > 0) {
                        add(MergeCartItemRequest(productId, quantity))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun add(productId: String, quantity: Int = 1): List<MergeCartItemRequest> {
        val items = getItems().toMutableList()
        val index = items.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = items[index]
            items[index] = item.copy(quantity = item.quantity + quantity)
        } else {
            items += MergeCartItemRequest(productId, quantity)
        }
        save(items)
        return items
    }

    fun update(productId: String, quantity: Int): List<MergeCartItemRequest> {
        val items = getItems()
            .map { if (it.productId == productId) it.copy(quantity = quantity) else it }
            .filter { it.quantity > 0 }
        save(items)
        return items
    }

    fun remove(productId: String): List<MergeCartItemRequest> {
        val items = getItems().filterNot { it.productId == productId }
        save(items)
        return items
    }

    fun clear() {
        preferences.edit().remove(key).apply()
    }

    private fun save(items: List<MergeCartItemRequest>) {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("product_id", it.productId)
                    .put("quantity", it.quantity)
            )
        }
        preferences.edit().putString(key, array.toString()).apply()
    }
}
