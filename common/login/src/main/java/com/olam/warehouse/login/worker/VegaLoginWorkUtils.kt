package com.olam.warehouse.login.worker

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint

/**
 * Created by Baskaran Kannan on 9/30/2020.
 */

fun updateQuickPinOneTimeRequestWorker(data: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaQuickPinUpdateWorker::class.java)
        .setConstraints(constraint).setInputData(data).build()
}
