package com.cocido.nonna.ui.screens.invitaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.data.remote.dto.InvitacionUiModel
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonSize
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import com.cocido.nonna.ui.viewmodel.InvitacionesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun InvitacionesPendientesScreen(
    onBack: () -> Unit,
    onCofreAceptado: (cofreId: String) -> Unit,
    viewModel: InvitacionesViewModel = hiltViewModel()
) {
    val invitaciones by viewModel.invitaciones.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.aceptarSuccess.collectLatest { nombreCofre ->
            // La navegación la maneja onCofreAceptado; aquí solo llegamos si hay cofreId
            // El evento aceptarSuccess lleva el nombre del cofre para el snackbar
            scope.launch { snackbarHostState.showSnackbar("¡Te uniste al cofre \"$nombreCofre\"!") }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.rechazarSuccess.collectLatest {
            scope.launch { snackbarHostState.showSnackbar("Invitación rechazada.") }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PageHeader(
                title = "Invitaciones",
                subtitle = if (invitaciones.isEmpty()) "" else "${invitaciones.size} pendiente${if (invitaciones.size != 1) "s" else ""}",
                onBack = onBack
            )

            when {
                isLoading && invitaciones.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                invitaciones.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateWithButton(
                            icon = Icons.Outlined.Mail,
                            title = "Sin invitaciones pendientes",
                            description = "Cuando alguien te invite a un cofre de recuerdos, aparecerá acá.",
                            buttonText = "Actualizar",
                            onButtonClick = { viewModel.load() }
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = NonnaDimens.screenPaddingHorizontal,
                            vertical = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = invitaciones,
                            key = { it.id }
                        ) { invitacion ->
                            InvitacionCard(
                                invitacion = invitacion,
                                onAceptar = {
                                    viewModel.aceptar(invitacion.id, invitacion.nombreCofre)
                                    // Navegar al cofre si tenemos el ID
                                    if (invitacion.cofreId.isNotBlank()) {
                                        onCofreAceptado(invitacion.cofreId)
                                    }
                                },
                                onRechazar = { viewModel.rechazar(invitacion.id) },
                                isLoading = isLoading
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun InvitacionCard(
    invitacion: InvitacionUiModel,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)
        ) {
            // Ícono y nombre del cofre
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PrimaryGradientStart.copy(alpha = 0.2f),
                                    PrimaryGradientEnd.copy(alpha = 0.2f)
                                )
                            ),
                            shape = NonnaCorners.Medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invitacion.nombreCofre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Invitado por ${invitacion.nombreInvitador}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = NonnaCorners.Medium
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "Te invitaron a colaborar en este cofre de recuerdos. ¿Querés unirte?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NonnaButton(
                    text = "Rechazar",
                    onClick = onRechazar,
                    style = NonnaButtonStyle.Outline,
                    size = NonnaButtonSize.Small,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                )
                NonnaButton(
                    text = "Aceptar",
                    onClick = onAceptar,
                    style = NonnaButtonStyle.Primary,
                    size = NonnaButtonSize.Small,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
