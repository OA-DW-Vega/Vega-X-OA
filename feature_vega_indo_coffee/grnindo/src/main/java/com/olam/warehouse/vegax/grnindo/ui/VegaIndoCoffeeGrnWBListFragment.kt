package com.olam.warehouse.vegax.grnindo.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnindo.R
import com.olam.warehouse.vegax.grnindo.databinding.FragmentIndoCoffeeGrnWeighbridgeListBinding
import com.olam.warehouse.vegax.grnindo.utils.GRN_FRAG
import com.olam.warehouse.vegax.grnindo.utils.GRN_OFFLINE_FRAG
import com.olam.warehouse.vegax.grnindo.utils.PROCURE
import com.olam.warehouse.vegax.grnindo.utils.getColor
import kotlinx.android.synthetic.main.item_indo_coffee_weighbridge_grn.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeGrnWBListFragment : BaseFragment() {

    private val vm: VegaIndoCoffeeGrnViewModel by viewModel()
    private var weighBridgeList = mutableListOf<VegaGrnWeighBridgeId>()
    private val mSearchList = mutableListOf<VegaGrnWeighBridgeId>()
    private lateinit var binding: FragmentIndoCoffeeGrnWeighbridgeListBinding
    private var callBack: CallBack? = null
    override val layoutResourceId = R.layout.fragment_indo_coffee_grn_weighbridge_list
    private var offlineDataList = mutableListOf<VegaGrnWeighBridgeId>()

    interface CallBack {
        fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffeeGrnWeighbridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnecuador/ui/weighbridge/VegaEcuadorGrnWBListFragment").title("Ecuador GRN")
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
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_wb_item)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(weighBridgeList)
                        } else {
                            mSearchList.clear()
                            weighBridgeList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId?.contains(text)!!) {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    private fun initUI() {
        if (AppUtils.isOnline()) {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
            vm.getWeighBridgeList()
        } else {
            vm.weighBridgeLocal.observe(viewLifecycleOwner, Observer { updateUIWithLocalData(it) })
            vm.getWeighBridgeDetail()
        }
        binding.ivSortDownUp.setOnClickListener {
            val data1 = weighBridgeList.filter { it.wbTempId.contains("TMP") }
            val data2 = weighBridgeList.filter { !it.wbTempId.contains("TMP") }
            weighBridgeList.clear()
            weighBridgeList.addAll(data2.asReversed())
            weighBridgeList.addAll(data1)
            setUpAdapter(weighBridgeList)
        }
        /*vm.weighBridgeOfflineCount.observe(this, Observer { enableOfflineBar(it) })
        vm.getOfflineWeighBridgeDetailCount()*/
        binding.llQualityOffline.setOnClickListener {
            callBack?.replaceFragment(
                GRN_OFFLINE_FRAG,
                VegaGrnWeighBridgeId()
            )
        }
    }

    private fun enableOfflineBar(items: List<VegaGrnWeighBridgeId>) {
        if (items.isNotEmpty()) {
            binding.llQualityOffline.visible()
            offlineDataList = items as MutableList<VegaGrnWeighBridgeId>
        } else binding.llQualityOffline.gone()
    }

    private fun updateUIWithLocalData(data: List<VegaGrnWeighBridgeId>?) {
        val weighBridge =
            data?.filter {
                (!it.qcStatus.isNullOrEmpty() && it.grnNumber.isNullOrEmpty()) || it.weighBridgeId?.contains(
                    "TMP"
                ) == true
            }
                ?.filter { value -> value.weighBridgeType == PROCURE }
        if (weighBridge?.size ?: 0 > 0) {
            weighBridgeList = weighBridge as MutableList<VegaGrnWeighBridgeId>
            setUpAdapter(weighBridgeList)
            binding.tvNoData.gone()
            binding.rvWeighBridgeId.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvWeighBridgeId.gone()
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>) {
        data.let { it ->
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge =
                            it.data?.data?.filter { it.qcStatus.equals("X") && it.grnNumber.isNullOrEmpty() }
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

    private fun setUpAdapter(data: List<VegaGrnWeighBridgeId>?) {
        val weighBridgeList1 = data as MutableList<VegaGrnWeighBridgeId>
        var count = 0
        binding.rvWeighBridgeId.setUp(
            weighBridgeList1.asReversed(),
            R.layout.item_indo_coffee_weighbridge_grn,
            { it, pos ->
                it.unitPrice = it.unitPrice.toString().replace("\\s".toRegex(), "")
                tvMaterialValue.text = it.materialName
                //if (it.purchaseDocNum.isNullOrEmpty()) getString(R.string.spot_purchase) else getString(R.string.fixed_purchase)
                tvSupplierName.text = it.supplierName
                tvWeight.text = it.netWeight.plus(it.unitsOfMeasure)
                tvWeighBridgeId.text = it.weighBridgeId
                val times = it.erdat?.split('(', ')')
                tvDate.text = times?.get(1).let { it1 ->
                    it1.let { it2 ->
                        it2?.let { it3 ->
                            DateUtils.getUTCDateTime(
                                it3,
                                App.getAppContext()
                            )
                        }
                    }
                }

                count++
            },
            {
                val item = this
                callBack?.replaceFragment(GRN_FRAG, item)
            })
    }
}
