package com.olam.warehouse.presentation.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import timber.log.Timber
import kotlin.random.Random

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
const val UNIQUE_ONE_TIME_WORKER = "MASTER"
const val UNIQUE_ONE_TIME_WORKER_TRANS = "TRANS_MASTER"
const val UNIQUE_ONE_TIME_WORKER_INVENTORY = "INVENTORY_SYNC"
const val UNIQUE_ONE_TIME_WORKER_VENDOR_CREDIT = "VENDER_CREDIT_SYNC"
const val UNIQUE_ONE_TIME_WORKER_EXCHANGE_RATE = "EXCHANGE_RATE_SYNC"
const val UNIQUE_ONE_TIME_WORKER_VENDER_ADVANCE_CREDIT = "VENDER_ADVANCE_CREDIT_SYNC"
const val UNIQUE_ONE_TIME_WORKER_LOGOUT = "LOGOUT"
const val UNIQUE_PERIODIC_WORKER = "MASTER_SCHEDULER"
const val TRANS_OUTPUT_DATA = "trans_work_data"
const val MASTER_OUTPUT_DATA = "master_work_data"
const val DO_RECEIVING_OFFLINE_DATA = "do_receiving_offline_data"
const val DO_QUALITY_OFFLINE_DATA = "do_quality_offline_data"
const val INVENTORY_SYNC_OUTPUT_DATA = "inventory_sync_data"
const val VENDORY_SYNC_OUTPUT_DATA = "vendor_sync_data"
const val SEASON_OUTPUT_DATA = "season_output_data"
const val VEHICLE_OUTPUT_DATA = "vehicle_output_data"
const val LOGOUT_DATA = "logout_data"
const val COTTON_GINNING_TRANS_DATA = "cotton_ginning_trans_data"
const val UNIQUE_ONE_TIME_WORKER_DATA = "unique_data_sync"
const val UNIQUE_ONE_TIME_WORKER_BALE = "unique_bale_data_sync"
const val DATA_SYNC = "data_sync"
const val BALE_DATA_SYNC = "bale_data_sync"
val constraint = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()


val constraintWithoutNetwork = Constraints.Builder()
    .setRequiresBatteryNotLow(true)
    .build()

fun enQueueWorker(worker: OneTimeWorkRequest, context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork(Random.nextInt().toString(), ExistingWorkPolicy.KEEP, worker)
}

fun enQueueWorkerWithName(worker: OneTimeWorkRequest, uniqueWorkName: String, context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.KEEP, worker)
}

fun enQueueWorkers(workers: List<OneTimeWorkRequest>, context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork(Random.nextInt().toString(), ExistingWorkPolicy.KEEP, workers)
}

fun enQueueUniqueWorker(worker: OneTimeWorkRequest, uniqueWorkName: String, context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, worker)
}

fun enQueueChainWorker(workers: List<OneTimeWorkRequest>, context: Context) {
    WorkManager.getInstance(context.applicationContext).beginWith(workers).enqueue()
}

fun enQueueChainWorker(worker1: OneTimeWorkRequest, worker2: OneTimeWorkRequest, context: Context) {
    WorkManager.getInstance(context.applicationContext).beginWith(worker1).then(worker2).enqueue()
}

fun enQueueChainWorker(worker: OneTimeWorkRequest, context: Context) {
    WorkManager.getInstance(context.applicationContext).beginWith(worker).enqueue()
}

fun enQueuePeriodicWorker(worker: PeriodicWorkRequest, context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniquePeriodicWork(Random.nextInt().toString(), ExistingPeriodicWorkPolicy.KEEP, worker)
}

fun enQueueUniquePeriodicWorker(worker: PeriodicWorkRequest, uniqueWorkName: String, context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniquePeriodicWork(uniqueWorkName, ExistingPeriodicWorkPolicy.KEEP, worker)
}

fun makeStatusNotification(message: String, context: Context) {
    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = Constants.VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
        channel.description = description

        // Add the channel
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_template_icon_bg)
        .setContentTitle(Constants.NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setOngoing(true)
        .setVibrate(LongArray(0))

    // Show the notification
    NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, builder.build())
}

/*
*  cancel the onoing notification
*/
fun dismissNotification(context: Context) {
    NotificationManagerCompat.from(context).cancel(Constants.NOTIFICATION_ID)
}

/**
 * Method for sleeping for a fixed about of time to emulate slower work
 */
fun sleep() {
    try {
        Thread.sleep(Constants.mDelay, 0)
    } catch (e: InterruptedException) {
        Timber.e(e.message ?: "")
    }
}
