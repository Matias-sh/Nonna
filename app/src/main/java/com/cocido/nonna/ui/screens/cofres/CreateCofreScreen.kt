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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.data.mock.relationOptions
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import kotlinx.coroutines.launch

data class NewCofre(
    val name: String,
    val relation: String,
    val description: String,
    val coverImageUrl: String?
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateCofreScreen(
    onBack: () -> Unit,
    onCreate: (NewCofre) -> Unit,
    viewModel: com.cocido.nonna.ui.viewmodel.CreateCofreViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var inviteEmails by remember { mutableStateOf<List<String>>(emptyList()) }
    var emailInput by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) coverImageUri = uri
    }

    LaunchedEffect(Unit) {
        viewModel.created.collectLatest { _ ->
            onCreate(NewCofre(name = name, relation = relation, description = description, coverImageUrl = coverImageUri?.toString()))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { msg ->
            msg?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
        }
    }

    val canSubmit = name.isNotBlank() && !isLoading
    
    NonnaDetailScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
        PageHeader(
            title = "Nuevo Cofre",
            subtitle = "Creá un espacio para preservar memorias",
            onBack = onBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.screenPaddingHorizontal)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Cover image upload
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
                        contentDescription = "Imagen de portada",
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
                            contentDescription = "Eliminar imagen",
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                        Text(
                            text = "o arrastrá una imagen aquí",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Name
            NonnaTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre del cofre *",
                placeholder = "Ej: Nonna Rosa, Abuelo Juan...",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Relation
            Text(
                text = "Parentesco",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                relationOptions.forEach { option ->
                    Surface(
                        onClick = { relation = option },
                        shape = NonnaCorners.Medium,
                        color = if (relation == option) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (relation == option) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NonnaTextField(
                value = relation,
                onValueChange = { relation = it },
                placeholder = "O escribí otro...",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            NonnaTextArea(
                value = description,
                onValueChange = { description = it },
                label = "Una frase que la/lo describe (opcional)",
                placeholder = "Ej: La mejor cocinera del mundo, Siempre con una sonrisa...",
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Privacy note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = NonnaCorners.Medium
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "🔒 Este cofre es privado por defecto. Solo las personas que invites podrán verlo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Invitar familiares (opcional)
            Text(
                text = "Invitar familiares (opcional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Agregá emails para que puedan ver y colaborar. También podés invitar después desde el cofre.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NonnaTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    placeholder = "email@ejemplo.com",
                    modifier = Modifier.weight(1f)
                )
                NonnaButton(
                    text = "Agregar",
                    onClick = {
                        val email = emailInput.trim()
                        if (email.isNotBlank() && email.contains("@") && email !in inviteEmails) {
                            inviteEmails = inviteEmails + email
                            emailInput = ""
                        }
                    },
                    style = NonnaButtonStyle.Outline,
                    size = com.cocido.nonna.ui.components.NonnaButtonSize.Small
                )
            }
            if (inviteEmails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    inviteEmails.forEach { email ->
                        Surface(
                            shape = NonnaCorners.Medium,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = { inviteEmails = inviteEmails - email },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Quitar",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NonnaButton(
                    text = if (isLoading) "Creando..." else "Crear cofre",
                    onClick = {
                        viewModel.create(
                            name = name,
                            relation = relation,
                            description = description.ifBlank { null },
                            coverImageUri = coverImageUri,
                            inviteEmails = inviteEmails
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
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}
