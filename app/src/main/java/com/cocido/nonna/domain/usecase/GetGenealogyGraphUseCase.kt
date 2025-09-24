package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Person
import com.cocido.nonna.domain.model.Relation
import com.cocido.nonna.domain.model.VaultId
import kotlinx.coroutines.flow.Flow

/**
 * Use case para obtener el grafo del árbol genealógico
 */
interface GetGenealogyGraphUseCase {
    operator fun invoke(vaultId: VaultId): Flow<Pair<List<Person>, List<Relation>>>
}





