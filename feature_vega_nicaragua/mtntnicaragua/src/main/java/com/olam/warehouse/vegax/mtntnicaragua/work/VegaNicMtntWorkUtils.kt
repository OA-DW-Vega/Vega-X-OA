package com.olam.warehouse.vegax.mtntnicaragua.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint

/**
 * Created by Baskaran Kannan on 12/7/2020.
 */

fun getMtntWSOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaNicaraguaMtntPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getDispatchQualityRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaNicMtntQualityWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
