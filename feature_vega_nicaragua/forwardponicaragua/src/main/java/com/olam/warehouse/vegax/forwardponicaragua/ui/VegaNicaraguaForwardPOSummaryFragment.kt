package com.olam.warehouse.vegax.forwardponicaragua.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPOPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPost
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.forwardponicaragua.R
import com.olam.warehouse.vegax.forwardponicaragua.databinding.FragmnetVegaNicaraguaForwardPoPreviewBinding
import com.olam.warehouse.vegax.forwardponicaragua.work.getPOLotSequnceOneTimeRequestWorker
import com.olam.warehouse.vegax.grnecuador.utils.covertToDouble
import kotlinx.android.synthetic.main.item_vega_nicaragua_forward_po_summary_yield.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*


class VegaNicaraguaForwardPOSummaryFragment : BaseFragment() {

    private var callBack: Callback? = null
    private lateinit var binding: FragmnetVegaNicaraguaForwardPoPreviewBinding
    private var receivingData = VegaNicaraguaForwardPODetails()
    private var receivingDatares = VegaNicaraguaForwardPoPost()
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var filteredPriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private val vm: VegaNicaraguaForwardPOViewModel by viewModel()
    var count:Int=0

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaForwardPOSummaryFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragmnet_vega_nicaragua_forward_po_preview

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmnetVegaNicaraguaForwardPoPreviewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("FORWORDPO/ui/VegaNicaraguaForwordPOPreviewFragment")
            .title("FORWORD PO Preview ")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        arguments?.let {
            if (it.getBoolean(UIUtils.FROM_TRANSACTION,false))
            {
                binding.btnProceed.visibility = View.GONE
            }
            receivingData=it.getParcelable(UIUtils.RECEIVING_DATA)!!
            receivingData.poSequenceNumber=vm.generatePoSequnceNumber()
            priceDetails=it.getParcelableArrayList<VegaNicaraguaGrnPriceDetails>(UIUtils.YIELD_DATA)!!
            if(!UIUtils.FILTEREDPRICEDETAILS.isNullOrEmpty())
            filteredPriceDetails=it.getParcelableArrayList<VegaNicaraguaGrnPriceDetails>(UIUtils.FILTEREDPRICEDETAILS)!!
        }
    }

       private fun initUI() {

        setPricingInformation()
        setTransactionInformation()
        setYieldInformation()
        if (!receivingData.poNumber.isNullOrEmpty()) {
            binding.btnProceed.visibility = View.GONE
        }
           vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        vm.postForwardPo.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        // var data = Bundle()
                        receivingDatares = it.data!!.data
                        // data.putParcelable(UIUtils.RECEIVING_DATA, receivingDatares)
                        receivingData.syncStatusMsg = it.data?.message
                        receivingData.poNumber = it.data?.data?.poNumber
                        receivingData.syncStatus = true
                        vm.saveForwardPoData(receivingData)
                        vm.saveForwardPoPriceDetails(prepareForwardPoPriceDetails())
                        if (it.data?.data?.poNumber?.isNotEmpty() == true)
                            moveToSuccess(
                                it.data?.data?.poNumber.toString(),
                                it.data?.message.toString()
                            )
                        else
                            moveToFailurePage(it.data!!.message)
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        receivingData.syncStatusMsg = it.error.toString()
                        vm.saveForwardPoData(receivingData)
                        vm.saveForwardPoPriceDetails(prepareForwardPoPriceDetails())
                        hideLoading()
                        UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    }
                }
            }
        })

        binding.btnProceed.setOnClickListener(View.OnClickListener {
             showConfirmDialog()
        })
           vm.configItems.observe(this, androidx.lifecycle.Observer {
               updateConfigItems(it)
           })
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.forward_po_proceed)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    receivingData.createdDate =
                        DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
                    receivingData.docDate =
                        DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

                    if (AppUtils.isOnline())
                        postCreateForwardPO()
                    else
                        saveData()
                },
                { dismiss() })
        }
    }

    private fun saveData() {
        vm.saveForwardPoData(receivingData)
        priceDetails.addAll(filteredPriceDetails)
        vm.saveForwardPoPriceDetails(prepareForwardPoPriceDetails())
        savePOSequence()
        moveToSuccess(
            receivingData.tempId,
            getString(R.string.forward_po_offline_success)
        )
    }

    private fun prepareForwardPoPriceDetails(): ArrayList<VegaNicaraguaForwardPOPriceDetails> {
        val forwardPoPriceDetails = arrayListOf<VegaNicaraguaForwardPOPriceDetails>()

        priceDetails.forEach {
            val lineItem = VegaNicaraguaForwardPOPriceDetails()
            lineItem.tempId = receivingData.tempId
            lineItem.fieldName =if(it.fieldName.isNotEmpty()) it.fieldName else it.description.toString()
            lineItem.companyCode = it.companyCode
            lineItem.division = it.division
            lineItem.plant = it.plant
            lineItem.purchasingOrg = it.purchasingOrg
            lineItem.purchasingGroup = it.purchasingGroup
            lineItem.description = it.description
            lineItem.priceDate = it.priceDate
            lineItem.price = it.price
            lineItem.differential = it.differential
            lineItem.currency = it.currency
            lineItem.baseUnit = it.baseUnit
            lineItem.createdOn = it.createdOn
            lineItem.percentage = it.percentage
            forwardPoPriceDetails.add(lineItem)
        }
        return forwardPoPriceDetails
    }

    private fun postCreateForwardPO() {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val post = VegaNicaraguaForwardPoPost()
        post.cascara = receivingData.cascara
        post.certificate = receivingData.certificate
        post.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        post.currency = receivingData.currency
        post.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        post.exchangeRate = receivingData.exchangeRate
        post.grade = receivingData.grade
        post.humedad = receivingData.humedad
        post.key = currentKey
        post.materialCode = receivingData.materialCode
        post.materialName = receivingData.materialName
        post.netPrice = receivingData.netPrice
        post.netWeight = receivingData.netWeight
        post.plant = getPlantDetails()
        post.priceDetails = priceDetails
        post.pricePerUnit = receivingData.pricePerUnit
        post.qualityGradeDesc = receivingData.qualityGradeDesc
        post.rendimientoBruto = receivingData.rendimientoBruto
        post.unitsOfMeasure = receivingData.unitsOfMeasure
        post.vendorCode = receivingData.vendorCode
        post.vendorName = receivingData.vendorName
        post.taxNumber=receivingData.taxNumber
        post.poSequenceNumber=receivingData.poSequenceNumber
        post.vendorAddress=receivingData.vendorAddress
        post.receiptNetPrice=receivingData.receiptNetPrice
        post.deliveryDate=receivingData.deliveryDate
        post.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        if (DateUtils.isFirstDayOfMonth(context!!,count)) {
            vm.postCreateForwardPO(post)
        } else {
            showSnack(getString(R.string.month_close_error))
        }

    }
  fun  prepareofflinedata():VegaNicaraguaForwardPoPost
    {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val post = VegaNicaraguaForwardPoPost()
        post.cascara = receivingData.cascara
        post.certificate = receivingData.certificate
        post.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        post.currency = receivingData.currency
        post.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        post.exchangeRate = receivingData.exchangeRate
        post.grade = receivingData.grade
        post.humedad = receivingData.humedad
        post.key = currentKey
        post.materialCode = receivingData.materialCode
        post.materialName = receivingData.materialName
        post.netPrice = receivingData.netPrice
        post.netWeight = receivingData.netWeight
        post.plant = getPlantDetails()
        post.priceDetails = priceDetails
        post.pricePerUnit = receivingData.pricePerUnit
        post.qualityGradeDesc = receivingData.qualityGradeDesc
        post.rendimientoBruto = receivingData.rendimientoBruto
        post.unitsOfMeasure = receivingData.unitsOfMeasure
        post.vendorCode = receivingData.vendorCode
        post.vendorName = receivingData.vendorName
        post.poNumber=receivingData.tempId
        post.poSequenceNumber=receivingData.poSequenceNumber
        post.taxNumber=receivingData.taxNumber
        post.vendorAddress=receivingData.vendorAddress
        post.receiptNetPrice=receivingData.receiptNetPrice
        post.deliveryDate=receivingData.deliveryDate
        post.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        return  post
    }

    private fun setPricingInformation() {
        binding.priceDetails.setOnClickListener(View.OnClickListener {
            if (binding.priceDetailsLayout.isVisible) {
                binding.priceDetailsLayout.visibility = View.GONE
                binding.priceDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_baseline_expand_more_24,
                    0
                )
            } else {
                binding.priceDetailsLayout.visibility = View.VISIBLE
                binding.priceDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_baseline_expand_less_24,
                    0
                )
            }
        })


        binding.tvExchangeRate.text = receivingData.exchangeRate
        binding.tvWeight.text = receivingData.netWeight
        binding.pricePerUnit.text = getString(R.string.c_doller) + " " + receivingData.pricePerUnit
        binding.DeliverydateUnit.text =  receivingData.deliveryDate
        binding.pricePerQQ.text =receivingData.receiptNetPrice
        binding.tvTotalPrice.text =
            formatString(covertToDouble(receivingData.netWeight) * covertToDouble(receivingData.pricePerUnit))

    }

    private fun setYieldInformation()
    {
        binding.yieldDetails.setOnClickListener(View.OnClickListener {
            if(binding.yieldDetailsLayout.isVisible) {
                binding.yieldDetailsLayout.visibility = View.GONE
                binding.yieldDetails.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_expand_more_24,0)
            }else
            {
                binding.yieldDetailsLayout.visibility = View.VISIBLE
                binding.yieldDetails.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_expand_less_24,0)
            }
        })

        binding.rvYield.setUp(filteredPriceDetails , R.layout.item_vega_nicaragua_forward_po_summary_yield, { it, pos ->
            description.text = it.description
            value.text = it.price!!.trim()
        })
        binding.rvYield.isNestedScrollingEnabled = false
    }

    private fun setTransactionInformation()
    {
        binding.transactionInformation.setOnClickListener(View.OnClickListener {
            if(binding.transactionInformationLayout.isVisible) {
                binding.transactionInformationLayout.visibility = View.GONE
                binding.transactionInformation.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_expand_more_24,0)
            }else
            {binding.transactionInformationLayout.visibility = View.VISIBLE
                binding.transactionInformation.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_expand_less_24,0)
            }
        })

        binding.tvVendorName.text = receivingData.vendorName
        binding.tvMaterial.text = receivingData.materialName
        binding.tvQualityGrade.text = receivingData.grade
    }

    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return getString(R.string.c_doller) + " " + strFormat.format(this).replace(",", "")
    }

    private fun moveToFailurePage(msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.transaction_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(getString(R.string.transaction_retry)))
        intent.putExtra(AppUtils.FAILURE, false)
        startActivity(intent)
    }

    private fun moveToSuccess(poNumber: String, message: String) {
        if (AppUtils.isOnline()) postUpdateLotSequence(receivingData.poNumber.toString())
        val intent = Intent(requireContext(), SuccessActivity::class.java)

        intent.putExtra(AppUtils.TITLE, message)
        intent.putExtra(AppUtils.SUB_TITLE, "Id : " + poNumber)
        intent.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_FORWARDPO_RECEIPT)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
        if(AppUtils.isOnline())
        {
            intent.putExtra(UIUtils.RECEIVING_DATA, receivingDatares)
            if(!receivingData.netWeight.isNullOrEmpty())
            {
                var input = receivingDatares.netWeight.toString()
                var qw = java.lang.Double.valueOf(input)
                var d = qw / 46
                intent.putExtra(UIUtils.NETWEIGHT, d.formatTwoDigits())
            }
        }
        else{
            val data:VegaNicaraguaForwardPoPost=prepareofflinedata()
            intent.putExtra(UIUtils.RECEIVING_DATA, data)
            var input = receivingData.netWeight.toString()
            var qw = java.lang.Double.valueOf(input)
            var d = qw / 46
            intent.putExtra(UIUtils.NETWEIGHT, d.formatTwoDigits())
        }
        startActivity(intent)
        requireActivity().finish()
    }
    fun savePOSequence() {
        val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
        if (!isEditTrans) {
            val currentPOSeq = PreferenceHelper.get(Constants.PO_SEQUENCE, "")
            if (currentPOSeq.isNotEmpty()) {
                val userIndicator = currentPOSeq.substring(0, 1)

                val poSequence = currentPOSeq.substring(currentPOSeq.length - 5).toInt().inc()
                when (poSequence.toString().length) {
                    1 -> PreferenceHelper.save(
                            Constants.PO_SEQUENCE,
                            userIndicator.plus("0000".plus(poSequence.toString()))
                    )
                    2 -> PreferenceHelper.save(
                            Constants.PO_SEQUENCE,
                            userIndicator.plus("000".plus(poSequence.toString()))
                    )
                    3 -> PreferenceHelper.save(
                            Constants.PO_SEQUENCE,
                            userIndicator.plus("00".plus(poSequence.toString()))
                    )
                    4 -> PreferenceHelper.save(
                            Constants.PO_SEQUENCE,
                            userIndicator.plus("0".plus(poSequence.toString()))
                    )
                    5 -> PreferenceHelper.save(Constants.PO_SEQUENCE, userIndicator.plus(poSequence.toString()))
                }
            }
        }
    }
    private fun postUpdateLotSequence(batchNumber: String?) {
        val input = workDataOf(UIUtils.FORWARD_PO_DATA to receivingData.poNumber)
        val worker = getPOLotSequnceOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
//                            saveInvoiceSequence()
                        }
                        WorkInfo.State.FAILED -> {
//                            saveInvoiceSequence()
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                    }
                }

            })
    }
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.DAYS_LIMIT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            if(!it.value.isNullOrEmpty())
                            count = it.value!!.toInt()
                            else
                                count=0
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


