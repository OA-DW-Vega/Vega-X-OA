package com.olam.warehouse.vegax.receivingghana.ui

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
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.PurchaseType
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receivingghana.R
import com.olam.warehouse.vegax.receivingghana.databinding.FragmentVegaGhanaReceivingSupplierBinding
import com.olam.warehouse.vegax.receivingghana.utils.*
import kotlinx.android.synthetic.main.item_vega_ghana_receiving_supplier_bag.view.*
import kotlinx.android.synthetic.main.layout_vega_ghana_receiving_supplier_info.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 9/25/2020.
 */
class VegaGhanaReceivingSupplierFragment : BaseFragment() {
    private var receivingData = VegaReceiving()
    private var supplierList = mutableListOf<VegaVendor>()
    private var storageLocationList = mutableListOf<VegaStorageLocation>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var mReceiving = mutableListOf<VegaReceiving>()

    private lateinit var binding: FragmentVegaGhanaReceivingSupplierBinding
    private var callBack: CallBack? = null
    private val vm: VegaGhanaReceivingViewModel by viewModel()

    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var purchaseOrderQuantity: String = "0"
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var bagCountTotal = 0
    private var procureType = ""

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

    override val layoutResourceId = R.layout.fragment_vega_ghana_receiving_supplier

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        ) = VegaGhanaReceivingSupplierFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaReceivingSupplierBinding.inflate(layoutInflater)
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

        vm.product.observe(viewLifecycleOwner, Observer {
            val products = it.map { data -> data.materialName }
            val productAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, products)
            tvProduct.threshold = 1
            tvProduct.setAdapter(productAdapter)
            tvProduct.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.materialName == tvProduct.text.toString()) {
                        receivingData.materialCode =
                            MATERIAL_CODE.plus(material.materialCode)
                        receivingData.materialName = material.materialName.toString()
                        receivingData.unitsOfMeasure = material.unitsOfMeasure.toString()
                        clearPurchaseOrder()
                    }
                }
            }
        })
        vm.getProducts()

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
                        receivingData.supplierCode = vendor.vendorCode
                        receivingData.supplierName = vendor.vendorName
                        clearPurchaseOrder()
                    }
                }
            }
        })
        vm.getSuppliers()

        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        vm.poListLocal.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUILocal(it) })

        vm.offloadingItemCountLocal.observe(this, Observer {
            if (it != null && it.size > 0) {
                binding.llOffline.visible()
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOffloadingWithLineItemCount()

        binding.clSupplierInfo.tvProcurementType.setOnClickListener { showProcurementTypeDialog() }
        binding.clSupplierInfo.tvSelectPO.setOnClickListener { showPurchaseOrderDialog(purchaseOrderList) }
        binding.btAddWeight.setOnClickListener { validateInputs() }
        binding.btProceed.setOnClickListener { moveToSummary() }
        binding.llOffline.setOnClickListener { moveToOfflineSummary() }
        updateTotalWeights()
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
                        if (AppUtils.isOnline()) vm.getPOList() else vm.getPOListLocal()
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
                    getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
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
            bagMaterial.procureType = tvProcurementType.text.toString()
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
            getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
        )
        else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        updateTotalWeights()
    }

    private fun setUpAdapter(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) {
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            binding.rvWeight.setUp(bagList, R.layout.item_vega_ghana_receiving_supplier_bag, { it, pos ->
                if (pos % 2 == 0) {
                    clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                } else {
                    clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                }
                tvSno.text = pos.plus(1).toString()
                tvBag.text = it.bagCount
                tvGrossWeight.text =
                    it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus(receivingData.unitsOfMeasure)
                val avgAvlue =
                    it.palletAverage?.toDouble()?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                tvTarWeight.text = avgAvlue?.formatThreeDigits().plus(" ").plus(receivingData.unitsOfMeasure)
                tvNetWeight.text =
                    it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                        .plus(receivingData.unitsOfMeasure)
                ivEdit.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(com.olam.warehouse.login.R.menu.transaction_menu, popupMenu.menu)
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible = false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible = false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible = false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible = true
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible = true
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        when (item.itemId) {
                            com.olam.warehouse.login.R.id.action_edit1 -> {
                                it.netWeight = it.grossWeight.toDouble().minus(avgAvlue).formatThreeDigits()
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
        binding.clNet.tvGrossWeightValue.text = grossWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        binding.clNet.tvTareWeightValue.text = tareWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        binding.clNet.tvNetWeightValue.text = netWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        receivingData.grossWeight = grossWeight.formatThreeDigits()
        receivingData.tareWeight = tareWeight.formatThreeDigits()
        receivingData.netWeight = netWeight.formatThreeDigits()
    }

    private fun updateMandatory() {
        tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        tvReceivingLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_location)) { mandatoryStars() } }
        tvSelectType.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_type)) { mandatoryStars() } }
        tvSelectPOLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_po)) { mandatoryStars() } }
    }

    private fun moveToSummary() {
        if (!receivingData.purchaseDocNum.isNullOrEmpty() && receivingData.netWeight.toDouble() > purchaseOrderQuantity.toDouble()) {
            showSnack(getString(R.string.net_weight_error))
        } else {
            receivingData.wsGate = WS01
            receivingData.weighBridgeType = PROCURE
            mReceiving.clear()
            bagCountTotal = 0
            bagList.forEachIndexed { index, it ->
                val receiving = receivingData.copy()
                receiving.bagType = it.bagType
                receiving.bagCount = it.bagCount
                receiving.bagWeight = it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!.formatThreeDigits()
                receiving.netWeight = it.netWeight
                receiving.grossWeight = it.grossWeight
                receiving.item = index.inc().toString()
                mReceiving.add(receiving)
                bagCountTotal = bagCountTotal.plus(it.bagCount.toInt())
            }
            receivingData.bagCount = bagCountTotal.toString()
            //prepareSuccessData(receivingData.weighBridgeId, false)
            callBack?.replaceFragment(OFFLOADING_SUMMARY_FRAG, receivingData, mReceiving, bagList)
        }
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
}
