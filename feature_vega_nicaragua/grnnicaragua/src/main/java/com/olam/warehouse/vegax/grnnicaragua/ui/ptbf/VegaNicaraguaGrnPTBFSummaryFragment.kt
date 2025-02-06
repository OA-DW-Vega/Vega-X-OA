package com.olam.warehouse.vegax.grnnicaragua.ui.ptbf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveGrnSequence
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.*
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnPtbfSummaryBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaGrnSummaryBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.getFormatedDate
import com.olam.warehouse.vegax.grnnicaragua.utils.isdatefunc
import com.olam.warehouse.vegax.grnnicaragua.utils.saveLotSequence
import com.olam.warehouse.vegax.grnnicaragua.work.getGrnLotSequnceOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class VegaNicaraguaGrnPTBFSummaryFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaNicaraguaGrnPtbfSummaryBinding
    private var receivingData = VegaReceiving()
    private var pricingInfo = VegaNicaraguaInvoicePriceInfo()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private var transactionList = mutableListOf<VegaReceiving>()
    private val vm: VegaNicaraguaGrnViewModel by viewModel()
    var count: Int = 0
    private var intent = Intent()
    private var isMoveOfflineSuccess = false

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            data: Bundle
        )
    }

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaGrnPTBFSummaryFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_ptbf_summary

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnPtbfSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnPTBF/ui/VegaNicaraguaGrnPTBFSummaryFragment")
            .title("GRN PTBF Summary")
            .with(tracker)
        intent = Intent(requireContext(), SuccessActivity::class.java)
        initExtra()
        initUI()
    }

    private fun initUI() {

        setVendorData()

        setQualityDetails()

        setPricingInfo()
        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)

        binding.btnProceed.setOnClickListener(View.OnClickListener {
            isMoveOfflineSuccess = false
            if (AppUtils.isOnline()) {
                if (!receivingData.postDate.toString().isNullOrEmpty()) {
                    if (isdatefunc(receivingData.postDate.toString()))
                        showConfirmDialog()
                    else
                        savefutureData()
                } else {
                    showConfirmDialog()
                }

            } else
                showConfirmDialog()
        })
        binding.btnSave.setOnClickListener { saveEditData() }
        vm.postGRN.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            receivingData.syncStatusMsg =
                                if (it.message.isNotEmpty()) it.message else it.data.errorMessage.toString()
                            receivingData.netPayment = binding.tvTotalPrice.text.toString()
                            vm.updateGrnDataSuccess(it.data, receivingData)
                            if (it.data.wbFlag == true && it.data.qcFlag == true && it.data.grnFlag == true)
                                moveToSuccessPage(
                                    it.message,
                                    it.data.wbId,
                                    it.data.batchNumber,
                                    it.data.grnNumber,
                                    it.data.poNumber
                                )
                            else
                                moveToFailurePage(it.data.errorMessage.toString())
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        receivingData.netPayment = binding.tvTotalPrice.text.toString()
                        receivingData.syncStatusMsg = it.error.toString()
                        vm.updateGrnDataSuccess(VegaNicaraguaGrnPost(), receivingData)
                        hideLoading()
                        //showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                        moveToFailurePage(
                            if (it.error.toString().isNotEmpty()) it.error.toString() else ""
                        )
                    }
                }
            }
        })
        vm.configItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })
        vm.grnTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateTransUI(it) })
        vm.getReceivingWithLineItem()
    }

    private fun updateTransUI(it: List<VegaReceiving>?) {
        it?.let {
            transactionList.clear()
            transactionList.addAll(it)
        }
        if(isMoveOfflineSuccess){
            isMoveOfflineSuccess = false
            saveLotSequence(receivingData.batchNumber!!, transactionList, false)
            saveGrnSequence(transactionList, false)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun moveToSuccessPage(
        message: String,
        wbid: String?,
        batchNumber: String?,
        grnNumber: String?,
        poNumber: String?
    ) {
        if (AppUtils.isOnline() && batchNumber!!.isNotEmpty()) postUpdateLotSequence(batchNumber.toString())
        /*else {
            saveLotSequence(batchNumber!!, transactionList, false)
            saveGrnSequence(transactionList, false)
        }*/
        //val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, message)
        receivingData.grnNumber = grnNumber
        receivingData.invoiceNumber = "TEM" + Calendar.getInstance().timeInMillis
        if (AppUtils.isOnline()) {
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.weighbridge_is).plus(wbid).plus(getString(R.string.batch_no_is)).plus(batchNumber)
                    .plus(getString(R.string.grn_no_is)).plus(grnNumber).plus(getString(R.string.po_no_is))
                    .plus(poNumber)
            )
        } else {
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.weighbridge_is).plus(wbid).plus(getString(R.string.batch_no_is)).plus(batchNumber)
                    .plus(getString(R.string.grn_no_is)).plus(grnNumber)
            )
        }
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra(UIUtils.RECEIVING_DATA, receivingData)
        intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
        intent.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_GRN_RECEIPT)
        intent.putParcelableArrayListExtra(UIUtils.BAGS_DATA, weighDetails as ArrayList<out Parcelable>)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard(batchNumber))
//        startActivity(intent)
//        requireActivity().finish()
    }

    private fun moveToFailurePage(msg: String) {
        if (AppUtils.isOnline()) postUpdateLotSequence(receivingData.batchNumber.toString())
        else {
            isMoveOfflineSuccess = true
           /* saveLotSequence(receivingData.batchNumber!!, transactionList, false)
            saveGrnSequence(transactionList, false)*/
        }
        //val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.transaction_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(getString(R.string.transaction_retry)))
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
        //startActivity(intent)
    }

    private fun setVendorData() {
        binding.tvMaterial.text = receivingData.materialName
        binding.tvGrade.text = receivingData.grade
        binding.tvStorageLoctaion.text = receivingData.storageLocationName
        binding.tvNetWeight.text = pricingInfo.netWeight
        binding.vendorNameTitle.text = receivingData.supplierName
        binding.tvpostdateval.text = receivingData.postDate
    }

    private fun postUpdateLotSequence(batchNumber: String?) {
        val input = workDataOf(UIUtils.GRN_DATA to receivingData.grnType)
        val worker = getGrnLotSequnceOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        WorkInfo.State.FAILED -> {
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {}
                    }
                }

            })
    }

    private fun setQualityDetails() {

        binding.qualityDetails.setOnClickListener(View.OnClickListener {
            if (binding.qualityDetailsLayout.isVisible) {
                binding.qualityDetailsLayout.visibility = View.GONE
                binding.qualityDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0,
                    R.drawable.ic_baseline_expand_more, 0
                )
            } else {
                binding.qualityDetailsLayout.visibility = View.VISIBLE
                binding.qualityDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0,
                    R.drawable.ic_baseline_expand_less, 0
                )
            }
        })

        binding.rvQuality.setUpAdapter(
            qualityParameterList,
            R.layout.item_vega_nicaragua_grn_summary,
            ItemVegaNicaraguaGrnSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.description.text = it!!.descrChar
                bindItem.value.text = it.qualityParameterValue!!.trim()
            })
        binding.rvQuality.isNestedScrollingEnabled = false
    }

    private fun setPricingInfo() {
        binding.priceCalculationDetails.setOnClickListener(View.OnClickListener {
            if (binding.priceCalculationLayout.isVisible) {
                binding.priceCalculationLayout.visibility = View.GONE
                binding.priceCalculationDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0,
                    R.drawable.ic_baseline_expand_more, 0
                )
            } else {
                binding.priceCalculationLayout.visibility = View.VISIBLE
                binding.priceCalculationDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0,
                    R.drawable.ic_baseline_expand_less, 0
                )
            }
        })

        binding.tvNetWeight.text = receivingData.netWeight + " KG(s)"
        binding.tvNetWeight2.text = receivingData.netWeight + " KG(s)"
        binding.tvBasePrice.text = formatString(receivingData.materialPrice!!)
        binding.tvExchangeRate.text =
            if (receivingData.exchangeRate?.isEmpty() == true) pricingInfo.exchangeRate else receivingData.exchangeRate

        val totalPrice = receivingData.materialPrice!! * receivingData.netWeight.toDouble()

        binding.tvTotalPrice.text = formatString(totalPrice)

    }


    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return getString(R.string.c_doller) + " " + strFormat.format(this).replace(",", "")
    }

    private fun initExtra() {
        arguments?.let {
            receivingData = it.getParcelable(UIUtils.RECEIVING_DATA)!!
            pricingInfo = it.getParcelable(UIUtils.INVOICE_PRICE_INFO)!!
            qualityParameterList = it.getParcelableArrayList<VegaQualityParameter>(UIUtils.QUALITY_DATA)!!
            priceDetails = it.getParcelableArrayList<VegaNicaraguaGrnPriceDetails>(UIUtils.PRICE_DATA)!!
            weighDetails = it.getParcelableArrayList<VegaNicaraguaWeighmentBagMaterial>(UIUtils.BAGS_DATA)!!
            updatePriceDeatilsToDB()
            if (receivingData.isEdit) binding.btnSave.visible()

        }
    }

    private fun updatePriceDeatilsToDB() {
        receivingData.exchangeRate = pricingInfo.exchangeRate
        receivingData.bagCount =
            weighDetails?.sumOf { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }.toString()
        receivingData.grossWeight =
            weighDetails?.sumOf { if (it.grossWeight.isNotEmpty()) it.grossWeight.toDouble() else 0.0 }
                .toString()
        receivingData.tareWeight = weighDetails?.sumOf {
            if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() ?: 0.0 else 0.0
        }.toString()
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.grn_proceed)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    receivingData.plantName = getPlantDetails().plantName
                    receivingData.plantId = getPlantDetails().plantId
                    receivingData.price =
                        binding.tvBasePrice.text.toString()
                            .replace(getString(R.string.c_doller), "").trim()
                    receivingData.createdDate =
                        DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
                    receivingData.erdat = DateUtils.getCurrentTimeInMills().toString()
                    receivingData.docDate =
                        DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
                    receivingData.netPayment = binding.tvTotalPrice.text.toString()
                    if (AppUtils.isOnline())

                        postCreateGRN()
                    else
                        saveData()
                },
                { dismiss() })
        }
    }

    private fun saveData() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        receivingData.price = binding.tvBasePrice.text.toString().replace(getString(R.string.c_doller), "").trim()
        receivingData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = DateUtils.getCurrentTimeInMills().toString()
        receivingData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.netPayment = binding.tvTotalPrice.text.toString()
        isMoveOfflineSuccess = true
        vm.saveGrnData(receivingData)
        moveToSuccessPage(
            getString(R.string.grn_offline_success),
            receivingData.tmpWbId,
            receivingData.batchNumber,
            receivingData.palletType,
            ""
        )
    }

    private fun savefutureData() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        receivingData.price = binding.tvBasePrice.text.toString().replace(getString(R.string.c_doller), "").trim()
        receivingData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = DateUtils.getCurrentTimeInMills().toString()
        receivingData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.netPayment = binding.tvTotalPrice.text.toString()
        isMoveOfflineSuccess = true
        vm.saveGrnData(receivingData)
        moveToFailurePage(getString(R.string.future_date_pendinglist))
    }

    private fun saveEditData() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        receivingData.price = binding.tvBasePrice.text.toString().replace(getString(R.string.c_doller), "").trim()
        receivingData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = DateUtils.getCurrentTimeInMills().toString()
        receivingData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.netPayment = binding.tvTotalPrice.text.toString()
        vm.saveGrnData(receivingData)
        showEditConfirmDialog()
    }

    private fun showEditConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.edit_confirm)
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true)) {
                activity?.finish()
            }
        }
    }

    private fun postCreateGRN() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        vm.updateSyncStartedStatus(receivingData.tmpWbId)
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val post = VegaNicaraguaGrnPost()

        post.batchNumber = receivingData.batchNumber
        post.certificate = receivingData.certificate
        post.qualityGradeDesc = receivingData.gradeDesc
        post.cascara = receivingData.cascara
        post.humedad = receivingData.humedad
        post.rendimientoBruto = receivingData.rendimientoBruto
        post.exchangeRate = pricingInfo.exchangeRate
        post.grade = null
        post.key = currentKey
        post.grnType = receivingData.grnType
        post.plant = getPlantDetails()
        post.grnData = preparingGrnData()
        post.lotDetails = preparingLotDetails()
        post.priceDetails = priceDetails
        post.tollingDetails = null
        //adding weighDetails
        post.weighDetails = weighDetails
        post.poNumber = receivingData.purchaseDocNum
        post.tempGRNNumber = receivingData.palletType
        if (DateUtils.isFirstDayOfMonth(requireContext(), count)) {
            vm.postCreateGRN(post)
        } else {
            showSnack(getString(R.string.month_close_error))
        }
    }

    private fun preparingGrnData(): ArrayList<VegaNicaraguaGrnData> {
        val grnDataList = ArrayList<VegaNicaraguaGrnData>()
        val grnData = VegaNicaraguaGrnData()
        grnData.batchNumber = receivingData.batchNumber
        grnData.currency = "NIO"
        grnData.price = binding.tvBasePrice.text.toString().replace(getString(R.string.c_doller), "").trim()
        grnData.netWeight = receivingData.netWeight
        grnData.materialCode = receivingData.materialCode
        grnData.supplierCode = receivingData.supplierCode
        grnData.weighBridgeType = receivingData.weighBridgeType
        grnData.weighBridgeId = receivingData.weighBridgeId
        grnData.unitsOfMeasure = receivingData.unitsOfMeasure
        grnData.purchaseDocNum = receivingData.purchaseDocNum
        grnData.storageLocationCode = receivingData.storageLocationCode
        grnData.plant = receivingData.plantId
        grnData.item = receivingData.item
        grnData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        grnData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        grnData.postDate = getFormatedDate(receivingData.postDate.toString())
        grnDataList.add(grnData)

        return grnDataList
    }

    private fun preparingLotDetails(): ArrayList<VegaNicaraguaLotDetails> {
        val lotDetailsList = ArrayList<VegaNicaraguaLotDetails>()
        val lotDetails = VegaNicaraguaLotDetails()

        lotDetails.batchNumber = receivingData.batchNumber
        lotDetails.netWeight = receivingData.netWeight
        lotDetails.materialCode = receivingData.materialCode
        lotDetails.supplierCode = receivingData.supplierCode
        lotDetails.weighBridgeType = receivingData.weighBridgeType
        lotDetails.weighBridgeId = receivingData.weighBridgeId
        lotDetails.unitsOfMeasure = receivingData.unitsOfMeasure
        lotDetails.purchaseDocNum = receivingData.purchaseDocNum
        lotDetails.storageLocationCode = receivingData.storageLocationCode
        lotDetails.storageLocation = receivingData.storageLocationName
        lotDetails.grossWeight = receivingData.grossWeight
        lotDetails.bagType = receivingData.bagType
        lotDetails.plant = receivingData.plantId
        lotDetails.item = receivingData.item
        //adding quality details to lot
        val qualityDetailsList = ArrayList<VegaNicaraguaQualityDetails>()
        qualityParameterList.forEach {

            val qualityDetail = VegaNicaraguaQualityDetails()
            qualityDetail.descrChar = it!!.descrChar
            qualityDetail.nameChar = it.nameChar
            qualityDetail.qualityParameterValue = it.qualityParameterValue
            qualityDetailsList.add(qualityDetail)
        }

        lotDetails.qualityDetails = qualityDetailsList
        lotDetailsList.add(lotDetails)

        return lotDetailsList
    }

    private fun prepareLotCard(batchNumber: String?): ArrayList<VegaCoffeeSalesLots> {
        val lotList = arrayListOf<VegaCoffeeSalesLots>()
        val lot = VegaCoffeeSalesLots()
        lot.batchNumber = batchNumber.toString()
        lot.editedWeight = receivingData.netWeight
        lot.unitOfMeasure = receivingData.unitsOfMeasure
        lot.materialName = receivingData.materialName
        lot.materialCode = receivingData.materialCode ?: ""
        lot.grade = receivingData.gradeDesc
        lot.certificate = receivingData.certificate
        lot.storageLocationCode = receivingData.storageLocationCode
        lotList.add(lot)
        return lotList
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.DAYS_LIMIT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            if (!it.value!!.isNullOrEmpty())
                                count = it.value!!.toInt()
                            else
                                count = 0
                        }
                        it.applicable?.contains("N")!! -> {
                            count = 0
                        }
                    }
                }
            }
        }
    }
}
