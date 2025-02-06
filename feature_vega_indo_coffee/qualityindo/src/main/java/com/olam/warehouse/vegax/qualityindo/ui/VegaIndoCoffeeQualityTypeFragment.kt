package com.olam.warehouse.vegax.qualityindo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindo.R
import com.olam.warehouse.vegax.qualityindo.databinding.FragmentIndoCoffeeQualityTypeBinding
import com.olam.warehouse.vegax.qualityindo.utils.MTNR
import com.olam.warehouse.vegax.qualityindo.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeQualityTypeFragment : BaseFragment() {

    private val mTAG = VegaIndoCoffeeQualityTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentIndoCoffeeQualityTypeBinding
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

    override val layoutResourceId = R.layout.fragment_indo_coffee_quality_type

    companion object {
        fun newInstance() = VegaIndoCoffeeQualityTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIndoCoffeeQualityTypeBinding.inflate(layoutInflater)
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

