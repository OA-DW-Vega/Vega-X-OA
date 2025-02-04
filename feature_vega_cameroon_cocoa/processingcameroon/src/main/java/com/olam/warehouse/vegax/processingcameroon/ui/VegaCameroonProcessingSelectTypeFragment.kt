package com.olam.warehouse.vegax.processingcameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcameroon.R
import com.olam.warehouse.vegax.processingcameroon.databinding.FragmentVegaCameroonProcessingTypeBinding
import com.olam.warehouse.vegax.processingcameroon.utils.FGRN
import com.olam.warehouse.vegax.processingcameroon.utils.RMIN
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonProcessingSelectTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cameroon_processing_type
    private lateinit var binding: FragmentVegaCameroonProcessingTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaCameroonProcessingSelectTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonProcessingTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcameroon/ui/VegaCameroonProcessingSelectTypeFragment")
            .title("Vega_Cameroon/Processing")
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
