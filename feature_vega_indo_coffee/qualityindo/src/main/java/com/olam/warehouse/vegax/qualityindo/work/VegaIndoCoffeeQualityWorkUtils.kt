package com.olam.warehouse.vegax.qualityindo.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraintWithoutNetwork

/**
 * Created by Baskaran Kannan on 4/23/2021.
 */

fun getQualityOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaIndoCoffeeQualityPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraintWithoutNetwork).setInputData(data).build()
}
