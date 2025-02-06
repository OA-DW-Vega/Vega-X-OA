package com.olam.warehouse.vegax.qualitycoffee.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.qualitycoffee.work.VegaPrintFileReceiptWorker


fun getPrintTicketOneTimeRequestWorker(data: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaPrintFileReceiptWorker::class.java)
        .setConstraints(constraint).setInputData(data).build()
}
