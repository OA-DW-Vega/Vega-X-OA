package com.olam.warehouse.vegax.createmapar.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.createmapar.data.api.ArApi
import com.olam.warehouse.vegax.createmapar.data.domain.model.ArLotDetails

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */

interface ArRepository {
    suspend fun saveLotDetails(lotDetails: ArLotDetails): LiveData<Resource<ArLotDetails>>
    suspend fun getLotDetails(): LiveData<Resource<List<ArLotDetails>>>
}

class ArRepositoryImpl(private val api: ArApi) : ArRepository {
    var plantId = PreferenceHelper.get(Constants.WERKS, "")
    override suspend fun saveLotDetails(lotDetails: ArLotDetails): LiveData<Resource<ArLotDetails>> {
        return object : NetworkOnlyBoundResource<ArLotDetails>() {
            override suspend fun createCall(): ArLotDetails =
                api.saveLotDetails(lotDetails)
        }.build().asLiveData()
    }

    override suspend fun getLotDetails(): LiveData<Resource<List<ArLotDetails>>> {
        return object : NetworkOnlyBoundResource<List<ArLotDetails>>() {
            override suspend fun createCall(): List<ArLotDetails> =
                api.getLotDetails(plantId)
        }.build().asLiveData()
    }

}