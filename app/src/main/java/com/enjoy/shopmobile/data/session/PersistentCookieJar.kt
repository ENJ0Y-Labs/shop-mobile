package com.enjoy.shopmobile.data.session

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val preferences = context.applicationContext.getSharedPreferences("http_cookies", Context.MODE_PRIVATE)
    private val cookies = mutableMapOf<String, Cookie>()

    init {
        preferences.all.values.filterIsInstance<String>().forEach { value ->
            runCatching {
                Cookie.parse(HttpUrl.Builder().scheme("https").host("shop-website-aner.onrender.com").build(), value)
            }.getOrNull()?.let { cookies[it.name] = it }
        }
    }

    override fun saveFromResponse(url: HttpUrl, responseCookies: List<Cookie>) {
        responseCookies.forEach {
            cookies[it.name] = it
            preferences.edit().putString(it.name, it.toString()).apply()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        cookies.values.filter { it.expiresAt >= System.currentTimeMillis() }

    fun clear() {
        cookies.clear()
        preferences.edit().clear().apply()
    }
}
