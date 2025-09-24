package com.cocido.nonna.domain.usecase

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.repository.MemoryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para CreateMemoryUseCase siguiendo mejores prácticas de testing
 */
class CreateMemoryUseCaseTest {
    
    private lateinit var memoryRepository: MemoryRepository
    private lateinit var createMemoryUseCase: CreateMemoryUseCase
    
    @Before
    fun setup() {
        memoryRepository = mockk()
        createMemoryUseCase = CreateMemoryUseCase(memoryRepository)
    }
    
    @Test
    fun `invoke should return success when memory is created successfully`() = runTest {
        // Given
        val memory = createTestMemory()
        coEvery { memoryRepository.saveMemory(memory) } returns Unit
        
        // When
        val result = createMemoryUseCase(memory)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(memory.id, result.getOrNull())
    }
    
    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val memory = createTestMemory()
        val exception = RuntimeException("Database error")
        coEvery { memoryRepository.saveMemory(memory) } throws exception
        
        // When
        val result = createMemoryUseCase(memory)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
    
    @Test
    fun `invoke should handle null memory gracefully`() = runTest {
        // Given
        val memory = createTestMemory()
        coEvery { memoryRepository.saveMemory(memory) } throws IllegalArgumentException("Memory cannot be null")
        
        // When
        val result = createMemoryUseCase(memory)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    private fun createTestMemory(): Memory {
        return Memory(
            id = MemoryId("test-memory-id"),
            vaultId = VaultId("test-vault-id"),
            title = "Test Memory",
            type = MemoryType.PHOTO_ONLY,
            photoLocalPath = null,
            photoRemoteUrl = null,
            audioLocalPath = null,
            audioRemoteUrl = null,
            hasTranscript = false,
            transcript = null,
            people = emptyList(),
            tags = emptyList(),
            dateTaken = System.currentTimeMillis(),
            location = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}

