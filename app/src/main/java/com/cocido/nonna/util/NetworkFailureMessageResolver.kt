package com.cocido.nonna.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

object NetworkFailureMessageResolver {

    fun fromIOException(error: IOException): String {
        val raw = error.message?.lowercase().orEmpty()
        if (error is SocketTimeoutException || error is InterruptedIOException || "timeout" in raw || "timed out" in raw) {
            return UserMessages.REQUEST_TIMEOUT
        }
        if (!hasInternetConnection()) {
            return UserMessages.NO_INTERNET
        }
        return UserMessages.SERVER_UNREACHABLE
    }

    private fun hasInternetConnection(): Boolean {
        val context = AppContextProvider.get() ?: return true
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
