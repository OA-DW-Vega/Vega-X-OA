package com.olam.warehouse.vegax.bagissuenigeriacocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.veganigeria.utils.portPlantIdList
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.api.VegaNigeriaCocoaBagIssueApi
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagIssuePost
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagManagementResp
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaCurrentBagsIssued


/**
 * Created by Roshna Parambil on 11/29/2020.
 */
interface VegaNigeriaCocoaBagIssueRepository {
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>>>>
    suspend fun postBagIssueData(bagIssuePost: VegaNigeriaCocoaBagIssuePost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaBagManagementResp>>>
    suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>
    suspend fun getSuppliers(selectedPlantId: String): LiveData<List<VegaVendor>>
}

class VegaNigeriaCocoaBagIssueRepositoryImpl(
    private val api: VegaNigeriaCocoaBagIssueApi,
    private val dao: VegaReceivingDao
) : VegaNigeriaCocoaBagIssueRepository {

    val werks = PreferenceHelper.get(Constants.WERKS, "")

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getMaterials() = dao.getMaterials()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>> =
                api.getCurrentBagsIssued(currentKey,materialCode,supplierCode)
        }.build().asLiveData()
    }

    override suspend fun postBagIssueData(bagIssuePost: VegaNigeriaCocoaBagIssuePost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaBagManagementResp>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaCocoaBagManagementResp>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaCocoaBagManagementResp> =
                api.postBagIssueData(bagIssuePost)
        }.build().asLiveData()
    }

    override suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaRminLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaRminLots>> =
                api.getStocks(currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getSuppliers(selectedPlantId: String) =
        if (portPlantIdList.contains(selectedPlantId)) {
            dao.getSuppliers()
        }else{
            dao.getSuppliersPlant(selectedPlantId)
        }

}
