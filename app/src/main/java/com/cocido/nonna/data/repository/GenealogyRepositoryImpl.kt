package com.cocido.nonna.data.repository

import com.cocido.nonna.data.local.dao.PersonDao
import com.cocido.nonna.data.local.dao.RelationDao
import com.cocido.nonna.data.local.entity.PersonEntity
import com.cocido.nonna.data.local.entity.RelationEntity
import com.cocido.nonna.data.remote.api.PersonApiService
import com.cocido.nonna.data.remote.dto.PersonDto
import com.cocido.nonna.data.remote.dto.RelationDto
import com.cocido.nonna.domain.model.Person
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.Relation
import com.cocido.nonna.domain.model.RelationType
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.GenealogyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de genealogía
 */
@Singleton
class GenealogyRepositoryImpl @Inject constructor(
    private val personApiService: PersonApiService,
    private val personDao: PersonDao,
    private val relationDao: RelationDao
) : GenealogyRepository {
    
    override fun getPersons(vaultId: VaultId): Flow<List<Person>> {
        return personDao.getAllPersons().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getPerson(personId: PersonId): Person? {
        val entity = personDao.getPersonById(personId.value)
        return entity?.toDomainModel()
    }
    
    override suspend fun createPerson(person: Person): Result<Person> {
        return try {
            val dto = person.toDto()
            val response = personApiService.createPerson(dto)
            val domainPerson = response.toDomainModel()
            
            // Guardar en base de datos local
            personDao.insertPerson(domainPerson.toEntity())
            
            Result.success(domainPerson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePerson(person: Person): Result<Person> {
        return try {
            val dto = person.toDto()
            val response = personApiService.updatePerson(person.id.value, dto)
            val domainPerson = response.toDomainModel()
            
            // Actualizar en base de datos local
            personDao.updatePerson(domainPerson.toEntity())
            
            Result.success(domainPerson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePerson(personId: PersonId): Result<Unit> {
        return try {
            personApiService.deletePerson(personId.value)
            
            // Eliminar de base de datos local
            personDao.deletePersonById(personId.value)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getRelations(vaultId: VaultId): Flow<List<Relation>> {
        return relationDao.getAllRelations().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun createRelation(relation: Relation): Result<Relation> {
        return try {
            val dto = relation.toDto()
            val response = personApiService.createRelation(dto)
            val domainRelation = response.toDomainModel()
            
            // Guardar en base de datos local
            relationDao.insertRelation(domainRelation.toEntity())
            
            Result.success(domainRelation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateRelation(relation: Relation): Result<Relation> {
        return try {
            val dto = relation.toDto()
            val response = personApiService.updateRelation(relation.id, dto)
            val domainRelation = response.toDomainModel()
            
            // Actualizar en base de datos local
            relationDao.updateRelation(domainRelation.toEntity())
            
            Result.success(domainRelation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteRelation(relation: Relation): Result<Unit> {
        return try {
            personApiService.deleteRelation(relation.id)
            
            // Eliminar de base de datos local
            relationDao.deleteRelation(relation.from.value, relation.to.value)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getGenealogyGraph(vaultId: VaultId): Flow<Pair<List<Person>, List<Relation>>> {
        return combine(
            getPersons(vaultId),
            getRelations(vaultId)
        ) { persons, relations ->
            Pair(persons, relations)
        }
    }
    
    // Extensiones para convertir entre DTOs, Entities y Domain Models
    
    private fun PersonEntity.toDomainModel(): Person {
        return Person(
            id = PersonId(id),
            fullName = fullName,
            birthDate = birthDate,
            deathDate = deathDate,
            avatarUrl = avatarUrl,
            notes = notes
        )
    }
    
    private fun Person.toEntity(): PersonEntity {
        return PersonEntity(
            id = id.value,
            fullName = fullName,
            birthDate = birthDate,
            deathDate = deathDate,
            avatarUrl = avatarUrl,
            notes = notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private fun Person.toDto(): PersonDto {
        return PersonDto(
            id = id.value,
            firstName = fullName.split(" ").firstOrNull() ?: "",
            lastName = fullName.split(" ").drop(1).joinToString(" "),
            middleName = "",
            fullName = fullName,
            birthDate = birthDate?.toString(),
            deathDate = deathDate?.toString(),
            birthPlace = "",
            deathPlace = "",
            email = "",
            phone = "",
            address = "",
            photo = avatarUrl,
            documents = emptyList(),
            occupation = "",
            notes = notes ?: "",
            isLiving = true,
            age = null,
            vault = "default",
            vaultName = "Default Vault",
            createdBy = "user_1",
            createdByName = "Usuario",
            memoriesCount = 0,
            createdAt = System.currentTimeMillis().toString(),
            updatedAt = System.currentTimeMillis().toString()
        )
    }
    
    private fun PersonDto.toDomainModel(): Person {
        return Person(
            id = PersonId(id),
            fullName = fullName,
            birthDate = birthDate?.let { java.time.LocalDate.parse(it).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() },
            deathDate = deathDate?.let { java.time.LocalDate.parse(it).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() },
            avatarUrl = photo,
            notes = notes
        )
    }
    
    private fun RelationEntity.toDomainModel(): Relation {
        return Relation(
            id = id.toString(),
            from = PersonId(fromPersonId),
            to = PersonId(toPersonId),
            type = RelationType.valueOf(type)
        )
    }
    
    private fun Relation.toEntity(): RelationEntity {
        return RelationEntity(
            id = id.toLongOrNull() ?: 0L,
            fromPersonId = from.value,
            toPersonId = to.value,
            type = type.name,
            createdAt = System.currentTimeMillis()
        )
    }
    
    private fun Relation.toDto(): RelationDto {
        return RelationDto(
            id = id,
            person1 = from.value,
            person1Name = "", // TODO: Obtener nombre real
            person2 = to.value,
            person2Name = "", // TODO: Obtener nombre real
            relationType = type.name,
            startDate = null,
            endDate = null,
            notes = "",
            createdAt = System.currentTimeMillis().toString(),
            updatedAt = System.currentTimeMillis().toString()
        )
    }
    
    private fun RelationDto.toDomainModel(): Relation {
        return Relation(
            id = id,
            from = PersonId(person1),
            to = PersonId(person2),
            type = RelationType.valueOf(relationType)
        )
    }
}
