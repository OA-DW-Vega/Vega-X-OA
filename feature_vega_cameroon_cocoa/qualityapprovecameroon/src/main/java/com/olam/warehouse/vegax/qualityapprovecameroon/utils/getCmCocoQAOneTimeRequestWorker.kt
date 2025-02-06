package com.olam.warehouse.vegax.qualityapprovecameroon.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.work.VegaCMQualityApprovalPrintReceiptWorker

fun getCmCocoQAOneTimeRequestWorker(data: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaCMQualityApprovalPrintReceiptWorker::class.java)
        .setConstraints(constraint).setInputData(data).build()
}
