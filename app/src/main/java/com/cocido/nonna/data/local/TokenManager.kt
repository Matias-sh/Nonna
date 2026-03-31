package com.cocido.nonna.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_ID = stringPreferencesKey("user_id")
        /**
         * Persiste el estado de verificación de email.
         * null (key ausente) = nunca guardado → se trata como verificado para usuarios legacy.
         * false = email explícitamente NO verificado.
         * true = email verificado.
         */
        val EMAIL_VERIFICADO = booleanPreferencesKey("email_verificado")
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACCESS_TOKEN]
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_ID]
    }

    /**
     * null cuando la key no existe en DataStore (usuario previo o campo no devuelto por el backend).
     * En ese caso la app asume verificado para no bloquear usuarios existentes.
     */
    val emailVerificado: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[Keys.EMAIL_VERIFICADO]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = token
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = userId
        }
    }

    suspend fun saveEmailVerificado(verified: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EMAIL_VERIFICADO] = verified
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.EMAIL_VERIFICADO)
        }
    }
}
