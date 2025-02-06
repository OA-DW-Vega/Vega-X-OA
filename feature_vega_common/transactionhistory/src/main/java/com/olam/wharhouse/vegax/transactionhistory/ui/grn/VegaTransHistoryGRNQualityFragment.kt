package com.olam.wharhouse.vegax.transactionhistory.ui.grn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCashewGRNHistoryTransactions
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.databinding.FragmentTransHistoryQualityBinding
import com.olam.wharhouse.vegax.transactionhistory.databinding.ItemQualityDetailsGrnBinding
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHisViewModel
import com.olam.wharhouse.vegax.transactionhistory.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/13/2021.
 */
class VegaTransHistoryGRNQualityFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_trans_history_quality
    private lateinit var binding: FragmentTransHistoryQualityBinding
    private val vm: VegaTransHisViewModel by viewModel()
    private var callBack: CallBack? = null
    private var inventoryLots = VegaGRNHistoryTransactions()
    private var qualityParameters = ArrayList<VegaQualityParams>()
    private var actualQualityList = ArrayList<VegaQualityParamsWithQualitative>()

    interface CallBack {
        fun replaceFragment()
    }

    companion object {
        fun newInstance(inventoryQuality: VegaGRNHistoryTransactions) = VegaTransHistoryGRNQualityFragment().putArgs {
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
        binding = FragmentTransHistoryQualityBinding.inflate(layoutInflater)
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
       // binding.tvLotNo.text = inventoryLots.batchNumber
        binding.tvOk.setOnClickListener { activity?.onBackPressed() }
        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateQualityUI(it) })
        inventoryLots.materialCode?.let { vm.getQualityParams("000000".plus(it)) }

    }

    private fun updateQualityUI(it: List<VegaQualityParamsWithQualitative>?) {
        if (qualityParameters.size > 0)
            setUpAdapter(qualityParameters)
        else
            vm.getPreSamplingQualitydata(inventoryLots.batchNumber ?: "", inventoryLots.materialCode ?: "",inventoryLots.plantId ?: "")
        it?.let {
            actualQualityList.clear()
            actualQualityList.addAll(it)
        }
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
        qualityParameters.forEach { item ->
            val filterItem = actualQualityList.filter { it.qualityParameter.nameChar.equals(item.sapQCName) }
            if(filterItem.isNotEmpty()) item.qualityParameterName = filterItem.get(0).qualityParameter.descrChar
            if(filterItem.isEmpty()) item.qualityParameterName= item.sapQCName
        }
        binding.rvQuality.setUpAdapter(
            qualityParameters,
            R.layout.item_quality_details_grn,
            ItemQualityDetailsGrnBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lits))
                }
                bindItem.tvQualityNameApprove.text = it.qualityParameterName
                bindItem.tvUnitApprove.text = it.satNam
                //  Log.d("qwerty",qualityParameters.toString())
                //   Log.d("qwerty",it.sapQCName.toString())
            })
    }
}
