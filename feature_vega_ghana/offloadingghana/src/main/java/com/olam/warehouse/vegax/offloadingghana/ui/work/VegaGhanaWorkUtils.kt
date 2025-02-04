package com.olam.warehouse.vegax.offloadingghana.ui.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint

/**
 * Created by Baskaran Kannan on 9/30/2020.
 */

fun getMtnrOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaGhanaMtnrPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
