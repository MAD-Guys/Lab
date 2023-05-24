package it.polito.mad.sportapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.SportAppActivity
import it.polito.mad.sportapp.application_utilities.checkIfUserIsLoggedIn
import it.polito.mad.sportapp.notifications.sendTokenToDatabase
import java.util.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val channelId = "ezsport_channel"
    private val tag = "MyFirebaseMessagingService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // send new token to server
        val user = FirebaseAuth.getInstance().currentUser?.uid

        user?.let {
            // send token to Firebase server
            sendTokenToDatabase(token, it)
        }

        Log.i(tag, "onNewToken completed with token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // create notification intent and put extras
        val notificationIntent = Intent(this, SportAppActivity::class.java).apply {
            action = remoteMessage.data["action"]
            putExtra("id_reservation", remoteMessage.data["id_reservation"]?.toInt())
            putExtra("status", remoteMessage.data["status"])
            putExtra("timestamp", remoteMessage.data["timestamp"])
        }

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // create pending intent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // initialize notification manager
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // set notification id
        val notificationID: Int = Random().nextInt(5000)

        // set notification sound
        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        // build notification
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle(remoteMessage.data["title"])
                .setContentText(remoteMessage.data["message"])
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSound(notificationSoundUri)
                .setAutoCancel(true)

        // send notification only if the user is logged in
        if (checkIfUserIsLoggedIn()) {
            notificationManager.notify(notificationID, builder.build())
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {

        val channelName = "ezsport_channel"
        val channelDescription = "ezsport_channel_description"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.description = channelDescription
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)

        notificationManager?.createNotificationChannel(channel)
    }
}