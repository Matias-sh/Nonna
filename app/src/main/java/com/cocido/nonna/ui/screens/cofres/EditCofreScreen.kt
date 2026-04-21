package com.cocido.nonna.ui.screens.cofres

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cocido.nonna.data.mock.relationOptionsByCategory
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaCropContract
import com.cocido.nonna.ui.components.NonnaCropRequest
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditCofreScreen(
    cofreId: String,
    onBack: () -> Unit,
    onUpdated: () -> Unit = {},
    viewModel: com.cocido.nonna.ui.viewmodel.EditCofreViewModel = hiltViewModel()
) {
    val cofre by viewModel.cofre.collectAsState()
    var name by remember { mutableStateOf("") }
    var selectedRelationOption by remember { mutableStateOf<String?>(null) }
    var customRelation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Success) }

    var hasInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(cofre) {
        if (!hasInitialized && cofre != null) {
            name = cofre!!.name
            val initialRelation = cofre!!.relation
            val predefined = relationOptionsByCategory.values.flatten().toSet()
            if (initialRelation in predefined) {
                selectedRelationOption = initialRelation
                customRelation = ""
            } else {
                selectedRelationOption = null
                customRelation = initialRelation
            }
            hasInitialized = true
        }
    }

    val finalRelation = customRelation.trim().ifBlank { selectedRelationOption.orEmpty() }
    val canSelectPresetRelation = customRelation.isBlank()
    val canWriteCustomRelation = selectedRelationOption == null

    val cropLauncher = rememberLauncherForActivityResult(
        contract = NonnaCropContract()
    ) { result ->
        result?.let { coverImageUri = it }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            cropLauncher.launch(
                NonnaCropRequest(
                    sourceUri = uri,
                    aspectRatio = 16f / 9f,
                    title = "Editar portada del cofre"
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collectLatest {
            feedbackMessage = "Cofre actualizado correctamente"
            feedbackType = NonnaFeedbackType.Success
            feedbackVisible = true
            delay(1200)
            feedbackVisible = false
            onUpdated()
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { msg ->
            msg?.let {
                feedbackMessage = it
                feedbackType = NonnaFeedbackType.Error
                feedbackVisible = true
                delay(1800)
                feedbackVisible = false
            }
        }
    }

    val canSubmit = name.isNotBlank() && finalRelation.isNotBlank() && !isLoading

    NonnaDetailScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                PageHeader(
                    title = "Editar cofre",
                    subtitle = "Modificá la información del cofre",
                    onBack = onBack
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(NonnaDimens.screenPaddingHorizontal)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Imagen de portada (opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (coverImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(192.dp)
                                .clip(NonnaCorners.Large)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = coverImageUri,
                                contentDescription = "Nueva imagen de portada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { coverImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .size(32.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        shape = NonnaCorners.Full
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Quitar imagen",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(192.dp)
                                .clip(NonnaCorners.Large)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = NonnaCorners.Large
                                )
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cofre?.coverImageUrl != null) {
                                AsyncImage(
                                    model = cofre?.coverImageUrl,
                                    contentDescription = "Imagen actual",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.Upload,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Subir imagen",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    NonnaTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nombre del cofre *",
                        placeholder = "Ej: Nonna Rosa, Abuelo Juan...",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Parentesco",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    relationOptionsByCategory.forEach { (category, options) ->
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            options.forEach { option ->
                                Surface(
                                    onClick = {
                                        selectedRelationOption = option
                                        customRelation = ""
                                    },
                                    enabled = canSelectPresetRelation,
                                    shape = NonnaCorners.Medium,
                                    color = if (selectedRelationOption == option) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selectedRelationOption == option) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (!canSelectPresetRelation) {
                        Text(
                            text = "Las opciones sugeridas se desactivan mientras escribís un parentesco personalizado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    NonnaTextField(
                        value = customRelation,
                        onValueChange = {
                            customRelation = it
                            if (it.isNotBlank()) selectedRelationOption = null
                        },
                        placeholder = "O escribí otro...",
                        enabled = canWriteCustomRelation,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!canWriteCustomRelation) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "El campo personalizado se habilita al deseleccionar el parentesco sugerido.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    NonnaTextArea(
                        value = description,
                        onValueChange = { description = it },
                        label = "Una frase que la/lo describe (opcional)",
                        placeholder = "Ej: La mejor cocinera del mundo...",
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NonnaButton(
                            text = if (isLoading) "Guardando..." else "Guardar cambios",
                            onClick = {
                                viewModel.update(
                                    name = name,
                                    relation = finalRelation,
                                    description = description.ifBlank { null },
                                    coverImageUri = coverImageUri
                                )
                            },
                            style = NonnaButtonStyle.Primary,
                            fullWidth = true,
                            enabled = canSubmit
                        )
                        NonnaButton(
                            text = "Cancelar",
                            onClick = onBack,
                            style = NonnaButtonStyle.Outline,
                            fullWidth = true
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            NonnaBottomFeedbackBanner(
                visible = feedbackVisible,
                message = feedbackMessage,
                type = feedbackType,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
