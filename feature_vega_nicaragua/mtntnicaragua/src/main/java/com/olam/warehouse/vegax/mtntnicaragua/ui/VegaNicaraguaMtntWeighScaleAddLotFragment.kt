package com.olam.warehouse.vegax.mtntnicaragua.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.model.VegaNicDispatchLotsWithBags
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
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicLotListModel
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentNicaraguaMtntWsAddLotLayoutBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.ItemNicWeighscaleLotCardLayoutBinding
import com.olam.warehouse.vegax.mtntnicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 11/12/2020.
 */
class VegaNicaraguaMtntWeighScaleAddLotFragment : BaseFragment(), BaseFragment.DialogClick {
    override val layoutResourceId = R.layout.fragment_nicaragua_mtnt_ws_add_lot_layout
    private lateinit var binding: FragmentNicaraguaMtntWsAddLotLayoutBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var callBack: Callback? = null
    private var mtnt: VegaNicaraguaMtnt? = null
    private var editLotId: String = ""
    private var lotChecked = false


    companion object {
        fun newInstance(model: VegaNicaraguaMtnt) =
            VegaNicaraguaMtntWeighScaleAddLotFragment()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, model)
                }
    }

    interface Callback {
        fun replaceFragment(type: String, data: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNicaraguaMtntWsAddLotLayoutBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        mtnt = arguments?.getParcelable(MODEL_BUNDLE)
        vm.mtnt = mtnt ?: VegaNicaraguaMtnt()
        binding.tvTruckValue.text = mtnt?.vehicleNumber
        binding.tvDestValue.text = mtnt?.sendingPlant
        binding.tvStoNumberValue.text = mtnt?.purchaseDocNum
        if (mtnt?.vendorName?.isEmpty() == true) {
            binding.tvVendor.gone()
            binding.tvVendorValue.gone()
        } else {
            binding.tvVendor.visible()
            binding.tvVendorValue.visible()
            binding.tvVendorValue.text = mtnt?.vendorName
        }
        binding.tvMaterialValue.text = mtnt?.materialName
        binding.tvQualityGradeValue.text = mtnt?.qualityGrade
        binding.tvStoWeightValue.text = mtnt?.soWeight?.plus(" ")?.plus(mtnt?.soUOM)
    }

    private fun initUI() {
        binding.clLotSummary.clRemarks.visibility = View.GONE
        binding.btnProceed.setOnClickListener {
            if (MERGED)
                validateProceed()
            else validateNonMergeProceed()

        }
        binding.clLotSummary.etEnterContainer.onChange { enableAddLot(it) }
        binding.clLotSummary.clScan.setOnClickListener { moveToScan() }
        binding.clLotSummary.btnAddLot.setOnClickListener {
            vm.updateLotDetails()
            val lotList = vm.lotList.map { it.lots } as ArrayList
            vm.mtnt.merged = MERGED
            callBack?.replaceFragment(
                FRAG_LOT_LIST,
                VegaNicLotListModel(
                    selectedList = lotList,
                    isMultipleAdd = true,
                    material = arrayListOf<String>(vm.mtnt.materialCode.toString()),
                    mtnt = vm.mtnt
                )
            )
        }
        binding.clLotSummary.btAdd.setOnClickListener { vm.validateLot(binding.clLotSummary.etEnterContainer.text.toString()) }

        vm.lotDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        vm.lotDetailsOffline.observe(viewLifecycleOwner, Observer { updateLotInfoOffline(it) })
        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.clLotSummary.etEnterContainer.setText("")
            } else
                fetchLotDetails(binding.clLotSummary.etEnterContainer.text.toString())
        })

        vm.weighBridgeLotsWithBags.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.lotList.clear()
                var items = VegaNicDispatchLotsWithBags()
                items.lots = VegaNicDispatchLots()
                var weight: String = "0"
                var materialcode: String = "0"
                var bag = 0
                if (it.lineItems.size > 1)
                    vm.lotList.addAll(it.lineItems.filter { it.lots.isMergedLot == false })
                else
                    vm.lotList.addAll(it.lineItems)

                // binding.clLotSummary.etRemarks.setText(it.mtnt.remarks)
                updateAdapter()
            }
        })
        vm.getMtntWithLots(mtnt?.tempId ?: "")
        vm.getConfigItems(UserRoles.PROCESSING.role)

        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })

        vm.qualitylist.observe(viewLifecycleOwner, {
            var filteredBagList = it.filter { it.qualityParameter.nameChar.equals("NISACOS") }
            vm.mtnt.namechar = filteredBagList.get(0).qualityParameter.nameChar
            vm.mtnt.deschar = filteredBagList.get(0).qualityParameter.descrChar
        })

        vm.getQualityParams(mtnt?.materialCode.toString())
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val millingPlants = configItems.filter { it.process.equals(ConfigItems.MILLING_PLANT.item) }
        val thirdPartyPalnts = configItems.filter { it.process.equals(ConfigItems.THIRD_PARTY_PLANT.item) }
        val dryingPlant = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }

        MILLING_PLANT = !millingPlants.isNullOrEmpty()
        THIRD_PARTY_PLANT = !thirdPartyPalnts.isNullOrEmpty()
        DRYING_PLANT = !dryingPlant.isNullOrEmpty()
        MERGED = !MILLING_PLANT && !THIRD_PARTY_PLANT && !DRYING_PLANT
    }

    private fun updateLotInfoOffline(lotData: List<VegaNicaraguaGRNInventoryDetails>?) {
        lotData?.let {
            if (it.size > 0) {
                var isvalidLot = true
                when (vm.mtnt.vendorName?.isEmpty()) {
                    true -> {
                        if (!lotData[0].qualityGrade.equals(vm.mtnt.qualityGrade) || !lotData[0].certification.equals(
                                vm.mtnt.certification
                            )
                        )
                            isvalidLot = false
                    }
                    else -> {
                        if (!lotData[0].vendorCode.equals(vm.mtnt.vendorCode) || !lotData[0].qualityGrade.equals(vm.mtnt.qualityGrade) || !lotData[0].certification.equals(
                                vm.mtnt.certification
                            )
                        )
                            isvalidLot = false
                    }
                }
                when (isvalidLot) {
                    true -> {
                        saveLotDetails(prepareLotItems(lotData, vm.mtnt.tempId)[0])
                    }
                    else -> {
                        showErrorDialogWithFAQLink(requireContext(), getString(R.string.incorrect_quality_error))
                    }
                }
            } else {
                showErrorDialogWithFAQLink(requireContext(), getString(R.string.add_valid_lot))
            }
        }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaNicDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        val lotData = prepareLotDataItems(it1)
                        var isvalidLot = true
                        when (vm.mtnt.vendorName?.isEmpty()) {
                            true -> {
                                if (!lotData[0].qualityGrade.equals(vm.mtnt.qualityGrade) || !lotData[0].certification.equals(
                                        vm.mtnt.certification
                                    )
                                )
                                    isvalidLot = false
                            }
                            else -> {
                                if (!lotData[0].vendor.equals(vm.mtnt.vendorCode) || !lotData[0].qualityGrade.equals(vm.mtnt.qualityGrade) || !lotData[0].certification.equals(
                                        vm.mtnt.certification
                                    )
                                )
                                    isvalidLot = false
                            }
                        }
                        when (isvalidLot) {
                            true -> when (it1.size == 1) {
                                true -> {
                                    saveLotDetails(it1[0])
                                }
                                else -> chooseOneLotDialog(it1)
                            }
                            else -> {
                                showErrorDialogWithFAQLink(
                                    requireContext(),
                                    getString(R.string.incorrect_quality_error)
                                )
                            }
                        }
                        lotChecked = true
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

    private fun chooseOneLotDialog(lots: List<VegaNicDispatchLots>) {
        val lotItem = lots.map {
            getString(com.olam.warehouse.login.R.string.lot_no).plus(" : ").plus(it.batchNumber).plus("\n")
                .plus(getString(com.olam.warehouse.login.R.string.weight)).plus(" : ").plus(it.weight).plus(" ")
                .plus(it.unitOfMeasure)
                .plus("\n").plus(getString(R.string.st_location)).plus(" : ").plus(it.storageLocationCode)
        }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_lot)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = lotItem) { _, index, text ->
                saveLotDetails(lots[index])
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun saveLotDetails(lot: VegaNicDispatchLots) {
        lot.tempId = vm.mtnt.tempId
        lot.tempIdWithBatch = vm.mtnt.tempId.plus(lot.batchNumber)
        lot.isEndLot = true
        lot.availbagCount = lot.materialQuality.qualityParams.NISACOS
        vm.saveLotDetails(lot)
    }

    private fun updateAdapter() {

        var lineItems = mutableListOf<VegaNicDispatchLotsWithBags>()
        if (editLotId.isNotEmpty()) {
            enableDisableItem(false)
            lineItems =
                vm.lotList.filter { it.lots.batchNumber.equals(editLotId) } as MutableList<VegaNicDispatchLotsWithBags>
        } else {
            enableDisableItem(true)
            lineItems = vm.lotList
        }
        binding.clLotSummary.rvList.setUpAdapter(
            lineItems,
            R.layout.item_nic_weighscale_lot_card_layout,
            ItemNicWeighscaleLotCardLayoutBinding::inflate,
            { it, pos, bindItem ->
                val lot = it.lots
                val lotBags = it.bagItems
                if (lineItems.size > 1) {
                    if (MERGED) {
                        bindItem.tvAddWeight.visibility = View.GONE
                        bindItem.cbEndLot.visibility = View.INVISIBLE
                        bindItem.tvEndLot.visibility = View.INVISIBLE
                        binding.btnProceed.text = "Proceed to merge Lot"
                        islotMERGED == true
                    }
                }

                if (it.bagItems.size > 0)
                    lot.editedWeight =
                        it.bagItems.sumOf { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                            .formatThreeDigits()
                else lot.editedWeight = "0"

                if(editLotId.isNotEmpty()){
               vm.lotList.forEach {
                   if(it.lots.batchNumber==editLotId){
                      it.lots.editedWeight=lot.editedWeight
                   }
               }}else vm.lotList[pos].lots.editedWeight = lot.editedWeight

                bindItem.tvScaleLotValue.text = lot.batchNumber
                bindItem.tvScaleWeightValue.text =
                    (if (lot.weight?.isNotEmpty() == true) lot.weight?.toDouble()?.formatTwoDigits()
                        .plus(" ")
                        .plus(lot.unitOfMeasure) else "").toString()
                bindItem.tvStLocationValue.text = lot.storageLocationCode
                bindItem.tvScaleGradeValue.text = lot.materialName
                if (!MERGED) {
                    bindItem.tvbagcount.text = lot.availbagCount.toString()
                    bindItem.tvbagcount.visible()
                    bindItem.tvbagcountvalid.visible()
                } else {
                    bindItem.tvbagcount.gone()
                    bindItem.tvbagcountvalid.gone()
                }
                bindItem.tvScaleDispatchValue.text =
                    lot.editedWeight.plus(" ").plus(lot.unitOfMeasure)
                bindItem.cbEndLot.isChecked = lot.isEndLot ?: false
                bindItem.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
                    lot.isEndLot = isChecked
                    bindItem.cbEndLot.isChecked = lot.isEndLot ?: false
                    vm.lotList[pos].lots.isEndLot = lot.isEndLot
                }
                bindItem.tvEndLot.setOnClickListener {
                    lot.isEndLot = !lot.isEndLot!!
                    bindItem.cbEndLot.isChecked = lot.isEndLot ?: false
                    vm.lotList[pos].lots.isEndLot = lot.isEndLot
                }
                if (editLotId.isNotEmpty()) bindItem.ivScaleClose.gone() else bindItem.ivScaleClose.visible()

                if (lot.materialName?.contains("TOLLING", true) == true) {
                    bindItem.vendorLl.visible()
                    bindItem.tvVendorValue.text = lot.vendorName
                }

                bindItem.ivScaleClose.setOnClickListener {
                    showConformationDialog(pos, bindItem.ivScaleClose)
                }
                bindItem.tvAddWeight.setOnClickListener {
                    if (lotBags.size == 0) vm.mtnt.startTime =
                        DateUtils.getCurrentTimeInMills().toString()
                    lot.truckNo = vm.mtnt.vehicleNumber
                    lot.bagType = vm.mtnt.bagType
                    vm.lotList[pos].lots.lotpos = pos.toString()
                    vm.lotList[pos].lots.isEndLot = bindItem.cbEndLot.isChecked
                    vm.updateLotDetails()
                    callBack?.replaceFragment(FRAG_ADD_WEIGHT, lot)
                }

                vm.lotList[pos].lots.isEndLot = bindItem.cbEndLot.isChecked

            }


        )
    }

    private fun enableDisableItem(flag: Boolean) {
        binding.clLotSummary.btAdd.isEnabled = flag
        binding.clLotSummary.btnAddLot.isEnabled = flag
        binding.clLotSummary.btnScan.isEnabled = flag
    }

    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.removeLotFromList(
                        vm.lotList[position].lots.batchNumber,
                        vm.lotList[position].lots.tempId
                    )
                    vm.lotList.removeAt(position)
                    if (vm.lotList.size == 1)
                        binding.btnProceed.text = "Proceed"
                    binding.clLotSummary.rvList.adapter?.notifyItemRemoved(position)
                },
                { dismiss() })
        }
    }

    private fun fetchLotDetails(lotId: String) {
        vm.updateLotDetails()
        binding.clLotSummary.etEnterContainer.hideKeyboard()
        binding.clLotSummary.etEnterContainer.setText("")
//        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(
            lotId,
            listOf(vm.mtnt.materialCode.toString()),
            getPlantDetails().plantId
        ) else vm.getOfflineLotDetails(lotId, vm.mtnt.materialCode.toString())
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

    private fun plantCheck() {
        if (MILLING_PLANT || THIRD_PARTY_PLANT || DRYING_PLANT) {
            proceedLotAfterCheck()
        } else {
            if (vm.lotList.size > 1) {
                proceedLotIfLotGreaterThanOne()
            } else {
                proceedLotAfterCheck()
            }
        }
    }


    private fun validateProceed() {
        if (vm.lotList.isNotEmpty()) {
            vm.mtnt.mtntDocSequence = vm.generateMtntSequnceNumber()
            if (lotChecked) {
                plantCheck()
            } else {
                if (validateGradeAndCertification()) {
                    plantCheck()
                } else Toast.makeText(
                    activity,
                    getString(R.string.incorrect_quality_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                activity,
                getString(R.string.please_add_lot),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun proceedLotIfLotGreaterThanOne() {
        vm.mtnt.endTime = DateUtils.getCurrentTimeInMills().toString()
        vm.saveWeighBridgeDetails()
        vm.updateLotDetails()
        var mtntLot: VegaNicDispatchLots? = null
        var lineItems = mutableListOf<VegaNicDispatchLotsWithBags>()
        var lineItems1 = mutableListOf<VegaNicDispatchLotsWithBags>()
        var items = VegaNicDispatchLotsWithBags()
        items.lots = VegaNicDispatchLots()
        var weight: String = "0"
        var materialcode: String = "0"
        var availeBagCount = 0
        lineItems = vm.lotList
        Log.d("lineitems", lineItems.toString())
        if (lineItems.size > 1) {
            lineItems.forEach { it1 ->
                weight =
                    (java.lang.Double.valueOf(weight) + java.lang.Double.valueOf(
                        it1.lots.weight.toString()
                    )).toString()
                materialcode = it1.lots.materialName.toString()
                items.lots.batchNumber = it1.lots.tempId
                items.lots.qualityGrade = vm.mtnt.qualityGrade
                items.lots.certification = vm.mtnt.certification
                items.lots.unitOfMeasure = it1.lots.unitOfMeasure
                it1.lots.availbagCount = it1.lots.availbagCount?.replace(",","")
                if(it1.lots.availbagCount.isNullOrEmpty()) it1.lots.availbagCount = "0"
                availeBagCount += (it1.lots.availbagCount?:"0").toInt()
            }
            items.lots.materialName = materialcode
            items.lots.weight = weight
            items.lots.availbagCount = availeBagCount.toString()
            islotMERGED = true
            items.lots.isMergedLot = true
            lineItems1.add(items)
            saveLotDetails(items.lots)
            callBack?.replaceFragment(FRAG_MERGE_LOT, mtnt!!)
        }

    }


    private fun proceedLotAfterCheck() {
        if (validateExceedSoWeight()) {
            if (validateEmptyWeight()) {
                showRemarkDialog()

            } else Toast.makeText(
                activity,
                getString(R.string.zero_weight_error),
                Toast.LENGTH_SHORT
            ).show()
        } else Toast.makeText(
            activity,
            getString(R.string.less_weight_so_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            vm.lotList.filter {
                it.lots.editedWeight.equals("0.0") || it.lots.editedWeight.equals("0") || it.lots.editedWeight.equals(
                    ""
                )
            }
        return emptyWeight.isEmpty()
    }

    private fun validateExceedSoWeight(): Boolean {
//        val soWeight = 1000.0
        val soWeight = vm.mtnt.soWeight?.toDouble() ?: 0.0
        val editedWeight = vm.lotList.sumOf {
            if (it.lots.editedWeight?.isNotEmpty() == true) it.lots.editedWeight?.toDouble()
                ?: 0.0 else 0.0
        }
        vm.lotList.forEachIndexed { index, v ->
            if (MILLING_PLANT || THIRD_PARTY_PLANT || DRYING_PLANT) {
                var diff = v.lots.weight!!.toDouble() - v.lots.editedWeight!!.toDouble()
                if (diff > 0)
                    vm.lotList[index].lots.Partially = 1
                else
                    vm.lotList[index].lots.Partially = 0
            } else
                vm.lotList[index].lots.Partially = 0
        }
        return when {
            vm.mtnt.soUOM.equals("kg", true) -> editedWeight <= soWeight
            vm.mtnt.soUOM.equals("MT", true) -> editedWeight.div(1000) <= soWeight
            else -> true
        }
    }

    private fun validateGradeAndCertification(): Boolean {
        when (vm.mtnt.certification?.isNotEmpty() == true) {
            true -> return !vm.lotList.any {
                !it.lots.qualityGrade.equals(vm.mtnt.qualityGrade) || !it.lots.certification.equals(
                    vm.mtnt.certification
                )
            }
            else -> return return !vm.lotList.any { !it.lots.qualityGrade.equals(vm.mtnt.qualityGrade) }
        }
    }

    private fun enableProceed(enable: Boolean) {
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    fun editLot(vegaNicDispatchLots: VegaNicDispatchLots) {
        editLotId = vegaNicDispatchLots.batchNumber
        vm.getMtntWithLots(vegaNicDispatchLots.tempId)
    }

    fun getBack() {
        editLotId = ""
        vm.getMtntWithLots(vm.mtnt.tempId)
    }

    private fun validateNonMergeProceed() {
        if (vm.lotList.isNotEmpty()) {
            if (validateGradeAndCertification()) {
                if (validateExceedSoWeight()) {
                    if (validateEmptyWeight()) {
                        vm.mtnt.mtntDocSequence = vm.generateMtntSequnceNumber()
                        /*  if (binding.clLotSummary.etRemarks.text.toString().isNotEmpty()) {
                              vm.mtnt.remarks = binding.clLotSummary.etRemarks.text.toString()
                              vm.mtnt.endTime = DateUtils.getCurrentTimeInMills().toString()
                              vm.saveWeighBridgeDetails()
                              vm.updateLotDetails()
                              callBack?.replaceFragment(FRAG_SUMMARY, vm.mtnt)
                          } else Toast.makeText(
                              activity,
                              getString(R.string.enter_remark),
                              Toast.LENGTH_SHORT
                          ).show()
  */
                        showRemarkDialog()
                    } else Toast.makeText(
                        activity,
                        getString(R.string.zero_weight_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else Toast.makeText(
                    activity,
                    getString(R.string.less_weight_so_error),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                activity,
                getString(R.string.incorrect_quality_error),
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


    private fun showRemarkDialog() {
        showDialogWithInput(
            getString(com.olam.warehouse.login.R.string.remarks),
            this,
            true,
            getString(R.string.remarks)
        )

    }

    override fun onPositive(remark: String) {
        if (!remark.isNullOrEmpty()) {
            vm.mtnt.remarks = remark
            vm.mtnt.endTime = DateUtils.getCurrentTimeInMills().toString()
            vm.saveWeighBridgeDetails()
            vm.updateLotDetails()
            callBack?.replaceFragment(FRAG_SUMMARY, vm.mtnt)
        } else {
            Toast.makeText(requireContext(), getString(R.string.enter_remark), Toast.LENGTH_SHORT).show()
        }
    }

}
