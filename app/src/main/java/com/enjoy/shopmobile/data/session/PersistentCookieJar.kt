package com.enjoy.shopmobile.data.session

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val cookies = mutableMapOf<CookieKey, Cookie>()

    init {
        restoreCookies()
    }

    override fun saveFromResponse(url: HttpUrl, responseCookies: List<Cookie>) {
        val editor = preferences.edit()
        responseCookies.forEach { cookie ->
            val key = CookieKey(cookie.domain, cookie.path, cookie.name)
            cookies[key] = cookie
            editor.putString(key.toStorageKey(), serialize(cookie))
        }
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val expiredKeys = cookies.filterValues { it.expiresAt < now }.keys

        if (expiredKeys.isNotEmpty()) {
            val editor = preferences.edit()
            expiredKeys.forEach { key ->
                cookies.remove(key)
                editor.remove(key.toStorageKey())
            }
            editor.apply()
        }

        return cookies.values.filter { it.matches(url) }
    }

    fun clear() {
        cookies.clear()
        preferences.edit().clear().apply()
    }

    private fun restoreCookies() {
        preferences.all.values.filterIsInstance<String>().forEach { serialized ->
            deserialize(serialized)?.let { cookie ->
                cookies[CookieKey(cookie.domain, cookie.path, cookie.name)] = cookie
            }
        }
    }

    private fun serialize(cookie: Cookie): String = listOf(
        cookie.name,
        cookie.value,
        cookie.domain,
        cookie.path,
        cookie.expiresAt.toString(),
        cookie.secure.toString(),
        cookie.httpOnly.toString(),
        cookie.hostOnly.toString()
    ).joinToString(FIELD_SEPARATOR)

    private fun deserialize(value: String): Cookie? {
        val fields = value.split(FIELD_SEPARATOR)
        if (fields.size != 8) return null

        return runCatching {
            val builder = Cookie.Builder()
                .name(fields[0])
                .value(fields[1])
                .path(fields[3])
                .expiresAt(fields[4].toLong())

            if (fields[7].toBoolean()) builder.hostOnlyDomain(fields[2])
            else builder.domain(fields[2])

            if (fields[5].toBoolean()) builder.secure()
            if (fields[6].toBoolean()) builder.httpOnly()

            builder.build()
        }.getOrNull()
    }

    private data class CookieKey(
        val domain: String,
        val path: String,
        val name: String
    ) {
        fun toStorageKey(): String = "cookie:$domain|$path|$name"
    }

    companion object {
        private const val PREFERENCES_NAME = "http_cookies"
        private const val FIELD_SEPARATOR = "\u0001"
    }
}
