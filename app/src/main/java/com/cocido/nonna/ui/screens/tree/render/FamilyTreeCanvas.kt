package com.cocido.nonna.ui.screens.tree.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocido.nonna.domain.models.genealogy.PersonNode
import com.cocido.nonna.domain.tree.layout.GenealogyLayoutConfig
import com.cocido.nonna.domain.tree.layout.GenealogyLayoutResult
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart

@Composable
fun FamilyTreeCanvas(
    layout: GenealogyLayoutResult,
    peopleById: Map<String, PersonNode>,
    onNodeTap: (nodeId: String, cofreId: String?) -> Unit,
    onNodeLongPress: (nodeId: String) -> Unit,
    config: GenealogyLayoutConfig = GenealogyLayoutConfig()
) {
    val connectorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    Box(
        modifier = Modifier
            .requiredWidth(layout.canvasWidth.dp)
            .requiredHeight(layout.canvasHeight.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            layout.connectors.forEach { c ->
                drawLine(
                    color = connectorColor,
                    start = c.from,
                    end = c.to,
                    strokeWidth = 3f
                )
            }
        }

        layout.people.forEach { node ->
            val person = peopleById[node.personId] ?: return@forEach
            Box(modifier = Modifier.offset(node.x.dp, node.y.dp)) {
                FamilyPersonCard(
                    person = person,
                    onNodeTap = onNodeTap,
                    onNodeLongPress = onNodeLongPress,
                    config = config
                )
            }
        }
    }
}

@Composable
private fun FamilyPersonCard(
    person: PersonNode,
    onNodeTap: (nodeId: String, cofreId: String?) -> Unit,
    onNodeLongPress: (nodeId: String) -> Unit,
    config: GenealogyLayoutConfig
) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(config.personWidth.dp)
    ) {
        Box {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.pointerInput(person.id, person.cofreId) {
                    detectTapGestures(
                        onTap = { onNodeTap(person.id, person.cofreId) },
                        onLongPress = { onNodeLongPress(person.id) }
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(config.personCircleDiameter.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PrimaryGradientStart.copy(alpha = 0.2f),
                                    PrimaryGradientEnd.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = person.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (!person.cofreId.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = "Tiene cofre",
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = person.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = person.relation,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
