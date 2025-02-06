package com.olam.warehouse.presentation.data.api

import com.olam.warehouse.presentation.data.domain.model.TruckManagementSeasonResponse
import com.olam.warehouse.presentation.data.domain.model.TruckManagementVehicleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Created by Roshna Parambil on 22-06-2021.
 */
interface TruckManageApi {
    @GET("seasonManagement/mobile/findAll")
    suspend fun getSeasonDetails(): TruckManagementSeasonResponse

    @POST("vehicleManagement/mobile/getVehicleBySeasonId")
    suspend fun getTruckDetails(@Body seasonId: String): TruckManagementVehicleResponse
}
