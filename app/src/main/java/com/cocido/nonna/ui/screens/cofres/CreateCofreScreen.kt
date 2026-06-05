package com.cocido.nonna.ui.screens.cofres

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.data.mock.relationOptionsByCategory
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaCropContract
import com.cocido.nonna.ui.components.NonnaCropRequest
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.InviteEmailTrailingAddPill
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.components.relationCategoryLabel
import com.cocido.nonna.ui.components.relationOptionLabel
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.util.FormValidators
import com.cocido.nonna.util.UserMessages
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var selectedRelationOption by remember { mutableStateOf<String?>(null) }
    var customRelation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var inviteEmails by remember { mutableStateOf<List<String>>(emptyList()) }
    var emailInput by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Success) }
    var attemptedSubmit by remember { mutableStateOf(false) }

    fun tryAddInviteEmailFromField() {
        val email = emailInput.trim().lowercase()
        if (email.isBlank()) return
        when {
            !FormValidators.isValidEmail(email) -> {
                feedbackMessage = UserMessages.INVALID_INVITE_EMAIL
                feedbackType = NonnaFeedbackType.Error
                feedbackVisible = true
            }
            inviteEmails.any { it.equals(email, ignoreCase = true) } -> {
                feedbackMessage = UserMessages.DUPLICATE_EMAIL
                feedbackType = NonnaFeedbackType.Error
                feedbackVisible = true
            }
            else -> {
                inviteEmails = inviteEmails + email
                emailInput = ""
            }
        }
    }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = NonnaCropContract()
    ) { result ->
        result?.let { coverImageUri = it }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            cropLauncher.launch(
                NonnaCropRequest(
                    sourceUri = uri,
                    aspectRatio = 16f / 9f,
                    title = context.getString(R.string.chest_cover_edit_title)
                )
            )
        }
    }

    val normalizedName = name.trim()
    val normalizedDescription = description.trim()
    val finalRelation = customRelation.trim().ifBlank { selectedRelationOption.orEmpty() }.trim()
    val canSelectPresetRelation = customRelation.isBlank()
    val nameError = attemptedSubmit && !FormValidators.hasMinLength(normalizedName, 2)
    val relationError = attemptedSubmit && !FormValidators.hasMinLength(finalRelation, 2)
    val descriptionError = attemptedSubmit && !FormValidators.hasMinLength(normalizedDescription, 3)

    LaunchedEffect(Unit) {
        viewModel.created.collectLatest { _ ->
            onCreate(
                NewCofre(
                    name = normalizedName,
                    relation = finalRelation,
                    description = normalizedDescription,
                    coverImageUrl = coverImageUri?.toString()
                )
            )
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

    val canSubmit = FormValidators.hasMinLength(normalizedName, 2) &&
        FormValidators.hasMinLength(finalRelation, 2) &&
        FormValidators.hasMinLength(normalizedDescription, 3) &&
        !isLoading
    
    NonnaDetailScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
        PageHeader(
            title = stringResource(R.string.create_chest_title),
            subtitle = stringResource(R.string.create_chest_subtitle),
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
            
            // Cover image upload
            Text(
                text = stringResource(R.string.chest_cover_optional_label),
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
                        contentDescription = stringResource(R.string.chest_cover_image_cd),
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
                            contentDescription = stringResource(R.string.common_remove_image_cd),
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
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                            )
                        },
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
                            text = stringResource(R.string.common_upload_image),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.common_upload_hint),
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
                label = stringResource(R.string.create_chest_name_required),
                placeholder = stringResource(R.string.onboarding_chest_name_placeholder),
                isError = nameError,
                errorMessage = if (nameError) UserMessages.INVALID_NAME_MIN_2 else null,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Relation
            Text(
                text = stringResource(R.string.common_relation),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            relationOptionsByCategory.forEach { (category, options) ->
                Text(
                    text = relationCategoryLabel(category),
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
                                if (selectedRelationOption == option) {
                                    selectedRelationOption = null
                                } else {
                                    selectedRelationOption = option
                                    customRelation = ""
                                }
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
                                text = relationOptionLabel(option),
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
                    text = stringResource(R.string.relation_presets_disabled_hint),
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
                placeholder = stringResource(R.string.relation_custom_placeholder),
                isError = relationError,
                errorMessage = if (relationError) UserMessages.INVALID_RELATION else null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            NonnaTextArea(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.create_chest_description_required),
                placeholder = stringResource(R.string.onboarding_description_placeholder),
                minLines = 4,
                isError = descriptionError,
                errorMessage = if (descriptionError) UserMessages.INVALID_SHORT_DESCRIPTION else null,
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
                    text = stringResource(R.string.chest_privacy_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Invitar familiares (opcional)
            Text(
                text = stringResource(R.string.chest_invite_optional_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.chest_invite_optional_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            NonnaTextField(
                value = emailInput,
                onValueChange = { emailInput = it.filterNot(Char::isWhitespace) },
                placeholder = stringResource(R.string.invite_email_placeholder),
                leadingIcon = Icons.Outlined.Email,
                modifier = Modifier.fillMaxWidth(),
                trailingIconContent = {
                    InviteEmailTrailingAddPill(
                        emailInput = emailInput,
                        existingEmails = inviteEmails,
                        onAdded = { normalized ->
                            inviteEmails = inviteEmails + normalized
                            emailInput = ""
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { tryAddInviteEmailFromField() })
            )
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
                                        contentDescription = stringResource(R.string.common_remove),
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
                    text = if (isLoading) stringResource(R.string.common_creating) else stringResource(R.string.onboarding_create_chest),
                    onClick = {
                        attemptedSubmit = true
                        if (!canSubmit) {
                            feedbackMessage = UserMessages.FIELD_REVIEW_REQUIRED
                            feedbackType = NonnaFeedbackType.Error
                            feedbackVisible = true
                            return@NonnaButton
                        }
                        viewModel.create(
                            name = normalizedName,
                            relation = finalRelation,
                            description = normalizedDescription,
                            coverImageUri = coverImageUri,
                            inviteEmails = inviteEmails
                        )
                    },
                    style = NonnaButtonStyle.Primary,
                    fullWidth = true,
                    enabled = canSubmit
                )
                
                NonnaButton(
                    text = stringResource(R.string.common_cancel),
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
