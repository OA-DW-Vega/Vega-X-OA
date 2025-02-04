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
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.entity.VegaCameroonOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.PurchaseType
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonMtntDetails
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentVegaCameroonOffloadingSupplierBinding
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingCustomSingleSelectDialog
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingSingleSelectListener
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcameroon.utils.*
import kotlinx.android.synthetic.main.item_vega_cameroon_offloading_supplier_bag.view.*
import kotlinx.android.synthetic.main.layout_vega_cameroon_offloading_supplier_info.*
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
            plantDetails: Plant
        ) = VegaCameroonOffloadingSupplierFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
            putParcelable(OFFLOADING_TRUCK_DATA, truckData)
            putString(OFFLOADING_PLANT_SELECTED, selectedPlant)
            putParcelable(OFFLOADING_PLANT_DETAILS, plantDetails)
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

        vm.deleteBagDetails()
        binding.tvTruckValue.text = truckData.weighBridgeId
        binding.tvDestValue.text = truckData.supplierName
        binding.tvMaterialValue.text = truckData.materialName
        binding.tvTruckNoValue.text = truckData.vehicleNumber
        binding.tvMtntNoValue.text = truckData.delivery
        binding.tvMtntWeightValue.text = truckData.netWeight
        binding.tvRecLocationValue.text = truckData.storageLocationCode
        binding.tvPlantValue.text = truckData.plant
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

        binding.clSupplierInfo.llSelectPO.visible()
        if (isOnline()) vm.getPOList(selectedPlantId) else vm.getPOListLocal()
        procureType = "Fixed"

        vm.offloadingItemCountLocal.observe(this, Observer {
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


        vm.getSavedBagItems(selectedPlantId)

        //Saved Data from VegaCameroonOffloadingBagMaterials Table
        vm.bagItemsSaved.observe(viewLifecycleOwner, Observer { item ->

            if(!isDeleteCameroon){

                var filteredList = item.filter { it.batchNumber != "" }
                savedBagList = filteredList as ArrayList<VegaEcuadorOffloadingBagMaterial>
                updateSavedBatchIdList(savedBagList)
                if(isToggleEnabled == false && onLocalSave == false){

                    var details = savedBagList.filter { it.wbId.equals(receivingData.weighBridgeId) }
                    vm.saveSelectedBatchBagDetails(details) // No Observer for this
                    savedBagList.forEach { item ->
                        if (item.wbId.equals(truckData.weighBridgeId)) {
                            wbList.add(item)
                            updateBagItems(wbList)
                            selectedPo = item.purcheseOrderNo
                            receivingData.purchaseDocNum = item.purcheseOrderNo.split(" - ")[0]
                            receivingData.purchaseDocQty = item.purcheseOrderNo.split(" - ")[1].replace("KG","")
                            purchaseOrderQuantity = receivingData.purchaseDocQty.toString()
                            receivingData.purchaseDocDesc = item.purchaseDocDesc
                            batchNo.setText(item.batchNumber)
                            receivingPlant = item.receivingPlant.toString()
                            binding.clSupplierInfo.tvReceivingLocation.text = item.receivingPlant
                            binding.clSupplierInfo.otLotValue.setText(item.otLotNumber)
                            otLotNumber = item.otLotNumber.toString()
                            receivingData.batchNumber = item.batchNumber // recheck this code

                        }
                    }
                }
            }

            //Observer is present for VegaEcuadorOffloadingBagMaterials

        })

    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        val receivingPlantList = configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }
        var list = if (receivingPlantList?.isNotEmpty() == true) receivingPlantList.get(0).value else ""

        var charValue = mutableListOf<String>()
        charValue = list?.split(",")?.map { it.trim() }?.toMutableList()?: mutableListOf<String>()

        binding.clSupplierInfo.tvReceivingLocation.setOnClickListener { showReceivingLocationDialog(charValue) }


    }

    private fun showReceivingLocationDialog(charValue: MutableList<String>) {
        customDialog =
            VegaCameroonOffloadingCustomSingleSelectDialog(
                getString(R.string.select_receiving_plant),
                false,
                charValue as ArrayList<String>,
                activity!!,
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
                                binding.tvMtntWeightValue.text = item.appoximateWeight?.replace(" ", "")
                                driverName = item.driverName.toString()
                                vehicleNumber = item.vehicleNumber.toString()
                                tempBagCount = item.tempBagCount.toString()
                                if(!item.autoBatchNumber.toString().isNullOrEmpty() )
                                {
                                    batchNumber = item.autoBatchNumber.toString()
                                    binding.clSupplierInfo.batchNo.isEnabled = false
                                    binding.clSupplierInfo.batchNo.setText(item.autoBatchNumber)
                                    isAutoBatch = true
                                }
                                else{
                                    binding.clSupplierInfo.batchNo.isEnabled = true
                                    isAutoBatch = false
                                }
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
            binding.tvDestValue.text = bagList[0].supplierName
            receivingData.supplierName = bagList[0].supplierName
            receivingData.supplierCode = bagList[0].supplierCode
            binding.tvMaterialValue.text = bagList[0].materialName
            receivingData.materialName = bagList[0].materialName
            binding.tvTruckNoValue.text = bagList[0].truckNo
            receivingData.truckNo = bagList[0].truckNo
            binding.tvMtntNoValue.text = bagList[0].mtntNo
            receivingData.mtnCode = bagList[0].mtntNo
            binding.tvMtntWeightValue.text = bagList[0].mtntWeight
            receivingData.approximateWeight = bagList[0].mtntWeight
            binding.tvRecLocationValue.text = bagList[0].receivingLocation
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
            material.mtntNo = binding.tvMtntNoValue.text.toString()
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
            material.mtntWeight = binding.tvMtntWeightValue.text.toString()
            material.isAutoBatchId = isAutoBatch
            material.receivingPlant = binding.clSupplierInfo.tvReceivingLocation.text.toString()
            material.otLotNumber = binding.clSupplierInfo.otLotValue.text.toString()
            material.tempBagCount = tempBagCount
        }


        isDeleteCameroon = false
        // Saving data to VegaCameroonOffloadingBagMaterials
        vm.saveCameroonSaveBagDetails(saveBagList)
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
                    purchaseOrders as ArrayList<String>,
                    activity!!,
                    this
                )
            customDialog?.show()
            customDialog?.setCanceledOnTouchOutside(false)

        }
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
            enableSaveBtn(bagList.size > 0)
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
                    getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
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
                getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
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
            binding.btProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            if (bagList.size > 0) {
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
            if (!addedBagItems.contains(it.bagType1) && it.bagType1!!.isNotEmpty()) addedBagItems.add(
                it.bagType1!!
            )
        }
        palletAddedCount += if (bagMaterial.noOfPallet?.isNotEmpty() == true) bagMaterial.noOfPallet?.toInt()
            ?: 0 else 0
        if (bagMaterial.bagType.isNotEmpty()) addedBagItems.add(bagMaterial.bagType)
        if (bagMaterial.bagType1!!.isNotEmpty()) addedBagItems.add(bagMaterial.bagType1!!)
        val distItem = addedBagItems.distinct()
        val tareWeightCalculation = if (palletAddedCount > 0) distItem.size + 1 else distItem.size
        if (tareWeightCalculation <= 3 || (tareWeightCalculation <= 3 && bagMaterial.bagType.isEmpty() && bagMaterial.bagType1!!.isEmpty() && bagMaterial.noOfPallet?.isEmpty() == true)) {
            if (!isExistValue) {
                bagMaterial.id = Random.nextInt()
                bagMaterial.materialCode = receivingData.materialCode.toString()
                bagMaterial.supplierCode = receivingData.supplierCode.toString()
                bagMaterial.procureType = "Fixed"
                bagMaterial.purcheseOrderNo = receivingData.purchaseDocNum.toString()
            } else {
                bagList.removeAt(pos)
            }
            bagList.add(bagMaterial)

            bagList.forEach { material ->
                material.message = getString(R.string.stored_locally)
                material.wbId = receivingData.weighBridgeId
                vm.saveBagDetails(material)
            }
            if (palletCount.isNotEmpty() && !palletCount.equals("0"))
                binding.btProceed.isEnabled =
                    (bagList.size == palletCount.toInt())
            else
                binding.btProceed.isEnabled =
                    bagList.size > 0
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
            updateTotalWeights()
            binding.rvWeight.adapter?.notifyDataSetChanged()
        } else {
            showSnack(getString(R.string.please_add_less_three))
        }

    }

    private fun setUpAdapter(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) {
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            binding.rvWeight.setUp(
                bagList,
                R.layout.item_vega_cameroon_offloading_supplier_bag,
                { it, pos ->
                    if (pos % 2 == 0) {
                        clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                    } else {
                        clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                    }
                    tvSno.text = pos.plus(1).toString()
                    tvBag.text = it.bagCount.toInt().plus(it.bagCount1!!.toInt()).toString()
                    tvGrossWeight.text =
                        it.grossWeight.toDouble().formatThreeDigits().plus(" ")
                            .plus(receivingData.unitsOfMeasure)
                    val avgAvlue =
                        it.palletAverage?.toDouble()
                            ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                            ?.plus(it.bagCount1?.toInt()!!.times(it.tareWeight1?.toDouble()!!))
                    tvTarWeight.text =
                        avgAvlue?.formatThreeDigits().plus(" ").plus(receivingData.unitsOfMeasure)
                    tvNetWeight.text =
                        it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                            .plus(receivingData.unitsOfMeasure)
                    ivEdit.setOnClickListener { view ->
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
            tareWeight =
                tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                    .plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
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
        otLotLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.ot_lot_number)) { mandatoryStars() } }
        tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        tvReceivingLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_plant)) { mandatoryStars() } }
        tvSelectType.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_type)) { mandatoryStars() } }
        tvSelectPOLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_po)) { mandatoryStars() } }
        batchNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.batch_no)) { mandatoryStars() } }
    }

    private fun moveToSummary() {
        when {
            batchNumber.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_batch_no))
            bagList.size == 0 -> showSnack(getString(R.string.enter_weight_details))
            binding.clSupplierInfo.tvReceivingLocation.text == "" -> showSnack(getString(R.string.select_receiving_plant))
            binding.clSupplierInfo.otLotValue.text.toString() == "" -> showSnack(getString(R.string.enter_valid_ot_lot_number))
            else ->
                if (!receivingData.purchaseDocNum.isNullOrEmpty() && receivingData.netWeight.toDouble() > purchaseOrderQuantity.toDouble()) {
                    showSnack(getString(R.string.net_weight_error))
                } else if (batchNumber.length > 0 && batchNumber.contains(" "))
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
                                    it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!
                                        .formatThreeDigits()
                                receiving.bagWeight1 =
                                    it.tareWeight1?.toDouble()?.times(it.bagCount1!!.toDouble())!!
                                        .formatThreeDigits()
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
                                    it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!
                                        .formatThreeDigits()
                                )
                                receiving.bagWeight1 = convertKgToMT(
                                    it.tareWeight1?.toDouble()?.times(it.bagCount1!!.toDouble())!!
                                        .formatThreeDigits()
                                )
                                receiving.netWeight = convertKgToMT(it.netWeight)!!
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
                                    it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!
                                        .formatThreeDigits()
                                receiving.bagWeight1 =
                                    it.tareWeight1?.toDouble()?.times(it.bagCount1!!.toDouble())!!
                                        .formatThreeDigits()
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
                        receiving.batchNumber = batchNo.text.toString()
                        receiving.item = index.inc().toString()
                        mReceiving.add(receiving)
                        bagCountTotal =
                            bagCountTotal.plus(it.bagCount.toInt()).plus(it.bagCount1!!.toInt())
                    }
                    receivingData.bagCount = bagCountTotal.toString()

                    receivingData.batchNumber = batchNo.text.toString()
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

    override fun clickOnItem(data: String, isWh: Boolean) {
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
        }
        else{
            binding.clSupplierInfo.tvReceivingLocation.text = data
            receivingPlant = data
        }


    }
}
