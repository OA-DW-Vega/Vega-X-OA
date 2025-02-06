package com.olam.warehouse.vegax.offloadingsesame.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingsesame.R
import com.olam.warehouse.vegax.offloadingsesame.databinding.FragmentSesameSelectMtnrTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingsesame.ui.VegaSesameOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingsesame.utils.WEIGHBRIDGE_WEIHSCALE
import com.olam.warehouse.vegax.offloadingsesame.utils.WEIGHSCALE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaSesameMtnrTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_sesame_select_mtnr_type_layout
    private lateinit var binding: FragmentSesameSelectMtnrTypeLayoutBinding
    private var callBack: VegaSesameOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaSesameOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaSesameMtnrTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSesameSelectMtnrTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingsesame/ui/mtnr/VegaSesameMtnrTypeSelectFragment").title("Mtnr Coffee")
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
