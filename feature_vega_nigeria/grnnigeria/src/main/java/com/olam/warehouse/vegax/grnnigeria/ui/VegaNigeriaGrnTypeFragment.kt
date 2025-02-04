package com.olam.warehouse.vegax.grnnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnigeria.R
import com.olam.warehouse.vegax.grnnigeria.databinding.FragmentVegaGrnNigeriaTypeBinding
import com.olam.warehouse.vegax.grnnigeria.utils.GRN
import com.olam.warehouse.vegax.grnnigeria.utils.GRNT
import com.olam.warehouse.vegax.grnnigeria.utils.PROCURE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
class VegaNigeriaGrnTypeFragment : BaseFragment() {
    private var gateEntryData = VegaGateEntry()
    private lateinit var binding: FragmentVegaGrnNigeriaTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_grn_nigeria_type

    companion object {
        fun newInstance() = VegaNigeriaGrnTypeFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaGrnNigeriaTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnigeria/ui/VegaNigeriaGrnTypeFragment").title("GRN")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.llGrnt.setOnClickListener { moveToGRNT() }
        binding.llGrn.setOnClickListener { moveToGRN() }
    }

    private fun moveToGRNT() {
        gateEntryData = VegaGateEntry()
        gateEntryData.weighBridgeType = PROCURE
        callBack?.replaceFragment(GRNT, gateEntryData)
    }

    private fun moveToGRN() {
        //displayFragment(VegaNigeriaGrnTypeFragment(), false)
        gateEntryData = VegaGateEntry()
        gateEntryData.weighBridgeType = PROCURE
        callBack?.replaceFragment(GRN, gateEntryData)
    }
}
