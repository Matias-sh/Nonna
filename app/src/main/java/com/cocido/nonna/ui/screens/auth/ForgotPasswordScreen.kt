package com.cocido.nonna.ui.screens.auth

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.viewmodel.ForgotPasswordStep
import com.cocido.nonna.ui.viewmodel.ForgotPasswordViewModel
import com.cocido.nonna.util.FormValidators
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val step by viewModel.step.collectAsState()
    val email by viewModel.email.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Error) }

    LaunchedEffect(Unit) {
        viewModel.completed.collectLatest {
            feedbackType = NonnaFeedbackType.Success
            feedbackMessage = context.getString(R.string.forgot_password_success)
            feedbackVisible = true
            delay(1400)
            feedbackVisible = false
            onCompleted()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            feedbackType = NonnaFeedbackType.Error
            feedbackMessage = it
            feedbackVisible = true
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.codeSent.collectLatest {
            feedbackType = NonnaFeedbackType.Success
            feedbackMessage = context.getString(R.string.forgot_password_code_sent)
            feedbackVisible = true
            delay(2200)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NonnaDimens.spacing16),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = stringResource(R.string.common_back),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(onClick = onBack)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NonnaDimens.spacing24),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(
                            when (step) {
                                ForgotPasswordStep.Email -> R.string.forgot_password_title_email
                                ForgotPasswordStep.Code -> R.string.forgot_password_title_code
                                ForgotPasswordStep.NewPassword -> R.string.forgot_password_title_new
                            }
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(
                            when (step) {
                                ForgotPasswordStep.Email -> R.string.forgot_password_subtitle_email
                                ForgotPasswordStep.Code -> R.string.forgot_password_subtitle_code
                                ForgotPasswordStep.NewPassword -> R.string.forgot_password_subtitle_new
                            }
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    when (step) {
                        ForgotPasswordStep.Email -> {
                            NonnaTextField(
                                value = email,
                                onValueChange = { viewModel.setEmail(it.filter { ch -> !ch.isWhitespace() }) },
                                label = stringResource(R.string.auth_email_label),
                                placeholder = stringResource(R.string.auth_email_placeholder),
                                leadingIcon = Icons.Outlined.Email,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            NonnaButton(
                                text = if (isLoading) {
                                    stringResource(R.string.auth_wait)
                                } else {
                                    stringResource(R.string.forgot_password_send_code)
                                },
                                onClick = { viewModel.requestCode() },
                                style = NonnaButtonStyle.Primary,
                                fullWidth = true,
                                enabled = !isLoading && FormValidators.isValidEmail(email.trim())
                            )
                        }
                        ForgotPasswordStep.Code -> {
                            NonnaTextField(
                                value = code,
                                onValueChange = { v -> code = v.filter { it.isDigit() }.take(6) },
                                label = stringResource(R.string.forgot_password_code_label),
                                placeholder = stringResource(R.string.forgot_password_code_placeholder),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            NonnaButton(
                                text = if (isLoading) {
                                    stringResource(R.string.auth_wait)
                                } else {
                                    stringResource(R.string.forgot_password_verify_code)
                                },
                                onClick = { viewModel.verifyCode(code) },
                                style = NonnaButtonStyle.Primary,
                                fullWidth = true,
                                enabled = !isLoading && code.length == 6
                            )
                        }
                        ForgotPasswordStep.NewPassword -> {
                            NonnaTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = stringResource(R.string.auth_password_label),
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
                                            contentDescription = null
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            NonnaTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = stringResource(R.string.auth_confirm_password_label),
                                leadingIcon = Icons.Outlined.Lock,
                                visualTransformation = if (confirmVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIconContent = {
                                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                        Icon(
                                            imageVector = if (confirmVisible) {
                                                Icons.Outlined.VisibilityOff
                                            } else {
                                                Icons.Outlined.Visibility
                                            },
                                            contentDescription = null
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            NonnaButton(
                                text = if (isLoading) {
                                    stringResource(R.string.auth_wait)
                                } else {
                                    stringResource(R.string.forgot_password_save)
                                },
                                onClick = {
                                    viewModel.confirmNewPassword(password, confirmPassword)
                                },
                                style = NonnaButtonStyle.Primary,
                                fullWidth = true,
                                enabled = !isLoading && password.length >= 8 && confirmPassword.length >= 8
                            )
                        }
                    }
                }
            }

            NonnaBottomFeedbackBanner(
                visible = feedbackVisible && feedbackMessage.isNotBlank(),
                message = feedbackMessage,
                type = feedbackType,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
