package com.cocido.nonna.data.mock

import com.cocido.nonna.ui.components.CofreRole
import com.cocido.nonna.ui.components.CofreUiModel
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.MemoryUiModel

// Mock User
data class MockUser(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val plan: UserPlan = UserPlan.Free,
    val cofresCount: Int = 0,
    val memoriesCount: Int = 0,
    val joinedDate: String = ""
)

enum class UserPlan {
    Free, Premium
}

val mockCurrentUser = MockUser(
    id = "user-1",
    name = "María García",
    email = "maria@email.com",
    plan = UserPlan.Free,
    cofresCount = 3,
    memoriesCount = 47,
    joinedDate = "Enero 2024"
)

// Mock Cofres
val mockCofres = listOf(
    CofreUiModel(
        id = "cofre-1",
        name = "Nonna Rosa",
        relation = "Abuela materna",
        photoCount = 24,
        audioCount = 8,
        textCount = 12,
        memberCount = 4,
        lastUpdated = "hace 2 días",
        coverImageUrl = null,
        isOwner = true
    ),
    CofreUiModel(
        id = "cofre-2",
        name = "Abuelo Juan",
        relation = "Abuelo paterno",
        photoCount = 15,
        audioCount = 3,
        textCount = 5,
        memberCount = 2,
        lastUpdated = "hace 1 semana",
        coverImageUrl = null,
        isOwner = true
    ),
    CofreUiModel(
        id = "cofre-3",
        name = "Tía Marta",
        relation = "Tía",
        photoCount = 8,
        audioCount = 0,
        textCount = 2,
        memberCount = 3,
        lastUpdated = "hace 3 semanas",
        coverImageUrl = null,
        isOwner = false
    )
)

// Mock Memories
val mockMemories = listOf(
    MemoryUiModel(
        id = "memory-1",
        type = MemoryType.Photo,
        title = "Cumpleaños de los 80",
        description = "La fiesta sorpresa que le organizamos a la Nonna",
        date = "15 Mar 2023",
        emotionalTag = EmotionalTag.Alegre,
        thumbnailUrl = null
    ),
    MemoryUiModel(
        id = "memory-2",
        type = MemoryType.Audio,
        title = "Historia del pueblo",
        description = "Cuando me contó cómo era vivir en el campo",
        date = "22 Ene 2024",
        emotionalTag = EmotionalTag.Nostalgico,
        duration = "3:45"
    ),
    MemoryUiModel(
        id = "memory-3",
        type = MemoryType.Text,
        title = "Receta de fideos caseros",
        description = "La receta secreta de la pasta de la Nonna",
        date = "5 Feb 2024",
        emotionalTag = EmotionalTag.Familiar
    ),
    MemoryUiModel(
        id = "memory-4",
        type = MemoryType.Photo,
        title = "Navidad 2022",
        description = "Toda la familia reunida",
        date = "25 Dic 2022",
        emotionalTag = EmotionalTag.Familiar,
        thumbnailUrl = null
    ),
    MemoryUiModel(
        id = "memory-5",
        type = MemoryType.Audio,
        title = "Canción de cuna",
        description = "La canción que siempre me cantaba",
        date = "10 Ago 2023",
        emotionalTag = EmotionalTag.Calmo,
        duration = "1:23"
    ),
    MemoryUiModel(
        id = "memory-6",
        type = MemoryType.Text,
        title = "Carta a los nietos",
        description = "Una carta que escribió para nosotros",
        date = "1 Ene 2024",
        emotionalTag = EmotionalTag.Nostalgico
    )
)

// Mock Family Members
data class FamilyMember(
    val id: String,
    val name: String,
    val email: String,
    val role: CofreRole,
    val avatarUrl: String? = null
)

val mockFamilyMembers = listOf(
    FamilyMember(
        id = "member-1",
        name = "María García",
        email = "maria@email.com",
        role = CofreRole.Creador
    ),
    FamilyMember(
        id = "member-2",
        name = "Carlos García",
        email = "carlos@email.com",
        role = CofreRole.Colaborador
    ),
    FamilyMember(
        id = "member-3",
        name = "Ana García",
        email = "ana@email.com",
        role = CofreRole.Colaborador
    ),
    FamilyMember(
        id = "member-4",
        name = "Rosa García",
        email = "rosa@email.com",
        role = CofreRole.Abuelo
    )
)

// Mock Family Tree
data class TreeNode(
    val id: String,
    val name: String,
    val relation: String,
    val cofreId: String? = null,
    val avatarUrl: String? = null,
    val birthDate: String? = null,
    val deathDate: String? = null,
    val children: List<TreeNode> = emptyList()
)

val mockFamilyTree = listOf(
    TreeNode(
        id = "node-1",
        name = "Nonna Rosa",
        relation = "Abuela",
        cofreId = "cofre-1",
        children = listOf(
            TreeNode(
                id = "node-2",
                name = "Mamá",
                relation = "Madre",
                children = listOf(
                    TreeNode(
                        id = "node-4",
                        name = "María",
                        relation = "Yo"
                    ),
                    TreeNode(
                        id = "node-5",
                        name = "Carlos",
                        relation = "Hermano"
                    )
                )
            ),
            TreeNode(
                id = "node-3",
                name = "Tía Marta",
                relation = "Tía",
                cofreId = "cofre-3"
            )
        )
    ),
    TreeNode(
        id = "node-6",
        name = "Abuelo Juan",
        relation = "Abuelo",
        cofreId = "cofre-2"
    )
)

// Relation options for creating cofre (display text)
val relationOptions = listOf(
    "Abuela",
    "Abuelo",
    "Bisabuela",
    "Bisabuelo",
    "Tía",
    "Tío",
    "Madre",
    "Padre",
    "Otro familiar"
)

/** Mapeo display → valor API para parentesco (backend espera ABUELA, ABUELO, etc.). */
val relationDisplayToApi: Map<String, String> = mapOf(
    "Abuela" to "ABUELA",
    "Abuelo" to "ABUELO",
    "Bisabuela" to "BISABUELA",
    "Bisabuelo" to "BISABUELO",
    "Tía" to "TIA",
    "Tío" to "TIO",
    "Madre" to "MADRE",
    "Padre" to "PADRE",
    "Otro familiar" to "OTRO"
)

fun relationToApi(display: String): String = relationDisplayToApi[display] ?: display.uppercase().replace(" ", "_")
