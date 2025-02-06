package com.olam.warehouse.master.dorigin.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.dorigin.entity.DOQuality
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
class DOWeighBridgeWithQualityParams {
    @Embedded
    lateinit var qualityWBDetails: DOQualityWBDetails
    @Relation(parentColumn = "wbTempId", entityColumn = "wbTempId", entity = DOQuality::class)
    var quality: List<DOQuality> = emptyList()
}
