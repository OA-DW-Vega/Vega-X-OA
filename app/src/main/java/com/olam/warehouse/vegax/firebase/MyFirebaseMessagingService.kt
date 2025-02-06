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
import com.olam.warehouse.login.ui.notification.NotificationActivity
import com.olam.warehouse.login.ui.notification.navigation.WorkFlowNavigation
import com.olam.warehouse.master.common.model.MessageModel
import com.olam.warehouse.master.common.model.NotificationModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentUserName
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.NOTIFICATION_RECEIVED
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentDateForNotification
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.DateUtils.isLastFiveDaysRecord
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.BuildConfig
import com.olam.warehouse.vegax.R


/**
 * Created by Baskaran Kannan on 4/30/2020.
 */

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val TAG = "FirebaseMessagingService"

    @SuppressLint("LongLogTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Push Notification: ${remoteMessage.from}")
        Log.d(TAG, "Push Notification1: ${remoteMessage.notification?.body}")

        if (remoteMessage.notification != null) {
            val bodyData = remoteMessage.notification?.body?.let { Gson().fromJson<Message>(it) }
            if (bodyData?.environment.equals(BuildConfig.BUILD_TYPE)) {
                val bodyData = remoteMessage.notification?.body?.let { Gson().fromJson<Message>(it) }
                val gson = GsonUtils()
                val notifiList = arrayListOf<NotificationModel>()
                val notifiListOfFiveDays = arrayListOf<NotificationModel>()
                val oldNotifiList = PreferenceHelper.get(Constants.NOTIFICATION_LIST, "")
                if (oldNotifiList.isNotEmpty()) notifiList.addAll(
                    Gson().fromJson<List<NotificationModel>>(
                        oldNotifiList
                    )
                )
                //if (notifiList.size >= 5) notifiList.removeAt(0)
                val notification = NotificationModel()
                notification.notification = remoteMessage.notification?.body.toString()
                notification.Date = getCurrentDateForNotification()
                notification.timeMillis = getCurrentTimeInMills()
                notifiList.add(notification)
                notifiList.forEach {
                    if (isLastFiveDaysRecord(it.timeMillis)) notifiListOfFiveDays.add(it)
                }
                val bodyModel = Gson().fromJson<MessageModel>(remoteMessage.notification?.body.toString())
                PreferenceHelper.save(Constants.NOTIFICATION_LIST, gson.toJson(notifiListOfFiveDays))
                val splitItem = bodyData?.flag?.split(",")
                when {
                    splitItem?.any {
                        it.equals(
                            getCurrentKey(),
                            true
                        )
                    } == true && !getCurrentKey().isNullOrEmpty() -> makeStatusNotification(
                        remoteMessage.notification?.title,
                        bodyData.message,
                        bodyModel
                    )

                    splitItem?.any {
                        it.equals(
                            getPlantDetails().plantId,
                            true
                        )
                    } == true && !getPlantDetails().plantId.isNullOrEmpty() -> makeStatusNotification(
                        remoteMessage.notification?.title,
                        bodyData.message,
                        bodyModel
                    )

                    splitItem?.any {
                        it.equals(
                            getCurrentUserName(),
                            true
                        )
                    } == true && !getCurrentUserName().isNullOrEmpty() -> makeStatusNotification(
                        remoteMessage.notification?.title,
                        bodyData.message,
                        bodyModel
                    )

                    splitItem?.any { it.equals(Constants.ALL, true) } == true -> makeStatusNotification(
                        remoteMessage.notification?.title,
                        bodyData.message,
                        bodyModel
                    )
                }

            }
        }
    }

    @SuppressLint("LongLogTag")
    override fun handleIntent(intent: Intent) {
        //super.handleIntent(intent)
        /* if (intent.extras != null) {
             for (key in intent.extras!!.keySet()) {
                 val value = intent.extras?.get(key)
                 Log.d(TAG, "Backgroud Service Key: $key Value: $value")
             }
         }*/

        intent.extras?.let { RemoteMessage(it) }?.let { this.onMessageReceived(it) }
        Log.d(TAG, "Backgroud Service: ${intent.extras?.get("gcm.notification.body Value")}")
    }

    @SuppressLint("LongLogTag")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    @SuppressLint("LongLogTag")
    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }


    fun makeStatusNotification(title: String?, body: String?, bodyModel: MessageModel) {
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
                App.getAppContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }
        //App.getAppContext().sendBroadcast(Intent(NOTIFICATION_RECEIVED))
        App.getAppContext().sendBroadcast(
            Intent(NOTIFICATION_RECEIVED).apply {
                setPackage(App.getAppContext().packageName)
            }
        )
        //Move Notification list page
        moveNotificationPage(title, body, bodyModel)

    }

    private fun moveNotificationPage(title: String?, body: String?, bodyModel: MessageModel) {
        var intent:Intent? = null
        if(bodyModel.navigationId?.isNotEmpty() == true)
            intent = WorkFlowNavigation().navigationProcessWithIntent(bodyModel.navigationId, bodyModel.transactionId, App.getAppContext(), Intent(this, NotificationActivity::class.java))
        else
            intent = Intent(this, NotificationActivity::class.java)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification
        val builder = NotificationCompat.Builder(App.getAppContext(), Constants.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVibrate(LongArray(0))

        // Show the notification
        var notificationId: Int? =
            if (bodyModel.id?.isNullOrEmpty() == true) Constants.NOTIFICATION_ID else bodyModel.id?.toInt()
        NotificationManagerCompat.from(App.getAppContext()).notify(notificationId ?: 0, builder.build())

    }
}
