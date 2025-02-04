package com.olam.warehouse.vegax.mtntsesame.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntsesame.R
import com.olam.warehouse.vegax.mtntsesame.databinding.FragmentNigeriaSesameMtntSelectTypeLayoutBinding
import com.olam.warehouse.vegax.mtntsesame.utils.MTNT_WEIGHSCALE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaSesameMtntSelectDispatchTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_nigeria_sesame_mtnt_select_type_layout
    private lateinit var binding: FragmentNigeriaSesameMtntSelectTypeLayoutBinding
    private var callBack: VegaNigeriaSesameReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaSesameReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaNigeriaSesameMtntSelectDispatchTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNigeriaSesameMtntSelectTypeLayoutBinding.inflate(layoutInflater)
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
        context?.let {
            UIUtils.getActionBtnChangedView(binding.tvTitle, it, false)
        }
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
