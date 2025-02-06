package com.olam.warehouse.vegax.lotqualitynigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.lotqualitynigeria.R
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityInspectionLots
import com.olam.warehouse.vegax.lotqualitynigeria.databinding.FragmentVegaCocoaLotQualityLotListBinding
import com.olam.warehouse.vegax.lotqualitynigeria.databinding.ItemVegaCocoaLotQualityLotDetailsBinding
import com.olam.warehouse.vegax.lotqualitynigeria.utils.PARAMS_FRAG
import com.olam.warehouse.vegax.lotqualitynigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Muskan Jain on 20/09/2021.
 */
class VegaCocoaLotQualityLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cocoa_lot_quality_lot_list
    private val vm: VegaCocoaLotQualityViewModel by viewModel()
    private lateinit var binding: FragmentVegaCocoaLotQualityLotListBinding
    private var callBack: CallBack? = null
    private var sortList = mutableListOf<VegaCocoaDispatchLots>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var lotQualityList = mutableListOf<VegaCocoaDispatchLots>()
    private lateinit var LotList: VegaCocoaLotQualityInspectionLots
    private var plantId: String? = ""
    private var materialIdList = arrayListOf<String>()
    private var currentKey = getCurrentKey()
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""

    //private var mAdapter = VegaCocoaLotQualityLotListAdapter { moveBagdetail(it) }
    private var selectedPlantId = ""
    private var plantList = mutableListOf<Plant>()
    private val mSearchList: MutableList<VegaCocoaDispatchLots> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var offlineDataList = mutableListOf<VegaQualityWBDetails>()

    interface CallBack {

        fun replaceQualityFragment(
            paramsFrag: String,
            item: VegaCocoaLotQualityInspectionLots
        )
    }

    companion object {
        fun newInstance() = VegaCocoaLotQualityLotListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search by LOT ID"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCocoaLotQualityLotListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("lotqualitynigeria/ui/lotquality/VegaCocoaLotQualityLotListFragment")
            .title("Lot Quality").with(tracker)
    }

    private fun initUI() {
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>

            if (getCurrentKey().split("_")[1].contains("NG") && getCurrentKey().split("_")[2].contains(
                    "COCO"
                )
            ) {
                materialIdList.addAll(materialList.map { "000000".plus(it.materialCode) })
            }
            vm.getStockList(materialIdList)
        })

        vm.getProducts()

        vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.ivSortDownUp.setOnClickListener {
            if (lotQualityList.isNotEmpty()) {
                lotQualityList.let { sortList = it }
                sortList = sortList.asReversed()
                lotQualityList = sortList
                setUpAdapter(lotQualityList)
            }
        }

    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            lotQualityList.clear()
                            lotQualityList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaCocoaDispatchLots> else mutableListOf()
                            setUpAdapter(lotQualityList)
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
        activity?.menuInflater?.inflate(R.menu.search_vega_nigeria_cocoa_lot_quality_menu, menu)
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
                            setUpAdapter(lotQualityList)
                        } else {
                            mSearchList.clear()
                            lotQualityList.forEach { lot ->
                                newText?.let { text ->
                                    if (lot.batchNumber.contains(text) == true) {
                                        mSearchList.add(lot)
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
            R.layout.item_vega_cocoa_lot_quality_lot_details,
            ItemVegaCocoaLotQualityLotDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvBatchNo.text = it.batchNumber
                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode)) {
                        bindItem.tvMaterialName.text = item.materialName
                    }
                }
                bindItem.tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                bindItem.flItem.setOnClickListener { view ->
                    val item = it
                    LotList = VegaCocoaLotQualityInspectionLots()
                    LotList.chargeNum = item.batchNumber
                    LotList.materialName = bindItem.tvMaterialName.text.toString()
                    LotList.weight = item.weight!!
                    LotList.materialCode = item.materialCode
                    LotList.plantId = plantId!!
                    callBack?.replaceQualityFragment(PARAMS_FRAG, LotList)
                }

            },
            {

            })
    }
}

