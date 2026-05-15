package com.cocido.nonna.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cocido.nonna.MainActivity
import com.cocido.nonna.R
import com.cocido.nonna.data.repository.NotificationTokenSyncManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NonnaFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val CHANNEL_ID = "nonna_notifications"
        private const val CHANNEL_NAME = "Notificaciones Nonna"
        private const val CHANNEL_DESCRIPTION = "Notificaciones de actividad familiar y suscripción."
    }

    @Inject
    lateinit var notificationTokenSyncManager: NotificationTokenSyncManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            notificationTokenSyncManager.onNewTokenAvailable(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("NonnaFCM", "Push recibida. dataKeys=${message.data.keys}")
        showForegroundNotification(message)
    }

    private fun showForegroundNotification(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.notifications_title)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["cuerpo"]
            ?: getString(R.string.notifications_subtitle)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(PUSH_TYPE_KEY, message.data["type"])
            putExtra(PUSH_COFRE_ID_KEY, message.data["cofreId"])
            putExtra(PUSH_PAYMENT_ID_KEY, message.data["pagoId"] ?: message.data["paymentId"])
            putExtra(PUSH_VERSION_KEY, message.data["version"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            intent.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)
    }
}

