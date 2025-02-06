package com.olam.warehouse.master.dorigin.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.dorigin.entity.DOQualitative
import com.olam.warehouse.master.dorigin.entity.DOQuality

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
class DOQualityWithQualitative {
    @Embedded
    lateinit var quality: DOQuality
    @Relation(parentColumn = "nameChar", entityColumn = "nameChar", entity = DOQualitative::class)
    var qualitative: List<DOQualitative>? = emptyList()
}
