package com.olam.warehouse.vegax.mtntghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntghanacocoa.R
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.FragmentGhanaCocoaMtntSelectTypeLayoutBinding
import com.olam.warehouse.vegax.mtntghanacocoa.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.mtntghanacocoa.utils.WEIGHBRIDGE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaMtntSelectDispatchTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_cocoa_mtnt_select_type_layout
    private lateinit var binding: FragmentGhanaCocoaMtntSelectTypeLayoutBinding
    private var callBack: VegaGhanaCocoaReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaCocoaReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaGhanaCocoaMtntSelectDispatchTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaCocoaMtntSelectTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcoffee/ui/VegaCoffeeProcessingSelectTypeFragment").title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        if (getCurrentKey().split("_")[2].contains("COCO"))
            if (getCurrentKey().split("_")[1].contains("GH")) {
                binding.tvWeighScale.text = getString(R.string.mtnt_text)
            }
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
