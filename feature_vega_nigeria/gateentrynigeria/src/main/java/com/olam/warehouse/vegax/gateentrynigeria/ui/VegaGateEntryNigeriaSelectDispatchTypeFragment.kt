package com.olam.warehouse.vegax.gateentrynigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrynigeria.R
import com.olam.warehouse.vegax.gateentrynigeria.databinding.FragmentNigeriaGateEntrySelectTypeLayoutBinding
import com.olam.warehouse.vegax.gateentrynigeria.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.gateentrynigeria.utils.WEIGHBRIDGE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGateEntryNigeriaSelectDispatchTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_nigeria_gate_entry_select_type_layout
    private lateinit var binding:  FragmentNigeriaGateEntrySelectTypeLayoutBinding
    private var callBack: VegaNigeriaReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaGateEntryNigeriaSelectDispatchTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNigeriaGateEntrySelectTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcoffee/ui/VegaGateEntryNigeriaSelectDispatchTypeFragment").title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        binding.llRmin.setOnClickListener {
            moveToWeighBridge()
        }
        binding.llFgrn.setOnClickListener {
            moveToWeighScale()
        }
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
