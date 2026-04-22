package com.cocido.nonna.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaCropContract
import com.cocido.nonna.ui.components.NonnaCropRequest
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.theme.NonnaDimens

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    viewModel: com.cocido.nonna.ui.viewmodel.ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val initialFirstName = user?.persona?.nombre ?: user?.nombre ?: user?.name ?: ""
    val initialLastName = user?.persona?.apellido ?: ""
    val initialUsername = user?.nombreUsuario ?: ""
    var firstName by remember(initialFirstName) { mutableStateOf(initialFirstName) }
    var lastName by remember(initialLastName) { mutableStateOf(initialLastName) }
    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Success) }

    val currentAvatarUrl = user?.profileImageUrl()
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    val cropLauncher = rememberLauncherForActivityResult(
        contract = NonnaCropContract()
    ) { result ->
        result?.let { avatarUri = it }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            cropLauncher.launch(
                NonnaCropRequest(
                    sourceUri = uri,
                    aspectRatio = 1f,
                    title = context.getString(R.string.profile_edit_photo_title)
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (user == null) {
            viewModel.load()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collectLatest {
            successMessage = "Perfil actualizado correctamente"
            feedbackType = NonnaFeedbackType.Success
            showSuccessBanner = true
            delay(1500)
            showSuccessBanner = false
            onBack()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            successMessage = it
            feedbackType = NonnaFeedbackType.Error
            showSuccessBanner = true
            delay(1800)
            showSuccessBanner = false
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            PageHeader(
                title = stringResource(R.string.profile_edit_title),
                subtitle = stringResource(R.string.profile_edit_subtitle),
                onBack = onBack
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = NonnaDimens.screenPaddingHorizontal),
                verticalArrangement = Arrangement.Top
            ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar block
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val model: Any? = avatarUri ?: currentAvatarUrl
                    if (model != null) {
                        AsyncImage(
                            model = model,
                            contentDescription = stringResource(R.string.profile_photo_label),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = stringResource(R.string.profile_photo_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.profile_photo_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.auth_name_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.profile_first_name_placeholder)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.auth_lastname_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.profile_last_name_placeholder)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.profile_username_optional_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.profile_username_placeholder)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            NonnaButton(
                text = stringResource(R.string.common_save_changes),
                onClick = {
                    viewModel.updateProfile(
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        avatarUri = avatarUri
                    )
                },
                style = NonnaButtonStyle.Primary,
                enabled = (firstName.isNotBlank() || lastName.isNotBlank() || username.isNotBlank() || avatarUri != null) && !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            NonnaButton(
                text = stringResource(R.string.common_cancel),
                onClick = onBack,
                style = NonnaButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
        }

        NonnaBottomFeedbackBanner(
            visible = showSuccessBanner,
            message = successMessage,
            type = feedbackType,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

