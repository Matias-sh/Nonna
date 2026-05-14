package com.cocido.nonna.ui.screens.cofres

sealed interface CofresListEvent {
    data class OpenCofre(val cofreId: String) : CofresListEvent
    data object CreateCofre : CofresListEvent
    data class SelectTab(val tab: com.cocido.nonna.ui.components.NonnaTab) : CofresListEvent
}

