package com.olam.warehouse.vegax.qualitycameroon.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.DIRECTIONIN
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycameroon.R
import com.olam.warehouse.vegax.qualitycameroon.databinding.FragmentVegaCameroonMtnrQualityWeighBridgeListBinding
import com.olam.warehouse.vegax.qualitycameroon.ui.VegaCameroonQualityViewModel
import com.olam.warehouse.vegax.qualitycameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCameroonMtnrQualityWBListFragment : BaseFragment() {

    private var mAdapter = VegaCameroonMtnrWeighBridgeListAdapter { moveBagdetail(it) }
    private val vm: VegaCameroonQualityViewModel by viewModel()
    private lateinit var mListener: OnWeighBridgeListener
    private lateinit var binding: FragmentVegaCameroonMtnrQualityWeighBridgeListBinding
    override val layoutResourceId = R.layout.fragment_vega_cameroon_mtnr_quality_weigh_bridge_list
    private var weightmentType: String? = ""
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""

    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()

    interface OnWeighBridgeListener {
        fun onWeighBridgeClick(
            wbDetails: VegaQualityWBDetails?,
            copiedWbid: String,
            copiedMaterial: String
        )
        fun setQualityWBList(it: List<VegaQualityWBDetails>?)
        fun onQualityOfflineClick()
    }

    companion object {
        fun newInstance() = VegaCameroonMtnrQualityWBListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonMtnrQualityWeighBridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExtra()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("qualitycameroon/ui/weighbridge/VegaCameroonMtnrQualityWBListFragment")
            .title("Vega_Cameroon/Quality").with(tracker)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_cameroon_quality_menu, menu)

        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            mAdapter.addItems(mQualityWBList)
                        } else {
                            mSearchList.clear()
                            mQualityWBList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            mAdapter.addItems(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        binding.rvWeighbridgeMtnr.layoutManager = LinearLayoutManager(this.context)
        binding.rvWeighbridgeMtnr.adapter = mAdapter


        if (isOnline()) {
            vm.weighBridgeOnline.observe(
                viewLifecycleOwner,
                Observer { updateUIWithOnlineData(it) })
            vm.getWeighBridgeDataOnline()
        }
        binding.llQualityOffline.setOnClickListener { mListener.onQualityOfflineClick() }
        binding.ivSortDownUp.setOnClickListener {
            val wbId = mQualityWBList
            mQualityWBList = wbId.asReversed()
            mAdapter.addItems(mQualityWBList)
        }

    }

    private fun initExtra() {
        arguments?.let {
            weightmentType = it.getString(WEIGHBRIDGE_LIST_TYPE)
            copiedWbid = it.getString(COPIED_WBID).toString()
            copiedMaterial = it.getString(COPIED_MATERIAL).toString()
        }

        when (weightmentType) {
            SUPPLIER -> {
                binding.tvType.text = getString(R.string.quality_analysis).plus(" - ").plus(SUPPLIER)
            }
            MTNR -> {
                binding.tvType.text = getString(R.string.quality_analysis).plus(" - ").plus(MTNR)
            }
        }
    }

    private fun moveBagdetail(wbDetails: VegaQualityWBDetails?) {
        mListener.onWeighBridgeClick(wbDetails, copiedWbid, copiedMaterial)
    }



    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val data = response.data?.data/*?.filter { value -> value.qcStatus.toString() == "R" }*/
                var weighTypedata: List<VegaQualityWBDetails>? = null
                when (weightmentType) {
                    SUPPLIER -> {
                        weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
                            ?.filter { value -> value.weighBridgeType == PROCURE }
                    }
                    MTNR -> {
                        weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
                            ?.filter { value -> value.weighBridgeType == STO }
                            ?.filter { value -> !value.netWeight.equals("0.000") }
                    }
                }
                weighTypedata?.let { it1 ->
                    var data1 = listOf<VegaQualityWBDetails>()
                    data1 = if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                        it.materialCode.equals(copiedMaterial)
                    } else it1
                    if (data1.isNotEmpty()) {
                        mListener.setQualityWBList(data1)
                        mQualityWBList.clear()
                        mQualityWBList = data1 as MutableList<VegaQualityWBDetails>
                        mAdapter.addItems(data1)
                        binding.tvNoData.gone()
                        binding.rvWeighbridgeMtnr.visible()
                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighbridgeMtnr.gone()
                    }

                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }


}
