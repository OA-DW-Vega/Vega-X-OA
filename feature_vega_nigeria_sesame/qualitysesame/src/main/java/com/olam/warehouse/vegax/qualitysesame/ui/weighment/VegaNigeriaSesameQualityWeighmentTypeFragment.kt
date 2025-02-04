package com.olam.warehouse.vegax.qualitysesame.ui.weighment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitysesame.R
import com.olam.warehouse.vegax.qualitysesame.databinding.FragmentVegaNigeriaSesameQualityWeighmentTypeBinding
import com.olam.warehouse.vegax.qualitysesame.utils.MTNR
import com.olam.warehouse.vegax.qualitysesame.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaNigeriaSesameQualityWeighmentTypeFragment : BaseFragment() {

    private val mTAG = VegaNigeriaSesameQualityWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaNigeriaSesameQualityWeighmentTypeBinding
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

    override val layoutResourceId = R.layout.fragment_vega_nigeria_sesame_quality_weighment_type

    companion object {
        fun newInstance() = VegaNigeriaSesameQualityWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaSesameQualityWeighmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitysesame/ui/weighment/VegaNigeriaSesameQualityWeighmentTypeFragment")
            .title("Ecuador Quality")
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
        callBack?.replaceFragment(MTNR)
    }

    private fun moveToSupplier() {
        callBack?.replaceFragment(SUPPLIER)
    }

}
