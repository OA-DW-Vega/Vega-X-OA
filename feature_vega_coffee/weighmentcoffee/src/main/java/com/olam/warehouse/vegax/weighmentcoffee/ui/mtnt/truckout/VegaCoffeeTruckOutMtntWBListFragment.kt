package com.olam.warehouse.vegax.weighmentcoffee.ui.mtnt.truckout

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
import com.olam.warehouse.vegax.weighmentcoffee.utils.DIRECTIONOUT
import com.olam.warehouse.vegax.weighmentcoffee.utils.MTNT_DATA
import com.olam.warehouse.vegax.weighmentcoffee.utils.TRUCKOUT_MTNT_ADD_WEIGHT_FRAG
import com.olam.warehouse.vegax.weighmentcoffee.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 9/2/2020.
 */
class VegaCoffeeTruckOutMtntWBListFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var mtntData = VegaMtnt()
    private var mWBList = mutableListOf<VegaMtnt>()
    private val mSearchList = mutableListOf<VegaMtnt>()

    interface CallBack {
        fun replaceMtntFragment(
            flag: String,
            moveFrag: String,
            mtntData: VegaMtnt
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeTruckoutMtntWblistBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_truckout_mtnt_wblist

    companion object {
        fun newInstance(mtntData: VegaMtnt) = VegaCoffeeTruckOutMtntWBListFragment().putArgs {
            putParcelable(MTNT_DATA, mtntData)
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
        TrackHelper.track().screen("receiving/ui/truckout/mtnt/VegaTruckOutMtntWBListFragment").title("IVC/Coffee/Weighment/Truck Out MTNT WBList")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        mtntData = arguments?.getParcelable(MTNT_DATA)!!
        vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        vm.getWeighBridgeDetail(false,false)
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
                        val wbList = it1?.filter { it.direction.equals(DIRECTIONOUT) }
                            ?.filter { !it.batchPicking.isNullOrEmpty() }
                        mWBList = wbList as MutableList<VegaMtnt>
                        setUpAdapter(wbList)
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
                callBack?.replaceMtntFragment("", TRUCKOUT_MTNT_ADD_WEIGHT_FRAG, item)

            })
    }
}
