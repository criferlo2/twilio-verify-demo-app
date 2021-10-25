package com.hashitoapps.twiliotestcriferlo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hashitoapps.twiliotestcriferlo.R
import com.hashitoapps.twiliotestcriferlo.ui.login.VerifyActivity

class MessagingService : FirebaseMessagingService() {

    private val channelId = "channel_id"
    private val notificationId = 100
    private val channelName="channel_name"
    private val channelDescription="channel_description"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        when (remoteMessage.data["type"]) {
            "verify_push_challenge" -> {
                val factorSid = remoteMessage.data["factor_sid"]
                val challengeSid = remoteMessage.data["challenge_sid"]
                val message = remoteMessage.data["message"]
                if (factorSid != null && challengeSid != null) {
                    showNotification(factorSid, challengeSid, message!!)
                }
            }
        }
    }

    private fun showNotification(factorSid: String, challengeSid: String, message: String) {

        createNotificationChannel()
        val intent = Intent(this, VerifyActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("factorSid", factorSid)
        intent.putExtra("challengeSid", challengeSid)
        intent.putExtra("message", message)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Twilio Criferlo")
            .setContentText("Verify you login ->")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = channelName
            val descriptionText = channelDescription
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(p0: String) {
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("token", p0).apply();
    }
}