package com.olam.warehouse.vegax.offloadingghana.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.PurchaseType
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.databinding.FragmentVegaOffloadingGhanaSupplierBinding
import com.olam.warehouse.vegax.offloadingghana.databinding.ItemVegaOffloadingGhanaSupplierBagBinding
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 19/6/2020.
 */
class VegaGhanaOffloadingSupplierFragment : BaseFragment(), VegaSingleSelectListener {
    private var receivingData = VegaReceiving()
    private var supplierList = mutableListOf<VegaVendor>()
    private var storageLocationList = mutableListOf<VegaCustomStLocation>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var storageLocation: List<VegaCustomStLocation>? = null
    private var savedList = mutableListOf<VegaEcuadorOffloadingBagMaterial>()

    private lateinit var binding: FragmentVegaOffloadingGhanaSupplierBinding
    private var callBack: CallBack? = null
    private val vm: VegaGhanaOffloadingViewModel by viewModel()

    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var purchaseOrderQuantity: String = "0"
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var bagCountTotal = 0
    private var procureType = ""
    private lateinit var productNameList:List<VegaMaterial>
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null



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
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_offloading_ghana_supplier

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        ) = VegaGhanaOffloadingSupplierFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaOffloadingGhanaSupplierBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingecuador/ui/VegaEcuadorOffloadingSupplierFragment")
            .title("Ecuador Offloading")
            .with(tracker)
        initUI()
        clickEvent()
    }

    private fun clickEvent() {
        binding.clSupplierInfo.tvProduct.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_product),false,false)
        }
        binding.clSupplierInfo.tvSupplier.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_supplier),true,false)
        }
        binding.clSupplierInfo.tvReceivingWH.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_warehouse),false,true)
        }
    }

    private fun initUI() {
        updateMandatory()
        receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(OFFLOADING_POST_BAG_DATA)!!
        if (receivingData.tmpWbId.isEmpty()) {
            receivingData.tmpWbId = getTmpId()
        }

//        vm.getSavedBagItems()
//        vm.bagItemsSaved.observe(viewLifecycleOwner, Observer {
//            savedList = it.filter { !it.mtntWeight.isNullOrEmpty()}.toMutableList()
//            updateBagItems(savedList)
//            ll_receiving_WH_prePopulate.visible()
//            ll_recevingWH.gone()
//            savedList.forEach { item ->
//                tvProduct.setText(item.mtntWeight)
//                tvSupplier.setText(item.supplierName)
//                binding.clSupplierInfo.tvReceivingWHPrePopulate.setText(item.receivingLocation)
//                tvTruckNo.setText(item.truckNo)
//                tvDriverName.setText(item.materialName)
//                tvPhoneNo.setText(item.wbId)
//            }
//
//        })

        vm.product.observe(viewLifecycleOwner, Observer {
            productNameList=it
            it.forEach { item ->
                if (item.materialCode == "100000005277") {
                    receivingData.materialCode =
                        MATERIAL_CODE.plus(item.materialCode)
                    receivingData.materialName = item.materialName.toString()
                    receivingData.unitsOfMeasure = item.unitsOfMeasure.toString()
                    binding.clSupplierInfo.tvProduct.setText(item.materialName.toString())
                    binding.clSupplierInfo.tvProduct.isEnabled = false
                }
            }
        })
        vm.getProducts()

        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
        })
        vm.getSuppliers()

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            storageLocation = it

            if (storageLocation?.size == 1) {
                binding.clSupplierInfo.llReceivingWHPrePopulate.visible()
                binding.clSupplierInfo.llRecevingWH.gone()

                binding.clSupplierInfo.tvReceivingWHPrePopulate.setText(
                    storageLocation!![0].procureLocationCode.plus(" - ").plus(
                        storageLocation!![0].procureLocationName
                    )
                )

                receivingData.storageLocationCode = storageLocation!![0].procureLocationCode
                receivingData.storageLocation = storageLocation!![0].procureLocationName

            } else {
                binding.clSupplierInfo.llReceivingWHPrePopulate.gone()
                binding.clSupplierInfo.llRecevingWH.visible()

                vm.custonLocation.observe(viewLifecycleOwner, Observer {
                    storageLocationList = it.toMutableList()

                })
            }
        })
        vm.getCustomLocations()

        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        vm.poListLocal.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUILocal(it) })

//        binding.btSave.setOnClickListener {
//            saveItemsLocally() }

        vm.offloadingItemCountLocal.observe(viewLifecycleOwner, Observer {
            if (it != null && it.size > 0) {
                binding.llOffline.visible()
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOffloadingWithLineItemCount()

        vm.getBagItems(receivingData.materialCode, receivingData.supplierCode.toString(), "", "", receivingData.tmpWbId)
        binding.btAddWeight.setOnClickListener { validateInputs() }
        binding.btProceed.setOnClickListener {

            when {
                binding.clSupplierInfo.tvProduct.text.toString().isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_product)
                )
                binding.clSupplierInfo.tvSupplier.text.toString().isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_supplier)
                )
//                tvWRNo!!.length() < 5 -> showSnack(requireContext().resources.getString(R.string.wr_no_should_be_5_digit))
                else -> {
                    moveToSummary()
                }
            }
        }
        binding.llOffline.setOnClickListener { moveToOfflineSummary() }
        updateTotalWeights()
    }

    private fun showSingleSelectDialog(title: String, isSupplier: Boolean,isWh:Boolean) {
        val list: List<String>
       if(isSupplier){
           list = supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
       }else if(isWh){
          list= storageLocationList.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
       }else{
           list = productNameList.map { data -> data.materialName.toString() }
       }

        customDialog =
            VegaCustomSingleSelectDialogWithSearch(title = title, isVendor = isSupplier,
                isWh = isWh, items = list, activity = requireActivity(), listener = this)

        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)

    }

    private fun updatePurchaseOrderUILocal(it: List<VegaEcuadorPurchaseOrder>?) {
        if (it != null) {
            purchaseOrderList = it.toMutableList()
        }
    }

    private fun moveToOfflineSummary() {
        callBack?.replaceFragment(OFFLOADING_OFFLINE)
    }

    private fun saveItemsLocally(){
        bagList.forEach { material ->
            material.message = getString(R.string.stored_locally)
            material.tmpWbId = receivingData.tmpWbId
            material.supplierCode = receivingData.supplierCode!!
            material.materialCode = receivingData.materialCode!!
            material.truckNo = receivingData.truckNo
            material.receivingLocation = receivingData.receivingWH
            material.wbId = receivingData.phoneNo
            material.batchNumber = receivingData.batchNumber!!
            material.supplierName = receivingData.supplierName!!
            material.materialName = receivingData.driverName
            material.purcheseOrderNo = receivingData.purchaseDocNum!!
            material.mtntWeight = receivingData.product
            vm.saveBagDetails(material)
        }
        activity?.finish()
    }

//    private fun enableSaveBtn(flag: Boolean) {
//        when (flag) {
//            true -> {
//                binding.btSave.isEnabled = true
//                binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.blue_light))
//            }
//            false -> {
//                binding.btSave.isEnabled = false
//                binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
//            }
//        }
//
//    }

    private fun showReceivingLocationDialog(it: List<VegaStorageLocation>) {
        val location = it.map { data -> data.storageLocationCode.plus(" - ").plus(data.storageLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.storage_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.clSupplierInfo.tvReceivingLocation.text = text
                receivingData.storageLocationCode = it[index].storageLocationCode
                receivingData.storageLocation = it[index].storageLocationName
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
        bundle.putString(UNITS_OF_MEASURE, "MT")
        bundle.putParcelable(BAG_MATERIAL, bagMaterial)
        callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT_SUPPLIER, bundle)
    }

    private fun updateBagItems(bagItems: List<VegaEcuadorOffloadingBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
//            enableSaveBtn(bagList.size > 0)
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
        val addedBagItems = arrayListOf<String>()
        var palletAddedCount = 0
        bagList.forEachIndexed { index, it ->
            if (it.id == bagMaterial.id) {
                isExistValue = true
                pos = index
            }
            palletAddedCount += if (it.noOfPallet?.isNotEmpty() == true) it.noOfPallet?.toInt() ?: 0 else 0
            if (!addedBagItems.contains(it.bagType) && it.bagType.isNotEmpty()) addedBagItems.add(it.bagType)
        }
        palletAddedCount += if (bagMaterial.noOfPallet?.isNotEmpty() == true) bagMaterial.noOfPallet?.toInt()
            ?: 0 else 0
        if (bagMaterial.bagType.isNotEmpty()) addedBagItems.add(bagMaterial.bagType)
        val distItem = addedBagItems.distinct()
        val tareWeightCalculation = if (palletAddedCount > 0) distItem.size + 1 else distItem.size
        if (tareWeightCalculation <= 3 || (tareWeightCalculation <= 3 && bagMaterial.bagType.isEmpty() && bagMaterial.noOfPallet?.isEmpty() == true)) {
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
        else {
            showSnack(getString(R.string.please_add_less_three))
        }

    }

    private fun setUpAdapter(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) {
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            binding.rvWeight.setUpAdapter(
                bagList,
                R.layout.item_vega_offloading_ghana_supplier_bag,
                ItemVegaOffloadingGhanaSupplierBagBinding::inflate,
                { it, pos, bindItem ->
                    if (pos % 2 == 0) {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                    } else {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                    }
                    bindItem.tvSno.text = pos.plus(1).toString()
                    bindItem.tvBag.text = it.bagCount
                    bindItem.tvGrossWeight.text =
                        it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus("MT")
                    val avgAvlue =
                        it.palletAverage?.toDouble()
                            ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                    bindItem.tvTarWeight.text = avgAvlue?.formatThreeDigits().plus(" ").plus("MT")
                    bindItem.tvNetWeight.text =
                        it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                            .plus("MT")
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
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    bagList.remove(item)
                    binding.rvWeight.adapter?.notifyItemRemoved(pos)
                    vm.deleteBagDetails(item.id, item.tmpWbId)

                    if (bagList.size == 0) {
                        binding.btProceed.isEnabled = false
                        binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                    }
                },
                { dismiss() })
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
        binding.clNet.tvGrossWeightValue.text = grossWeight.formatThreeDigits().plus("MT")
        binding.clNet.tvTareWeightValue.text = tareWeight.formatThreeDigits().plus("MT")
        binding.clNet.tvNetWeightValue.text = netWeight.formatThreeDigits().plus("MT")
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
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_locations)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSelectType.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_type)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSelectPOLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_po)) { mandatoryStars() } }
        binding.clSupplierInfo.tvReceiving.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_wh)) { mandatoryStars() } }
        binding.clSupplierInfo.WRNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.wr_no_m)) { mandatoryStars() } }
    }

    private fun moveToSummary() {
        if (!receivingData.purchaseDocNum.isNullOrEmpty() && receivingData.netWeight.toDouble() > purchaseOrderQuantity.toDouble()) {
            showSnack(getString(R.string.net_weight_error))
        } else {
            receivingData.wsGate = "WS01"
            receivingData.weighBridgeType = PROCURE
            mReceiving.clear()
            bagCountTotal = 0
            bagList.forEachIndexed { index, it ->
                val receiving = receivingData.copy()
                receiving.bagType = it.bagType
                receiving.bagCount = it.bagCount
                receiving.bagWeight =
                    it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!.formatThreeDigits()
                receiving.netWeight = it.netWeight
                receiving.grossWeight = it.grossWeight
                receiving.tareWeight = it.tareWeight
                receiving.driverName = binding.clSupplierInfo.tvDriverName.text.toString()
                receiving.receivingWH = binding.clSupplierInfo.tvReceivingWH.text.toString()
//                receiving.challan = tvWRNo.text.toString()
                receiving.truckNo = binding.clSupplierInfo.tvTruckNo.text.toString()
                receiving.driverName = binding.clSupplierInfo.tvDriverName.text.toString()
                receiving.phoneNo = binding.clSupplierInfo.tvPhoneNo.text.toString()
                receiving.item = index.inc().toString()
                mReceiving.add(receiving)
                bagCountTotal = bagCountTotal.plus(it.bagCount.toInt())
            }
            receivingData.bagCount = bagCountTotal.toString()
            receivingData.product = binding.clSupplierInfo.tvProduct.text.toString()
            receivingData.supplierName = binding.clSupplierInfo.tvSupplier.text.toString()
            if (binding.clSupplierInfo.llReceivingWHPrePopulate.isVisible) {
                receivingData.receivingWH =
                    binding.clSupplierInfo.tvReceivingWHPrePopulate.text.toString()
            } else {
                receivingData.receivingWH = binding.clSupplierInfo.tvReceivingWH.text.toString()
            }
//            receivingData.challan = tvWRNo.text.toString()
            receivingData.truckNo = binding.clSupplierInfo.tvTruckNo.text.toString()
            receivingData.driverName = binding.clSupplierInfo.tvDriverName.text.toString()
            receivingData.phoneNo = binding.clSupplierInfo.tvPhoneNo.text.toString()
            callBack?.replaceFragment(OFFLOADING_SUMMARY_FRAG, receivingData, mReceiving, bagList)
        }
    }

    private fun validateInputs() {
        if (binding.clSupplierInfo.llReceivingWHPrePopulate.isVisible) {
            when {
                binding.clSupplierInfo.tvProduct.text.toString().isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_product)
                )
                binding.clSupplierInfo.tvSupplier.text.toString().isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_supplier)
                )
                binding.clSupplierInfo.tvReceivingWHPrePopulate.length() == 0 -> showSnack(
                    getString(
                        R.string.message_valid_receiving_wh
                    )
                )
//                tvWRNo!!.length() < 5 -> showSnack(requireContext().resources.getString(R.string.wr_no_should_be_5_digit))
                binding.clSupplierInfo.llSelectPO.isVisible && binding.clSupplierInfo.tvSelectPO.text.isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_po_type)
                )
                else -> {
                    vm.getBagItems(
                        receivingData.materialCode,
                        receivingData.supplierCode.toString(),
                        "",
                        "",
                        receivingData.tmpWbId
                    )

                    moveBagAddWeight(VegaEcuadorOffloadingBagMaterial())
                }
            }
        } else if (binding.clSupplierInfo.llRecevingWH.isVisible) {
            when {
                binding.clSupplierInfo.tvProduct.text.toString().isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_product)
                )
                binding.clSupplierInfo.tvSupplier.text.toString().isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_supplier)
                )
                binding.clSupplierInfo.tvReceivingWH.length() == 0 -> showSnack(getString(R.string.message_valid_receiving_wh))
//                tvWRNo!!.length() < 5 -> showSnack(requireContext().resources.getString(R.string.wr_no_should_be_5_digit))
                binding.clSupplierInfo.llSelectPO.isVisible && binding.clSupplierInfo.tvSelectPO.text.isNullOrEmpty() -> showSnack(
                    getString(R.string.message_valid_po_type)
                )
                else -> {
                    vm.getBagItems(
                        receivingData.materialCode,
                        receivingData.supplierCode.toString(),
                        "",
                        "",
                        receivingData.tmpWbId
                    )

                    moveBagAddWeight(VegaEcuadorOffloadingBagMaterial())
                }
            }
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        if(isWh){
            binding.clSupplierInfo.tvReceivingWH.text= data
            storageLocationList.forEach { location ->
                if (location.procureLocationCode == binding.clSupplierInfo.tvReceivingWH.text.toString()
                        .split(" - ")[0].trim()
                ) {
                    receivingData.storageLocationCode = location.procureLocationCode
                    receivingData.storageLocation = location.procureLocationName
                    clearPurchaseOrder()
                }
            }
        }else if(isVendor){
            binding.clSupplierInfo.tvSupplier.text=data
            supplierList.forEach { vendor ->
                if (vendor.vendorCode == data.split(" - ")[0].trim()) {
                    receivingData.supplierCode = vendor.vendorCode
                    receivingData.supplierName = vendor.vendorName
                    clearPurchaseOrder()
                }
            }
        }else{
            binding.clSupplierInfo.tvProduct.text=data
            productNameList.forEach {material->
                if (material.materialName == binding.clSupplierInfo.tvProduct.text.toString()) {
                    receivingData.materialCode =
                        MATERIAL_CODE.plus(material.materialCode)
                    receivingData.materialName = material.materialName.toString()
                    receivingData.unitsOfMeasure =
                        material.unitsOfMeasure.toString()
                    clearPurchaseOrder()
                }

            }
        }
    }
}
