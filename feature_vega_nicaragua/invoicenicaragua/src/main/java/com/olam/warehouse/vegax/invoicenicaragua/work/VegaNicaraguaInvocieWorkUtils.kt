package com.olam.warehouse.vegax.invoicenicaragua.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.presentation.utils.constraintWithoutNetwork

/**
 * Created by Baskaran Kannan on 9/30/2020.
 */

fun getInvoiceOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaNicaraguaInvoicePostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraintWithoutNetwork).setInputData(data).build()
}

fun getGrnInvoiceSequnceOneTimeRequestWorker(data: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaInvoiceSequnceUpdateWorker::class.java)
        .setConstraints(constraint).setInputData(data).build()
}

