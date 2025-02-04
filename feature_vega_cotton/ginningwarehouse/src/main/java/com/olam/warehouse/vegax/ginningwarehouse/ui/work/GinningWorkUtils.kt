package com.olam.warehouse.ginning.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint
import com.olam.warehouse.vegax.ginningwarehouse.ui.work.GinningBaleTransMasterDataWorker
import com.olam.warehouse.vegax.ginningwarehouse.ui.work.GinningTransDeliveryMasterDataWorker

/**
 * Created by Baskaran Kannan on 3/26/2020.
 */


fun dispatchPostWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(GinningDispatchPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getTransBaleOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(GinningBaleTransMasterDataWorker::class.java)
            .addTag(tag.toString())
            .setConstraints(constraint).setInputData(data).build()
}

fun getTransDeliveryOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(GinningTransDeliveryMasterDataWorker::class.java)
            .addTag(tag.toString())
            .setConstraints(constraint).setInputData(data).build()
}
