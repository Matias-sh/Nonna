package com.cocido.nonna.domain.tree

import com.cocido.nonna.domain.models.genealogy.GenealogyGraph
import com.cocido.nonna.domain.models.genealogy.VisibleSubgraph
import java.util.ArrayDeque

object VisibleSubgraphProjector {

    data class Config(
        val ancestorsDepth: Int = 3,
        val descendantsDepth: Int = 3,
        val includeCollaterals: Boolean = true
    )

    fun project(
        graph: GenealogyGraph,
        focusPersonId: String?,
        config: Config = Config()
    ): VisibleSubgraph {
        val focus = resolveFocus(graph, focusPersonId)
        val visiblePersons = linkedSetOf<String>()
        val visibleUnions = linkedSetOf<String>()

        visiblePersons += focus

        // Ancestors: subir por uniones parentales y sumar padres
        val upQ = ArrayDeque<Pair<String, Int>>()
        upQ.add(focus to 0)
        val visitedUp = mutableSetOf<String>()
        while (upQ.isNotEmpty()) {
            val (personId, depth) = upQ.removeFirst()
            if (!visitedUp.add("$personId:$depth")) continue
            if (depth >= config.ancestorsDepth) continue
            graph.parentUnionIdsByChildId[personId].orEmpty().forEach { unionId ->
                val union = graph.unionsById[unionId] ?: return@forEach
                visibleUnions += unionId
                visiblePersons += union.parents.parent1Id
                upQ.add(union.parents.parent1Id to depth + 1)
                union.parents.parent2Id?.let {
                    visiblePersons += it
                    upQ.add(it to depth + 1)
                }
            }
        }

        // Descendants: bajar por uniones donde la persona es padre/madre
        val downQ = ArrayDeque<Pair<String, Int>>()
        downQ.add(focus to 0)
        val visitedDown = mutableSetOf<String>()
        while (downQ.isNotEmpty()) {
            val (personId, depth) = downQ.removeFirst()
            if (!visitedDown.add("$personId:$depth")) continue
            if (depth >= config.descendantsDepth) continue
            graph.unionsByParentId[personId].orEmpty().forEach { unionId ->
                val union = graph.unionsById[unionId] ?: return@forEach
                visibleUnions += unionId
                visiblePersons += union.parents.parent1Id
                union.parents.parent2Id?.let { visiblePersons += it }
                union.children.forEach { child ->
                    visiblePersons += child.childId
                    downQ.add(child.childId to depth + 1)
                }
            }
        }

        if (config.includeCollaterals) {
            val currentPersons = visiblePersons.toList()
            currentPersons.forEach { personId ->
                graph.parentUnionIdsByChildId[personId].orEmpty().forEach { unionId ->
                    val union = graph.unionsById[unionId] ?: return@forEach
                    visibleUnions += unionId
                    union.children.forEach { visiblePersons += it.childId }
                }
            }
        }

        return VisibleSubgraph(
            focusPersonId = focus,
            personIds = visiblePersons,
            unionIds = visibleUnions
        )
    }

    private fun resolveFocus(graph: GenealogyGraph, providedFocus: String?): String {
        if (!providedFocus.isNullOrBlank() && graph.personsById.containsKey(providedFocus)) return providedFocus
        val yo = graph.personsById.values.firstOrNull { it.relation.trim().uppercase() == "YO" }?.id
        if (yo != null) return yo
        return graph.personsById.keys.firstOrNull().orEmpty()
    }
}
