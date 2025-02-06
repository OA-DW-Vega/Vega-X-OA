package com.olam.warehouse.vegax.advanceniicaragua.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceTransactionDetails
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.advancenicaragua.R
import com.olam.warehouse.vegax.advancenicaragua.databinding.FragmentVegaNicaraguaAdvanceDetailsBinding
import com.olam.warehouse.vegax.advanceniicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class VegaNicaraguaAdvanceDetailsFragment : BaseFragment() {

    private var callBack: Callback? = null
    private var receiveData = VegaNicaraguaAdvanceTransactionDetails()
    private lateinit var binding: FragmentVegaNicaraguaAdvanceDetailsBinding

    private val vm: VegaNicaraguaAdvanceViewModel by viewModel()

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaAdvanceDetailsFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_advance_vendors

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaAdvanceDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("advanceCreation/ui/VegaNicaraguaAdvanceDetailsFragment")
            .title("Advance Creation  vendors list")
            .with(tracker)


        initExtra()
        initUI()
    }

    private fun initUI() {

        updateMandatory()

        fetchingExchangeRate()

        binding.vendorName.text=receiveData.vendorName+" - "+receiveData.vendorCode

        binding.btnProceed.setOnClickListener(View.OnClickListener {
            saveData()

        })

        binding.requestAdvanceAmount.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                validateFields()
            }

        })

        binding.tenureInDays.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                var tenureDays:Int= covertToInt(binding.tenureInDays.text.toString())
                if(tenureDays>0)
                {
                   var maturityDate= DateUtils.addingDaysToCurrentDate(tenureDays)
                    binding.maturityDateLayout.visibility=View.VISIBLE
                    binding.maturityDate.text=maturityDate
                }else
                { binding.maturityDateLayout.visibility=View.GONE}

                validateFields()
            }

        })

        binding.promissoryNumber.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                validateFields()
            }

        })

    }

    private fun initExtra() {
        arguments?.let {
            receiveData = it.getParcelable(UIUtils.RECEIVING_DATA)!!
        }
    }

    private fun fetchingExchangeRate() {
        vm.exchangeRate.observe(viewLifecycleOwner, androidx.lifecycle.Observer {   response ->
            response?.let {

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            receiveData.exchangeRate = it?.exchangeRate
                            fetchingAdvanceDetails()
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })

        vm.exchangeRateOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            receiveData.exchangeRate = it?.exchangeRate
            fetchingAdvanceDetails()
        })

        if (AppUtils.isOnline()) vm.getExchangeRate() else vm.getExchangeRateOffline()
    }

    private fun fetchingAdvanceDetails() {
        vm.getAdvanceDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer {   response ->
            response?.let {

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            if(it?.size!!>0) {

                                val advanceDetails: VegaNicaraguaAdvanceDetails =
                                    it[0].advanceCreationDetailsDTO!!.get(0)
                                receiveData.availableLimitSign = advanceDetails.availableLimitSign
                                receiveData.creditLimit = advanceDetails.creditLimit

                                setDetails()
                            }else
                            {
                                showDialog(getString(R.string.advance_info_not_found)+" "+receiveData.vendorName)
                            }
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })

        vm.getAdvanceDetailsOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if(it!=null) {
                receiveData.availableLimitSign = it.availableLimitSign
                receiveData.creditLimit = it.creditLimit

                setDetails()
            }
            else
            {
                showDialog(getString(R.string.advance_info_not_found)+" "+receiveData.vendorName)
            }
        })

        if (AppUtils.isOnline()) vm.getAdvanceDetails(receiveData.vendorCode!!) else vm.getAdvanceDetailsOffline("000"+receiveData.vendorCode!!)
    }


    private fun setDetails() {
        if (!receiveData.exchangeRate.isNullOrEmpty()) {
            binding.tvExchangeRate.text = receiveData.exchangeRate!!.trim()
        }
        if (!receiveData.availableLimitSign.isNullOrEmpty()) {
            binding.tvAvailableLimit.text = postfixToPrefix(receiveData.availableLimitSign!!.trim())
        }
        if (!receiveData.creditLimit.isNullOrEmpty()) {
            binding.tvCreditLimit.text = receiveData.creditLimit!!.trim()
        }


        if (!receiveData.tempId.isNullOrEmpty()) {
            binding.requestAdvanceAmount.setText(receiveData.requestedAdvanceAmount)
            binding.tenureInDays.setText(receiveData.tenureInDays)
            binding.maturityDateLayout.visibility = View.VISIBLE
            binding.maturityDate.text = receiveData.maturityDate
            binding.promissoryNumber.setText(receiveData.promissoryNumber)
        }

    }
    private fun validateFields() {
        var flag = true

        var requestedAmount: Double = covertToDouble(binding.requestAdvanceAmount.text.toString())
        var tenureDays: Int = covertToInt(binding.tenureInDays.text.toString())
        var promissoryNumber: String = binding.promissoryNumber.text.toString()
        // As per the business request removed this validation
        /*if (requestedAmount <= 0.0 || requestedAmount > covertToDouble(removeNonNumeric(receiveData.availableLimitSign?:""))) {
            flag = false
            if (requestedAmount > covertToDouble(removeNonNumeric(receiveData.availableLimitSign?:""))) {
                showSnack(getString(R.string.request_advance_amount_err_msg))
            }
        }*/
        if (tenureDays <= 0) {
            flag = false
        }
        if (promissoryNumber.isNullOrEmpty()) {
            flag = false
        }
        /*if (receiveData.exchangeRate.isNullOrEmpty()) {
            flag = false
        }
        if (receiveData.availableLimitSign.isNullOrEmpty()) {
            flag = false
        }
        if (receiveData.creditLimit.isNullOrEmpty()) {
            flag = false
        }*/
        if (flag) {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            binding.btnProceed.isEnabled = true
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
            binding.btnProceed.isEnabled = false
        }

    }
    private fun updateMandatory() {
        binding.requestAdvanceAmountHeader.text =
            with(UIUtils) { with(resources.getString(R.string.request_advance_amount_cs)) { mandatoryStars() } }
       binding.tenureInDaysHeader.text =
            with(UIUtils) { with(resources.getString(R.string.tenure_in_days)) { mandatoryStars() } }
        binding.promissoryNumberHeader.text =
            with(UIUtils) { with(resources.getString(R.string.promissory_number)) { mandatoryStars() } }
    }
    private fun saveData()
    {
        if( receiveData.tempId.isNullOrEmpty()) {
            receiveData.tempId = getTmpId()
            receiveData.erdat = getCurrentTimeInMills().toString()

        }
        receiveData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receiveData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receiveData.requestedAdvanceAmount=binding.requestAdvanceAmount.text.toString()
        receiveData.tenureInDays=binding.tenureInDays.text.toString()
        receiveData.maturityDate=binding.maturityDate.text.toString()
        receiveData.promissoryNumber=binding.promissoryNumber.text.toString()
        vm.saveTransactionAdvanceData(receiveData)
        var data=Bundle()
        data.putParcelable(UIUtils.RECEIVING_DATA,receiveData)
        callBack?.replaceFragment(
            ADVANCE_SUMMARY_FRAG,data)
    }


}
