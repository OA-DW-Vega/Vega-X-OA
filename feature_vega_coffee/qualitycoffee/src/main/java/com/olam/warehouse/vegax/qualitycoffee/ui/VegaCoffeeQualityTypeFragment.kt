package com.olam.warehouse.vegax.qualitycoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycoffee.R
import com.olam.warehouse.vegax.qualitycoffee.databinding.FragmentVegaCoffeeQualityTypeBinding
import com.olam.warehouse.vegax.qualitycoffee.utils.MTNR
import com.olam.warehouse.vegax.qualitycoffee.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeQualityTypeFragment : BaseFragment() {

    private val mTAG = VegaCoffeeQualityTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaCoffeeQualityTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            weightmentType: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_coffee_quality_type

    companion object {
        fun newInstance() = VegaCoffeeQualityTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeQualityTypeBinding.inflate(layoutInflater)
        if (getCurrentKey().split("_")[1].contains("NI")) {
            binding.llSupplier.visibility = View.GONE
        }
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/weighment/VegaCoffeeQualityTypeFragment").title("Quality")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }
    }

    private fun moveToMtnr() {
        callBack?.replaceFragment(MTNR)
    }

    private fun moveToSupplier() {
        callBack?.replaceFragment(SUPPLIER)
    }

}
