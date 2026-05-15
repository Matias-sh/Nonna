package com.cocido.nonna.data.repository

import android.content.SharedPreferences
import com.cocido.nonna.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTokenSyncManager @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val tokenManager: TokenManager,
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_PENDING_FCM_TOKEN = "pending_fcm_token"
        private const val KEY_LAST_SYNCED_FCM_TOKEN_PREFIX = "last_synced_fcm_token_"
    }

    suspend fun syncIfLoggedIn() {
        val authToken = tokenManager.token.firstOrNull()?.takeIf { it.isNotBlank() } ?: return
        if (authToken.isBlank()) return

        val syncScope = resolveSyncScope(authToken)
        val lastSyncedKey = "$KEY_LAST_SYNCED_FCM_TOKEN_PREFIX$syncScope"
        val pending = sharedPreferences.getString(KEY_PENDING_FCM_TOKEN, null)?.trim().orEmpty()
        if (pending.isBlank()) return
        val lastSynced = sharedPreferences.getString(lastSyncedKey, null)?.trim().orEmpty()
        if (pending == lastSynced) return

        when (notificationsRepository.registerDeviceToken(pending)) {
            is ApiResult.Success -> {
                sharedPreferences.edit()
                    .putString(lastSyncedKey, pending)
                    .apply()
            }
            else -> Unit
        }
    }

    suspend fun onNewTokenAvailable(token: String) {
        val clean = token.trim()
        if (clean.isBlank()) return
        sharedPreferences.edit().putString(KEY_PENDING_FCM_TOKEN, clean).apply()
        syncIfLoggedIn()
    }

    fun onLogout() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_PENDING_FCM_TOKEN)
        sharedPreferences.all.keys
            .filter { it.startsWith(KEY_LAST_SYNCED_FCM_TOKEN_PREFIX) }
            .forEach { key -> editor.remove(key) }
        editor.apply()
    }

    private suspend fun resolveSyncScope(authToken: String): String {
        val userId = tokenManager.userId.firstOrNull()?.trim().orEmpty()
        if (userId.isNotBlank()) {
            return "uid_$userId"
        }
        // Fallback para escenarios donde aún no se persistió userId.
        return "auth_${sha256(authToken).take(16)}"
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(Locale.US, byte) }
    }
}

