package com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.sales

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentVegaCoffeeTruckoutMtntWblistBinding
import com.olam.warehouse.vegax.weighmentcoffee.databinding.ItemVegaCoffeeTruckoutMtntWbListBinding
import com.olam.warehouse.vegax.weighmentcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCoffeeSalesTruckOutWBListFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var mtntData = VegaMtnt()
    private var mWBList = mutableListOf<VegaMtnt>()
    private val mSearchList = mutableListOf<VegaMtnt>()

    interface CallBack {
        fun replaceSalesFragment(
            flag: String,
            moveFrag: String,
            mtntData: VegaMtnt,
            isThirdPartySale: Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeTruckoutMtntWblistBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_truckout_mtnt_wblist
    private var isThirdPartySale: Boolean = false
    private var isLocalSale: Boolean = false

    companion object {
        fun newInstance(mtntData: VegaMtnt, isThirdPartySale: Boolean) =
            VegaCoffeeSalesTruckOutWBListFragment().putArgs {
                putParcelable(MTNT_DATA, mtntData)
                putBoolean(THIRD_PARTY_SALES, isThirdPartySale)
            }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCoffeeTruckoutMtntWblistBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView = search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint =
                getString(com.olam.warehouse.presentation.R.string.search_by_lot)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(mWBList)
                        } else {
                            mSearchList.clear()
                            mWBList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId.contains(text)) {
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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckout/mtnt/VegaTruckOutMtntWBListFragment").title("IVC/Coffee/Weighment/Sales Truck Out WBList")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        mtntData = arguments?.getParcelable(MTNT_DATA)!!
        isThirdPartySale = arguments?.getBoolean(THIRD_PARTY_SALES) ?: false
        vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        vm.getWeighBridgeDetail(!isThirdPartySale,isThirdPartySale)
       // vm.getWeighBridgeDetail(!isThirdPartySale)
        binding.ivSortDownUp.setOnClickListener {
            if (mWBList.isNotEmpty()) {
                val data = mWBList
                mWBList = data.asReversed()
                setUpAdapter(mWBList)
            }
        }
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaMtnt>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data.let { it1 ->
                        var list = ArrayList<VegaMtnt>()
                        val completeList = it1?.filter { it.direction.equals(DIRECTIONOUT) }!!
                            .filter { it.grossWeight == null || it.grossWeight == "0" || it.grossWeight == "0.000" }
                        if (!isThirdPartySale) {
                            list = completeList.filter { !it.batchPicking.isNullOrEmpty() } as ArrayList<VegaMtnt>
                        } else {
                            list = completeList as ArrayList<VegaMtnt>
                        }
                        mWBList = list
                        setUpAdapter(list)
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun setUpAdapter(data: List<VegaMtnt>?) {
        val receiving = data as MutableList<VegaMtnt>
        binding.rvWeighbridge.setUpAdapter(
            receiving.asReversed(),
            R.layout.item_vega_coffee_truckout_mtnt_wb_list,
            ItemVegaCoffeeTruckoutMtntWbListBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvTruckNo.text =
                    if (it.vehicleNumber.isNullOrEmpty()) "-" else it.vehicleNumber
                bindItem.tvWeighBridgeId.text = it.weighBridgeId
                bindItem.tvDelivery.text = it.delivery
                bindItem.tvDeliveryLable.visibility =
                    if (isThirdPartySale) View.GONE else View.VISIBLE
                bindItem.tvDelivery.visibility = if (isThirdPartySale) View.GONE else View.VISIBLE
                bindItem.tvWarehouseLoc.text = it.storageLocationCode
                bindItem.tvMaterialValue.text = it.materialName
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    bindItem.tvDate.text = times?.get(1)?.let { it1 ->
                        DateUtils.getUTCDateTime(
                            it1,
                            App.getAppContext()
                        )
                    }
                }
            },
            {
                val item = this
                item.weighBridgeType = mtntData.weighBridgeType
                item.truckDirection = mtntData.truckDirection
                callBack?.replaceSalesFragment(
                    "",
                    TRUCK_OUT_SALES_ADD_WEIGHT,
                    item,
                    isThirdPartySale
                )

            })
    }
}
