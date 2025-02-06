package com.olam.warehouse.vegax.qualityindo.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindo.R
import com.olam.warehouse.vegax.qualityindo.databinding.FragmentIndoCoffeeQualityWeighBridgeListBinding
import com.olam.warehouse.vegax.qualityindo.ui.VegaIndoCoffeeQualityViewModel
import com.olam.warehouse.vegax.qualityindo.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeQualityWBListFragment : BaseFragment() {

    private var mAdapter = VegaIndoCoffeeWeighBridgeListAdapter { moveBagdetail(it) }
    private val vm: VegaIndoCoffeeQualityViewModel by viewModel()
    private lateinit var mListener: OnWeighBridgeListener
    private lateinit var binding: FragmentIndoCoffeeQualityWeighBridgeListBinding
    override val layoutResourceId = R.layout.fragment_indo_coffee_quality_weigh_bridge_list
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
        fun newInstance() = VegaIndoCoffeeQualityWBListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffeeQualityWeighBridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExtra()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/weighbridge/VegaCoffeeQualityWBListFragment - $weightmentType")
            .title("Quality").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_quality_indo_coffee_menu, menu)
        //search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
        //searchView = search?.actionView as SearchView?
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
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
        binding.rvWeighbridge.layoutManager = LinearLayoutManager(this.context)
        binding.rvWeighbridge.adapter = mAdapter
        /*vm.qualityOfflineList.observe(viewLifecycleOwner, Observer { enableOfflineLabel(it) })
        vm.getQualityOfflineListCount()*/
        if (AppUtils.isOnline()) {
            vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
            vm.getWeighBridgeDataOnline()
        } else {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
            vm.getWeighBridgeDetail()
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
                binding.tvType.text = getString(R.string.quality_analysis).plus(" - ").plus(getString(R.string.supplier))
            }
            MTNR -> {
                binding.tvType.text = getString(R.string.quality_analysis).plus(" - ").plus(MTNR)
            }
        }
    }

    private fun moveBagdetail(wbDetails: VegaQualityWBDetails?) {

        mListener.onWeighBridgeClick(wbDetails, copiedWbid, copiedMaterial)
    }

    fun updateAdapter(mQualityWBList: MutableList<VegaQualityWBDetails>) {
        mQualityWBList.let { data ->
            binding.rvWeighbridge.let {
                //if (mAdapter.itemCount != mQualityWBList.size) {
                if (data.size > 0 && isValidDataAvailable(data)) {
                    mAdapter.addItems(data)
                    binding.tvNoData.gone()
                    binding.rvWeighbridge.visible()
                } else {
                    binding.tvNoData.visible()
                    binding.rvWeighbridge.gone()
                }
            }
            //}
        }
    }

    private fun isValidDataAvailable(weighbridge: MutableList<VegaQualityWBDetails>): Boolean {
        return weighbridge.any { data -> !data.qcStatus!!.contains("X") }
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val data = response.data?.data/*?.filter { value -> value.qcStatus.toString() == "R" }*/
                var weighTypedata: List<VegaQualityWBDetails>? = null
                when (weightmentType) {
                    SUPPLIER -> {
                        if (getCurrentKey().contains("VEGA_IV") && getCurrentKey().contains("COCO"))
                            weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
                                ?.filter { value -> value.weighBridgeType == PROCURE }
                                ?.filter { va -> va.qcStatus.isNullOrEmpty() }
                        else
                            weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
                                ?.filter { value -> value.weighBridgeType == PROCURE }
                                ?.filter { va -> va.qcStatus.isNullOrEmpty() }
                                ?.filter { value -> !value.netWeight.equals("0.000") }

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
                        binding.rvWeighbridge.visible()
                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighbridge.gone()
                    }

                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun enableOfflineLabel(response: List<VegaQualityWBDetails>) {
        response.let {
            if (it.isNotEmpty()) {
                binding.llQualityOffline.visible()
            } else {
                binding.llQualityOffline.gone()
            }
        }
    }


    private fun updateUI(response: List<VegaQualityWBDetails>?) {
        response?.let { data ->
            var weighTypedata: List<VegaQualityWBDetails>? = null
            when (weightmentType) {
                SUPPLIER -> {
                    weighTypedata = data.filter { wb -> wb.direction == DIRECTIONIN }
                        .filter { value -> value.weighBridgeType == PROCURE }
                        .filter { va -> va.qcStatus.isNullOrEmpty() }
                        .filter { value -> !value.netWeight.equals("0.000") }
                }
                MTNR -> {
                    weighTypedata = data.filter { wb -> wb.direction == DIRECTIONIN }
                        .filter { value -> value.weighBridgeType == STO }
                        .filter { value -> !value.netWeight.equals("0.000") }
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
                    binding.rvWeighbridge.visible()
                } else {
                    binding.tvNoData.visible()
                    binding.rvWeighbridge.gone()
                }

            }
        }
    }
}

