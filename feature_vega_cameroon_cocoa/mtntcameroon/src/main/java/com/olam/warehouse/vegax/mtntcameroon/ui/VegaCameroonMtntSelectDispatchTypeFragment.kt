package com.olam.warehouse.vegax.mtntcameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcameroon.R
import com.olam.warehouse.vegax.mtntcameroon.databinding.FragmentCameroonMtntSelectTypeLayoutBinding
import com.olam.warehouse.vegax.mtntcameroon.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.mtntcameroon.utils.WEIGHBRIDGE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonMtntSelectDispatchTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_cameroon_mtnt_select_type_layout
    private lateinit var binding: FragmentCameroonMtntSelectTypeLayoutBinding
    private var callBack: VegaCameroonReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCameroonReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaCameroonMtntSelectDispatchTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCameroonMtntSelectTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("mtntcameroon/ui/VegaCameroonMtntSelectDispatchTypeFragment")
            .title("Vega_Cameroon/Mtnt").with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        binding.llRmin.setOnClickListener { moveToWeighBridge() }
        binding.llFgrn.setOnClickListener { moveToWeighScale() }
    }

    private fun moveToWeighBridge() {
        callBack?.replaceFragment(
            WEIGHBRIDGE, ""
        )
    }

    private fun moveToWeighScale() {
        callBack?.replaceFragment(
            MTNT_WEIGHSCALE, ""
        )
    }
}
