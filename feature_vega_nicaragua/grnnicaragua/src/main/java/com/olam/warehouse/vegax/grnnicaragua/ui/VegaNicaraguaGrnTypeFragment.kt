package com.olam.warehouse.vegax.grnnicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.compareLastSyncTime
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnTypeBinding
import com.olam.warehouse.vegax.grnnicaragua.utils.GRN_FIXED
import com.olam.warehouse.vegax.grnnicaragua.utils.GRN_PTBF
import com.olam.warehouse.vegax.grnnicaragua.utils.GRN_SPOT
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaGrnTypeFragment : BaseFragment() {

    private val mTAG = VegaNicaraguaGrnTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaNicaraguaGrnTypeBinding
    private var callBack: CallBack? = null
    private var lotSequence = ""

    interface CallBack {
        fun replaceFragment(
            grnType: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_type

    companion object {
        fun newInstance() = VegaNicaraguaGrnTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnicaragua/ui/VegaNicaraguaGrnTypeFragment")
            .title("Nicaragua GRN")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        lotSequence = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        PreferenceHelper.save(Constants.IS_EDIT_TRANS, false)
        binding.llSpot.setOnClickListener { moveToGrnSpot() }
        binding.llFixed.setOnClickListener { moveToGrnFixed() }
        binding.llPtbf.setOnClickListener { moveToGrnPtbf() }
    }

    private fun moveToGrnSpot() {
//        if (compareLastSyncTime(App.getAppContext())) {
            if(lotSequence.isNotEmpty())callBack?.replaceFragment(GRN_SPOT) else activity?.toast(getString(R.string.lot_sequence_error))
//        }else
//            activity?.toast(getString(R.string.sync_master_chk))

    }

    private fun moveToGrnFixed() {
        if(lotSequence.isNotEmpty()) callBack?.replaceFragment(GRN_FIXED) else activity?.toast(getString(R.string.lot_sequence_error))
    }

    private fun moveToGrnPtbf() {
        if(lotSequence.isNotEmpty()) callBack?.replaceFragment(GRN_PTBF) else activity?.toast(getString(R.string.lot_sequence_error))
    }

}
