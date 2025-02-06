package com.olam.warehouse.vegax.mtntcoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcoffee.R
import com.olam.warehouse.vegax.mtntcoffee.databinding.FragmentCoffeeMtntSelectTypeLayoutBinding
import com.olam.warehouse.vegax.mtntcoffee.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.mtntcoffee.utils.WEIGHBRIDGE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeMtntSelectDispatchTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_coffee_mtnt_select_type_layout
    private lateinit var binding: FragmentCoffeeMtntSelectTypeLayoutBinding
    private var callBack: VegaCoffeeReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaCoffeeMtntSelectDispatchTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCoffeeMtntSelectTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcoffee/ui/VegaCoffeeSelectTypeFragment").title("IVC/Coffee/MTNT/Select Dispatch")
            .with(tracker)
    }

    private fun initUI() {
        binding.llRmin.setOnClickListener { moveToWeighBridge() }
        binding.llFgrn.setOnClickListener { moveToWeighScale() }
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
