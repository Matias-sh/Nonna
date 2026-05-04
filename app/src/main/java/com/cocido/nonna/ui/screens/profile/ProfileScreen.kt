package com.cocido.nonna.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cocido.nonna.R
import com.cocido.nonna.BuildConfig
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.components.SimpleHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import com.cocido.nonna.ui.theme.NonnaTheme
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import com.cocido.nonna.data.remote.dto.SuscripcionPlanDto
import java.util.Locale

@Composable
fun ProfileScreen(
    onTabSelected: (NonnaTab) -> Unit,
    onEditProfile: () -> Unit,
    onOpenInvitations: () -> Unit,
    onLogout: () -> Unit,
    viewModel: com.cocido.nonna.ui.viewmodel.ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val suscripcion by viewModel.suscripcion.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var planFeedbackVisible by remember { mutableStateOf(false) }
    var planFeedbackMessage by remember { mutableStateOf("") }
    var planFeedbackType by remember { mutableStateOf(NonnaFeedbackType.Success) }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            planFeedbackType = NonnaFeedbackType.Error
            planFeedbackMessage = msg
            planFeedbackVisible = true
            delay(2200)
            planFeedbackVisible = false
            viewModel.clearError()
        }
    }

    val displayUser = user
    // En perfil mostramos el nombre \"humano\" (persona/nombre) más que el username técnico.
    val userName = displayUser?.displayName() ?: ""
    val userEmail = displayUser?.email ?: ""
    val avatarUrl = displayUser?.profileImageUrl()
    val joinedDate = displayUser?.createdAt ?: displayUser?.created_at ?: ""

    AppShell(
        currentTab = NonnaTab.Perfil,
        onTabSelected = onTabSelected
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SimpleHeader(
                title = stringResource(R.string.profile_title),
                subtitle = stringResource(R.string.profile_subtitle)
            )
            
            Column(
                modifier = Modifier.padding(horizontal = NonnaDimens.screenPaddingHorizontal)
            ) {
                // User card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = NonnaCorners.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NonnaDimens.cardPaddingLarge),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryGradientStart.copy(alpha = 0.2f),
                                            PrimaryGradientEnd.copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = stringResource(R.string.profile_photo_desc),
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
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
                        
                        // Info: nombre de usuario (o nombre completo) y email
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (userEmail.isNotBlank()) {
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isLoading) {
                                Spacer(modifier = Modifier.height(12.dp))
                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                suscripcion?.let { sub ->
                    SubscriptionSummaryCard(suscripcion = sub)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = stringResource(R.string.profile_options),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                OptionCard(
                    icon = Icons.Outlined.Edit,
                    title = stringResource(R.string.profile_edit_title),
                    onClick = onEditProfile,
                    subtitle = stringResource(R.string.profile_edit_subtitle)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OptionCard(
                    icon = Icons.Outlined.MailOutline,
                    title = stringResource(R.string.invites_title),
                    onClick = onOpenInvitations,
                    subtitle = stringResource(R.string.invites_profile_subtitle)
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                // Logout
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = NonnaCorners.Medium,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NonnaDimens.cardPadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.profile_logout),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Footer info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (joinedDate.isNotBlank()) {
                            stringResource(R.string.profile_member_since, joinedDate)
                        } else {
                            ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
            }
        }

            NonnaBottomFeedbackBanner(
                visible = planFeedbackVisible && planFeedbackMessage.isNotBlank(),
                message = planFeedbackMessage,
                type = planFeedbackType,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

    }
}

@Composable
private fun SubscriptionSummaryCard(
    suscripcion: com.cocido.nonna.data.remote.dto.SuscripcionActualDto
) {
    val plan = suscripcion.plan
    val limites = suscripcion.limites
    val uso = suscripcion.uso
    val planDisplay = plan?.nombre?.takeIf { it.isNotBlank() }
        ?: plan?.codigo?.takeIf { it.isNotBlank() }
        ?: "—"
    val showUpsell = isLikelyFreeTier(plan)
    val upsellBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    val upsellBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val upsellText = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.subscription_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = NonnaCorners.Full,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = planDisplay,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            plan?.descripcion?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            suscripcion.estado?.takeIf { it.isNotBlank() }?.let { est ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.subscription_status_label) + ": " + est,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showUpsell) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NonnaCorners.Medium)
                        .background(upsellBg)
                        .border(1.dp, upsellBorder, NonnaCorners.Medium)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.subscription_upsell_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = upsellText
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.subscription_upsell_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = upsellText.copy(alpha = 0.92f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.subscription_limits_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            limites?.maxCofres?.let {
                PlanStatRow(
                    label = stringResource(R.string.subscription_limit_cofres),
                    value = it.toString()
                )
            }
            limites?.maxRecuerdos?.let {
                PlanStatRow(
                    label = stringResource(R.string.subscription_limit_recuerdos),
                    value = it.toString()
                )
            }
            limites?.maxMiembrosPorCofre?.let {
                PlanStatRow(
                    label = stringResource(R.string.subscription_limit_miembros),
                    value = it.toString()
                )
            }
            limites?.maxCofresInvitado?.let {
                PlanStatRow(
                    label = stringResource(R.string.subscription_limit_invitado_cofres),
                    value = it.toString()
                )
            }
            limites?.maxArchivosPorRecuerdo?.let {
                PlanStatRow(
                    label = stringResource(R.string.subscription_limit_archivos_recuerdo),
                    value = it.toString()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.subscription_usage_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            uso?.cofresCreados?.let {
                PlanStatRow(
                    label = stringResource(R.string.subscription_usage_cofres),
                    value = it.toString()
                )
            }
            uso?.recuerdosCreados?.let {
                PlanStatRow(
                    label = stringResource(R.string.subscription_usage_recuerdos),
                    value = it.toString()
                )
            }
        }
    }
}

@Composable
private fun PlanStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun isLikelyFreeTier(plan: SuscripcionPlanDto?): Boolean {
    if (plan == null) return true
    val code = plan.codigo?.trim()?.uppercase(Locale.getDefault()).orEmpty()
    val name = plan.nombre?.trim()?.lowercase(Locale.getDefault()).orEmpty()
    return when {
        code == "FREE" || code == "GRATIS" || code == "GRATUIT" -> true
        name.contains("gratis") || name == "free" -> true
        else -> false
    }
}

@Composable
private fun OptionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NonnaDimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun ProfileScreenPreview() {
    NonnaTheme {
        ProfileScreen(
            onTabSelected = {},
            onEditProfile = {},
            onOpenInvitations = {},
            onLogout = {}
        )
    }
}
