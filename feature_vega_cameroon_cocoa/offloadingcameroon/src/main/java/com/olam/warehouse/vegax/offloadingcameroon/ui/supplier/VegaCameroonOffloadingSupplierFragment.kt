package com.olam.warehouse.vegax.offloadingcameroon.ui.supplier

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaTrackTraceFragment
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.entity.VegaCameroonOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.PurchaseType
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonMtntDetails
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentVegaCameroonOffloadingSupplierBinding
import com.olam.warehouse.vegax.offloadingcameroon.databinding.ItemVegaCameroonOffloadingSupplierBagBinding
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingCustomSingleSelectDialog
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingSingleSelectListener
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 19/6/2020.
 */
class VegaCameroonOffloadingSupplierFragment : BaseFragment(),
    VegaCameroonOffloadingSingleSelectListener {
    private var receivingData = VegaReceiving()
    private var savedBagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var storageLocationList = mutableListOf<VegaStorageLocation>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var purData = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var customstorageLocationList = mutableListOf<VegaCustomStLocation>()
    private var mReceiving = mutableListOf<VegaReceiving>()

    private lateinit var binding: FragmentVegaCameroonOffloadingSupplierBinding
    private var callBack: CallBack? = null
    private val vm: VegaCameroonOffloadingViewModel by viewModel()

    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var batchNoBagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var savedBatchIdWeighList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var purchaseOrderQuantity: String = "0"
    private var selectedBatchId: String = ""
    private var batchNumber: String = ""
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var bagCountTotal = 0
    private var procureType = ""
    private var truckData = VegaOffloadingTrucks()
    private var wbList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private var selectedPo: String = ""
    private var driverName: String = ""
    private var vehicleNumber: String = ""
    private var tempBagCount: String = ""
    private var receivingPlant: String = ""
    private var otLotNumber: String = ""
    private var customDialog: VegaCameroonOffloadingCustomSingleSelectDialog? = null
    private var isToggleEnabled = false
    private var onLocalSave = false
    private var isDeleteCameroon = false
    private var isDeleteEcuador = false
    private var batchIDDuplication = false
    private var isAutoBatch = false
    private var selectedPlantId = ""
    private var plantDetails = Plant()
    private var avgValueWithPallet: Double? = 0.0
    private var approximateWt:String=""
    private var mtntNumber:String=""
    private var productLists = ArrayList<VegaMaterial>()
    private var supplierList = mutableListOf<VegaVendor>()


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
            receivingPlant: String?,
            otLotNumber: String?,
            plantDetails: Plant
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_cameroon_offloading_supplier

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
            truckData: VegaOffloadingTrucks,
            selectedPlant: String,
            plantDetails: Plant,
            procurementType: String
        ) = VegaCameroonOffloadingSupplierFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
            putParcelable(OFFLOADING_TRUCK_DATA, truckData)
            putString(OFFLOADING_PLANT_SELECTED, selectedPlant)
            putParcelable(OFFLOADING_PLANT_DETAILS, plantDetails)
            putString(Constants.PROCUREMENT_TYPE, procurementType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonOffloadingSupplierBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcameroon/ui/supplier/VegaCameroonOffloadingSupplierFragment")
            .title("Vega_Cameroon/Offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        updateMandatory()
        updatePalletBlock(null)
        vm.getConfigItems(UserRoles.APPROVE.role)
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        ttProcurementType = arguments?.getString(Constants.PROCUREMENT_TYPE)?:""
        if(ttProcurementType.isNotEmpty()) {
            loadTTSupplierTypeFragment()
        }
        receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        truckData = arguments?.getParcelable(OFFLOADING_TRUCK_DATA)!!
        selectedPlantId = arguments?.getString(OFFLOADING_PLANT_SELECTED)!!
        plantDetails = arguments?.getParcelable(OFFLOADING_PLANT_DETAILS)!!
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(
            OFFLOADING_POST_BAG_DATA
        )!!
        if (receivingData.tmpWbId.isEmpty()) {
            receivingData.tmpWbId = getTmpId()
        }

        vm.getFeatureMaster()
        vm.featureMaster.observe(viewLifecycleOwner, Observer {
            if (it != null && it.size > 0) {
                updateFeatureUI(it)
            }
        })
        vm.product.observe(viewLifecycleOwner, Observer {
            productLists = it as ArrayList<VegaMaterial>
        })
        vm.getProducts()

        vm.deleteBagDetails()
        binding.tvTruckValue.text = truckData.weighBridgeId
//        binding.tvDestValue.text = truckData.supplierName
//        binding.tvMaterialValue.text = truckData.materialName
//        binding.tvTruckNoValue.text = truckData.vehicleNumber
//        binding.tvMtntNoValue.text = truckData.delivery
//        binding.tvMtntWeightValue.text = truckData.netWeight
//        binding.tvRecLocationValue.text = truckData.storageLocationCode
//        binding.tvPlantValue.text = truckData.plant
        receivingData.materialCode = truckData.materialCode
        receivingData.materialName = truckData.materialName
        receivingData.unitsOfMeasure = truckData.unitsOfMeasure.toString()
        receivingData.supplierCode = truckData.supplierCode
        receivingData.supplierName = truckData.supplierName
        receivingData.storageLocationCode = truckData.storageLocationCode
        receivingData.receivingWH = truckData.storageLocationCode
        receivingData.weighBridgeId = truckData.weighBridgeId
        receivingData.weighBridgeType = truckData.weighBridgeType
        receivingData.contactNumber = truckData.driverNumber
        receivingData.challan = truckData.challan
        receivingData.truckNo = truckData.vehicleNumber
        receivingData.vehicleNumber = truckData.vehicleNumber
        receivingData.plantId = truckData.plant

        binding.clSupplierInfo.tvSelectPO.setOnClickListener { showPurchaseOrderDialog(purchaseOrderList) }
        binding.clSupplierInfo.tvProduct.setOnClickListener {
            if(ttProcurementType.equals(Constants.IN_DIRECT) && isSourceLotMandatory && ttIndirectSourceLotDetails.sourceLotId?.isEmpty()==true){
                showSnack(getString(R.string.source_lot_id_should_not_be_empty) )
            }else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && isTransIdMandatory && ttFarmerlessTransactionDetails.dwTransactionId?.isEmpty()==true){
                showSnack(getString(R.string.trans_id_should_not_be_empty) )
            } else {
                showProductDialog()
            }
        }
        binding.clSupplierInfo.tvSupplier.setOnClickListener {
            if(ttProcurementType.equals(Constants.IN_DIRECT) && isSourceLotMandatory && ttIndirectSourceLotDetails.sourceLotId?.isEmpty()==true){
                showSnack(getString(R.string.source_lot_id_should_not_be_empty) )
            }else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && isTransIdMandatory && ttFarmerlessTransactionDetails.dwTransactionId?.isEmpty()==true){
                showSnack(getString(R.string.trans_id_should_not_be_empty) )
            } else {
                showSupplierDialog()
            }
        }

        vm.mtntWeight.observe(viewLifecycleOwner, Observer { updateMtntDetails(it) })
        vm.getMtntWeightDetails(receivingData.weighBridgeId, selectedPlantId)


        binding.clSupplierInfo.otLotValue.onChange {
            otLotNumber = it
        }


        vm.bagItems.observe(viewLifecycleOwner, Observer {
            if(!isDeleteEcuador) {
                updateBagItems(it)}
        })

        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        vm.poListLocal.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUILocal(it) })
        vm.getSuppliers()
        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
        })
        binding.clSupplierInfo.llSelectPO.visible()
        if (isOnline()) vm.getPOList(selectedPlantId) else vm.getPOListLocal()
        procureType = "Fixed"

        vm.offloadingItemCountLocal.observe(viewLifecycleOwner, Observer {
            if (it != null && it.size > 0) {
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOffloadingWithLineItemCount()

        binding.clSupplierInfo.batchNo.onChange {
            batchNumber = it
        }
        binding.btAddWeight.setOnClickListener { validateInputs() }
        binding.btProceed.setOnClickListener {
            //val bundle = Bundle()
            //callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT_SUPPLIER,bundle )
            moveToSummary()
        }
        binding.btSave.setOnClickListener {
            showMaterialDialog()

        }



        vm.getSavedReceivingBagItems(receivingData.weighBridgeId)

     //Saved Data from VegaCameroonOffloadingBagMaterials Table
        vm.receivingBag.observe(viewLifecycleOwner, Observer { item ->
            if(item!=null) {
                receivingData = item.receiving
                if (receivingData.palletCount?.isNotEmpty() == true && receivingData.palletWeight?.isNotEmpty() == true) {
                    palletWeight = receivingData.palletWeight.toString()
                    palletCount = receivingData.palletCount.toString()
                    val bundle = Bundle()
                    bundle.putString(Constants.PALLET_WEIGHT, palletWeight)
                    bundle.putString(Constants.PALLET_COUNT, palletCount)
                    bundle.putBoolean(Constants.PALLET_EDIT, true)
                    updatePalletBlock(bundle)
                }
                if (!isDeleteCameroon) {

                    var filteredList = item.lineItems.filter { it.batchNumber != "" }
                    savedBagList = filteredList as ArrayList<VegaEcuadorOffloadingBagMaterial>
                    updateSavedBatchIdList(savedBagList)
                    if (isToggleEnabled == false && onLocalSave == false) {

                        var details =
                            savedBagList.filter { it.wbId.equals(receivingData.weighBridgeId) }
                        vm.saveSelectedBatchBagDetails(details) // No Observer for this
                        savedBagList.forEach { item ->
                            if (item.wbId.equals(truckData.weighBridgeId)) {
                                wbList.add(item)
                                updateBagItems(wbList)
                                selectedPo = item.purcheseOrderNo
                                receivingData.purchaseDocNum = item.purcheseOrderNo.split(" - ")[0]
                                receivingData.purchaseDocQty =
                                    item.purcheseOrderNo.split(" - ")[1].replace("KG", "")
                                purchaseOrderQuantity = receivingData.purchaseDocQty.toString()
                                receivingData.purchaseDocDesc = item.purchaseDocDesc
                                binding.clSupplierInfo.batchNo.setText(item.batchNumber)
                                receivingPlant = item.receivingPlant.toString()
                                binding.clSupplierInfo.tvReceivingLocation.text = item.receivingPlant
                                binding.clSupplierInfo.otLotValue.setText(item.otLotNumber)
                                otLotNumber = item.otLotNumber.toString()
                                receivingData.batchNumber = item.batchNumber // recheck this code
                            }
                        }
                    }
                }
            }
            //Observer is present for VegaEcuadorOffloadingBagMaterials
        })
    }
    private fun loadTTSupplierTypeFragment(){
        val bundle = Bundle()
        bundle.putString(
            Constants.PROCUREMENT_TYPE, ttProcurementType)
        displayTTFragment(VegaTrackTraceFragment.newInstance(bundle), false)
        if(ttProcurementType.equals(Constants.DIRECT)) {
            binding.clSupplierInfo.llSupplier.gone()
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

    fun showMaterialDialog(){
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_save)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    validateBatchNumberDuplication()
                    when(batchIDDuplication) {
                        true -> showSnack(getString(R.string.batch_already_exist))
                        false -> saveItemsLocally()
                    }
                },
                { dismiss() })
        }

    }


    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        val receivingPlantList = configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }
        var charValue = mutableListOf<String>()
        if (receivingPlantList?.isNotEmpty() == true) {
            receivingPlantList.forEach {
                var list = it.value
                charValue = list?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf<String>()
            }
        }

        binding.clSupplierInfo.tvReceivingLocation.setOnClickListener { showReceivingLocationDialog(charValue) }


    }

    private fun showReceivingLocationDialog(charValue: MutableList<String>) {
        customDialog =
            VegaCameroonOffloadingCustomSingleSelectDialog(
                getString(R.string.select_receiving_plant),
                false, false, false,
                charValue as ArrayList<String>,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun validateBatchNumberDuplication() {
        batchIDDuplication = false
        var savedWbidList = savedBagList.map { it.wbId }
        var savedBatchIdList = savedBagList.map { it.batchNumber }

        savedBagList.forEach {
            if ((it.wbId == receivingData.weighBridgeId && savedWbidList.contains(receivingData.weighBridgeId) && savedBatchIdList.contains(
                    batchNumber
                )
                        && it.batchNumber != batchNumber)
                || (!savedWbidList.contains(receivingData.weighBridgeId) && savedBatchIdList.contains(
                    batchNumber
                ))
            ) {
                batchIDDuplication = true
            }

        }
    }

    private fun checkBatchIdExist(): Boolean {
        var batchExist = false
        savedBagList.forEach {
            if(it.batchNumber == receivingData.batchNumber)
                batchExist = true
        }
        return batchExist
    }

    private fun updateMtntDetails(response: Resource<GenericReqAndResp<VegaCameroonMtntDetails>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let { item ->
//                                binding.tvMtntWeightValue.text = item.appoximateWeight?.replace(" ", "")
                                driverName = item.driverName.toString()
                                vehicleNumber = item.vehicleNumber.toString()
                                tempBagCount = item.tempBagCount.toString()
                                approximateWt=item.appoximateWeight?.replace(" ", "").toString()
                                mtntNumber=item.delivery.toString()

                                binding.clSupplierInfo.batchNo.isEnabled = true
                                isAutoBatch = false
                          /*      if(!item.autoBatchNumber.toString().isNullOrEmpty() )
                                {
                                    batchNumber = item.autoBatchNumber.toString()
                                    binding.clSupplierInfo.batchNo.isEnabled = false
                                    binding.clSupplierInfo.batchNo.setText(item.autoBatchNumber)
                                    isAutoBatch = true
                                }
                                else{
                                    binding.clSupplierInfo.batchNo.isEnabled = true
                                    isAutoBatch = false
                                }*/
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

    private fun updateSavedBatchIdList(savedBags: MutableList<VegaEcuadorOffloadingBagMaterial>) {
        savedBags.let {
            val batchId = arrayListOf<String>()
            batchId.add(getString(R.string.select_batch_no))
            val batchId1 = savedBags.map { it.batchNumber }.distinct()
            batchId1.forEach { batchId.add(it) }
            val stageAdapter =
                ArrayAdapter(requireContext(), R.layout.item_vega_cameroon_offloading_batchno_grade, batchId)
            stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            binding.spSavedBatchId.adapter = stageAdapter
            binding.spSavedBatchId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    if(position>0){
                        selectedBatchId = batchId[position]
                        handleBatchIdSelection(batchId[position])
                    }

                }
            }

            if (batchId.isNotEmpty()) {
                binding.spSavedBatchId.isEnabled = true
            }
        }
    }

    private fun handleBatchIdSelection(batchid: String) {
        if (selectedBatchId != "Select Batch No") {
            isToggleEnabled = true

            isDeleteEcuador = true

            //Removing all data from VegaEcuadorOffloadingBagMaterial Table
            vm.deleteBagDetails()
            showLoading()
            Handler().postDelayed({
                hideLoading()
            }, 2000)
            var list = savedBagList.filter { it.batchNumber.equals(batchid) }
            wbList.clear()
            bagList = list as ArrayList<VegaEcuadorOffloadingBagMaterial>
            palletWeight = "0"
            palletCount = "0"
            palletAvg = "0"
            // Add the weighments for the selected batch to VegaEcuadorOffloadingBagMaterial table
            // while saving the EcuadorOffloading Table"
            isDeleteEcuador = false
            vm.saveSelectedBatchBagDetails(bagList)


            binding.tvTruckValue.text = bagList[0].wbId
            receivingData.weighBridgeId = bagList[0].wbId.toString()
//            binding.tvDestValue.text = bagList[0].supplierName
            receivingData.supplierName = bagList[0].supplierName
            receivingData.supplierCode = bagList[0].supplierCode
//            binding.tvMaterialValue.text = bagList[0].materialName
            receivingData.materialName = bagList[0].materialName
//            binding.tvTruckNoValue.text = bagList[0].truckNo
            receivingData.truckNo = bagList[0].truckNo
//            binding.tvMtntNoValue.text = bagList[0].mtntNo
            receivingData.mtnCode = bagList[0].mtntNo
//            binding.tvMtntWeightValue.text = bagList[0].mtntWeight
            receivingData.approximateWeight = bagList[0].mtntWeight
//            binding.tvRecLocationValue.text = bagList[0].receivingLocation
            receivingData.storageLocationCode = bagList[0].receivingLocation
            receivingData.receivingWH = bagList[0].receivingLocation
            binding.clSupplierInfo.tvSelectPO.text = bagList[0].purcheseOrderNo
            binding.clSupplierInfo.tvReceivingLocation.text = bagList[0].receivingPlant
            binding.clSupplierInfo.otLotValue.setText(bagList[0].otLotNumber.toString())
            receivingData.purchaseDocNum = bagList[0].purcheseOrderNo.split(" - ")[0]
            receivingData.purchaseDocQty =
                bagList[0].purcheseOrderNo.split(" - ")[1].replace("KG", "")
            purchaseOrderQuantity = receivingData.purchaseDocQty.toString()
            receivingData.purchaseDocDesc = bagList[0].purchaseDocDesc
            binding.clSupplierInfo.batchNo.setText(bagList[0].batchNumber)
            receivingData.batchNumber = bagList[0].batchNumber
            receivingData.batchNumber = selectedBatchId
            receivingData.plantId = selectedPlantId
            receivingData.plantId = truckData.plant
            batchNumber = selectedBatchId
            isAutoBatch = bagList[0].isAutoBatchId!!
            receivingPlant = bagList[0].receivingPlant.toString()
            otLotNumber = bagList[0].otLotNumber.toString()
            tempBagCount = bagList[0].tempBagCount.toString()
            //This will update the recycler view for the weighments
            vm.getWBBagItems(
                bagList[0].wbId.toString()
            )
            this.binding.clSupplierInfo.batchNo.isEnabled = !isAutoBatch

        }
    }

    private fun updatePalletBlock(bundle: Bundle?) {
        displayFragment(VegaCocoaAddPalletFragment.newInstance(bundle), false)
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
            containerViewId = R.id.flPalletSup,
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

    private fun enableSaveBtn(flag: Boolean) {
        when (flag) {
            true -> {
                binding.btSave.isEnabled = true
                binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.blue_light))
            }
            false -> {
                binding.btSave.isEnabled = false
                binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            }
        }
    }



    private fun saveItemsLocally() {

            onLocalSave = true
            var saveBagList = arrayListOf<VegaCameroonOffloadingBagMaterial>()
            isDeleteCameroon = true
            vm.deleteCameroonSavedBagDetails(receivingData.batchNumber.toString())

            bagList.forEach {
                saveBagList.add(it.convertToVegaCameroonBagMaterial())
            }

            saveBagList.forEach { material ->
                material.message = getString(R.string.stored_locally)
                material.tmpWbId = receivingData.tmpWbId
                material.supplierCode = receivingData.supplierCode!!
                material.materialCode = receivingData.materialCode!!
                material.truckNo = receivingData.truckNo!!
//                material.mtntNo = binding.tvMtntNoValue.text.toString()
                material.mtntNo = truckData.delivery
                material.receivingLocation = receivingData.storageLocationCode!!
                material.wbId = receivingData.weighBridgeId
                material.batchNumber = batchNumber
                material.plant = selectedPlantId
                material.plant = truckData.plant!!
                material.supplierName = receivingData.supplierName!!
                material.materialName = receivingData.materialName!!
                material.purcheseOrderNo = binding.clSupplierInfo.tvSelectPO.text.toString()
                material.purchaseDocDesc = receivingData.purchaseDocDesc.toString()
                material.batchNumber = binding.clSupplierInfo.batchNo.text.toString()
//                material.mtntWeight = binding.tvMtntWeightValue.text.toString()
                material.mtntWeight = truckData.netWeight
                material.isAutoBatchId = isAutoBatch
                material.receivingPlant = binding.clSupplierInfo.tvReceivingLocation.text.toString()
                material.otLotNumber = binding.clSupplierInfo.otLotValue.text.toString()
                material.tempBagCount = tempBagCount
            }


            isDeleteCameroon = false
            // Saving data to VegaCameroonOffloadingBagMaterials
            vm.saveCameroonSaveBagDetails(saveBagList)

           receivingData.palletWeight = palletWeight
           receivingData.palletCount = palletCount
           vm.saveOffloading(receivingData)

            Toast.makeText(
                activity,
                getString(R.string.saved),
                Toast.LENGTH_SHORT
            ).show()


    }

    private fun updatePurchaseOrderUILocal(it: List<VegaEcuadorPurchaseOrder>?) {
        if (it != null) {
            purchaseOrderList = it.toMutableList()
        }
    }

    private fun moveToOfflineSummary() {
        callBack?.replaceFragment(OFFLOADING_OFFLINE)
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
                                binding.clSupplierInfo.tvSelectPO.text = selectedPo
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
            purData = (list.filter {
                receivingData.materialCode?.let { it1 -> it.materialNumber.contains(it1) }!! &&
                        receivingData.supplierCode?.let { it1 -> it.supplier.contains(it1) }!! &&
                        it.bsart.equals(PurchaseType.FIXEDPURCHASE.type)
            }).toMutableList()
            val purchaseOrders =
                purData.map { data -> data.poId.plus(" - ").plus(data.openQuantity).plus(receivingData.unitsOfMeasure) }

            customDialog =
                VegaCameroonOffloadingCustomSingleSelectDialog(
                    getString(R.string.select_purchase_order),
                    true,
                    false,
                    false,
                    purchaseOrders as ArrayList<String>,
                    requireActivity(),
                    this
                )
            customDialog?.show()
            customDialog?.setCanceledOnTouchOutside(false)

        }
    }

    private fun showProductDialog() {
        var productListString = if(isTTComplaint){ productLists.filter { it.complainceFlag.equals("Compliant") }.map { data -> data.materialCode.plus(" - ").plus(data.materialName)} } else { productLists.filter { it.complainceFlag.equals("Non-Compliant") }.map { data -> data.materialCode.plus(" - ").plus(data.materialName)} }
        if(productListString.isEmpty()) { productListString = productLists.map { data -> data.materialCode.plus(" - ").plus(data.materialName) } }
            customDialog =
                VegaCameroonOffloadingCustomSingleSelectDialog(
                    getString(R.string.select_product),
                    false, true, false,
                    productListString as ArrayList<String>,
                    requireActivity(),
                    this
                )
            customDialog?.show()
            customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun showSupplierDialog() {
        val suppliers = supplierList
            .map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
        customDialog =
            VegaCameroonOffloadingCustomSingleSelectDialog(
                getString(R.string.select_supplier),
                false, false, true,
                suppliers as ArrayList<String>,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun moveBagAddWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val bundle = Bundle()
        bundle.putString(MATERIAL_NAME, receivingData.materialName)
        bundle.putString(UNITS_OF_MEASURE, receivingData.unitsOfMeasure)
        bundle.putParcelable(BAG_MATERIAL, bagMaterial)
        bundle.putString(Constants.PALLET_AVG, palletAvg)
        callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT_SUPPLIER, bundle)
    }

    private fun updateBagItems(bagItems: List<VegaEcuadorOffloadingBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
           // enableSaveBtn(bagList.size > 0)
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

                updatePalletBlock(bundle)
                updatePalletDetails(
                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet.toString() else palletCount,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight.toString() else palletWeight
                )

                if (palletCount.isNotEmpty() && !palletCount.equals("0"))
                    binding.btProceed.isEnabled =
                        (bagList.size == palletCount.toInt())
                else
                    binding.btProceed.isEnabled =
                        bagList.size > 0
                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                    getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
                )
                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))


                updateTotalWeights()
            } else {
                val bundle = Bundle()
                bundle.putString(Constants.PALLET_WEIGHT, palletWeight)
                bundle.putString(Constants.PALLET_COUNT, palletCount)
                bundle.putBoolean(Constants.PALLET_EDIT, true)
                updatePalletBlock(bundle)
            }

            setUpAdapter(bagList)
        }
    }

    fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        if (noOfPallet.isNotEmpty() && palletWeight.isNotEmpty() && !palletWeight.equals("0")) {
                 enableSaveBtn(true)
            this.palletWeight = palletWeight
            palletCount = noOfPallet
            palletAvg = palletWeight.toDouble().div(noOfPallet.toInt()).formatThreeDigits().replace(",", "")

            if (palletCount.isNotEmpty() && !palletCount.equals("0"))
                binding.btProceed.isEnabled =
                    (bagList.size == palletCount.toInt())
            else
                binding.btProceed.isEnabled =
                    bagList.size > 0
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))

            if (bagList.size > 0) {
                bagList.forEach {
                    it.palletAverage = palletAvg
                    it.palletWeight = palletWeight
                    it.noOfPallet = palletCount
                }
                updateTotalWeights()
                binding.rvWeight.adapter?.notifyDataSetChanged()
            }
        } else {
            this.palletWeight = "0"
            palletCount = "0"
            palletAvg = "0"
            binding.btProceed.isEnabled = true
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            if (bagList.size > 0) {
                enableSaveBtn(true)
                bagList.forEach {
                    it.palletAverage = "0"
                    it.palletWeight = "0"
                    it.noOfPallet = "0"
                }
                updateTotalWeights()
                binding.rvWeight.adapter?.notifyDataSetChanged()
            }
        }
    }



    fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        isDeleteEcuador = false
        var isExistValue = false
        var pos: Int = 0
        val addedBagItems = arrayListOf<String>()
        var palletAddedCount = 0
        bagList.forEachIndexed { index, it ->
            if (it.id == bagMaterial.id) {
                isExistValue = true
                pos = index
            }
            palletAddedCount += if (it.noOfPallet?.isNotEmpty() == true) it.noOfPallet?.toInt()
                ?: 0 else 0
            if (!addedBagItems.contains(it.bagType) && it.bagType.isNotEmpty()) addedBagItems.add(it.bagType)
            if (!addedBagItems.contains(it.bagType1) && it.bagType1?.isNullOrEmpty() == false) addedBagItems.add(
                it.bagType1 ?: ""
            )
        }

        palletAddedCount += if (bagMaterial.noOfPallet?.isNotEmpty() == true) bagMaterial.noOfPallet?.toInt()
            ?: 0 else 0
        if (bagMaterial.bagType.isNotEmpty()) addedBagItems.add(bagMaterial.bagType)
        if (bagMaterial.bagType1?.isNullOrEmpty() == false) addedBagItems.add(bagMaterial.bagType1!!)
        val distItem = addedBagItems.distinct()
        val tareWeightCalculation = if (palletAddedCount > 0) distItem.size + 1 else distItem.size
        if (tareWeightCalculation <= 3 || (tareWeightCalculation <= 3 && bagMaterial.bagType.isEmpty() && bagMaterial.bagType1?.isEmpty() == true && bagMaterial.noOfPallet?.isEmpty() == true)) {
            if (!isExistValue) {
                pos= bagList.size
                bagMaterial.id = Random.nextInt()
                bagMaterial.materialCode = receivingData.materialCode.toString()
                bagMaterial.supplierCode = receivingData.supplierCode.toString()
                bagMaterial.procureType = "Fixed"
                bagMaterial.purcheseOrderNo = receivingData.purchaseDocNum.toString()
            } else {
                bagList.removeAt(pos)
            }
            bagList.add(pos,bagMaterial)

            bagList.forEach { material ->
                material.message = getString(R.string.stored_locally)
                material.wbId = receivingData.weighBridgeId
                if (material.bagCount1.isNullOrEmpty()) material.bagCount1 = "0"
                if (material.bagCount.isNullOrEmpty()) material.bagCount = "0"
                if (material.tareWeight.isNullOrEmpty()) material.tareWeight = "0"
                if (material.tareWeight1.isNullOrEmpty()) material.tareWeight1 = "0"
                vm.saveBagDetails(material)
            }
            if (palletCount.isNotEmpty() && !palletCount.equals("0"))
                binding.btProceed.isEnabled =
                    (bagList.size == palletCount.toInt())
            else
                binding.btProceed.isEnabled =
                    bagList.size > 0
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
            updateTotalWeights()
            setUpAdapter(bagList)
           // binding.rvWeight.adapter?.notifyDataSetChanged()

        } else {
            showSnack(getString(R.string.please_add_less_three))
        }

    }

    private fun setUpAdapter(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) {
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            binding.rvWeight.setUpAdapter(
                bagList,
                R.layout.item_vega_cameroon_offloading_supplier_bag,
                ItemVegaCameroonOffloadingSupplierBagBinding::inflate,
                { it, pos, bindItem ->
                    if (pos % 2 == 0) {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                    } else {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                    }
                    bindItem.tvSno.text = pos.plus(1).toString()
                    bindItem.tvBag.text =
                        it.bagCount.toInt().plus(
                            if (!it.bagCount1.isNullOrEmpty()) it.bagCount1?.toInt() ?: 0 else 0
                        ).toString()
                    bindItem.tvGrossWeight.text =
                        it.grossWeight.toDouble().formatThreeDigits().plus(" ")
                            .plus(receivingData.unitsOfMeasure)
                    val avgAvlue =
                        it.bagCount.toInt().times(it.tareWeight?.toDouble() ?: 0.0)
                            .plus(
                                (if (!it.bagCount1.isNullOrEmpty()) it.bagCount1?.toInt()
                                    ?: 0 else 0).times(
                                    if (!it.tareWeight1.isNullOrEmpty()) it.tareWeight1?.toDouble()
                                        ?: 0.0 else 0.0
                                )
                            )
                    bindItem.tvTarWeight.text =
                        avgAvlue.formatThreeDigits().plus(" ").plus(receivingData.unitsOfMeasure)

                    if (it.palletWeight?.isNotEmpty() == true) {
                        avgValueWithPallet =
                            it.palletWeight?.toDouble()
                                ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble() ?: 0.0))
                                ?.plus(
                                    (if (!it.bagCount1.isNullOrEmpty()) it.bagCount1?.toInt()
                                        ?: 0 else 0).times(
                                        if (!it.tareWeight1.isNullOrEmpty()) it.tareWeight1?.toDouble()
                                            ?: 0.0 else 0.0
                                    )
                                )
                    }

                    bindItem.tvNetWeight.text =
                        it.grossWeight.toDouble().minus(avgAvlue).formatThreeDigits().plus(" ")
                            .plus(receivingData.unitsOfMeasure)
                    bindItem.ivEdit.tag=pos

                    bindItem.ivEdit.setOnClickListener { view ->
                        val clickedPosition= bindItem.ivEdit.tag as Int
                        val clickItem= bagList[clickedPosition]
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
                                    clickItem.netWeight = clickItem.grossWeight.toDouble().minus(avgAvlue)
                                        .formatThreeDigits()
                                    moveBagAddWeight(clickItem)
                                }
                                com.olam.warehouse.login.R.id.action_delete -> {
                                    showDeleteConfirmationDialog(clickItem, pos)
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

    private fun showDeleteConfirmationDialog(item: VegaEcuadorOffloadingBagMaterial, pos: Int) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    bagList.remove(item)
                    binding.rvWeight.adapter?.notifyItemRemoved(pos)
                    vm.deleteSelectedBagDetails(item.id, item.wbId.toString())
                    if (bagList.size == 0) {
                        binding.btProceed.isEnabled = false
                        binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                    }
                    updateTotalWeights()
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
            if (item.tareWeight?.isNotEmpty() == true && item.bagCount.isNotEmpty() && item.tareWeight1?.isNotEmpty() == true && item.bagCount1?.isNotEmpty() == true) {
                tareWeight =
                    tareWeight.plus(
                        item.tareWeight?.toDouble()?.times(item.bagCount.toDouble()) ?: 0.0
                    )
                        .plus(
                            item.tareWeight1?.toDouble()?.times((item.bagCount1 ?: "0").toDouble())
                                ?: 0.0
                        )
            }

        }
        netWeight = grossWeight.minus((tareWeight).plus(palletWeight.toDouble()))
        binding.clNet.tvGrossWeightValue.text = grossWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        binding.clNet.tvTareWeightValue.text = tareWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        binding.clNet.tvPalletWeightValue.text = palletWeight.plus(receivingData.unitsOfMeasure)
        binding.clNet.tvNetWeightValue.text = netWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        receivingData.grossWeight = grossWeight.formatThreeDigits()
        receivingData.tareWeight = tareWeight.formatThreeDigits()
        receivingData.palletWeight = palletWeight
        receivingData.netWeight = netWeight.formatThreeDigits()
    }

    private fun updateMandatory() {
        binding.clSupplierInfo.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.clSupplierInfo.otLotLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.ot_lot_number)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.clSupplierInfo.tvReceivingLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_plant)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSelectType.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_type)) { mandatoryStars() } }
        binding.clSupplierInfo.tvSelectPOLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_po)) { mandatoryStars() } }
        binding.clSupplierInfo.batchNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.batch_no)) { mandatoryStars() } }
    }

    private fun moveToSummary() {
        when {
            batchNumber.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_batch_no))
            batchNumber.length<6 -> showSnack(getString(R.string.minimum_length_batch_no))
            bagList.size == 0 -> showSnack(getString(R.string.enter_weight_details))
            binding.clSupplierInfo.tvReceivingLocation.text == "" -> showSnack(getString(R.string.select_receiving_plant))
//            binding.clSupplierInfo.otLotValue.text.toString() == "" -> showSnack(getString(R.string.enter_valid_ot_lot_number))
            else ->
                /*if (!receivingData.purchaseDocNum.isNullOrEmpty() && receivingData.netWeight.toDouble() > purchaseOrderQuantity.toDouble()) {
                    showSnack(getString(R.string.net_weight_error))
                } else*/ if (batchNumber.length > 0 && batchNumber.contains(" "))
                    showSnack(getString(R.string.no_spaces_for_batch_number))
                else {
                    receivingData.wsGate = WS01
                    receivingData.weighBridgeType = PROCURE
                    mReceiving.clear()
                    bagCountTotal = 0
                    bagList.forEachIndexed { index, it ->
                        val receiving = receivingData.copy()
                        when (it.unitsOfMeasure) {
                            "KG" -> {
                                receiving.bagWeight =
                                    it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())
                                        ?.formatThreeDigits()
                                receiving.bagWeight1 =
                                    it.tareWeight1?.toDouble()
                                        ?.times((it.bagCount1 ?: "0").toDouble())
                                        ?.formatThreeDigits()
                                receiving.netWeight = it.netWeight
                                receiving.grossWeight = it.grossWeight
                                receiving.tareWeight = it.tareWeight
                                receiving.tareWeight1 = it.tareWeight1
                                receiving.palletCount = it.noOfPallet
                                receiving.palletWeight = it.palletWeight
                                receiving.driverName = driverName
                                receiving.vehicleNumber = vehicleNumber
                                receiving.truckDriverName = driverName
                                receiving.tempBagCount = tempBagCount
                            }
                            "MT" -> {
                                receiving.bagWeight = convertKgToMT(
                                    it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())
                                        ?.formatThreeDigits()
                                )
                                receiving.bagWeight1 = convertKgToMT(
                                    it.tareWeight1?.toDouble()
                                        ?.times((it.bagCount1 ?: "0").toDouble())
                                        ?.formatThreeDigits()
                                )
                                receiving.netWeight = convertKgToMT(it.netWeight).toString()
                                receiving.grossWeight = convertKgToMT(it.grossWeight)
                                receiving.tareWeight = convertKgToMT(it.tareWeight)
                                receiving.tareWeight1 = convertKgToMT(it.tareWeight1)
                                receiving.palletCount = convertKgToMT(it.noOfPallet)
                                receiving.palletWeight = convertKgToMT(it.palletWeight)
                                receiving.driverName = driverName
                                receiving.truckDriverName = driverName
                                receiving.vehicleNumber = vehicleNumber
                                receiving.tempBagCount = tempBagCount
                            }
                            else -> {
                                receiving.bagWeight =
                                    it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())
                                        ?.formatThreeDigits()
                                receiving.bagWeight1 =
                                    it.tareWeight1?.toDouble()
                                        ?.times((it.bagCount1 ?: "0").toDouble())
                                        ?.formatThreeDigits()
                                receiving.netWeight = it.netWeight
                                receiving.grossWeight = it.grossWeight
                                receiving.tareWeight = it.tareWeight
                                receiving.tareWeight1 = it.tareWeight1
                                receiving.palletCount = it.noOfPallet
                                receiving.palletWeight = it.palletWeight
                                receiving.driverName = driverName
                                receiving.vehicleNumber = vehicleNumber
                                receiving.truckDriverName = driverName
                                receiving.tempBagCount = tempBagCount
                            }
                        }
                        receiving.bagType = it.bagType
                        receiving.bagType1 = it.bagType1
                        receiving.bagCount = it.bagCount
                        receiving.bagCount1 = it.bagCount1
                        receiving.receivingWH = receivingData.storageLocationCode

                        receiving.wRNo = receivingData.wRNo
                        receiving.batchNumber = binding.clSupplierInfo.batchNo.text.toString()
                        receiving.item = index.inc().toString()
                        mReceiving.add(receiving)
                        bagCountTotal =
                            bagCountTotal.plus(it.bagCount.toInt())
                                .plus((it.bagCount1 ?: "0").toInt())
                    }
                    receivingData.bagCount = bagCountTotal.toString()
                    receivingData.approximateWeight= approximateWt
                    receivingData.mtnCode= mtntNumber

                    receivingData.batchNumber = binding.clSupplierInfo.batchNo.text.toString()
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
                    otLotNumber = binding.clSupplierInfo.otLotValue.text.toString()
                    callBack?.replaceFragment(
                        OFFLOADING_SUMMARY_FRAG,
                        receivingData,
                        mReceiving,
                        bagList,
                        receivingPlant.split(":")[0],
                        otLotNumber,
                        plantDetails
                    )

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

    private fun validateInputs() {
        when {
            receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_product))
            receivingData.supplierName.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_supplier))
            batchNumber.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_batch_no))
            binding.clSupplierInfo.llSelectPO.isVisible && binding.clSupplierInfo.tvSelectPO.text.isNullOrEmpty() -> showSnack(
                getString(R.string.message_valid_po_type)
            )
            else -> moveBagAddWeight(VegaEcuadorOffloadingBagMaterial())
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isProduct: Boolean, isSupplier: Boolean) {
        customDialog?.dismiss()
        if(isWh){
            binding.clSupplierInfo.tvSelectPO.text = data
            var selectedPO = purData.filter {
                it.poId.equals(data.split(" - ")[0])
            }
            receivingData.purchaseDocNum = selectedPO[0].poId
            receivingData.purchaseDocDesc = selectedPO[0].ebelp

            purchaseOrderQuantity = selectedPO[0].openQuantity.toString()
            receivingData.purchaseDocQty = purchaseOrderQuantity

            vm.getWBBagItems(
                receivingData.weighBridgeId
            )
        }else if(isProduct){
            binding.clSupplierInfo.tvProduct.text = data
            var materialCode = data.split(" - ")[0]
            if(materialCode.length < 18){
                materialCode = MATERIAL_CODE.plus(materialCode)
            }
            receivingData.materialCode = materialCode
            receivingData.materialName = data.split(" - ")[1]

        } else if(isSupplier){
            binding.clSupplierInfo.tvSupplier.text = data
            receivingData.supplierCode = data.split("-")[0].trim()
            receivingData.supplierName = data.split("-")[1].trim()
        } else{
            binding.clSupplierInfo.tvReceivingLocation.text = data
            receivingPlant = data
        }

    }

    private fun resetMaterial(){
        binding.clSupplierInfo.tvSupplier.text = ""
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
            receivingData.supplierCode = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(0)?.trim()
            receivingData.supplierName = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(1)?.trim()

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
//            var supplierList = mutableListOf<VegaVendor>()
            var checkSupplier = supplierList.filter { it.vendorCode.equals(sourceLotDetails.vendorCode) }
                if(checkSupplier.isNullOrEmpty()){
                    showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
                } else {
                    var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                    binding.clSupplierInfo.tvSupplier.text = data
                receivingData.supplierCode = data.split("-")[0].trim()
                receivingData.supplierName = data.split("-")[1].trim()
//                clearPurchaseOrder()
            }
        } else {
            showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
        }
    }

    fun updateFarmerlessTransactionDetails(transactionIdDetails: TrackTraceTransactionIdDetails){
        resetMaterial()
        isTTComplaint = transactionIdDetails?.compliantFlag?:false
        this.ttFarmerlessTransactionDetails = transactionIdDetails
        binding.clSupplierInfo.tvSupplier.text = ""
        if(transactionIdDetails.vendorCode?.isNotEmpty() == true){
            var checkSupplier = supplierList.filter { it.vendorCode.equals(transactionIdDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the transaction id is not available in SAP, do you want to continue?")
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
}
