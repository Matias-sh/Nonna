package com.cocido.nonna.domain.models.genealogy

data class PersonNode(
    val id: String,
    val name: String,
    val relation: String,
    val cofreId: String? = null
)

data class ParentSet(
    val parent1Id: String,
    val parent2Id: String? = null
)

data class ChildLink(
    val childId: String,
    val lineageType: LineageType = LineageType.BIOLOGICAL
)

data class UnionNode(
    val id: String,
    val parents: ParentSet,
    val children: List<ChildLink>
)

data class GenealogyGraph(
    val personsById: Map<String, PersonNode>,
    val unionsById: Map<String, UnionNode>,
    val unionsByParentId: Map<String, List<String>>,
    val parentUnionIdsByChildId: Map<String, List<String>>
)

data class VisibleSubgraph(
    val focusPersonId: String,
    val personIds: Set<String>,
    val unionIds: Set<String>
)
