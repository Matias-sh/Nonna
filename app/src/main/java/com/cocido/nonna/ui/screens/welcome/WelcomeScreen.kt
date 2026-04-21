package com.cocido.nonna.ui.screens.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart

@Composable
fun WelcomeScreen(
    onCreateCofre: () -> Unit,
    onLogin: () -> Unit
) {
    var logoVisible by remember { mutableStateOf(false) }
    var descriptionVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }
    var privacyVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        logoVisible = true
        kotlinx.coroutines.delay(200)
        descriptionVisible = true
        kotlinx.coroutines.delay(100)
        buttonsVisible = true
        kotlinx.coroutines.delay(100)
        privacyVisible = true
    }
    NonnaDetailScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.spacing24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
        // Logo - usamos el mismo gráfico que el launcher, sin marcos extra
        AnimatedVisibility(
            visible = logoVisible,
            enter = scaleIn(initialScale = 0.8f, animationSpec = spring())
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo de NONNA",
                modifier = Modifier.size(160.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "NONNA",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        AnimatedVisibility(visible = descriptionVisible, enter = fadeIn(animationSpec = spring())) {
            Text(
                text = "El cofre donde la memoria vive para siempre",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Illustration placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryGradientStart.copy(alpha = 0.1f),
                            PrimaryGradientEnd.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                    ),
                    shape = NonnaCorners.ExtraLarge
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = NonnaCorners.ExtraLarge
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Un lugar privado y cálido para preservar\nlas historias que nos conectan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Action buttons
        AnimatedVisibility(
            visible = buttonsVisible,
            enter = slideInVertically(initialOffsetY = { it / 5 }, animationSpec = spring()) + fadeIn()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NonnaButton(
                    text = "Crear mi primer cofre",
                    onClick = onCreateCofre,
                    style = NonnaButtonStyle.Primary,
                    fullWidth = true
                )

                NonnaButton(
                    text = "Ingresar",
                    onClick = onLogin,
                    style = NonnaButtonStyle.Outline,
                    fullWidth = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Privacy note
        AnimatedVisibility(visible = privacyVisible, enter = fadeIn(animationSpec = spring())) {
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
                    text = "Tu privacidad es sagrada. NONNA es un espacio privado, solo para tu familia.",
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

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun WelcomeScreenPreview() {
    NonnaTheme {
        WelcomeScreen(
            onCreateCofre = {},
            onLogin = {}
        )
    }
}

