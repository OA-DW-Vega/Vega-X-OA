package com.olam.warehouse.vegax.forwardponicaragua.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.grnnicaragua.work.VegaNicaraguaAdvancePostWorker

/**
 * Created by Baskaran Kannan on 9/30/2020.
 */

fun getForwardPoOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaNicaraguaAdvancePostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}


