package com.cocido.nonna.ui.genealogy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Person
import com.cocido.nonna.domain.model.Relation
import com.cocido.nonna.domain.usecase.GetGenealogyGraphUseCaseImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el fragment de árbol genealógico
 */
@HiltViewModel
class GenealogyViewModel @Inject constructor(
    private val getGenealogyGraphUseCase: GetGenealogyGraphUseCaseImpl
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GenealogyUiState>(GenealogyUiState.Idle)
    val uiState: StateFlow<GenealogyUiState> = _uiState.asStateFlow()
    
    fun loadGenealogy() {
        viewModelScope.launch {
            _uiState.value = GenealogyUiState.Loading
            
            getGenealogyGraphUseCase(com.cocido.nonna.domain.model.VaultId("default"))
                .collect { (persons, relations) ->
                    _uiState.value = GenealogyUiState.Success(persons, relations)
                }
        }
    }
    
    fun refreshGenealogy() {
        loadGenealogy()
    }
}

/**
 * Estados de la UI para el árbol genealógico
 */
sealed class GenealogyUiState {
    object Idle : GenealogyUiState()
    object Loading : GenealogyUiState()
    data class Success(val persons: List<Person>, val relations: List<Relation>) : GenealogyUiState()
    data class Error(val message: String) : GenealogyUiState()
}

