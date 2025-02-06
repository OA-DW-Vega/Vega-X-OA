package com.olam.warehouse.vegax.offloadingcameroon.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.offloadingcameroon.ui.work.VegaCmOffloadingPrintReceiptWorker

fun getCmCocoOneTimeRequestWorker(data: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaCmOffloadingPrintReceiptWorker::class.java)
        .setConstraints(constraint).setInputData(data).build()
}
