package com.cocido.nonna.ui.screens.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.components.SimpleHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart

@Composable
fun FamilyTreeScreen(
    onTabSelected: (NonnaTab) -> Unit,
    onNodeClick: (nodeId: String, cofreId: String?) -> Unit,
    onAddNode: () -> Unit,
    nodes: List<TreeNode>,
    isLoading: Boolean
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    
    AppShell(
        currentTab = NonnaTab.Arbol,
        onTabSelected = onTabSelected
    ) {
        if (isLoading) {
            // Mientras cargamos, mostramos el empty con CTA (mejor que una pantalla en blanco)
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SimpleHeader(
                    title = "Árbol Familiar",
                    subtitle = "Conectá a tu familia y sus historias"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(NonnaDimens.screenPaddingHorizontal),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateWithButton(
                        icon = Icons.Outlined.AccountTree,
                        title = "Cargando tu árbol...",
                        description = "Aguantá un segundo mientras traemos a tu familia",
                        buttonText = "Crear primer cofre",
                        onButtonClick = onAddNode
                    )
                }
            }
        } else if (nodes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SimpleHeader(
                    title = "Árbol Familiar",
                    subtitle = "Conectá a tu familia y sus historias"
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(NonnaDimens.screenPaddingHorizontal),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateWithButton(
                        icon = Icons.Outlined.AccountTree,
                        title = "Tu árbol empieza aquí",
                        description = "Creá un cofre y sumá a tu familia para empezar a construir el árbol genealógico",
                        buttonText = "Crear primer cofre",
                        onButtonClick = onAddNode
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
                            .padding(top = NonnaDimens.spacing8)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Árbol Familiar",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Conectá a tu familia y sus historias",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Zoom controls - compacto
                            ZoomControls(
                                zoom = zoom,
                                onZoomChange = { zoom = it }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Tree visualization
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = NonnaDimens.screenPaddingHorizontal),
                        shape = NonnaCorners.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                                .padding(NonnaDimens.cardPaddingLarge),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, _, zoomChange, _ ->
                                            zoom = (zoom * zoomChange).coerceIn(0.5f, 2f)
                                        }
                                    }
                                    .graphicsLayer {
                                        scaleX = zoom
                                        scaleY = zoom
                                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                                    }
                            ) {
                                nodes.forEach { rootNode ->
                                    TreeNodeView(
                                        node = rootNode,
                                        onNodeClick = onNodeClick
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Info note
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
                            .padding(bottom = NonnaDimens.spacing8)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = NonnaCorners.Medium
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Tocá una persona para ver su cofre (si tiene uno) o crear uno nuevo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // FABs - posicionados sobre la nota
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = NonnaDimens.screenPaddingHorizontal)
                        .padding(bottom = 72.dp), // Espacio para la nota
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { /* TODO: Settings */ },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Administrar árbol"
                        )
                    }
                    
                    FloatingActionButton(
                        onClick = onAddNode,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar persona"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomControls(
    zoom: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = NonnaCorners.Medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = { onZoomChange((zoom - 0.1f).coerceAtLeast(0.5f)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom out",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "${(zoom * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            IconButton(
                onClick = { onZoomChange((zoom + 0.1f).coerceAtMost(2f)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom in",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun TreeNodeView(
    node: TreeNode,
    onNodeClick: (nodeId: String, cofreId: String?) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Node button
        Surface(
            onClick = { onNodeClick(node.id, node.cofreId) },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
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
                    text = node.name.first().toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Badge if has cofre
                if (node.cofreId != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
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
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Name and relation
        Text(
            text = node.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = node.relation,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Children
        if (node.children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            
            // Connector line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Children row
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                node.children.forEach { child ->
                    TreeNodeView(
                        node = child,
                        onNodeClick = onNodeClick
                    )
                }
            }
        }
    }
}
