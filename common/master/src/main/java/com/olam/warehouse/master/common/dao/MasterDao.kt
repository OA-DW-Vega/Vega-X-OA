package com.olam.warehouse.master.common.dao

import androidx.room.Dao
import androidx.room.Query

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Dao
abstract class MasterDao {

    @Query("DELETE FROM DOQualityWBDetails where weighBridgeId = :wbid")
    abstract fun updateWBDB(wbid: String)

    @Query("DELETE FROM DOQuality where wbid = :wbid")
    abstract fun deleteOfflineParams(wbid: String)

}
