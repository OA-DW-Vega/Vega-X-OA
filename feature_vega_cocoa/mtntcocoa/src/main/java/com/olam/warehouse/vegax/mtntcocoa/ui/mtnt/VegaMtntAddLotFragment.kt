package com.olam.warehouse.vegax.mtntcocoa.ui.mtnt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrder
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaLotListModel
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaSummary
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentMtntLotSelectLayoutBinding
import com.olam.warehouse.vegax.mtntcocoa.ui.*
import com.olam.warehouse.vegax.mtntcocoa.utils.MTNT_LOT_LIST
import com.olam.warehouse.vegax.mtntcocoa.utils.getColor
import com.olam.warehouse.vegax.mtntcocoa.utils.getDrawable
import kotlinx.android.synthetic.main.fragment_mtnt_lot_select_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.concurrent.TimeUnit

class VegaMtntAddLotFragment : BaseFragment(), RecyclerViewItemClickListener, ItemRemoveListener {
    override val layoutResourceId: Int = R.layout.fragment_mtnt_lot_select_layout
    private lateinit var binding: FragmentMtntLotSelectLayoutBinding

    private var callBack: CallBack? = null
    private var listener: RecyclerViewItemClickListener? = null
    private var customDialog: CustomSingleSelectDialog? = null
    private var isLoadingStarted = false
    private var purchaseOrderList = mutableListOf<VegaCocoaPurchaseOrder>()
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var wareHouseId = ""
    private var selectedPurchaseOrder: VegaCocoaPurchaseOrders? = null
    private var isEditableLot = true
    private var isMultipleLot = false
    private var adapter: VegaCocoaMtntLotAdapter? = null
    private var purchaseOrder = ArrayList<VegaCocoaPurchaseOrders>()
    private var remark: String = ""
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var dispatchWbFromTruckList: VegaCocoaDispatchWB? = null
    private val vm: VegaCocoaMtntViewModel by viewModel()
    private var thirdPartyMaterialList = ArrayList<VegaMaterial>()
    private var isThirdPartyMaterial: Boolean = false
    val material = ArrayList<String>()

    companion object {
        fun newInstance(dispatch: VegaCocoaDispatchWB) = VegaMtntAddLotFragment().putArgs {
            putParcelable("data", dispatch)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentMtntLotSelectLayoutBinding.inflate(inflater)
        initExtra()
        initUi()
        listener = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/mtntcocoa/ui/mtnt/VegaMtntAddLotFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
    }

    private fun initExtra() {
        vm.getConfigItems(UserRoles.MTNT.role)
        vm.dispatchWh = arguments?.getParcelable<VegaCocoaDispatchWB>("data") as VegaCocoaDispatchWB
        dispatchWbFromTruckList =
            arguments?.getParcelable<VegaCocoaDispatchWB>("data") as VegaCocoaDispatchWB
        vm.dispatchWB.observeOnce(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.dispatchWh = it
                updateUIWithLocalData()
            }
        })

        vm.dispatchLots.observeOnce(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                vm.lots = it as ArrayList<VegaCocoaDispatchLots>
                adapter?.addAllLots(vm.lots)
            }
            disableAddMoreLots(vm.lots.isEmpty() || isMultipleLot)
        })
        vm.getWeighBrideData(vm.dispatchWh.weighBridgeId)
        vm.getLotsDataFromLocal(vm.dispatchWh.weighBridgeId)
        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                thirdPartyMaterialList.addAll(it)
            }
        })

        adapter = VegaCocoaMtntLotAdapter(ArrayList(), isEditableLot, true, this, isThirdPartyMaterial)
        binding.rvLots.adapter = adapter
        if(vm.dispatchWh.remarks==null)
            vm.dispatchWh.remarks=" "
    }

    private fun updateUIWithLocalData() {
        isLoadingStarted = vm.dispatchWh.isStarted
        selectedPurchaseOrder = VegaCocoaPurchaseOrders()
        enableProceed(vm.dispatchWh.isStarted && vm.dispatchWh.isEnded)
        enableStartLoad(vm.dispatchWh.isStarted && !vm.dispatchWh.isEnded)
        updateLoadingState(vm.dispatchWh.isStarted, vm.dispatchWh.isEnded)
        binding.tvWhValue.text = vm.dispatchWh.storageLocationCode.plus("-").plus(vm.dispatchWh.plantName)
        binding.tvStoWeightValue.setText(
            vm.dispatchWh.netWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                .plus(vm.dispatchWh.unitsOfMeasure)
        )
        binding.tvstoValue.text = vm.dispatchWh.purchaseDocNum.plus("-").plus(vm.dispatchWh.purchaseDocDesc)
        binding.tvMaterialName.text = vm.dispatchWh.materialName
        selectedPurchaseOrder?.materialName = vm.dispatchWh.materialName ?: ""
        selectedPurchaseOrder?.materialCode = vm.dispatchWh.materialCode ?: ""
        selectedPurchaseOrder?.plantId = vm.dispatchWh.plantId
    }

    private fun initUi() {
        binding.tvTruckValue.text = vm.dispatchWh.vehicleNumber
        binding.tvWhValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_dest_wh)) }
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(false, getString(R.string.select_sto)) }
        binding.tvInventory.setOnClickListener {

            callBack?.replaceFragment(
                MTNT_LOT_LIST,
                VegaCocoaLotListModel(
                    selectedList = prepareLots(),
                    isMultipleAdd = isMultipleLot,
                    material = getMaterialList()
                )
            )
        }
        vm.getPurchaseOrder("")
        binding.btStartLoad.setOnClickListener {
            if (!isLoadingStarted) {
                showStartLoadDialog(false)
            } else if (isLoadingStarted && adapter?.getSize() == true) {
                showStartLoadDialog(true)
            } else Toast.makeText(activity, getString(R.string.add_lot_msg), Toast.LENGTH_SHORT).show()
        }
        enableStartLoad(false)
        enableProceed(false)
        binding.btProceed.setOnClickListener {
            if (vm.lots.isNotEmpty())
                if (validateLotWeight()) callBack?.replaceFragment("Summary", prepareSummaryData())
                else Toast.makeText(
                    activity,
                    getString(R.string.less_weight_error),
                    Toast.LENGTH_SHORT
                ).show()
            else Toast.makeText(
                activity,
                getString(R.string.prceed_error),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        vm.product.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    binding.tvMaterialName.text = it.materialName ?: ""
                    selectedPurchaseOrder?.materialName = it.materialName ?: ""
                    vm.dispatchWh.materialName = it.materialName ?: ""
                }
            })
        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        binding.btAdd.setOnClickListener {
            vm.validateLot(
                etEnterContainer.text.toString(),
                vm.dispatchWh.materialCode ?: ""
            )
        }

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rvLots.layoutManager = linearLayoutManager

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            material.clear()
            if (it != null) {
                showLotAlreadyExistDialog()
                etEnterContainer.setText("")
            } else {

                material.add(selectedPurchaseOrder?.materialCode ?: "")
                fetchLotDetails(
                    etEnterContainer.text.toString(),
                    material, selectedPurchaseOrder?.plantId ?: ""
                )
            }
        })

        vm.vendorDetails.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.lots[0].vendorName = it.vendorName
            }
            updateLotWithVendorInfo(vm.lots[0])
        })

        binding.tvClearValue.setOnClickListener {
            vm.deleteTruckAndLotsData(vm.dispatchWh.weighBridgeId)
            callBack?.replaceFragment("Add_Lot_Refresh", dispatchWbFromTruckList ?: VegaCocoaDispatchWB())
        }
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_SCAN.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> removeScanLot(false)
                        it.applicable?.contains("N")!! -> removeScanLot(true)
                    }
                }
                ConfigItems.LOT_MANUAL.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            removeManualLot(false)
                        }
                        it.applicable?.contains("N")!! -> {
                            removeManualLot(true)
                        }
                    }
                }
                ConfigItems.LOT_INVENTORY.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> removeInventoryLot(false)
                        it.applicable?.contains("N")!! -> removeInventoryLot(false)
                    }
                }
                ConfigItems.LOT_WEIGHT_EDITABLE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isEditableLot = true
                        it.applicable?.contains("N")!! -> isEditableLot = false
                    }
                }
                ConfigItems.LOT_SELECTION_MULTI.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isMultipleLot = true
                        it.applicable?.contains("N")!! -> {
                            isMultipleLot = false
                        }
                    }
                }
            }
            adapter?.updateEditState(isEditableLot)
        }
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String) {
        if (isLoadingStarted) return
        if (storageLocation.isEmpty()) filterWHFromPurchaseOrder()
        customDialog =
            CustomSingleSelectDialog(title, isWh, if (isWh) storageLocation else STONumbers, activity!!, listener!!)
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean) {
        customDialog?.dismiss()
        if (isWh) {
            binding.tvWhValue.text = data
            wareHouseId = data
            val da = data.split("-").toTypedArray()
            binding.tvMaterialName.text = ""
            binding.tvStoWeightValue.setText("")
            binding.tvstoValue.text = ""
            enableStartLoad(false)
            STONumbers = filterSTONumber(da[0]) as ArrayList<String>
            vm.dispatchWh.storageLocationCode = da[0]
            vm.dispatchWh.plantName = da[1]
        } else {
            binding.tvstoValue.text = data
            getWeight(data)
            val materialSubStr = selectedPurchaseOrder?.materialCode?.substring(6)
            vm.getProduct(materialSubStr ?: "")
            binding.tvStoWeightValue.setText(
                selectedPurchaseOrder?.menge?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                    .plus(selectedPurchaseOrder?.meins)
            )
            enableStartLoad(true)
            vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
            vm.dispatchWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
            vm.dispatchWh.materialCode = selectedPurchaseOrder?.materialCode
            vm.dispatchWh.netWeight = selectedPurchaseOrder?.menge
            vm.dispatchWh.unitsOfMeasure = selectedPurchaseOrder?.meins
        }
    }

    private fun showStartLoadDialog(isEndLoad: Boolean) {
        showDialog(
            if (isEndLoad) getString(R.string.end_load_msg) else
                getString(R.string.start_msg),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    if (isEndLoad) {
                        if (remark.isEmpty()) Toast.makeText(
                            activity,
                            getString(com.olam.warehouse.presentation.R.string.enter_remark),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        else {
                            enableProceed(isEndLoad)
                            updateState(true, remark)
                        }
                    } else {
                        updateState(false, remark)
                        vm.dispatchWh.isStarted = true
                        vm.dispatchWh.isEnded = false
                    }
                }
            },
            isEndLoad
        )
    }

    private fun updateState(isEndLoad: Boolean, remark: String) {
        isLoadingStarted = true
        updateLoadingState(true, isEndLoad)
        vm.dispatchWh.remarks = remark
        vm.updateRemarks(remark, true, vm.dispatchWh.weighBridgeId)
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        when (it1.size == 1) {
                            true -> updateAdapter(it1[0])
                            else -> chooseOneLotDialog(it1)
                        }

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

    private fun chooseOneLotDialog(lots: List<VegaCocoaDispatchLots>) {
        val lotItem = lots.map {
            getString(com.olam.warehouse.login.R.string.lot_no).plus(" : ").plus(it.batchNumber).plus("\n")
                .plus(getString(R.string.weight)).plus(" : ").plus(it.weight).plus(" ").plus(it.unitOfMeasure)
                .plus("\n").plus(getString(R.string.st_location)).plus(" : ").plus(it.storageLocationCode)
        }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_lot)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = lotItem) { _, index, text ->
                updateAdapter(lots[index])
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun updateAdapter(vegaCocoaDispatchLots: VegaCocoaDispatchLots) {
        validateThirdPartyMaterialMapping(vm.dispatchWh.materialCode ?: "")
        vm.lots.add(vegaCocoaDispatchLots)
        if (isThirdPartyMaterial && !vegaCocoaDispatchLots.vendor.isNullOrEmpty()) {
            vm.getVendorInfo(vegaCocoaDispatchLots.vendor ?: "")
        } else {
            updateLotWithVendorInfo(vegaCocoaDispatchLots)
        }
    }

    private fun updateLotWithVendorInfo(vegaCocoaDispatchLots: VegaCocoaDispatchLots) {
        adapter?.isThirdPartyMaterial = isThirdPartyMaterial
        adapter?.addLotData(vegaCocoaDispatchLots)
        binding.etEnterContainer.setText("")
        vm.addLoTInDB(vegaCocoaDispatchLots)
        disableAddMoreLots(isMultipleLot)
    }


    private fun updateLoadingState(isAlreadyStarted: Boolean, isAlreadyEnd: Boolean) {
        if (isAlreadyStarted) {
            if (isAlreadyEnd) {
                vm.dispatchWh.isEnded = true
                endTime = System.currentTimeMillis()
                vm.dispatchWh.endTime = endTime.toString()
                val duration = endTime?.minus(startTime ?: 0)
                vm.dispatchWh.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration ?: 0).toString()
                btStartLoad.text = resources.getString(R.string.end_loading)
                btStartLoad.setTextColor(getColor(com.olam.warehouse.presentation.R.color.white))
                binding.btStartLoad.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_corner_violet_ofi)
                ViewCompat.setBackgroundTintList(
                    binding.btStartLoad,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.light_grey_3
                        )
                    }
                )
                ivPlay.setImageDrawable(getDrawable(R.drawable.ic_pause_white))
                btStartLoad.isEnabled = false
                vm.updateEndLoading()
            } else {
                vm.dispatchWh.isStarted = true
                startTime = System.currentTimeMillis()
                vm.dispatchWh.startTime = startTime.toString()
                btStartLoad.text = resources.getString(R.string.end_loading)
                binding.btStartLoad.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_corner_violet_ofi)
                ViewCompat.setBackgroundTintList(
                    binding.btStartLoad,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.red
                        )
                    }
                )
                ivPlay.setImageDrawable(getDrawable(R.drawable.ic_pause_white))
                vm.saveWeighBridgeAndLotDetails()
            }
        } else {
            enableStartLoad(true)
        }
    }

    private fun enableProceed(isEnable: Boolean) {
        binding.btProceed.isEnabled = isEnable
        binding.btProceed.setBackgroundColor(
            if (isEnable) getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
            else getColor(com.olam.warehouse.presentation.R.color.grey)
        )
    }

    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(
                    activity!!,
                    if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }

    private fun enableStartLoad(isEnable: Boolean) {
        binding.btStartLoad.isEnabled = isEnable
        if (isEnable) {
            binding.btStartLoad.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_corner_violet_ofi)
            ViewCompat.setBackgroundTintList(
                binding.btStartLoad,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                }
            )
        } else {
            binding.btStartLoad.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_corner_violet_ofi)
            ViewCompat.setBackgroundTintList(
                binding.btStartLoad,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.grey
                    )
                }
            )
        }
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                material.clear()
                material.add(selectedPurchaseOrder?.materialCode ?: "")
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    fetchLotDetails(it, material, selectedPurchaseOrder?.plantId ?: "")
                }
            }
        }
    }

    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        if (vm.dispatchWh.materialCode.isNullOrEmpty())
                            purchaseOrderList = it1 as MutableList<VegaCocoaPurchaseOrder>
                        else
                            purchaseOrderList =
                                it1.filter { it.materialCode == vm.dispatchWh.materialCode } as MutableList<VegaCocoaPurchaseOrder>
                        STONumbers = filterSTONumber()
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

    private fun fetchLotDetails(lotId: String, materialCode: List<String>, whId: String) {
        binding.etEnterContainer.hideKeyboard()
        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(lotId, materialCode, whId)
    }

    private fun filterWHFromPurchaseOrder() {
            for (item in purchaseOrderList) {
                purchaseOrder.addAll(item.purchaseOrders)
            }
            val location = purchaseOrder.map { it.warehouseId.plus("-").plus(getPlantName(it.warehouseId)) }.toSet()
            storageLocation.addAll(location)

        /*for (item in allPlants()) {
            storageLocation.add(item.plantId.plus("-").plus(getPlantName(item.plantId)))
        }*/
    }

    private fun filterSTONumber(): ArrayList<String> {
        val sto = purchaseOrder.distinctBy { it.purchaseDocNum } as ArrayList
        val purchaseList = ArrayList<String>()
        for (item in sto) {
            purchaseList.add(item.purchaseDocNum)
        }
        return purchaseList
    }
    private fun filterSTONumber(destWarehouse: String): List<String> {
        val sto = purchaseOrder.distinctBy { it.purchaseDocNum } as ArrayList
        val purchaseList = ArrayList<String>()
        for (item in sto) {
            if (item.warehouseId.equals(destWarehouse))
                purchaseList.add(item.purchaseDocNum)
        }
        return purchaseList
    }

    private fun getWeight(purchaseId: String) {
        selectedPurchaseOrder =
            purchaseOrder.single { s -> s.purchaseDocNum  == purchaseId }
    }

    private fun disableAddMoreLots(isEnable: Boolean) {
        binding.clScan.isEnabled = isEnable
        binding.etEnterContainer.isEnabled = isEnable
        binding.tvInventory.isEnabled = isEnable
    }

    private fun removeInventoryLot(isRemove: Boolean) {
        binding.tvInventory.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.tvOrInv.visibility = if (isRemove) View.GONE else View.VISIBLE
    }

    private fun removeScanLot(isRemove: Boolean) {
        binding.clScan.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.tvOr.visibility = if (isRemove) View.GONE else View.VISIBLE
    }

    private fun removeManualLot(isRemove: Boolean) {
        binding.etEnterContainer.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.btAdd.visibility = if (isRemove) View.GONE else View.VISIBLE
    }

    private fun prepareSummaryData(): VegaCocoaSummary {
        val duration = endTime?.minus(startTime ?: 0)
        val min = TimeUnit.MILLISECONDS.toMinutes(duration ?: 0)
        return VegaCocoaSummary(remark, min.toString(), vm.dispatchWh, vm.lots, isThirdPartyMaterial)
    }

    override fun itemRemoved(item: VegaCocoaDispatchLots) {
        vm.removeLotFromList(item.batchNumber, item.materialCode)
        vm.lots.remove(item)
        if (vm.lots.isEmpty()) disableAddMoreLots(true) else disableAddMoreLots(isMultipleLot)
    }

    private fun validateLotWeight(): Boolean {
        val selected = vm.lots.filter { !it.isLowerWeight }
        return selected.isEmpty()
    }

    private fun showLotAlreadyExistDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.already_added)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                dismiss()
            }
        }
    }

    private fun getPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }

    private fun allPlants(): List<Plant> {
        return Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
    }

    fun saveLotDetails() {
        if (vm.dispatchWh.purchaseDocNum != "")
            vm.saveWeighBridgeAndLotDetails()
    }

    private fun validateThirdPartyMaterialMapping(materialCode: String) {
        val material = thirdPartyMaterialList.filter { materialCode.contains(it.materialCode) }
        if (material.isNotEmpty()) {
            isThirdPartyMaterial = true
        }
    }

    private fun getMaterialList(): ArrayList<String> {
        val material = ArrayList<String>()
        material.add(vm.dispatchWh.materialCode ?: "")
        return material
    }

    private fun prepareLots(): ArrayList<VegaCocoaNoWeighmentLot> {
        val list = ArrayList<VegaCocoaNoWeighmentLot>()
        vm.lots.forEach {
            val data = VegaCocoaNoWeighmentLot().apply {
                batchNumber = it.batchNumber
                isAdded = it.isAdded
                weight = it.weight
                weighBridgeId = it.weighBridgeId
            }
            list.add(data)
        }
        return list
    }

    fun updateLotList(list: ArrayList<VegaCocoaNoWeighmentLot>) {
        val lots = ArrayList<VegaCocoaDispatchLots>()
        list.forEach {
            val mtntLot = VegaCocoaDispatchLots().apply {
                batchNumber = it.batchNumber
                weight = it.weight
                weightToDispatchUOM = it.weightToDispatchUOM
                unitOfMeasure = it.unitOfMeasure
                materialCode = it.materialCode
                materialName = it.materialName
                plantId = it.plantId
                plantName = it.plantName
                storageLocationCode = it.storageLocationCode
                processOrderNo = it.processOrderNo
                delivery = it.delivery
                deliveryItem = it.deliveryItem
                vendor = it.vendor
                vendorName = it.vendorName
            }
            lots.add(mtntLot)
        }
        val addedNew = ArrayList<VegaCocoaDispatchLots>()
        val removedLots = ArrayList<VegaCocoaDispatchLots>()
        val batchMap = vm.lots.map { it.batchNumber }
        val batchNewMap = lots.map { it.batchNumber }
        lots.forEach {
            if (!batchMap.contains(it.batchNumber)) {
                addedNew.add(it)
            }
        }
        lots.forEach {
            if (!batchNewMap.contains(it.batchNumber)) {
                removedLots.add(it)
            }
        }
        lots.removeAll(removedLots)

        for (item in removedLots) {
            vm.removeNoWeighmentFromList(item.batchNumber, item.materialCode)
        }
        addedNew.forEach {
            it.weighBridgeId = vm.dispatchWh.weighBridgeId.toString()
        }
        addedNew.forEach { updateAdapter(it) }

    }
}
