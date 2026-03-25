package com.cocido.nonna.domain.tree.layout

import androidx.compose.ui.geometry.Offset
import com.cocido.nonna.domain.models.genealogy.GenealogyGraph
import com.cocido.nonna.domain.models.genealogy.VisibleSubgraph
import java.util.TreeMap

object GenealogyLayoutEngine {

    fun compute(
        graph: GenealogyGraph,
        subgraph: VisibleSubgraph,
        config: GenealogyLayoutConfig = GenealogyLayoutConfig()
    ): GenealogyLayoutResult {
        val visiblePersons = subgraph.personIds.filter { graph.personsById.containsKey(it) }
        if (visiblePersons.isEmpty()) {
            return GenealogyLayoutResult(emptyList(), emptyList(), emptyList(), 1200f, 900f)
        }

        val parentUnionIdsByChild = graph.parentUnionIdsByChildId
        val personGeneration = mutableMapOf<String, Int>()
        val visiting = mutableSetOf<String>()

        fun generation(personId: String): Int {
            personGeneration[personId]?.let { return it }
            if (!visiting.add(personId)) return 0
            val parentUnions = parentUnionIdsByChild[personId].orEmpty().filter { it in subgraph.unionIds }
            val g = if (parentUnions.isEmpty()) 0 else {
                val parentMax = parentUnions.maxOf { unionId ->
                    val union = graph.unionsById[unionId]
                    if (union == null) 0 else {
                        val g1 = generation(union.parents.parent1Id)
                        val g2 = union.parents.parent2Id?.let { generation(it) } ?: g1
                        maxOf(g1, g2)
                    }
                }
                parentMax + 1
            }
            visiting.remove(personId)
            personGeneration[personId] = g
            return g
        }

        visiblePersons.forEach { generation(it) }
        val byGen = TreeMap<Int, MutableList<String>>()
        visiblePersons.forEach { personId ->
            byGen.getOrPut(personGeneration[personId] ?: 0) { mutableListOf() }.add(personId)
        }

        val xByPerson = mutableMapOf<String, Float>()
        byGen[0].orEmpty().forEachIndexed { idx, personId ->
            xByPerson[personId] = idx * (config.personWidth + config.horizontalGap)
        }
        byGen.filterKeys { it > 0 }.forEach { (_, people) ->
            var cursor = (xByPerson.values.maxOrNull() ?: 0f) + (config.personWidth + config.horizontalGap)
            people.sortBy { graph.personsById[it]?.name.orEmpty() }
            people.forEach { personId ->
                val parentUnion = parentUnionIdsByChild[personId]
                    .orEmpty()
                    .firstOrNull { it in subgraph.unionIds }
                    ?.let { graph.unionsById[it] }
                val target = if (parentUnion != null) {
                    val p1x = xByPerson[parentUnion.parents.parent1Id]
                    val p2x = parentUnion.parents.parent2Id?.let { xByPerson[it] }
                    when {
                        p1x != null && p2x != null -> (p1x + p2x) / 2f
                        p1x != null -> p1x
                        else -> null
                    }
                } else null
                xByPerson[personId] = target ?: cursor.also { cursor += config.personWidth + config.horizontalGap }
            }
        }

        byGen.forEach { (_, people) ->
            people.sortBy { xByPerson[it] ?: 0f }
            var prev = Float.NEGATIVE_INFINITY
            val minGap = config.personWidth + 24f
            people.forEach { personId ->
                val x = xByPerson[personId] ?: 0f
                val adjusted = if (x < prev + minGap) prev + minGap else x
                xByPerson[personId] = adjusted
                prev = adjusted
            }
        }

        val minX = xByPerson.values.minOrNull() ?: 0f
        val maxX = xByPerson.values.maxOrNull() ?: 0f
        val maxGen = byGen.keys.maxOrNull() ?: 0

        val people = visiblePersons.map { personId ->
            val x = (xByPerson[personId] ?: 0f) - minX + config.margin
            val y = (personGeneration[personId] ?: 0) * (config.personHeight + config.verticalGap) + config.margin
            LayoutPersonNode(personId = personId, x = x, y = y)
        }
        val personById = people.associateBy { it.personId }

        val unions = subgraph.unionIds.mapNotNull { unionId ->
            val union = graph.unionsById[unionId] ?: return@mapNotNull null
            val p1 = personById[union.parents.parent1Id] ?: return@mapNotNull null
            val p2 = union.parents.parent2Id?.let { personById[it] }
            val y = p1.y + config.personCircleDiameter / 2f
            val x = if (p2 != null) (p1.x + p2.x) / 2f else p1.x
            LayoutUnionNode(
                unionId = unionId,
                x = x,
                y = y,
                parent1Id = union.parents.parent1Id,
                parent2Id = union.parents.parent2Id
            )
        }

        val connectors = mutableListOf<LayoutConnector>()
        unions.forEach { unionLayout ->
            val p1 = personById[unionLayout.parent1Id] ?: return@forEach
            val p2 = unionLayout.parent2Id?.let { personById[it] }
            val p1Center = Offset(p1.x + config.personWidth / 2f, p1.y + config.personCircleDiameter / 2f)
            if (p2 != null) {
                val p2Center = Offset(p2.x + config.personWidth / 2f, p2.y + config.personCircleDiameter / 2f)
                connectors += LayoutConnector(p1Center, p2Center)
            }

            val union = graph.unionsById[unionLayout.unionId] ?: return@forEach
            val visibleChildren = union.children.map { it.childId }.filter { it in subgraph.personIds }
            val childPositions = visibleChildren.mapNotNull { personById[it] }
            if (childPositions.isEmpty()) return@forEach

            val unionCenter = Offset(unionLayout.x + config.personWidth / 2f, unionLayout.y)
            if (childPositions.size == 1) {
                val child = childPositions.first()
                val childTop = Offset(child.x + config.personWidth / 2f, child.y)
                connectors += LayoutConnector(unionCenter, childTop)
            } else {
                val childrenY = childPositions.minOf { it.y } - 14f
                val minChildX = childPositions.minOf { it.x + config.personWidth / 2f }
                val maxChildX = childPositions.maxOf { it.x + config.personWidth / 2f }
                val trunkBottom = Offset(unionCenter.x, childrenY)
                connectors += LayoutConnector(unionCenter, trunkBottom)
                connectors += LayoutConnector(Offset(minChildX, childrenY), Offset(maxChildX, childrenY))
                childPositions.forEach { child ->
                    val childTop = Offset(child.x + config.personWidth / 2f, child.y)
                    connectors += LayoutConnector(Offset(childTop.x, childrenY), childTop)
                }
            }
        }

        val width = ((maxX - minX) + config.personWidth + config.margin * 2f).coerceAtLeast(1200f)
        val height = ((maxGen + 1) * (config.personHeight + config.verticalGap) + config.margin * 2f).coerceAtLeast(900f)

        return GenealogyLayoutResult(
            people = people,
            unions = unions,
            connectors = connectors,
            canvasWidth = width,
            canvasHeight = height
        )
    }
}
