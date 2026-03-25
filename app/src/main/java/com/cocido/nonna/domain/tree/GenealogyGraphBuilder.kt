package com.cocido.nonna.domain.tree

import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.data.remote.dto.UnionArbolDto
import com.cocido.nonna.domain.models.genealogy.ChildLink
import com.cocido.nonna.domain.models.genealogy.GenealogyGraph
import com.cocido.nonna.domain.models.genealogy.ParentSet
import com.cocido.nonna.domain.models.genealogy.PersonNode
import com.cocido.nonna.domain.models.genealogy.UnionNode

object GenealogyGraphBuilder {

    fun build(
        roots: List<TreeNode>,
        unions: List<UnionArbolDto>
    ): GenealogyGraph {
        val personsById = linkedMapOf<String, PersonNode>()
        val childrenByParentId = mutableMapOf<String, MutableSet<String>>()

        fun walk(nodes: List<TreeNode>) {
            nodes.forEach { node ->
                personsById.putIfAbsent(
                    node.id,
                    PersonNode(
                        id = node.id,
                        name = node.name,
                        relation = node.relation,
                        cofreId = node.cofreId
                    )
                )
                node.children.forEach { child ->
                    childrenByParentId.getOrPut(node.id) { linkedSetOf() }.add(child.id)
                }
                walk(node.children)
            }
        }
        walk(roots)

        val unionsById = linkedMapOf<String, UnionNode>()
        val unionsByParentId = mutableMapOf<String, MutableList<String>>()
        val parentUnionIdsByChild = mutableMapOf<String, MutableList<String>>()

        unions.forEach { dto ->
            val p1 = dto.parent1 ?: return@forEach
            val unionId = dto.id.toString()
            val p1Id = p1.id
            val p2Id = dto.parent2?.id

            personsById.putIfAbsent(
                p1Id,
                PersonNode(
                    id = p1Id,
                    name = p1.displayName().ifBlank { "Sin nombre" },
                    relation = p1.displayRelation().ifBlank { "Familiar" },
                    cofreId = p1.cofreIdOrNull()
                )
            )
            p2Id?.let { p2 ->
                val p2Dto = dto.parent2
                if (p2Dto != null) {
                    personsById.putIfAbsent(
                        p2,
                        PersonNode(
                            id = p2,
                            name = p2Dto.displayName().ifBlank { "Sin nombre" },
                            relation = p2Dto.displayRelation().ifBlank { "Familiar" },
                            cofreId = p2Dto.cofreIdOrNull()
                        )
                    )
                }
            }

            val childIds = linkedSetOf<String>()
            dto.hijos.orEmpty().forEach { childDto ->
                childIds.add(childDto.id)
                personsById.putIfAbsent(
                    childDto.id,
                    PersonNode(
                        id = childDto.id,
                        name = childDto.displayName().ifBlank { "Sin nombre" },
                        relation = childDto.displayRelation().ifBlank { "Familiar" },
                        cofreId = childDto.cofreIdOrNull()
                    )
                )
            }
            childrenByParentId[p1Id].orEmpty().forEach { childIds.add(it) }
            if (p2Id != null) childrenByParentId[p2Id].orEmpty().forEach { childIds.add(it) }

            val unionNode = UnionNode(
                id = unionId,
                parents = ParentSet(parent1Id = p1Id, parent2Id = p2Id),
                children = childIds.map { ChildLink(it) }
            )
            unionsById[unionId] = unionNode
            unionsByParentId.getOrPut(p1Id) { mutableListOf() }.add(unionId)
            p2Id?.let { unionsByParentId.getOrPut(it) { mutableListOf() }.add(unionId) }
            childIds.forEach { childId ->
                parentUnionIdsByChild.getOrPut(childId) { mutableListOf() }.add(unionId)
            }
        }

        return GenealogyGraph(
            personsById = personsById,
            unionsById = unionsById,
            unionsByParentId = unionsByParentId.mapValues { it.value.distinct() },
            parentUnionIdsByChildId = parentUnionIdsByChild.mapValues { it.value.distinct() }
        )
    }
}
