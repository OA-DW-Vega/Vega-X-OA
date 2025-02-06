package com.olam.warehouse.vegax.thirdpartysalescoffee.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.thirdpartysalescoffee.work.VegaTPLotSequnceUpdateWorker

/**
 * Created by Baskaran Kannan on 6/2/2023.
 */

fun getThirdPartyLotSequnceOneTimeRequestWorker(data: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaTPLotSequnceUpdateWorker::class.java)
        .setConstraints(constraint).setInputData(data).build()
}