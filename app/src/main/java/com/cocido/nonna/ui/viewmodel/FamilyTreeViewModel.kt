package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.data.remote.dto.UnionArbolDto
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
    val errorMessage: String? = null,
    val focusPersonId: String? = null
)

@HiltViewModel
class FamilyTreeViewModel @Inject constructor(
    private val repository: ArbolFamiliarRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FamilyTreeUiState())
    val state: StateFlow<FamilyTreeUiState> = _state.asStateFlow()

    private var isLoadingInProgress = false
    private val _uniones = MutableStateFlow<List<UnionArbolDto>>(emptyList())
    val uniones: StateFlow<List<UnionArbolDto>> = _uniones.asStateFlow()

    init {
        // Cargamos el árbol una sola vez cuando se crea el ViewModel
        load()
        loadUniones()
    }

    fun load(forceRefresh: Boolean = false) {
        // Evitar múltiples llamadas simultáneas
        if (isLoadingInProgress) return

        // Solo cargar si no hay datos y no está cargando
        if (!forceRefresh && (_state.value.isLoading || (_state.value.nodes.isNotEmpty() && _state.value.errorMessage == null))) {
            return
        }

        isLoadingInProgress = true
        viewModelScope.launch {
            repository.miArbol().collect { result ->
                when (result) {
                    is ApiResult.Loading -> _state.update { it.copy(isLoading = true, errorMessage = null) }
                    is ApiResult.Success -> {
                        val inferredFocus = result.data.firstOrNull { it.relation.trim().uppercase() == "YO" }?.id
                        _state.update {
                            it.copy(
                                nodes = result.data,
                                isLoading = false,
                                errorMessage = null,
                                focusPersonId = it.focusPersonId ?: inferredFocus ?: result.data.firstOrNull()?.id
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

    fun setFocusPerson(personId: String?) {
        _state.update { it.copy(focusPersonId = personId) }
    }

    fun loadUniones() {
        viewModelScope.launch {
            when (val result = repository.listarUniones()) {
                is ApiResult.Success -> _uniones.value = result.data
                is ApiResult.Error -> _state.update { it.copy(errorMessage = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    /**
     * Crear una nueva persona en el árbol.
     * Se encarga de mapear el nombre seleccionado a un ID de conexión válido
     * y refrescar el árbol al finalizar.
     */
    fun addPerson(
        fullName: String,
        selectedRelationName: String?,
        selectedParentReferenceName: String?,
        birthDate: String?,
        deathDate: String?,
        notes: String?,
        createCofre: Boolean,
        unionPadresId: Int? = null,
        parentescoConmigo: String? = null,
        onFinished: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            // Marcamos loading en el árbol mientras se crea la persona
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            // IDs de referencia opcionales
            val partnerId: Int? = selectedRelationName?.let { relationName ->
                findConnectionIdByName(relationName)
            }
            val parentReferenceId: Int? = selectedParentReferenceName?.let { relationName ->
                findConnectionIdByName(relationName)
            }

            // Normalizamos fechas al formato que espera el backend (yyyy-MM-dd)
            val normalizedBirthDate = normalizeDate(birthDate)
            val normalizedDeathDate = normalizeDate(deathDate)

            when (
                val result = repository.crearPersona(
                    nombreCompleto = fullName,
                    unionPadresId = unionPadresId,
                    parentescoConmigo = mapParentescoConmigoToBackend(parentescoConmigo),
                    fechaNacimiento = normalizedBirthDate,
                    fechaFallecimiento = normalizedDeathDate,
                    notasPersonales = notes,
                    crearCofre = createCofre
                )
            ) {
                is ApiResult.Success -> {
                    val nuevaPersonaId = result.data.id.toIntOrNull()

                    // Si se seleccionó progenitor y no se eligió unión explícita,
                    // creamos una unión de un solo padre/madre y vinculamos a la persona como hija.
                    if (unionPadresId == null && parentReferenceId != null && nuevaPersonaId != null) {
                        when (val unionResult = repository.crearUnion(parent1Id = parentReferenceId, parent2Id = null)) {
                            is ApiResult.Success -> {
                                repository.actualizarPersona(
                                    id = nuevaPersonaId.toString(),
                                    nombreCompleto = fullName,
                                    unionPadresId = unionResult.data.id,
                                    parentescoConmigo = mapParentescoConmigoToBackend(parentescoConmigo),
                                    fechaNacimiento = normalizedBirthDate,
                                    fechaFallecimiento = normalizedDeathDate,
                                    notasPersonales = notes
                                )
                            }
                            else -> Unit
                        }
                    }

                    // Si se seleccionó pareja, creamos unión pareja en segundo plano.
                    if (partnerId != null && nuevaPersonaId != null && partnerId != parentReferenceId) {
                        repository.crearUnion(
                            parent1Id = partnerId,
                            parent2Id = nuevaPersonaId
                        )
                    }

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

    fun createUnion(
        parent1Id: Int,
        parent2Id: Int? = null,
        onFinished: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repository.crearUnion(parent1Id, parent2Id)) {
                is ApiResult.Success -> {
                    loadUniones()
                    load(forceRefresh = true)
                    onFinished(true, null)
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message) }
                    onFinished(false, result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun deleteUnion(
        unionId: Int,
        onFinished: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repository.eliminarUnion(unionId.toString())) {
                is ApiResult.Success -> {
                    loadUniones()
                    load(forceRefresh = true)
                    onFinished(true, null)
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message) }
                    onFinished(false, result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun updatePerson(
        personId: String,
        fullName: String,
        parentescoConmigo: String?,
        onFinished: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (
                val result = repository.actualizarPersona(
                    id = personId,
                    nombreCompleto = fullName,
                    unionPadresId = null,
                    parentescoConmigo = mapParentescoConmigoToBackend(parentescoConmigo),
                    fechaNacimiento = null,
                    fechaFallecimiento = null,
                    notasPersonales = null
                )
            ) {
                is ApiResult.Success -> {
                    load(forceRefresh = true)
                    loadUniones()
                    onFinished(true, null)
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message) }
                    onFinished(false, result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun deletePerson(
        personId: String,
        onFinished: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repository.eliminarPersona(personId)) {
                is ApiResult.Success -> {
                    load(forceRefresh = true)
                    loadUniones()
                    onFinished(true, null)
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message) }
                    onFinished(false, result.message)
                }
                ApiResult.Loading -> Unit
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
        val parts = raw.split("/", "-", ".")
        if (parts.size != 3) return null

        val (d, m, y) = parts
        if (d.length !in 1..2 || m.length !in 1..2 || y.length != 4) return null

        val day = d.toIntOrNull() ?: return null
        val month = m.toIntOrNull() ?: return null
        val year = y.toIntOrNull() ?: return null

        if (day !in 1..31 || month !in 1..12 || year < 1900) return null

        return "%04d-%02d-%02d".format(year, month, day)
    }

    /**
     * El backend valida un enum específico (ej: PAPA/MAMA en lugar de PADRE/MADRE).
     * Mantenemos la UI amigable y traducimos antes de enviar.
     */
    private fun mapParentescoConmigoToBackend(raw: String?): String? {
        val value = raw?.trim()?.uppercase() ?: return null
        return when (value) {
            "PADRE" -> "PAPA"
            "MADRE" -> "MAMA"
            else -> value
        }
    }
}

