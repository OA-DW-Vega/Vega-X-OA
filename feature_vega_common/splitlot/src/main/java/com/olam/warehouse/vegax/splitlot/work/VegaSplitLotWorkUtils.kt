package com.olam.warehouse.vegax.splitlot.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint

/**
 * Created by Baskaran Kannan on 10/5/2022.
 */

fun getSplitPrintTicketOneTimeRequestWorker(data: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaSplitLotPrintTicketWorker::class.java)
        .setConstraints(constraint).setInputData(data).build()
}