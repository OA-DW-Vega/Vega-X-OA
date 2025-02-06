package com.olam.warehouse.vegax.qualityecuador.ui.weighment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityecuador.R
import com.olam.warehouse.vegax.qualityecuador.databinding.FragmentVegaEcuadorQualityWeighmentTypeBinding
import com.olam.warehouse.vegax.qualityecuador.utils.MTNR
import com.olam.warehouse.vegax.qualityecuador.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaEcuadorQualityWeighmentTypeFragment : BaseFragment() {

    private val mTAG = VegaEcuadorQualityWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaEcuadorQualityWeighmentTypeBinding
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

    override val layoutResourceId = R.layout.fragment_vega_ecuador_quality_weighment_type

    companion object {
        fun newInstance() = VegaEcuadorQualityWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorQualityWeighmentTypeBinding.inflate(layoutInflater)
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
