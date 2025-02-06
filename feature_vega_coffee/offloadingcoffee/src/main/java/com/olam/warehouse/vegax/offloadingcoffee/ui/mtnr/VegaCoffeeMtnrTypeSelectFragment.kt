package com.olam.warehouse.vegax.offloadingcoffee.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcoffee.R
import com.olam.warehouse.vegax.offloadingcoffee.databinding.FragmentSelectMtnrTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcoffee.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeMtnrTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_select_mtnr_type_layout
    private lateinit var binding: FragmentSelectMtnrTypeLayoutBinding
    private var callBack: VegaCoffeeOffloadReplaceFragmentCallback? = null
    private var offloadData = VegaCoffeeReceiving()
    private var offloadType: String = ""
    private var currentKey = getCurrentKey()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance(type: String) = VegaCoffeeMtnrTypeSelectFragment().putArgs {
            putString("GEType", type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        offloadType = arguments?.getString("GEType", "") ?: ""
        binding = FragmentSelectMtnrTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcoffee/ui/mtnr/VegaCoffeeMtnrTypeSelectFragment").title("IVC/Coffee/Offloading/MTNR Type Select")
            .with(tracker)
    }

    private fun initUI() {
        if (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF")) {
           /* val tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
            if(tallySequence.isNotEmpty())binding.llRmin.setOnClickListener { moveToNicWS() } else activity?.toast(getString(
                            R.string.tally_empty_error))*/
            binding.llRmin.setOnClickListener { moveToNicWS() }
        } else {
            binding.tvType.text = if (offloadType.equals(PROCURE)) SUPPLIER else MTNR
            binding.llRmin.setOnClickListener { moveToWS() }
        }
        binding.llFgrn.setOnClickListener { moveToWBWS() }
    }

    private fun moveToWBWS() {
        offloadData.imageString = WEIGHBRIDGE_WEIHSCALE
        offloadData.weighBridgeType = offloadType
        callBack?.replaceFragment(
            WEIGHSCALE_LIST, offloadData
        )
    }

    private fun moveToNicWS() {
        offloadData.imageString = WEIGHSCALE
        offloadData.weighBridgeType = offloadType
        callBack?.replaceFragment(
            WEIGHSCALE, offloadData
        )
    }

    private fun moveToWS() {
        offloadData.imageString = WEIGHSCALE
        offloadData.weighBridgeType = offloadType
        callBack?.replaceFragment(
            WEIGHSCALE_LIST, offloadData
        )
    }
}
