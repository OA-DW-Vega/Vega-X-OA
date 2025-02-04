package com.olam.warehouse.vegax.processing.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaProcessingTypeBinding
import com.olam.warehouse.vegax.processing.utils.FGRN
import com.olam.warehouse.vegax.processing.utils.RMIN
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaProcessingTypeFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_processing_type
    private lateinit var binding: FragmentVegaProcessingTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(fragment: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaProcessingTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaProcessingTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/VegaProcessingTypeFragment").title("Processing").with(tracker)
    }


    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        binding.llRmin.setOnClickListener { moveToRmin() }
        binding.llFgrn.setOnClickListener { movetoFgrn() }
    }

    private fun moveToRmin() {
        callBack?.replaceFragment(RMIN)
    }

    private fun movetoFgrn() {
        callBack?.replaceFragment(FGRN)
    }
}
