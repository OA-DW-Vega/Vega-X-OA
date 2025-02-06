package com.olam.warehouse.vegax.salescocoa.ui

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
import com.olam.warehouse.vegax.salescocoa.databinding.FragmentCocoaLocalSalesTypesSelectBinding
import com.olam.warehouse.vegax.salescocoa.utils.BRIDE
import com.olam.warehouse.vegax.salescocoa.utils.SALES_TYPE_ANTICIPATED
import com.olam.warehouse.vegax.salescocoa.utils.SALES_TYPE_ANTICIPATED_VIRTUAL
import com.olam.warehouse.vegax.salescocoa.utils.SCALE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaSalesSelectSaleTypeFragment : BaseFragment() {
    override val layoutResourceId: Int =
        com.olam.warehouse.vegax.salescocoa.R.layout.fragment_cocoa_local_sales_types_select
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentCocoaLocalSalesTypesSelectBinding
    private val vm: VegaCocoaSalesViewModel by viewModel()

    interface CallBack {
        fun replaceFragment(fragment: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaCocoaSalesSelectSaleTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCocoaLocalSalesTypesSelectBinding.inflate(layoutInflater)
        vm.getConfigItems(UserRoles.SALES.role)
        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })

        return binding.root
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.SALES_ANTICIPATED.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> binding.clAnticipated.visibility = View.VISIBLE
                        it.applicable?.contains("N")!! -> binding.clAnticipated.visibility = View.GONE
                    }
                }

                ConfigItems.SALES_WEIGHBRIDGE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> binding.clWeighBridge.visibility = View.VISIBLE
                        it.applicable?.contains("N")!! -> binding.clWeighBridge.visibility = View.GONE
                    }
                }
                ConfigItems.SALES_WEIGHSCALE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> binding.clWeightScale.visibility = View.VISIBLE
                        it.applicable?.contains("N")!! -> binding.clWeightScale.visibility = View.GONE
                    }
                }
            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("salescocoa/ui/VegaCocoaSalesSelectSaleTypeFragment")
            .title("Sales Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        binding.clWeighBridge.setOnClickListener { moveToWeightBridge() }
        binding.clWeightScale.setOnClickListener { moveToWeighScale() }
        binding.clAnticipated.setOnClickListener { moveToAnticipatedVirtual() }
        binding.clAnticipatedVirtual.setOnClickListener { moveToAnticipatedVirtual() }
    }

    private fun moveToWeightBridge() {
        callBack?.replaceFragment(
            BRIDE
        )
    }

    private fun moveToWeighScale() {
        callBack?.replaceFragment(
            SCALE
        )
    }

    private fun moveToAnticipated() {
        callBack?.replaceFragment(SALES_TYPE_ANTICIPATED)
    }

    private fun moveToAnticipatedVirtual() {
        callBack?.replaceFragment(SALES_TYPE_ANTICIPATED_VIRTUAL)
    }
}
