package com.olam.warehouse.vegax.forwardponicaragua.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.utils.prepareGrnCharDetails
import com.olam.warehouse.master.common.utils.prepareGrnPriceDetailsList
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnCharDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.forwardponicaragua.R
import com.olam.warehouse.vegax.forwardponicaragua.databinding.FragmentVegaNicaraguaForwardPoYielsDetailsBinding
import com.olam.warehouse.vegax.forwardponicaragua.databinding.ItemVegaNicaraguaForwardPoYieldBinding
import com.olam.warehouse.vegax.forwardponicaragua.utils.FORWARD_PO_CREATION_PRICING_DETAILS_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaNicaraguaForwardPOYieldDetailsFragment : BaseFragment() {

    private var callBack: Callback? = null
    private lateinit var binding: FragmentVegaNicaraguaForwardPoYielsDetailsBinding
    private val vm: VegaNicaraguaForwardPOViewModel by viewModel()
    private var receivingData = VegaNicaraguaForwardPODetails()
    private var grnCharList = arrayListOf<VegaNicaraguaGrnCharDetails>()
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var filterpriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var userfilterpriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var YieldDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaForwardPOYieldDetailsFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_forward_po_yiels_details

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaForwardPoYielsDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("FORWORDPO/ui/VegaNicaraguaForwordPOTransactionDetailsFragment")
            .title("FORWORD PO TRANSACTION DETAILS ")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        arguments?.let {
            receivingData = it.getParcelable(UIUtils.RECEIVING_DATA)!!
        }
    }

    private fun initUI() {
        binding.btnProceed.setOnClickListener(View.OnClickListener {
            var export: String = ""
            var desemD: String = ""
            var desemR: String = ""
            userfilterpriceDetails.clear()
            if (priceDetails.size > 0) {
                for (i in 0 until filterpriceDetails.size) {
                    val post = VegaNicaraguaGrnPriceDetails()
                    val view: View = binding.rvYield.getChildAt(i)
                    val editText: EditText = view.findViewById(R.id.tvUnitApprove)
                    val textview: TextView = view.findViewById(R.id.tvQualityNameApprove)
                    var values: String = editText.text.toString()
                    post.description = textview.text.toString()
                    post.price = editText.text.toString()
                    userfilterpriceDetails.add(post)
                    if (textview.text.equals("Cascarilla"))
                        receivingData.cascara = editText.text.toString()
                    if (textview.text.equals("Rendimiento Oro Bruto"))
                        receivingData.rendimientoBruto = editText.text.toString()
                    if(textview.text.equals("Desmanche Robusta")){
                        desemR = editText.text.toString()
                    }
                    if (textview.text.equals("Exportable in %"))
                        export = editText.text.toString()
                    if (textview.text.equals("Desmanche D"))
                        desemD = editText.text.toString()
                    Log.d("vales_check", values)

                }
                YieldDetails.forEach {
                    if (it.fieldName.equals("EXPGRD03") || it.fieldName.equals("EXPGRD08") || it.fieldName.equals("EXPGRD07"))
                        it.percentage = export
                    if (it.fieldName.equals("LOWGRD03"))
                        it.percentage = desemD
                    if(it.fieldName.equals("LOWGRD04"))
                        it.percentage = desemR
                }

                var data = Bundle()
                data.putParcelable(UIUtils.RECEIVING_DATA, receivingData)
                data.putParcelableArrayList(UIUtils.YIELD_DATA, YieldDetails)
                data.putParcelableArrayList(UIUtils.FILTEREDPRICEDETAILS, userfilterpriceDetails)

                callBack?.replaceFragment(
                    FORWARD_PO_CREATION_PRICING_DETAILS_FRAG, data
                )
            } else {
                showSnack(getString(R.string.yield_info_not_found))
            }
        })

        fetchingExchangeRate()
    }

    private fun getGrnCharDetails() {
        vm.grnCharDetails.observe(viewLifecycleOwner, Observer {
            updateGrnCharList(it)
        })
        vm.grnCharDetailsOffline.observe(viewLifecycleOwner, Observer {
            grnCharList.clear()
            grnCharList = it as ArrayList<VegaNicaraguaGrnCharDetails>
            fetchingGrnPriceDetails()
        })
        if (AppUtils.isOnline()) vm.getGrnCharDetails(
            receivingData.grade.toString(),
            receivingData.materialCode.toString()
        )
        else vm.getGrnCharDetailsOffline(receivingData.grade.toString(), receivingData.materialCode.toString())

    }

    private fun fetchingExchangeRate() {
        vm.exchangeRate.observe(viewLifecycleOwner, Observer { response ->
            response?.let {

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            receivingData.exchangeRate = it?.exchangeRate
                            receivingData.currency = it?.currencyCode
                            getGrnCharDetails()
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
            receivingData.exchangeRate = it?.exchangeRate
            receivingData.currency = it?.currencyCode
            getGrnCharDetails()
        })

        if (AppUtils.isOnline()) vm.getExchangeRate() else vm.getExchangeRateOffline()
    }

    private fun updateGrnCharList(response: Resource<GenericReqAndResp<List<GrnCharDetails>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            grnCharList.clear()
                            val dataValue = it.data?.data!!
                            grnCharList = prepareGrnCharDetails(dataValue)
                            fetchingGrnPriceDetails()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
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
                            setUpAdapter()
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
            setUpAdapter()
        })
        //    gradeMappingDescription.toString()
        if (AppUtils.isOnline()) vm.getGrnPriceDetails()
        else vm.getGrnPriceDetailsOffline()
    }

    private fun getFilteredPriceDetails( priceDetails: ArrayList<VegaNicaraguaGrnPriceDetails>): ArrayList<VegaNicaraguaGrnPriceDetails> {

        //YieldDetails=priceDetails
        var DESMA: String? = ""
        var DESMC: String? = ""
        var DESMD: String? = ""
        var export: String? = ""
        grnCharList.forEach {
            //val post = VegaNicaraguaGrnPriceDetails()
            if (it.qualityParamName.contains("NIFG0014"))
                receivingData.certificate = it.charValue
            else if (it.qualityParamName.contains("NIRM0010")) {
                receivingData.rendimientoBruto = it.numValue
            } else if (it.qualityParamName.contains("NICASCAB"))
            //receivingData.cascara = it.numValue
            else if (it.qualityParamName.contains("NIRM0003"))
                receivingData.humedad =it.numValue
            else if (it.qualityParamName.contains("NIDESA"))
                DESMA = it.numValue?.replace("%", "")?.trim()
            else if (it.qualityParamName.contains("NIDESC"))
                DESMC = it.numValue?.replace("%", "")?.trim()
            else if (it.qualityParamName.contains("NIDESD") || it.qualityParamName.contains("NIDESR"))
                DESMD = it.numValue?.replace("%", "")?.trim()
            else if (it.qualityParamName.contains("NIEXPOP"))
                export = it.numValue?.replace("%", "")?.trim()
           // post.description=it.qualityParamDesc
           /* if (it.numValue.isNullOrEmpty())
                post.price = it.charValue
            else
                post.price = it.numValue!!.trim()*/
            //YieldDetails.add(post)
        }

        priceDetails.forEach {
            if (it.fieldName.equals("LOWGRD01", true)) {
                it.percentage = DESMA

            } else if ("LOWGRD02".equals(it.fieldName, true)) {
                it.percentage = DESMC

            } else if ("LOWGRD03".equals(it.fieldName, true)) {
                it.percentage = DESMD
            }else if ("LOWGRD04".equals(it.fieldName, true)) {
                it.percentage = DESMD
            }
            else if ("EXPGRD03".equals(it.fieldName, true)) {
                it.percentage = export

            }
            else if ("EXPGRD07".equals(it.fieldName, true)) {
                it.percentage = export
            }
            else if ("EXPGRD08".equals(it.fieldName, true)) {
                it.percentage = export
            }

        }
        return priceDetails
        /*return ArrayList( priceDetails.filter {
            !it.description.isNullOrEmpty()
        })*/
    }
    private fun setUpAdapter() {
        filterpriceDetails.clear()
        YieldDetails.clear()
        grnCharList.forEach {
            val post = VegaNicaraguaGrnPriceDetails()
          if (it.qualityParamName.contains("NICASCAB")||(it.qualityParamName.contains("NIDESD") /*&& receivingData.materialProductGrp.equals(Constants.ARAB)*/)
              ||it.qualityParamName.contains("NIRM0010")||it.qualityParamName.contains("NIEXPOP")
              ||(it.qualityParamName.contains("NIDESR") && receivingData.materialProductGrp.equals(Constants.ROBU)))
           {
               post.description=it.qualityParamDesc
               // post.price = it.numValue!!.trim()
                filterpriceDetails.add(post)
          }
        }


        priceDetails.forEach {
            if ((receivingData.materialName.equals("Pergamino Certificado")||receivingData.materialName.equals("Pergamino Certificado UA") ) && it.fieldName.equals("EXPGRD08")) {
                var post = priceDetails.filter {
                    it.fieldName.equals("EXPGRD08")
                }

                YieldDetails.addAll(post)

            } else if ((receivingData.materialName.equals("Pergamino")|| receivingData.materialName.equals("Pergamino UA")) && it.fieldName.equals("EXPGRD03")) {
                var post = priceDetails.filter {
                    it.fieldName.equals("EXPGRD03")
                }

                YieldDetails.addAll(post)

            }
            if (it.fieldName.equals("LOWGRD03")) {
                var post = priceDetails.filter {
                    it.fieldName.equals("LOWGRD03")
                }
                YieldDetails.addAll(post)

            }
            if(receivingData.materialProductGrp.equals(Constants.ROBU)){
                YieldDetails.clear()
                var robu = priceDetails.filter {
                    it.fieldName.equals("LOWGRD04")
                }
                YieldDetails.addAll(robu)
                var exportable = priceDetails.filter {
                    it.fieldName.equals("EXPGRD07")
                }
                YieldDetails.addAll(exportable)

            }

        }
        YieldDetails.forEach { it1->
            filterpriceDetails.find { it.fieldName.equals(it1.fieldName) }?.apply {
                it1.description = this.description
            }
        }


        binding.rvYield.setUpAdapter(
            filterpriceDetails,
            R.layout.item_vega_nicaragua_forward_po_yield,
            ItemVegaNicaraguaForwardPoYieldBinding::inflate,
            { it, pos, bindItem ->

                if (!it.description.isNullOrEmpty()) {
                    bindItem.tvQualityNameApprove.text = it.description
                }

                if (!it.price.isNullOrEmpty()) {
                    bindItem.tvUnitApprove.setText(it.price!!.toString().trim())
                }
                /* if(it.description.equals("Cascarilla")||it.description.equals("Rendimiento Oro Bruto")||it.description.equals("Exportable in %")||it.description.equals("Desmanche D")){
                tvUnitApprove.text = it.percentage!!.trim()
            }*/

            })
    }
}
