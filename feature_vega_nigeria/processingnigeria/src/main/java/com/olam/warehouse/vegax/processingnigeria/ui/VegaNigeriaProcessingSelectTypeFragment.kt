package com.olam.warehouse.vegax.processingnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentVegaNigeriaProcessingTypeBinding
import com.olam.warehouse.vegax.processingnigeria.utils.FGRN
import com.olam.warehouse.vegax.processingnigeria.utils.RMIN
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaProcessingSelectTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_processing_type
    private lateinit var binding: FragmentVegaNigeriaProcessingTypeBinding
    private var model: VegaCoffeeRminProcessing? = null
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaNigeriaProcessingSelectTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaProcessingTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingnigeria/ui/VegaCoffeeSelectTypeFragment").title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        binding.llRmin.setOnClickListener { moveToRmin() }
        binding.llFgrn.setOnClickListener { moveToFgrn() }
    }

    private fun moveToRmin() {
        callBack?.replaceFragment(
            RMIN,
            VegaCocoaRminProcessing()
        )
    }

    private fun moveToFgrn() {
        callBack?.replaceFragment(
            FGRN,
            VegaCocoaRminProcessing()
        )
    }
}
