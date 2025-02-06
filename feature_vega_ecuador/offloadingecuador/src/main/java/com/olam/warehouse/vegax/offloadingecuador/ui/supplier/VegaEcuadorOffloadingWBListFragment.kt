package com.olam.warehouse.vegax.offloadingecuador.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingecuador.R
import com.olam.warehouse.vegax.offloadingecuador.databinding.FragmentVegaEcuadorOffloadingWeighBridgeListBinding
import com.olam.warehouse.vegax.offloadingecuador.databinding.ItemVegaEcuadorOffloadWeighBridgeDetailsBinding
import com.olam.warehouse.vegax.offloadingecuador.ui.VegaEcuadorOffloadingViewModel
import com.olam.warehouse.vegax.offloadingecuador.utils.CONSIGNMENT
import com.olam.warehouse.vegax.offloadingecuador.utils.PROCURE
import com.olam.warehouse.vegax.offloadingecuador.utils.WEIGHMENT_TYPE
import com.olam.warehouse.vegax.offloadingecuador.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaEcuadorOffloadingWBListFragment : BaseFragment() {

    private val vm: VegaEcuadorOffloadingViewModel by viewModel()
    private lateinit var binding: FragmentVegaEcuadorOffloadingWeighBridgeListBinding
    override val layoutResourceId = R.layout.fragment_vega_ecuador_offloading_weigh_bridge_list
    private lateinit var mListener: VegaEccuadorOffloadingListener
    private var weighmentType:String = ""
    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mWBList: MutableList<VegaQualityWBDetails> = mutableListOf()



    companion object {
        fun newInstance(weighmentType: String) = VegaEcuadorOffloadingWBListFragment().putArgs {
            putString(WEIGHMENT_TYPE, weighmentType)
        }
    }

    /*interface CallBack {
        fun replaceFragment(
            moveFrag: String, weighBridgeData: VegaQualityWBDetails
        )

    }*/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as VegaEccuadorOffloadingListener
    }


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaEcuadorOffloadingWeighBridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExtra()
    }

    private fun initExtra(){
        weighmentType = arguments?.getString(WEIGHMENT_TYPE).toString()
    }

    private fun initUI() {
        if (AppUtils.isOnline()) {
            vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
            vm.getWeighBridgeDetailOnline()
        }
        binding.btnAddNewTruck.setOnClickListener {
            mListener.replaceFragment(CONSIGNMENT, VegaQualityWBDetails())
        }
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val data = response.data?.data
                var weighTypedata: List<VegaQualityWBDetails>? = null
                weighTypedata = data?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                            ?.filter { value -> value.weighBridgeType == PROCURE }
                    ?.filter { wbType -> wbType.weighMethod == "WB" }
                    ?.filter { grn -> grn.grnNumber.isNullOrEmpty() }
                if(weighTypedata?.isNotEmpty()==true){
                    mWBList.addAll(weighTypedata)
                    setUpAdapter(mWBList)
                } else {
                    binding.tvNoData.visible()
                    binding.rvWeighbridge.gone()
                }

                /*weighTypedata?.let { it1 ->
                    var data1 = listOf<VegaQualityWBDetails>()
                    var filteredList = arrayListOf<VegaQualityWBDetails>()
                    data1 = if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                        it.materialCode.equals(copiedMaterial)
                    } else it1
                    if (data1.isNotEmpty()) {
                        data1.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }.contains(wb.weighBridgeId)) {
                                filteredList.add(wb)
                            }
                        }
                        mListener.setQualityWBList(filteredList)
                        mQualityWBList.clear()
                        mQualityWBList = filteredList
                        mAdapter.addItems(filteredList)
                        binding.tvNoData.gone()
                        binding.rvWeighbridge.visible()
                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighbridge.gone()
                    }

                }*/
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {

                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_ecuador_offload_menu, menu)
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_wb_item)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {

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

    private fun setUpAdapter(data: List<VegaQualityWBDetails>) {
        binding.rvWeighbridge.setUpAdapter(data as MutableList<VegaQualityWBDetails>,
            R.layout.item_vega_ecuador_offload_weigh_bridge_details,
            ItemVegaEcuadorOffloadWeighBridgeDetailsBinding::inflate,
            {it, pos, bindingItem ->
                bindingItem.tvWeighBridgeId.setText(it.weighBridgeId)
                bindingItem.tvProcureType.setText(it.materialName)
                bindingItem.tvSupplierName.setText(it.supplierName)
                bindingItem.tvWeight.setText(it.netWeight)
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    bindingItem.tvDate.text =
                        times?.get(1)?.let { it1 -> DateUtils.getUTCDateTime(it1, App.getAppContext()) }
                }

            },{
                mListener.replaceFragment(weighmentType, this as VegaQualityWBDetails)
            })

    }

}
