package com.olam.warehouse.master.common.data.domain.model

import android.util.Log
import com.google.gson.Gson
import com.olam.warehouse.master.common.model.Master
import com.olam.warehouse.master.common.model.TransactionListPojo
import com.olam.warehouse.master.common.model.TransactionMaster
import com.olam.warehouse.master.common.model.WeighBridgeListPojo
import com.olam.warehouse.master.common.utils.*
import com.olam.warehouse.master.dorigin.dao.DOQualityDao
import com.olam.warehouse.master.dorigin.dao.DOReceivingDao
import com.olam.warehouse.presentation.utils.PreferenceHelper

/**
 * Created by Baskaran Kannan on 17-11-2019.
 */

class ODMasterUseCase(
    private val doQualityDao: DOQualityDao,
    private val doReceivingDao: DOReceivingDao
) {
    suspend fun saveMasterData(listItem: List<Master>) {
        listItem.forEach { item ->
            if (item.key.split("_")[0].contains("DO")) {
                item.masterDTO.packingMaterials?.let { doReceivingDao.insertBagType(prepareDOPackingMaterial(it)) }
                item.masterDTO.storageLocations?.let { doReceivingDao.insertLocation(prepareDOStorageLocation(it)) }
                item.masterDTO.sapMaterials?.let { doReceivingDao.insertMaterial(prepareDOSapMaterial(it)) }
                item.masterDTO.vendors?.let { doReceivingDao.insertVendor(prepareDOVendor(it)) }
                item.masterDTO.warehouses?.let { doReceivingDao.insertWarehouse(prepareDOWarehouse(it)) }
                //item.masterDTO.purchaseOrders?.let { insertPurchasesOrder(it) }
                item.masterDTO.materials?.let { material ->
                    material.let {
                        it.forEachIndexed { index, qualityParameter -> qualityParameter.position = index }
                        doQualityDao.insertQParam(prepareDOQualityParams(it))
                        it.forEach { qua ->
                            qua.qualitative?.let { it1 ->
                                it1.forEach { item ->
                                    item.nameChar = qua.nameChar
                                    item.materialCode = qua.materialCode
                                    doQualityDao.insertQualitative(
                                        prepareDOQualitative(
                                            item
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                item.masterDTO.customStLocation?.let { location ->
                    doReceivingDao.insertCustomStLocation(prepareDOCustomStLocation(location))
                    location.forEach {
                        it.binDetails?.forEach { bin ->
                            bin.procureLocationCode = it.procureLocationCode
                            doReceivingDao.insertBinDetails(prepareDOBinDetails(bin))
                        }

                    }
                }
            }
        }
    }

    suspend fun updateTempIdToWbid(wbid: String, grossWeight: String, tempId: String) =
        doQualityDao.updateTempIdToWbid(wbid, grossWeight, tempId)

    suspend fun saveTrnasMasterData(data: List<TransactionMaster>) {
        data.forEach {
            if (it.key?.split("_")?.get(0)?.contains("DO")!!) {
                it.masterDTO.wbListDetailsDTO?.forEach { item ->
                    if (doQualityDao.isWBExist(item.weighBridgeId).isNotEmpty()) return@forEach
                    //    it.wbTempId = it.wbid
                    doQualityDao.insertWeighBridge(prepareDOQualityWBData(item))
                }
                it.masterDTO.wbListDetailsDTO?.forEach { item ->
                    //Log.i("it.masterDTO.wbListDetailsDTO", item.toString())
                }
                val wbMasterPojo = WeighBridgeListPojo(it.masterDTO.wbListDetailsDTO!!)
                val wbStr: String = Gson().toJson(wbMasterPojo, WeighBridgeListPojo::class.java)
                PreferenceHelper.save("wbData", wbStr)

                Log.i("wbMasterData", wbStr)

                /*it.masterDTO.doTransList?.forEach { transItem ->
                    if (doReceivingDao.isTarnsactionIdExist(transItem.lotTransactionId).isNotEmpty()) return@forEach
                    doReceivingDao.insertTransactionDetail(transItem)
                }*/

                doReceivingDao.deleteAllDispatchDetails()
                it.masterDTO.doDispatchDetails?.forEach { dispatchDetail ->
                    doReceivingDao.insertDispatchDetail(dispatchDetail)
                }

                it.masterDTO.doTransList?.forEach { txn ->
                    //Log.i("it.masterDTO.doTransList", txn.toString())
                }
                val txnMasterPojo = TransactionListPojo(it.masterDTO.doTransList?: emptyList())
                val str: String = Gson().toJson(txnMasterPojo, TransactionListPojo::class.java)
                PreferenceHelper.save("txnData", str)

                Log.i("txnMasterData", str)

            }
        }
    }

    fun getAllDOBagsOfflineInfo() = doReceivingDao.getAllDOBagsOfflineInfo()
}
