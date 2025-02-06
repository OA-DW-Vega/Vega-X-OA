package com.olam.warehouse.vegax.processing.utils

import android.os.Build
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.work.VegaProcessingQualityWorker

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */

fun getProcessingQualityRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaProcessingQualityWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
