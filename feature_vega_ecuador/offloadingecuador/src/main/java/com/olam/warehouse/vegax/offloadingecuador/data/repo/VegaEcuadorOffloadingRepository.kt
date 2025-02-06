package com.olam.warehouse.vegax.offloadingecuador.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.offloadingecuador.data.api.VegaEcuadorOffloadingApi
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
interface VegaEcuadorOffloadingRepository {
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>>
    suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial)
    suspend fun deleteBagDetails(id: Int, tmpWbId: String)
    suspend fun clearBagDetails()
    suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>
    suspend fun postEcuadorOffloadingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun saveOffloading(receivingData: VegaReceiving)
    suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>)
    suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>>
    suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>>
    suspend fun updateDeletedItem(tmpWbId: String)
    suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String)

    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getFeatureMaster(module: String): LiveData<List<VegaFeatureMaster>>
}

class VegaEcuadorOffloadingRepositoryImpl(
    private val api: VegaEcuadorOffloadingApi,
    private val dao: VegaEcuadorOffloadingDao,
    private val masterDao: MasterDao
) : VegaEcuadorOffloadingRepository {
    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getSuppliers() = dao.getSuppliers()

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>> = dao.getPOListLocal()

    override suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) = dao.saveBagDetails(material)
    override suspend fun deleteBagDetails(id: Int, tmpWbId: String) = dao.deleteBagDetails(id, tmpWbId)
    override suspend fun clearBagDetails() = dao.clearBagDetails()
    override suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return if (poId.isEmpty()) dao.getBagItems(materialCode, supplierCode, type, tmpWbId) else dao.getBagItems(
            materialCode,
            supplierCode,
            type,
            poId,
            tmpWbId
        )
    }

    override suspend fun postEcuadorOffloadingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postEcuadorOffloadingDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun saveOffloading(receivingData: VegaReceiving) = dao.saveOffloading(receivingData)
    override suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) =
        dao.saveReceivingLineItems(bagList)

    override suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        dao.getOffloadingWithLineItem()

    override suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        dao.getOffloadingWithLineItemCount()

    override suspend fun updateDeletedItem(tmpWbId: String) {
        dao.deleteOffloadingItem(tmpWbId)
        dao.deleteBagItem(tmpWbId)
        if (tmpWbId.contains("TMP")) {
            dao.deleteGrnData(tmpWbId)
            dao.updateDeletedItem(tmpWbId)
        }
    }

    override suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) {
        dao.updateTempIdToWbid(tmpWbid, wbid)
    }

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }
    override suspend fun getFeatureMaster(module: String) =
        dao.getFeatureMaster(module)
}
