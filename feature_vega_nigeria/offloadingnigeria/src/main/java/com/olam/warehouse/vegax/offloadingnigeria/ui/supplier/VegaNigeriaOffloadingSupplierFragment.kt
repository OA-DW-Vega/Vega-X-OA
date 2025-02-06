package com.olam.warehouse.vegax.offloadingnigeria.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaTrackTraceFragment
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrynigeria.utils.PRODUCT
import com.olam.warehouse.vegax.gateentrynigeria.utils.isNGCashewEnabled
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.databinding.FragmentVegaNigeriaOffloadingSupplierBinding
import com.olam.warehouse.vegax.offloadingnigeria.databinding.ItemVegaNigeriaOffloadingSupplierBagBinding
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 19/6/2020.
 */
class VegaNigeriaOffloadingSupplierFragment : BaseFragment(), VegaSingleSelectCommonListener ,  VegaCocoaAddPalletFragment.CallBackPallet{
    private var receivingData = VegaReceiving()
    private var offloadingData = VegaOffloadingTrucks()
    private var supplierList = mutableListOf<VegaVendor>()
    private var storageLocationList = mutableListOf<VegaStorageLocation>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private lateinit var productList:List<VegaMaterial>

    private lateinit var binding: FragmentVegaNigeriaOffloadingSupplierBinding
    private var callBack: CallBack? = null
    private val vm: VegaNigeriaOffloadingViewModel by viewModel()

    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var purchaseOrderQuantity: String = "0"
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var bagCountTotal = 0
    private var procureType = ""
    var ttProcurementType = ""
    var isTTComplaint = false
    var ttIndirectSourceLotDetails = TrackTraceSourceLotDetails()
    var ttFarmerlessTransactionDetails = TrackTraceTransactionIdDetails()
    var ttDirectFarmerDataList = java.util.ArrayList<TrackTraceFarmerModel>()
    var isVendorStatusFlag=false
    var ttMaterialCode= ""
    var totalTareWeight=0.0
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
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        )
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving,
            mReceiving: MutableList<VegaReceiving>,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
            palletCount:String,
            palletGrossweight:String,
            palletAvergaeCount:String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nigeria_offloading_supplier

    companion object {


        fun newInstance(offloadingData: VegaOffloadingTrucks, procurementType: String?) = VegaNigeriaOffloadingSupplierFragment().putArgs {
            putParcelable(OFFLOADING_TRUCK_DATA, offloadingData)
            putString(Constants.PROCUREMENT_TYPE, procurementType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaOffloadingSupplierBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingnigeria/ui/VegaNigeriaOffloadingSupplierFragment")
            .title("Ecuador Offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        updateMandatory()
        // receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        ttProcurementType = arguments?.getString(Constants.PROCUREMENT_TYPE) ?: ""
        offloadingData = arguments?.getParcelable(OFFLOADING_TRUCK_DATA)!!
        if(isNGCashewEnabled()) offloadingData.unitsOfMeasure = "KG" else offloadingData.unitsOfMeasure = "MT"
//        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(OFFLOADING_POST_BAG_DATA)!!
//        if (receivingData.tmpWbId.isEmpty()) {
//            receivingData.tmpWbId = getTmpId()
//        }
        vm.getFeatureMaster(Constants.TRACK_TRACE)
        vm.featureMaster.observe(viewLifecycleOwner, Observer { updateFeatureUI(it) })

        if (offloadingData.materialCode?.length == 18)
            ttMaterialCode = offloadingData.materialCode?.takeLast(12).toString()
        else ttMaterialCode = offloadingData.materialCode.toString()

        if(offloadingData.weighMethod?.equals("WB") == true){
            hideForWbFlow()
             binding.btProceed.isEnabled = true
            binding.btProceed.setBackgroundColor(
                getColor(
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        }



        if (!isNGCashewEnabled()&& ttProcurementType.isNotEmpty()) {
            loadTTSupplierTypeFragment()
        }


        if (ttMaterialCode.isNotEmpty()) vm.getProductByName(ttMaterialCode.toString()) else vm.getProducts()

        vm.productName.observe(viewLifecycleOwner, Observer {
            productList = it
            if(isNGCashewEnabled())setProudctValues()
        })
        vm.product.observe(viewLifecycleOwner, Observer {
            productList = it
            if(isNGCashewEnabled())setProudctValues()
        })

        binding.clSupplierInfo.tvProduct.setOnClickListener {
            if(ttProcurementType.equals(Constants.IN_DIRECT) && isSourceLotMandatory && ttIndirectSourceLotDetails.sourceLotId?.isEmpty()==true){
                showSnack(getString(R.string.source_lot_id_should_not_be_empty) )
            }else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && isTransIdMandatory && ttFarmerlessTransactionDetails.dwTransactionId?.isEmpty()==true){
                showSnack(getString(R.string.trans_id_should_not_be_empty) )
            }else {
                showSingleSelectDialog(getString(R.string.select_product), PRODUCT)
            }
        }

        binding.clSupplierInfo.tvSupplier.setOnClickListener {
            if(ttProcurementType.equals(Constants.IN_DIRECT) && isSourceLotMandatory && ttIndirectSourceLotDetails.sourceLotId?.isEmpty()==true){
                showSnack(getString(R.string.source_lot_id_should_not_be_empty) )
            }else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && isTransIdMandatory && ttFarmerlessTransactionDetails.dwTransactionId?.isEmpty()==true){
                showSnack(getString(R.string.trans_id_should_not_be_empty) )
            }else {
                showSingleSelectDialog(getString(R.string.select_supplier), SUPPLIER)
            }
        }

        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
            if(isNGCashewEnabled())setSupplierValues()
        })
        vm.getSuppliers()
        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.getBagItems(
            offloadingData.materialCode,
            offloadingData.supplierCode.toString(),
            procureType,
            "",
            offloadingData.wbTempId
        )
        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        vm.poListLocal.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUILocal(it) })

        vm.offloadingItemCountLocal.observe(viewLifecycleOwner, Observer {
            if (it != null && it.size > 0) {
//                binding.llOffline.visible()
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOffloadingWithLineItemCount()

        binding.tvMaterial.text = offloadingData.materialName
        binding.clSupplierInfo.tvProcurementType.setOnClickListener { showProcurementTypeDialog() }
        //binding.clSupplierInfo.tvSelectPO.setOnClickListener { showPurchaseOrderDialog(purchaseOrderList) }
        binding.btAddWeight.setOnClickListener { validateInputs() }
        binding.btProceed.setOnClickListener { moveToSummary() }
        binding.llOffline.setOnClickListener { moveToOfflineSummary() }
        if (offloadingData.weighMethod.equals("WS"))
        updateTotalWeights()

        if (ttProcurementType.equals(Constants.DIRECT)){
            binding.clSupplierInfo.llSupplier.gone()
          }else {
              binding.clSupplierInfo.llSupplier.visible()
              binding.clSupplierInfo.tvProcHeader.gone()
              binding.clSupplierInfo.llSelectType.gone()
          }

        if (isNGCashewEnabled()) {
            displayFragment(VegaCocoaAddPalletFragment(), false)
            binding.clNetCash.clNet.visible()
            binding.clNet.clNet.gone()

            binding.tvTarWeightLbl.text= getString(R.string.tare_n_bag_pallet)

        }else{
            binding.clNetCash.clNet.gone()
            binding.clNet.clNet.visible()
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

    private fun loadTTSupplierTypeFragment() {
        val bundle = Bundle()
        bundle.putString(Constants.PROCUREMENT_TYPE, ttProcurementType)
        bundle.putParcelable(Constants.OFFLOADING, offloadingData)
        displayTTFragment(VegaTrackTraceFragment.newInstance(bundle), false)
    }

    private fun displayTTFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            "",
            allowStateLoss = true,
            containerViewId = R.id.flTTContainer,
            allowBackStack = flag
        )
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            "",
            allowStateLoss = true,
            containerViewId = R.id.flPallet,
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
//                receivingData.storageLocationCode = it[index].storageLocationCode
//                receivingData.storageLocationName = it[index].storageLocationName
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
                            offloadingData.materialCode,
                            offloadingData.supplierCode.toString(),
                            procureType,
                            "",
                            receivingData.tmpWbId
                        )
//                        vm.getBagItems(
////                            receivingData.materialCode,
////                            receivingData.supplierCode.toString(),
//                            procureType,
//                            "",
//                            receivingData.tmpWbId
//                        )
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
//        receivingData.purchaseDocNum = ""
//        receivingData.purchaseDocDesc = ""
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

//    private fun showPurchaseOrderDialog(list: List<VegaEcuadorPurchaseOrder>) {
//        if (receivingData.materialCode.isNullOrEmpty()) {
//            showSnack(getString(R.string.message_valid_product))
//        } else if (receivingData.supplierCode.isNullOrEmpty()) {
//            showSnack(getString(R.string.message_valid_supplier))
//        } else {
//            val purData = list.filter {
//                receivingData.materialCode?.let { it1 -> it.materialNumber.contains(it1) }!! &&
//                        receivingData.supplierCode?.let { it1 -> it.supplier.contains(it1) }!! && it.bsart.equals(
//                    PurchaseType.FIXEDPURCHASE.type
//                )
//            }
//            val purchaseOrders =
//                purData.map { data -> data.poId.plus(" - ").plus(data.menge).plus(receivingData.unitsOfMeasure) }
//            MaterialDialog(requireContext()).show {
//                title(R.string.select_purchase_order)
//                listItemsSingleChoice(items = purchaseOrders) { _, index, text ->
//                    binding.clSupplierInfo.tvSelectPO.text = text
//                    receivingData.purchaseDocNum = purData[index].poId
//                    receivingData.purchaseDocDesc = purData[index].ebelp
//                    purchaseOrderQuantity = purData[index].menge
//                    receivingData.purchaseDocQty = purchaseOrderQuantity
//                    vm.getBagItems(
//                        receivingData.materialCode,
//                        receivingData.supplierCode.toString(),
//                        procureType,
//                        receivingData.purchaseDocNum.toString(),
//                        receivingData.tmpWbId
//                    )
//                }
//                positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
//            }
//        }
//    }

    private fun moveBagAddWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val bundle = Bundle()
//        bundle.putString(MATERIAL_NAME, receivingData.materialName)
//        bundle.putString(UNITS_OF_MEASURE, receivingData.unitsOfMeasure)
        bundle.putString(MATERIAL_NAME, offloadingData.materialName)
        bundle.putString(UNITS_OF_MEASURE, offloadingData.unitsOfMeasure)
        bundle.putParcelable(BAG_MATERIAL, bagMaterial)
        callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT, bundle)
    }

    private fun updateBagItems(bagItems: List<VegaEcuadorOffloadingBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
            /*if (bagItems.size > 0) {
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
                //bundle.putInt(Constants.PALLET_ADDED, bagList.size)
                if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
                    (bagList.size == palletCount.toInt()) else binding.btProceed.isEnabled = bagList.size > 0
                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                    getColor(com.olam.warehouse.presentation.R.color.green)
                )
                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                updateTotalWeights()
            }*/
            if (bagList.size > 0) {
                binding.btProceed.isEnabled = true
                binding.btProceed.setBackgroundColor(
                    getColor(
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                )
            } else {
                if(offloadingData.weighMethod?.equals("WS") == true) {
                    binding.btProceed.isEnabled = false
                    binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                }

            }

            if(offloadingData.weighMethod.equals("WS")) {
                updateTotalWeights()
                setUpAdapter(bagList)
            }
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
//            bagMaterial.materialCode = receivingData.materialCode.toString()
//            bagMaterial.supplierCode = receivingData.supplierCode.toString()
//            bagMaterial.procureType = tvProcurementType.text.toString()
//            bagMaterial.purcheseOrderNo = receivingData.purchaseDocNum.toString()
            bagMaterial.materialCode = offloadingData.materialCode.toString()
            bagMaterial.supplierCode = offloadingData.supplierCode.toString()
            bagMaterial.procureType = binding.clSupplierInfo.tvProcurementType.text.toString()
            bagMaterial.purcheseOrderNo = offloadingData.purchaseDocNum.toString()
        } else {
            bagList.removeAt(pos)
        }
        bagList.add(bagMaterial)
        bagList.forEach { material ->
            material.message = getString(R.string.stored_locally)
            //material.tmpWbId = receivingData.tmpWbId
            material.tmpWbId = offloadingData.wbTempId
            vm.saveBagDetails(material)
        }

        /* if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
             (bagList.size == palletCount.toInt())
        else binding.btProceed.isEnabled = bagList.size > 0
         if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
             getColor(com.olam.warehouse.presentation.R.color.green)
         )
         else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))*/
        if(offloadingData.weighMethod?.equals("WS") == true)
        updateTotalWeights()
    }

    private fun setUpAdapter(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) {
        val bagList1 = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            val dat = bagList.sortedByDescending { it.id }
            bagList1.addAll(dat)
            binding.rvWeight.setUpAdapter(
                bagList1,
                R.layout.item_vega_nigeria_offloading_supplier_bag,
                ItemVegaNigeriaOffloadingSupplierBagBinding::inflate,
                { it, pos, bindItem ->
                    if (pos % 2 == 0) {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                    } else {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                    }
                    bindItem.tvSno.text = pos.plus(1).toString()
                    bindItem.tvBag.text = it.bagCount
//                tvGrossWeight.text =
//                    it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus(receivingData.unitsOfMeasure)
                    bindItem.tvGrossWeight.text =
                        it.grossWeight.toDouble().formatThreeDigits().plus(" ")
                            .plus(offloadingData.unitsOfMeasure)
                    var tareWeightBag=""
                    var avgAvlue=0.0
                    if(isNGCashewEnabled()){
                        tareWeightBag = it.tareWeight.toString()
                        avgAvlue =
                            palletAvg?.toDouble()
                                ?.plus(
                                    ((it.bagCount.toInt()
                                        .times(tareWeightBag.toDouble())).formatThreeDigits()).toDouble()
                                )!!


                    }else{
                         tareWeightBag = if ((it.tareWeight).equals("KG")) convertKgToMTNigeria(
                            (it.tareWeight).toString().trim()
                        ) else (it.tareWeight).toString().trim()
                         avgAvlue =
                            it.palletAverage?.toDouble()
                                ?.plus(
                                    ((it.bagCount.toInt()
                                        .times(tareWeightBag.toDouble())).formatThreeDigits()).toDouble()
                                )!!
                    }


                    //tvTarWeight.text = avgAvlue?.formatThreeDigits().plus(" ").plus(receivingData.unitsOfMeasure)
                    bindItem.tvTarWeight.text =
                        avgAvlue.formatThreeDigits().plus(" ").plus(offloadingData.unitsOfMeasure)
//                tvNetWeight.text =
//                    it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
//                        .plus(receivingData.unitsOfMeasure)
                    bindItem.tvNetWeight.text =
                        it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                            .plus(offloadingData.unitsOfMeasure)
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
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
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
        if(bagList.isNotEmpty()) {
            for (item in bagList) {
                grossWeight = grossWeight.plus(item.grossWeight.toDouble())
                var tareWeightBag = if ((item.tareWeight).equals("KG"))
                    convertKgToMTNigeria((item.tareWeight).toString().trim())
                else (item.tareWeight).toString().trim()

                tareWeight =
                    tareWeight.plus(
                        ((tareWeightBag.toDouble()
                            .times(item.bagCount.toDouble())).formatThreeDigits()).toDouble()
                    )
                        .plus(palletAvg.toDouble())
            }
            netWeight = grossWeight.minus(tareWeight)
//        binding.clNet.tvGrossWeightValue.text = grossWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
//        binding.clNet.tvTareWeightValue.text = tareWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
//        binding.clNet.tvNetWeightValue.text = netWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
            receivingData.grossWeight = grossWeight.formatThreeDigits()
            receivingData.tareWeight = tareWeight.formatThreeDigits()
            receivingData.netWeight = netWeight.formatThreeDigits()


            binding.clNet.tvGrossWeightValue.text =
                grossWeight.formatThreeDigits().plus(offloadingData.unitsOfMeasure)
            binding.clNet.tvTareWeightValue.text =
                tareWeight.formatThreeDigits().plus(offloadingData.unitsOfMeasure)
            binding.clNet.tvNetWeightValue.text =
                netWeight.formatThreeDigits().plus(offloadingData.unitsOfMeasure)

            if (isNGCashewEnabled()) {
                binding.clNetCash.tvGrossWeightValue.text =
                    grossWeight.formatThreeDigits().plus(offloadingData.unitsOfMeasure)

                binding.clNetCash.tvPalletWeightValue.text =
                    tareWeight.formatThreeDigits().plus(offloadingData.unitsOfMeasure)

                binding.clNetCash.tvNetWeightValue.text =
                    netWeight.formatThreeDigits().plus(offloadingData.unitsOfMeasure)
            }
            offloadingData.grossWeight = grossWeight.formatThreeDigits()
            // offloadingData.tareWeight = tareWeight.formatThreeDigits()
            offloadingData.netWeight = netWeight.formatThreeDigits()

        }

        //procureType = "Spot"
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
        }else if(!isVendorStatusFlag && ttDirectFarmerDataList.isEmpty() && ttProcurementType.equals(Constants.DIRECT)){
            showSnack(getString(R.string.pls_select_farmer))
        }else if(binding.clSupplierInfo.tvProduct.text.isEmpty()){
            showSnack(getString(R.string.pls_select_product))
        } else {

            receivingData.batchNumber= offloadingData.batchNumber
            receivingData.wsGate =if(offloadingData.weighMethod.equals("WB"))WB01 else WS01
            receivingData.weighMethod= offloadingData.weighMethod
            receivingData.weighBridgeType = PROCURE
            receivingData.weighBridgeId = offloadingData.weighBridgeId
            receivingData.vehicleNumber = offloadingData.vehicleNumber
            receivingData.supplierName = offloadingData.supplierName
            receivingData.supplierCode = offloadingData.supplierCode
           // receivingData.materialCode = offloadingData.materialCode
            //receivingData.materialName = offloadingData.materialName
            receivingData.storageLocationCode = offloadingData.storageLocationCode
            receivingData.unitsOfMeasure = offloadingData.unitsOfMeasure.toString()
            receivingData.item = offloadingData.item.toString()
            receivingData.plantId = offloadingData.plant.toString()
            receivingData.challan = offloadingData.challan.toString()
            receivingData.procurementType = offloadingData.procurementType.toString()
            receivingData.grnModel = offloadingData.grnModel.toString()
            receivingData.grntNumber = offloadingData.grntNumber.toString()
            mReceiving.clear()
            bagCountTotal = 0
            if(offloadingData.weighMethod.equals("WS")) {
                bagList.forEachIndexed { index, it ->
                    val receiving = receivingData.copy()
                    receiving.bagType = it.bagType
                    receiving.bagCount = it.bagCount
                    var tareWeightBag = if ((it.tareWeight).equals("KG")) convertKgToMTNigeria(
                        (it.tareWeight).toString().trim()
                    ) else (it.tareWeight).toString().trim()
                    receiving.bagWeight =
                        (tareWeightBag.toDouble().times(it.bagCount.toDouble())).formatThreeDigits()
                    receiving.netWeight = it.netWeight
                    receiving.grossWeight = it.grossWeight
                    receiving.batchNumber = receivingData.batchNumber
                    //receiving.item = index.inc().toString()
                    var code = binding.clSupplierInfo.tvProduct.text.split("-")[0]
                    if (code.length == 12) {
                        code = "000000".plus(code)
                    }
                    receiving.materialCode = code
                    receiving.materialName = binding.clSupplierInfo.tvProduct.text.split("-")[1]

                    mReceiving.add(receiving)
                    bagCountTotal = bagCountTotal.plus(it.bagCount.toInt())
                }
            }
            if(offloadingData.weighMethod=="WB"){
                val receiving = receivingData.copy()
                receiving.batchNumber= receivingData.batchNumber
                //receiving.item = index.inc().toString()
                var code= binding.clSupplierInfo.tvProduct.text.split("-")[0]
                if(code.length==12){ code="000000".plus(code)}
                receiving.materialCode= code
                receiving.materialName=binding.clSupplierInfo.tvProduct.text.split("-")[1]
//                receiving.materialName=binding.clSupplierInfo.tvProduct.text.split("&&")[1]
                receiving.grossWeight= offloadingData.grossWeight
                receiving.netWeight= offloadingData.netWeight.toString()
                receiving.bagCount= "0"
                receiving.tareWeight= offloadingData.tareWeight

                mReceiving.add(receiving)


            }
            receivingData.bagCount = bagCountTotal.toString()
            //prepareSuccessData(receivingData.weighBridgeId, false)
            /*Track & Trace*/
            if(ttProcurementType.equals(Constants.DIRECT)){
                receivingData.ttFarmerList = getTTFarmerDataList()
                receivingData.eudrStatus = isTTComplaint

            } else if(ttProcurementType.equals(Constants.IN_DIRECT)) {
                receivingData.sourceLotId = ttIndirectSourceLotDetails.sourceLotId.toString()
                receivingData.eudrStatus = ttIndirectSourceLotDetails.isEudrComplaintFlag ?: false
            } else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)){
                receivingData.farmerLessTransactionId = ttFarmerlessTransactionDetails.dwTransactionId.toString()
                receivingData.eudrStatus = ttFarmerlessTransactionDetails?.compliantFlag ?: false
            }

            /*Code Related To CropLimit*/
            if (PreferenceHelper.get(Constants.CROP_LIMIT, "")
                    .isNotEmpty() && !isVendorStatusFlag && ttDirectFarmerDataList.isNotEmpty()) {
                val limit = ttDirectFarmerDataList.get(0).cropLimit
                val uom = ttDirectFarmerDataList.get(0).uomFromOfis
                var cropLimitPercentage = ""
                productList.find { offloadingData.materialCode?.contains(it.materialCode) == true }.apply {
                    cropLimitPercentage = this?.cropLimit.toString()
                }
                var limitValue = convertWeights(
                    (if (limit.toString()
                            .isEmpty()
                    ) 0.0 else limit?.times(cropLimitPercentage.toDouble())?.div(100)).toString(),
                    uom,
                    offloadingData.unitsOfMeasure.toString()
                )
                if (offloadingData.netWeight?.toDouble()!! > limitValue.toDouble() && limitValue.toDouble() > 0) {
                    showDialog(
                        getString(
                            R.string.crop_limit_alert,
                            limitValue.toString().plus(offloadingData.unitsOfMeasure)
                        )
                    )
                }

                receivingData.capacityWeight = limitValue
                receivingData.leftOverWeight =
                    limitValue.toDouble().minus(offloadingData.netWeight!!.toDouble()).coerceAtLeast(0.0).toString()

            }


            if(ttMaterialCode.isNotEmpty() && !ttMaterialCode.equals(binding.clSupplierInfo.tvProduct.text.toString().split("-")[0].takeLast(12))){

                MaterialDialog(requireContext()).show {
                    message(text = getString(R.string.change_from_material))
                    UIUtils.getMetirialCustomView(
                        this,
                        getString(com.olam.warehouse.presentation.R.string.yes),
                        getString(com.olam.warehouse.presentation.R.string.no),
                        {
                            dismiss()
                            //receivingData.challan=""
                            mReceiving[0].batchNumber=""
                            receivingData.batchNumber=""
                            receivingData.isDelete = true
                            callBack?.replaceFragment(OFFLOADING_SUMMARY_FRAG, receivingData, mReceiving, bagList)
                        },
                        { dismiss()
                        })

                }

            }else{
                if(isNGCashewEnabled()){
                    val bagListItem  = ArrayList(bagList.distinctBy { it.bagType }.map { it.bagType })
                    if(palletCount.toInt()>0){ bagListItem.add("Pallet") }

                    if(bagListItem.size>3){
                     showSnack(getString(R.string.please_add_less_three))
                    }else if(palletCount.toInt()>0){
                        if(bagList.size.toString() == palletCount){
                            callBack?.replaceFragment(OFFLOADING_SUMMARY_FRAG, receivingData, mReceiving, bagList,palletCount,palletWeight,palletAvg)
                        }else{
                            showSnack(getString(R.string.pallet_count_bag_list))
                        }
                    }else{
                        callBack?.replaceFragment(OFFLOADING_SUMMARY_FRAG, receivingData, mReceiving, bagList,palletCount,palletWeight,palletAvg)
                    }
                }else{
                    callBack?.replaceFragment(OFFLOADING_SUMMARY_FRAG, receivingData, mReceiving, bagList)
                }
            }
        }
    }


    fun showConfirmDialog(){
        MaterialDialog(requireContext()).show {
            message(text = getString(R.string.change_from_material))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.yes),
                getString(com.olam.warehouse.presentation.R.string.no),
                {
                    dismiss()
                    //receivingData.challan=""
                    mReceiving[0].batchNumber=""
                    receivingData.batchNumber=""
                    receivingData.isDelete = true
                    callBack?.replaceFragment(OFFLOADING_SUMMARY_FRAG, receivingData, mReceiving, bagList)
                },
                { dismiss()
                })

        }
    }

    private fun validateInputs() {
        when {
//            receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_product))
//            receivingData.supplierName.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_supplier))
//            binding.clSupplierInfo.tvProcurementType.text.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_procurement_type))
//            binding.clSupplierInfo.llSelectPO.isVisible && binding.clSupplierInfo.tvSelectPO.text.isNullOrEmpty() -> showSnack(
//                getString(R.string.message_valid_po_type)
//            )
//            bagList.sizetv == 3 -> showSnack(getString(R.string.restrict_add_bag))
            else -> moveBagAddWeight(VegaEcuadorOffloadingBagMaterial())
        }
    }

    private fun setSupplierValues(){
//        binding.clSupplierInfo.tvSupplier.isEnabled=false
//        binding.clSupplierInfo.tvSupplier.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)

        val trimmedSupplierCode = offloadingData.supplierCode!!.substring(3)

        val result = supplierList.find { it.vendorCode == trimmedSupplierCode }
            ?.let { "${it.vendorCode}-${it.vendorName}" } ?: "Not Found"
        binding.clSupplierInfo.tvSupplier.text=result
        supplierList.forEach { vendor ->
            if (vendor.vendorCode == binding.clSupplierInfo.tvSupplier.text.toString()
                    .split(" - ")[0]
            ) {
                clearPurchaseOrder()
            }
        }

    }
    private fun setProudctValues(){
//        binding.clSupplierInfo.tvProduct.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
//        binding.clSupplierInfo.tvProduct.isEnabled=false
        val result = productList.find { it.materialCode == offloadingData.materialCode!!.drop(6) }
            ?.let { "${it.materialCode}-${it.materialName}" } ?: "Not Found"
        binding.clSupplierInfo.tvProduct.text=result
        var code= binding.clSupplierInfo.tvProduct.text.split("-")[0]
        if(code.length==12){ code="000000".plus(code)}
        receivingData.materialCode= code
        receivingData.materialName= result.split("-")[1]

        productList.forEach { material ->
            if (material.materialName == binding.clSupplierInfo.tvProduct.text.toString().split("-")[1].trim()) {
                clearPurchaseOrder()
            }
        }

    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when(currentFlag){
            SUPPLIER->{
                binding.clSupplierInfo.tvSupplier.text=data
                supplierList.forEach { vendor ->
                    if (vendor.vendorCode == binding.clSupplierInfo.tvSupplier.text.toString()
                            .split(" - ")[0]
                    ) {
                        clearPurchaseOrder()
                    }
                }

            } else->{
            binding.clSupplierInfo.tvProduct.text=data
            var code= binding.clSupplierInfo.tvProduct.text.split("-")[0]
            if(code.length==12){ code="000000".plus(code)}
            receivingData.materialCode= code
            receivingData.materialName= data.split("-")[1]

            productList.forEach { material ->
                if (material.materialName == binding.clSupplierInfo.tvProduct.text.toString().split("-")[1].trim()) {
                    clearPurchaseOrder()
                }
            }

        }
        }
    }

    private fun showSingleSelectDialog(title: String, currentFlag: String) {
        var list = ArrayList<String>()
        when (currentFlag) {
            SUPPLIER->{
                list = supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) } as ArrayList<String>
            }
            else->{
                    if (isTTComplaint) {
                        list = productList.filter { it.complainceFlag == Constants.COMPLAINT }
                            .map { data -> data.materialCode.plus("-").plus(data.materialName) } as ArrayList<String>
                    } else if (!isTTComplaint) {
                        list = productList.filter { it.complainceFlag == Constants.NON_COMPLAINT }
                            .map { data -> data.materialCode.plus("-").plus(data.materialName).plus("-").plus(data.cropLimit) } as ArrayList<String>
                    }
                if(list.isEmpty()){
                    list = productList.map { data -> data.materialCode.plus("-").plus(data.materialName).plus("-").plus(data.cropLimit) } as ArrayList<String>
                }
            }
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFlag,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


    /*Track & Trace*/
    fun isComplaint(status:Int){
        if(status == 0){
            isTTComplaint = false
        } else {
            isTTComplaint = true
        }
    }

    private fun resetMaterial(){
        binding.clSupplierInfo.tvSupplier.text = ""
        binding.clSupplierInfo.tvProduct.text = ""
        receivingData.materialCode = ""
        receivingData.materialName = ""
    }


    /*Track & Trace*/
    fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails){
        resetMaterial()
        isTTComplaint = sourceLotDetails?.isEudrComplaintFlag?:false
        this.ttIndirectSourceLotDetails = sourceLotDetails
        preBindTTMaterialCondition()
        if(sourceLotDetails.vendorCode?.isNotEmpty() == true){
            var supplierList = mutableListOf<VegaVendor>()
            var checkSupplier = supplierList.filter { it.vendorCode.equals(sourceLotDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog(getString(R.string.vendor_not_available))
            } else {
                var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                binding.clSupplierInfo.tvSupplier.text = data
                receivingData.supplierCode = data.split("-")[0].trim()
                receivingData.supplierName = data.split("-")[1].trim()
//                clearPurchaseOrder()
            }
        } else {
            showOkDialog(getString(R.string.vendor_not_available))
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

    fun updateFarmerListDetails(farmerList: java.util.ArrayList<TrackTraceFarmerModel>){
        binding.clSupplierInfo.tvProduct.text=""
        ttDirectFarmerDataList.clear()
        ttDirectFarmerDataList.addAll(farmerList)
        if(ttDirectFarmerDataList.isNotEmpty()) {

            if(ttDirectFarmerDataList.get(0).supplier?.isNotEmpty() == true) {
                receivingData.supplierCode = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(0)?.trim()
                receivingData.supplierName = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(1)?.trim()
            }else{
                receivingData.supplierCode=""
                receivingData.supplierName=""
            }
            var result = ttDirectFarmerDataList.any { it.isComplaint == 0 }

            if(result){
                isTTComplaint = false
            }else{
                isTTComplaint = true
            }
        } else {
            isTTComplaint = false
        }
    }

    fun preBindTTMaterialCondition(){
        binding.clSupplierInfo.tvProduct.text=""
    }


    fun isVendor(isVendor: Boolean) {
        isVendorStatusFlag= isVendor
       // binding.clSupplierInfo.tvProduct.text=""
    }

    /* private fun validateData(){
         var msg=""
         if(receivingData.supplierCode?.isEmpty() == true){
           msg= "Kindly select supplier"
         }else
        if(receivingData.supplierCode?.isNotEmpty() == true){
          isAllUpdated = true
        }else if(ttDirectFarmerDataList.isNotEmpty()){
            isAllUpdated=true
        }
    }*/

    private fun getTTFarmerDataList(): java.util.ArrayList<TrackTraceFarmerModel> {
        if(ttDirectFarmerDataList.isNotEmpty() && ttDirectFarmerDataList.size > 0) {
            if (AppUtils.isOnline()) {
                return ttDirectFarmerDataList
            } else{
                ttDirectFarmerDataList.forEach {
                    it.tmpWbId = receivingData.tmpWbId
                }
                return ttDirectFarmerDataList
            }
        }
        return ttDirectFarmerDataList
    }
    private fun hideForWbFlow(){
        binding.tvWeightSummary.gone()
        binding.btAddWeight.gone()
        binding.ivAdd.gone()
        binding.clBagHead.gone()
        binding.rvWeight.gone()
        binding.tvNoWeight.gone()
        binding.clNet.clNet.gone()
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        this.palletWeight=palletWeight
        this.palletCount=noOfPallet
        if(!palletWeight.isNullOrEmpty() && !noOfPallet.isNullOrEmpty()) {
            this.palletAvg = palletWeight.toDouble().div(noOfPallet.toDouble()).toString()
        }
        binding.clNetCash.tvPalletWeightValue.text= palletWeight.plus(offloadingData.unitsOfMeasure)

        binding.rvWeight.adapter?.notifyDataSetChanged()
        updateTotalWeights()
    }


}
