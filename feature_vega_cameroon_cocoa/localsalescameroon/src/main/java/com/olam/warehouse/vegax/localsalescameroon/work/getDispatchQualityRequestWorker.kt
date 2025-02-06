package com.olam.warehouse.vegax.localsalescameroon.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint

fun getDispatchQualityRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaNicMtnrQualityWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
