package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Person
import com.cocido.nonna.domain.model.Relation
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.data.repository.MemoryRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementación del caso de uso para obtener el grafo del árbol genealógico
 */
class GetGenealogyGraphUseCaseImpl @Inject constructor(
    private val memoryRepository: MemoryRepositoryImpl
) : GetGenealogyGraphUseCase {
    
    override fun invoke(vaultId: VaultId): Flow<Pair<List<Person>, List<Relation>>> = flow {
        try {
            // Por ahora, devolver datos dummy para demo
            val persons = getDummyPersons()
            val relations = getDummyRelations()
            emit(Pair(persons, relations))
        } catch (e: Exception) {
            throw e
        }
    }
    
    private fun getDummyPersons(): List<Person> {
        return listOf(
            Person(
                id = com.cocido.nonna.domain.model.PersonId("1"),
                fullName = "María González",
                birthDate = null,
                deathDate = null,
                avatarUrl = null,
                notes = "Abuela materna"
            ),
            Person(
                id = com.cocido.nonna.domain.model.PersonId("2"),
                fullName = "José González",
                birthDate = null,
                deathDate = null,
                avatarUrl = null,
                notes = "Abuelo materno"
            ),
            Person(
                id = com.cocido.nonna.domain.model.PersonId("3"),
                fullName = "Ana González",
                birthDate = null,
                deathDate = null,
                avatarUrl = null,
                notes = "Madre"
            ),
            Person(
                id = com.cocido.nonna.domain.model.PersonId("4"),
                fullName = "Carlos Pérez",
                birthDate = null,
                deathDate = null,
                avatarUrl = null,
                notes = "Padre"
            ),
            Person(
                id = com.cocido.nonna.domain.model.PersonId("5"),
                fullName = "Luis Pérez",
                birthDate = null,
                deathDate = null,
                avatarUrl = null,
                notes = "Usuario"
            )
        )
    }
    
    private fun getDummyRelations(): List<Relation> {
        return listOf(
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("1"),
                to = com.cocido.nonna.domain.model.PersonId("3"),
                type = com.cocido.nonna.domain.model.RelationType.PARENT
            ),
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("2"),
                to = com.cocido.nonna.domain.model.PersonId("3"),
                type = com.cocido.nonna.domain.model.RelationType.PARENT
            ),
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("3"),
                to = com.cocido.nonna.domain.model.PersonId("5"),
                type = com.cocido.nonna.domain.model.RelationType.PARENT
            ),
            Relation(
                from = com.cocido.nonna.domain.model.PersonId("4"),
                to = com.cocido.nonna.domain.model.PersonId("5"),
                type = com.cocido.nonna.domain.model.RelationType.PARENT
            )
        )
    }
}


