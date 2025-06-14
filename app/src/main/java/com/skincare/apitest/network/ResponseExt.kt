package com.skincare.apitest.network

import retrofit2.Response
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Response<T>.await(): Response<T> {
    return suspendCancellableCoroutine { continuation ->
        if (isSuccessful) {
            continuation.resume(this)
        } else {
            continuation.resumeWithException(Exception("API call failed with code ${code()}"))
        }
    }
}
