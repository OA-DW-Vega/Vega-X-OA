package com.olam.warehouse.vegax.secretidcommon.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants.PROCURE
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.databinding.FragmentWbListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaCommonSecretIdOffloadingFragment:BaseFragment() {

    override val layoutResourceId= R.layout.fragment_wb_list
    private lateinit var binding: FragmentWbListBinding
    private var mAdapter = VegaCommonSecretWbListAdapter { moveBagdetail(it) }
    private lateinit var mListener: OnWeighBridgeListener
    private val vm: VegaCommonSecretIdViewModel by viewModel()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var sortList = mutableListOf<VegaQualityWBDetails>()



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
    }

    companion object {
        fun newInstance() = VegaCommonSecretIdOffloadingFragment().putArgs {
            //  putParcelableArrayList(UIUtils.LOT_DETAIL, lotList)
        }
    }



    private fun moveBagdetail(wbDetails: VegaQualityWBDetails?) {
        mListener.onWeighBridgeClick(wbDetails)

    }

    interface OnWeighBridgeListener {
        fun onWeighBridgeClick(
            wbDetails: VegaQualityWBDetails?
        )
        fun setQualityWBList(it: List<VegaQualityWBDetails>?)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding= FragmentWbListBinding.inflate(layoutInflater)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        clickEvent()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_common_secretid, menu)
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(requireContext().getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_wb_item)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            mAdapter.addItems(mQualityWBList)
                        } else {
                            mSearchList.clear()
                            mQualityWBList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            mAdapter.addItems(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }


    private fun clickEvent(){
        binding.ivSortDownUp.setOnClickListener {
            if (mQualityWBList.isNotEmpty()) {
                mQualityWBList.let { sortList = it }
                sortList = sortList.asReversed()
                mQualityWBList = sortList
                mAdapter.addItems(mQualityWBList)
            }

        }


    }

    private fun initUI() {
        binding.rvWb.layoutManager = LinearLayoutManager(this.context)
        binding.rvWb.adapter = mAdapter

        vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer {
            updateUIWithOnlineData(it)
        })
        vm.getWeighBridgeDetailOnline()
    }


    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()

                var weighTypedata: List<VegaQualityWBDetails>? = null
                val data = response.data?.data
                    ?.filter { value -> !value.qcStatus!!.contains("X") }

                weighTypedata = data?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                    ?.filter { value -> value.weighBridgeType == PROCURE }
                    ?.filter { value -> !value.netWeight.equals("0.0") }
                    ?.filter { value -> value.challan.isNullOrEmpty() }


                weighTypedata?.let { it1 ->
                    var data1 = listOf<VegaQualityWBDetails>()
                    data1 =it1
                    if (data1.isNotEmpty()) {
                        mQualityWBList= data1.toMutableList()
                        mListener.setQualityWBList(data1)
                        mAdapter.addItems(data1)
                        binding.tvNoData.gone()
                        binding.rvWb.visible()
                    } else {
                        binding.tvNoData.visible()
                        binding.rvWb.gone()
                    }

                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), "${response.error}")
            }
        }
    }



}
