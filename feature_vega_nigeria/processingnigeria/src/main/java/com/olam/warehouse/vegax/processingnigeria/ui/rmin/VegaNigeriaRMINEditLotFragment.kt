package com.olam.warehouse.vegax.processingnigeria.ui.rmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentNigeriaRminAddLotBinding
import com.olam.warehouse.vegax.processingnigeria.utils.FRAG_ADD_WEIGHT
import com.olam.warehouse.vegax.processingnigeria.utils.Lot_List
import com.olam.warehouse.vegax.processingnigeria.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNigeriaRMINEditLotFragment : BaseFragment(), NigeriaRminItemRemoveListener,
    NigeriaRminAddWeightListener {

    override val layoutResourceId = R.layout.fragment_nigeria_rmin_add_lot
    private lateinit var binding: FragmentNigeriaRminAddLotBinding

    private var callBack: CallBack? = null
    private val vm: VegaNigeriaRminViewModel by viewModel()
    private var model: VegaCoffeeRminProcessing? = null
    private var lotsFromInventory = ArrayList<VegaCoffeeRminLots>()
    private var weightToProcess = 0.0
    private var previousValue = 0.0
    private var isMultipleLot: Boolean = true
    private val isRmin: Boolean = true
    private var gradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var adapter: VegaNigeriaRminLotAdapter? = null
    private var addWeightPosition = 0
    private var selectedItemPosition: Int = 0
    private var noOfGradeItems: Int = 0
    private var currentMaterial = ""
    private var currentMaterialName = ""
    private var fgrnIdMaterialCode: String? = ""
    private var isThirdPartyMaterial = false
    private var moreWeightBatches = ""

    interface CallBack {
        fun replaceFragment(
            fragment: String,
            flag: Boolean,
            model: VegaCoffeeRminProcessing,
            materialCode: String,
            batchNo: String = ""
        )

        fun replaceFragment(
            fragment: String,
            flag: Boolean,
            model: VegaCoffeeRminProcessing,
            materialCode: String,
            batchNo: String, isThirdParty: Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(fgrnIdMaterialCode: String, model: VegaCoffeeRminProcessing) =
            VegaNigeriaRMINEditLotFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putString("material", fgrnIdMaterialCode)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaRminAddLotBinding.inflate(layoutInflater)
        initExtra()
        initAdapter()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        fgrnIdMaterialCode = arguments?.getString("material")
        if (model?.gradeListDetails.isNullOrEmpty()) {
            currentMaterial = model?.materialCode.toString()
        } else {
            gradeList =
                Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(model?.gradeListDetails ?: "")

            gradeList.forEachIndexed { index, vegaCoffeeFgrnItemsGrades ->
                if (vegaCoffeeFgrnItemsGrades.fgrnIdMaterialCode == fgrnIdMaterialCode) selectedItemPosition =
                    index
            }
            currentMaterialCode()
        }
        vm.getLotInfo(model?.poNumber ?: "", currentMaterial)
        vm.lotsInfoForMaterial.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) updateLotFromLocalDB(it as ArrayList<VegaCoffeeRminLots>)
        })
    }

    private fun initUI() {
        binding.cvCard.tvBomValue.text = model?.bom
        binding.cvCard.tvMaterialValue.text = model?.materialName
        binding.cvCard.tvStageValue.text = model?.stage
        binding.cvCard.tvPoNoValue.text = model?.poNumber
        binding.btnProceed.text = getString(R.string.proceed)
        if (model?.gradeListDetails.isNullOrEmpty()) {
            binding.cvCard.tvMaterialValue.text = model?.materialName.toString()
        } else {
            binding.cvCard.tvMaterialValue.text = gradeList[selectedItemPosition].materialName
        }
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.btAdd.setOnClickListener { vm.validateLot(binding.etEnterContainer.text.toString()) }
        binding.clScan.setOnClickListener { moveToScan() }
        binding.cvCard.tvProceesValue.text = model?.weightToProcess ?: ""

        if (model?.poNumber?.isNotEmpty()!!) {
            binding.cvCard.tvBomValue.invisible()
            binding.cvCard.tvBom.invisible()
            binding.cvCard.tvPoNoValue.visible()
            binding.cvCard.tvPoNo.visible()
        } else {
            binding.cvCard.tvBomValue.visible()
            binding.cvCard.tvBom.visible()
            binding.cvCard.tvPoNoValue.invisible()
            binding.cvCard.tvPoNo.invisible()
        }

        if ((model?.poNumber?.isNotEmpty()) == true) {
            binding.linearOne.visibility = View.GONE
        } else {
            binding.linearOne.visibility = View.VISIBLE
            binding.tvPoQuantityLabel.text =
                with(UIUtils) { with(requireContext().resources.getString(R.string.po_quantity_text)) { mandatoryStars() } }
        }

        binding.btnAddLot.setOnClickListener { navigateToLotList() }
        if (isThirdPartyMaterial) {
            binding.cvCard.tvBom.text = getString(R.string.vendor)
            binding.cvCard.tvBomValue.text = model?.vendor
            binding.cvCard.tvBom.visible()
            binding.cvCard.tvBomValue.visible()

        }
        binding.btnProceed.setOnClickListener {
            validateProceed()
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.PROCESSING.role)
        vm.poGradeList.observe(viewLifecycleOwner, Observer { updateFgrnGradeUI(it) })
        if (model?.poNumber?.isNotEmpty()!!) vm.getPoGrades(model?.poNumber.toString(), isRmin)

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.etEnterContainer.setText("")
            } else {
                currentMaterialCode()
                fetchLotDetails(
                    binding.etEnterContainer.text.toString(),
                    currentMaterial, getPlantDetails().plantId
                )
            }
        })
    }

    private fun validateProceed() {
        if (binding.tvPoQuantity.text.toString()
                .isNullOrEmpty() && (model?.poNumber?.isNotEmpty()) == false
        ) {
            Toast.makeText(
                activity,
                getString(R.string.please_add_po_quantity),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (vm.lotList.size > 0) {
                if (validateEmptyWeight()) {
                    if (validateLotWeight()) {
                        model?.lotList?.clear()
                        model?.weightToProcess = binding.cvCard.tvProceesValue.text.toString()
                        if ((model?.poNumber?.isNotEmpty()) == true) {
                            model?.poQuantity = ""
                        } else {
                            model?.poQuantity = binding.tvPoQuantity.text.toString()
                        }
                        model?.lotList?.addAll(vm.lotList)
                        if (model?.gradeListDetails.isNullOrEmpty()) {
                        } else {
                            gradeList.get(selectedItemPosition).weightToProcess =
                                binding.cvCard.tvProceesValue.text.toString()
                            val gson = Gson().toJson(gradeList)
                            model?.gradeListDetails = gson
                        }
                        vm.saveWeighBridgeAndLotDetails(model?.lotList ?: ArrayList(), model!!)
                        if (model?.gradeListDetails.isNullOrEmpty()) {
                        } else {
                            vm.updateGradeWeightInfo(
                                binding.cvCard.tvProceesValue.text.toString(),
                                gradeList.get(selectedItemPosition).fgrnId,
                                gradeList.get(selectedItemPosition).materialCode,
                                gradeList.get(selectedItemPosition).processOrderNo
                            )
                        }
                        activity?.supportFragmentManager?.popBackStackImmediate()
                        //callBack?.replaceFragment(SUMMARY, isMultipleLot, model!!, "")
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

    fun moveToShiftSelect() {
        if (vm.lotList.isNotEmpty())
            validateProceed()
        else showSnack(getString(R.string.please_assign_lot))
    }

    private fun fetchLotDetails(lotId: String, materialCode: String, whId: String) {
        binding.etEnterContainer.hideKeyboard()
        binding.etEnterContainer.setText("")
        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(lotId, materialCode, whId)
    }


    private fun updateFgrnGradeUI(response: Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                val gradeList =
                                    it.filter { item -> item.materialCode.contains(model?.materialCode.toString()) }
                                if (gradeList.size > 0) {
                                    model?.bwart = gradeList[0].bwart
                                    model?.rsnum = gradeList[0].rsNum
                                    model?.rspos = gradeList[0].rsPos
                                    model?.phase = gradeList[0].phase
                                    model?.xchpf = gradeList[0].xchpf
                                    model?.resource = gradeList[0].resource
                                }
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

    private fun updateWeightToProcess() {
        /*calculateWtp(vm.lotList.value!!)
        binding.cvCard.tvProceesValue.text =
            addedLotList.sumByDouble { if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble()!! }
                .formatThreeDigits().plus(" ").plus(if (addedLotList.size > 0) addedLotList[0].unitOfMeasure else "MT")*/
    }


    private fun navigateToLotList() {
        model?.lotList?.clear()
        vm.lotList.let { model?.lotList?.addAll(it) }
        callBack?.replaceFragment(Lot_List, isMultipleLot, model!!, currentMaterial, "", isThirdPartyMaterial)
    }

    private fun navigateToWeighEntry(item: VegaCoffeeRminLots) {
        callBack?.replaceFragment(FRAG_ADD_WEIGHT, false, model!!, selectedItemPosition.toString(), item.batchNumber)
    }

    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    private fun enableManualLotAdd(value: Boolean) {
        binding.etEnterContainer.isEnabled = value
        binding.btnScan.isClickable = value
        binding.btnAddLot.isClickable = value
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    fun updateLotList(list: ArrayList<VegaCoffeeRminLots>) {
        val addedNew = ArrayList<VegaCoffeeRminLots>()
        val removedLots = ArrayList<VegaCoffeeRminLots>()
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
            vm.deleteLot(item.batchNumber, item.cgfNo)
        }
        for (item in addedNew) {
            item.cgfNo = model?.cgfNo ?: ""
            item.BOMNumber = model?.bom ?: ""
            item.poNumber = model?.poNumber ?: ""
            item.stage = model?.stage ?: ""
            item.baseMaterialCode = model?.materialName ?: ""
            if (model?.gradeListDetails.isNullOrEmpty()) {
            } else {
                item.fgrnIdMaterialCode = gradeList[selectedItemPosition].fgrnIdMaterialCode
            }
        }
        addedNew.forEach { it.rminId = model?.rminId.toString() }
        vm.saveWeighBridgeAndLotDetails(addedNew, model!!)
        vm.lotList.addAll(addedNew)
        adapter?.clear()
        adapter?.addAllLots(vm.lotList)
        enableProceed(vm.lotList.isNotEmpty())
        calculateWtp()
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

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight = vm.lotList.filter { it.editedWeight.equals("0.0") }
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

    private fun calculateWtp() {
        weightToProcess = 0.0
        vm.lotList.forEach { weightToProcess += it.editedWeight?.toDouble() ?: 0.0 }
        binding.cvCard.tvProceesValue.text =
            weightToProcess.toString().toDouble().formatThreeDigits().plus(" ")
                .plus(if (vm.lotList.size > 0) vm.lotList[0].unitOfMeasure else "KG")
    }

    fun saveLotDetails() {
        vm.saveWeighBridgeAndLotDetails(vm.lotList, model!!)
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>) {
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

    private fun chooseOneLotDialog(lots: List<VegaCoffeeRminLots>) {
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

    private fun updateAdapter(vegaCocoaDispatchLots: VegaCoffeeRminLots) {
        vm.lotList.add(vegaCocoaDispatchLots)
        adapter?.addLot(vegaCocoaDispatchLots)
        vegaCocoaDispatchLots.poNumber = model?.poNumber ?: ""
        vegaCocoaDispatchLots.cgfNo = model?.cgfNo ?: ""
        vegaCocoaDispatchLots.processOrderNo = model?.poNumber ?: ""
        vegaCocoaDispatchLots.rminId = model?.rminId ?: ""
        model?.lotList?.forEach {
            vegaCocoaDispatchLots.isEndLot = it.isEndLot
        }
        //vegaCocoaDispatchLots.fgrnIdMaterialCode = gradeList[selectedItemPosition].fgrnIdMaterialCode
        vm.saveLotDetails(vegaCocoaDispatchLots)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    fetchLotDetails(it, model?.materialCode ?: "", getPlantDetails().plantId)
                }
            }
        }
    }

    fun updateAddWeight(weight: String, batchNo: String?) {
        val split = weight.split(" ")
        adapter?.getItemList()?.forEachIndexed { index, vegaCoffeeRminLots ->
            if(vegaCoffeeRminLots.batchNumber.equals(batchNo))adapter?.updateAddWeight(index, split[0], split[1])
        }
        vm.lotList.find{it.batchNumber.equals(batchNo)}.apply {
            this?.editedWeight = split[0]
            this?.weightToDispatchUOM = split[1]
            val come: Int? = split[0].toDouble().compareTo(vm.lotList[addWeightPosition].weight?.toDouble() ?: 0.0)
            this?.isLowerWeight= come ?: 0 <= 0
            this?.let { vm.saveLotDetails(it) }
        }
        //adapter?.updateAddWeight(vm.lotList)
        calculateWtp()
    }

    override fun itemRemoved(item: VegaCoffeeRminLots) {
        vm.deleteLot(item.batchNumber, model?.cgfNo ?: "")
        vm.lotList.remove(item)
        enableProceed(vm.lotList.isNotEmpty())
        enableManualLotAdd(vm.lotList.isEmpty() || isMultipleLot)
        calculateWtp()
    }

    override fun weightUpdated(item: VegaCoffeeRminLots) {
        vm.saveLotDetails(item)
    }

    override fun addWeightForLot(pos: Int, item: VegaCoffeeRminLots) {
        addWeightPosition = pos
        navigateToWeighEntry(item)
    }

    private fun initAdapter() {
        adapter =
            VegaNigeriaRminLotAdapter(
                ArrayList(),
                this,
                this
            )
        val manager = LinearLayoutManager(activity)
        manager.orientation = LinearLayoutManager.VERTICAL
        binding.rvList.layoutManager = manager
        binding.rvList.adapter = adapter
    }

    private fun currentMaterialCode() {
        currentMaterial = gradeList[selectedItemPosition].materialCode
        isThirdPartyMaterial = gradeList[selectedItemPosition].isThirdPartyMaterial
    }

    private fun updateLotFromLocalDB(list: ArrayList<VegaCoffeeRminLots>) {
        if (list.isNotEmpty()) {
            vm.lotList.clear()
            adapter?.clear()
            vm.lotList.addAll(list)
            adapter?.addAllLots(vm.lotList)
            enableProceed(true)
            enableManualLotAdd(isMultipleLot)
            calculateWtp()
        } else {
            enableManualLotAdd(true)
        }
    }
}
