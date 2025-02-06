package com.olam.warehouse.vegax.ppqsesame.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ppqsesame.R
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaPpqWarehouseModel
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLots
import com.olam.warehouse.vegax.ppqsesame.databinding.FragmentVegaSesamePpqLotsListBinding
import com.olam.warehouse.vegax.ppqsesame.databinding.ItemVegaSesamePpqLotDetailsBinding
import com.olam.warehouse.vegax.ppqsesame.utils.PARAMS_FRAG
import com.olam.warehouse.vegax.ppqsesame.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 8/5/2020.
 */
class VegaSesamePpqLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_sesame_ppq_lots_list
    private val vm: VegaSesamePpqViewModel by viewModel()
    private lateinit var binding: FragmentVegaSesamePpqLotsListBinding
    private var callBack: CallBack? = null
    private var sortList = mutableListOf<VegaCocoaDispatchLots>()
    private var mSearchList = mutableListOf<VegaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<VegaPpqWarehouseModel?>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var ppqList = mutableListOf<VegaCocoaDispatchLots>()
    private var storage = mutableListOf<StorageLoc>()
    private lateinit var ppqLotList: VegaSesamePpqInspectionLots
    private var plantId: String? = ""
    private var materialIdList = arrayListOf<String>()
    private var currentKey = getCurrentKey()


    interface CallBack {
        fun replaceQualityFragment(
            paramsFrag: String,
            item: VegaSesamePpqInspectionLots
        )
    }

    companion object {
        fun newInstance() = VegaSesamePpqLotListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search by LOT ID"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaSesamePpqLotsListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ppqsesame/ui/ppq/VegaSesamePpqLotListFragment")
            .title("PPQ").with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
            if (getCurrentKey().split("_")[2].contains("SESA")) {
                materialIdList.add("000000100000005393")
                materialIdList.add("000000100000005394")
                materialIdList.add("000000100000005395")
                materialIdList.add("000000100000005408")
                materialIdList.add("000000100000005409")
            } else if (getCurrentKey().split("_")[2].contains("CASH")) {
//                materialIdList.add("000000100000042506")
//                materialIdList.add("000000100000042505")
                materialIdList.addAll(materialList.map { "000000".plus(it.materialCode) })
            }
            //NIC Coffee
            else if (getCurrentKey().split("_")[1].contains("NI")) {
                materialIdList.addAll(materialList.map { "000000".plus(it.materialCode) })
            }
            vm.getStockList(materialIdList)
        })
        vm.getProducts()

        vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.ivSortDownUp.setOnClickListener {
            if (ppqList.isNotEmpty()) {
                ppqList.let { sortList = it }
                sortList = sortList.asReversed()
                ppqList = sortList
                setUpAdapter(ppqList)
            }
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            ppqList.clear()
                            ppqList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaCocoaDispatchLots> else mutableListOf()
                            setUpAdapter(ppqList)
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_nigeria_sesame_ppq_menu, menu)
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint =
                SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(ppqList)
                        } else {
                            mSearchList.clear()
                            ppqList.forEach { lot ->
                                newText?.let { text ->

                                    if(currentKey.split("_")[1].contains("NI")){
                                        if (lot.batchNumber.contains(text.uppercase(Locale.getDefault()))) {
                                            mSearchList.add(lot)
                                        }
                                    }
                                    else{
                                        if (lot.batchNumber.contains(text)) {
                                            mSearchList.add(lot)
                                        }
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

    private fun setUpAdapter(lotList: MutableList<VegaCocoaDispatchLots>) {
        if (lotList.size > 0) {
            binding.rvInspectiontLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvInspectiontLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvInspectiontLots.setUpAdapter(
            lotList,
            R.layout.item_vega_sesame_ppq_lot_details,
            ItemVegaSesamePpqLotDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvBatchNo.text = it.batchNumber
                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode)) {
                        bindItem.tvMaterialName.text = item.materialName
                    }
                }
                bindItem.tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)

            },
            {
                val item = this
                ppqLotList = VegaSesamePpqInspectionLots()
                ppqLotList.chargeNum = item.batchNumber
                ppqLotList.materialName = item.materialName.toString()
                ppqLotList.weight = item.weight!!
                ppqLotList.materialCode = item.materialCode
                ppqLotList.storageLocationCode = item.storageLocationCode.toString()
                ppqLotList.unitOfMeasure = item.unitOfMeasure.toString()
                ppqLotList.plantId = plantId!!
                callBack?.replaceQualityFragment(PARAMS_FRAG, ppqLotList)
            })
    }
}
