package com.olam.warehouse.vegax.grnindo.works

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraintWithoutNetwork

/**
 * Created by Baskaran Kannan on 4/26/2021.
 */

fun getIndoGrnOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaIndoCoffeeGrnPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraintWithoutNetwork).setInputData(data).build()
}
