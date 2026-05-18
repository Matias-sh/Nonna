package com.cocido.nonna

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.data.repository.NotificationTokenSyncManager
import com.cocido.nonna.notifications.PushTokenProvider
import com.cocido.nonna.ui.navigation.NonnaNavHost
import com.cocido.nonna.ui.theme.NonnaTheme
import com.cocido.nonna.ui.viewmodel.AuthState
import com.cocido.nonna.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val PREFS_NOTIFICATIONS = "nonna_notification_permission"
        private const val KEY_NOTIFICATIONS_PERMISSION_REQUESTED = "notifications_permission_requested"
    }

    @Inject
    lateinit var pushTokenProvider: PushTokenProvider

    @Inject
    lateinit var notificationTokenSyncManager: NotificationTokenSyncManager

    private val latestIntentState = mutableStateOf<Intent?>(null)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        latestIntentState.value = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appStartMs = SystemClock.elapsedRealtime()

        requestNotificationsPermissionIfNeeded()

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

        // Intenta obtener token FCM al arrancar la app.
        // Si Firebase no está configurado en este entorno, el provider retorna null sin romper la UI.
        latestIntentState.value = intent
        pushTokenProvider.fetchToken { token ->
            if (!token.isNullOrBlank()) {
                lifecycleScope.launch {
                    notificationTokenSyncManager.onNewTokenAvailable(token)
                }
            }
        }

        setContent {
            NonnaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val mainViewModel: MainViewModel = hiltViewModel()
                    val authState by mainViewModel.authState.collectAsStateWithLifecycle()
                    val ttiLogged = remember { mutableStateOf(false) }

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
                            if (!ttiLogged.value) {
                                ttiLogged.value = true
                                Log.d("NonnaPerf", "app_tti_ms=${SystemClock.elapsedRealtime() - appStartMs}")
                            }
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
                                    onEmailVerified = { mainViewModel.onEmailVerified() },
                                    externalIntent = latestIntentState.value
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) return

        val prefs = getSharedPreferences(PREFS_NOTIFICATIONS, MODE_PRIVATE)
        val alreadyRequested = prefs.getBoolean(KEY_NOTIFICATIONS_PERMISSION_REQUESTED, false)
        if (alreadyRequested) return

        prefs.edit().putBoolean(KEY_NOTIFICATIONS_PERMISSION_REQUESTED, true).apply()
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
