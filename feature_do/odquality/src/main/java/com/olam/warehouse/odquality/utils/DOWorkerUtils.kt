package com.olam.warehouse.odquality.utils

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.odquality.work.DOQualityPostWorker
import com.olam.warehouse.presentation.utils.constraint

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
fun getQualityOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(DOQualityPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
