package com.olam.warehouse.presentation.data.api

import com.olam.warehouse.presentation.data.domain.model.TruckManagementSeasonResponse
import com.olam.warehouse.presentation.data.domain.model.TruckManagementVehicleResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TruckManageOffflineApi {

    @GET("seasonManagement/mobile/findAll")
    fun getSeasonDetailsOffline(): Call<TruckManagementSeasonResponse>

    @POST("vehicleManagement/mobile/getVehicleBySeasonId")
    fun getTruckDetailsOffline(@Body seasonId: String): Call<TruckManagementVehicleResponse>
}
