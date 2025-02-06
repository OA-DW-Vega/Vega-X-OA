package com.olam.warehouse.vegax.dispatch.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.dispatch.work.VegaDispatchQualityWorker

/**
 * Created by Baskaran Kannan on 2/28/2020.
 */

fun getDispatchQualityRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaDispatchQualityWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
