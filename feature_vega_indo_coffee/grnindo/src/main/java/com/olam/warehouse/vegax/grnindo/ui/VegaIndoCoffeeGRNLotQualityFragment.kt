package com.olam.warehouse.vegax.grnindo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnindo.R
import com.olam.warehouse.vegax.grnindo.databinding.FragmentIndoCoffeeLotQualityBinding
import com.olam.warehouse.vegax.grnindo.databinding.ItemIndoCoffeeQualityDetailsGrnBinding
import com.olam.warehouse.vegax.grnindo.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/13/2021.
 */
class VegaIndoCoffeeGRNLotQualityFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_indo_coffee_lot_quality
    private lateinit var binding: FragmentIndoCoffeeLotQualityBinding
    private val vm: VegaIndoCoffeeGrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var inventoryLots = VegaGrnWeighBridgeId()
    private var qualityParameters = ArrayList<VegaQualityParams>()
    private var qualityDBList = arrayListOf<VegaQualityParamsWithQualitative>()

    interface CallBack {
        fun replaceFragment()
    }

    companion object {
        fun newInstance(inventoryQuality: VegaGrnWeighBridgeId) = VegaIndoCoffeeGRNLotQualityFragment().putArgs {
            putParcelable(Constants.SELECTED_STOCKS_LIST, inventoryQuality)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentIndoCoffeeLotQualityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorycocoa/ui/VegaCocoaInventoryQualityFragment").title("Inventory Cocoa")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        inventoryLots = arguments?.getParcelable(Constants.SELECTED_STOCKS_LIST)!!
        binding.tvLotNo.text = inventoryLots.batchNumber
        binding.tvOk.setOnClickListener { activity?.onBackPressed() }
        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateQualitParamUI(it) })
        inventoryLots.materialCode?.let { vm.getQualityParams(it) }
        /*if (qualityParameters.size > 0)
            setUpAdapter(qualityParameters)
        else
            vm.getPreSamplingQualitydata(inventoryLots.batchNumber ?: "", inventoryLots.materialCode ?: "")*/
    }

    private fun updateQualitParamUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            if (it.isNotEmpty()) {
                qualityDBList.clear()
                qualityDBList.addAll(it)

            }
        }
        if (qualityParameters.size > 0)
            setUpAdapter(qualityParameters)
        else
            vm.getPreSamplingQualitydata(inventoryLots.batchNumber ?: "", inventoryLots.materialCode ?: "")
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>?) {
        data?.let { it ->
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let {
                        if (it.isNotEmpty()) {
                            /* vm.getQualityParams(inventoryLots.materialCode!!, false, inventoryLots.weighBridgeId)
                             vm.qualitylist.observe(viewLifecycleOwner, Observer { updatequalityUI(it) })*/
                            qualityDBList.forEach {it1->
                                it[0].qualityParameters?.forEach {
                                    if (it1.qualityParameter.nameChar.equals(it.sapQCName)){
                                        if(it1.qualitative?.isNotEmpty() == true){
                                            it1.qualitative?.find { it1->it1.charValue.equals(it.satNam) }?.apply {
                                                it.sapQCNameDesc = it1.qualityParameter.descrChar
                                                it.satNam = this.descValue
                                            }
                                        }else{
                                            it.sapQCNameDesc = it1.qualityParameter.descrChar
                                        }
                                    }
                                }
                            }
                            setUpAdapter(it[0].qualityParameters)
                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun setUpAdapter(qualityParameters: List<VegaQualityParams>?) {
        qualityParameters as MutableList
        if(!getCurrentKey().contains("VEGA_IV")) {
            val vegaQualityParams = VegaQualityParams()
            vegaQualityParams.qualityParameterId = "qwertyy"
            vegaQualityParams.qualityParameterName = "qwertyuiop"
            vegaQualityParams.qualityParameterType = "qwertyuiop"
            vegaQualityParams.maxValue = "qwertyuiop"
            vegaQualityParams.minValue = "qwertyuiop"
            vegaQualityParams.sapQCName = "Departure"
            vegaQualityParams.satNam = "1.00 %"
            ///  var VegaQualityParams1=ArrayList<VegaQualityParams>()
            qualityParameters.add(vegaQualityParams)
        }
        binding.rvQuality.setUpAdapter(
            qualityParameters,
            R.layout.item_indo_coffee_quality_details_grn,
            ItemIndoCoffeeQualityDetailsGrnBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lits))
                }
                bindItem.tvQualityNameApprove.text = if(it.sapQCNameDesc?.isNotEmpty() == true)it.sapQCNameDesc else it.sapQCName
                bindItem.tvUnitApprove.text = it.satNam
                //  Log.d("qwerty",qualityParameters.toString())
                //   Log.d("qwerty",it.sapQCName.toString())
            })
    }
}
