package com.olam.warehouse.vegax.dispatchnigeria.ui.weighscale


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
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.work.convertKgToMT
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatchnigeria.R
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.*
import com.olam.warehouse.vegax.dispatchnigeria.databinding.FragmentNigeriaCocoaMtntWsAddLotLayoutBinding
import com.olam.warehouse.vegax.dispatchnigeria.databinding.ItemNigeriaCocoaMaterialLayoutBinding
import com.olam.warehouse.vegax.dispatchnigeria.databinding.ItemNigeriaCocoaWeighscaleLotCardLayoutBinding
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntViewModel
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaReplaceFragmentCallback
import com.olam.warehouse.vegax.dispatchnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class VegaNigeriaCocoaMtntWeighScaleAddLotFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_nigeria_cocoa_mtnt_ws_add_lot_layout
    private lateinit var binding: FragmentNigeriaCocoaMtntWsAddLotLayoutBinding
    private lateinit var vegaNigeriaCocoaMtntBinMerge: VegaNigeriaCocoaMtntBinMerge
    private val vm: VegaNigeriaCocoaMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var weightToProcess = 0.0
    private var isMultipleLot: Boolean = true
    private var materialCode: String = ""
    private var grossWeightValue: Double = 0.0

    //private var updatedgrossWeightValue: Double = 0.0
    private var storageLocation = ArrayList<String>()
    private var selectedPurchaseOrder: VegaNigeriaSesameMtntPurchaseOrders? = null
    private var isEditableLot = true
    private var purchaseOrder = ArrayList<VegaNigeriaSesameMtntPurchaseOrders>()
    private var remark: String = ""
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var dispatchWbFromTruckList: VegaCocoaDispatchWB? = null
    private lateinit var callback: VegaNigeriaCocoaReplaceFragmentCallback
    private var moreWeightBatches = ""
    private var mergedBatchNumber = ""
    private var mergedTotalNetWeight = ""
    private var mergedType = ""
    private var plantId = ""
    private var addWeightPosition = 0
    private var vegaCocoaMtntWithLots: VegaCocoaMtntWithLots? = null
    private var thirdPartyMaterials: List<VegaCoffeeThirdPartyMaterialDetail>? = null
    private var grnProcessType: String? = ""
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var weightedAvgList = arrayListOf<VegaNigeriaCocoaWeightedAverageDetails>()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private var jsonData = mutableListOf<String>()
    private var updateWeightFlag = false
    private var bin_mergebatchnumber = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as VegaNigeriaCocoaReplaceFragmentCallback
    }

    companion object {
        fun newInstance(model: VegaCocoaDispatchWB) =
            VegaNigeriaCocoaMtntWeighScaleAddLotFragment()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, model)
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaCocoaMtntWsAddLotLayoutBinding.inflate(layoutInflater)
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
                it.dispatch.imagesList = model?.imagesList!!
                it.dispatch.imagePath = model?.imagePath
                it.dispatch.encodedImageContent = model?.encodedImageContent
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
            model?.recPlantId.plus("-").plus(model?.plantName)
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
                VegaNigeriaCocoaMtntLotListModel(selectedList = vm.lotList, isMultipleAdd = true, material = getMaterialList())
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

        vm.getWeighBridgeWithLotAndMaterial(vm.dispatchWh.weighBridgeId)
        vm.weighBridgeWithLotsSource.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                it.dispatch.imagesList = model?.imagesList!!
                it.dispatch.imagePath = model?.imagePath
                it.dispatch.encodedImageContent = model?.encodedImageContent
                vm.dispatchWh = it.dispatch
//                vm.lotList.clear()
//                vm.lotList.addAll(it.lineItems)
//                vm.materialModelList.clear()
//                vm.materialModelList.addAll(it.materialList)
//                vegaCocoaMtntWithLots = it
//                updateLocalDbData(it)
                if(!updateWeightFlag) {
                    vm.lotList.clear()
                    vm.lotList.addAll(it.lineItems)
                    vm.materialModelList.clear()
                    vm.materialModelList.addAll(it.materialList)
                    vegaCocoaMtntWithLots = it
                    updateLocalDbData(it)
                } else {
                    vm.materialModelList.clear()
                    vm.materialModelList.addAll(it.materialList)
                    vegaCocoaMtntWithLots?.lineItems = vm.lotList
                    if(vegaCocoaMtntWithLots?.lineItems?.isNotEmpty() == true) updateLocalDbData(vegaCocoaMtntWithLots!!)
                }
            }
        })

        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            thirdPartyMaterials = it
        })
        vm.getThirdPartyMaterials()

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        vm.wsWeightedAvgPost.observe(viewLifecycleOwner, Observer { updateWeightedAvgUI(it) })

        vm.wsdeliveryBinMergePost.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<MergedData>>) {

        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let { it1 ->
                    mergedBatchNumber = it1.batchNumber.toString()
                    mergedTotalNetWeight = it1.totalNetWeight.toString()
                    mergedType = it1.type.toString()
                    showRemarkDialog()
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun updateWeightedAvgUI(response: Resource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>) {

        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let { it1 ->
                    if (it1.weightedAvg.isNullOrEmpty()) {
                        showErrorDialogWithFAQLink(requireContext(), "Weighted Average value is Empty")
                    } else {
                        plantId = it1.imWerks.toString()
                        weightedAvgList =
                            it1.weightedAvg as ArrayList<VegaNigeriaCocoaWeightedAverageDetails>

                        if (dispatchLotsList.size > 0) {
                            callback.replaceFragment(
                                PARAMS_LIST,
                                dispatchLotsList.get(0).materialCode,
                                mergedBatchNumber,
                                vm.dispatchWh,
                                weightedAvgList
                            )
                        }
                    }
                    // callback.replaceFragment(WEIGHSCALE_SUMMARY, vm.dispatchWh)
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        var processTypeList = mutableListOf<VegaNigeriaCocoaMtntProcessType>()
        jsonData.forEach {
            if (it.contains(JSON_PROCESS_TYPE_LIST)) {
                val processType =
                    gson.fromJson(it, VegaNigeriaCocoaMtntProcessTypeModel::class.java)
                grnProcessType = processType.PROCESS_TYPE_LIST[0].MTNT
            }
        }

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

    private fun updateLocalDbData(weighBridge: VegaCocoaMtntWithLots) {
        dispatchLotsList.clear()
        /*if (weighBridge.lineItems.size == 1) {
            binding.tvText.visibility = View.GONE
        } else
            binding.tvText.visibility = View.VISIBLE
        setUpAdapter(weighBridge.lineItems)*/
        setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
        calculateAndUpdateWeightToDispatch()
        vm.bagItems.observe(viewLifecycleOwner, Observer { getBagList(it) })
        vm.getBagItems()
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }

    private fun fetchLotDetails(lotId: String) {
        binding.clLotSummary.etEnterContainer.hideKeyboard()
        binding.clLotSummary.etEnterContainer.setText("")
        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(
            lotId,
            getMaterialList(),
            getPlantDetails().plantId
        )
    }

    fun updateAddWeight(weight: String, grossWeight: String) {
        updateWeightFlag = true
        val split = weight.split(" ")
        val splitNew = grossWeight.split(" ")
        grossWeightValue = (/*(grossWeightValue) + */(splitNew[0].toDouble()))
        vm.lotList[addWeightPosition].region = split[0]
        vm.lotList[addWeightPosition].editedWeight = split[0]
        vm.lotList[addWeightPosition].weightToDispatchUOM = split[1]
        vm.lotList[addWeightPosition].grossWeight = splitNew[0]
        binding.clLotSummary.rvList.adapter?.notifyItemChanged(addWeightPosition)
        val come: Int? =
            split[0].toDouble().compareTo(vm.lotList[addWeightPosition].weight?.toDouble() ?: 0.0)
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
    }

    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.clLotSummary.btAdd,
                ContextCompat.getColorStateList(requireActivity(), com.olam.warehouse.presentation.R.color.green)
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
        updateWeightFlag = false
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
                        it1.forEachIndexed { index, s ->
                            var tareWeightBag =  if ((it1[index].unitOfMeasure).equals("KG")) convertKgToMT((it1[index].weight).toString().trim()) else (it1[index].weight).toString().trim()
                            /*it1[index].weight =
                                convertMtToKg(it1[index].weight.toString(), it1[index].unitOfMeasure.toString())*/
                            it1[index].weight = tareWeightBag
                            it1[index].oldBatchNumber =  it1[index].batchNumber
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
        enableProceed(true)
        vm.saveLotDetails(vegaCocoaDispatchLots)

    }

    private fun refreshAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
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
            R.layout.item_nigeria_cocoa_material_layout,
            ItemNigeriaCocoaMaterialLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvStoWeightValue.text =
                    it.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus(it.uom)
                bindItem.tvDispatchWeightValue.text = it.dispatchWeight?.plus(it.uom)
                bindItem.llTotalWeightLoss.visibility = View.GONE
            })
    }

    private fun validateProceed() {
        if (vm.lotList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    /*if (validateMorethanMaterialWeight())*/

                    vegaNigeriaCocoaMtntBinMerge = VegaNigeriaCocoaMtntBinMerge(
                        getCurrentKey(),
                        getPlantDetails(),
                        prepareMergedDeliveryList()
                    )
                    vm.postWeighScaleDeliveryBinMerge(vegaNigeriaCocoaMtntBinMerge)


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

    private fun prepareMergedDeliveryList(): List<VegaNigeriaCocoaMtntDispatchLotsBinMerge> {

        var list = ArrayList<VegaNigeriaCocoaMtntDispatchLotsBinMerge>()

        //val deliveryDetail = VegaNigeriaCocoaMtntDispatchLotsBinMerge()

        var currentDate = DateUtils.formatDate(DateUtils.getDate())
        var currentMonth = currentDate.split("-")[1]
        var currentYear = (currentDate.split("-")[0])
        var twoDigitsYear = currentYear.substring(currentYear.length - 2)
        var loggedInPlantId = ((PreferenceHelper.get(
            Constants.WERKS,
            ""
        )).substring((PreferenceHelper.get(Constants.WERKS, "")).length - 2))
        var qualityCode = getQualityCode(291.0, 6.0, 3.4)
        for (item in dispatchLotsList) {

            var twoDigitsLocationCode =
                (item.storageLocationCode.toString()).substring((((item.storageLocationCode.toString())).length) - 2)
            var mergedBatchNumber = twoDigitsYear.plus(currentMonth).plus(qualityCode).plus("-")
                .plus(twoDigitsLocationCode)
            // var mergedBatchNumber = loggedInPlantId.plus(twoDigitsYear).plus(currentMonth).plus(qualityCode).plus("-")
            item.binMergedBatchNumber = mergedBatchNumber
            var deliveryDetail = VegaNigeriaCocoaMtntDispatchLotsBinMerge()
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.postingDate = ""
            var tareWeightBag =  if ((item.unitOfMeasure).equals("KG")) convertKgToMT((item.editedWeight).toString().trim()) else (item.editedWeight).toString().trim()
            deliveryDetail.netWeight = tareWeightBag
           /* deliveryDetail.netWeight =
                convertKgToMT(item.editedWeight, item.unitOfMeasure.toString())*/
            //deliveryDetail.netWeight =item.editedWeight
            deliveryDetail.recStorageLocationCode = vm.dispatchWh.recStorageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            //item.unitOfMeasure = "MT"
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            /*deliveryDetail.unitsOfMeasure = UNIT_KG*/
            deliveryDetail.storageLossFlag = false
            deliveryDetail.mergedBatchNumber = mergedBatchNumber.toString()
            list.add(deliveryDetail)
        }
        return list
    }

    private fun generateBatchNumber(): String {
        var productTypeCode = ""

        thirdPartyMaterials?.forEach { item ->
            if (item.materialName.toString()
                    .contains(dispatchLotsList[0].materialName.toString(), true)
            ) {
                productTypeCode = item.typeCode.toString()
            }
        }
        var year = Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(1)
        return dispatchLotsList[0].plantId?.takeLast(2)
            .plus(dispatchLotsList[0].storageLocationCode?.takeLast(1))
            .plus(year).plus(grnProcessType).plus(productTypeCode.takeLast(1))
    }

    private fun setUpAdapter(list: List<VegaCocoaDispatchLots>) {
        val lots = list as MutableList
        binding.clLotSummary.rvList.setUpAdapter(
            lots,
            R.layout.item_nigeria_cocoa_weighscale_lot_card_layout,
            ItemNigeriaCocoaWeighscaleLotCardLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.batchNumber
                bindItem.tvStLocationValue.text = item.storageLocationCode
                var tareWeightBag = if ((item.unitOfMeasure).equals("KG")) convertKgToMT(
                    (item.weight).toString().trim()
                ) else (item.weight).toString().trim()
                /*tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus("MT")*/
                bindItem.tvScaleWeightValue.text =
                    tareWeightBag.toDouble().formatThreeDigits().plus(item.unitOfMeasure)
                bindItem.tvScaleGradeValue.text = item.materialName
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits()
                bindItem.tvScaleDispatchValue.text = editedWeight
                bindItem.tvDispatchUOMValue.text = item.unitOfMeasure/*item.weightToDispatchUOM*/
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
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
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
                    if(startTime==null){
                        startTime = System.currentTimeMillis()
                        vm.dispatchWh.startTime = startTime.toString()
                    }

                    endTime = System.currentTimeMillis()
                    vm.dispatchWh.endTime = endTime.toString()
                    val duration = endTime?.minus(startTime ?: 0)
                    vm.dispatchWh.remarks = remark
                    vm.dispatchWh.grossWeight = grossWeightValue.toString()
                    vm.dispatchWh.batchNumber = mergedBatchNumber
                    vm.dispatchWh.netWeight = mergedTotalNetWeight
                    vm.dispatchWh.imagesList = model?.imagesList!!
                    vm.dispatchWh.imagePath = model?.imagePath
                    vm.dispatchWh.turnAroundTime =
                        TimeUnit.MILLISECONDS.toMinutes(duration ?: 0).toString()
                    vm.saveWeighBridgeAndLotDetails()

                    //callback.replaceFragment(WEIGHSCALE_SUMMARY, vm.dispatchWh)

                    var list = ArrayList<VegaNigeriaCocoaWeightedAverageDeliveryDetail>()
                    for (item in dispatchLotsList) {
                        var deliveryDetail = VegaNigeriaCocoaWeightedAverageDeliveryDetail()
                        deliveryDetail.batchNumber = item.batchNumber
                        deliveryDetail.materialCode = item.materialCode
                        deliveryDetail.qualityDetails = emptyList()
                        list.add(deliveryDetail)
                    }

                    vm.postWeightedAverage(
                        (VegaNigeriaCocoaWeightedAveragePost(
                            key = getCurrentKey(),
                            batchUpdateFlag = false,
                            weightedAvgFlag = true,
                            plant = getPlantDetails(),
                            deliveryDetails = list
                        ))
                    )
                }
            }

        }, true, vm.dispatchWh.remarks)
    }
}
