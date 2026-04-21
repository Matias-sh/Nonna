package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.ArbolFamiliarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyTreeUiState(
    val nodes: List<TreeNode> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FamilyTreeViewModel @Inject constructor(
    private val repository: ArbolFamiliarRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FamilyTreeUiState())
    val state: StateFlow<FamilyTreeUiState> = _state.asStateFlow()

    private var isLoadingInProgress = false

    init {
        // Cargamos el árbol una sola vez cuando se crea el ViewModel
        load()
    }

    fun load() {
        // Evitar múltiples llamadas simultáneas
        if (isLoadingInProgress) return

        // Solo cargar si no hay datos y no está cargando
        if (_state.value.isLoading || (_state.value.nodes.isNotEmpty() && _state.value.errorMessage == null)) {
            return
        }

        isLoadingInProgress = true
        viewModelScope.launch {
            repository.miArbol().collect { result ->
                when (result) {
                    is ApiResult.Loading -> _state.update { it.copy(isLoading = true, errorMessage = null) }
                    is ApiResult.Success -> {
                        _state.update {
                            it.copy(
                                nodes = result.data,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        isLoadingInProgress = false
                    }
                    is ApiResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                        isLoadingInProgress = false
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Crear una nueva persona en el árbol.
     * Se encarga de mapear el nombre seleccionado a un ID de conexión válido
     * y refrescar el árbol al finalizar.
     */
    fun addPerson(
        fullName: String,
        selectedRelationName: String?,
        birthDate: String?,
        deathDate: String?,
        notes: String?,
        createCofre: Boolean,
        onFinished: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            // Marcamos loading en el árbol mientras se crea la persona
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            // Buscamos el ID de la persona de conexión (si se eligió alguna)
            val conexionId: Int? = selectedRelationName?.let { relationName ->
                findConnectionIdByName(relationName)
            }

            // Normalizamos fechas al formato que espera el backend (yyyy-MM-dd)
            val normalizedBirthDate = normalizeDate(birthDate)
            val normalizedDeathDate = normalizeDate(deathDate)

            when (
                val result = repository.crearPersona(
                    nombreCompleto = fullName,
                    conexionId = conexionId,
                    fechaNacimiento = normalizedBirthDate,
                    fechaFallecimiento = normalizedDeathDate,
                    notasPersonales = notes,
                    crearCofre = createCofre
                )
            ) {
                is ApiResult.Success -> {
                    // Avisamos éxito al formulario para que pueda cerrar,
                    // y refrescamos el árbol en segundo plano.
                    onFinished(true, null)

                    // Refresco del árbol (no bloquea la UI ni cambia el resultado de onFinished)
                    when (val treeResult = repository.miArbol().first()) {
                        is ApiResult.Success -> {
                            _state.update {
                                it.copy(
                                    nodes = treeResult.data,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = treeResult.message
                                )
                            }
                        }
                        is ApiResult.Loading -> {
                            // Ya gestionamos loading arriba, no hacemos nada especial acá
                        }
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    onFinished(false, result.message)
                }
                ApiResult.Loading -> {
                    // No debería llegar este caso aquí, ignoramos
                }
            }
        }
    }

    /**
     * Extrae todos los nombres de personas del árbol (recursivamente, incluyendo hijos)
     */
    fun getAllPersonNames(): List<String> {
        fun extractNames(nodes: List<TreeNode>): List<String> {
            return nodes.flatMap { node ->
                listOf(node.name) + extractNames(node.children)
            }
        }
        return extractNames(_state.value.nodes).distinct()
    }

    /**
     * Busca el ID de conexión a partir del nombre mostrado en el árbol.
     * Se apoya en el TreeNode.id (que viene del backend) y lo castea a Int si es posible.
     */
    private fun findConnectionIdByName(name: String): Int? {
        fun search(nodes: List<TreeNode>): String? {
            nodes.forEach { node ->
                if (node.name == name) return node.id
                val childResult = search(node.children)
                if (childResult != null) return childResult
            }
            return null
        }

        val idAsString = search(_state.value.nodes) ?: return null
        return idAsString.toIntOrNull()
    }

    /**
     * Convierte una fecha ingresada como dd/MM/yyyy a yyyy-MM-dd.
     * Si es null, vacía o inválida, devuelve null para que el backend la ignore.
     */
    private fun normalizeDate(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        val isoMatch = Regex("""^\d{4}-\d{2}-\d{2}$""")
        if (isoMatch.matches(trimmed)) return trimmed
        val parts = trimmed.split("/", "-", ".")
        if (parts.size != 3) return null

        val (d, m, y) = parts
        if (d.length !in 1..2 || m.length !in 1..2 || y.length != 4) return null

        val day = d.toIntOrNull() ?: return null
        val month = m.toIntOrNull() ?: return null
        val year = y.toIntOrNull() ?: return null

        if (day !in 1..31 || month !in 1..12 || year < 1900) return null

        return "%04d-%02d-%02d".format(year, month, day)
    }
}

