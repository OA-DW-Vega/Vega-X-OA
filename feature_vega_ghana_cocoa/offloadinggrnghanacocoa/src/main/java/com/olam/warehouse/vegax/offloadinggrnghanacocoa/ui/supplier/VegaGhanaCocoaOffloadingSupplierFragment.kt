package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.common.VegaTrackTraceFragment
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaStorageLocationDetail
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.ValidateNumber
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWLotManualModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentVegaOffloadingGhanaCocoaSupplierBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.BAG
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.BAG_MATERIAL
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.JSON_COST_CENTRE_LIST
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.MATERIAL_CODE
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.OFFLOADING_DATA
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.OFFLOADING_GANA_DSC_GRN
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.OFFLOADING_OFFLINE
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.OFFLOADING_POST_BAG_DATA
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.OFFLOADING_SUMMARY_FRAG
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.PROCURE
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.SUPPLIER
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.WAREHOUSE
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.getColor
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.getTmpId
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGhanaCocoaOffloadingSupplierFragment : BaseFragment(), VegaSingleSelectCommonListener,
    AdapterView.OnItemSelectedListener {
    private var uomDetails = ArrayList<VegaUomDetails>()
    private var receivingData = VegaReceiving()
    private var selectedLotModel = VegaGRNDWLotManualModel()
    private var supplierList = mutableListOf<VegaVendor>()
    private var suppliervendorList = mutableListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var materialStorageLocationList = mutableListOf<VegaStorageLocationDetail>()
    private var storageLocationList = mutableListOf<VegaCustomStLocation>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var storageLocation: List<VegaCustomStLocation>? = null
    private var savedList = mutableListOf<VegaEcuadorOffloadingBagMaterial>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var offlineStocksList = mutableListOf<VegaEcuadorDispatchStocks>()
    private var model = mutableListOf<VegaReceiving>()
    val grnListLocal = arrayListOf<VegaEcuaOffloadingWithLineItems>()

    private lateinit var binding: FragmentVegaOffloadingGhanaCocoaSupplierBinding
    private var callBack: CallBack? = null
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()

    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var purchaseOrderQuantity: String = "0"
    private var grnPrice: String = "0"
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var bagCountTotal = 0
    private var procureType = ""
    private var defaulted: Boolean? = false
    private var supplierLists = ArrayList<String>()
    private var materialLists = ArrayList<String>()
    private var jsonData = mutableListOf<String>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var costCentreType: String? = ""
    private var stocksList = mutableListOf<VegaCocoaRminLots>()
    private var loggedInPlantId: String = ""
    private var defaultLocation = ArrayList<String>()

    /*Track & Trace*/
    var ttProcurementType = ""
    var ttComplaintType = ""
    var ttDirectFarmerDataList = java.util.ArrayList<TrackTraceFarmerModel>()
    var ttIndirectSourceLotDetails = TrackTraceSourceLotDetails()
    var ttFarmerlessTransactionDetails = TrackTraceTransactionIdDetails()
    var ttDbFarmerList = mutableListOf<VegaTrackTraceFarmerData>()
    var isTTComplaint = false
    var ttVendorFlag = false
    var isSourceLotMandatory = false
    var isTransIdMandatory = false

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            offloadingBagMaterialData: Any
        )

        fun replaceFragment(moveFrag: String)

        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving,
            mReceiving: MutableList<VegaReceiving>,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
            grnPrice: String,
            isOffline: Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_offloading_ghana_cocoa_supplier

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
            procurementType: String, complaintType: String,
            selectedLotModel: VegaGRNDWLotManualModel? = null
        ) = VegaGhanaCocoaOffloadingSupplierFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
            putParcelable(OFFLOADING_GANA_DSC_GRN, selectedLotModel ?: VegaGRNDWLotManualModel())
            putString(Constants.PROCUREMENT_TYPE, procurementType)
            putString(Constants.COMPLAINT_TYPE, complaintType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaOffloadingGhanaCocoaSupplierBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingecuador/ui/VegaEcuadorOffloadingSupplierFragment")
            .title("Ecuador Offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        updateMandatory()
        receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        selectedLotModel = arguments?.getParcelable(OFFLOADING_GANA_DSC_GRN)!!
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(OFFLOADING_POST_BAG_DATA)!!
        ttProcurementType = arguments?.getString(Constants.PROCUREMENT_TYPE)?:""
        if(ttProcurementType.isNotEmpty()) {
            loadTTSupplierTypeFragment()
        }
        if (receivingData.tmpWbId.isEmpty()) {
            receivingData.tmpWbId = getTmpId()
        }
        vm.getFeatureMaster()
        vm.featureMaster.observe(viewLifecycleOwner, Observer {
            if (it != null && it.size > 0) {
                updateFeatureUI(it)
            }
        })
        (!selectedLotModel.lotId.isNullOrEmpty()).let {
            if (it) {
                binding.apply {
                    clSupplierInfo.apply {
                        llLotIdLayout.visible()
                        selectedLotModel.let { its ->
                            tvLotValue.isEnabled = false
                            tvLotValue.text = its.lotId
                            tvSupplier.text = its.vendorName
                            tvNoOfBags.setText(its.noOfBags)
                        }
                    }
                }
            }
        }


        binding.clSupplierInfo.tvReceivingWH.setOnClickListener {
            if(ttProcurementType.equals(Constants.IN_DIRECT) && isSourceLotMandatory && ttIndirectSourceLotDetails.sourceLotId?.isEmpty()==true){
                showSnack(getString(R.string.source_lot_id_should_not_be_empty) )
            }else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && isTransIdMandatory && ttFarmerlessTransactionDetails.dwTransactionId?.isEmpty()==true){
                showSnack(getString(R.string.trans_id_should_not_be_empty) )
            } else {
                showSingleSelectDialog(getString(R.string.warehouse), WAREHOUSE)
            }
        }
        binding.clSupplierInfo.tvSupplier.setOnClickListener {
            if(ttProcurementType.equals(Constants.IN_DIRECT) && isSourceLotMandatory && ttIndirectSourceLotDetails.sourceLotId?.isEmpty()==true){
                showSnack(getString(R.string.source_lot_id_should_not_be_empty) )
            } else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && isTransIdMandatory && ttFarmerlessTransactionDetails.dwTransactionId?.isEmpty()==true){
                showSnack(getString(R.string.trans_id_should_not_be_empty) )
            } else {
                showSingleSelectDialog(getString(R.string.select_supplier), SUPPLIER)
            }
        }
        binding.clSupplierInfo.tvProduct.setOnClickListener {
           // showSingleSelectDialog(getString(R.string.select_material), BAG_MATERIAL)
        }

        binding.clSupplierInfo.tvDateValue.text = DateUtils.getUTCDateTime(
            System.currentTimeMillis().toString(),
            App.getAppContext()
        )

        vm.offloadingItemLocal.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                updateOffloadingUI(it)
            }
        })
        vm.getFarmerList()
        vm.farmerList.observe(viewLifecycleOwner, Observer {
            ttDbFarmerList = it.toMutableList()
        })
        vm.getOffloadingWithLineItem()

        vm.storageLocationList.observe(
            viewLifecycleOwner,
            Observer { updatestorageLocationList(it) })
        vm.getStorageLocationDetail()

        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it.toMutableList()
            materialList.forEachIndexed { _, vegaMaterial ->
                selectedLotModel?.let {its->
                   if (its.productName.equals(vegaMaterial.materialName))
                   {
                      binding.clSupplierInfo.tvProduct.text = its.productName
                   }
                }

                materialLists.add(vegaMaterial.materialCode.plus("-").plus(vegaMaterial.materialName))
            }
            enableProceedBtn()
        })
        vm.getProducts()

        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })

        /* val materialStorageLocationList = runBlocking {
             withContext(Dispatchers.IO) {
                 vm.getMaterialStorageLocation()
             }
         }*/

        /*vm.materialStorgeLocation.observe(viewLifecycleOwner, Observer {
            materialStorageLocationList = it.toMutableList()
        })
        vm.getMaterialStorageLocation()*/
        //vm.getSuppliers()
        vm.supplier.observe(viewLifecycleOwner, Observer {
            it?.let {
                supplierList = it.toMutableList()
                if (suppliervendorList != null) {
                    suppliervendorList.clear()
                    suppliervendorList = supplierList.filter { vegaVendor ->
                        vegaVendor.storagelocationcodeName!!.isNotEmpty()
                                && binding.clSupplierInfo.tvReceivingWH.text.toString() == vegaVendor.storagelocationcodeName
                    }.toMutableList()

                    val suppliers =
                        suppliervendorList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
                    supplierLists = suppliers as ArrayList<String>

                } else {
                    suppliervendorList = mutableListOf()
                    suppliervendorList.clear()
                    suppliervendorList = supplierList.filter { vegaVendor ->
                        vegaVendor.storagelocationcodeName!!.isNotEmpty()
                                && binding.clSupplierInfo.tvReceivingWH.text.toString() == vegaVendor.storagelocationcodeName
                    }.toMutableList()

                    val suppliers =
                        suppliervendorList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
                    supplierLists = suppliers as ArrayList<String>

                }
            }
            /*val supplierAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.clSupplierInfo.tvSupplier.threshold = 1
            binding.clSupplierInfo.tvSupplier.setAdapter(supplierAdapter)
            binding.clSupplierInfo.tvSupplier.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    it.forEach { vendor ->
                        if (vendor.vendorCode == binding.clSupplierInfo.tvSupplier.text.toString()
                                .split(" - ")[0]
                        ) {
                            receivingData.supplierCode = "000".plus(vendor.vendorCode)
                            receivingData.supplierName = vendor.vendorName
                            clearPurchaseOrder()
                        }
                    }
                }*/
            enableProceedBtn()
        })
        vm.getSuppliers()


        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            storageLocation = it

            defaultLocation = it.map { data ->
                data.procureLocationCode.plus(" - ").plus(data.procureLocationName)
            } as ArrayList<String>

            defaultLocation.let {
                if (defaultLocation.size == 1) {
                    binding.clSupplierInfo.tvReceivingWH.text = defaultLocation[0]
                    onItemSelected(null, null, 0, 0)
                }
            }


            enableProceedBtn()
            vm.validateNumber.observe(viewLifecycleOwner, Observer { updateValidateUI(it) })
        })
        //  vm.getCustomLocations()
        vm.stocksOffline.observe(viewLifecycleOwner, Observer { item ->
            //  var tempList = item.filter { !it.batchNumber.startsWith("Z") }

            offlineStocksList.clear()
            offlineStocksList.addAll(item)
            var materialCode = receivingData.bagMaterialCode
            var stockValue =
                offlineStocksList.filter { it.storageLocationCode.equals(receivingData.storageLocationCode) }
                    .filter {
                        it.plantId.equals(loggedInPlantId)
                    }.filter { it.materialCode.equals(materialCode) }

            var databaseCount =
                model.filter { it.storageLocationCode.equals(receivingData.storageLocationCode) }
                    .filter {
                        it.plantId.equals(loggedInPlantId)
                    }.filter { it.bagMaterialCode.equals(materialCode) }

            var totalEnteredBags: Double? = 0.0
            var weight: Double? = 0.0
            var totalWeight: Double? = 0.0
            stockValue.forEach { it1 ->
                totalWeight = ((weight!!) + (it1.weight?.toDouble()!!))
            }
            //var enteredBags: Double? = (calculateWeight()).toDouble()

            if (databaseCount.size > 0) {
                databaseCount.forEach { item ->
                    totalEnteredBags =
                        ((totalEnteredBags)!! + ((item.bagCount.toString()).toDouble()))
                }
                totalEnteredBags =
                    ((totalEnteredBags)!! + ((binding.clSupplierInfo.tvNoOfBags.text.toString()).toDouble()))
            } else {
                totalEnteredBags =
                    (binding.clSupplierInfo.tvNoOfBags.text.toString()).toDouble()
            }
            receivingData.dseLotId = binding.clSupplierInfo.tvLotValue.text.toString()
            // var enteredBags: Double? = (tvNoOfBags.text.toString()).toDouble()
            if (((totalWeight!!) >= (totalEnteredBags!!))) {
                receivingData.wsGate = "WS01"
                receivingData.weighBridgeType = PROCURE
                mReceiving.clear()
                receivingData.bagType = binding.clSupplierInfo.tvBagTypeName.text.toString()
                receivingData.whReceiptNum =
                    binding.clSupplierInfo.tvWHReceiptNoValue.text.toString()     /*Warehouse Receipt Number Offline Stocks - Edited by Muskan Jain*/
                receivingData.bagCount = binding.clSupplierInfo.tvNoOfBags.text.toString()
                receivingData.product = binding.clSupplierInfo.tvProduct.text.toString()
                if(!ttProcurementType.equals(Constants.DIRECT))
                receivingData.supplierName = binding.clSupplierInfo.tvSupplier.text.toString()
                receivingData.createdDate = binding.clSupplierInfo.tvDateValue.text.toString()
                receivingData.netWeight = calculateWeight()
                receivingData.grossWeight = calculateWeight()
                receivingData.receivingWH =
                    binding.clSupplierInfo.tvReceivingWH.text.toString() /*receivingWH - Edited by Muskan Jain*/
                receivingData.whReceiptNum =
                    binding.clSupplierInfo.tvWHReceiptNoValue.text.toString()
                callBack?.replaceFragment(
                    OFFLOADING_SUMMARY_FRAG,
                    receivingData,
                    mReceiving,
                    bagList,
                    grnPrice,
                    false
                )

            } else {
                showErrorDialogWithFAQLink(
                    requireContext(),
                    getString(R.string.dis_stock_not_available)
                )
            }
        })

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
            if (bagTypeList.size == 1) {
                binding.clSupplierInfo.tvBagTypeName.text = bagTypeList[0].bagType
                receivingData.bagType = bagTypeList[0].bagType
                receivingData.bagMaterialCode = bagTypeList[0].bagMaterialCode
            }
        })
        vm.getMaterials()

        binding.clSupplierInfo.tvBagTypeLabel.setOnClickListener { showBagTypeDialog(bagTypeList) }

        vm.offloadingItemCountLocal.observe(viewLifecycleOwner, Observer {
            if (it != null && it.size > 0) {
                binding.llOffline.visible()
                model.clear()
                it.forEach { item ->
                    model.add(item.receiving)
                }
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOffloadingWithLineItemCount()

        binding.btProceed.setOnClickListener {
            when {
                binding.clSupplierInfo.tvReceivingWH.text.toString()
                    .isNullOrEmpty() -> showSnack(getString(R.string.message_valid_receiving_wh_id)) /*tvRecivingWH - Edited by Muskan Jain*/
                binding.clSupplierInfo.tvProduct.text.toString().isNullOrEmpty() -> showSnack(getString(R.string.message_valid_product))



                binding.clSupplierInfo.llSupplier.isVisible && binding.clSupplierInfo.tvSupplier.text.toString().isNullOrEmpty() -> {
                    showSnack(getString(R.string.message_valid_supplier))
                }

                binding.clSupplierInfo.tvBagTypeName.text.toString().isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_bag_type)
                )

                binding.clSupplierInfo.tvNoOfBags.text.toString().isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_bag_count)
                )

                binding.clSupplierInfo.tvNoOfBags.text.toString()
                    .equals("0") -> showSnack(getString(R.string.number_of_bags_zero))

                receivingData.supplierName!!.isEmpty() -> showSnack(getString(R.string.message_valid_supplier1))
                binding.clSupplierInfo.tvWHReceiptNoValue.text.toString()
                    .isNullOrEmpty() -> showSnack(getString(R.string.message_valid_wh_receipt_number))  /*WhReceiptNum binding proceed - Edited by Muskan Jain*/
                else -> {
                    moveToSummary()
                }
            }
        }
        binding.llOffline.setOnClickListener { moveToOfflineSummary() }
        vm.uomDetail.observe(viewLifecycleOwner, Observer {
            uomDetails = it as ArrayList<VegaUomDetails>
        })
        vm.getUomDetails()

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())
    }

    private fun updateOffloadingUI(data: List<VegaEcuaOffloadingWithLineItems>) {
        data.let { receiving ->
            when {
                receiving.isNotEmpty() -> {
                    grnListLocal.addAll(receiving)
                }

                else -> {
                    setErrorContentView("No data available")
                }
            }
        }
    }

    private fun updateFeatureUI(featureList: List<VegaFeatureMaster>?) {
        featureList?.let {item ->
            item.forEach {
                when (it.featureName) {
                    "Source_Lot_Field" ->
                    {
                        if (it.mandatory == true) isSourceLotMandatory = true
                    }
                    "Transaction_Id_Field" ->
                    {
                        if (it.mandatory == true) isTransIdMandatory = true
                    }
                }
            }
        }
    }

    private fun updateValidateUI(response: Resource<GenericReqAndResp<ValidateNumber>>?) {
        response.let {
            if (it != null) {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        hideLoading()
                        when (it.data?.success) {
                            true -> {
                                var materialCode = receivingData.bagMaterialCode.toString()
                                vm.fetchStocks(materialCode)
                            }

                            else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                    }

                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                    }
                }
            }
        }

    }


    private fun updatestorageLocationList(data: List<VegaStorageLocationDetail>) {
        materialStorageLocationList.clear()
        materialStorageLocationList.addAll(data)

        vm.getCustomLocations()

    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains(JSON_COST_CENTRE_LIST)) {
                costCentreType = (JSONObject(it).getJSONArray(JSON_COST_CENTRE_LIST).get(0)).toString()
                receivingData.costCentre = costCentreType
            }
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            stocksList.clear()
                            val dataValue = it.data?.data!!
                            stocksList.addAll(dataValue)
                            var materialCode = receivingData.bagMaterialCode
                            var stockValue =
                                stocksList.filter { it.storageLocationCode.equals(receivingData.storageLocationCode) }
                                    .filter {
                                        it.plantId.equals(loggedInPlantId)
                                    }.filter { it.materialCode.equals(materialCode) }
                            var weight: Double? = 0.0
                            var totalWeight: Double? = 0.0
                            stockValue.forEach { it1 ->
                                totalWeight = ((weight!!) + (it1.weight?.toDouble()!!))
                            }
                            //var enteredBags: Double? = (calculateWeight()).toDouble()
                            var enteredBags: Double? =
                                (binding.clSupplierInfo.tvNoOfBags.text.toString()).toDouble()
                            receivingData.dseLotId = binding.clSupplierInfo.tvLotValue.text.toString()
                            if (((totalWeight!!) >= (enteredBags!!))) {
                                receivingData.wsGate = "WS01"
                                receivingData.weighBridgeType = PROCURE
                                mReceiving.clear()
                                receivingData.bagType =
                                    binding.clSupplierInfo.tvBagTypeName.text.toString()
                                receivingData.bagCount =
                                    binding.clSupplierInfo.tvNoOfBags.text.toString()
                                receivingData.product =
                                    binding.clSupplierInfo.tvProduct.text.toString()
                                if(!ttProcurementType.equals(Constants.DIRECT))
                                receivingData.supplierName =
                                    binding.clSupplierInfo.tvSupplier.text.toString()
                                receivingData.createdDate =
                                    binding.clSupplierInfo.tvDateValue.text.toString()
                                receivingData.netWeight = calculateWeight()
                                receivingData.grossWeight = calculateWeight()
                                receivingData.totalStockWeight = totalWeight
                                receivingData.receivingWH =
                                    binding.clSupplierInfo.tvReceivingWH.text.toString()   /*Update UI - Edited by Muskan Jain*/
                                receivingData.whReceiptNum =
                                    binding.clSupplierInfo.tvWHReceiptNoValue.text.toString()   /*Update UI - Edited by Muskan Jain*/
                                /*Track & Trace*/
                                if(ttProcurementType.equals(Constants.DIRECT)){
                                    receivingData.ttFarmerList = getTTFarmerDataList()
                                    receivingData.eudrStatus = isTTComplaint

                                } else if(ttProcurementType.equals(Constants.IN_DIRECT)) {
                                    receivingData.sourceLotId = ttIndirectSourceLotDetails?.sourceLotId.toString()
                                    receivingData.eudrStatus = ttIndirectSourceLotDetails?.isEudrComplaintFlag ?: false
                                } else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)){
                                    receivingData.farmerLessTransactionId = ttFarmerlessTransactionDetails.dwTransactionId.toString()
                                    receivingData.eudrStatus = ttFarmerlessTransactionDetails?.compliantFlag ?: false
                                }
                                callBack?.replaceFragment(
                                    OFFLOADING_SUMMARY_FRAG,
                                    receivingData,
                                    mReceiving,
                                    bagList,
                                    grnPrice,
                                    false
                                )

                            } else {
                                showErrorDialogWithFAQLink(
                                    requireContext(),
                                    getString(R.string.dis_stock_not_available)
                                )
                            }
                        }

                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }


    }
    private fun getTTFarmerDataList(): java.util.ArrayList<TrackTraceFarmerModel> {
        if(ttDirectFarmerDataList.isNotEmpty() && ttDirectFarmerDataList.size > 0) {
            if (AppUtils.isOnline()) {
                if(ttDirectFarmerDataList.size==1){
                    if(ttDirectFarmerDataList.get(0).farmerWeight.isNullOrEmpty() || ttDirectFarmerDataList.get(0).farmerWeight=="0"){
                        ttDirectFarmerDataList.get(0).farmerWeight = receivingData.netWeight
                    }
                }
                ttDirectFarmerDataList.forEach {
                    it.uom = receivingData.unitsOfMeasure
                }
                return ttDirectFarmerDataList
            } else{
                ttDirectFarmerDataList.forEach {
                    it.tmpWbId = receivingData.tmpWbId
                    if(it.farmerWeight.isNullOrEmpty())it.farmerWeight = receivingData.netWeight
                    it.uom = receivingData.unitsOfMeasure
                }
                return ttDirectFarmerDataList
            }
        }
        return ttDirectFarmerDataList
    }

    private fun showBagTypeDialog(data: List<VegaPackageMaterial>) {
        val bagTypes = data.map { data1 -> data1.bagType }
        MaterialDialog(requireContext()).show {
            title(com.olam.warehouse.login.R.string.bag_types)
            listItemsSingleChoice(items = bagTypes) { _, index, text ->
                binding.clSupplierInfo.tvBagTypeName.text = text
                receivingData.bagType = text.toString()
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun moveToOfflineSummary() {
        callBack?.replaceFragment(OFFLOADING_OFFLINE)
    }

    private fun enableProceedBtn() {
        var flag = true
        when (flag) {
            true -> {
                binding.btProceed.isEnabled = true
                binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            }

            false -> {
                binding.btProceed.isEnabled = false
                binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            }
        }
    }

    private fun clearPurchaseOrder() {
        receivingData.purchaseDocNum = ""
        receivingData.purchaseDocDesc = ""
        purchaseOrderQuantity = "0"
    }

    private fun updateMandatory() {
        binding.clSupplierInfo.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.clSupplierInfo.tvReceiving.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse)) { mandatoryStars() } }
        binding.clSupplierInfo.tvDateLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.date)) { mandatoryStars() } }
        binding.clSupplierInfo.tvBagTypeLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.bag_type)) { mandatoryStars() } }
        binding.clSupplierInfo.tvNoBagsLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.no_of_bags)) { mandatoryStars() } }
        /*Mandatory Update WHReceiptNum Label - Edited by Muskan Jain*/
        binding.clSupplierInfo.tvWHReceiptNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse_receipt_number)) { mandatoryStars() } }
    }

    private fun moveToSummary() {
        val whNumber = binding.clSupplierInfo.tvWHReceiptNoValue.text.toString()
        if (AppUtils.isOnline()) {
            val plantId = getPlantDetails().plantId
            val wbType = "PROCURE"
            vm.validateReceiptNumber(plantId, whNumber, wbType)
        } else {
            val list = grnListLocal.filter { it.receiving.whReceiptNum?.equals(whNumber) == true }

            if (list.isNotEmpty()) {
                showSnack(getString(R.string.wh_number_duplicated))
            } else vm.fetchStocksOffline()
        }
    }

    private fun calculateWeight(): String {
        var netWeight = ""
        var uom =
            ((uomDetails.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(receivingData.materialCode)) }).filter {
                it.fromUom.equals(BAG)
            }).singleOrNull()
        netWeight =
            ((uom?.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))?.times(
                (binding.clSupplierInfo.tvNoOfBags.text.toString()
                    .toInt().toDouble())
            )).toString()
        return netWeight
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        suppliervendorList.clear()
        suppliervendorList = supplierList.filter { vegaVendor ->
            vegaVendor.storagelocationcodeName!!.isNotEmpty()
                    && binding.clSupplierInfo.tvReceivingWH.text.toString() == vegaVendor.storagelocationcodeName

        }.toMutableList()

        val suppliers = suppliervendorList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
        supplierLists = suppliers as ArrayList<String>

        /*the below case is to check the prepopulated source lot vendor id available in selected stroage location vendor*/
        if(ttIndirectSourceLotDetails.sourceLotId?.isNotEmpty() == true && receivingData.supplierCode?.isNotEmpty() == true){
            var checkSupplier = suppliervendorList.filter { it.vendorCode.equals(receivingData.supplierCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the source lot is not allocated to the selected storage location")
                binding.clSupplierInfo.tvSupplier.text = ""
            }
        }
        //val supplierAdapter =
        //ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
        // binding.clSupplierInfo.tvSupplier.threshold = 1
        //binding.clSupplierInfo.tvSupplier.setAdapter(supplierAdapter)
        //binding.clSupplierInfo.tvSupplier.onItemClickListener =
        AdapterView.OnItemClickListener { _, _, _, _ ->
            suppliervendorList.forEach { vendor ->
                if (vendor.vendorCode == binding.clSupplierInfo.tvSupplier.text.toString()
                        .split(" - ")[0]
                ) {
                    receivingData.supplierCode = "000".plus(vendor.vendorCode)
                    receivingData.supplierName = vendor.vendorName
                    clearPurchaseOrder()
                }
            }
        }
        enableProceedBtn()


        //val item = p0?.getItemAtPosition(p2) as String

        receivingData.storageLocationCode = storageLocation!![p2].procureLocationCode
        receivingData.storageLocation = storageLocation!![p2].procureLocationName
        loggedInPlantId = (storageLocation!![p2].plant).toString()

        val data =
            materialStorageLocationList.filter { it.storageLocationCode.equals(receivingData.storageLocationCode) }
        if (data.size > 0) {
            var materialData =
                materialList.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[0].materialCode)) || ((MATERIAL_CODE.plus(it.eudrEquivalentMap)).equals(data[0].materialCode)) }
            if (materialData.size > 0) {
                if(isTTComplaint){
                    materialData = materialData.filter { it.complainceFlag.equals(Constants.COMPLAINT) }
                } else {
                    materialData = materialData.filter { it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                }
                if(materialData.size>0) {
                    receivingData.materialCode =
                        MATERIAL_CODE.plus(materialData[0].materialCode)
                    receivingData.materialName = materialData[0].materialName.toString()
                    receivingData.unitsOfMeasure = materialData[0].unitsOfMeasure.toString()
                    receivingData.currency = materialData[0].currency
                    receivingData.item = "00001"
                    grnPrice = calculatePrice(
                        if (materialData.isEmpty())
                            "0.0"
                        else materialData[0].price.toString()
                    )
                    binding.clSupplierInfo.tvProduct.text =
                        materialData[0].materialCode.plus("-").plus(materialData[0].materialName)
                    /* binding.clSupplierInfo.tvProduct.text = if (selectedLotModel.lotId.isNullOrEmpty())
                    materialData[0].materialCode.plus("-")
                        .plus(materialData[0].materialName) else binding.clSupplierInfo.tvProduct.text.toString()*/
                    clearPurchaseOrder()
                }
            }
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Toast.makeText(requireContext(), "Please select a Warehouse", Toast.LENGTH_SHORT).show()
    }

    private fun calculatePrice(price: String): String {
        var mPrice = 0.0
        var singleBags = 0.0
        var grnPrice = price.toDouble()
        var uom =
            ((uomDetails.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(receivingData.materialCode)) }).filter {
                it.fromUom.equals(BAG)
            }).single()
        singleBags =
            (((uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))) ?: 0.0)
        mPrice = singleBags.times(grnPrice)
        return mPrice.formatTwoDigits()
    }

    private fun getBagInfo(): String {
        var uom =
            ((uomDetails.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(receivingData.materialCode)) }).filter {
                it.fromUom.equals(BAG)
            }).single()
        return uom.fromUom
    }

    private fun showSingleSelectDialog(title: String, currentFalg: String) {
        var list = ArrayList<String>()
        when (currentFalg) {
            SUPPLIER -> {
                list = supplierLists
            }

            WAREHOUSE -> {
                list = defaultLocation
            }
            BAG_MATERIAL->
            {
                if(ttProcurementType.isNotEmpty()) {
                    if (isTTComplaint) {
                        list = materialList.filter { it.complainceFlag.equals(Constants.COMPLAINT) }
                            .map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
                    } else {
                        list = materialList.filter { it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                            .map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
                    }
                    if (list.isEmpty()) {
                        list = materialList.map {
                            it.materialCode.plus("-").plus(it.materialName)
                        } as java.util.ArrayList<String>
                    }
                } else {
                    list = materialList.map {
                        it.materialCode.plus("-").plus(it.materialName)
                    } as java.util.ArrayList<String>
                }
//                list = materialLists
            }
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFalg,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            SUPPLIER -> {
                binding.clSupplierInfo.tvSupplier.text = data
                supplierList.forEach { vendor ->
                    if (vendor.vendorCode == binding.clSupplierInfo.tvSupplier.text.toString()
                            .split(" - ")[0]
                    ) {
                        receivingData.supplierCode = "000".plus(vendor.vendorCode)
                        receivingData.supplierName = vendor.vendorName
                        clearPurchaseOrder()
                    }
                    /*if(ttProcurementType.isNotEmpty()){
                        if(ttProcurementType.equals(Constants.DIRECT)) {
                            validateFarmer(vendor.vendorName ?: "")
                        }
                    }*/
                }

            }

            WAREHOUSE -> {
                binding.clSupplierInfo.tvReceivingWH.text = data
                //storageLocationList.forEach { warehouse -> }
                val pos = defaultLocation.indexOf(data)
                onItemSelected(null, null, pos, 0)

            }

            BAG_MATERIAL->
            {
               // val selectedMaterial = materialList.filter { its-> its.materialCode.plus(" - ").plus(its.materialName).equals(data,true) }
                binding.clSupplierInfo.tvProduct.text = data
                receivingData.materialCode =
                    MATERIAL_CODE.plus(data.split("-")[0])
                receivingData.materialName = data.split("-")[1]
            }

        }
    }

    /*Track & Trace*/
    /*fun isVendor(flag: Boolean){
        binding.clSupplierInfo.tvEudrStatus.gone()
        if(flag) {
            binding.clSupplierInfo.llSupplier.gone()
            ttVendorFlag = true
        } else {
            binding.clSupplierInfo.llSupplier.visible()
            ttVendorFlag = false
        }
    }*/

    private fun resetMaterial(){
        binding.clSupplierInfo.tvReceivingWH.text = ""
        binding.clSupplierInfo.tvProduct.text = ""
        receivingData.materialCode = ""
        receivingData.materialName = ""
    }

    /*Track & Trace*/
    fun updateFarmerListDetails(farmerList: java.util.ArrayList<TrackTraceFarmerModel>){
        resetMaterial()
        ttDirectFarmerDataList.clear()
        ttDirectFarmerDataList.addAll(farmerList)
        if(ttDirectFarmerDataList.isNotEmpty()) {
//            receivingData.supplierCode = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(0)
            receivingData.supplierCode = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(0)?.trim()
            receivingData.supplierName = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(1)

            var result = ttDirectFarmerDataList.any { it.isComplaint == 0 }
            if(result){
                isTTComplaint = false
            }else{
                isTTComplaint = true
            }
        } else {
            isTTComplaint = false
        }
//        updateEudrStatus()
    }

/*    *//*Track & Trace*//*
    fun updateEudrStatus(){
        if(ttProcurementType.equals(Constants.DIRECT)) {
            binding.clSupplierInfo.tvEudrStatus.visible()
            binding.clSupplierInfo.tvEudrStatus.text =
                if (isTTComplaint) "Eudr Status : ${Constants.COMPLAINT}" else "Eudr Status : ${Constants.UNKNOWN_ATTRIBUTE}"
        }
    }*/

    /*Track & Trace*/
    fun isComplaint(status:Int){
        if(status == 0){
            isTTComplaint = false
        } else {
            isTTComplaint = true
        }
    }

    /*Track & Trace*/
    fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails){
        resetMaterial()
        isTTComplaint = sourceLotDetails?.isEudrComplaintFlag?:false
        this.ttIndirectSourceLotDetails = sourceLotDetails
        if(sourceLotDetails.vendorCode?.isNotEmpty() == true){
            var checkSupplier = supplierList.filter { it.vendorCode.equals(sourceLotDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
            } else {
                var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                binding.clSupplierInfo.tvSupplier.text = data
                receivingData.supplierCode = data.split("-")[0].trim()
                receivingData.supplierName = data.split("-")[1].trim()
            }
        } else {
            showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
        }
    }

    fun updateFarmerlessTransactionDetails(transactionIdDetails: TrackTraceTransactionIdDetails){
        resetMaterial()
        isTTComplaint = transactionIdDetails?.compliantFlag?:false
        this.ttFarmerlessTransactionDetails = transactionIdDetails
        binding.clSupplierInfo.tvSupplier.text=""
        if(transactionIdDetails.vendorCode?.isNotEmpty() == true){
            var checkSupplier = supplierList.filter { it.vendorCode.equals(transactionIdDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the transaction id is not available in SAP, do you want to continue?")
            } else {
                var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                val split = data.split("-")
                binding.clSupplierInfo.tvSupplier.text = data
                receivingData.supplierCode = data.split("-")[0].trim()
                receivingData.supplierName = data.split("-")[1].trim()
            }
        } else {
            showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
        }
    }

   /* private fun validateFarmer(farmerName: String){
        var localList = mutableListOf<VegaTrackTraceFarmerData>()
        ttDbFarmerList.forEach {
            if(it.farmerName.contains(farmerName)){
                localList.add(it)
            }
        }
        if(localList.size > 1){
            chooseOneLotDialog(localList)
        } else if(localList.size == 1){
            if(localList.get(0).isComplaint == 1){
                isTTComplaint = true
            }
        } else{
            isTTComplaint = false
        }
//        updateEudrStatus()
    }*/

    /*@SuppressLint("CheckResult")
    private fun chooseOneLotDialog(farmerList: List<VegaTrackTraceFarmerData>) {
        val farmerItem = farmerList.map { it.farmerName.plus(" : ").plus(it.farmerId) }
        MaterialDialog(requireContext()).show {
            message(R.string.select_farmer)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = farmerItem) { _, index, text ->
//                context.toast("dfedf")
                receivingData.supplierCode = text.split("-")[0]
                receivingData.supplierName = text.split("-")[1]
                binding.clSupplierInfo.tvSupplier.text = text
                if(farmerList.get(index).isComplaint == 1){
                    isTTComplaint = true
                } else {
                    isTTComplaint = false
                }
                updateEudrStatus()
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.login.R.string.ok),
                    true
                )
            )
        }
    }*/
    private fun loadTTSupplierTypeFragment(){
        val bundle = Bundle()
        bundle.putString(
            Constants.PROCUREMENT_TYPE, ttProcurementType)
        displayFragment(VegaTrackTraceFragment.newInstance(bundle), false)
       if(ttProcurementType.equals(Constants.DIRECT)) {
           binding.clSupplierInfo.llSupplier.gone()
       }
    }
    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            "",
            allowStateLoss = true,
            containerViewId = R.id.flTTContainer,
            allowBackStack = flag
        )
    }

    fun replaceFragment(
        fragment: Fragment,
        tag: String?,
        allowStateLoss: Boolean = false,
        @IdRes containerViewId: Int,
        @AnimRes enterAnimation: Int = 0,
        @AnimRes exitAnimation: Int = 0,
        @AnimRes popEnterAnimation: Int = 0,
        @AnimRes popExitAnimation: Int = 0,
        allowBackStack: Boolean = false
    ) {
        val ft = activity?.supportFragmentManager
            ?.beginTransaction()
            ?.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        when {
            allowBackStack -> {
                ft?.add(containerViewId, fragment, tag)
                ft?.addToBackStack(tag)
            }
            else -> ft?.replace(containerViewId, fragment, tag)
        }
        if (!activity?.supportFragmentManager?.isStateSaved!!) {
            ft?.commit()
        } else if (allowStateLoss) {
            ft?.commitAllowingStateLoss()
        }
    }


}

