package com.olam.warehouse.vegax.mtntindo.works

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.master.work.VegaEcuadorDispatchPostWorker
import com.olam.warehouse.presentation.utils.constraint

/**
 * Created by Baskaran Kannan on 4/26/2021.
 */

fun getEcuadorDispatchOneTimeRequestWorker(deliveryId: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaEcuadorDispatchPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(deliveryId).build()
}
