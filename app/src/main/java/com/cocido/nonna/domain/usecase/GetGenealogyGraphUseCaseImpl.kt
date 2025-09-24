package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Person
import com.cocido.nonna.domain.model.Relation
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.GenealogyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del caso de uso para obtener el grafo del árbol genealógico
 */
class GetGenealogyGraphUseCaseImpl @Inject constructor(
    private val genealogyRepository: GenealogyRepository
) : GetGenealogyGraphUseCase {
    
    override fun invoke(vaultId: VaultId): Flow<Pair<List<Person>, List<Relation>>> {
        return genealogyRepository.getGenealogyGraph(vaultId)
    }
}


