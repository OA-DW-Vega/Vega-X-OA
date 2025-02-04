package com.olam.warehouse.vegax.ginningwarehouse.ui.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.olam.warehouse.presentation.utils.constraintWithoutNetwork
import com.olam.warehouse.vegax.ginningwarehouse.ui.work.IncomingMtnPostWorker
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 23-10-2019.
 */

fun getMtnOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(IncomingMtnPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraintWithoutNetwork).setInputData(data).build()
}

fun enQueueWorker(worker: OneTimeWorkRequest, context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork(Random.nextInt().toString(), ExistingWorkPolicy.KEEP, worker)
}




