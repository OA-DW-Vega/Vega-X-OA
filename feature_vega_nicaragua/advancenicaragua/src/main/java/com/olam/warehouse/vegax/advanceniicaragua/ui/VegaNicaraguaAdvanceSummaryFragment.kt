package com.olam.warehouse.vegax.advanceniicaragua.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceTransactionDetails
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.advancenicaragua.R
import com.olam.warehouse.vegax.advancenicaragua.databinding.FragmentVegaNicaraguaAdvanceSummaryBinding
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostRequest
import com.olam.warehouse.vegax.advanceniicaragua.utils.postfixToPrefix
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaNicaraguaAdvanceSummaryFragment : BaseFragment() {

    private var callBack: Callback? = null
    private var receiveData = VegaNicaraguaAdvanceTransactionDetails()
    private lateinit var binding: FragmentVegaNicaraguaAdvanceSummaryBinding
    var count:Int=0

    private val vm: VegaNicaraguaAdvanceViewModel by viewModel()

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaAdvanceSummaryFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_advance_summary

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaAdvanceSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("advanceCreation/ui/VegaNicaraguaAdvanceSummaryFragment")
            .title("Advance Summary")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initUI() {
        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        binding.vendorNameTitle.text = receiveData.vendorName
        binding.tvVendorName.text = receiveData.vendorName
        binding.tvVendorId.text = receiveData.vendorCode

        binding.tvCreditLimit.text = receiveData.creditLimit
        binding.tvRequestAdvance.text = receiveData.requestedAdvanceAmount
        binding.tvAvailableLimit.text = postfixToPrefix(receiveData.availableLimitSign)
        binding.tvExchnageRate.text = receiveData.exchangeRate
        binding.tvTenure.text = receiveData.tenureInDays + " DAY(S)"
        binding.tvMaturity.text = receiveData.maturityDate
        binding.tvPromissoryNumber.text = receiveData.promissoryNumber

        binding.btnProceed.setOnClickListener(View.OnClickListener {
            showConfirmDialog()
        })

        vm.configItems.observe(this, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })
        vm.createAdvance.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {


                        if((it.data!!.data.cashJournalDocumentNumber.isNullOrEmpty()) || it.data!!.data.accountingDocNumber.isNullOrEmpty())
                            {
                                receiveData.syncStatusMsg =  it.data!!.data.errorMessage
                                receiveData.erdat = DateUtils.getCurrentTimeInMills().toString()
                                vm.saveTransactionAdvanceData(receiveData)
                                UIUtils.showErrorDialog(requireContext(), it.data!!.data.errorMessage)
                            }else {
                            receiveData.syncStatusMsg = it.data!!.message
                            receiveData.documentNumber = it.data!!.data.cashJournalDocumentNumber
                            receiveData.accountingNumber = it.data!!.data.accountingDocNumber
                            receiveData.erdat = DateUtils.getCurrentTimeInMills().toString()
                            receiveData.syncStatus = true
                            vm.saveTransactionAdvanceData(receiveData)
                            moveToSuccess(
                                it.data!!.data.cashJournalDocumentNumber,
                                it.data!!.data.accountingDocNumber,
                                (if (it.data?.data?.cashJournalMessage.isNullOrEmpty()) getString(R.string.advance_online_success) else it.data!!.data.cashJournalMessage) + "\n" + (if (it.data?.data?.accountingDocMessage.isNullOrEmpty()) getString(
                                    R.string.accounting_online_success
                                ) else it.data!!.data.accountingDocMessage)
                            )

                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        receiveData.syncStatusMsg = it.error.toString()
                        receiveData.erdat = DateUtils.getCurrentTimeInMills().toString()
                        vm.saveTransactionAdvanceData(receiveData)
                        hideLoading()
                        UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    }
                }
            }
        })

    }

    private fun initExtra() {

        arguments?.let {
            receiveData = it.getParcelable(UIUtils.RECEIVING_DATA)!!

            if (it.getBoolean(UIUtils.FROM_TRANSACTION,false))
            {
                binding.btnProceed.visibility = View.GONE

                if(!receiveData.documentNumber.isNullOrEmpty())
                {
                    binding.cashJournalNumberLayout.visibility = View.VISIBLE
                    binding.accountingNumberLayout.visibility = View.VISIBLE

                    binding.tvAccountingNumber.text = receiveData.documentNumber
                    binding.tvCashJournalNumber.text= receiveData.accountingNumber
                }

            }

        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.advance_proceed)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (AppUtils.isOnline())
                        postCreateForwardPO()
                    else
                        saveData()
                },
                { dismiss() })
        }
    }

    private fun saveData() {
        receiveData.erdat = DateUtils.getCurrentTimeInMills().toString()
        vm.saveTransactionAdvanceData(receiveData)

        moveToSuccess(receiveData.tempId,"", getString(R.string.advance_offline_success))
    }

    private fun postCreateForwardPO() {

        val post = VegaNicaraguaAdvancePostRequest()
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

        post.totalAmount = receiveData.requestedAdvanceAmount
        post.vendorName = receiveData.vendorName
        post.vendorNo=receiveData.vendorCode
        post.documentNumber=receiveData.promissoryNumber
        post.plant = getPlantDetails()
        post.key=currentKey
        post.advanceFlag=true

        if (DateUtils.isFirstDayOfMonth(context!!,count)) {
            vm.postCreateAdvance(post)
        } else {
            showSnack(getString(R.string.month_close_error))
        }

    }

    private fun moveToSuccess(docNumber: String,accountingNumber: String, message: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, message)
        if(accountingNumber.isNullOrEmpty())
        {
            intent.putExtra(
                AppUtils.SUB_TITLE,
                "ID : " + docNumber
            )
        }else {
            intent.putExtra(
                AppUtils.SUB_TITLE,
                "Cash Journal DocumentNumber : " + docNumber + "\n" + "Accounting DocumentNumber : " + accountingNumber
            )

        }
        startActivity(intent)
        requireActivity().finish()
    }
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.DAYS_LIMIT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            if(!it.value!!.isNullOrEmpty())
                                count=it.value!!.toInt()
                            else
                                count=0
                        }
                        it.applicable?.contains("N")!! -> {
                            count=0
                        }
                    }
                }
            }
        }
    }


}
