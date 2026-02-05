package com.kickpaws.hopspot.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kickpaws.hopspot.MainActivity
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.data.local.TokenManager
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.RefreshFCMTokenRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HopSpotMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var api: HopSpotApi

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (tokenManager.isLoggedIn()) {
                try {
                    api.refreshFcmToken(RefreshFCMTokenRequest(token))
                } catch (e: Exception) {
                    // Retry later - not critical
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "HopSpot",
                body = notification.body ?: "",
                data = message.data
            )
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val channelId = "hopspot_benches"
        val benchId = data["bench_id"]?.toIntOrNull()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            benchId?.let { putExtra("bench_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_beer_marker)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
