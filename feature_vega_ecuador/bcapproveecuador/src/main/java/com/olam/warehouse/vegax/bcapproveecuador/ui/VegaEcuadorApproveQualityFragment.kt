package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.QUALITY_DATA
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentVegaEcuadorApproveQualityBinding
import com.olam.warehouse.vegax.bcapproveecuador.databinding.ItemVegaEcuadorApproveQualityDetailsBinding
import com.olam.warehouse.vegax.bcapproveecuador.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Roshna Parambil on 1/18/2022.
 */
class VegaEcuadorApproveQualityFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ecuador_approve_quality
    private lateinit var binding: FragmentVegaEcuadorApproveQualityBinding
    private val vm: VegaEcuadorBcApproveViewModel by viewModel()
    private var callBack: CallBack? = null
    private var inventoryLots = VegaGrnWeighBridgeId()
    private var approveQuality = mutableListOf<VegaEcuadorApproveWbDetail>()


    interface CallBack {
        fun replaceFragment()
    }

    companion object {
        fun newInstance(qualityData: ArrayList<VegaEcuadorApproveWbDetail>) =
            VegaEcuadorApproveQualityFragment().putArgs {
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
        binding = FragmentVegaEcuadorApproveQualityBinding.inflate(layoutInflater)
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

        approveQuality =
            arguments?.getParcelableArrayList<VegaEcuadorApproveWbDetail>(QUALITY_DATA) as MutableList<VegaEcuadorApproveWbDetail>

        binding.tvOk.setOnClickListener { activity?.onBackPressed() }
        binding.tvLotNo.text = approveQuality[0].charg
        setUpAdapter(approveQuality)

    }


    private fun setUpAdapter(data: MutableList<VegaEcuadorApproveWbDetail>) {
        data.let {
            approveQuality = it

            var i = 0
            binding.rvQuality.setUpAdapter(
                approveQuality.first().qualityParameters as MutableList<VegaQualityParams>,
                R.layout.item_vega_ecuador_approve_quality_details,
                ItemVegaEcuadorApproveQualityDetailsBinding::inflate,
                { it, pos, bindItem ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }
                    if (it.sapQCName != "ZNG_GR_DATE") {
                        bindItem.tvQualityGRNNameApprove.text = it.sapQCName?.removePrefix("GH_")
                        bindItem.tvQualityValue.text = it.satNam
                    }
                },
                {

                })
        }
    }
}
