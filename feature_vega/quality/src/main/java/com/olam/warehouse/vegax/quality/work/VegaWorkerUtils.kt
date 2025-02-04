package com.olam.warehouse.vegax.quality.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint

fun getQualityOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaQualityPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
