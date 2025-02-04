package com.olam.warehouse.portwarehouse.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.work.PortIncommingMtnPostWorker
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 23-10-2019.
 */


fun enQueueWorker(worker: OneTimeWorkRequest, context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork(Random.nextInt().toString(), ExistingWorkPolicy.KEEP, worker)
}

fun getMtnOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(PortIncommingMtnPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}



