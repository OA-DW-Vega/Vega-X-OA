package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentVegaOffloadingGhanaCocoaSupplierBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.*
import kotlinx.android.synthetic.main.layout_vega_offloading_ghana_cocoa_supplier_info.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGhanaCocoaOffloadingSupplierFragment : BaseFragment(),AdapterView.OnItemSelectedListener{
    private var uomDetails = ArrayList<VegaUomDetails>()
    private var receivingData = VegaReceiving()
    private var supplierList = mutableListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var materialStorageLocationList = mutableListOf<VegaStorageLocationDetail>()
    private var storageLocationList = mutableListOf<VegaCustomStLocation>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var storageLocation: List<VegaCustomStLocation>? = null
    private var savedList = mutableListOf<VegaEcuadorOffloadingBagMaterial>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var offlineStocksList = mutableListOf<VegaEcuadorDispatchStocks>()

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
    private var jsonData = mutableListOf<String>()
    private var costCentreType :String? =""
    private var stocksList = mutableListOf<VegaCocoaRminLots>()
    private var loggedInPlantId: String = ""

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
            grnPrice:String,
            isOffline:Boolean
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
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        ) = VegaGhanaCocoaOffloadingSupplierFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
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
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(OFFLOADING_POST_BAG_DATA)!!
        if (receivingData.tmpWbId.isEmpty()) {
            receivingData.tmpWbId = getTmpId()
        }
        binding.clSupplierInfo.tvDateValue.text = DateUtils.getUTCDateTime(
            System.currentTimeMillis().toString(),
            App.getAppContext()
        )

        vm.storageLocationList.observe(this, Observer { updatestorageLocationList(it) })
        vm.getStorageLocationDetail()

        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it.toMutableList()
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

        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
            val suppliers = it.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val supplierAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            tvSupplier.threshold = 1
            tvSupplier.setAdapter(supplierAdapter)
            tvSupplier.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { vendor ->
                    if (vendor.vendorCode == tvSupplier.text.toString().split(" - ")[0]) {
                        receivingData.supplierCode = "000".plus(vendor.vendorCode)
                        receivingData.supplierName = vendor.vendorName
                        clearPurchaseOrder()
                    }
                }
            }
            enableProceedBtn()
        })
        vm.getSuppliers()

        vm.custonLocation.observe(this, Observer {
            storageLocation = it

            val defaultLocation = it.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
            tvReceivingWH.onItemSelectedListener = this
            val defaultLocationAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, defaultLocation)
            defaultLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tvReceivingWH.adapter = defaultLocationAdapter

           /* if (storageLocation?.size == 1) {
//                ll_recevingWH.run { ll_recevingWH.gone() }

                binding.clSupplierInfo.tvReceivingWH.setText(
                    storageLocation!![0].procureLocationCode.plus(" - ").plus(
                        storageLocation!![0].procureLocationName
                    )
                )
                receivingData.storageLocationCode = storageLocation!![0].procureLocationCode
                receivingData.storageLocation = storageLocation!![0].procureLocationName
                loggedInPlantId =  (storageLocation!![0].plant).toString()

                val data = materialStorageLocationList.filter { it.storageLocationCode.equals(receivingData.storageLocationCode) }
                if(data.size>0) {
                    val materialData =
                        materialList.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[0].materialCode)) }
                    if (materialData.size > 0) {
                        receivingData.materialCode =
                            MATERIAL_CODE.plus(materialData[0].materialCode)
                        receivingData.materialName = materialData[0].materialName.toString()
                        receivingData.unitsOfMeasure = materialData[0].unitsOfMeasure.toString()
                        receivingData.currency = materialData[0].currency
                        receivingData.item = "00001"
                        grnPrice = materialData[0].price.toString()
                        binding.clSupplierInfo.tvProduct.setText(
                            materialData[0].materialCode.plus("-").plus(materialData[0].materialName)
                        )
                        clearPurchaseOrder()
                    }
                }
            } else {
//                ll_recevingWH.visible()

                vm.custonLocation.observe(viewLifecycleOwner, Observer {
                    storageLocationList = it.toMutableList()
                    val storageLocations =
                        it.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
                    val storageLocationAdapter =
                        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, storageLocations)
                    tvReceivingWH.threshold = 1
                    tvReceivingWH.setAdapter(storageLocationAdapter)
                    tvReceivingWH.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                        it.forEach { location ->
                            if (location.procureLocationCode == tvReceivingWH.text.toString().split(" - ")[0]) {
                                receivingData.storageLocationCode = location.procureLocationCode
                                receivingData.storageLocation = location.procureLocationName
                                loggedInPlantId =  location.plant.toString()
                                clearPurchaseOrder()

                                val data =
                                    materialStorageLocationList.filter { it.storageLocationCode.equals(receivingData.storageLocationCode) }
                                if (data.size > 0) {
                                    val materialData =
                                        materialList.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[0].materialCode)) }
                                    if (materialData.size > 0) {
                                        receivingData.materialCode =
                                            MATERIAL_CODE.plus(materialData[0].materialCode)
                                        receivingData.materialName = materialData[0].materialName.toString()
                                        receivingData.unitsOfMeasure = materialData[0].unitsOfMeasure.toString()
                                        receivingData.currency = materialData[0].currency
                                        receivingData.item = "00001"
                                        grnPrice = materialData[0].price.toString()
                                        binding.clSupplierInfo.tvProduct.setText(
                                            materialData[0].materialCode.plus("-").plus(materialData[0].materialName)
                                        )
                                        clearPurchaseOrder()

                                    }
                                }
                            }
                        }
                    }
                })
            }*/
            enableProceedBtn()
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
                var weight: Double? = 0.0
                var totalWeight: Double? = 0.0
                stockValue.forEach { it1 ->
                    totalWeight = ((weight!!) + (it1.weight?.toDouble()!!))
                }
                var enteredBags: Double? = (calculateWeight()).toDouble()
                if (((totalWeight!!) >= (enteredBags!!))) {
                    receivingData.wsGate = "WS01"
                    receivingData.weighBridgeType = PROCURE
                    mReceiving.clear()
                    receivingData.bagType = tvBagTypeName.text.toString()
                    receivingData.whReceiptNum =
                        tvWHReceiptNoValue.text.toString()     /*Warehouse Receipt Number Offline Stocks - Edited by Muskan Jain*/
                    receivingData.bagCount = tvNoOfBags.text.toString()
                    receivingData.product = tvProduct.text.toString()
                    receivingData.supplierName = tvSupplier.text.toString()
                    receivingData.createdDate = tvDateValue.text.toString()
                    receivingData.netWeight = calculateWeight()
                    receivingData.grossWeight = calculateWeight()
                    receivingData.receivingWH =
                        tvReceivingWH.selectedItem.toString() /*receivingWH - Edited by Muskan Jain*/
                    receivingData.whReceiptNum = tvWHReceiptNoValue.text.toString()
                    callBack?.replaceFragment(
                        OFFLOADING_SUMMARY_FRAG,
                        receivingData,
                        mReceiving,
                        bagList,
                        grnPrice,
                        false
                    )

                } else {
                    UIUtils.showErrorDialog(
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

        vm.offloadingItemCountLocal.observe(this, Observer {
            if (it != null && it.size > 0) {
                binding.llOffline.visible()
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOffloadingWithLineItemCount()

        binding.btProceed.setOnClickListener {
            when {
                tvReceivingWH.selectedItem.toString().isNullOrEmpty() -> showSnack(getString(R.string.message_valid_receiving_wh_id)) /*tvRecivingWH - Edited by Muskan Jain*/
                tvProduct.text.toString().isNullOrEmpty() -> showSnack(getString(R.string.message_valid_product))
                tvSupplier.text.toString().isNullOrEmpty() -> showSnack(getString(R.string.message_valid_supplier))
                tvBagTypeName.text.toString().isNullOrEmpty() -> showSnack(getString(R.string.message_valid_bag_type))
                tvNoOfBags.text.toString().isNullOrEmpty() -> showSnack(getString(R.string.message_valid_bag_count))
                tvNoOfBags.text.toString().equals("0") -> showSnack(getString(R.string.number_of_bags_zero))
                tvWHReceiptNoValue.text.toString().isNullOrEmpty() -> showSnack(getString(R.string.message_valid_wh_receipt_number))  /*WhReceiptNum binding proceed - Edited by Muskan Jain*/
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
                            var enteredBags: Double? = (calculateWeight()).toDouble()
                            if (((totalWeight!!) >= (enteredBags!!))) {
                                receivingData.wsGate = "WS01"
                                receivingData.weighBridgeType = PROCURE
                                mReceiving.clear()
                                receivingData.bagType = tvBagTypeName.text.toString()
                                receivingData.bagCount = tvNoOfBags.text.toString()
                                receivingData.product = tvProduct.text.toString()
                                receivingData.supplierName = tvSupplier.text.toString()
                                receivingData.createdDate = tvDateValue.text.toString()
                                receivingData.netWeight = calculateWeight()
                                receivingData.grossWeight = calculateWeight()
                                receivingData.receivingWH = tvReceivingWH.selectedItem.toString()   /*Update UI - Edited by Muskan Jain*/
                                receivingData.whReceiptNum = tvWHReceiptNoValue.text.toString()   /*Update UI - Edited by Muskan Jain*/

                                callBack?.replaceFragment(
                                    OFFLOADING_SUMMARY_FRAG,
                                    receivingData,
                                    mReceiving,
                                    bagList,
                                    grnPrice,
                                    false
                                )

                            } else {
                                UIUtils.showErrorDialog(
                                    requireContext(),
                                    getString(R.string.dis_stock_not_available)
                                )
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }


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
                binding.btProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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
        tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        tvReceiving.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse)) { mandatoryStars() } }
        tvDateLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.date)) { mandatoryStars() } }
        tvBagTypeLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.bag_type)) { mandatoryStars() } }
        tvNoBagsLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.no_of_bags)) { mandatoryStars() } }
        /*Mandatory Update WHReceiptNum Label - Edited by Muskan Jain*/
        tvWHReceiptNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse_receipt_number)) { mandatoryStars() } }
    }

    private fun moveToSummary() {
        if (AppUtils.isOnline()) {
           var materialCode = receivingData.bagMaterialCode.toString()
            vm.fetchStocks(materialCode)
        }else{
            vm.fetchStocksOffline()
        }
    }

    private fun calculateWeight(): String {
        var netWeight = ""
        var uom =
            ((uomDetails.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(receivingData.materialCode)) }).filter {
                it.fromUom.equals(BAG)
            }).single()
        netWeight =
            ((uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))?.times(
                (tvNoOfBags.text.toString()
                    .toInt().toDouble())
            )).toString()
        return netWeight
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {


        val item = p0?.getItemAtPosition(p2) as String

        receivingData.storageLocationCode = storageLocation!![p2].procureLocationCode
        receivingData.storageLocation = storageLocation!![p2].procureLocationName
        loggedInPlantId =  (storageLocation!![p2].plant).toString()

        val data = materialStorageLocationList.filter { it.storageLocationCode.equals(receivingData.storageLocationCode) }
        if(data.size>0) {
            val materialData =
                materialList.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[0].materialCode)) }
            if (materialData.size > 0) {
                receivingData.materialCode =
                    MATERIAL_CODE.plus(materialData[0].materialCode)
                receivingData.materialName = materialData[0].materialName.toString()
                receivingData.unitsOfMeasure = materialData[0].unitsOfMeasure.toString()
                receivingData.currency = materialData[0].currency
                receivingData.item = "00001"
                grnPrice = calculatePrice(materialData[0].price.toString())
                binding.clSupplierInfo.tvProduct.setText(
                    materialData[0].materialCode.plus("-").plus(materialData[0].materialName)
                )
                clearPurchaseOrder()
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
}

