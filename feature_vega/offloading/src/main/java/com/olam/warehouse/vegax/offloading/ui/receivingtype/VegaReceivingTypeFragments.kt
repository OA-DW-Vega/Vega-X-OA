package com.olam.warehouse.vegax.offloading.ui.receivingtype

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloading.R
import com.olam.warehouse.vegax.offloading.databinding.FragmentVegaReceivingTypeBinding
import com.olam.warehouse.vegax.offloading.utils.MTNR
import com.olam.warehouse.vegax.offloading.utils.PROCURE
import com.olam.warehouse.vegax.offloading.utils.STO
import com.olam.warehouse.vegax.offloading.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 2/1/2020.
 */

class VegaReceivingTypeFragments : BaseFragment() {

    private val mTAG = VegaReceivingTypeFragments::class.java.canonicalName
    private var offloadingData = VegaOffloadingTrucks()
    private lateinit var binding: FragmentVegaReceivingTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(receivingType: String, offloadingData: VegaOffloadingTrucks)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_receiving_type

    companion object {
        fun newInstance() = VegaReceivingTypeFragments().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaReceivingTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloading/ui/receivingtype/VegaReceivingTypeFragments").title("Offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }
    }

    private fun moveToMtnr() {
        offloadingData = VegaOffloadingTrucks()
        offloadingData.weighBridgeType = STO
        callBack?.replaceFragment(MTNR, offloadingData)
    }

    private fun moveToSupplier() {
        offloadingData = VegaOffloadingTrucks()
        offloadingData.weighBridgeType = PROCURE
        callBack?.replaceFragment(SUPPLIER, offloadingData)
    }


}
