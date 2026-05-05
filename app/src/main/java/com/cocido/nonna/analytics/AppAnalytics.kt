package com.cocido.nonna.analytics

interface AppAnalytics {
    fun track(event: AnalyticsEvent)
}

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap()
)
