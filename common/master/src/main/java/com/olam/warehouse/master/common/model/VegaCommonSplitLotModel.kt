package com.olam.warehouse.master.common.model

import android.os.Parcelable
import com.olam.warehouse.presentation.enums.Status
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaCommonSplitLotModel(
    var position: Int? = 0,
    var batchNumber: String? = "",
    var parentBatchNumber: String? = "",
    var ticketNumber: String? = "",
    var grnNumber: String? = "",
    var netWeight: String? = "",
    var certification: String? = "",
    var qualityGrade: String? = "",
    var qualityGradeDesc: String? = "",
    var bagCount: String? = "",
    var danoValue: String? = "",
    var qcStatus: Status? = Status.SYNC_PENDING
): Parcelable
