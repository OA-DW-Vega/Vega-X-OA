package com.olam.warehouse.odreceiving.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.odreceiving.work.DOReceivingPostWorker
import com.olam.warehouse.presentation.utils.constraint

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
fun getReceivingOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(DOReceivingPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
