package com.olam.warehouse.master.common.data.domain

import android.util.Log
import androidx.lifecycle.LiveData
import com.airbnb.lottie.LottieCompositionFactory.fromJson
import com.google.gson.Gson
import com.olam.warehouse.master.common.dao.TrackTraceDao
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.model.Master
import com.olam.warehouse.master.common.model.TransactionListPojo
import com.olam.warehouse.master.common.model.TransactionMaster
import com.olam.warehouse.master.common.model.WeighBridgeListPojo
import com.olam.warehouse.master.common.model.ftdcItem
import com.olam.warehouse.master.common.utils.*
import com.olam.warehouse.master.dorigin.dao.DOQualityDao
import com.olam.warehouse.master.dorigin.dao.DOReceivingDao
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.user.model.NotifyModuleList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.dao.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaRminBomWithLots
import com.olam.warehouse.master.vegacocoa.dao.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnItemWithGrades
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaRminItemWithLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeOffloadDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorDispatchDao
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.master.vegaghana.dao.VegaGhanaProcessingDao
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaAdvanceDao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaForwardPODao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceTransactionDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 17-11-2019.
 */

class MasterUseCase(
    private val doQualityDao: DOQualityDao,
    private val doReceivingDao: DOReceivingDao,
    private val vegaQualityDao: VegaQualityDao,
    private val vegaReceivingDao: VegaReceivingDao,
//    private val vegaContainerDao: VegaCameroonContainerDao,
    private val vegaMtntDao: VegaMtntDao,
    private val vegaDispatchDao: VegaDispatchDao,
    private val vegaProcessingDao: VegaProcessingDao,
    private val vegaCocoaDispatchDao: VegaCocoaDispatchDao,
    private val vegaCocoaRminDao: VegaCocoaRMinDao,
    private val vegaCocoaSalesDao: VegaCocoaSalesDao,
    private val vegaEcuadorGrnDao: VegaEcuadorGrnDao,
    private val vegaCoCoaQualityDao: VegaCoCoaQualityDao,
    private val vegaEcuadorDispatchDao: VegaEcuadorDispatchDao,
    private val vegaNicaraguaGrnDao: VegaNicaraguaGrnDao,
    private val vegaCoCoaOffloadDao: VegaCoCoaOffloadDao,
    private val vegaNicaraguaForwardPODao: VegaNicaraguaForwardPODao,
    private val vegaNicaraguaAdvanceDao: VegaNicaraguaAdvanceDao,
    private val vegaGhanaProcessingDao: VegaGhanaProcessingDao,
    private val vegaCoffeeOffloadDao: VegaCoffeeOffloadDao,
    private val sourceLotDao: TrackTraceDao
) : KoinComponent {
    suspend fun saveMasterData(listItem: List<Master>) {
        listItem.forEach { item ->
            if (item.key.split("_")[0].contains("DO")) {
                item.masterDTO.packingMaterials?.let {
                    doReceivingDao.insertBagType(
                        prepareDOPackingMaterial(it)
                    )
                }
                item.masterDTO.storageLocations?.let {
                    doReceivingDao.insertLocation(
                        prepareDOStorageLocation(it)
                    )
                }
                item.masterDTO.sapMaterials?.let {
                    doReceivingDao.insertMaterial(
                        prepareDOSapMaterial(it)
                    )
                }
                item.masterDTO.vendors?.let { doReceivingDao.insertVendor(prepareDOVendor(it)) }
                item.masterDTO.warehouses?.let {
                    doReceivingDao.insertWarehouse(
                        prepareDOWarehouse(
                            it
                        )
                    )
                }
                //item.masterDTO.purchaseOrders?.let { insertPurchasesOrder(it) }
                item.masterDTO.materials?.let { material ->
                    material.let {
                        it.forEachIndexed { index, qualityParameter ->
                            qualityParameter.position = index
                        }
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
            } else if (item.key.split("_")[0].contains("VEGA")) {
                vegaReceivingDao.clearMasterData()
                vegaQualityDao.clearMasterData()
                item.masterDTO.packingMaterials?.let { vegaReceivingDao.insertBagType(prepareVegaPackingMaterial(it)) }
                item.masterDTO.storageLocations?.let { vegaReceivingDao.insertLocation(prepareVegaStorageLocation(it)) }
                item.masterDTO.sapMaterials?.let {
                    vegaReceivingDao.insertMaterial(prepareVegaSapMaterial(it))
                    it.forEach { sapMaterial ->
                        sapMaterial.uomDetails?.let { it1->
                            it1.forEach { item ->
                                vegaReceivingDao.insertUomDetails(item)
                            }
                        }
                    }
                }
                item.masterDTO.plantRouteDetails?.let { vegaReceivingDao.insertPlantRoute(prepareVegaPlanRoute(it)) }
                item.masterDTO.materialStlocDetails?.let { vegaReceivingDao.insertMaterialStlocDetails(prepareVegaMaterialStlocDetails(it)) }
                item.masterDTO.vendors?.let { vegaReceivingDao.insertVendor(prepareVegaVendor(it)) }
                item.masterDTO.warehouses?.let { vegaReceivingDao.insertWarehouse(prepareVegaWarehouse(it)) }
                item.masterDTO.processingStage?.let { vegaReceivingDao.insertProcessingStage(prepareVegaProcessStage(it)) }
                item.masterDTO.qualityGradeDesc?.let { config ->
                    config.forEach {
                        it.paramName= it.paramName.replace("\\s+".toRegex(), " ")
                    }
                    vegaReceivingDao.insertQualitativeParams(config)
                }
                item.masterDTO.materialQualitGrades?.let {
                    vegaReceivingDao.insertMaterialQualityGrades(
                        prepareVegaMaterialQualityGrades(it)
                    )
                }
                item.masterDTO.thirdPartyMaterials?.let {
                    vegaReceivingDao.insertThirdPartyMaterial(
                        prepareCoffeeThirdPartyMaterial(it)
                    )
                }
                //item.masterDTO.purchaseOrders?.let { insertPurchasesOrder(it) }
                item.masterDTO.materials?.let { material ->
                    material.let {
                        it.forEachIndexed { index, qualityParameter -> qualityParameter.position = index }
                        vegaQualityDao.insertQParam(prepareVegaQualityParams(it))
                        it.forEach { qua ->
                            qua.qualitative?.let { it1 ->
                                it1.forEach { item ->
                                    if(item.nameChar!="LOBM_UDCODE"){
                                        item.charValue = item.charValue.replace("\\s+".toRegex(), " ")
                                    }else {
                                        item.charValue = item.charValue
                                    }
                                    item.nameChar = qua.nameChar
                                    item.materialCode = qua.materialCode
                                    vegaQualityDao.insertQualitative(
                                        prepareVegaQualitative(
                                            item
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                item.masterDTO.customStLocation?.let { location ->
                    vegaReceivingDao.insertCustomStLocation(prepareVegaCustomStLocation(location))
                    location.forEach {
                        it.binDetails?.forEach { bin ->
                            bin.procureLocationCode = it.procureLocationCode
                            vegaReceivingDao.insertBinDetails(prepareVegaBinDetails(bin))
                        }

                    }
                }
                item.masterDTO.bcZoneMapping?.let { bc ->
                    vegaReceivingDao.insertBcZone(prepareVegaBcZoneMapping(bc))
                }
                item.masterDTO.configInfo?.let { config ->
                    //vegaReceivingDao.deleteConfigItems()
                    config.forEach { item ->
                        item.configDetails?.let {
                            it.forEach { it1 ->
                                when (it1.materialCode) {
                                    null -> it1.materialCode = ""
                                    else -> it1.materialCode = it1.materialCode
                                }
                                when (it1.process) {
                                    null -> it1.process = ""
                                    else -> it1.process = it1.process
                                }
                                it1.cfgNo = it1.processStage?.cfgNo.toString()
                                it1.processStageName = it1.processStage?.processName.toString()
                                it1.sapClosureDaycount?.let {
                                    PreferenceHelper.save(Constants.SAP_CLOSURE_DAY_COUNT,it)
                                }
                                if (it1.rolekey.equals(Constants.VIRTUAL)) {
                                    when (it1.applicable) {
                                        "Y" -> PreferenceHelper.save(Constants.VIRTUAL, true)
                                        "N" -> PreferenceHelper.save(Constants.VIRTUAL, false)
                                    }
                                }
                                if(it1.rolekey.equals(Constants.PROCUREMENT))
                                {

                                    when (it1.applicable) {
                                        "Y" ->
                                        {
                                            PreferenceHelper.save(Constants.PROCUREMENT, true)
                                            val gson = GsonUtils()
                                            val configfwdPoMateriallist = arrayListOf<VegaConfigDetails>()
                                            configfwdPoMateriallist.addAll(it)
                                            PreferenceHelper.save(Constants.PROCUREMENT, gson.toJson(configfwdPoMateriallist))
                                        }
                                        "N" -> PreferenceHelper.save(Constants.PROCUREMENT, false)
                                    }
                                }
                                vegaReceivingDao.insertConfigDetail(it1)
                            }
                        }
                    }
                }
                item.masterDTO.miscellaneousList?.let { mis ->
                    vegaReceivingDao.insertMiscellaneousDetails(mis)
                }
                item.masterDTO.containerSizeList?.let { containerList ->
                    vegaReceivingDao.insertContainerSizeDetails(containerList)
                }
                item.masterDTO.shippingLineList?.let { shippingLineList ->
                    vegaReceivingDao.insertShippingLineList(shippingLineList)
                }
                item.masterDTO.notificationsModuleList?.let {
                    val gson = GsonUtils()
                    val notifyModuleList = arrayListOf<NotifyModuleList>()
                    notifyModuleList.addAll(it)
                    PreferenceHelper.save(Constants.NOTIFIY_MODULE_LIST, gson.toJson(notifyModuleList))
                }
                item.masterDTO.plantList?.let { pl ->
                    val gson = GsonUtils()
                    val plantList = arrayListOf<Plant>()
                    plantList.addAll(pl)
                    PreferenceHelper.save(Constants.PLANT_LIST, gson.toJson(plantList))
                }
                item.masterDTO.mtntPlantList?.let { pl ->
                    val gson = GsonUtils()
                    val mtntPlantList = arrayListOf<Plant>()
                    mtntPlantList.addAll(pl)
                    PreferenceHelper.save(Constants.MTNT_PLANT_LIST, gson.toJson(mtntPlantList))
                }
                item.masterDTO.multiPlantList?.let { pl ->
                    val gson = GsonUtils()
                    val multiPlantList = arrayListOf<Plant>()
                    multiPlantList.addAll(pl)
                    PreferenceHelper.save(Constants.MULTI_PLANT_LIST, gson.toJson(multiPlantList))
                }
                item.masterDTO.priceConfigDetails?.let { config ->
                    vegaReceivingDao.insertPriceConfigDetails(preparePriceConfigDetails(config))
                }
                item.masterDTO.positionGradeMappings?.let { config ->
                    vegaReceivingDao.insertPositionGradeMappings(preparePositionGradeMappings(config))
                }
                item.masterDTO.timestamp?.let { timeStamp ->
                    PreferenceHelper.save(Constants.LAST_SYNC_TIME, timeStamp)
                    vegaReceivingDao.saveLastSyncTime(VegaLastSyncTime(timeStamp))
                }

                item.masterDTO.lotSequence?.let { config ->
                    if (checkBatchSequence(config)) saveLotSequence(config)
                }
                item.masterDTO.invoiceSequence?.let { config ->
                    if (checkInvoiceSequence(config)) saveInvoiceSequenceMaster(config)
                }
                item.masterDTO.grnSequence?.let { config ->
                    if (checkGrnSequence(config)) saveGrnSequence(config)
                }
                item.masterDTO.mtntDocSequence?.let { config ->
                    if (checkMtntSequence(config)) saveMtntSequence(config)
                }
                item.masterDTO.mtnrDocSequence?.let { config ->
                    if (checkMtnrSequence(config)) saveMtnrSequence(config)
                }
                item.masterDTO.poSequence?.let { config ->
                    if (checkPoSequence(config)) savePoSequence(config)
                }
                item.masterDTO.tallySheetSequence?.let { config ->
                    if (checkTallySequence(config)) saveTallySequence(config)
                }

                item.masterDTO.tallySheetCropYear?.let { config ->
                    PreferenceHelper.save(Constants.CROP_YEAR, config)
                    PreferenceHelper.save(Constants.CROP_FINANCIAL_YEAR, config)

                }
                item.masterDTO.fgrnSequence?.let { config ->
                    if (checkFgrnBatchSequence(config)) saveFgrnBatchSequence(config)
                }
                item.masterDTO.fgrnTicketSequence?.let { config->
                    if(checkFGRNTallySequence(config)) saveFGRNTallySequence(config)
                }
                item.masterDTO.pileSequence?.let { config ->
                    if (checkPileSequence(config)) savePileSequence(config)
                }
                item.masterDTO.workflowProcessList?.let { items ->
                    vegaReceivingDao.saveWorkflowProcess(items)
                }
                try {
                    item.masterDTO.configFeaturesList?.let { item ->
                        vegaReceivingDao.insertFeatureMaster(prepareFeatureMater(item))
                    }
                } catch (exception: Exception) {
                    Log.e("Master data exception", "Config Features Exception")
                }
                item.masterDTO.workflowDetails?.let { items ->
                    items.forEach {
                        val gson = GsonUtils()
                        val workFlowList = arrayListOf<WorkflowFields>()
                        val flow = Gson().fromJson(it.workflow, WorkFlowItems::class.java)
                        workFlowList.addAll(flow.workflow)
                        if(!it.plantId.isNullOrEmpty()) {
                            PreferenceHelper.save(it.plantId.toString(), gson.toJson(workFlowList))
                        }else {
                            PreferenceHelper.save(it.storageLocCode.toString(), gson.toJson(workFlowList))
                        }
                    }
                }
                item.masterDTO.ftdcList?.let { ftdcItems ->
                    val gson = GsonUtils()
                    val ftdcList = arrayListOf<ftdcItem>()
                    ftdcList.addAll(ftdcItems)
                    PreferenceHelper.save(Constants.FTDC_LIST, gson.toJson(ftdcList))
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
                val txnMasterPojo = TransactionListPojo(it.masterDTO.doTransList!!)
                val str: String = Gson().toJson(txnMasterPojo, TransactionListPojo::class.java)
                PreferenceHelper.save("txnData", str)

            } else if (it.key?.split("_")?.get(0)?.contains("VEGA") == true) {
                try {
                    it.masterDTO.mtnDetails?.let { it1 -> vegaReceivingDao.saveWarehouseAndMtns(it1) }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.vegaProcessOrderTransDTO?.let { it1 ->
                        vegaReceivingDao.saveProcessOrders(it1)
                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.purchaseOrderList?.let { it1 ->
                        vegaReceivingDao.deletePurcheseOrder()
                        vegaReceivingDao.savePurcheseOrder(preparePurcheseOrder(it1.toMutableList()))
                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.dispatchPlantMTNTPurchaseOrderList?.let { it1 ->
                        vegaCocoaDispatchDao.deletePurcheseOrder()
                        vegaCocoaDispatchDao.savePurcheseOrder(
                            prepareVirtualPurchaseOrder(it1.toMutableList())
                        )
                        if (getCurrentKey().split("_")[2].contains("ARAB")) {
                            vegaEcuadorDispatchDao.deletePurcheseOrder()
                            vegaEcuadorDispatchDao.savePurchaseOrders(prepareIndoPurchaseOrder(it1.toMutableList()))
                        }

                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.mtntPurchaseOrderList?.let { po ->
                        po.forEach { it1 ->
                            if (!getCurrentKey().split("_")[2].contains("ARAB"))
                                vegaEcuadorDispatchDao.savePurchaseOrders(prepareMtntPurchaseOrder(it1.purchaseOrders.toMutableList()))
                        }
                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.grnCharDetails?.let { it1 ->
                        vegaNicaraguaGrnDao.deleteGrnCharDetails()
                        vegaNicaraguaGrnDao.saveGrnCharDetails(
                            prepareGrnCharDetails(
                                it1.toMutableList()
                            )
                        )
                    }
                } catch (e: Exception) { e.printStackTrace() }
                try {
                    it.masterDTO.exchangeRate?.let { it1 ->
                        vegaReceivingDao.deleteExchangeRate()
                        vegaReceivingDao.insertExchangeRate(prepareExchangeRate(it1))
                    }
                } catch (e: Exception) { e.printStackTrace() }
                try {
                    it.masterDTO.grnPriceDetails?.let { it1 ->
                        vegaReceivingDao.deleteGrnPriceDetails()
                        vegaReceivingDao.saveGrnPriceDetails(
                            prepareGrnPriceDetailsList(it1.toMutableList())
                        )
                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.mtnBatchQualityDTO?.let { it1 ->
                        vegaReceivingDao.saveMtnBatchNumbers(prepareMtnBatchNumberList(it1.toMutableList()))
                    }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    it.masterDTO.lotQualityDetails?.let { it1 ->
                        vegaReceivingDao.saveLotQualityDetails(prepareLotQualityDetailsList(it1.toMutableList()))
                    }
                } catch (e: Exception) { e.printStackTrace()}

                try {
                    it.masterDTO.vegaFetchBomResponseList?.let { it1 ->
                        var bomList = listOf<VegaProcessingRminBoms>()
                        bomList = prepareRminBomList(it1.toMutableList())
                        vegaGhanaProcessingDao.saveRminBomDetails(bomList)
                    }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    it.masterDTO.stockDetails?.let { it1 ->
                        vegaEcuadorDispatchDao.deleteStocks()
                        vegaEcuadorDispatchDao.saveStocks(prepareStocksList(it1.toMutableList()))
                    }
                } catch (e: Exception) { e.printStackTrace() }
                try {
                    it.masterDTO.advanceLineItems?.let { it1 ->
                        vegaNicaraguaGrnDao.saveAdvanceLineDetails(
                            prepareAdvanceLineDetails(it1.toMutableList())
                        )
                    }
                } catch (e: Exception) { e.printStackTrace() }
                try {
                    it.masterDTO.offlineInventory?.let { it1 ->
                        vegaNicaraguaGrnDao.deleteGrnInventoryDetails()
                        vegaNicaraguaGrnDao.saveGrnInventoryDetails(
                            prepareGrnInventoryDetails(
                                it1,
                                vegaReceivingDao.getProductsAll()
                            )
                        )
                    }
                } catch (e: Exception) { e.printStackTrace() }
                try {
                    it.masterDTO.salesOrderDetails?.let { it1 ->
                        it1.forEach { item ->
                            vegaCoffeeOffloadDao.saveIndoCoffeSalesOrder(item.salesOrderList)
                        }

                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.sourceLotDTO?.let { it1 ->
                        sourceLotDao.clearTTSourceLotDetails()
                        sourceLotDao.insertMultipleSourceLotDetails(it1)
                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                it.masterDTO.vendorTransactionDTO?.let { it1 ->
                    sourceLotDao.clearTTTransactionIdDetails()
                    sourceLotDao.saveFarmerLessTransactionDetails(prepareFarmerLessTransactionDetails(it1))
                }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.wbListDetailsDTO?.let { it1 ->
                        it1.forEach { item ->
                            if (!vegaQualityDao.isWBExist(item.weighBridgeId).isNullOrEmpty()) return@forEach
                            vegaQualityDao.insertWeighBridge(prepareVegaQualityWBData(item))

                            if (!vegaEcuadorGrnDao.isWBExist(item.weighBridgeId).isNullOrEmpty()) return@forEach
                            vegaEcuadorGrnDao.insertWeighBridge(prepareVegaGrnWBData(item))
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    it.masterDTO.wbListHeaderDetailsDTO?.let { it1 ->
                        it1.forEach { item ->

                            if (vegaCoCoaQualityDao.isQualityWBExist(item.weighBridgeId).isNotEmpty()) return@forEach
                            vegaCoCoaQualityDao.insertWeighBridge(item)
                        }
                    }
                } catch (e: Exception) { e.printStackTrace()}

                try {
                    it.masterDTO.vendorGRNDetails?.let { it1 ->
                        vegaReceivingDao.deleteGrnDetails()
                        it1.forEach { item ->
                            item.grnDetails?.let { it2 -> vegaReceivingDao.saveGrnDetails(it2) }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace()}

                try {
                    it.masterDTO.storageLocationLst?.let { it1 ->
                        vegaCoCoaOffloadDao.deleteAll()
                        it1.forEach { item ->
                            vegaCoCoaOffloadDao.insertStorageLocation(item)
                        }
                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.advanceDetails?.let { it1 ->
                        it1.forEach {
                            it.advanceCreationDetailsDTO!!.forEach { item ->
                                if (item.deletedFlag == true)
                                    vegaNicaraguaAdvanceDao.deleteAdvanceDetailsForCredit(item.vendor)
                            }
                        }
                        vegaNicaraguaAdvanceDao.savAdvanceDetails(
                            prepareAdvanceDetails(it1.toMutableList())
                        )
                    }
                } catch (e: Exception) { e.printStackTrace() }
                try {
                    it.masterDTO.invoiceDetails?.let { it1 ->
                        val supplier = vegaNicaraguaGrnDao.getSupplierList()
                        vegaNicaraguaGrnDao.saveInvoiceReceipt(
                            PrepareInvoiceReceiptData(
                                it1,
                                supplier as ArrayList<VegaVendor>
                            )
                        )
                    }
                } catch (e: Exception) { e.printStackTrace()}
                try {
                    it.masterDTO.receiptReprintDto?.let { it1 ->
                        val supplier = vegaNicaraguaGrnDao.getSupplierList()
                        val material = vegaNicaraguaGrnDao.getProductsLocal()
                        val materialQualityGrade =
                            vegaNicaraguaGrnDao.getAllMaterialQualityGradesOffline()
                        val qualityGrade = arrayListOf<VegaNicaraguaMaterialQualitGrades>()
                        qualityGrade.addAll(materialQualityGrade)
                        vegaNicaraguaGrnDao.saveGrnReceipt(
                            PrepareGrnReceiptData(
                                it1,
                                supplier as ArrayList<VegaVendor>,
                                material as ArrayList<VegaMaterial>,
                                qualityGrade
                            )
                        )
                    }
                } catch (e: Exception) { e.printStackTrace()}
            }
        }
    }


    suspend fun getMaterials() = vegaMtntDao.getMaterials()

//Transaction History for Cashew

    fun getReceivingItem() = vegaReceivingDao.getReceivingItem()
    fun getReceivingWithLineItem() = vegaReceivingDao.getReceivingWithLineItemAll()
    fun deleteWeighBride(tmpWbId: String) {
        vegaReceivingDao.deleteItemReceiving(tmpWbId)
    }

    suspend fun saveReceiving(receivingData: VegaReceiving) = vegaReceivingDao.save(receivingData)

    suspend fun getWeighBridgeDetail() = vegaQualityDao.getQualityWeighBridgeDetailAll()

    // suspend fun getWeighBridgeDetailqualitycount() = vegaQualityDao.getReceivingQualityDetail()
    suspend fun getWeighBridgeDetailqualitycount(): LiveData<List<VegaQualityWBDetails>> {
        val receiveItem = vegaQualityDao.getReceivingQualityDetail()
        receiveItem.forEach {
            if (vegaQualityDao.isWBExist(it.tmpWbId).isNotEmpty()) return@forEach
            vegaQualityDao.insertQualityWbDetail(prepareWeighBridgeData(it))
        }
        return vegaQualityDao.getQualityWeighBridgeDetail()
    }

    fun deleteQualityWeighBride(tmpWbId: String) {
        vegaQualityDao.updateWBDB(tmpWbId)
        vegaQualityDao.deleteOfflineParams(tmpWbId)
    }

    fun getMtntWithLineItem() = vegaMtntDao.getMtntWithLineItem()

    fun deleteMtnt(tmpWbId: String) = vegaMtntDao.deleteItemMtnt(tmpWbId)
    fun saveWB(wbid: String, batchNo: String, msg: String, status: Int) {
        if (status == 4) {
            vegaQualityDao.updateWBListStatusSuccess(wbid, batchNo, msg, status)
            vegaQualityDao.deleteOfflineParams(wbid)
        } else {
            vegaQualityDao.updateWBListStatusFail(wbid, batchNo, msg, status)
        }
    }

    fun saveMtnt(mtntData: VegaMtnt) = vegaMtntDao.insertMtnt(mtntData)
    fun getDispatchWithLots() = vegaDispatchDao.getDispatchWithLots()
    fun deleteDispatchItem(tmpWbId: String) {
        vegaDispatchDao.updateLotStatus(tmpWbId, "")
        vegaDispatchDao.deleteVegaDispatchTruckItem(tmpWbId)
    }

    fun savedispatch(dispatch: VegaDispatchTrucks) {
        vegaDispatchDao.updateDispatchStatus(
            dispatch.weighBridgeId,
            dispatch.isSyncStatus,
            dispatch.status!!,
            dispatch.message.toString(),
            dispatch.batchNumber.toString()
        )
    }

    fun getRminWithLots(): LiveData<List<VegaRminBomWithLots>> = vegaProcessingDao.getRminWithLots()
    fun deleteRmin(batchNumber: String) {
        vegaProcessingDao.deleteRminLots(batchNumber)
        vegaProcessingDao.deleteRmin(batchNumber)
    }

    fun deleteCocoaRMinLot(batchNumber: String) {
        vegaCocoaRminDao.deleteRminLots(batchNumber)
    }

    fun deleteCocoaRMinMaterial(chNo: String) {
        vegaCocoaRminDao.deleteRminMaterial(chNo)
    }


//Transaction History for Cocoa

    fun getMtntWithLots() = vegaCocoaDispatchDao.getMtntWithLots()
    fun deleteMtntItem(tmpWbId: String) {
        vegaCocoaDispatchDao.removeLotsFromLocal(tmpWbId)
        vegaCocoaDispatchDao.deleteVegaDispatchTrucks(tmpWbId)
    }

    fun savedispatchMtnt(dispatch: VegaCocoaDispatchWB) {
        vegaCocoaDispatchDao.updateDispatchMtntStatus(
            dispatch.weighBridgeId,
            dispatch.isSyncStatus,
            dispatch.status!!,
            dispatch.message.toString()
        )
    }

    fun getFgrnItems(): LiveData<List<VegaCocoaFgrnItemWithGrades>> = vegaCocoaRminDao.getFgrnItemsAll()
    fun deleteFgrnItem(fgrnId: String) {
        vegaCocoaRminDao.deleteAllBagDetails(fgrnId)
        vegaCocoaRminDao.deleteAllGrades(fgrnId)
        vegaCocoaRminDao.deleteFgrnItems(fgrnId)
    }

    fun getRminItems(): LiveData<List<VegaCocoaRminItemWithLots>> = vegaCocoaRminDao.getRminItemsAll()
    fun deleteRminItem(rminId: String) {
        vegaCocoaRminDao.deleteRminItems(rminId)
        vegaCocoaRminDao.deleteRminLotItems(rminId)
    }

    fun updateGrn(wbid: String, grnNo: String, batch: String, msg: String, status: Int) {
        if (status == 4) vegaEcuadorGrnDao.updateGrnSuccess(wbid, grnNo, batch, msg, status)
        else vegaEcuadorGrnDao.updateGrn(wbid, grnNo, batch, msg, status)
    }

    fun updateGhanaProcessing(wbid: String, rmin: String, msg: String, status: Int) {
        vegaGhanaProcessingDao.updateSuccessResponse(wbid, rmin, msg, status)
    }

    fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) {
        vegaEcuadorGrnDao.updateGrnNoToQuality(wbid, grnNo, batchNo)
    }

    fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) {
        vegaQualityDao.updateTempIdToWbid(wbid, tmpWbid)
        vegaEcuadorGrnDao.updateTempIdToWbid(wbid, tmpWbid)
    }

    fun updateDispatchStatus(dispatch: VegaEcuadorDispatch) {
        vegaEcuadorDispatchDao.updateDispatchStatus(
            dispatch.wbTempId,
            dispatch.status,
            dispatch.deliveryId,
            dispatch.syncStatusMsg,
            dispatch.isSynced,
            dispatch.binFormation,
            dispatch.delivery,
            dispatch.pgi,
            dispatch.picking
        )
    }



    fun updateMtntDispatchStatus(dispatch: VegaCocoaDispatchWB) {
//        vegaCocoaDispatchDao.updateSuccessData()
        /* vegaCocoaDispatchDao.updateMtntDispatchStatus(
             dispatch.weighBridgeId,
             dispatch.syncStatus,
             "dispatch.syncStatusMsg",
             dispatch.isSyncStatus,
             dispatch.delivery
         )*/
    }

    fun getGrnItem(): LiveData<List<VegaReceiving>> = vegaReceivingDao.getGrnItem(Status.SYNC_PENDING)
    fun getPendingItem(): LiveData<List<VegaCoCoaReceivingMtnrWithLots>> = vegaCoCoaOffloadDao.getPendingList()

    fun getInvoiceOfflineData(): LiveData<List<VegaNicaraguaInvoiceDetails>> =
        vegaNicaraguaGrnDao.getInvoiceOfflineData()

    fun getForwardOfflineData(): LiveData<List<VegaNicaraguaForwardPODetails>> =
        vegaNicaraguaForwardPODao.getOfflineForwardPODetails()


    suspend fun getLastSyncTime(): LiveData<VegaLastSyncTime> = vegaReceivingDao.getLastSyncTime()
    suspend fun getPendingListWithLot() =
        vegaCocoaDispatchDao.getPendingListWithLot()

    suspend fun getListOfMtntWithLots(): LiveData<List<VegaMtntWithLotsWithBags>> =
        vegaNicaraguaGrnDao.getListOfMtntWithLots()

    fun getAdvanceTransaction(): LiveData<List<VegaNicaraguaAdvanceTransactionDetails>> =
        vegaNicaraguaAdvanceDao.getOfflineAdvanceODetails()

    fun updateSuccessData(
        delivery:String,
        weighBridgeId: String,
        issyncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaDispatchLots>
    ) {
        println("Roshna => updateSuccessData")
        vegaCocoaDispatchDao.updateGhanaDispatchStatus(delivery, weighBridgeId, issyncStatus, status, msg)
        /*lots.forEach {
            it.isProgress = true
            vegaCocoaDispatchDao.updateDispatchLotStatus(weighBridgeId, true)
        }*/
    }

    fun updateGhanaSuccessData(
        delivery: String,
        weighBridgeId: String,
        issyncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaGhanaCocoaDispatchLots>
    ) {

        vegaCocoaDispatchDao.updateGhanaDispatchStatus(
            delivery,
            weighBridgeId,
            issyncStatus,
            status,
            msg
        )
        /*lots.forEach {
            it.isProgress = true
            vegaCocoaDispatchDao.updateDispatchLotStatus(weighBridgeId, true)
        }*/
    }

    fun getAllDOBagsOfflineInfo() = doReceivingDao.getAllDOBagsOfflineInfo()
    suspend fun getOffloadingItem(): LiveData<List<VegaCoffeeReceiving>> =
        vegaCoffeeOffloadDao.getOffloadingItem()

    suspend fun getQualityItem(): LiveData<List<VegaQualityWBDetails>> =
        vegaCoffeeOffloadDao.getQualityItem()

    suspend fun getOfflineWeighBridgeDetailCount() =
        vegaEcuadorGrnDao.getOfflineWeighBridgeDetailCount()

    suspend fun getDispatchWithLineItemCount(): LiveData<List<VegaEcuadorDispatchWithLineItems>> =
        vegaEcuadorDispatchDao.getDispatchWithLineItemCount()

    suspend fun getIndoExportSalesItem(): LiveData<List<VegaIndoCoffeeExportSalesOrder>> =
        vegaCoffeeOffloadDao.getIndoExportSalesItem()

    // suspend fun getWeighBridgeDetailOnline() = vegaQualityDao.getWeighBridgeDetailOnline()
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val api: MasterApi by inject()
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    suspend fun DOgetWeighBridgeDetailOnline(): LiveData<Resource<List<DOQualityWBDetails>>> {
        val api: MasterApi by inject()
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object :
            NetworkBoundResource<List<DOQualityWBDetails>, GenericReqAndResp<List<DOQualityWBDetails>>>() {

            override fun processResponse(response: GenericReqAndResp<List<DOQualityWBDetails>>): List<DOQualityWBDetails> =
                response.data

            override suspend fun saveCallResults(items: List<DOQualityWBDetails>) =
                doQualityDao.save(items)

            override fun shouldFetch(data: List<DOQualityWBDetails>?): Boolean = true

            override suspend fun loadFromDb(): List<DOQualityWBDetails> =
                doQualityDao.getWeighBridgeDetailOnline(currentKey)

            override suspend fun createCall(): GenericReqAndResp<List<DOQualityWBDetails>> =
                api.fetchDOWeighBridgeDetail(currentKey)

        }.build().asLiveData()
    }

    suspend fun getDOWeighBridgeDetailoffline(): LiveData<List<DOQualityWBDetails>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val receiveItem = doQualityDao.getReceivingDetail(currentKey)
        receiveItem.forEach {
            if (doQualityDao.isWBExist(it.tmpWbId).isNotEmpty()) return@forEach
            doQualityDao.insertQualityWbDetail(prepareWeighBridgeData(it))
        }
        return doQualityDao.getQualityWeighBridgeDetail(currentKey)
    }

    suspend fun getTTFeatureMaster(module: String): LiveData<List<VegaFeatureMaster>>{
        return vegaReceivingDao.getTTFeatureMaster(module)
    }


    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        vegaCocoaRminDao.getConfigItems(role)

}
