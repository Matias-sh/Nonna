package com.cocido.nonna.ui.genealogy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Person
import com.cocido.nonna.domain.model.Relation
import com.cocido.nonna.domain.model.VaultId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el árbol genealógico
 * Maneja la carga de personas y relaciones familiares
 */
@HiltViewModel
class GenealogyViewModel @Inject constructor(
    // TODO: Inyectar repositorios cuando estén implementados
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GenealogyUiState>(GenealogyUiState.Loading)
    val uiState: StateFlow<GenealogyUiState> = _uiState.asStateFlow()
    
    fun loadGenealogy() {
        viewModelScope.launch {
            _uiState.value = GenealogyUiState.Loading
            
            try {
                // TODO: Implementar llamada al repositorio
                // val persons = personRepository.getPersonsByVault(currentVaultId)
                // val relations = relationRepository.getRelationsByVault(currentVaultId)
                
                // Simulación temporal con datos dummy
                val persons = createDummyPersons()
                val relations = createDummyRelations()
                
                _uiState.value = GenealogyUiState.Success(
                    persons = persons,
                    relations = relations
                )
                
            } catch (e: Exception) {
                _uiState.value = GenealogyUiState.Error(
                    message = e.message ?: "Error al cargar el árbol genealógico"
                )
            }
        }
    }
    
    private fun createDummyPersons(): List<Person> {
        return listOf(
            Person(
                id = com.cocido.nonna.domain.model.PersonId("person1"),
                fullName = "María González",
                birthDate = System.currentTimeMillis() - (70 * 365 * 24 * 60 * 60 * 1000L), // 70 años atrás
                deathDate = null,
                avatarUrl = null,
                notes = "Abuela materna"
            ),
            Person(
                id = com.cocido.nonna.domain.model.PersonId("person2"),
                fullName = "Carlos González",
                birthDate = System.currentTimeMillis() - (75 * 365 * 24 * 60 * 60 * 1000L), // 75 años atrás
                deathDate = System.currentTimeMillis() - (5 * 365 * 24 * 60 * 60 * 1000L), // Falleció hace 5 años
                avatarUrl = null,
                notes = "Abuelo materno"
            ),
            Person(
                id = com.cocido.nonna.domain.model.PersonId("person3"),
                fullName = "Ana González",
                birthDate = System.currentTimeMillis() - (45 * 365 * 24 * 60 * 60 * 1000L), // 45 años atrás
                deathDate = null,
                avatarUrl = null,
                notes = "Madre"
            ),
            Person(
                id = com.cocido.nonna.domain.model.PersonId("person4"),
                fullName = "Roberto Martínez",
                birthDate = System.currentTimeMillis() - (50 * 365 * 24 * 60 * 60 * 1000L), // 50 años atrás
                deathDate = null,
                avatarUrl = null,
                notes = "Padre"
            ),
            Person(
                id = com.cocido.nonna.domain.model.PersonId("person5"),
                fullName = "Sofía Martínez",
                birthDate = System.currentTimeMillis() - (25 * 365 * 24 * 60 * 60 * 1000L), // 25 años atrás
                deathDate = null,
                avatarUrl = null,
                notes = "Hija"
            )
        )
    }
    
    private fun createDummyRelations(): List<Relation> {
        return listOf(
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("person1"),
                to = com.cocido.nonna.domain.model.PersonId("person3"),
                type = com.cocido.nonna.domain.model.RelationType.PARENT
            ),
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("person2"),
                to = com.cocido.nonna.domain.model.PersonId("person3"),
                type = com.cocido.nonna.domain.model.RelationType.PARENT
            ),
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("person3"),
                to = com.cocido.nonna.domain.model.PersonId("person4"),
                type = com.cocido.nonna.domain.model.RelationType.SPOUSE
            ),
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("person3"),
                to = com.cocido.nonna.domain.model.PersonId("person5"),
                type = com.cocido.nonna.domain.model.RelationType.PARENT
            ),
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("person4"),
                to = com.cocido.nonna.domain.model.PersonId("person5"),
                type = com.cocido.nonna.domain.model.RelationType.PARENT
            )
        )
    }
}

/**
 * Estados de la UI para el árbol genealógico
 */
sealed class GenealogyUiState {
    object Loading : GenealogyUiState()
    data class Success(
        val persons: List<Person>,
        val relations: List<Relation>
    ) : GenealogyUiState()
    data class Error(val message: String) : GenealogyUiState()
}


