package com.olam.warehouse.vegax.grnnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnigeria.R
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGRNQuality
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGRNQualityParams
import com.olam.warehouse.vegax.grnnigeria.databinding.FragmentVegaNigeriaGrnQualityBinding
import com.olam.warehouse.vegax.grnnigeria.databinding.ItemVegaNigeriaGrnQualityDetailsBinding
import com.olam.warehouse.vegax.grnnigeria.utils.GRN_DATA
import com.olam.warehouse.vegax.grnnigeria.utils.QUALITY_DATA
import com.olam.warehouse.vegax.grnnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Roshna Parambil on 9/9/2020.
 */
class VegaNigeriaGRNQualityFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_grn_quality
    private lateinit var binding: FragmentVegaNigeriaGrnQualityBinding
    private val vm: VegaNigeriaGrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var inventoryLots = VegaGrnWeighBridgeId()
    private var approveQuality = mutableListOf<VegaNigeriaGRNQuality>()

    interface CallBack {
        fun replaceFragment()
    }

    companion object {
        fun newInstance(
            grnData: VegaGrnWeighBridgeId,
            qualityData: ArrayList<VegaNigeriaGRNQuality>
        ) =
            VegaNigeriaGRNQualityFragment().putArgs {
                putParcelable(GRN_DATA, grnData)
                putParcelableArrayList(QUALITY_DATA, qualityData)
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
        binding = FragmentVegaNigeriaGrnQualityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnigeria/ui/VegaNigeriaGRNQualityFragment").title("Inventory Cocoa")
            .with(tracker)
        initUI()
    }

    private fun initUI() {

        val wbid = arguments?.get(GRN_DATA) as VegaGrnWeighBridgeId
        approveQuality =
            arguments?.getParcelableArrayList<VegaNigeriaGRNQuality>(QUALITY_DATA) as MutableList<VegaNigeriaGRNQuality>

        binding.tvOk.setOnClickListener { activity?.onBackPressed() }
        binding.tvLotNo.text = wbid.batchNumber
        setUpAdapter(approveQuality)
    }

    private fun setUpAdapter(data: MutableList<VegaNigeriaGRNQuality>) {
        data.let {
            approveQuality = it
            var i = 0
            var list = mutableListOf<VegaNigeriaGRNQualityParams>()
            //  if (getCurrentKey().split("_")[2].contains("CASH")) {
            list =
                (approveQuality.first().qualityParameters.filter { it.sapQCName != "ZNG_GR_DATE" }).toMutableList()
            /* } else if (getCurrentKey().split("_")[2].contains("SESA")) {
                 list = approveQuality.first().qualityParameters  as MutableList<VegaNigeriaGRNQualityParams>
             } else if (getCurrentKey().split("_")[2].contains("COFF")) {
                 list = approveQuality.first().qualityParameters  as MutableList<VegaNigeriaGRNQualityParams>
             }*/

            binding.rvQuality.setUpAdapter(
                list,
                R.layout.item_vega_nigeria_grn_quality_details,
                ItemVegaNigeriaGrnQualityDetailsBinding::inflate,
                { it, pos, bindItem ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }
                    if (it.sapQCName != "ZNG_GR_DATE") {
                        bindItem.tvQualityNameApprove.text = it.sapQCName?.removePrefix("ZNG_")
                        bindItem.tvUnitApprove.text = it.satNam
                    }
                },
                {

                })
        }
    }
}
