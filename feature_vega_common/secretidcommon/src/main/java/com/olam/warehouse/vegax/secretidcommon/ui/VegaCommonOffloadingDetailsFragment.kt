package com.olam.warehouse.vegax.secretidcommon.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.databinding.FragmentCommonSecretidOffloadingDetailsBinding

class VegaCommonOffloadingDetailsFragment:BaseFragment() {

    private lateinit var wbDetails:VegaQualityWBDetails
    override val layoutResourceId= R.layout.fragment_common_secretid_offloading_details
    private lateinit var binding:FragmentCommonSecretidOffloadingDetailsBinding


    companion object {
        fun newInstance(wbDetails: VegaQualityWBDetails) = VegaCommonOffloadingDetailsFragment().putArgs {
            putParcelable(UIUtils.LOT_DETAIL, wbDetails)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= FragmentCommonSecretidOffloadingDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUI()
    }

    private fun initUI() {
        binding.tvTruckNo.text= wbDetails.vehicleNumber
        binding.tvWeighBridgeId.text= wbDetails.weighBridgeId
        binding.tvSupplierName.text= wbDetails.supplierName
        binding.tvWeight.text= wbDetails.netWeight
            if ("null" != wbDetails.erdat && wbDetails.erdat != "") {
                val times = wbDetails.erdat?.split('(', ')')
                binding.tvDate.text =
                    times?.get(1)?.let { it1 -> DateUtils.getUTCDateTime(it1, App.getAppContext()) }
            }

        binding.tvDriverName.text= wbDetails.truckDriverName
        binding.tvDriverPhone.text= wbDetails.contactNumber

    }


    private fun initExtra(){
        wbDetails= arguments?.getParcelable(UIUtils.LOT_DETAIL)!!

    }


}
