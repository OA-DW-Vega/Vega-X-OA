package com.olam.warehouse.vegax.gateentryghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryghanacocoa.R
import com.olam.warehouse.vegax.gateentryghanacocoa.databinding.FragmentVegaGhanaCocoaGateEntryTypeBinding
import com.olam.warehouse.vegax.gateentryghanacocoa.utils.MTNR
import com.olam.warehouse.vegax.gateentryghanacocoa.utils.PROCURE
import com.olam.warehouse.vegax.gateentryghanacocoa.utils.STO
import com.olam.warehouse.vegax.gateentryghanacocoa.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGateEntryGhanaCocoaTypeFragment : BaseFragment() {
    private var gateEntryData = VegaGateEntry()
    private lateinit var binding: FragmentVegaGhanaCocoaGateEntryTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_ghana_cocoa_gate_entry_type

    companion object {
        fun newInstance() = VegaGateEntryGhanaCocoaTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaCocoaGateEntryTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentry/ui/VegaGateEntryGhanaTypeFragment").title("Gate Entry").with(tracker)
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
