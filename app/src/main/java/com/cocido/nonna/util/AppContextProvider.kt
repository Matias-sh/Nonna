package com.cocido.nonna.util

import android.content.Context

object AppContextProvider {
    @Volatile
    private var applicationContext: Context? = null

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    fun get(): Context? = applicationContext
}
