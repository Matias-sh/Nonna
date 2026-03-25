package com.cocido.nonna.domain.tree.layout

import androidx.compose.ui.geometry.Offset

data class LayoutPersonNode(
    val personId: String,
    val x: Float,
    val y: Float
)

data class LayoutUnionNode(
    val unionId: String,
    val x: Float,
    val y: Float,
    val parent1Id: String,
    val parent2Id: String?
)

data class LayoutConnector(
    val from: Offset,
    val to: Offset
)

data class GenealogyLayoutResult(
    val people: List<LayoutPersonNode>,
    val unions: List<LayoutUnionNode>,
    val connectors: List<LayoutConnector>,
    val canvasWidth: Float,
    val canvasHeight: Float
)

data class GenealogyLayoutConfig(
    val personWidth: Float = 110f,
    val personHeight: Float = 140f,
    val personCircleDiameter: Float = 80f,
    val horizontalGap: Float = 56f,
    val verticalGap: Float = 120f,
    val margin: Float = 48f
)
