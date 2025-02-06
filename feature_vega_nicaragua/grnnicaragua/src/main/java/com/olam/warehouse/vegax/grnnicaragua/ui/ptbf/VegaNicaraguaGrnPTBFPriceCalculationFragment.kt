package com.olam.warehouse.vegax.grnnicaragua.ui.ptbf

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.prepareGrnPriceDetailsList
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPriceConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.DateUtils.getDate
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnPtbfPriceCalculationBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.FRAG_PTBF_GRN_SUMMARY
import com.olam.warehouse.vegax.grnnicaragua.utils.changeState
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaNicaraguaGrnPTBFPriceCalculationFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var priceConfigDetails: VegaNicaraguaPriceConfigDetails? = null
    private lateinit var binding: FragmentVegaNicaraguaGrnPtbfPriceCalculationBinding

    private val vm: VegaNicaraguaGrnViewModel by viewModel()

    private var netWeight: String? = ""
    private var totalPrice: Double? = 0.0
    private var gradeMappingDescription: String? = ""

    private var yieldPercentage: String? = "1"//it is in percentage
    private var exchangeRate: String? = ""//it is in percentage
    private var currency: String? = ""
    private var USDAmount: String? = "1"

    private var receivingData = VegaReceiving()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()

    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            data: Bundle
        )
    }

    companion object {
        fun newInstance(receivingData: VegaReceiving, qualityParameterList: ArrayList<VegaQualityParameter?>) =
            VegaNicaraguaGrnPTBFPriceCalculationFragment().putArgs {
                putParcelable(UIUtils.RECEIVING_DATA, receivingData)
                putParcelableArrayList(UIUtils.QUALITY_DATA, qualityParameterList)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_ptbf_price_calculation

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnPtbfPriceCalculationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnicaragua/ui/ptbf/VegaNicaraguaGrnPTBFPriceCalculationFragment")
            .title("GRN PTBF Price")
            .with(tracker)

        initExtra()
        initUI()
    }

    override fun onResume() {
        changeState(4, binding.stateBar.root, context)
        super.onResume()
    }

    private fun initUI() {
        if (receivingData.yieldPercentage!!.isNotEmpty()) yieldPercentage = receivingData.yieldPercentage
        vm.getBagItems(receivingData.tmpWbId)

        vm.bagItems.observe(viewLifecycleOwner, Observer {
            weighDetails = it
        })

        vm.getGradeMapping(receivingData.grade!!.substring(receivingData.grade!!.length - 4))
        vm.gradeMapping.observe(viewLifecycleOwner, Observer {
            gradeMappingDescription = it?.description ?: ""
            if (gradeMappingDescription!!.isNotEmpty()) fetchingGrnPriceDetails()
            else fetchingExchangeRate()
        })

        /*vm.updateLotSequence.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            saveLotSequence(it?.sequence.toString())
                        }
                    }
                    Resource.Status.LOADING -> {}
                    Resource.Status.ERROR -> {
                        showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    }
                }
            }
        })*/



        binding.btnProceed.setOnClickListener {
            if (priceConfigDetails != null) moveToSummary()
            else showErrorDialogWithFAQLink(requireContext(), getString(R.string.price_info_not_found))
        }
    }

    private fun fetchingExchangeRate() {
        vm.exchangeRate.observe(viewLifecycleOwner, Observer { response ->
            response?.let {

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            exchangeRate = it?.exchangeRate
                            USDAmount = it?.currencyValue
                            currency = it?.currencyCode
                            binding.tvExchangeRate.text =
                                if (receivingData.exchangeRate?.isEmpty() == true) exchangeRate else receivingData.exchangeRate
                            getPricingInfo()
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

        vm.exchangeRateOffline.observe(viewLifecycleOwner, Observer {
            exchangeRate = it?.exchangeRate
            USDAmount = it?.currencyValue
            currency = it?.currencyCode
            binding.tvExchangeRate.text =
                if (receivingData.exchangeRate?.isEmpty() == true) exchangeRate else receivingData.exchangeRate
            getPricingInfo()

        })

        if (isOnline()) vm.getExchangeRate() else vm.getExchangeRateOffline()
    }

    private fun fetchingGrnPriceDetails() {
        vm.grnPriceDetails.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        when (it.data?.success) {
                            true -> {
                                priceDetails.clear()
                                val dataValue = it.data?.data!!
                                priceDetails = prepareGrnPriceDetailsList(dataValue)
                                priceDetails = getFilteredPriceDetails(priceDetails)
                                fetchingExchangeRate()
                            }
                            else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
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

        vm.grnPriceDetailsOffline.observe(viewLifecycleOwner, Observer {
            priceDetails = it as ArrayList<VegaNicaraguaGrnPriceDetails>
            priceDetails = getFilteredPriceDetails(priceDetails)
            fetchingExchangeRate()
        })

        if (isOnline()) vm.getGrnPriceDetails()
        else vm.getGrnPriceDetailsOffline()
    }

    private fun getFilteredPriceDetails(priceDetails: ArrayList<VegaNicaraguaGrnPriceDetails>): ArrayList<VegaNicaraguaGrnPriceDetails> {
        var filteredPriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

        priceDetails.forEach {
            if (gradeMappingDescription?.toString().equals(it.description)) {
                it.percentage = receivingData.exportablePercentage
                filteredPriceDetails.add(it)
            } else if (it.fieldName.equals("LOWGRD01", true)) {
                it.percentage = receivingData.DESMA
                filteredPriceDetails.add(it)
            } else if ("LOWGRD02".equals(it.fieldName, true)) {
                it.percentage = receivingData.DESMC
                filteredPriceDetails.add(it)
            } else if ("LOWGRD03".equals(it.fieldName, true)) {
                it.percentage = receivingData.DESMD
                filteredPriceDetails.add(it)
            }else if ("LOWGRD04".equals(it.fieldName, true)) {
                it.percentage = receivingData.DESMR
                filteredPriceDetails.add(it)
            }
        }
        return filteredPriceDetails
    }

    private fun getPricingInfo() {
        vm.priceConfigInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            priceConfigDetails = it
            setPriceDetails()
        })

        vm.getPriceConfigInfo(
            receivingData.materialCode.toString(),
            receivingData.grade!!.substring(receivingData.grade!!.length - 4)
        )
    }

    private fun initExtra() {
        arguments?.let {
            receivingData = it.getParcelable(UIUtils.RECEIVING_DATA)!!
            qualityParameterList = it.getParcelableArrayList<VegaQualityParameter>(UIUtils.QUALITY_DATA)!!
            netWeight = receivingData.netWeight
        }

        if (receivingData.imageString?.isNotEmpty() == true) {
            binding.rlStateProgressBar.gone()
            binding.btnProceed.gone()
        } else {
            binding.rlStateProgressBar.visible()
            binding.btnProceed.visible()
        }
        receivingData.createdDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = getCurrentTimeInMills().toString()
        receivingData.docDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        binding.tvpostingdateval.text = receivingData.postDate

    }

    private fun setPriceDetails() {

        if (priceConfigDetails != null) {
            try {
                binding.tvNetWeight.text = netWeight!! + " " + "KG(S)"
                binding.tvBasePrice.text = formatString(receivingData.materialPrice!!)
                totalPrice = receivingData.materialPrice!! * netWeight!!.toDouble()

                binding.tvTotalPrice.text = formatString(totalPrice!!)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            Toast.makeText(context, getString(R.string.price_info_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveToSummary() {

        val info = VegaNicaraguaInvoicePriceInfo()

        info.exchangeRate = exchangeRate
        info.currency = currency

        HandlerUtils.runOnUiThread {
            var data = Bundle()
            data.putParcelable(UIUtils.RECEIVING_DATA, receivingData)
            data.putParcelable(UIUtils.INVOICE_PRICE_INFO, info)
            data.putParcelableArrayList(UIUtils.BAGS_DATA, ArrayList(weighDetails!!.toMutableList()))
            data.putParcelableArrayList(UIUtils.QUALITY_DATA, ArrayList(qualityParameterList))
            data.putParcelableArrayList(UIUtils.PRICE_DATA, priceDetails)

            callBack?.replaceFragment(FRAG_PTBF_GRN_SUMMARY, data)
        }
    }
    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return getString(R.string.c_doller) + " " + strFormat.format(this).replace(",", "")
    }

}

