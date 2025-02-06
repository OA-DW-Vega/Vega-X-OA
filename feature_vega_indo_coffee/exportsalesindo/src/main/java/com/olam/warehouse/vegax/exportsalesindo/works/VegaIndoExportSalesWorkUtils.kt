package com.olam.warehouse.vegax.exportsalesindo.works

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint

/**
 * Created by Baskaran Kannan on 4/27/2021.
 */

fun getIndoExportSalesOneTimeRequestWorker(deliveryId: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaIndoCoffeeExportSalesPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(deliveryId).build()
}
