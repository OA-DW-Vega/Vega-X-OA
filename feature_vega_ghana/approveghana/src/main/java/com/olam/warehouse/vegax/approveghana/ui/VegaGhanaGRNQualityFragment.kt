package com.olam.warehouse.vegax.approveghana.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.approveghana.R
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGRNGhanaQuality
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGRNGhanaQualityParams
import com.olam.warehouse.vegax.approveghana.databinding.FragmentVegaGhanaGrnQualityBinding
import com.olam.warehouse.vegax.approveghana.utils.GRN_DATA
import com.olam.warehouse.vegax.approveghana.utils.QUALITY_DATA
import com.olam.warehouse.vegax.approveghana.utils.getColor
import kotlinx.android.synthetic.main.item_vega_ghana_grn_quality_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Roshna Parambil on 9/9/2020.
 */
class VegaGhanaGRNQualityFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_grn_quality
    private lateinit var binding: FragmentVegaGhanaGrnQualityBinding
    private val vm: VegaGhanaGrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var inventoryLots = VegaGrnWeighBridgeId()
    private var approveQuality = mutableListOf<VegaGRNGhanaQuality>()


    interface CallBack {
        fun replaceFragment()
    }

    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId,qualityData: ArrayList<VegaGRNGhanaQuality>) =
            VegaGhanaGRNQualityFragment().putArgs {
            putParcelable(GRN_DATA, grnData)
                putParcelableArrayList(QUALITY_DATA,qualityData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaGrnQualityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnigeria/ui/VegaNigeriaSesameGRNQualityFragment").title("Inventory Cocoa")
            .with(tracker)
        initUI()
    }

    private fun initUI() {

        val wbid = arguments?.get(GRN_DATA) as VegaGrnWeighBridgeId
        approveQuality =
            arguments?.getParcelableArrayList<VegaGRNGhanaQuality>(QUALITY_DATA) as MutableList<VegaGRNGhanaQuality>

        binding.tvOk.setOnClickListener { activity?.onBackPressed() }
        binding.tvLotNo.text = wbid.batchNumber
        setUpAdapter(approveQuality)

    }


    private fun setUpAdapter(data: MutableList<VegaGRNGhanaQuality>) {
        data.let {
            approveQuality = it

            var i = 0
            binding.rvQuality.setUp(approveQuality.first().qualityParameters as MutableList<VegaGRNGhanaQualityParams>,
                R.layout.item_vega_ghana_grn_quality_details,
                { it, pos ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }
                    if(it.sapQCName != "ZNG_GR_DATE"){
                        tvQualityGRNNameApprove.text = it.sapQCName?.removePrefix("GH_")
                        tvQualityValue.text = it.satNam
                    }
                },
                {

                })
        }
    }
}
