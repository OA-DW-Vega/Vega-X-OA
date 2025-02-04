package com.olam.warehouse.vegax.grnnicaragua.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.presentation.utils.constraintWithoutNetwork

/**
 * Created by Baskaran Kannan on 9/30/2020.
 */

fun getGrnOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaNicaraguaGrnPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraintWithoutNetwork).setInputData(data).build()
}

fun getGrnLotSequnceOneTimeRequestWorker(data: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaLotSequnceUpdateWorker::class.java)
        .setConstraints(constraint).setInputData(data).build()
}
