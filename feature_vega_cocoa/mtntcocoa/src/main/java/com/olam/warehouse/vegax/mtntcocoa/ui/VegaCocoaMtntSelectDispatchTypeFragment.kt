package com.olam.warehouse.vegax.mtntcocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentSelectMtntTypeLayoutBinding
import com.olam.warehouse.vegax.mtntcocoa.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.mtntcocoa.utils.WEIGHBRIDGE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaMtntSelectDispatchTypeFragment : BaseFragment() {
    override val layoutResourceId: Int =
        com.olam.warehouse.vegax.mtntcocoa.R.layout.fragment_select_mtnt_type_layout
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentSelectMtntTypeLayoutBinding
    private val vm: VegaCocoaMtntViewModel by viewModel()

//    interface CallBack {
//        fun replaceFragment(fragment: String)
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaCocoaMtntSelectDispatchTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectMtntTypeLayoutBinding.inflate(layoutInflater)
        vm.getConfigItems(UserRoles.MTNT.role)
        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })

        return binding.root
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {

                ConfigItems.MTNT_WEIGHSCALE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            binding.clMtntWeightScale.visibility = View.VISIBLE
                        }
                        it.applicable?.contains("N")!! -> {
                            binding.clMtntWeightScale.visibility = View.GONE
                        }
                    }
                }
                /*ConfigItems.MTNT_NO_WEIGHMENT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> binding.clWeightScale.visibility = View.VISIBLE
                        it.applicable?.contains("N")!! -> binding.clWeightScale.visibility = View.GONE
                    }
                }*/
            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/mtntcocoa/ui/VegaCocoaMtntSelectDispatchTypeFragment").title("Dispatch Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        binding.clWeighBridge.setOnClickListener { moveToWeightBridge() }
        binding.clWeightScale.setOnClickListener { moveToWeighScale() }
        binding.clMtntWeightScale.setOnClickListener { moveToMtntWeiscale() }
    }

    private fun moveToWeightBridge() {
        callBack?.replaceFragment(
            WEIGHBRIDGE,""
        )
    }

    private fun moveToWeighScale() {
       /* callBack?.replaceFragment(
            NO_WEIGHMENT,""
        )*/
        callBack?.replaceFragment(
            WEIGHBRIDGE,""
        )
    }
    private fun moveToMtntWeiscale()
    {
        callBack?.replaceFragment(
            MTNT_WEIGHSCALE,""
        )
    }

}
