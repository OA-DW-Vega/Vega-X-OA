package com.olam.warehouse.vegax.offloadingghana.ui.mtnr.weighbridge

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
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.DIRECTIONIN
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.databinding.FragmentVegaGhanaMtnrWeighBridgeListBinding
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingghana.utils.COPIED_MATERIAL
import com.olam.warehouse.vegax.offloadingghana.utils.COPIED_WBID
import com.olam.warehouse.vegax.offloadingghana.utils.MTNR
import com.olam.warehouse.vegax.offloadingghana.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaGhanaMtnrWBListFragment : BaseFragment() {

    private var mAdapter = VegaGhanaMtnrWBListAdapter { moveBagdetail(it) }
    private val vm: VegaGhanaOffloadingViewModel by viewModel()
    private lateinit var mListener: OnWeighBridgeListener
    private lateinit var binding: FragmentVegaGhanaMtnrWeighBridgeListBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_mtnr_weigh_bridge_list
    private var weightmentType: String? = ""
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private var batchNo: String? = ""

    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var offlineDataList = mutableListOf<VegaQualityWBDetails>()

    interface OnWeighBridgeListener {
        fun onWeighBridgeClick(
            wbDetails: VegaQualityWBDetails?
        )

        fun setQualityWBList(it: List<VegaQualityWBDetails>?)
//        fun onQualityOfflineClick()
    }

    companion object {
        fun newInstance() = VegaGhanaMtnrWBListFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaMtnrWeighBridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExtra()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitysesame/ui/weighbridge/VegaNigeriaSesameQualityWBListFragment - $weightmentType")
            .title("Ecuador Quality").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_ghana_mtnr_menu, menu)
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_wb_item)
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

        vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        vm.getWeighBridgeDataOnline()

//        binding.llQualityOffline.setOnClickListener { mListener.onQualityOfflineClick() }
        binding.ivSortDownUp.setOnClickListener {
            val data1 = mQualityWBList.filter { it.wbTempId.contains("TMP") }
            val data2 = mQualityWBList.filter { !it.wbTempId.contains("TMP") }
            mQualityWBList.clear()
            mQualityWBList.addAll(data2.asReversed())
            mQualityWBList.addAll(data1)
            mAdapter.addItems(mQualityWBList)
        }

    }

    private fun initExtra() {
        arguments?.let {
            weightmentType = MTNR
            copiedWbid = it.getString(COPIED_WBID).toString()
            copiedMaterial = it.getString(COPIED_MATERIAL).toString()
        }
    }

    private fun moveBagdetail(wbDetails: VegaQualityWBDetails?) {
        mListener.onWeighBridgeClick(wbDetails)
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
                println("success ==>")
                hideLoading()
                var weighTypedata: List<VegaQualityWBDetails>? = null
                val data = response.data?.data/*?.filter { value -> value.qcStatus.toString() == "R" }*/
                weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
//                    ?.filter { value -> value.weighBridgeType == STO }
                    ?.filter { value -> value.weighMethod == "WB" }
                    ?.filter { value -> !value.netWeight.equals("0.000") }
//                when (weightmentType) {
//                    SUPPLIER -> {
//                        println("Supplier ==>")
//                        val data = response.data?.data
//                            ?.filter { value -> !value.qcStatus!!.contains("X") }
////                            ?.filter { it.grnNumber.isNotEmpty() }
//                        weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
//                            ?.filter { value -> value.weighBridgeType == PROCURE }
//                            ?.filter { value -> !value.netWeight.equals("0.0") }
//                    }
//                    MTNR -> {
//                        println("Mtnr ==>")
//                        val data = response.data?.data/*?.filter { value -> value.qcStatus.toString() == "R" }*/
//                        weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
//                            ?.filter { value -> value.weighBridgeType == STO }
//                            ?.filter { value -> !value.netWeight.equals("0.000") }
//                    }
//                }

                println("Out ==>")

                /*val data = response.data?.data?.filter { value -> !value.qcStatus!!.contains("X") }
                    ?.filter { it.grnNumber.isNotEmpty() }

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
                }*/

                weighTypedata?.let { it1 ->
                    var data1 = listOf<VegaQualityWBDetails>()
                    var filteredList = arrayListOf<VegaQualityWBDetails>()
                    data1 = if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                        it.materialCode.equals(copiedMaterial)
                    } else it1
                    if (data1.isNotEmpty()) {
                        data1.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }.contains(wb.weighBridgeId)) {
                                filteredList.add(wb)
                            }
                        }
                        mListener.setQualityWBList(filteredList)
                        mQualityWBList.clear()
                        mQualityWBList = filteredList
                        mAdapter.addItems(filteredList)
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
                UIUtils.showErrorDialog(requireContext(), "Error")
                hideLoading()
            }
        }
    }

}
