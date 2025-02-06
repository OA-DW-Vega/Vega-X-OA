package com.olam.warehouse.vegax.offloadingcocoa.ui.wblist

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.DIRECTIONIN
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentVegaCocoOffloadWeighscaleListBinding
import com.olam.warehouse.vegax.offloadingcocoa.databinding.ItemVegaCocoWeighscaleTruckListBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcocoa.utils.MTNR
import com.olam.warehouse.vegax.offloadingcocoa.utils.OFFLOAD_DATA
import com.olam.warehouse.vegax.offloadingcocoa.utils.PROCURE
import com.olam.warehouse.vegax.offloadingcocoa.utils.STO
import com.olam.warehouse.vegax.offloadingcocoa.utils.SUPPLIER
import com.olam.warehouse.vegax.offloadingcocoa.utils.WAREHOUSE
import com.olam.warehouse.vegax.offloadingcocoa.utils.WEIGHSCALE
import com.olam.warehouse.vegax.offloadingcocoa.utils.WEIGHSCALE_SUPPLIER
import com.olam.warehouse.vegax.offloadingcocoa.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel


class VegaCocoOffloadWeighscaleListFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaCocoOffloadWeighscaleListBinding
    private var callBack: VegaCoCoaOffloadReplaceFragmentCallback? = null
    private var gateEntryData = VegaCoCoaReceiving()
    private var gateEntry = mutableListOf<VegaCoCoaReceiving>()
    private val mSearchList = mutableListOf<VegaCoCoaReceiving>()

    override val layoutResourceId = R.layout.fragment_vega_coco_offload_weighscale_list
    private val vm: VegaCoCoaOffloadingViewModel by viewModel()
    private var vegaWbIds = listOf<VegaCoCoaReceiving>()
    private var weighmentType: String = ""
    private var currentKey = getCurrentKey()

    companion object {
            fun newInstance(type: VegaCoCoaReceiving) = VegaCocoOffloadWeighscaleListFragment().putArgs {
            putParcelable(OFFLOAD_DATA, type)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as VegaCoCoaOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        gateEntryData = arguments?.getParcelable(OFFLOAD_DATA)!!
        binding = FragmentVegaCocoOffloadWeighscaleListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.llWeighbridge.gone()
         binding.tvType.text =
            getString(R.string.waiting_truck_header).plus(" ")
                .plus(if (gateEntryData.weighBridgeType.equals(PROCURE)) getString(R.string.supplier) else MTNR)
        if (isOnline()) {
            vm.getWeighBridgeDetail()
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        }
       binding.ivSortDownUp.setOnClickListener {
            if (gateEntry.isNotEmpty()) {
                val data = gateEntry
                gateEntry = data.asReversed()
                setUpAdapter(gateEntry)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
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
                            setUpAdapter(gateEntry)
                        } else {
                            mSearchList.clear()
                            gateEntry.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.vehicleNumber?.contains(text)!!) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setUpAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun setUpAdapter(data: List<VegaCoCoaReceiving>) {
        val gateEntry1 = data as MutableList<VegaCoCoaReceiving>
        binding.tvTruckTotal.text = getString(R.string.no_of_truck).plus(" ").plus(gateEntry1.size.toString())
        binding.rvWeighbridge.setUpAdapter(
            gateEntry1.asReversed(),
            R.layout.item_vega_coco_weighscale_truck_list,
            ItemVegaCocoWeighscaleTruckListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvTruckNo.text =
                    if (it.vehicleNumber.isNullOrEmpty()) "-" else it.vehicleNumber
                if (gateEntryData.weighBridgeType == PROCURE) {
                    bindItem.tvdifference.text = getString(R.string.supplier)
                    bindItem.tvSupplierName.text = it.supplierName ?: it.supplierCode
                    bindItem.tvObdNumber.visibility = View.GONE
                    bindItem.tvObdNumberLabel.visibility = View.GONE
                } else {
                    bindItem.tvdifference.visibility = View.GONE
                    bindItem.tvSupplierName.visibility = View.GONE
                    bindItem.tvdifference.text = WAREHOUSE
                    bindItem.tvSupplierName.text = "-"
                    bindItem.tvObdNumber.visibility = View.VISIBLE
                    bindItem.tvObdNumberLabel.visibility = View.VISIBLE
                }
                bindItem.tvdifference.text = getString(R.string.supplier)
                bindItem.tvSupplierName.text = it.supplierName ?: it.supplierCode
                bindItem.tvWeighBridgeId.text = it.weighBridgeId
                bindItem.tvMaterial.text = it.materialName
                bindItem.tvObdNumber.text = it.delivery
                bindItem.tvWeight.text = it.grossWeight.plus(" ").plus(it.unitsOfMeasure)
                val times = it.erdat?.split('(', ')')
                bindItem.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(
                        it1,
                        App.getAppContext()
                    )
                }
            },
            {
                val item = this
                item.weighBridgeType = gateEntryData.weighBridgeType
                item.imageString = gateEntryData.imageString
                callBack?.replaceFragment(
                    if (gateEntryData.weighBridgeType == STO) WEIGHSCALE else WEIGHSCALE_SUPPLIER,
                    item
                )
            })
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaCoCoaReceiving>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.let { it1 ->
                    if (it1.data.isNotEmpty()) {
                        vegaWbIds = listOf()
                        var filterList = ArrayList<VegaCoCoaReceiving>()
                        if (gateEntryData.imageString.equals(WEIGHSCALE)) {
                            filterList = it1.data.filter { it.weighMethod == "WS" } as ArrayList<VegaCoCoaReceiving>
                        } else {
                            filterList = it1.data.filter { it.weighMethod == "WB" }
                                .filter { wb -> !wb.grossWeight.equals("0.000") } as ArrayList<VegaCoCoaReceiving>
                        }

                        vegaWbIds = if (gateEntryData.weighBridgeType == PROCURE)
                            filterList.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == PROCURE }
                                .filter { wb -> wb.qcStatus?.equals("X") == true }/*.filter { wb-> wb.bagWeight=="0.000" }*/
                        else
                            filterList.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == STO }
                                .filter { wb -> wb.qcStatus.isNullOrEmpty() }

                        gateEntry = vegaWbIds as MutableList<VegaCoCoaReceiving>
                        if (vegaWbIds.size > 0) {
                            setUpAdapter(vegaWbIds)
                            binding.tvNoData.gone()
                            binding.llWeighbridge.visible()
                        } else {
                            binding.tvNoData.visible()
                            binding.llWeighbridge.gone()
                        }
                    } else {
                        binding.tvNoData.visible()
                        binding.llWeighbridge.gone()
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
}
