package com.enjoy.shopmobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(val error: ErrorBody? = null)

@Serializable
data class ErrorBody(val code: String, val message: String)
