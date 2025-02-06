package com.olam.warehouse.vegax.stockrecon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaStockReconDao
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.master.vega.model.VegaStockReconIdDetails
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.stockrecon.data.api.VegaStockReconApi
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportAuditDetails
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportReconList
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconCreateReconIdReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataResp
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPrintRecipt
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconUpdateStatus

interface VegaStockReconRepo {

    suspend fun getStockList(
        materialList: java.util.ArrayList<String>,
        plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>>

    suspend fun validateLot(batchNumber: String): VegaDispatchLots
    suspend fun getLotDetails(
        charge: String,
        whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>>

    suspend fun getStockReconInprogressCompletedList(
        plantId: String,
        storageLocation: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaStockReconIdDetails>>>>

    suspend fun getStockReconId(req: VegaStockReconCreateReconIdReq): LiveData<Resource<GenericReqAndResp<VegaStockReconIdDetails>>>

    suspend fun postAuditData(req: VegaStockReconPostAuditDataReq): LiveData<Resource<GenericReqAndResp<VegaStockReconPostAuditDataResp>>>

    suspend fun fetchAllAuditData(reconId: String): LiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>>

    suspend fun updateReconIdStatus(
        reconId: String,
        status: String
    ): LiveData<Resource<GenericReqAndResp<VegaStockReconUpdateStatus>>>

    suspend fun deleteAuditData(reconId: String, auditId: String): LiveData<Resource<GenericMessage>>

    suspend fun getSAPMaterials(): LiveData<List<VegaPackageMaterial>>

    suspend fun getVegaMaterials(): LiveData<List<VegaMaterial>>

    suspend fun postPrintRecipt(req: VegaStockReconPrintRecipt): LiveData<Resource<GenericReqAndResp<VegaStockReconPrintRecipt>>>

    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    suspend fun getReconReportAuditList(reconId: String): LiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>>

    suspend fun getReconReportReconList(
        plantId: String,
        fromDate: String,
        toDate: String
    ): LiveData<Resource<GenericReqAndResp<VegaReconReportReconList>>>

    suspend fun getReconReportAuditDetails(
        reconId: String,
        auditId: String
    ): LiveData<Resource<GenericReqAndResp<VegaReconReportAuditDetails>>>

    suspend fun getAuditImage(reconId: String, auditId: String): LiveData<Resource<GenericReqAndResp<String>>>

    suspend fun getReconReportImage(reconId: String): LiveData<Resource<GenericReqAndResp<String>>>

}


class VegaStockReconRepoImpl(private val dao: VegaStockReconDao, private val api: VegaStockReconApi) :
    VegaStockReconRepo {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    var emptyList: List<String> = arrayListOf()

    override suspend fun getStockList(
        materialList: ArrayList<String>,
        plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorDispatchStocks>> =
                api.getStockList(getCurrentKey(), plantId)
        }.build().asLiveData()
    }

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)

    override suspend fun getLotDetails(
        charge: String,
        whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaDispatchLots>> =
//                api.getLot(currentKey, charge, emptyList, whId)
                api.getLotInfo(currentKey, charge, "", whId)
        }.build().asLiveData()
    }

    override suspend fun getStockReconInprogressCompletedList(
        plantId: String,
        storageLocation: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaStockReconIdDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaStockReconIdDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaStockReconIdDetails>> =
                api.getStockReconInProgressAndCompetedList(plantId, storageLocation)
        }.build().asLiveData()
    }

    override suspend fun getStockReconId(req: VegaStockReconCreateReconIdReq): LiveData<Resource<GenericReqAndResp<VegaStockReconIdDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaStockReconIdDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaStockReconIdDetails> =
                api.getReconId(req)
        }.build().asLiveData()
    }

    override suspend fun postAuditData(req: VegaStockReconPostAuditDataReq): LiveData<Resource<GenericReqAndResp<VegaStockReconPostAuditDataResp>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaStockReconPostAuditDataResp>>() {
            var reconId = req.reconId
            override suspend fun createCall(): GenericReqAndResp<VegaStockReconPostAuditDataResp> =
                api.postAuditData(req, reconId!!)
        }.build().asLiveData()
    }

    override suspend fun fetchAllAuditData(reconId: String): LiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaStockReconGetAllAuditData>> =
                api.getAllAuditData(reconId)
        }.build().asLiveData()
    }

    override suspend fun updateReconIdStatus(
        reconId: String,
        status: String
    ): LiveData<Resource<GenericReqAndResp<VegaStockReconUpdateStatus>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaStockReconUpdateStatus>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaStockReconUpdateStatus> =
                api.updatReconIdStatus(reconId, status)
        }.build().asLiveData()
    }

    override suspend fun deleteAuditData(reconId: String, auditId: String): LiveData<Resource<GenericMessage>> {
        return object : NetworkOnlyBoundResource<GenericMessage>() {
            override suspend fun createCall(): GenericMessage =
                api.deleteAuditData(reconId, auditId)
        }.build().asLiveData()
    }

    override suspend fun postPrintRecipt(req: VegaStockReconPrintRecipt): LiveData<Resource<GenericReqAndResp<VegaStockReconPrintRecipt>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaStockReconPrintRecipt>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaStockReconPrintRecipt> =
                api.postPrintReceipt(req)
        }.build().asLiveData()
    }

    override suspend fun getSAPMaterials() = dao.getMaterials()

    override suspend fun getVegaMaterials() = dao.getVegaMaterials()

    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getReconReportAuditList(reconId: String): LiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaStockReconGetAllAuditData>> =
                api.getReconReportAuditList(reconId)
        }.build().asLiveData()
    }

    override suspend fun getReconReportReconList(
        plantId: String,
        fromDate: String,
        toDate: String
    ): LiveData<Resource<GenericReqAndResp<VegaReconReportReconList>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReconReportReconList>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReconReportReconList> =
                api.getReconReportReconList(plantId, fromDate, toDate)
        }.build().asLiveData()
    }

    override suspend fun getReconReportAuditDetails(
        reconId: String,
        auditId: String
    ): LiveData<Resource<GenericReqAndResp<VegaReconReportAuditDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReconReportAuditDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReconReportAuditDetails> =
                api.getReconReportAuditDetails(reconId, auditId)
        }.build().asLiveData()
    }

    override suspend fun getAuditImage(
        reconId: String,
        auditId: String
    ): LiveData<Resource<GenericReqAndResp<String>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<String>>() {
            override suspend fun createCall(): GenericReqAndResp<String> =
                api.getAuditImage(reconId, auditId)
        }.build().asLiveData()
    }

    override suspend fun getReconReportImage(
        reconId: String
    ): LiveData<Resource<GenericReqAndResp<String>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<String>>() {
            override suspend fun createCall(): GenericReqAndResp<String> =
                api.getReconReportImage(reconId)
        }.build().asLiveData()

    }



}
