package com.cocido.nonna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.ui.navigation.NonnaNavHost
import com.cocido.nonna.ui.theme.NonnaTheme
import com.cocido.nonna.ui.viewmodel.AuthState
import com.cocido.nonna.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NonnaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val mainViewModel: MainViewModel = hiltViewModel()
                    val authState by mainViewModel.authState.collectAsState()

                    when (authState) {
                        is AuthState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        else -> {
                            val startDestination = when (authState) {
                                is AuthState.LoggedInVerified -> "home"
                                is AuthState.LoggedInUnverified -> "verify-email"
                                else -> "welcome"
                            }
                            key(authState) {
                                NonnaNavHost(
                                    isLoggedIn = authState is AuthState.LoggedInVerified || authState is AuthState.LoggedInUnverified,
                                    onLogout = { mainViewModel.logout() },
                                    startDestination = startDestination,
                                    onEmailVerified = { mainViewModel.onEmailVerified() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
