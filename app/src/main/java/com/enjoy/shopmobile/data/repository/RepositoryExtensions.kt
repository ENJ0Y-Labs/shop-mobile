package com.enjoy.shopmobile.data.repository

import retrofit2.Response

internal fun <T> Response<T>.requireBody(): T {
    if (isSuccessful) return body() ?: error("API returned an empty response body.")
    error("API request failed with HTTP ${code()}.")
}

internal suspend fun <T> execute(block: suspend () -> T): Result<T> = runCatching { block() }
