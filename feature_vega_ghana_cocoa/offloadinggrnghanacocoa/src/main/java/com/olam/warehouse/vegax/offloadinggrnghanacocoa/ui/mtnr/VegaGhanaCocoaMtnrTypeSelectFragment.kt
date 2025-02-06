package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentGhanaCocoaSelectMtnrTypeLayoutBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.WEIGHBRIDGE_WEIHSCALE
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.WEIGHSCALE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaMtnrTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_cocoa_select_mtnr_type_layout
    private lateinit var binding: FragmentGhanaCocoaSelectMtnrTypeLayoutBinding
    private var callBack: VegaGhanaCocoaOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaCocoaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaGhanaCocoaMtnrTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaCocoaSelectMtnrTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingghana/ui/mtnr/VegaGhanaMtnrTypeSelectFragment").title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
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
