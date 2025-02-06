package com.olam.warehouse.vegax.mtntghana.ui.weighscale


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
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
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
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntLotListModel
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntPurchaseOrders
import com.olam.warehouse.vegax.mtntghana.databinding.FragmentGhanaMtntWsAddLotLayoutBinding
import com.olam.warehouse.vegax.mtntghana.databinding.ItemGhanaMaterialLayoutBinding
import com.olam.warehouse.vegax.mtntghana.databinding.ItemGhanaWeighscaleLotCardLayoutBinding
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaMtntViewModel
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntghana.utils.LOT_LIST
import com.olam.warehouse.vegax.mtntghana.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.mtntghana.utils.WEIGHSCALE_SUMMARY
import com.olam.warehouse.vegax.mtntghana.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class VegaGhanaMtntWeighScaleAddLotFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_ghana_mtnt_ws_add_lot_layout
    private lateinit var binding: FragmentGhanaMtntWsAddLotLayoutBinding

    private val vm: VegaGhanaMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var weightToProcess = 0.0
    private var isMultipleLot: Boolean = true
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()
    private var selectedPurchaseOrder: VegaGhanaMtntPurchaseOrders? = null
    private var isEditableLot = true
    private var purchaseOrder = ArrayList<VegaGhanaMtntPurchaseOrders>()
    private var remark: String = ""
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var dispatchWbFromTruckList: VegaCocoaDispatchWB? = null
    private lateinit var callback: VegaGhanaReplaceFragmentCallback
    private var moreWeightBatches = ""
    private var addWeightPosition = 0
    private var dispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as VegaGhanaReplaceFragmentCallback
    }

    companion object {
        fun newInstance(model: VegaCocoaDispatchWB) =
            VegaGhanaMtntWeighScaleAddLotFragment()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, model)
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaMtntWsAddLotLayoutBinding.inflate(layoutInflater)
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
        enableProceed(false)
        binding.tvDestValue.text =
            model?.recPlantId.plus("-").plus(model?.plantName).plus("-").plus(model?.destinationWH).plus("-").plus(model?.destinationWHName)
        binding.tvSendingWHValueValue.text = model?.storageLocationCode.toString()
        binding.tvMaterialValue.text = model?.materialName
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
                VegaGhanaMtntLotListModel(selectedList = vm.lotList, isMultipleAdd = true, material = getMaterialList())
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

    fun updateAddWeight(weight: String) {
        val split = weight.split(" ")
        vm.lotList[addWeightPosition].editedWeight = split[0]
        vm.lotList[addWeightPosition].weightToDispatchUOM = "MT"
        binding.clLotSummary.rvList.adapter?.notifyItemChanged(addWeightPosition)
        val come: Int? = split[0].toDouble().compareTo(vm.lotList[addWeightPosition].weight?.toDouble() ?: 0.0)
        vm.lotList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
        vm.addLoTInDB(vm.lotList[addWeightPosition])
        // calculateAndUpdateWeightToDispatch()
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

        vm.materialModelList.forEach {
            it.dispatchWeight = materialWeightMap[it.materialCode]
        }
        setUpMaterialAdapter()
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

    fun updateLotList(list: ArrayList<VegaGhanaCocoaDispatchLots>) {
        if (vm.dispatchWh.startTime.isEmpty()) {
            startTime = System.currentTimeMillis()
            vm.dispatchWh.startTime = startTime.toString()
            updateStartTime()
        }
        val addedNew = ArrayList<VegaGhanaCocoaDispatchLots>()
        val removedLots = ArrayList<VegaGhanaCocoaDispatchLots>()
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

    private fun validateMorethanMaterialWeight(): Boolean {
        val list = ArrayList<Boolean>()
        vm.materialModelList.forEach {
            if ((it.dispatchWeight?.toDouble() ?: 0.0) > (it.soWeight?.toDouble() ?: 0.0)) {
                val result = true
                list.add(result)
            }
//            val come: Int? = it.dispatchWeight?.toDouble()!!.compareTo(it.soWeight?.toDouble() ?: 0.0)
//            list.add(come ?: 0 <= 0)
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

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        it1.forEachIndexed { index, s ->
                            it1[index].weight = it1[index].weight.toString()
                        }
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

    private fun chooseOneLotDialog(lots: List<VegaGhanaCocoaDispatchLots>) {
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

    private fun updateAdapter(vegaCocoaDispatchLots: VegaGhanaCocoaDispatchLots) {
        binding.clLotSummary.etEnterContainer.setText("")
        vegaCocoaDispatchLots.weighBridgeId = vm.dispatchWh.weighBridgeId
        vm.lotList.add(vegaCocoaDispatchLots)
        vm.addLoTInDB(vegaCocoaDispatchLots)
        setUpAdapter(vm.lotList)
        enableProceed(true)
//        vm.saveLotDetails(vegaCocoaDispatchLots)
        vm.saveLot(vegaCocoaDispatchLots)

    }

    private fun refreshAdapter(list: ArrayList<VegaGhanaCocoaDispatchLots>) {
        setUpAdapter(list)
        enableProceed(true)
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
            R.layout.item_ghana_material_layout,
            ItemGhanaMaterialLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvStoWeightValue.text =
                    it.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus("MT")
                bindItem.tvDispatchWeightValue.text = it.dispatchWeight?.plus(" MT")
                bindItem.llTotalWeightLoss.visibility = View.GONE
            })
    }

    private fun validateProceed() {
        if (vm.lotList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    //if (validateMorethanMaterialWeight())
                    // showRemarkDialog()
                    endTime = System.currentTimeMillis()
                    vm.dispatchWh.endTime = endTime.toString()
                    val duration = endTime?.minus(startTime ?: 0)
                    //vm.dispatchWh.remarks = remark
                    vm.dispatchWh.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration
                            ?: 0).toString()
                    vm.saveWeighBridgeAndLotDetails()
                    callback.replaceFragment(WEIGHSCALE_SUMMARY, vm.dispatchWh)
                    /*else Toast.makeText(
                        activity,
                        getString(R.string.morethan_so_weight),
                        Toast.LENGTH_SHORT
                    ).show()*/
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

    private fun setUpAdapter(list: ArrayList<VegaGhanaCocoaDispatchLots>) {
        val lots = list
        binding.clLotSummary.rvList.setUpAdapter(
            lots,
            R.layout.item_ghana_weighscale_lot_card_layout,
            ItemGhanaWeighscaleLotCardLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.batchNumber
                bindItem.tvStLocationValue.text = item.storageLocationCode
                bindItem.tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus("MT")
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
                /*   tv_add_weight.setOnClickListener {
                   addWeightPosition = pos
                   callback.replaceFragment(ADD_WEIGHT, item)
               }*/
                bindItem.etWeight.setText(editedWeight)

                bindItem.etWeight.onChange {
                    addWeightPosition = pos
                    if (!bindItem.etWeight.text.toString()
                            .equals("") || !bindItem.etWeight.text.isNullOrEmpty()
                    ) {
                        var grossWeight = if(bindItem.etWeight.text.isNotEmpty())bindItem.etWeight.text.toString().toDouble() else 0.0
                        vm.lotList[addWeightPosition].editedWeight =
                            grossWeight.formatThreeDigits().replace(",", "")
                        val come: Int? = grossWeight.compareTo(
                            vm.lotList[addWeightPosition].weight?.toDouble() ?: 0.0
                        )
                        vm.lotList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
                        item.editedWeight = bindItem.etWeight.text.toString()
                    } else {
                        vm.lotList[addWeightPosition].editedWeight = ""
                        vm.lotList[addWeightPosition].isLowerWeight = true
                        showSnack(getString(com.olam.warehouse.login.R.string.enter_net))
                    }

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
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.removeGhanaLotFromList(
                        vm.lotList[position].batchNumber,
                        vm.lotList[position].weighBridgeId
                    )
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

    private fun itemRemoved(pos: Int) {
        vm.removeLotFromList(vm.lotList[pos].batchNumber)
        vm.lotList.removeAt(pos)
        binding.clLotSummary.rvList.adapter?.notifyItemRemoved(pos)
        calculateAndUpdateWeightToDispatch()
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
