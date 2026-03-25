package com.cocido.nonna.ui.screens.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.data.remote.dto.UnionArbolDto
import com.cocido.nonna.domain.tree.GenealogyGraphBuilder
import com.cocido.nonna.domain.tree.VisibleSubgraphProjector
import com.cocido.nonna.domain.tree.layout.GenealogyLayoutEngine
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.components.SimpleHeader
import com.cocido.nonna.ui.screens.tree.render.FamilyTreeCanvas
import com.cocido.nonna.ui.screens.tree.viewport.rememberTreeViewportState
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaDimens
import kotlinx.coroutines.launch

@Composable
fun FamilyTreeScreen(
    onTabSelected: (NonnaTab) -> Unit,
    onNodeClick: (nodeId: String, cofreId: String?) -> Unit,
    onAddNode: () -> Unit,
    onCreateUnion: (parent1Id: Int, parent2Id: Int?, onResult: (Boolean, String?) -> Unit) -> Unit,
    onDeleteUnion: (unionId: Int, onResult: (Boolean, String?) -> Unit) -> Unit,
    onEditPerson: (personId: String, fullName: String, parentescoConmigo: String?, onResult: (Boolean, String?) -> Unit) -> Unit,
    onDeletePerson: (personId: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    nodes: List<TreeNode>,
    uniones: List<UnionArbolDto>,
    focusPersonId: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onErrorConsumed: () -> Unit
) {
    val viewport = rememberTreeViewportState()
    var showUnionManager by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedNodeForActions by remember { mutableStateOf<TreeNode?>(null) }
    var nodePendingDelete by remember { mutableStateOf<TreeNode?>(null) }
    var nodePendingEdit by remember { mutableStateOf<TreeNode?>(null) }
    var editName by remember { mutableStateOf("") }
    var editParentesco by remember { mutableStateOf("") }

    val graph = remember(nodes, uniones) { GenealogyGraphBuilder.build(nodes, uniones) }
    val subgraph = remember(graph) {
        VisibleSubgraphProjector.project(
            graph = graph,
            focusPersonId = focusPersonId,
            config = VisibleSubgraphProjector.Config(
                ancestorsDepth = 4,
                descendantsDepth = 4,
                includeCollaterals = true
            )
        )
    }
    val layout = remember(graph, subgraph) { GenealogyLayoutEngine.compute(graph, subgraph) }

    val onTreeNodeTap: (String, String?) -> Unit = { nodeId, cofreId ->
        if (!cofreId.isNullOrBlank()) {
            onNodeClick(nodeId, cofreId)
        } else {
            selectedNodeForActions = findNodeById(nodes, nodeId)
        }
    }
    val onTreeNodeLongPress: (String) -> Unit = { nodeId ->
        selectedNodeForActions = findNodeById(nodes, nodeId)
    }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorConsumed()
        }
    }

    AppShell(currentTab = NonnaTab.Arbol, onTabSelected = onTabSelected) {
        if (isLoading) {
            Column(modifier = Modifier.fillMaxSize()) {
                SimpleHeader(title = "Árbol Familiar", subtitle = "Conectá a tu familia y sus historias")
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
            return@AppShell
        }

        if (nodes.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize()) {
                SimpleHeader(title = "Árbol Familiar", subtitle = "Conectá a tu familia y sus historias")
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
            return@AppShell
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                        Column(modifier = Modifier.weight(1f)) {
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
                        ZoomControls(
                            zoom = viewport.zoom,
                            onZoomOut = { viewport.zoomOut() },
                            onZoomIn = { viewport.zoomIn() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = NonnaDimens.screenPaddingHorizontal),
                    shape = NonnaCorners.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoomChange, _ ->
                                    viewport.onTransform(
                                        panDx = pan.x,
                                        panDy = pan.y,
                                        zoomChange = zoomChange
                                    )
                                }
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(NonnaDimens.cardPaddingLarge)
                                .graphicsLayer {
                                    scaleX = viewport.zoom
                                    scaleY = viewport.zoom
                                    translationX = viewport.panX
                                    translationY = viewport.panY
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                                }
                        ) {
                            FamilyTreeCanvas(
                                layout = layout,
                                peopleById = graph.personsById,
                                onNodeTap = onTreeNodeTap,
                                onNodeLongPress = onTreeNodeLongPress
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        text = "💡 Toque: abre cofre si existe. Mantener presionado: editar/eliminar familiar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = NonnaDimens.screenPaddingHorizontal)
                    .padding(bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { showUnionManager = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Administrar árbol")
                }
                FloatingActionButton(
                    onClick = onAddNode,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar persona")
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp)
            )
        }
    }

    if (showUnionManager) {
        UnionManagerDialog(
            nodes = nodes,
            uniones = uniones,
            onDismiss = { showUnionManager = false },
            onCreateUnion = { p1, p2 ->
                onCreateUnion(p1, p2) { success, message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message ?: if (success) "Unión creada correctamente" else "No se pudo crear la unión"
                        )
                    }
                }
            },
            onDeleteUnion = { unionId ->
                onDeleteUnion(unionId) { success, message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message ?: if (success) "Unión eliminada correctamente" else "No se pudo eliminar la unión"
                        )
                    }
                }
            }
        )
    }

    selectedNodeForActions?.let { node ->
        AlertDialog(
            onDismissRequest = { selectedNodeForActions = null },
            title = { Text(node.name) },
            text = { Text("Elegí una acción para este familiar.") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!node.cofreId.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                selectedNodeForActions = null
                                onNodeClick(node.id, node.cofreId)
                            }
                        ) { Text("Ver cofre") }
                    }
                    TextButton(
                        onClick = {
                            selectedNodeForActions = null
                            nodePendingEdit = node
                            editName = node.name
                            editParentesco = node.relation
                        }
                    ) { Text("Editar") }
                    TextButton(
                        onClick = {
                            selectedNodeForActions = null
                            nodePendingDelete = node
                        }
                    ) { Text("Eliminar") }
                }
            },
            dismissButton = { TextButton(onClick = { selectedNodeForActions = null }) { Text("Cancelar") } }
        )
    }

    nodePendingDelete?.let { node ->
        AlertDialog(
            onDismissRequest = { nodePendingDelete = null },
            title = { Text("Eliminar familiar") },
            text = { Text("¿Seguro querés eliminar a ${node.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val personId = node.id
                        nodePendingDelete = null
                        onDeletePerson(personId) { success, message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message ?: if (success) "Familiar eliminado" else "No se pudo eliminar"
                                )
                            }
                        }
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { nodePendingDelete = null }) { Text("Cancelar") } }
        )
    }

    nodePendingEdit?.let { node ->
        AlertDialog(
            onDismissRequest = { nodePendingEdit = null },
            title = { Text("Editar familiar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editParentesco,
                        onValueChange = { editParentesco = it },
                        label = { Text("Parentesco") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val personId = node.id
                        val newName = editName.trim()
                        if (newName.isBlank()) return@TextButton
                        val newParentesco = editParentesco.trim().ifBlank { null }
                        nodePendingEdit = null
                        onEditPerson(personId, newName, newParentesco) { success, message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message ?: if (success) "Familiar actualizado" else "No se pudo actualizar"
                                )
                            }
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { nodePendingEdit = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ZoomControls(
    zoom: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit
) {
    Surface(
        shape = NonnaCorners.Medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(onClick = onZoomOut, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out", modifier = Modifier.size(16.dp))
            }
            Text(
                text = "${(zoom * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(40.dp)
            )
            IconButton(onClick = onZoomIn, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in", modifier = Modifier.size(16.dp))
            }
        }
    }
}

private data class PersonOption(val id: Int, val name: String)

@Composable
private fun UnionManagerDialog(
    nodes: List<TreeNode>,
    uniones: List<UnionArbolDto>,
    onDismiss: () -> Unit,
    onCreateUnion: (parent1Id: Int, parent2Id: Int?) -> Unit,
    onDeleteUnion: (unionId: Int) -> Unit
) {
    fun flatten(list: List<TreeNode>): List<PersonOption> {
        val acc = mutableListOf<PersonOption>()
        fun walk(items: List<TreeNode>) {
            items.forEach { node ->
                node.id.toIntOrNull()?.let { acc += PersonOption(it, node.name) }
                if (node.children.isNotEmpty()) walk(node.children)
            }
        }
        walk(list)
        return acc.distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }

    val people = remember(nodes) { flatten(nodes) }
    var parent1 by remember { mutableStateOf<PersonOption?>(null) }
    var parent2 by remember { mutableStateOf<PersonOption?>(null) }
    var showP1 by remember { mutableStateOf(false) }
    var showP2 by remember { mutableStateOf(false) }
    var unionToDelete by remember { mutableStateOf<UnionArbolDto?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Administrar uniones") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Creá una unión de padres o pareja para estructurar el árbol.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box {
                    OutlinedButton(onClick = { showP1 = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(parent1?.name ?: "Parent1 (obligatorio)")
                    }
                    DropdownMenu(expanded = showP1, onDismissRequest = { showP1 = false }) {
                        people.forEach { person ->
                            DropdownMenuItem(
                                text = { Text(person.name) },
                                onClick = {
                                    parent1 = person
                                    showP1 = false
                                    if (parent2?.id == person.id) parent2 = null
                                }
                            )
                        }
                    }
                }

                Box {
                    OutlinedButton(onClick = { showP2 = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(parent2?.name ?: "Parent2 (opcional)")
                    }
                    DropdownMenu(expanded = showP2, onDismissRequest = { showP2 = false }) {
                        DropdownMenuItem(
                            text = { Text("Sin segundo padre/madre") },
                            onClick = {
                                parent2 = null
                                showP2 = false
                            }
                        )
                        people.filter { it.id != parent1?.id }.forEach { person ->
                            DropdownMenuItem(
                                text = { Text(person.name) },
                                onClick = {
                                    parent2 = person
                                    showP2 = false
                                }
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        val p1 = parent1 ?: return@OutlinedButton
                        if (parent2?.id == p1.id) return@OutlinedButton
                        onCreateUnion(p1.id, parent2?.id)
                        parent1 = null
                        parent2 = null
                    },
                    enabled = parent1 != null && parent1?.id != parent2?.id,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear unión")
                }

                if (uniones.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Uniones existentes", style = MaterialTheme.typography.titleSmall)
                    uniones.forEach { union ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val p1Name = union.parent1?.displayName().orEmpty().ifBlank { "Sin nombre" }
                            val p2Name = union.parent2?.displayName().orEmpty().ifBlank { "Sin segundo padre/madre" }
                            Text(
                                text = "$p1Name - $p2Name",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { unionToDelete = union }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar unión",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )

    unionToDelete?.let { selected ->
        AlertDialog(
            onDismissRequest = { unionToDelete = null },
            title = { Text("Eliminar unión") },
            text = { Text("Las personas hijas de esta unión quedarán sin padres asignados. ¿Querés continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteUnion(selected.id)
                        unionToDelete = null
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { unionToDelete = null }) { Text("Cancelar") } }
        )
    }
}

private fun findNodeById(nodes: List<TreeNode>, id: String): TreeNode? {
    nodes.forEach { node ->
        if (node.id == id) return node
        val child = findNodeById(node.children, id)
        if (child != null) return child
    }
    return null
}
