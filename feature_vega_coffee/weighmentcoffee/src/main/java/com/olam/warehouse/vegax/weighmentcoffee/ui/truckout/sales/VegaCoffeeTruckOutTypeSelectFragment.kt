package com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.sales

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentCoffeeSaleTruckOutTypeBinding
import com.olam.warehouse.vegax.weighmentcoffee.utils.LOCAL_SALES
import com.olam.warehouse.vegax.weighmentcoffee.utils.THIRD_PARTY_SALES
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeTruckOutTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_coffee_sale_truck_out_type
    private lateinit var binding: FragmentCoffeeSaleTruckOutTypeBinding
    private var callBack: CallBack? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    interface CallBack {
        fun replaceMtntFragment(
            flag: String,
            moveFrag: Boolean
        )
    }

    companion object {
        fun newInstance() = VegaCoffeeTruckOutTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCoffeeSaleTruckOutTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcoffee/ui/VegaCoffeeOffloadTypeSelectFragment").title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
        binding.llRmin.setOnClickListener { moveToSupplier() }
        binding.llFgrn.setOnClickListener { moveToMtnr() }
    }

    private fun moveToSupplier() {
        callBack?.replaceMtntFragment(
            THIRD_PARTY_SALES, true
        )
    }

    private fun moveToMtnr() {
        callBack?.replaceMtntFragment(
            LOCAL_SALES, false
        )
    }
}
