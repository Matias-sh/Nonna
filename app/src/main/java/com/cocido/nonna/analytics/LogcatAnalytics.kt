package com.cocido.nonna.analytics

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogcatAnalytics @Inject constructor() : AppAnalytics {
    private companion object {
        const val TAG = "NonnaAnalytics"
    }

    override fun track(event: AnalyticsEvent) {
        if (event.params.isEmpty()) {
            Log.d(TAG, "event=${event.name}")
            return
        }
        val encoded = event.params.entries.joinToString(separator = " ") { (k, v) -> "$k=$v" }
        Log.d(TAG, "event=${event.name} $encoded")
    }
}
