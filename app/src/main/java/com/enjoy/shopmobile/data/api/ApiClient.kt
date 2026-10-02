package com.enjoy.shopmobile.data.api

import android.content.Context
import com.enjoy.shopmobile.BuildConfig
import com.enjoy.shopmobile.data.session.PersistentCookieJar
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {
    @Volatile private var service: ApiService? = null
    @Volatile private var cookieJar: PersistentCookieJar? = null

    fun service(context: Context): ApiService =
        service ?: synchronized(this) {
            service ?: createService(context.applicationContext).also { service = it }
        }

    fun clearSession(context: Context) {
        getCookieJar(context.applicationContext).clear()
    }

    private fun createService(context: Context): ApiService {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        val jar = getCookieJar(context)
        val client = OkHttpClient.Builder()
            .cookieJar(jar)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.ensureTrailingSlash())
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(ApiService::class.java)
    }

    private fun getCookieJar(context: Context): PersistentCookieJar =
        cookieJar ?: synchronized(this) {
            cookieJar ?: PersistentCookieJar(context).also { cookieJar = it }
        }

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"
}
