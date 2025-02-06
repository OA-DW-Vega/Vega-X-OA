package com.olam.warehouse.vegax.ppqindo.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ppqindo.R
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindo.databinding.FragmentIndoCoffePpqLotsListBinding
import com.olam.warehouse.vegax.ppqindo.databinding.ItemIndoCoffeePpqLotDetailsBinding
import com.olam.warehouse.vegax.ppqindo.utils.PARAMS_FRAG
import com.olam.warehouse.vegax.ppqindo.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeePpqLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_indo_coffe_ppq_lots_list
    private val vm: VegaIndoCoffeePpqViewModel by viewModel()
    private lateinit var binding: FragmentIndoCoffePpqLotsListBinding
    private var callBack: CallBack? = null
    private var inspectionLotList = mutableListOf<VegaIndoCoffeePpqInspectionLots>()
    private var sortList = mutableListOf<VegaIndoCoffeePpqInspectionLots>()
    private var mSearchList = mutableListOf<VegaIndoCoffeePpqInspectionLots>()

    interface CallBack {
        fun replaceQualityFragment(
            paramsFrag: String,
            item: VegaIndoCoffeePpqInspectionLots
        )
    }

    companion object {
        fun newInstance() = VegaIndoCoffeePpqLotListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffePpqLotsListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExtra()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ppqcoffee/ui/ppq/VegaCoffeePpqLotListFragment")
            .title("PPQ").with(tracker)
    }

    private fun initExtra() {

    }

    private fun initUI() {
        vm.inspectionLots.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getInspectionLots()
        binding.ivSortDownUp.setOnClickListener {
            if (inspectionLotList.isNotEmpty()) {
                inspectionLotList.let { sortList = it }
                sortList = sortList.asReversed()
                inspectionLotList = sortList
                setUpAdapter(inspectionLotList)
            }
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            inspectionLotList.clear()
                            inspectionLotList.addAll(it.data!!.data)
                            setUpAdapter(inspectionLotList)
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    setUpAdapter(inspectionLotList)
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_indo_coffee_ppq_menu, menu)
        //search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
        //searchView = search?.actionView as SearchView?
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
                            setUpAdapter(inspectionLotList)
                        } else {
                            mSearchList.clear()
                            inspectionLotList.forEach { lot ->
                                newText?.let { text ->
                                    if (lot.chargeNum.contains(text)) {
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

    private fun setUpAdapter(lotList: MutableList<VegaIndoCoffeePpqInspectionLots>) {
        if (lotList.size > 0) {
            binding.rvInspectiontLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvInspectiontLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvInspectiontLots.setUpAdapter(lotList, R.layout.item_indo_coffee_ppq_lot_details,
            ItemIndoCoffeePpqLotDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvInspectionLotNo.text = it.inspectionLotNum
                bindItem.tvBatchNo.text = it.chargeNum
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                val times = it.inspectionDate.split('(', ')')
                bindItem.tvDateTxt.text = times.get(1).let { it1 ->
                    DateUtils.getUTCDateTime(
                        it1,
                        App.getAppContext()
                    )
                }
            }, {
                val item = this
                callBack?.replaceQualityFragment(PARAMS_FRAG, item)
            })
    }
}
