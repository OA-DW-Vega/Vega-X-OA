package com.olam.warehouse.vegax.mtntghana.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.databinding.FragmentGhanaMtntSelectTypeLayoutBinding
import com.olam.warehouse.vegax.mtntghana.utils.MTNT_WEIGHSCALE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaMtntSelectDispatchTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_mtnt_select_type_layout
    private lateinit var binding: FragmentGhanaMtntSelectTypeLayoutBinding
    private var callBack: VegaGhanaReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaGhanaMtntSelectDispatchTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaMtntSelectTypeLayoutBinding.inflate(layoutInflater)
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
        binding.llRmin.setOnClickListener { moveToWeighBridge() }
        binding.llFgrn.setOnClickListener { moveToWeighScale() }
    }

    private fun moveToWeighBridge() {
       /* callBack?.replaceFragment(
            WEIGHBRIDGE, ""
        )*/
    }

    private fun moveToWeighScale() {
        callBack?.replaceFragment(
            MTNT_WEIGHSCALE, ""
        )
    }
}
