package com.olam.warehouse.master.common.data.api

import com.olam.warehouse.master.common.data.domain.model.ReleaseDeatils
import com.olam.warehouse.master.common.data.domain.model.ReleaseUrls
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url


/**
 * Created by Baskaran Kannan on 27-07-2020.
 */

interface AppCenterUrlApi {
    @Streaming
    @GET
    fun getReleaseId(@Url fileUrl: String): Call<List<ReleaseDeatils>>

    @Streaming
    @GET
    fun getReleaseUrl(@Url fileUrl: String): Call<ReleaseUrls>

}
