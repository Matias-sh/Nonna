package com.cocido.nonna.ui.screens.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.nonna.data.remote.dto.UnionArbolDto
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme

@Composable
fun AddPersonScreen(
    onBack: () -> Unit,
    onAddPerson: (
        name: String,
        relation: String?,
        parentReference: String?,
        birthDate: String?,
        deathDate: String?,
        notes: String?,
        createCofre: Boolean,
        unionPadresId: Int?,
        parentescoConmigo: String?
    ) -> Unit,
    existingMembers: List<String> = emptyList(),
    uniones: List<UnionArbolDto> = emptyList()
) {
    var fullName by remember { mutableStateOf("") }
    var selectedRelation by remember { mutableStateOf<String?>(null) }
    var selectedParentReference by remember { mutableStateOf<String?>(null) }
    var selectedParentescoConmigo by remember { mutableStateOf<String?>(null) }
    var showParentescoDropdown by remember { mutableStateOf(false) }
    var selectedUnionPadresId by remember { mutableStateOf<Int?>(null) }
    var showUnionDropdown by remember { mutableStateOf(false) }
    var birthDate by remember { mutableStateOf("") }
    var deathDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var createCofre by remember { mutableStateOf(false) }
    var showRelationDropdown by remember { mutableStateOf(false) }
    var showParentReferenceDropdown by remember { mutableStateOf(false) }
    
    NonnaDetailScaffold {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "Agregar persona",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Sumá un familiar al árbol",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.screenPaddingHorizontal)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nombre completo
            Text(
                text = buildAnnotatedString {
                    append("Nombre completo ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append("*")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            NonnaTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Ej: Rosa García",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Parentesco conmigo (enum backend)
            Text(
                text = "Parentesco conmigo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box {
                Surface(
                    onClick = { showParentescoDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = NonnaCorners.Medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedParentescoConmigo ?: "Seleccionar (opcional)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedParentescoConmigo != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = showParentescoDropdown,
                    onDismissRequest = { showParentescoDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin especificar") },
                        onClick = {
                            selectedParentescoConmigo = null
                            showParentescoDropdown = false
                        }
                    )
                    parentescoConmigoOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedParentescoConmigo = option
                                showParentescoDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Unión de padres explícita (hijos de esa unión)
            Text(
                text = "Unión de padres",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box {
                Surface(
                    onClick = { showUnionDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = NonnaCorners.Medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val selectedUnionLabel = selectedUnionPadresId?.let { selectedId ->
                            uniones.firstOrNull { it.id == selectedId }?.let { formatUnionLabel(it) }
                        }
                        Text(
                            text = selectedUnionLabel ?: "Seleccionar unión (opcional)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedUnionLabel != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = showUnionDropdown,
                    onDismissRequest = { showUnionDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin unión de padres") },
                        onClick = {
                            selectedUnionPadresId = null
                            showUnionDropdown = false
                        }
                    )
                    uniones.forEach { union ->
                        DropdownMenuItem(
                            text = { Text(formatUnionLabel(union)) },
                            onClick = {
                                selectedUnionPadresId = union.id
                                selectedParentReference = null
                                showUnionDropdown = false
                            }
                        )
                    }
                }
            }

            if (selectedUnionPadresId == null) {
                Spacer(modifier = Modifier.height(24.dp))

                // Progenitor de referencia (opcional, solo si no se eligio union de padres)
                Text(
                    text = "Hijo/a de (opcional)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    Surface(
                        onClick = { showParentReferenceDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = NonnaCorners.Medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedParentReference ?: "Seleccionar progenitor existente (opcional)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedParentReference != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Icon(
                                imageVector = Icons.Outlined.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showParentReferenceDropdown,
                        onDismissRequest = { showParentReferenceDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin progenitor de referencia") },
                            onClick = {
                                selectedParentReference = null
                                showParentReferenceDropdown = false
                            }
                        )
                        existingMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member) },
                                onClick = {
                                    selectedParentReference = member
                                    showParentReferenceDropdown = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Si el usuario elige union de padres, el campo "Hijo/a de" no aplica.
                if (selectedParentReference != null) {
                    selectedParentReference = null
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Pareja opcional (independiente de la union de padres)
            Text(
                text = "Pareja (opcional)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Box {
                Surface(
                    onClick = { showRelationDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = NonnaCorners.Medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedRelation ?: "Seleccionar pareja existente (opcional)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedRelation != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showRelationDropdown,
                    onDismissRequest = { showRelationDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin pareja") },
                        onClick = {
                            selectedRelation = null
                            showRelationDropdown = false
                        }
                    )
                    existingMembers.forEach { member ->
                        DropdownMenuItem(
                            text = { Text(member) },
                            onClick = {
                                selectedRelation = member
                                showRelationDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "La conexión principal se define con 'Parentesco conmigo' y, si corresponde, con 'Unión de padres' o 'Hijo/a de'. El campo de pareja solo agrega el vínculo con la otra persona.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Fecha de nacimiento
            Text(
                text = "Fecha de nacimiento",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            NonnaTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                placeholder = "dd/mm/aaaa",
                trailingIcon = Icons.Outlined.CalendarMonth,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Fecha de fallecimiento
            Text(
                text = "Fecha de fallecimiento (opcional)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            NonnaTextField(
                value = deathDate,
                onValueChange = { deathDate = it },
                placeholder = "dd/mm/aaaa",
                trailingIcon = Icons.Outlined.CalendarMonth,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Notas personales
            Text(
                text = "Notas personales",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            NonnaTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Ej: Vivió en Italia hasta los 30 años...",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Crear cofre checkbox
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = NonnaCorners.Medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = createCofre,
                        onCheckedChange = { createCofre = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Crear cofre para esta persona",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Podrás empezar a guardar recuerdos inmediatamente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Bottom buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NonnaDimens.screenPaddingHorizontal)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NonnaButton(
                    text = "Cancelar",
                    onClick = onBack,
                    style = NonnaButtonStyle.Outline,
                    modifier = Modifier.weight(1f)
                )
                NonnaButton(
                    text = "Agregar persona",
                    onClick = {
                        onAddPerson(
                            fullName,
                            selectedRelation,
                            if (selectedUnionPadresId == null) selectedParentReference else null,
                            birthDate.ifBlank { null },
                            deathDate.ifBlank { null },
                            notes.ifBlank { null },
                            createCofre,
                            selectedUnionPadresId,
                            selectedParentescoConmigo
                        )
                    },
                    style = NonnaButtonStyle.Primary,
                    enabled = fullName.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = NonnaCorners.Medium,
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "💡 ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)) {
                                append("Consejo: ")
                            }
                            append("Podés agregar personas de cualquier generación. Si no sabés todas las fechas, no te preocupes, podés completarlas después.")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun AddPersonScreenPreview() {
    NonnaTheme {
        AddPersonScreen(
            onBack = {},
            onAddPerson = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}

private fun formatUnionLabel(union: UnionArbolDto): String {
    val p1 = union.parent1?.displayName().orEmpty().ifBlank { "Sin nombre" }
    val p2 = union.parent2?.displayName().orEmpty().ifBlank { "Sin segundo padre/madre" }
    return "$p1 - $p2"
}

private val parentescoConmigoOptions = listOf(
    "YO",
    "PAPA", "MAMA",
    "HIJO", "HIJA",
    "ABUELO", "ABUELA",
    "NIETO", "NIETA",
    "BISABUELO", "BISABUELA",
    "HERMANO", "HERMANA",
    "TIO", "TIA",
    "SOBRINO", "SOBRINA",
    "PRIMO", "PRIMA",
    "SUEGRO", "SUEGRA",
    "YERNO", "NUERA",
    "CUÑADO", "CUÑADA",
    "PADRINO", "MADRINA",
    "AHIJADO", "AHIJADA",
    "AMIGO", "AMIGA",
    "OTRO"
)
