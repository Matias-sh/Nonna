package com.cocido.nonna.ui.screens.auth

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.cocido.nonna.ui.viewmodel.VerifyEmailViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VerifyEmailScreen(
    onVerified: () -> Unit,
    onLogout: () -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val cooldownSeconds by viewModel.cooldownSeconds.collectAsState()
    val expiredCodeHighlight by viewModel.expiredCodeHighlight.collectAsState()

    var code by remember { mutableStateOf("") }
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Success) }
    var feedbackMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.verified.collectLatest {
            feedbackType = NonnaFeedbackType.Success
            feedbackMessage = context.getString(R.string.verify_email_success)
            feedbackVisible = true
            onVerified()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.infoMessage.collectLatest { msg ->
            feedbackType = NonnaFeedbackType.Success
            feedbackMessage = msg
            feedbackVisible = true
        }
    }
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { msg ->
            feedbackType = NonnaFeedbackType.Error
            feedbackMessage = msg
            feedbackVisible = true
        }
    }

    NonnaDetailScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(NonnaDimens.screenPaddingHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = stringResource(R.string.verify_email_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.verify_email_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (expiredCodeHighlight) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(NonnaDimens.spacing16),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.msg_expired_verification_code),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            if (cooldownSeconds > 0) {
                                Text(
                                    text = stringResource(R.string.verify_email_resend_cooldown, cooldownSeconds),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                NonnaButton(
                                    text = stringResource(R.string.verify_email_resend_button),
                                    onClick = { viewModel.resendCode() },
                                    enabled = !isLoading,
                                    fullWidth = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                NonnaTextField(
                    value = code,
                    onValueChange = { value ->
                        code = value.filter { it.isDigit() }.take(6)
                    },
                    label = stringResource(R.string.verify_email_code_label),
                    placeholder = stringResource(R.string.verify_email_code_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
                NonnaButton(
                    text = stringResource(R.string.verify_email_button),
                    onClick = { viewModel.verifyCode(code) },
                    enabled = !isLoading && code.length == 6,
                    fullWidth = true
                )

                if (!expiredCodeHighlight) {
                    Spacer(modifier = Modifier.height(12.dp))
                    NonnaButton(
                        text = if (cooldownSeconds > 0) {
                            stringResource(R.string.verify_email_resend_cooldown, cooldownSeconds)
                        } else {
                            stringResource(R.string.verify_email_resend_button)
                        },
                        onClick = { viewModel.resendCode() },
                        style = NonnaButtonStyle.Outline,
                        enabled = !isLoading && cooldownSeconds == 0,
                        fullWidth = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    NonnaButton(
                        text = stringResource(R.string.profile_logout),
                        onClick = onLogout,
                        style = NonnaButtonStyle.Ghost
                    )
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
