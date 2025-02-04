package com.olam.warehouse.vegax.processingsesame.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.databinding.FragmentVegaSesameProcessingTypeBinding
import com.olam.warehouse.vegax.processingsesame.utils.FGRN
import com.olam.warehouse.vegax.processingsesame.utils.RMIN
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaSesameProcessingSelectTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_sesame_processing_type
    private lateinit var binding: FragmentVegaSesameProcessingTypeBinding
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
        fun newInstance() = VegaSesameProcessingSelectTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaSesameProcessingTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcoffee/ui/VegaSesameSelectTypeFragment").title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
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
