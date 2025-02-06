package com.olam.warehouse.vegax.offloadingcameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentSelectCameroonOffloadTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingcameroon.utils.MTNR
import com.olam.warehouse.vegax.offloadingcameroon.utils.PROCURE
import com.olam.warehouse.vegax.offloadingcameroon.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonOffloadTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_select_cameroon_offload_type_layout
    private lateinit var binding: FragmentSelectCameroonOffloadTypeLayoutBinding
    private var callBack: VegaCameroonOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCameroonOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaCameroonOffloadTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectCameroonOffloadTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcameroon/ui/VegaCameroonOffloadTypeSelectFragment")
            .title("Vega_Cameroon/Offloading")
            .with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        binding.llRmin.setOnClickListener { moveToSupplier() }
        binding.llFgrn.setOnClickListener { moveToMtnr() }
    }

    private fun moveToSupplier() {
        var offloadingTruck = VegaOffloadingTrucks()
        offloadingTruck.weighBridgeType = PROCURE
        callBack?.replaceFragment(
            SUPPLIER, offloadingTruck
        )
    }

    private fun moveToMtnr() {
        callBack?.replaceFragment(
            MTNR, ""
        )
    }
}
