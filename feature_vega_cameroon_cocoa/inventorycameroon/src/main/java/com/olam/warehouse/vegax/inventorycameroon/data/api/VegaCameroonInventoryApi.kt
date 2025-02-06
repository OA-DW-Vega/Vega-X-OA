package com.olam.warehouse.vegax.inventorycameroon.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.VegaInventoryAndSyncModel
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
interface VegaCameroonInventoryApi {
    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getEcuadorInventoryList(@Query("key") key: String): GenericReqAndResp<VegaInventoryAndSyncModel>
}
