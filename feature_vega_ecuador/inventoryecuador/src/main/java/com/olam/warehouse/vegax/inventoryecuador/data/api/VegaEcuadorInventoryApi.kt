package com.olam.warehouse.vegax.inventoryecuador.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventoryecuador.data.domain.model.VegaInventoryAndSyncModel
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
interface VegaEcuadorInventoryApi {
    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getEcuadorInventoryList(@Query("key") key: String): GenericReqAndResp<VegaInventoryAndSyncModel>
}
