package com.berlintransit.app.ui

import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> when (val httpCode = code()) {
        404 -> "Stop not found"
        429 -> "Rate limit reached — try again shortly"
        in 500..599 -> "VBB server error ($httpCode)"
        else -> "Server error ($httpCode)"
    }
    is IOException -> "Network error — check your connection"
    else -> "Unexpected error: ${message ?: "unknown"}"
}
