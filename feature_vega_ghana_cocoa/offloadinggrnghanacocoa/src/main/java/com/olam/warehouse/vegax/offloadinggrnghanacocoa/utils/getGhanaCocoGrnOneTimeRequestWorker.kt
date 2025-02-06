package com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.master.work.VegaGhanaCocoaOffloadingGrnPostWorker
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.work.VegaGhanaCocoaGrnWhNoPostWorker

fun getGhanaCocoGrnOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {

    return OneTimeWorkRequest.Builder(VegaGhanaCocoaGrnWhNoPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
