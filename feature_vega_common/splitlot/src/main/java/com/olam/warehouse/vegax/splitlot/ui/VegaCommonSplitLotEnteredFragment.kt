package com.olam.warehouse.vegax.splitlot.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.ui.printformats.NicaraguaMTNRPrintReceipt
import com.olam.warehouse.login.ui.printformats.NicaraguaMTNRPrintTicket
import com.olam.warehouse.login.ui.printformats.generateFgrnTallySheetBitMap
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaCommonSplitLotModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveTallySequence
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.splitlot.R
import com.olam.warehouse.vegax.splitlot.data.domain.model.*
import com.olam.warehouse.vegax.splitlot.databinding.FragmentSplitLotEnteredBinding
import com.olam.warehouse.vegax.splitlot.databinding.ItemCommonSplitLotBinding
import com.olam.warehouse.vegax.splitlot.utils.*
import com.olam.warehouse.vegax.splitlot.work.getSplitPrintTicketOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 9/27/2022.
 */
class VegaCommonSplitLotEnteredFragment : BaseFragment(), VegaSingleSelectCommonListener {
    override val layoutResourceId = R.layout.fragment_split_lot_entered
    private lateinit var binding: FragmentSplitLotEnteredBinding
    private var lotDetails = VegaCoffeeLot()
    private var receivingData = VegaReceiving()
    private var splitLotList = arrayListOf<VegaCommonSplitLotModel>()
    private var splitLotListPost = arrayListOf<VegaCommonSplitLotModel>()
    private var lotQualityDetails = arrayListOf<VegaQualityPreParameter>()
    private var gradeList = mutableListOf<VegaQualitative>()
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var callBack: Callback? = null
    private val vm: VegaCommonSplitLotViewModel by viewModel()
    private var currentPos: Int = 0
    private var materialCode: String? = ""
    private val charValue = mutableListOf<String>()
    private var printReceiptKeys = java.util.ArrayList<String>()

    // private var isdanofound: HashMap<Int, Boolean> = HashMap<Int, Boolean>()
    private var islotinc: Int = 0
    private var isFirstReceipt = true

    interface Callback {
        fun replaceFragment(splitLotList: ArrayList<VegaCommonSplitLotModel>)
    }

    companion object {
        fun newInstance(
            lotList: VegaCoffeeLot,
            splitLotList: ArrayList<VegaCommonSplitLotModel>,
            receivingdata: VegaReceiving
        ) =
            VegaCommonSplitLotEnteredFragment().putArgs {
                putParcelable(UIUtils.LOT_DETAIL, lotList)
                putParcelableArrayList("LOT_LIST", splitLotList)
                putParcelable(UIUtils.RECEIVING_DATA, receivingdata)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as Callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSplitLotEnteredBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
//        isdanofound.clear()
        lotDetails = arguments?.getParcelable<VegaCoffeeLot>(UIUtils.LOT_DETAIL) ?: VegaCoffeeLot()
        receivingData = arguments?.getParcelable<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()
        splitLotList = arguments?.getParcelableArrayList<VegaCommonSplitLotModel>("LOT_LIST")
            ?: arrayListOf<VegaCommonSplitLotModel>()
        lotDetails.materialCode =
            if (lotDetails.materialCode?.length != 18) "000000".plus(lotDetails.materialCode) else lotDetails.materialCode
        binding.tvSplitValue.text = "2"
        if (splitLotList.size > 0) {
            binding.tvSplitValue.text = "${splitLotList.size}"
            splitLotList.forEachIndexed { index, it ->
                when (index) {
                    0 -> binding.etWeight1.setText(it.netWeight)
                    1 -> binding.etWeight2.setText(it.netWeight)
                    2 -> binding.etWeight3.setText(it.netWeight)
                    3 -> binding.etWeight4.setText(it.netWeight)
                }
            }
            visibleWeightBlock()
            updateLotAdapter()
        }
        visibleWeightBlock()
        binding.tvLotNumber.text = getString(R.string.parent_lot_no).plus(lotDetails.batchNumber)
        binding.tvLotWeight.text =
            getString(R.string.parent_lot_weight).plus(lotDetails.netWeight).plus(" ").plus(lotDetails.unitsOfMeasure)
        binding.tvSplit.setOnClickListener {
            if (checkWeightValidation()) {
                updateLotValues(true, emptyList())
                val postData = prepareSplitData(lotDetails, splitLotListPost)
                vm.postSplitDeatils(postData)
            } else {
                activity?.toast(getString(R.string.weight_validation))
            }
        }
        binding.etWeight1.onChange { enableSplitBtn() }
        binding.etWeight2.onChange { enableSplitBtn() }
        binding.etWeight3.onChange { enableSplitBtn() }
        binding.etWeight4.onChange { enableSplitBtn() }
        binding.ivMinus.setOnClickListener {
            val currentVal = binding.tvSplitValue.text.toString().trim()
            if (!currentVal.equals("2")) {
                binding.tvSplitValue.text = (currentVal.toString().toInt().dec()).toString()
                visibleWeightBlock()
            }
        }
        binding.ivAdd.setOnClickListener {
            val currentVal = binding.tvSplitValue.text.toString().trim()
            if (!currentVal.equals("4")) {
                binding.tvSplitValue.text = (currentVal.toString().toInt().inc()).toString()
                visibleWeightBlock()
            }
        }
        binding.tvProceed.setOnClickListener { getBack() }
        vm.splitPost.observe(viewLifecycleOwner, Observer { updateSplitPostResponse(it) })
        vm.qualityPost.observe(viewLifecycleOwner, Observer { updateQualityPostUI(it) })
        vm.updatetallySequence.observe(viewLifecycleOwner, { UpdateTally(it) })
        materialCode = lotDetails.materialCode.toString()
        vm.lotQuality.observe(viewLifecycleOwner, Observer { updateLotQualityDetails(it) })
        vm.getLotQualityDetails(lotDetails.batchNumber, lotDetails.materialCode.toString())
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getQualityParams(lotDetails.materialCode.toString())
        vm.grade.observe(viewLifecycleOwner, Observer {
            gradeList = it.toMutableList()
        })
        vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
            materialQualityGradeList = it.toMutableList()
        })
        vm.getGrades(lotDetails.materialCode.toString())
        vm.getMaterialQualityGrades(lotDetails.materialCode.toString())
        binding.tvprintsplitlot.setOnClickListener {
            showLoading()
            PrintMTNRReceipt()
        }
    }

    private fun PrintMTNRReceipt() {
        val intent = Intent()
        intent.putExtra(UIUtils.RECEIVING_DATA, receivingData)
        intent.putExtra(UIUtils.ISSPLIT_LOT, true)
        intent.putParcelableArrayListExtra(UIUtils.SPLIT_LOTS, splitLotList)
        printReceiptKeys = NicaraguaMTNRPrintReceipt(intent, context)
        if (isFirstReceipt) {
            isFirstReceipt = false
            DoAsync {
                runOnUiThread {
                    postPrintTicket(
                        TYPE_R,
                        printReceiptKeys.get(0),
                        receivingData.batchNumber,
                        receivingData.grnNumber.plus("_split"),
                        receivingData.materialCode
                    )
                }
            }.execute()
        } else
            DoAsync {
                runOnUiThread {
                    hideLoading()
                    val gson = GsonUtils()
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(printReceiptKeys))
                    startActivity(Intent(this.context, WifiMainActivity::class.java))
                }
            }.execute()
    }

    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val danolist = it.filter { vega -> vega.qualityParameter.nameChar.equals("NIDANO") }
                    charValue.clear()
                    if (danolist.size > 0)
                        charValue.addAll(danolist.get(0).qualitative!!.filter { it.materialCode == materialCode }
                            .map { data -> data.charValue })
                    else setErrorContentView("Quality params not available for this material")
                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }

    private fun updateQualityPostUI(data: Resource<GenericReqAndResp<VegaCoffeeQualityParamPostResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let {
                        updateQualityPostValueInAdapter(it)
                    }
                    postTallyUpdateSequence()
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    splitLotList[currentPos].qcStatus = Status.SYNC_ERROR
                    updateLotAdapter()
                    // binding.rvSplitLot.adapter?.notifyItemChanged(currentPos)
                    postTallyUpdateSequence()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun updateQualityPostValueInAdapter(it: VegaCoffeeQualityParamPostResponse) {
        splitLotList[currentPos].qcStatus = Status.SYNC_COMPLETED
        updateLotAdapter()
        //binding.rvSplitLot.adapter?.notifyItemChanged(currentPos)
        /*splitLotList.forEachIndexed { index, item ->
            if(it.lotQuality?.isNotEmpty() == true && it.lotQuality?.get(0)?.batchNumber.equals(item.batchNumber)){
                splitLotList[index].qcStatus = Status.SYNC_COMPLETED
                if(lotQualityDetails.isNotEmpty()) {
                    try {
                        val cert = lotQualityDetails.get(0).qualityParameters?.filter {
                            it.sapQCName.equals("NIFG0014")
                        }
                        if(cert?.isNullOrEmpty() == false) {
                            splitLotList[index].certification = cert.get(0).satNam
                        }
                    } catch (e: ArrayIndexOutOfBoundsException){
                        e.printStackTrace()
                    }catch (e: NullPointerException){
                        e.printStackTrace()
                    }

                }
                binding.rvSplitLot.adapter?.notifyItemChanged(index)
                return@forEachIndexed
            }
        }*/
    }

    private fun updateSplitPostResponse(data: Resource<GenericReqAndResp<VegaCommonSplitMainModel>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let {
                        val resLotList = it.splitLots
                        updateLotValues(false, resLotList)
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    //updateLotValues(false, emptyList())
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun updateLotQualityDetails(data: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let {
                        lotQualityDetails.clear()
                        lotQualityDetails.addAll(it)
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun updateLotValues(isPost: Boolean, resLotList: List<VegaCommonSplitLotModel>) {
        if (!isPost) binding.tvSplit.isEnabled = false
        splitLotList.clear()
        val currentVal = binding.tvSplitValue.text.toString().trim()
        if (splitLotList.isEmpty()) {
            when (currentVal) {
                "2" -> {
                    IntArray(currentVal.toInt()).toList().forEachIndexed { index, i ->
                        val splitLot = VegaCommonSplitLotModel()
                        when (index) {
                            0 -> {
                                splitLot.netWeight = binding.etWeight1.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 0) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                            1 -> {
                                splitLot.netWeight = binding.etWeight2.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 1) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                        }
                        if (isPost) splitLotListPost.add(splitLot) else splitLotList.add(splitLot)
                    }

                }
                "3" -> {
                    IntArray(currentVal.toInt()).toList().forEachIndexed { index, i ->
                        val splitLot = VegaCommonSplitLotModel()
                        when (index) {
                            0 -> {
                                splitLot.netWeight = binding.etWeight1.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 0) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                            1 -> {
                                splitLot.netWeight = binding.etWeight2.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 1) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                            2 -> {
                                splitLot.netWeight = binding.etWeight3.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 2) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                        }
                        if (isPost) splitLotListPost.add(splitLot) else splitLotList.add(splitLot)
                    }

                }
                "4" -> {
                    IntArray(currentVal.toInt()).toList().forEachIndexed { index, i ->
                        val splitLot = VegaCommonSplitLotModel()
                        when (index) {
                            0 -> {
                                splitLot.netWeight = binding.etWeight1.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 0) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                            1 -> {
                                splitLot.netWeight = binding.etWeight2.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 1) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                            2 -> {
                                splitLot.netWeight = binding.etWeight3.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 2) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                            3 -> {
                                splitLot.netWeight = binding.etWeight4.text.toString()
                                splitLot.position = index
                                splitLot.parentBatchNumber = lotDetails.batchNumber
                                splitLot.grnNumber = lotDetails.grnNumber
                                splitLot.batchNumber =
                                    if (resLotList.size > 3) resLotList.get(index).batchNumber else ""
                                if (!isPost) {
                                    val tallySequence =
                                        PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                                    splitLot.ticketNumber = vm.generateTallySequnceNumber()
                                    saveTallySequence(tallySequence)
                                }
                            }
                        }
                        if (isPost) splitLotListPost.add(splitLot) else splitLotList.add(splitLot)
                    }

                }
            }
        }
        if (!isPost) updateLotAdapter()
    }

    private fun enableSplitBtn() {
        val currentVal = binding.tvSplitValue.text.toString().trim()
        if (checkWeightValidation()) {
            when (currentVal) {
                "4" -> {
                    binding.tvSplit.isEnabled =
                        (binding.etWeight1.text?.isNotEmpty() == true && binding.etWeight2.text?.isNotEmpty() == true
                                && binding.etWeight3.text?.isNotEmpty() == true && binding.etWeight4.text?.isNotEmpty() == true)
                    changeBackgroundColorforSplit()
                }
                "3" -> {
                    binding.tvSplit.isEnabled =
                        (binding.etWeight1.text?.isNotEmpty() == true && binding.etWeight2.text?.isNotEmpty() == true
                                && binding.etWeight3.text?.isNotEmpty() == true)
                    changeBackgroundColorforSplit()
                }
                "2" -> {
                    binding.tvSplit.isEnabled =
                        (binding.etWeight1.text?.isNotEmpty() == true && binding.etWeight2.text?.isNotEmpty() == true)
                    changeBackgroundColorforSplit()
                }
            }
        } else {
            activity?.toast(getString(R.string.weight_validation))
        }
    }

    private fun changeBackgroundColorforSplit() {
        when (binding.tvSplit.isEnabled) {
            true -> ViewCompat.setBackgroundTintList(
                binding.tvSplit,
                ContextCompat.getColorStateList(
                    binding.tvSplit.context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            false -> ViewCompat.setBackgroundTintList(
                binding.tvSplit,
                ContextCompat.getColorStateList(binding.tvSplit.context, com.olam.warehouse.presentation.R.color.grey)
            )
        }
    }

    private fun checkWeightValidation(): Boolean {
        val w1 = if (binding.etWeight1.text?.isNotEmpty() == true) binding.etWeight1.text.toString() else "0"
        val w2 = if (binding.etWeight2.text?.isNotEmpty() == true) binding.etWeight2.text.toString() else "0"
        val w3 = if (binding.etWeight3.text?.isNotEmpty() == true) binding.etWeight3.text.toString() else "0"
        val w4 = if (binding.etWeight4.text?.isNotEmpty() == true) binding.etWeight4.text.toString() else "0"
        val totalWeight =
            ((if (w1.startsWith(".")) "0".plus(w1) else w1).toDouble()).plus((if (w2.startsWith(".")) "0".plus(w2) else w2).toDouble())
                .plus((if (w3.startsWith(".")) "0".plus(w3) else w3).toDouble())
                .plus((if (w4.startsWith(".")) "0".plus(w4) else w4).toDouble())
        return (lotDetails.netWeight?.toDouble() ?: 0.0) >= totalWeight
    }

    private fun visibleWeightBlock() {
        disableWeightBlock()
        val currentVal = binding.tvSplitValue.text.toString().trim()
        when (currentVal) {
            "2" -> {
                binding.etWeight1.visible()
                binding.etWeight2.visible()
            }
            "3" -> {
                binding.etWeight1.visible()
                binding.etWeight2.visible()
                binding.etWeight3.visible()
            }
            "4" -> {
                binding.etWeight1.visible()
                binding.etWeight2.visible()
                binding.etWeight3.visible()
                binding.etWeight4.visible()
            }
        }
    }

    fun disableWeightBlock() {
        binding.etWeight1.gone()
        binding.etWeight2.gone()
        binding.etWeight3.gone()
        binding.etWeight4.gone()
    }

    fun enableDisableAddMinusBlock() {
        IntArray(binding.clAddMinus.childCount).toList().forEachIndexed { index, i ->
            binding.clAddMinus.getChildAt(index).isEnabled = false
        }
    }

    private fun updateLotAdapter() {
        enableDisableAddMinusBlock()
        binding.rvSplitLot.setUpAdapter(
            splitLotList,
            R.layout.item_common_split_lot,
            ItemCommonSplitLotBinding::inflate,
            { it, pos, bindingItem ->
                bindingItem.tvScaleLotValue.text = it.batchNumber
                bindingItem.tvTicketValue.text = it.ticketNumber
                bindingItem.tvScaleWeightValue.text = it.netWeight
                bindingItem.tvQualityGradeValue.text = it.qualityGrade
                bindingItem.tvBagCountValue.setText(it.bagCount.toString())
                bindingItem.tvdanoValue.text = it.danoValue
                if (it.qualityGradeDesc?.contains("DAÑO") == true) {
                    bindingItem.tvdanoValue.visible()
                    bindingItem.tvdano.visible()
                } else {
                    bindingItem.tvdanoValue.gone()
                    bindingItem.tvdano.gone()
                }
                if (it.qcStatus == Status.SYNC_PENDING) {
                    bindingItem.tvQcStatusValue.text = getString(R.string.qc_status_pending)
                    bindingItem.tvQcStatusValue.setTextColor(
                        ContextCompat.getColor(
                            bindingItem.tvQcStatusValue.context,
                            com.olam.warehouse.presentation.R.color.red1
                        )
                    )
                } else if (it.qcStatus == Status.SYNC_ERROR) {
                    bindingItem.tvQcStatusValue.text = getString(R.string.qc_status_error)
                    bindingItem.tvQcStatusValue.setTextColor(
                        ContextCompat.getColor(
                            bindingItem.tvQcStatusValue.context,
                            com.olam.warehouse.presentation.R.color.red1
                        )
                    )
                } else {
                    bindingItem.tvQualityGradeValue.isEnabled = false
                    bindingItem.tvBagCountValue.isEnabled = false
                    bindingItem.tvQcStatusValue.text = getString(R.string.qc_status_done)
                    bindingItem.tvQcStatusValue.setTextColor(
                        ContextCompat.getColor(
                            bindingItem.tvQcStatusValue.context,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    )
                    bindingItem.tvSaveQuality.text = context.getString(
                        R.string.print_tally
                    )

                    if (splitLotList.all { it.qcStatus == Status.SYNC_COMPLETED }) {
                        binding.tvprintsplitlot.isEnabled = true
                        ViewCompat.setBackgroundTintList(
                            binding.tvprintsplitlot,
                            ContextCompat.getColorStateList(
                                binding.tvprintsplitlot.context,
                                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                            )
                        )
                    } else
                        binding.tvprintsplitlot.isEnabled = false
                }
                bindingItem.tvBagCountValue.onChange {
                    splitLotList.get(pos).bagCount = it
                }
                bindingItem.tvQualityGradeValue.setOnClickListener {
                    showSingleSelectDialog(
                        context.getString(R.string.select_quality_grade),
                        QUALITY_GRADE.plus("-").plus(pos)
                    )
                }
                bindingItem.tvdanoValue.setOnClickListener {
                    showSingleSelectDialog(context.getString(R.string.dano), DANO.plus("-").plus(pos))
                }

                /*if (isdanofound.size > 0) {
                    var danoposval = isdanofound.get(pos)
                    if (danoposval == true) {
                        bindingItem.tvdanoValue.visible()
                        bindingItem.tvdano.visible()
                    } else {
                        bindingItem.tvdanoValue.gone()
                        bindingItem.tvdano.gone()
                    }
                } else {
                    bindingItem.tvdanoValue.gone()
                    bindingItem.tvdano.gone()
                }*/


                bindingItem.tvSaveQuality.setOnClickListener { view ->
                    if ((it.qcStatus == Status.SYNC_PENDING || it.qcStatus == Status.SYNC_ERROR)
                        && bindingItem.tvQualityGradeValue.text.isNotEmpty() && bindingItem.tvBagCountValue.text?.isNotEmpty() == true
                    ) {
                        if (it.qualityGradeDesc?.contains("DAÑO") == true && bindingItem.tvdanoValue.text.isEmpty())
                            activity?.toast(getString(R.string.check_entey))
                        else
                            postQuality(it, pos)
                        /*var danovalue = isdanofound.get(pos)
                        if (danovalue == true && bindingItem.tvdanoValue.text.isNotEmpty())
                            postQuality(it, pos)
                        else if (danovalue == false)
                            postQuality(it, pos)
                        else
                            activity?.toast(getString(R.string.check_entey))*/
                    } else if (it.qcStatus == Status.SYNC_COMPLETED) {
                        //PrintTallySheet(it, false)
                        showPrintSelectionDialog(it)
                    } else {
                        activity?.toast(getString(R.string.check_entey))
                    }
                }
            })
    }

    private fun PrintTallySheet(vegaCommonSplitLotModel: VegaCommonSplitLotModel, isSync: Boolean) {
        var receivingData = VegaReceiving()
        receivingData.palletType = vegaCommonSplitLotModel.ticketNumber
        receivingData.erdat =
            this.context?.let { getUTCDateTime(getCurrentTimeInMills().toString(), it) }
        receivingData.gradeDesc = vegaCommonSplitLotModel.qualityGrade
        receivingData.qualityGradeDesc = vegaCommonSplitLotModel.qualityGradeDesc
        receivingData.certificate = vegaCommonSplitLotModel.certification
        receivingData.netWeight = vegaCommonSplitLotModel.netWeight.toString()
        receivingData.unitsOfMeasure = lotDetails.unitsOfMeasure.toString()
        receivingData.materialName = lotDetails.materialName
        receivingData.tollingVendorName = ""
        receivingData.bagCount = vegaCommonSplitLotModel.bagCount
        receivingData.batchNumber = vegaCommonSplitLotModel.batchNumber
        receivingData.storageLocationCode = this.receivingData.storageLocationCode
        /*val bundle = Bundle().apply {
            this.putParcelable(UIUtils.RECEIVING_DATA, receivingData)
        }*/
        val intent = Intent()
        intent.putExtra(UIUtils.RECEIVING_DATA, receivingData)
        //intent.putExtras(bundle)

        val printKeys = NicaraguaMTNRPrintTicket(intent, context)
        if (!isSync) {
            DoAsync {
                runOnUiThread {
                    val gson = GsonUtils()
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(printKeys))
                    startActivity(Intent(this.context, WifiMainActivity::class.java))
                }
            }.execute()
        } else {
            DoAsync {
                runOnUiThread {
                    if (printKeys.size > 0)
                        postPrintTicket(
                            TYPE_T,
                            printKeys.get(0),
                            vegaCommonSplitLotModel.batchNumber,
                            vegaCommonSplitLotModel.ticketNumber,
                            receivingData.materialCode
                        )
                }
            }.execute()

        }
    }

    private fun printSampleTicket(vegaCommonSplitLotModel: VegaCommonSplitLotModel, isSync: Boolean) {
        val printKeys = generateFgrnTallySheetBitMap(vegaCommonSplitLotModel.ticketNumber.toString(), context)
        if (!isSync) {
            DoAsync {
                runOnUiThread {
                    val gson = GsonUtils()
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(printKeys))
                    startActivity(Intent(this.context, WifiMainActivity::class.java))
                }
            }.execute()
        } else {
            DoAsync {
                runOnUiThread {
                    if (printKeys.size > 0)
                        postPrintTicket(
                            TYPE_SAMPLE,
                            printKeys.get(0),
                            vegaCommonSplitLotModel.batchNumber,
                            vegaCommonSplitLotModel.ticketNumber,
                            receivingData.materialCode
                        )
                }
            }.execute()
        }
    }

    private fun showPrintSelectionDialog(vegaCommonSplitLotModel: VegaCommonSplitLotModel) {
        var items1 = arrayListOf<String>()
        items1.add(0, getString(R.string.print_ticket))
        items1.add(1, getString(R.string.print_sample_ticket))
        MaterialDialog(requireContext()).show {
            title(text = getString(R.string.select_ticket))
            listItemsSingleChoice(items = items1) { dialog, index, text ->
                if (index == 0)
                    PrintTallySheet(vegaCommonSplitLotModel, false)
                else
                    printSampleTicket(vegaCommonSplitLotModel, false)

            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok))) {
                dismiss()
            }
        }

    }

    private fun postQuality(it: VegaCommonSplitLotModel, pos: Int) {
        currentPos = pos
        splitLotList.get(pos).qcStatus = Status.SYNC_ERROR
        val lotDetails = lotDetails.copy()
        lotDetails.batchNumber = it.batchNumber.toString()
        lotDetails.netWeight = it.netWeight.toString()
        var qualityList = arrayListOf<VegaQuality>()
        if (lotQualityDetails.isNotEmpty()) {
            lotQualityDetails.get(0).qualityParameters?.forEach { it1 ->
                val quality = VegaQuality()
                if (it1.sapQCName.equals("NIPOSITI"))
                    quality.qualityParameterValue = it.qualityGrade
                else if (it1.sapQCName.equals("NIFG0014"))
                    splitLotList.get(pos).certification = it1.satNam
                else if (it1.sapQCName.equals("NISACOS"))
                    quality.qualityParameterValue = it.bagCount
                else if (it1.sapQCName.equals("NIDANO") && it.qualityGradeDesc?.contains("DAÑO") == true)
                    quality.qualityParameterValue = it.danoValue
                else if (it1.sapQCName.equals("NICERTI"))
                    quality.qualityParameterValue = it.ticketNumber
                else
                    quality.qualityParameterValue = it1.satNam
                quality.nameChar = it1.sapQCName.toString()
                qualityList.add(quality)
            }
        }
        lotDetails.qualityDetails = qualityList
        vm.postQualityParams(
            VegaCoffeeQualityParamPost(
                grnApplicable = false,
                grnFlag = false,
                key = getCurrentKey(),
                plant = getPlantDetails(),
                lotDetails = listOf(lotDetails)
            )
        )
    }

    private fun showSingleSelectDialog(
        title: String,
        currentFalg: String
    ) {
        var list = ArrayList<String>()
        when {
            currentFalg.contains(QUALITY_GRADE) -> {
                val gradeListFilter = mutableListOf<VegaQualitative>()
                materialQualityGradeList.forEach { qualityGrade ->
                    gradeListFilter.addAll(gradeList.filter {
                        it.charValue.split(" ").get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
                    })
                }
                list = gradeListFilter.map { it.charValue.plus("-").plus(it.descValue) } as ArrayList<String>
            }
            currentFalg.contains(DANO) -> {
                if (charValue.size > 0)
                    list = charValue as ArrayList<String>
            }
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFalg,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(
        data: String,
        currentFlag: String
    ) {
        customDialog?.dismiss()
        when {
            currentFlag.contains(QUALITY_GRADE) -> {
                var pos = "0"
                pos = currentFlag.split("-").get(1)
                splitLotList[pos.toInt()].qualityGrade = data.split("-")[0]
                splitLotList[pos.toInt()].qualityGradeDesc = data.split("-")[1]
                splitLotList[pos.toInt()].danoValue = ""
                //isdanofound.put(pos.toInt(), data.split("-")[1].contains("DAÑO", false))
                binding.rvSplitLot.adapter?.notifyItemChanged(pos.toInt())
            }
            currentFlag.contains(DANO) -> {
                var pos = 0
                pos = currentFlag.split("-").get(1).toInt()
                splitLotList[pos].danoValue = data
                binding.rvSplitLot.adapter?.notifyItemChanged(pos)
            }
        }
    }

    fun getBack() {
        if (splitLotList.isNotEmpty() && splitLotList.any { it.qcStatus == Status.SYNC_PENDING })
            showSnack(getString(R.string.save_quality_all))
        else {
            callBack?.replaceFragment(splitLotList)
        }
    }

    private fun postTallyUpdateSequence() {
        val rightNow = Calendar.getInstance()
        var currentmonth = rightNow.get(Calendar.MONTH).toString()
        var currentyear = rightNow.get(Calendar.YEAR).toString()

        val year =
           if (currentmonth.equals("10") || currentmonth.equals("11") || currentmonth.equals("12")) (currentyear + 1) else currentyear

        val prefix1 = Constants.TALLY_SHEET
        val prefix3 = Constants.TALLY_SHEET
        var tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        var tallysequence = ""

        if (tallySequence.isNotEmpty()) {
            val talSeq = tallySequence.substring(tallySequence.length - 5)
            if (talSeq.isNotEmpty()) tallysequence = (talSeq.toInt() - 1).toString()
        }
        when (tallysequence.length) {
            1 -> tallySequence = "0000".plus(tallysequence)
            2 -> tallySequence = "000".plus(tallysequence)
            3 -> tallySequence = "00".plus(tallysequence)
            4 -> tallySequence = "0".plus(tallysequence)
            5 -> tallysequence
        }
        val postData = SplitUpdateTallySequencePost(
            getPlantDetails(),
            prefix1,
            year,
            tallySequence,
            "",
            "N",
            "",
            "",
            "",
            "N",
            "",
            "N",
            "Y",
            "N",
            "",
            prefix3
        )
        vm.updateTallySequence(postData)
    }

    private fun UpdateTally(response: Resource<GenericReqAndResp<SplitUpdateTallySequencePost>>?) {
        val tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                PrintTallySheet(splitLotList.get(currentPos), true)
                printSampleTicket(splitLotList.get(currentPos), true)
                if (response.data?.success == true) {

                }
            }
            Resource.Status.LOADING -> showCustomLoading()
            Resource.Status.ERROR -> {
                hideCustomLoading()
                PrintTallySheet(splitLotList.get(currentPos), true)
                printSampleTicket(splitLotList.get(currentPos), true)
                showErrorDialogWithFAQLink(requireContext(), "${response.data?.errors}")
            }
            else -> {}
        }
    }

    private fun postPrintTicket(
        isTicketType: String,
        printTicketKey: String,
        batchNumber: String?,
        grnNumber: String?,
        materialCode: String?
    ) {
        PreferenceHelper.save(TICKET_KEY, printTicketKey)
        val input = workDataOf(
            BATCH_NO to batchNumber, GRN_NO to grnNumber, UIUtils.MATERIAL to materialCode,
            PRINT_TYPE to isTicketType
        )
        val worker = getSplitPrintTicketOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            if (isTicketType.equals(TYPE_R)) {
                                DoAsync {
                                    runOnUiThread {
                                        hideLoading()
                                        val gson = GsonUtils()
                                        PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(printReceiptKeys))
                                        startActivity(Intent(this.context, WifiMainActivity::class.java))
                                    }
                                }.execute()

                            }
                        }
                        WorkInfo.State.FAILED -> {
                            if (isTicketType.equals(TYPE_R)) {
                                DoAsync {
                                    runOnUiThread {
                                        hideLoading()
                                        val gson = GsonUtils()
                                        PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(printReceiptKeys))
                                        startActivity(Intent(this.context, WifiMainActivity::class.java))
                                    }
                                }.execute()
                            }
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {}
                    }
                }

            })
    }

}
