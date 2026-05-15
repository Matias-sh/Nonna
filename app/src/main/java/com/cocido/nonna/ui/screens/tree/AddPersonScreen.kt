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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaDatePickerField
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme
import com.cocido.nonna.util.FormValidators
import com.cocido.nonna.util.UserMessages

@Composable
fun AddPersonScreen(
    onBack: () -> Unit,
    onAddPerson: (name: String, relation: String?, birthDate: String?, deathDate: String?, notes: String?, createCofre: Boolean) -> Unit,
    existingMembers: List<String> = emptyList()
) {
    var fullName by remember { mutableStateOf("") }
    var selectedRelation by remember { mutableStateOf<String?>(null) }
    var birthDate by remember { mutableStateOf("") }
    var deathDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var createCofre by remember { mutableStateOf(false) }
    var showRelationDropdown by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }
    val normalizedFullName = fullName.trim()
    val fullNameError = attemptedSubmit && !FormValidators.hasMinLength(normalizedFullName, 2)
    val invalidDateRange = attemptedSubmit && FormValidators.isBirthAfterDeath(birthDate, deathDate)
    
    NonnaDetailScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
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
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = stringResource(R.string.add_person_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.add_person_subtitle),
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
                    append(stringResource(R.string.add_person_full_name_required))
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
                placeholder = stringResource(R.string.add_person_name_placeholder),
                isError = fullNameError,
                errorMessage = if (fullNameError) UserMessages.INVALID_NAME_MIN_2 else null,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Relación con familiar existente
            Text(
                text = stringResource(R.string.add_person_relation_question),
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
                            text = selectedRelation ?: stringResource(R.string.add_person_select_existing_optional),
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
                        text = { Text(stringResource(R.string.add_person_no_direct_connection)) },
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            NonnaDatePickerField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = stringResource(R.string.add_person_birth_date_label),
                disallowFutureDates = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            NonnaDatePickerField(
                value = deathDate,
                onValueChange = { deathDate = it },
                label = stringResource(R.string.add_person_death_date_optional_label),
                disallowFutureDates = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (invalidDateRange) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.add_person_invalid_date_range),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Notas personales
            Text(
                text = stringResource(R.string.add_person_notes_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            NonnaTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = stringResource(R.string.add_person_notes_placeholder),
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
                            text = stringResource(R.string.add_person_create_chest_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.add_person_create_chest_hint),
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
                    text = stringResource(R.string.common_cancel),
                    onClick = onBack,
                    style = NonnaButtonStyle.Outline,
                    modifier = Modifier.weight(1f)
                )
                NonnaButton(
                    text = stringResource(R.string.add_person_button),
                    onClick = {
                        attemptedSubmit = true
                        if (!FormValidators.hasMinLength(normalizedFullName, 2) || FormValidators.isBirthAfterDeath(birthDate, deathDate)) {
                            return@NonnaButton
                        }
                        onAddPerson(
                            normalizedFullName,
                            selectedRelation,
                            birthDate.ifBlank { null },
                            deathDate.ifBlank { null },
                            notes.ifBlank { null },
                            createCofre
                        )
                    },
                    style = NonnaButtonStyle.Primary,
                    enabled = normalizedFullName.isNotBlank(),
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
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)) {
                                append(stringResource(R.string.common_tip_prefix))
                            }
                            append(stringResource(R.string.add_person_tip_text))
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
            onAddPerson = { _, _, _, _, _, _ -> }
        )
    }
}
