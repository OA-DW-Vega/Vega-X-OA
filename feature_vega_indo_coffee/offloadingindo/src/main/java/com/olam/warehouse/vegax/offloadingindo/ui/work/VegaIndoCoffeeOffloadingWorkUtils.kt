package com.olam.warehouse.vegax.offloadingindo.ui.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraintWithoutNetwork

/**
 * Created by Baskaran Kannan on 4/22/2021.
 */

fun getOffloadingOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaIndoCoffeeOffloadingPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraintWithoutNetwork).setInputData(data).build()
}
