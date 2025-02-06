package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryapprovalnigeria.R
import com.olam.warehouse.vegax.gateentryapprovalnigeria.databinding.FragmentNigeriaGateEntryApprovalSelectTypeLayoutBinding
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.WEIGHBRIDGE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGateEntryNigeriaApprovalSelectDispatchTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_nigeria_gate_entry_approval_select_type_layout
    private lateinit var binding:  FragmentNigeriaGateEntryApprovalSelectTypeLayoutBinding
    private var callBack: VegaNigeriaApprovalReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaApprovalReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaGateEntryNigeriaApprovalSelectDispatchTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNigeriaGateEntryApprovalSelectTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcoffee/ui/VegaGateEntryNigeriaApprovalSelectDispatchTypeFragment").title("Processing Coffee")
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
