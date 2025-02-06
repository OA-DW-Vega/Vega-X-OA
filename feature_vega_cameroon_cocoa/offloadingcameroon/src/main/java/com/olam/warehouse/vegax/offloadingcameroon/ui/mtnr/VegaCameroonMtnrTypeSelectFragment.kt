package com.olam.warehouse.vegax.offloadingcameroon.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentSelectCameroonMtnrTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcameroon.utils.WEIGHBRIDGE_WEIHSCALE
import com.olam.warehouse.vegax.offloadingcameroon.utils.WEIGHSCALE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonMtnrTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_select_cameroon_mtnr_type_layout
    private lateinit var binding: FragmentSelectCameroonMtnrTypeLayoutBinding
    private var callBack: VegaCameroonOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCameroonOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaCameroonMtnrTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectCameroonMtnrTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcameroon/ui/mtnr/VegaCameroonMtnrTypeSelectFragment")
            .title("Vega_Cameroon/Mtnr")
            .with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        binding.llRmin.setOnClickListener { moveToWS() }
        binding.llFgrn.setOnClickListener { moveToWBWS() }
    }

    private fun moveToWBWS() {
        callBack?.replaceFragment(
            WEIGHBRIDGE_WEIHSCALE, ""
        )
    }

    private fun moveToWS() {
        callBack?.replaceFragment(
            WEIGHSCALE, ""
        )
    }
}
