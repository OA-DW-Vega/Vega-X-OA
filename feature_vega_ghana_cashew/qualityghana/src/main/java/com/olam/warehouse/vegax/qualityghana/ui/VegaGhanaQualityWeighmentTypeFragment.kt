package com.olam.warehouse.vegax.qualityghana.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityghana.R
import com.olam.warehouse.vegax.qualityghana.databinding.FragmentVegaGhanaQualityWeighmentTypeBinding
import com.olam.warehouse.vegax.qualityghana.utils.MTNR
import com.olam.warehouse.vegax.qualityghana.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 9/25/2020.
 */
class VegaGhanaQualityWeighmentTypeFragment : BaseFragment() {

    private val mTAG = VegaGhanaQualityWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaGhanaQualityWeighmentTypeBinding
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

    override val layoutResourceId = R.layout.fragment_vega_ghana_quality_weighment_type

    companion object {
        fun newInstance() = VegaGhanaQualityWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaQualityWeighmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualityecuador/ui/weighment/VegaEcuadorQualityWeighmentTypeFragment")
            .title("Ecuador Quality")
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

