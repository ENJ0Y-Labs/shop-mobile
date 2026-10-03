package com.enjoy.shopmobile.data.repository

import android.database.sqlite.SQLiteException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class ApiError(message: String) : Exception(message) {
    class Http(val statusCode: Int, message: String) : ApiError(message)
}

internal fun Throwable.toUserMessage(): String = when (this) {
    is ApiError -> message ?: "Something went wrong. Please try again."
    is SocketTimeoutException -> "The server took too long to respond. Check your connection and try again."
    is UnknownHostException, is ConnectException, is IOException ->
        "No internet connection or the shop server is unavailable. Check your connection and try again."
    is SerializationException -> "The server returned an invalid response. Please try again later."
    is HttpException -> when (code()) {
        401 -> "Your session has expired. Please log in again."
        403 -> "You do not have permission to do that."
        404 -> "We could not find what you requested."
        409 -> "That change conflicts with the latest shop data. Refresh and try again."
        422 -> "Some of the information you entered is not valid. Check it and try again."
        in 500..599 -> "The shop server is having trouble right now. Please try again later."
        else -> "The shop could not complete that request. Please try again."
    }
    is SQLiteException -> "The app could not read its local data. Please try again."
    is IllegalStateException -> message ?: "The server returned an invalid response. Please try again later."
    else -> "Something went wrong. Please try again."
}
