package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.work

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.olam.warehouse.presentation.utils.constraint


fun getMtnrOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaGhanaCocoaMtnrPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}
