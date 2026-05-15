package com.cocido.nonna.notifications

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePushTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : PushTokenProvider {

    override fun fetchToken(onResult: (String?) -> Unit) {
        val app = runCatching {
            FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
        }.getOrNull()

        if (app == null) {
            onResult(null)
            return
        }

        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(task.result?.takeIf { it.isNotBlank() })
                    } else {
                        onResult(null)
                    }
                }
        }.onFailure {
            onResult(null)
        }
    }

    override fun deleteToken(onComplete: (Boolean) -> Unit) {
        val app = runCatching {
            FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
        }.getOrNull()

        if (app == null) {
            onComplete(false)
            return
        }

        runCatching {
            FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        }.onFailure {
            onComplete(false)
        }
    }
}

