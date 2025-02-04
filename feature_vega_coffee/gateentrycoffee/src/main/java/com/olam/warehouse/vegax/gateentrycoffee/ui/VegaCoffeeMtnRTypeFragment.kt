package com.olam.warehouse.vegax.gateentrycoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrycoffee.R
import com.olam.warehouse.vegax.gateentrycoffee.databinding.FragmentSelectTypeLayoutBinding
import com.olam.warehouse.vegax.gateentrycoffee.utils.MTNR
import com.olam.warehouse.vegax.gateentrycoffee.utils.WEIGHBRIDGE_WEIGHSCALE
import com.olam.warehouse.vegax.gateentrycoffee.utils.WEIGHSCALE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeMtnRTypeFragment : BaseFragment() {

    private val mTAG = VegaCoffeeMtnRTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentSelectTypeLayoutBinding
    private var callBack: VegaCoffeeReplaceCallback? = null
    private var gateEntryData = VegaGateEntry()
    private var gateEntryType: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceCallback
    }

    override val layoutResourceId = R.layout.fragment_select_type_layout

    companion object {
        fun newInstance(type: String) = VegaCoffeeMtnRTypeFragment().putArgs {
            putString("GEType", type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        gateEntryType = arguments?.getString("GEType", "") ?: ""
        binding = FragmentSelectTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentrycoffee/ui/VegaCoffeeMtnRTypeFragment")
            .title("Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.llOwnershipTransfer.setOnClickListener { moveToWeighscale() }
        binding.llThirdPartyOlam.setOnClickListener { moveToWbWS() }
    }

    private fun moveToWbWS() {
        gateEntryData = VegaGateEntry()
        gateEntryData.imageString = WEIGHBRIDGE_WEIGHSCALE
        gateEntryData.weighBridgeType = gateEntryType
        callBack?.replaceFragment(MTNR, gateEntryData)
    }

    private fun moveToWeighscale() {
        gateEntryData = VegaGateEntry()
        gateEntryData.imageString = WEIGHSCALE
        gateEntryData.weighBridgeType = gateEntryType
        callBack?.replaceFragment(MTNR, gateEntryData)
    }
}
