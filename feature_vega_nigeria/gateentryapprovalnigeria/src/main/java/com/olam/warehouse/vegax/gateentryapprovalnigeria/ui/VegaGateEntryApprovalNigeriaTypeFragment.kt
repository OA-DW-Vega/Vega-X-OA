package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryapprovalnigeria.R
import com.olam.warehouse.vegax.gateentryapprovalnigeria.databinding.FragmentVegaGateEntryApprovalNigeriaTypeBinding
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.MTNR
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.PROCURE
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.STO
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
class VegaGateEntryApprovalNigeriaTypeFragment : BaseFragment() {
    private var gateEntryData = VegaGateEntry()
    private lateinit var binding: FragmentVegaGateEntryApprovalNigeriaTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_gate_entry_approval_nigeria_type

    companion object {
        fun newInstance() = VegaGateEntryApprovalNigeriaTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGateEntryApprovalNigeriaTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentryApprovalNigeria/ui/VegaGateEntryApprovalNigeriaTypeFragment").title("Vega_ApprovalNigeria/Gate Entry").with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }
    }

    private fun moveToSupplier() {
        gateEntryData = VegaGateEntry()
        gateEntryData.weighBridgeType = PROCURE
        callBack?.replaceFragment(SUPPLIER, gateEntryData)
    }

    private fun moveToMtnr() {
        gateEntryData = VegaGateEntry()
        gateEntryData.weighBridgeType = STO
        callBack?.replaceFragment(MTNR, gateEntryData)
    }
}
