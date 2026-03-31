package com.cocido.nonna.ui.screens.auth

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import com.cocido.nonna.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.navigation.AuthMode
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AuthScreen(
    mode: AuthMode,
    onBack: () -> Unit,
    onAuth: (email: String, password: String) -> Unit,
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
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.authSuccess.collectLatest { _ ->
            onAuth(email, password)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message, duration = androidx.compose.material3.SnackbarDuration.Short)
        }
    }
    // Cuando el email no está verificado, el MainViewModel detecta el cambio en DataStore
    // y recrea el NavHost con EmailVerificationScreen como destino.
    // Solo mostramos feedback informativo al usuario mientras ocurre la transición.
    LaunchedEffect(Unit) {
        viewModel.emailVerificationNeeded.collectLatest {
            snackbarHostState.showSnackbar(
                "Revisá tu correo para verificar tu cuenta.",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
        }
    }

    NonnaDetailScaffold {
        Box {
            SnackbarHost(hostState = snackbarHostState)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Volver",
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
                        text = if (mode == AuthMode.Login) "Bienvenido de nuevo" else "Crear cuenta",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (mode == AuthMode.Login) {
                            "Ingresá a tu espacio de memorias"
                        } else {
                            "Empezá a preservar las historias que importan"
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
                            label = "Nombre",
                            placeholder = "Juan",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NonnaTextField(
                            value = apellido,
                            onValueChange = { apellido = it },
                            label = "Apellido",
                            placeholder = "Pérez",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NonnaTextField(
                            value = nombreUsuario,
                            onValueChange = { nombreUsuario = it },
                            label = "Nombre de usuario",
                            placeholder = "juanperez123",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Email field
                    NonnaTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "tu@email.com",
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password field
                    NonnaTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                placeholder = "••••••••",
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
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (mode == AuthMode.Signup) ImeAction.Next else ImeAction.Done
                ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Confirm password (signup only)
                    if (mode == AuthMode.Signup) {
                Spacer(modifier = Modifier.height(16.dp))
                
                NonnaTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar contraseña",
                    placeholder = "••••••••",
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
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    errorMessage = if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        "Las contraseñas no coinciden"
                    } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Submit button
                    NonnaButton(
                text = when {
                    isLoading -> "Esperá..."
                    mode == AuthMode.Login -> "Ingresar"
                    else -> "Crear cuenta"
                },
                onClick = {
                    if (mode == AuthMode.Login) {
                        viewModel.login(email, password)
                    } else {
                        viewModel.signup(email, password, nombre, apellido, nombreUsuario)
                    }
                },
                style = NonnaButtonStyle.Primary,
                fullWidth = true,
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() &&
                        (mode == AuthMode.Login || (password == confirmPassword && nombre.isNotBlank() && apellido.isNotBlank() && nombreUsuario.isNotBlank()))
                    )
                    
                    // Forgot password (login only)
                    if (mode == AuthMode.Login) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(
                            onClick = { /* TODO: Forgot password */ },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
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
                            text = "Al continuar, aceptás que este es un espacio privado y seguro para tu familia. Tus datos son solo tuyos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
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
private fun AuthScreenLoginPreview() {
    NonnaTheme {
        AuthScreen(
            mode = AuthMode.Login,
            onBack = {},
            onAuth = { _, _ -> }
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
            onAuth = { _, _ -> }
        )
    }
}
