package com.cocido.nonna.ui.screens.cofres

import com.cocido.nonna.ui.components.CofreUiModel

data class CofresListUiState(
    val isLoading: Boolean = false,
    val cofres: List<CofreUiModel> = emptyList(),
    val errorMessage: String? = null
)

