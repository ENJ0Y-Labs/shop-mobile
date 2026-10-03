package com.enjoy.shopmobile.data.repository

import retrofit2.Response

internal fun <T> Response<T>.requireBody(): T {
    if (isSuccessful) return body() ?: error("The server returned an empty response.")
    throw ApiError.Http(code(), errorMessage())
}

internal fun Response<Unit>.requireBodyIfPresent() {
    if (!isSuccessful) throw ApiError.Http(code(), errorMessage())
}

private fun <T> Response<T>.errorMessage(): String {
    val body = errorBody()?.string().orEmpty()
    val detail = Regex("""["'](?:message|error|detail)["']\s*:\s*["']([^"']+)["']""")
        .find(body)?.groupValues?.getOrNull(1)
    return when {
        !detail.isNullOrBlank() -> detail
        code() == 401 -> "Your session has expired. Please log in again."
        code() == 403 -> "You do not have permission to do that."
        code() == 404 -> "We could not find what you requested."
        code() == 409 -> "That change conflicts with the latest shop data. Refresh and try again."
        code() == 422 -> "Some of the information entered is not valid. Check it and try again."
        code() in 500..599 -> "The shop server is having trouble right now. Please try again later."
        else -> "The shop could not complete that request. Please try again."
    }
}

internal suspend fun <T> execute(block: suspend () -> T): Result<T> =
    runCatching { block() }.recoverCatching { error ->
        throw Exception(error.toUserMessage(), error)
    }
