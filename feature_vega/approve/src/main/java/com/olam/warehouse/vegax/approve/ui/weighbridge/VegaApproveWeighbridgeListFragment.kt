package com.olam.warehouse.vegax.approve.ui.weighbridge

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
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.approve.R
import com.olam.warehouse.vegax.approve.data.domain.model.VegaApproveWeighBridgeId
import com.olam.warehouse.vegax.approve.databinding.FragmentVegaApproveWeighbridgeListBinding
import com.olam.warehouse.vegax.approve.databinding.ItemVegaWeighbridgeApproveBinding
import com.olam.warehouse.vegax.approve.ui.OnFragmentInteractionListener
import com.olam.warehouse.vegax.approve.ui.VegaApproveViewModel
import com.olam.warehouse.vegax.approve.ui.details.VegaApproveDetailsFragment
import com.olam.warehouse.vegax.approve.utils.APPROVE_DATA
import com.olam.warehouse.vegax.approve.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
class VegaApproveWeighbridgeListFragment : BaseFragment() {

    private val vm: VegaApproveViewModel by viewModel()
    private var weighBridgeList = mutableListOf<VegaApproveWeighBridgeId>()
    private val mSearchList = mutableListOf<VegaApproveWeighBridgeId>()
    private var mListener: OnFragmentInteractionListener? = null
    private var approveWeighBridgeId = VegaApproveWeighBridgeId()
    private lateinit var binding: FragmentVegaApproveWeighbridgeListBinding

    override val layoutResourceId = R.layout.fragment_vega_approve_weighbridge_list

    companion object {
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaApproveWeighbridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("approve/ui/weighbridge/VegaApproveWeighbridgeListFragment").title("Approve")
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
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
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
        if (context is OnFragmentInteractionListener) {
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
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getWeighBridgeList()
        binding.ivSortDownUp.setOnClickListener {
            val data = weighBridgeList
            weighBridgeList = data.asReversed()
            setUpAdapter(weighBridgeList)
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge =
                        it.data?.data?.filter { it.qcStatus.equals("X") }?.filter { it.netWeight.isNotEmpty() }
                            ?.filter { !it.netWeight.equals("0.000") }
                    if (weighBridge?.size!! > 0) {
                        weighBridgeList = weighBridge as MutableList<VegaApproveWeighBridgeId>
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
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }

    private fun setUpAdapter(data: List<VegaApproveWeighBridgeId>?) {
        if(data?.isNotEmpty() == true) {
            val weighBridgeList1 = data.sortedByDescending {
                it.erdat?.split('(', ')')?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(
                        it1,
                        App.getAppContext()
                    )
                }
            } as MutableList<VegaApproveWeighBridgeId>
            var count = 0
            binding.rvWeighBridgeId.setUpAdapter(
                weighBridgeList1,
                R.layout.item_vega_weighbridge_approve,
                ItemVegaWeighbridgeApproveBinding::inflate,
                { it, pos, bnding ->
                    it.unitPrice = it.unitPrice.toString().replace("\\s".toRegex(), "")
                    bnding.tvTruckNo.text = it.vehicleNumber
                    bnding.tvSupplierName.text = it.supplierName
                    bnding.tvWeight.text = it.netWeight.plus(it.unitsOfMeasure)
                    bnding.tvWeighBridgeId.text = it.weighBridgeId
                    val times = it.erdat?.split('(', ')')
                    bnding.tvDate.text = times?.get(1).let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTime(
                                it2,
                                App.getAppContext()
                            )
                        }
                    }

                    count++
                }, {

                    val fragment = VegaApproveDetailsFragment()
                    val args = Bundle()
                    approveWeighBridgeId = this
                    args.putParcelable(APPROVE_DATA, approveWeighBridgeId)
                    fragment.arguments = args
                    mListener?.onFragmentInteraction(fragment)

                })
        }
    }
}
