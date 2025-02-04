package com.olam.warehouse.vegax.bcapprovenigeria.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bcapprovenigeria.R
import com.olam.warehouse.vegax.bcapprovenigeria.databinding.FragmentVegaBcApproveNigeriaWeighbridgeListBinding
import com.olam.warehouse.vegax.bcapprovenigeria.ui.OnFragmentBcApproveNigeriaInteractionListener
import com.olam.warehouse.vegax.bcapprovenigeria.ui.VegaBcApproveNigeriaViewModel
import com.olam.warehouse.vegax.bcapprovenigeria.ui.quality.VegaBcApproveNigeriaFragment
import com.olam.warehouse.vegax.bcapprovenigeria.utils.APPROVE_DATA
import com.olam.warehouse.vegax.bcapprovenigeria.utils.getColor
import kotlinx.android.synthetic.main.item_vega_weighbridge_bc_approve_nigeria.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
class VegaBcApproveNigeriaWeighbridgeListFragment : BaseFragment() {

    private val vm: VegaBcApproveNigeriaViewModel by viewModel()
    private var weighBridgeList = mutableListOf<VegaQualityApproveCameroonWeighBridge>()
    private val mSearchList = mutableListOf<VegaQualityApproveCameroonWeighBridge>()
    private var mListener: OnFragmentBcApproveNigeriaInteractionListener? = null
    private var approveWeighBridgeId = VegaQualityApproveCameroonWeighBridge()
    private lateinit var binding: FragmentVegaBcApproveNigeriaWeighbridgeListBinding

    override val layoutResourceId = R.layout.fragment_vega_bc_approve_nigeria_weighbridge_list

    companion object {
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaBcApproveNigeriaWeighbridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("bcapproveNigeria/ui/weighbridge/VegaBcApproveNigeriaWeighbridgeListFragment").title("Vega_Nigeria/Approve")
            .with(tracker)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        //search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
        //searchView = search?.actionView as SearchView?
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = SEARCH_HINT_TEXT
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
                                    if (qtyWb.wbid?.contains(text)!!) {
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
        if (context is OnFragmentBcApproveNigeriaInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initUI() {
        vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getWeighBridgeList()
        binding.ivSortDownUp.setOnClickListener {
            val data = weighBridgeList
            weighBridgeList = data.asReversed()
            setUpAdapter(weighBridgeList)
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge =
                        it.data?.data
                    /*?.filter { it.qcStatus.equals("X") }
                    ?.filter { it.netWeight.isNotEmpty() }
                    ?.filter { !it.netWeight.equals("0.000") }*/
                    if (weighBridge?.size!! > 0) {
                        weighBridgeList = weighBridge as MutableList<VegaQualityApproveCameroonWeighBridge>
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

    private fun setUpAdapter(data: List<VegaQualityApproveCameroonWeighBridge>?) {
        val weighBridgeList1 = data?.sortedByDescending {
            it.wbid
            /* it.erdat?.split('(', ')')?.get(1)?.let { it1 ->
                 DateUtils.getUTCDateTime(
                     it1,
                     App.getAppContext()
                 )
             }*/
        } as MutableList<VegaQualityApproveCameroonWeighBridge>
        var count = 0
        binding.rvWeighBridgeId.setUp(weighBridgeList1, R.layout.item_vega_weighbridge_bc_approve_nigeria, { it, pos ->
            it.unitPrice = it.unitPrice.toString().replace("\\s".toRegex(), "")
            tvMaterialValue.text = it.materialName
//            tvTruckNo.text = it.vehicleNumber
            tvSupplierName.text = it.supplierName
            tvWeight.text = it.grnQty?.replace(" ", "").plus(it.meins)
            tvWeighBridgeId.text = it.wbid

//            val times = it.erdat?.split('(', ')')
            tvDate.text = it.year
            /* times?.get(1).let { it1 ->
             it1?.let { it2 ->
                 DateUtils.getUTCDateTime(
                     it2,
                     App.getAppContext()
                 )
             }
         }*/

            count++
        }, {

            /* val fragment = VegaQualityApproveCameroonDetailsFragment()
             val args = Bundle()
             approveWeighBridgeId = this
             args.putParcelable(APPROVE_DATA, approveWeighBridgeId)
             fragment.arguments = args
             mListener?.onFragmentInteraction(fragment)*/

            val fragment = VegaBcApproveNigeriaFragment()
            val args = Bundle()
            approveWeighBridgeId = this
            args.putParcelable(APPROVE_DATA, approveWeighBridgeId)
            fragment.arguments = args
            mListener?.onFragmentInteraction(fragment)

        })
    }
}
