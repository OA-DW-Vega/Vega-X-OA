package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentVegaEcuadorBcApproveWbListBinding
import com.olam.warehouse.vegax.bcapproveecuador.databinding.ItemEcuadorBcapproveWbListBinding
import com.olam.warehouse.vegax.bcapproveecuador.utils.LIST__WB_DETAILPAGE
import com.olam.warehouse.vegax.bcapproveecuador.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Roshna Parambil on 1/12/2022.
 */
class VegaEcuadorBCApproveWBListFragment : BaseFragment() {

    private val vm: VegaEcuadorBcApproveViewModel by viewModel()
    private var weighBridgeList = mutableListOf<VegaGrnWeighBridgeId>()
    private val mSearchList = mutableListOf<VegaGrnWeighBridgeId>()
    private lateinit var binding: FragmentVegaEcuadorBcApproveWbListBinding
    private var callBack: CallBack? = null
    override val layoutResourceId = R.layout.fragment_vega_ecuador_bc_approve_wb_list
    private var offlineDataList = mutableListOf<VegaGrnWeighBridgeId>()
    private var BcApproveWblist = mutableListOf<VegaEcuadorBcApproveWBDetails>()

    companion object {
        fun newInstance() = VegaEcuadorBCApproveWBListFragment()
            .putArgs {
            }

    }

    interface CallBack {
        fun replaceFragment(moveFrag: String, item: VegaEcuadorBcApproveWBDetails)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaEcuadorBcApproveWbListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("bcapproveecuador/ui/weighbridge/VegaEcuadorGrnWBListFragment").title("Ecuador GRN")
            .with(tracker)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_wb_item)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setupAdapter(BcApproveWblist)
                        } else {
                            mSearchList.clear()
                            weighBridgeList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId?.contains(text)!!) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setupAdapter(BcApproveWblist)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
//                    showBackConformationDialog()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initUI() {
        if (isOnline()) {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
//            vm.getWeighBridgeList()
            vm.getWeighBridgeDetail(getPlantDetails().plantId)

        } else {
//            vm.weighBridgeLocal.observe(viewLifecycleOwner, Observer { updateUIWithLocalData(it) })
//            vm.getWeighBridgeDetail()
        }
        binding.ivSortDownUp.setOnClickListener {
            val data1 = BcApproveWblist.filter { it.batchNumber?.contains("TMP") == true }
            val data2 = BcApproveWblist.filter {  it.batchNumber?.contains("TMP") != true }
            BcApproveWblist.clear()
            BcApproveWblist.addAll(data2.asReversed())
            BcApproveWblist.addAll(data1)
            setupAdapter(BcApproveWblist)
        }

        //Commented lines are for OFFLINE flow.

//        vm.weighBridgeOfflineCount.observe(this, Observer { enableOfflineBar(it) })
//        vm.getOfflineWeighBridgeDetailCount()

//        binding.llQualityOffline.setOnClickListener {
//            callBack?.replaceFragment(
//                GRN_OFFLINE_FRAG,
//                VegaGrnWeighBridgeId()
//            )
//        }
    }

    private fun enableOfflineBar(items: List<VegaGrnWeighBridgeId>) {
        if (items.size > 0) {
            binding.llQualityOffline.visible()
            offlineDataList = items as MutableList<VegaGrnWeighBridgeId>
        } else binding.llQualityOffline.gone()
    }

   /* private fun updateUIWithLocalData(data: List<VegaGrnWeighBridgeId>?) {
        val weighBridge =
            data?.filter { it.qcStatus.isNullOrEmpty() && it.grnNumber.isNullOrEmpty() }
        if (weighBridge?.size!! > 0) {
            weighBridgeList = weighBridge as MutableList<VegaGrnWeighBridgeId>
            setUpAdapter(weighBridgeList)
            binding.tvNoData.gone()
            binding.rvWeighBridgeId.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvWeighBridgeId.gone()
        }
    }*/

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            BcApproveWblist.clear()
                            val dataValue = it.data?.data!!
                            BcApproveWblist.addAll(dataValue.asReversed())
//                            updateSelectLotValues()
                            setupAdapter(BcApproveWblist)

                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING ->
                    showLoading()
                Resource.Status.ERROR -> {
                    BcApproveWblist.clear()
                    setupAdapter(BcApproveWblist)
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun setupAdapter(data: MutableList<VegaEcuadorBcApproveWBDetails>) {
        if (data.size > 0) {
            binding.rvWeighBridgeId.visible()
            binding.tvNoData.invisible()
        } else {
            binding.rvWeighBridgeId.invisible()
            binding.tvNoData.visible()
        }
        binding.rvWeighBridgeId.setUpAdapter(
            data,
            R.layout.item_ecuador_bcapprove_wb_list,
            ItemEcuadorBcapproveWbListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvsupplierid.text = it.grnNumber
                bindItem.tvWeightValue.text =
                    it.grnQty.toString().trim().plus(" ").plus(it.unitsOfMeasure)
                bindItem.tvGrnPriceValue.text =
                    it.unitPrice.toString().trim().plus(" ").plus(it.waers)
                bindItem.tvDateValue.text = it.grnType

                bindItem.llLotItem.setOnClickListener { view ->
                    callBack?.replaceFragment(LIST__WB_DETAILPAGE, data[pos])
                }


            })
        hideLoading()
    }


/*
    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge =
                        it.data?.data?.filter { it.qcStatus.isNullOrEmpty() && it.grnNumber.isNullOrEmpty() }
                    if (weighBridge?.size!! > 0) {
                        //weighBridgeList = weighBridge as MutableList<VegaGrnWeighBridgeId>
                        weighBridge.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }.contains(wb.weighBridgeId)) {
                                weighBridgeList.add(wb)
                            }
                        }
                        setUpAdapter(weighBridgeList)
                        binding.tvNoData.gone()
                        binding.rvWeighBridgeId.visible()
                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighBridgeId.gone()
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }
*/

    /*private fun setUpAdapter(data: List<VegaGrnWeighBridgeId>?) {
        val weighBridgeList1 = data as MutableList<VegaGrnWeighBridgeId>
        var count = 0
        binding.rvWeighBridgeId.setUp(
            weighBridgeList1.asReversed(),
            R.layout.item_vega_ecuador_weighbridge_grn,
            { it, pos ->
                it.unitPrice = it.unitPrice.toString().replace("\\s".toRegex(), "")
                tvProcurementType.text =
                    if (it.purchaseDocNum.isNullOrEmpty()) getString(R.string.spot_purchase) else getString(R.string.fixed_purchase)
                tvSupplierName.text = it.supplierName
                tvWeight.text = it.netWeight.plus(it.unitsOfMeasure)
                tvWeighBridgeId.text = it.weighBridgeId
                val times = it.erdat?.split('(', ')')
                tvDate.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }

                count++
            },
            {
                val item = this
                callBack?.replaceFragment(GRN_FRAG, item)
            })
    }*/
}
