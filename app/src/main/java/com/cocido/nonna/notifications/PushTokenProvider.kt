package com.cocido.nonna.notifications

interface PushTokenProvider {
    fun fetchToken(onResult: (String?) -> Unit)
    fun deleteToken(onComplete: (Boolean) -> Unit)
}

