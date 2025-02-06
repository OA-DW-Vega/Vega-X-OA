package com.olam.warehouse.vegax.qualityindiacoffee.ui.weighment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindiacoffee.R
import com.olam.warehouse.vegax.qualityindiacoffee.databinding.FragmentVegaIndiaCoffeeQualityWeighmentTypeBinding
import com.olam.warehouse.vegax.qualityindiacoffee.utils.DRIEDLOTS
import com.olam.warehouse.vegax.qualityindiacoffee.utils.MTNR
import com.olam.warehouse.vegax.qualityindiacoffee.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeQualityWeighmentTypeFragment : BaseFragment() {

    private val mTAG = VegaIndiaCoffeeQualityWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaIndiaCoffeeQualityWeighmentTypeBinding
    private var callBack: CallBack? = null
    private var isMtnrOnly = false

    interface CallBack {
        fun replaceFragment(
            weightmentType: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_india_coffee_quality_weighment_type

    companion object {
        fun newInstance() = VegaIndiaCoffeeQualityWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeQualityWeighmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/weighment/VegaQualityWeighmentTypeFragment").title("Quality")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        isMtnrOnly = arguments?.getBoolean(UIUtils.MTNR) ?: false
        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }
        binding.llDriedLots.setOnClickListener { moveToDriedLots() }
        /*if(isMtnrOnly){
            binding.llSupplier.gone()
            binding.llMtnr.visible()
        }else{
            binding.llSupplier.visible()
            binding.llMtnr.visible()
        }*/
    }

    private fun moveToMtnr() {
        callBack?.replaceFragment(MTNR)
    }

    private fun moveToSupplier() {
        callBack?.replaceFragment(SUPPLIER)
    }

    private fun moveToDriedLots() {
        callBack?.replaceFragment(DRIEDLOTS)
    }


}
