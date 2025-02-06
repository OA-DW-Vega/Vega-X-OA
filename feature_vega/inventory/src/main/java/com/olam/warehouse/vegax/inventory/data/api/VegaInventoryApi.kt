package com.olam.warehouse.vegax.inventory.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventory.data.domain.model.VegaInventoryAndSyncModel
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaInventoryApi {

    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getInventoryList(@Query("key") key: String): GenericReqAndResp<VegaInventoryAndSyncModel>
}
