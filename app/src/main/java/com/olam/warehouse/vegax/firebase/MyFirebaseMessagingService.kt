package com.olam.warehouse.vegax.firebase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.presentation.BuildConfig
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.R


/**
 * Created by Baskaran Kannan on 4/30/2020.
 */

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val TAG = "FirebaseMessagingService"

    @SuppressLint("LongLogTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Dikirim dari: ${remoteMessage.from}")

        if (remoteMessage.notification != null && BuildConfig.BUILD_TYPE == "sit") {
            val bodyData = remoteMessage.notification?.body?.let { Gson().fromJson<Message>(it) }
            when (bodyData?.flag) {
                "VEGA_IV_CASH_SAP" -> makeStatusNotification(
                        remoteMessage.notification?.title,
                        remoteMessage.notification?.body
                )
                else -> makeStatusNotification(
                        remoteMessage.notification?.title,
                        remoteMessage.notification?.body
                )
            }

        }
    }

    /* @SuppressLint("LongLogTag")
     override fun onNewToken(token: String) {
         Log.d(TAG, "Refreshed token: $token")

         // If you want to send messages to this application instance or
         // manage this apps subscriptions on the server side, send the
         // Instance ID token to your app server.
         sendRegistrationToServer(token)
     }

     @SuppressLint("LongLogTag")
     private fun sendRegistrationToServer(token: String?) {
         // TODO: Implement this method to send token to your app server.
         Log.d(TAG, "sendRegistrationTokenToServer($token)")
     }*/


    fun makeStatusNotification(title: String?, body: String?) {
        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = Constants.VERBOSE_NOTIFICATION_CHANNEL_NAME
            val description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)

            // Add the channel
            val notificationManager =
                App.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        // Create the notification
        val builder = NotificationCompat.Builder(App.getAppContext(), Constants.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVibrate(LongArray(0))

        // Show the notification
        NotificationManagerCompat.from(App.getAppContext()).notify(Constants.NOTIFICATION_ID, builder.build())
    }
}
