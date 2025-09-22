package com.cocido.nonna.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "auth_preferences", 
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
    }

    fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: Int,
        email: String,
        name: String,
        avatar: String? = null,
        expiresAt: Long? = null
    ) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_AVATAR, avatar)
            putBoolean(KEY_IS_LOGGED_IN, true)
            if (expiresAt != null) {
                putLong(KEY_TOKEN_EXPIRES_AT, expiresAt)
            }
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserAvatar(): String? = prefs.getString(KEY_USER_AVATAR, null)
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getTokenExpiresAt(): Long = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0L)

    fun clearAuthData() {
        prefs.edit().clear().apply()
    }

    fun updateUserProfile(name: String, avatar: String? = null) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            if (avatar != null) {
                putString(KEY_USER_AVATAR, avatar)
            }
            apply()
        }
    }

    fun isTokenExpired(): Boolean {
        val expiresAt = getTokenExpiresAt()
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt
    }
}
