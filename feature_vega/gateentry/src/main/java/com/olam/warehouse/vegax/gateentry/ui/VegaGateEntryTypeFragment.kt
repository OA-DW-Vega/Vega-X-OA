package com.olam.warehouse.vegax.gateentry.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentry.R
import com.olam.warehouse.vegax.gateentry.databinding.FragmentVegaGateEntryTypeBinding
import com.olam.warehouse.vegax.gateentry.utils.MTNR
import com.olam.warehouse.vegax.gateentry.utils.PROCURE
import com.olam.warehouse.vegax.gateentry.utils.STO
import com.olam.warehouse.vegax.gateentry.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
class VegaGateEntryTypeFragment : BaseFragment() {
    private var gateEntryData = VegaGateEntry()
    private lateinit var binding: FragmentVegaGateEntryTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_gate_entry_type

    companion object {
        fun newInstance() = VegaGateEntryTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGateEntryTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentry/ui/VegaGateEntryTypeFragment").title("Gate Entry").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
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
