package com.olam.warehouse.vegax.offloadingecuador.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.common.VegaTrackTraceFragment
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.PurchaseType
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingecuador.R
import com.olam.warehouse.vegax.offloadingecuador.databinding.FragmentVegaEcuadorOffloadingSupplierBinding
import com.olam.warehouse.vegax.offloadingecuador.databinding.ItemVegaEcuadorOffloadingSupplierBagBinding
import com.olam.warehouse.vegax.offloadingecuador.ui.VegaEcuadorOffloadingViewModel
import com.olam.warehouse.vegax.offloadingecuador.utils.BAG_MATERIAL
import com.olam.warehouse.vegax.offloadingecuador.utils.FRAG_ADD_BAG_WEIGHT
import com.olam.warehouse.vegax.offloadingecuador.utils.MATERIAL_CODE
import com.olam.warehouse.vegax.offloadingecuador.utils.MATERIAL_NAME
import com.olam.warehouse.vegax.offloadingecuador.utils.OFFLOADING_DATA
import com.olam.warehouse.vegax.offloadingecuador.utils.OFFLOADING_OFFLINE
import com.olam.warehouse.vegax.offloadingecuador.utils.OFFLOADING_POST_BAG_DATA
import com.olam.warehouse.vegax.offloadingecuador.utils.OFFLOADING_SUMMARY_FRAG
import com.olam.warehouse.vegax.offloadingecuador.utils.PROCURE
import com.olam.warehouse.vegax.offloadingecuador.utils.PRODUCT
import com.olam.warehouse.vegax.offloadingecuador.utils.SUPPLIER
import com.olam.warehouse.vegax.offloadingecuador.utils.UNITS_OF_MEASURE
import com.olam.warehouse.vegax.offloadingecuador.utils.WB01
import com.olam.warehouse.vegax.offloadingecuador.utils.WB_LIST
import com.olam.warehouse.vegax.offloadingecuador.utils.WEIGHBRIDGE
import com.olam.warehouse.vegax.offloadingecuador.utils.WEIGHMENT_TYPE
import com.olam.warehouse.vegax.offloadingecuador.utils.WEIGHSCALE
import com.olam.warehouse.vegax.offloadingecuador.utils.WS01
import com.olam.warehouse.vegax.offloadingecuador.utils.getColor
import com.olam.warehouse.vegax.offloadingecuador.utils.getTmpId
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 19/6/2020.
 */
class VegaEcuadorOffloadingSupplierFragment : BaseFragment() , VegaSingleSelectCommonListener {
    private var receivingData = VegaReceiving()
    private var wbData = VegaQualityWBDetails()
    private var supplierList = mutableListOf<VegaVendor>()
    private var storageLocationList = mutableListOf<VegaStorageLocation>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var mReceiving = mutableListOf<VegaReceiving>()

    private lateinit var binding: FragmentVegaEcuadorOffloadingSupplierBinding
    private var callBack: VegaEccuadorOffloadingListener? = null
    private val vm: VegaEcuadorOffloadingViewModel by viewModel()

    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var purchaseOrderQuantity: String = "0"
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var bagCountTotal = 0
    private var procureType = ""
    private var productList = ArrayList<String>()
    private var supplierLists = ArrayList<String>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var materials = ArrayList<VegaMaterial>()
    private var weighmentType:String = ""

    var ttProcurementType = ""
    var ttComplaintType = ""
    var ttDirectFarmerDataList = java.util.ArrayList<TrackTraceFarmerModel>()
    var isComplaint = false
    var vendorFlag = false
    var ttDbFarmerList = mutableListOf<VegaTrackTraceFarmerData>()
    var ttFarmerlessTransactionDetails = TrackTraceTransactionIdDetails()
    var sourceLotDetails = TrackTraceSourceLotDetails()
    var isSourceLotMandatory = false
    var isTransIdMandatory = false

   /* interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            offloadingBagMaterialData: Any
        )

        fun replaceFragment(moveFrag: String)

        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving,
            mReceiving: MutableList<VegaReceiving>,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        )
    }*/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaEccuadorOffloadingListener
    }

    override val layoutResourceId = R.layout.fragment_vega_ecuador_offloading_supplier

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
            procurementType: String,
            wbData:VegaQualityWBDetails,
            weighmentType: String) = VegaEcuadorOffloadingSupplierFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
            putString(Constants.PROCUREMENT_TYPE, procurementType)
            putParcelable(WB_LIST, wbData)
            putString(WEIGHMENT_TYPE, weighmentType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorOffloadingSupplierBinding.inflate(layoutInflater)
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
        weighmentType = arguments?.getString(WEIGHMENT_TYPE)?:""
        wbData = arguments?.getParcelable(WB_LIST)?:VegaQualityWBDetails()
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(OFFLOADING_POST_BAG_DATA)!!
        ttProcurementType = arguments?.getString(Constants.PROCUREMENT_TYPE)?:""
        if(wbData.weighBridgeId.isNotEmpty()){
            receivingData.weighBridgeId = wbData.weighBridgeId
        }
        if(ttProcurementType.isNotEmpty()) {
            loadTTSupplierTypeFragment()
        }
        if (receivingData.tmpWbId.isEmpty()) {
            receivingData.tmpWbId = getTmpId()
        }

        vm.getFeatureMaster(Constants.TRACK_TRACE)
        vm.featureMaster.observe(viewLifecycleOwner, Observer { updateFeatureUI(it) })

        vm.product.observe(viewLifecycleOwner, Observer {
            materials = it as ArrayList<VegaMaterial>

            it.filter { it.materialType=="RM" }.forEachIndexed { index, vegaMaterial ->
                productList.add(vegaMaterial.materialCode.plus("-").plus(vegaMaterial.materialName))
            }

//            val productAdapter =
//                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, products)
//            tvProduct.threshold = 1
//            tvProduct.setAdapter(productAdapter)
//            tvProduct.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
//                it.forEach { material ->
//                    if (material.materialName == tvProduct.text.toString()) {
//                        receivingData.materialCode =
//                            MATERIAL_CODE.plus(material.materialCode)
//                        receivingData.materialName = material.materialName.toString()
//                        receivingData.unitsOfMeasure = material.unitsOfMeasure.toString()
//                        clearPurchaseOrder()
//                    }
//                }
//            }
        })
        vm.getProducts()

        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
            val suppliers = it.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            supplierLists = suppliers as ArrayList<String>
//            val supplierAdapter =
//                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
//            tvSupplier.threshold = 1
//            tvSupplier.setAdapter(supplierAdapter)
//            tvSupplier.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
//                it.forEach { vendor ->
//                    if (vendor.vendorCode == tvSupplier.text.toString().split(" - ")[0]) {
//                        receivingData.supplierCode = vendor.vendorCode
//                        receivingData.supplierName = vendor.vendorName
//                        clearPurchaseOrder()
//                    }
//                }
//            }
        })
        vm.getSuppliers()

        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        vm.poListLocal.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUILocal(it) })

        vm.offloadingItemCountLocal.observe(viewLifecycleOwner, Observer {
            if (it != null && it.size > 0) {
                binding.llOffline.visible()
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOffloadingWithLineItemCount()
        binding.clSupplierInfo.tvSupplier.setOnClickListener {
            if(ttProcurementType.equals(Constants.IN_DIRECT) && isSourceLotMandatory && sourceLotDetails.sourceLotId?.isEmpty()==true){
                showSnack(getString(R.string.source_lot_id_should_not_be_empty) )
            }else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && isTransIdMandatory && ttFarmerlessTransactionDetails.dwTransactionId?.isEmpty()==true){
                showSnack(getString(R.string.trans_id_should_not_be_empty) )
            }else {
                showSingleSelectDialog(getString(R.string.select_supplier), SUPPLIER)
            }
        }

        binding.clSupplierInfo.tvProduct.setOnClickListener {
            if(ttProcurementType.equals(Constants.IN_DIRECT) && isSourceLotMandatory && sourceLotDetails.sourceLotId?.isEmpty()==true){
                showSnack(getString(R.string.source_lot_id_should_not_be_empty) )
            }else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && isTransIdMandatory && ttFarmerlessTransactionDetails.dwTransactionId?.isEmpty()==true){
                showSnack(getString(R.string.trans_id_should_not_be_empty) )
            }else {
                showSingleSelectDialog(getString(R.string.select_product), PRODUCT)
            }
        }
        binding.clSupplierInfo.tvProcurementType.setOnClickListener { showProcurementTypeDialog() }
        binding.clSupplierInfo.tvSelectPO.setOnClickListener { showPurchaseOrderDialog(purchaseOrderList) }
        binding.btAddWeight.setOnClickListener { validateInputs() }
        binding.btProceed.setOnClickListener {
            if(weighmentType.equals(WEIGHBRIDGE)){
                validateInputsForWBFlow()
            } else {
                moveToSummary()
            }
        }
        binding.llOffline.setOnClickListener { moveToOfflineSummary() }
        updateTotalWeights()
        if(weighmentType.equals(WEIGHBRIDGE)){
            hideAddWeight()
            binding.clNet.tvGrossWeightValue.text = wbData.grossWeight.plus(wbData.unitsOfMeasure)
            binding.clNet.tvTareWeightValue.text = wbData.bagWeight.plus(wbData.unitsOfMeasure)
            binding.clNet.tvNetWeightValue.text = wbData.netWeight.plus(wbData.unitsOfMeasure)
            binding.btProceed.isEnabled = true
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        }
    }

    private fun hideAddWeight(){
        binding.tvWeightSummary.gone()
        binding.btAddWeight.gone()
        binding.ivAdd.gone()
        binding.clBagHead.gone()
        binding.rvWeight.gone()
        binding.tvNoWeight.gone()
    }

    private fun loadTTSupplierTypeFragment(){
        val bundle = Bundle()
        bundle.putString(
            Constants.PROCUREMENT_TYPE, ttProcurementType)
        displayFragment(VegaTrackTraceFragment.newInstance(bundle), false)
        if(ttProcurementType.equals(Constants.DIRECT)) {
            binding.clSupplierInfo.llSupplier.gone()
        }
    }



    private fun updatePurchaseOrderUILocal(it: List<VegaEcuadorPurchaseOrder>?) {
        if (it != null) {
            purchaseOrderList = it.toMutableList()
        }
    }

    private fun moveToOfflineSummary() {
        callBack?.replaceFragment(OFFLOADING_OFFLINE)
    }

    private fun showReceivingLocationDialog(it: List<VegaStorageLocation>) {
        val location = it.map { data -> data.storageLocationCode.plus(" - ").plus(data.storageLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.storage_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.clSupplierInfo.tvReceivingLocation.text = text
                receivingData.storageLocationCode = it[index].storageLocationCode
                receivingData.storageLocationName = it[index].storageLocationName
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showProcurementTypeDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.select_procurement_type)
            listItemsSingleChoice(R.array.procurementType) { _, index, text ->
                binding.clSupplierInfo.tvProcurementType.text = text
                when (index) {
                    0 -> {
                        binding.clSupplierInfo.llSelectPO.gone()
                        clearPurchaseOrder()
                        procureType = "Spot"
                        vm.getBagItems(
                            receivingData.materialCode,
                            receivingData.supplierCode.toString(),
                            procureType,
                            "",
                            receivingData.tmpWbId
                        )
                    }
                    1 -> {
                        binding.clSupplierInfo.llSelectPO.visible()
                        if (isOnline()) vm.getPOList() else vm.getPOListLocal()
                        procureType = "Fixed"
                    }
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun clearPurchaseOrder() {
        receivingData.purchaseDocNum = ""
        receivingData.purchaseDocDesc = ""
        purchaseOrderQuantity = "0"
        binding.clSupplierInfo.tvSelectPO.text = ""
    }

    private fun updatePurchaseOrderUI(response: Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                purchaseOrderList = it.toMutableList()
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

    private fun showPurchaseOrderDialog(list: List<VegaEcuadorPurchaseOrder>) {
        if (receivingData.materialCode.isNullOrEmpty()) {
            showSnack(getString(R.string.message_valid_product))
        } else if (receivingData.supplierCode.isNullOrEmpty()) {
            showSnack(getString(R.string.message_valid_supplier))
        } else {
            val purData = list.filter {
                receivingData.materialCode?.let { it1 -> it.materialNumber.contains(it1) }!! &&
                        receivingData.supplierCode?.let { it1 -> it.supplier.contains(it1) }!! && it.bsart.equals(
                    PurchaseType.FIXEDPURCHASE.type
                )
            }
            val purchaseOrders =
                purData.map { data -> data.poId.plus(" - ").plus(data.menge).plus(receivingData.unitsOfMeasure) }
            MaterialDialog(requireContext()).show {
                title(R.string.select_purchase_order)
                listItemsSingleChoice(items = purchaseOrders) { _, index, text ->
                    binding.clSupplierInfo.tvSelectPO.text = text
                    receivingData.purchaseDocNum = purData[index].poId
                    receivingData.purchaseDocDesc = purData[index].ebelp
                    purchaseOrderQuantity = purData[index].menge
                    receivingData.purchaseDocQty = purchaseOrderQuantity
                    vm.getBagItems(
                        receivingData.materialCode,
                        receivingData.supplierCode.toString(),
                        procureType,
                        receivingData.purchaseDocNum.toString(),
                        receivingData.tmpWbId
                    )
                }
                positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
            }
        }
    }

    private fun moveBagAddWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val bundle = Bundle()
        bundle.putString(MATERIAL_NAME, receivingData.materialName)
        bundle.putString(UNITS_OF_MEASURE, receivingData.unitsOfMeasure)
        bundle.putParcelable(BAG_MATERIAL, bagMaterial)
        callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT, bundle)
    }

    private fun updateBagItems(bagItems: List<VegaEcuadorOffloadingBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
            if (bagItems.size > 0) {
                val bundle = Bundle()
                bundle.putString(
                    Constants.PALLET_WEIGHT,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight else palletWeight
                )
                bundle.putString(
                    Constants.PALLET_COUNT,
                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet else palletCount
                )
                bundle.putBoolean(Constants.PALLET_EDIT, true)
                bundle.putInt(Constants.PALLET_ADDED, bagList.size)
                if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
                    (bagList.size == palletCount.toInt()) else binding.btProceed.isEnabled = bagList.size > 0
                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                    getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
                )
                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))

                updateTotalWeights()
            }
            setUpAdapter(bagList)
        }
    }

    fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        var isExistValue = false
        var pos: Int = 0
        bagList.forEachIndexed { index, it ->
            if (it.id == bagMaterial.id) {
                isExistValue = true
                pos = index
            }
        }
        if (!isExistValue) {
            bagMaterial.id = Random.nextInt()
            bagMaterial.materialCode = receivingData.materialCode.toString()
            bagMaterial.supplierCode = receivingData.supplierCode.toString()
            bagMaterial.procureType = binding.clSupplierInfo.tvProcurementType.text.toString()
            bagMaterial.purcheseOrderNo = receivingData.purchaseDocNum.toString()
        } else {
            bagList.removeAt(pos)
        }
        bagList.add(bagMaterial)
        bagList.forEach { material ->
            material.message = getString(R.string.stored_locally)
            material.tmpWbId = receivingData.tmpWbId
            vm.saveBagDetails(material)
        }

        if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
            (bagList.size == palletCount.toInt())
        else binding.btProceed.isEnabled = bagList.size > 0
        if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
            getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
        )
        else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        updateTotalWeights()
    }

    private fun setUpAdapter(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) {
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            binding.rvWeight.setUpAdapter(
                bagList,
                R.layout.item_vega_ecuador_offloading_supplier_bag,
                ItemVegaEcuadorOffloadingSupplierBagBinding::inflate,
                { it, pos, bindItem ->
                    if (pos % 2 == 0) {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                    } else {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                    }
                    bindItem.tvSno.text = pos.plus(1).toString()
                    bindItem.tvBag.text = it.bagCount
                    bindItem.tvGrossWeight.text =
                        it.grossWeight.toDouble().formatThreeDigits().plus(" ")
                            .plus(receivingData.unitsOfMeasure)
                    val avgAvlue =
                        it.palletAverage?.toDouble()
                            ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                    bindItem.tvTarWeight.text =
                        avgAvlue?.formatThreeDigits().plus(" ").plus(receivingData.unitsOfMeasure)
                    bindItem.tvNetWeight.text =
                        it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                            .plus(receivingData.unitsOfMeasure)
                    bindItem.ivEdit.setOnClickListener { view ->
                        val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                        popupMenu.menuInflater.inflate(
                            com.olam.warehouse.login.R.menu.transaction_menu,
                            popupMenu.menu
                        )
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible =
                            false
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible =
                            false
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible =
                            false
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible =
                            true
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible =
                            true
                        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                            when (item.itemId) {
                                com.olam.warehouse.login.R.id.action_edit1 -> {
                                    it.netWeight = it.grossWeight.toDouble().minus(avgAvlue)
                                        .formatThreeDigits()
                                    moveBagAddWeight(it)
                                }
                                com.olam.warehouse.login.R.id.action_delete -> {
                                    showDeleteConfirmationDialog(it, pos)
                                }
                            }
                            true
                        })
                        popupMenu.show()
                    }
                })
        } else {
            binding.tvNoWeight.visible()
            binding.rvWeight.gone()
        }
    }

    fun showDeleteConfirmationDialog(item: VegaEcuadorOffloadingBagMaterial, pos: Int) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            getMetirialCustomView(this, getString(R.string.proceed), getString(R.string.cancel), {
                bagList.remove(item)
                binding.rvWeight.adapter?.notifyItemRemoved(pos)
                vm.deleteBagDetails(item.id, item.tmpWbId)
                if (bagList.size == 0) {
                    binding.btProceed.isEnabled = false
                    binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                }
            }, { dismiss() })
        }
    }

    fun updateTotalWeights() {
        var grossWeight = 0.0
        var tareWeight = 0.0
        val netWeight: Double
        for (item in bagList) {
            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                .plus(palletAvg.toDouble())
        }
        netWeight = grossWeight.minus(tareWeight)
        binding.clNet.tvGrossWeightValue.text = grossWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        binding.clNet.tvTareWeightValue.text = tareWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        binding.clNet.tvNetWeightValue.text = netWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        receivingData.grossWeight = grossWeight.formatThreeDigits()
        receivingData.tareWeight = tareWeight.formatThreeDigits()
        receivingData.netWeight = netWeight.formatThreeDigits()
    }

    private fun updateMandatory() {
        binding.clSupplierInfo.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.clSupplierInfo.tvReceivingLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_location)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSelectType.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_type)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSelectPOLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_po)) { mandatoryStars() } }
    }

    private fun moveToSummary() {
        if (!receivingData.purchaseDocNum.isNullOrEmpty() && receivingData.netWeight.toDouble() > purchaseOrderQuantity.toDouble()) {
            showSnack(getString(R.string.net_weight_error))
        } else {
            receivingData.weighBridgeType = PROCURE
            mReceiving.clear()
            bagCountTotal = 0
            /*Track & Trace*/
            if (ttProcurementType.equals(Constants.DIRECT)) {
                receivingData.ttFarmerList = getTTFarmerDataList()
                receivingData.eudrStatus = isComplaint
            } else if (ttProcurementType.equals(Constants.IN_DIRECT)) {
                receivingData.sourceLotId = sourceLotDetails?.sourceLotId.toString()
                receivingData.eudrStatus = sourceLotDetails?.isEudrComplaintFlag ?: false
            } else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)){
                receivingData.farmerLessTransactionId = ttFarmerlessTransactionDetails.dwTransactionId.toString()
                receivingData.eudrStatus = ttFarmerlessTransactionDetails?.compliantFlag ?: false
            }
            if (weighmentType.equals(WEIGHSCALE)) {
                receivingData.wsGate = WS01
            }
            bagList.forEachIndexed { index, it ->
                val receiving = receivingData.copy()
                receiving.bagType = it.bagType
                receiving.bagCount = it.bagCount
                receiving.bagWeight = it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!.formatThreeDigits()
                receiving.netWeight = it.netWeight
                receiving.grossWeight = it.grossWeight

//                receiving.sourceLotId = sourceLotDetails?.sourceLotId.toString()
//                receiving.eudrStatus = sourceLotDetails?.isEudrComplaintFlag?:false
                receiving.item = index.inc().toString()
                mReceiving.add(receiving)
                bagCountTotal = bagCountTotal.plus(it.bagCount.toInt())
            }
            receivingData.bagCount = bagCountTotal.toString()
             if (weighmentType.equals(WEIGHBRIDGE)) {
                receivingData.wsGate = WB01
                receivingData.netWeight = wbData.netWeight.toString()
                receivingData.tareWeight = wbData.bagWeight.toString()
                receivingData.grossWeight = wbData.grossWeight.toString()
                mReceiving.add(receivingData)
            }
            if (weighmentType.equals(WEIGHBRIDGE)) {
                if(!wbData.materialCode?.takeLast(12).equals(receivingData.materialCode?.takeLast(12))) {
                    showMaterialMismatchDialog()
                } else {
                    moveToSummaryPage()
                }
            } else {
                moveToSummaryPage()
                //prepareSuccessData(receivingData.weighBridgeId, false)
            }
        }
    }

    private fun moveToSummaryPage(){
        callBack?.replaceFragment(OFFLOADING_SUMMARY_FRAG, receivingData, mReceiving, bagList)
    }

    private fun showMaterialMismatchDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.material_mismatch_warning)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    receivingData.isDelete = true
                    moveToSummaryPage()
                },
                { dismiss() })
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

    private fun validateInputs() {
        when {
            receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_product))
            receivingData.supplierName.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_supplier))
            binding.clSupplierInfo.tvProcurementType.text.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_procurement_type))
            binding.clSupplierInfo.llSelectPO.isVisible && binding.clSupplierInfo.tvSelectPO.text.isNullOrEmpty() -> showSnack(
                getString(R.string.message_valid_po_type)
            )
            bagList.size == 3 -> showSnack(getString(R.string.restrict_add_bag))
            else -> moveBagAddWeight(VegaEcuadorOffloadingBagMaterial())
        }
    }

    private fun validateInputsForWBFlow(){
        when {
            receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_product))
            receivingData.supplierName.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_supplier))
            binding.clSupplierInfo.tvProcurementType.text.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_procurement_type))
            binding.clSupplierInfo.llSelectPO.isVisible && binding.clSupplierInfo.tvSelectPO.text.isNullOrEmpty() -> showSnack(
            getString(R.string.message_valid_po_type)
            )
//            bagList.size == 3 -> showSnack(getString(R.string.restrict_add_bag))
            else -> moveToSummary()
        }
    }

    private fun showSingleSelectDialog(title: String,currentFalg: String) {
        var list = ArrayList<String>()
       when(currentFalg) {
           SUPPLIER -> {
               list = supplierLists
           }
           PRODUCT -> {
               if(isComplaint){
                   list = materials.filter { it.materialType=="RM" }.filter { it.complainceFlag.equals("Compliant") }.map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
               } else {
                   list = materials.filter { it.materialType=="RM" }.filter { it.complainceFlag.equals("Non-Compliant") }.map {it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
               }
               if(list.isEmpty()){
                   list = materials.filter { it.materialType=="RM" }.map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
               }
//               list = productList
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
              receivingData.supplierCode = data.split("-")[0].trim()
                       receivingData.supplierName = data.split("-")[1].trim()
                        clearPurchaseOrder()
          }
            PRODUCT -> {
                binding.clSupplierInfo.tvProduct.text = data
                val materialData = data.split("-")

                receivingData.materialCode = MATERIAL_CODE.plus(materialData[0])
                receivingData.materialName = materialData[1]

                materials.find { it.materialCode == materialData[0] }.apply { receivingData.unitsOfMeasure = this?.unitsOfMeasure.toString() }

                clearPurchaseOrder()
            }
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

    fun isVendor(flag: Boolean) {
        if(flag){
            binding.clSupplierInfo.llSupplier.gone()
        } else {
            binding.clSupplierInfo.llSupplier.visible()
        }
    }

    fun updateFarmerListDetails(farmerList: java.util.ArrayList<TrackTraceFarmerModel>){
        ttDirectFarmerDataList.clear()
        ttDirectFarmerDataList.addAll(farmerList)
        if(ttDirectFarmerDataList.isNotEmpty()) {
            receivingData.supplierCode = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(0)?.trim()
            receivingData.supplierName = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(1)
//            receivingData.supplierCode = data.split("-")[0]
//            receivingData.supplierName = data.split("-")[1]
            var result = ttDirectFarmerDataList.any { it.isComplaint == 0 }
            if(result){
                isComplaint = false
            }else{
                isComplaint = true
            }
        } else {
            isComplaint = false
        }
    }

    fun isComplaint(status:Int){
        if(status == 0){
            isComplaint = false
        } else {
            isComplaint = true
        }
    }

    fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails){
        isComplaint = sourceLotDetails?.isEudrComplaintFlag?:false
        this.sourceLotDetails = sourceLotDetails
        if(sourceLotDetails.vendorCode?.isNotEmpty() == true){
            var checkSupplier = supplierList.filter { it.vendorCode.equals(sourceLotDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
            } else {
                var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                binding.clSupplierInfo.tvSupplier.text = data
                receivingData.supplierCode = data.split("-")[0].trim()
                receivingData.supplierName = data.split("-")[1].trim()
                clearPurchaseOrder()
            }
        } else {
            showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
        }
    }

    fun updateFarmerlessTransactionDetails(transactionIdDetails: TrackTraceTransactionIdDetails){
        resetMaterial()
        isComplaint = transactionIdDetails?.compliantFlag?:false
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

    private fun resetMaterial(){
        binding.clSupplierInfo.tvSupplier.text = ""
        binding.clSupplierInfo.tvProduct.text = ""
        receivingData.materialCode = ""
        receivingData.materialName = ""
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

}
