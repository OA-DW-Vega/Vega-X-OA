package com.olam.warehouse.master.dorigin.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.dorigin.entity.DOQualitative
import com.olam.warehouse.master.dorigin.entity.DOQualityParameter

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
class DOQualityParamsWithQualitative {
    @Embedded
    lateinit var qualityParameter: DOQualityParameter
    @Relation(parentColumn = "nameChar", entityColumn = "nameChar", entity = DOQualitative::class)
    var qualitative: List<DOQualitative>? = emptyList()
}
