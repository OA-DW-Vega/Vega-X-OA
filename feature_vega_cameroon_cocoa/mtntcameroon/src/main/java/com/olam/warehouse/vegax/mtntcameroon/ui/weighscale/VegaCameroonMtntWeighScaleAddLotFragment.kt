package com.olam.warehouse.vegax.mtntcameroon.ui.weighscale

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.work.convertMtToKg
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcameroon.R
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonLotListModel
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonPurchaseOrders
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonWSBagModel
import com.olam.warehouse.vegax.mtntcameroon.databinding.FragmentCameroonMtntWsAddLotLayoutBinding
import com.olam.warehouse.vegax.mtntcameroon.databinding.ItemCameroonMaterialLayoutBinding
import com.olam.warehouse.vegax.mtntcameroon.databinding.ItemCameroonWeighscaleLotCardLayoutBinding
import com.olam.warehouse.vegax.mtntcameroon.ui.VegaCameroonMtntViewModel
import com.olam.warehouse.vegax.mtntcameroon.ui.VegaCameroonReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntcameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.concurrent.TimeUnit


class VegaCameroonMtntWeighScaleAddLotFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_cameroon_mtnt_ws_add_lot_layout
    private lateinit var binding: FragmentCameroonMtntWsAddLotLayoutBinding

    private val vm: VegaCameroonMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var isMultipleLot: Boolean = true

    private var selectedPurchaseOrder: VegaCameroonPurchaseOrders? = null

    private var startTime: Long? = 0
    private var endTime: Long? = null
    private var dispatchWbFromTruckList: VegaCocoaDispatchWB? = null
    private lateinit var callback: VegaCameroonReplaceFragmentCallback
    private var moreWeightBatches = ""
    private var palletCountNotMatchBatches = ""
    private var addWeightPosition = 0


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as VegaCameroonReplaceFragmentCallback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("mtntcameroon/ui/weighscale/VegaCameroonMtntWeighScaleAddLotFragment")
            .title("Vega_Cameroon/Mtnt").with(tracker)
    }

    companion object {
        fun newInstance(model: VegaCocoaDispatchWB) =
            VegaCameroonMtntWeighScaleAddLotFragment()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, model)
                }
    }

    override fun onResume() {
        super.onResume()
        val tvWhValue = requireActivity().findViewById<View>(R.id.tvWhValue)
        val tvSendingWHValue = requireActivity().findViewById<View>(R.id.tvSendingWHValue)
        tvWhValue!!.isFocusable = true
        tvWhValue.isFocusableInTouchMode = true
        tvWhValue.requestFocus()
        tvWhValue.visibility = View.GONE
        tvSendingWHValue.visibility = View.GONE
    }
    override fun onPause() {
        super.onPause()
        val tvWhValue = requireActivity().findViewById<View>(R.id.tvWhValue)
        val tvSendingWHValue = requireActivity().findViewById<View>(R.id.tvSendingWHValue)
        tvWhValue.visibility = View.VISIBLE
        tvSendingWHValue.visibility = View.VISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCameroonMtntWsAddLotLayoutBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }


    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        vm.dispatchWh = model ?: VegaCocoaDispatchWB()
        vm.getConfigItems(UserRoles.MTNT.role)
        dispatchWbFromTruckList = arguments?.getParcelable<VegaCocoaDispatchWB>(MODEL_BUNDLE) as VegaCocoaDispatchWB
        vm.weighBridgeWithLots.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.dispatchWh = it.dispatch
                vm.lotList.clear()
                vm.lotList.addAll(it.lineItems)
                vm.materialModelList.clear()
                vm.materialModelList.addAll(it.materialList)
                updateUIWithLocalData()
            }
        })
        vm.getMtntWithLots(model?.weighBridgeId ?: "")
        binding.tvTruckValue.requestFocus()
    }

    private fun updateUIWithLocalData() {
        selectedPurchaseOrder?.materialName = vm.dispatchWh.materialName ?: ""
        selectedPurchaseOrder?.materialCode = vm.dispatchWh.materialCode ?: ""
        selectedPurchaseOrder?.plantId = vm.dispatchWh.plantId
        setUpMaterialAdapter()
        refreshAdapter(vm.lotList)
        enableProceed(vm.lotList.isNotEmpty())
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        enableProceed(false)
        binding.tvDestValue.text =
            model?.recPlantId.plus("-").plus(model?.plantName)
        binding.tvTruckValue.text = model?.vehicleNumber
        if (!model?.erdat.isNullOrBlank() && model?.erdat?.contains(',') == true) {
            val times = model?.erdat?.split('(', ')')
            binding.tvDateValue.text = times?.get(1).let { it1 ->
                it1?.let { it2 ->
                    DateUtils.getUTCDateTime(
                        it2,
                        App.getAppContext()
                    )
                }
            }
        } else binding.tvDateValue.text = model?.erdat
        binding.tvStoNumberValue.text =
            model?.purchaseDocNum.plus("-").plus(model?.purchaseDocDesc)


        binding.clLotSummary.etEnterContainer.onChange { enableAddLot(it) }
        binding.clLotSummary.clScan.setOnClickListener { moveToScan() }
        binding.clLotSummary.btnAddLot.setOnClickListener {
            callback.replaceFragment(
                LOT_LIST,
                VegaCameroonLotListModel(selectedList = vm.lotList, isMultipleAdd = true, material = getMaterialList()),vm.dispatchWh
            )
        }
        binding.clLotSummary.btAdd.setOnClickListener { vm.validateLot(binding.clLotSummary.etEnterContainer.text.toString()) }
        binding.btnProceed.setOnClickListener { validateProceed() }


        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.clLotSummary.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    binding.clLotSummary.etEnterContainer.text.toString()
                )
        })

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        binding.clLotSummary.btAdd.setOnClickListener {
            vm.validateLot(binding.clLotSummary.etEnterContainer.text.toString())
        }

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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


    private fun fetchLotDetails(lotId: String) {
        binding.clLotSummary.etEnterContainer.hideKeyboard()
        binding.clLotSummary.etEnterContainer.setText("")
        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(lotId, getMaterialList(), getPlantDetails().plantId)
    }

    fun updateAddWeight(weight: VegaCameroonWSBagModel) {
        val split = weight.weight.split(" ")
        vm.lotList[addWeightPosition].editedWeight = split[0]
        vm.lotList[addWeightPosition].weightToDispatchUOM = split[1]
        binding.clLotSummary.rvList.adapter?.notifyItemChanged(addWeightPosition)
        var come :Int? = 0
        when(vm.lotList[addWeightPosition].unitOfMeasure){
            "KG" -> {
                come = split[0].toDouble().compareTo(vm.lotList[addWeightPosition].weight?.toDouble() ?: 0.0)
            }
            "MT" -> {
                come = split[0].toDouble()
                    .compareTo(convertMtToKg(vm.lotList[addWeightPosition].weight.toString()).toDouble())
            }
        }
        vm.lotList[addWeightPosition].isBagCountMatched = weight.isPalletMatched
        vm.lotList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
        vm.addLoTInDB(vm.lotList[addWeightPosition])
        calculateAndUpdateWeightToDispatch()
    }

    private fun calculateAndUpdateWeightToDispatch() {
        val materialWeightMap = HashMap<String, String>()
        vm.lotList.forEach {
            val data = materialWeightMap[it.materialCode]
            val editWeight =
                if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble()
            if (data != null) {
                val sum = data.toDouble().plus(editWeight!!)
                materialWeightMap[it.materialCode] = sum.toString()
            } else materialWeightMap[it.materialCode] = editWeight.toString()
        }
        vm.materialModelList.forEach { it.dispatchWeight = materialWeightMap[it.materialCode] }
        setUpMaterialAdapter()
        setUpAdapter(vm.lotList)
    }

    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.clLotSummary.btAdd,
                ContextCompat.getColorStateList(
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.clLotSummary.btAdd,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }


    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    fun updateLotList(list: ArrayList<VegaCocoaDispatchLots>) {
        if (vm.dispatchWh.startTime.isEmpty()) {
            startTime = System.currentTimeMillis()
            vm.dispatchWh.startTime = startTime.toString()
            updateStartTime()
        }
        val addedNew = ArrayList<VegaCocoaDispatchLots>()
        val removedLots = ArrayList<VegaCocoaDispatchLots>()
        val batchMap = vm.lotList.map { it.batchNumber }
        val batchNewMap = list.map { it.batchNumber }
        list.forEach {
            if (!batchMap.contains(it.batchNumber)) {
                addedNew.add(it)
            }
        }
        vm.lotList.forEach {
            if (!batchNewMap.contains(it.batchNumber)) {
                removedLots.add(it)
            }
        }
        vm.lotList.removeAll(removedLots)

        for (item in removedLots) {
            vm.removeLotFromList(item.batchNumber)
        }
        addedNew.forEach { it.weighBridgeId = model?.weighBridgeId.toString() }
        vm.addLoTInDB(addedNew)
        vm.lotList.addAll(addedNew)
        setUpAdapter(vm.lotList)
        enableProceed(vm.lotList.isNotEmpty())
    }

    private fun enableProceed(enable: Boolean) {
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    private fun validateLotWeight(): Boolean {
        val selected = vm.lotList.filter { !it.isLowerWeight }
        moreWeightBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun validatePalletCount(): Boolean {
        val selected = vm.lotList.filter { !it.isBagCountMatched }
        palletCountNotMatchBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun validateMorethanMaterialWeight(): Boolean {
        val list = ArrayList<Boolean>()
        vm.materialModelList.forEach {
            val come: Int? = it.dispatchWeight?.toDouble()!!.compareTo(it.soWeight?.toDouble() ?: 0.0)
            list.add(come ?: 0 <= 0)
        }
        return list.none { true }
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            vm.lotList.filter { it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals("") }
        return emptyWeight.isEmpty()
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_SELECTION_MULTI.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isMultipleLot = true
                        it.applicable?.contains("N")!! -> {
                            isMultipleLot = false
                        }
                    }
                }
            }
        }
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
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
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
        binding.clLotSummary.etEnterContainer.setText("")
        vegaCocoaDispatchLots.weighBridgeId = vm.dispatchWh.weighBridgeId
        vm.lotList.add(vegaCocoaDispatchLots)
        vm.addLoTInDB(vegaCocoaDispatchLots)
        setUpAdapter(vm.lotList)
    }

    private fun refreshAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
        setUpAdapter(list)
        calculateAndUpdateWeightToDispatch()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    fetchLotDetails(it)
                }
            }
        }
    }


    private fun updateStartTime() {
        vm.updateStartLoading()
    }

    private fun setUpMaterialAdapter() {
        binding.rvMaterialList.setUpAdapter(
            vm.materialModelList,
            R.layout.item_cameroon_material_layout,
            ItemCameroonMaterialLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialName.text = it.materialName
                when (it.uom) {
                    "KG" ->
                        bindItem.tvStoWeightValue.text =
                            it.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                                .plus(it.uom)
                    "MT" ->
                        bindItem.tvStoWeightValue.text =
                            convertMtToKg(it.soWeight.toString()).toDouble().formatThreeDigits()
                                .replace(",", "").plus(" ")
                                .plus("KG")
                }

                bindItem.tvDispatchWeightValue.text = it.dispatchWeight?.plus(" KG")
                bindItem.llTotalWeightLoss.visibility = View.GONE
            })
    }

    private fun validateProceed() {
        if (vm.lotList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    if (validatePalletCount())
                        showRemarkDialog()
                    else Toast.makeText(
                        activity,
                        getString(R.string.pallet_not_matched).plus(" ").plus(palletCountNotMatchBatches),
                        Toast.LENGTH_SHORT
                    ).show()
                } else Toast.makeText(
                    activity,
                    getString(R.string.lot_more_weight).plus(" - ").plus(moreWeightBatches),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                activity,
                getString(R.string.less_weight_error),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                activity,
                getString(R.string.please_add_lot),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
        val lots = list
        binding.clLotSummary.rvList.setUpAdapter(
            lots,
            R.layout.item_cameroon_weighscale_lot_card_layout,
            ItemCameroonWeighscaleLotCardLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.batchNumber
                bindItem.tvStLocationValue.text = item.storageLocationCode
                when (item.unitOfMeasure) {
                    "KG" ->
                        bindItem.tvScaleWeightValue.text =
                            item.weight?.toDouble()?.formatThreeDigits().plus(" ")
                                .plus(item.unitOfMeasure)
                    "MT" ->
                        bindItem.tvScaleWeightValue.text =
                            convertMtToKg(item.weight.toString()).toDouble().formatThreeDigits()
                                .plus(" ").plus("KG")
                }

                bindItem.tvScaleGradeValue.text = item.materialName
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits()
                bindItem.tvScaleDispatchValue.text = editedWeight
                bindItem.tvDispatchUOMValue.text = item.weightToDispatchUOM
                bindItem.ivScaleClose.setOnClickListener {
                    showConformationDialog(pos, bindItem.ivScaleClose)
                }
                bindItem.tvAddWeight.setOnClickListener {
                    addWeightPosition = pos
                    callback.replaceFragment(ADD_WEIGHT, item)
                }
                bindItem.cbEndLot.isChecked = item.isEndLot ?: false
                bindItem.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
                    item.isEndLot = isChecked
                }
            })
    }

    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                view.context.getString(R.string.proceed),
                view.context.getString(R.string.cancel),
                {
                    vm.removeLotFromList(vm.lotList[position].batchNumber)
                    vm.lotList.removeAt(position)
                    binding.clLotSummary.rvList.adapter?.notifyItemRemoved(position)
                },
                { dismiss() })
        }
    }


    private fun getMaterialList(): ArrayList<String> {
        val material = ArrayList<String>()
        if (vm.materialModelList.size > 0) {
            vm.materialModelList.forEach { material.add(it.materialCode) }
        }
        return material
    }

    private fun showRemarkDialog() {

        showDialog(getString(R.string.end_load_msg), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else {
                    vm.dispatchWh.startTime = startTime.toString()
                    endTime = System.currentTimeMillis()
                    vm.dispatchWh.endTime = endTime.toString()
                    val duration = endTime?.minus(startTime ?: 0)
                    vm.dispatchWh.remarks = remark
                    vm.dispatchWh.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration ?: 0).toString()
                    vm.saveWeighBridgeAndLotDetails()

                    callback.replaceFragment(WEIGHSCALE_SUMMARY, vm.dispatchWh)
                }
            }

        }, true, vm.dispatchWh.remarks)
    }
}
