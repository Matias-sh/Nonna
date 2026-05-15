package com.cocido.nonna.notifications

interface PushTokenProvider {
    fun fetchToken(onResult: (String?) -> Unit)
}

