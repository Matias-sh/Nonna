package com.cocido.nonna.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.navigation.AuthMode
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme
import com.cocido.nonna.util.FormValidators
import com.cocido.nonna.util.UserMessages
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AuthScreen(
    mode: AuthMode,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onAuth: (user: UserDto) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    val normalizedEmail = email.trim()
    val normalizedNombre = nombre.trim()
    val normalizedApellido = apellido.trim()
    val normalizedNombreUsuario = nombreUsuario.trim()
    val isSignup = mode == AuthMode.Signup

    val hasValidEmail = FormValidators.isValidEmail(normalizedEmail)
    val hasValidPassword = password.length >= 8
    val matchesConfirmPassword = password == confirmPassword
    val hasValidNombre = FormValidators.hasMinLength(normalizedNombre, 2)
    val hasValidApellido = FormValidators.hasMinLength(normalizedApellido, 2)
    val hasValidNombreUsuario = FormValidators.isValidUsername(normalizedNombreUsuario)

    val emailError = attemptedSubmit && !hasValidEmail
    val passwordError = attemptedSubmit && !hasValidPassword
    val nombreError = attemptedSubmit && isSignup && !hasValidNombre
    val apellidoError = attemptedSubmit && isSignup && !hasValidApellido
    val usuarioError = attemptedSubmit && isSignup && !hasValidNombreUsuario
    val confirmError = attemptedSubmit && isSignup && !matchesConfirmPassword

    val canSubmit = if (isSignup) {
        hasValidEmail &&
            hasValidPassword &&
            hasValidNombre &&
            hasValidApellido &&
            hasValidNombreUsuario &&
            matchesConfirmPassword
    } else {
        hasValidEmail && hasValidPassword
    }

    LaunchedEffect(Unit) {
        viewModel.authSuccess.collectLatest { user ->
            attemptedSubmit = false
            onAuth(user)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            feedbackMessage = message
            feedbackVisible = true
            kotlinx.coroutines.delay(1800)
            feedbackVisible = false
        }
    }

    NonnaDetailScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                // Header with back button
                Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NonnaDimens.spacing16),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.auth_back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = stringResource(R.string.auth_back),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NonnaDimens.spacing24),
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = if (mode == AuthMode.Login) {
                            stringResource(R.string.auth_welcome_back)
                        } else {
                            stringResource(R.string.auth_create_account)
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (mode == AuthMode.Login) {
                            stringResource(R.string.auth_login_subtitle)
                        } else {
                            stringResource(R.string.auth_signup_subtitle)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Nombre, apellido, nombreUsuario (solo registro)
                    if (mode == AuthMode.Signup) {
                        NonnaTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = stringResource(R.string.auth_name_label),
                            placeholder = stringResource(R.string.auth_name_placeholder),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            isError = nombreError,
                            errorMessage = if (nombreError) UserMessages.INVALID_NAME_MIN_2 else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NonnaTextField(
                            value = apellido,
                            onValueChange = { apellido = it },
                            label = stringResource(R.string.auth_lastname_label),
                            placeholder = stringResource(R.string.auth_lastname_placeholder),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            isError = apellidoError,
                            errorMessage = if (apellidoError) UserMessages.INVALID_LASTNAME_MIN_2 else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NonnaTextField(
                            value = nombreUsuario,
                            onValueChange = { nombreUsuario = it.filter { ch -> !ch.isWhitespace() } },
                            label = stringResource(R.string.auth_username_label),
                            placeholder = stringResource(R.string.auth_username_placeholder),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            isError = usuarioError,
                            errorMessage = if (usuarioError) {
                                stringResource(R.string.auth_username_error)
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Email field
                    NonnaTextField(
                value = email,
                onValueChange = { email = it.filter { ch -> !ch.isWhitespace() } },
                label = stringResource(R.string.auth_email_label),
                placeholder = stringResource(R.string.auth_email_placeholder),
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                        isError = emailError,
                        errorMessage = if (emailError) UserMessages.INVALID_EMAIL else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password field
                    NonnaTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.auth_password_label),
                placeholder = stringResource(R.string.auth_password_placeholder),
                leadingIcon = Icons.Outlined.Lock,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIconContent = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                stringResource(R.string.auth_hide_password)
                            } else {
                                stringResource(R.string.auth_show_password)
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (mode == AuthMode.Signup) ImeAction.Next else ImeAction.Done
                ),
                        isError = passwordError,
                        errorMessage = if (passwordError) stringResource(R.string.auth_password_min_error) else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Confirm password (signup only)
                    if (mode == AuthMode.Signup) {
                Spacer(modifier = Modifier.height(16.dp))
                
                NonnaTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = stringResource(R.string.auth_confirm_password_label),
                    placeholder = stringResource(R.string.auth_password_placeholder),
                    leadingIcon = Icons.Outlined.Lock,
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIconContent = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (confirmPasswordVisible) {
                                    stringResource(R.string.auth_hide_password)
                                } else {
                                    stringResource(R.string.auth_show_password)
                                },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    isError = confirmError,
                    errorMessage = if (confirmError) {
                        UserMessages.PASSWORD_MISMATCH
                    } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    }

                    if (mode == AuthMode.Login) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = stringResource(R.string.auth_forgot_password),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onForgotPassword() }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Submit button
                    NonnaButton(
                text = when {
                    isLoading -> stringResource(R.string.auth_wait)
                    mode == AuthMode.Login -> stringResource(R.string.auth_login_button)
                    else -> stringResource(R.string.auth_signup_button)
                },
                onClick = {
                    attemptedSubmit = true
                    if (!canSubmit) {
                        feedbackMessage = UserMessages.FORM_REVIEW_REQUIRED
                        feedbackVisible = true
                        return@NonnaButton
                    }

                    if (mode == AuthMode.Login) {
                        viewModel.login(normalizedEmail, password)
                    } else {
                        viewModel.signup(
                            email = normalizedEmail,
                            password = password,
                            nombre = normalizedNombre,
                            apellido = normalizedApellido,
                            nombreUsuario = normalizedNombreUsuario
                        )
                    }
                },
                style = NonnaButtonStyle.Primary,
                fullWidth = true,
                enabled = !isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
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
                            text = stringResource(R.string.auth_privacy_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            NonnaBottomFeedbackBanner(
                visible = feedbackVisible,
                message = feedbackMessage,
                type = NonnaFeedbackType.Error,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun AuthScreenLoginPreview() {
    NonnaTheme {
        AuthScreen(
            mode = AuthMode.Login,
            onBack = {},
            onAuth = { }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun AuthScreenSignupPreview() {
    NonnaTheme {
        AuthScreen(
            mode = AuthMode.Signup,
            onBack = {},
            onAuth = { }
        )
    }
}
