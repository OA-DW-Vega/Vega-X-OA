package com.olam.warehouse.vegax.offloadingcocoa.work

import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import androidx.work.Data
/**
 * Created by Baskaran Kannan on 9/30/2020.
 */

fun getMtnrOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaCocoaMtnrPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
