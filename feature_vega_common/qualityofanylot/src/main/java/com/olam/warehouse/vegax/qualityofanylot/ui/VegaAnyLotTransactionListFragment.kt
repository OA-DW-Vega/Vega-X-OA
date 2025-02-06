package com.olam.warehouse.vegax.qualityofanylot.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityofanylot.R
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListData
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostRequest
import com.olam.warehouse.vegax.qualityofanylot.databinding.FragmentVegaAnyLotQualityLotListBinding
import com.olam.warehouse.vegax.qualityofanylot.databinding.ItemVegaAnyLotQualityLotDetailsBinding
import com.olam.warehouse.vegax.qualityofanylot.utils.CREATE_QUALITY_LOT
import com.olam.warehouse.vegax.qualityofanylot.utils.TYPE_POSITION
import com.olam.warehouse.vegax.qualityofanylot.utils.TYPE_SELECT
import com.olam.warehouse.vegax.qualityofanylot.utils.VIEW_TRANSACTION_LIST
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaAnyLotTransactionListFragment : BaseFragment() {

    private val vm: VegaAnyLotQualityViewModel by viewModel()
    private lateinit var binding: FragmentVegaAnyLotQualityLotListBinding
    private var anyLotQualityList = ArrayList<VegaAnyLotListData>()
    private lateinit var lotListReq: VegaAnyLotQualityPostRequest
    private var callBack: CallBack? = null
    var typeSelect=""
    private val mQualitySearchList: MutableList<VegaAnyLotListData> = mutableListOf()
    private var itemPosition=-1
    private var lotItemPosition=-1


    override val layoutResourceId = R.layout.fragment_vega_any_lot_quality_lot_list


    interface CallBack {
        fun replaceQualityParamsFragment(
            id: String,
            item: VegaAnyLotQualityPostRequest,
            type: String)

    }

    companion object {
        fun newInstance(type:String,list:List<VegaAnyLotListData>,pos:Int) = VegaAnyLotTransactionListFragment().putArgs {
            putString(TYPE_SELECT,type)
            putInt(TYPE_POSITION,pos)
            putParcelableArrayList(VIEW_TRANSACTION_LIST,list as ArrayList<VegaAnyLotListData>)
        }
        const val SEARCH_HINT_TEXT = "Search by Transaction Number"

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    fun initExtra(){
        typeSelect= arguments?.getString(TYPE_SELECT,"").toString()
        lotItemPosition= arguments?.getInt(TYPE_POSITION)?:-1
        anyLotQualityList= arguments?.getParcelableArrayList<VegaAnyLotListData>(VIEW_TRANSACTION_LIST) as ArrayList<VegaAnyLotListData>

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaAnyLotQualityLotListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clScan.gone()
        binding.ivSortDownUp.gone()
        binding.ivScan.gone()
        initExtra()
        initUi()
    }

    private fun initUi() {
        setUpQualityAdapter(anyLotQualityList)

        vm.deleteTransaction.observe(viewLifecycleOwner, Observer {
            updateList(it)
        })

    }

    private fun updateList(response: Resource<GenericReqAndResp<List<VegaAnyLotListData>>>?) {
        response.let {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    try{
                        when (it.data?.success) {
                            true -> {
                              // setUpQualityAdapter(anyLotQualityList)
                                anyLotQualityList.removeAt(itemPosition)
                                binding.rvInspectiontLots.adapter?.notifyItemRemoved(itemPosition)

                                it.data?.message?.let { it1 ->
                                    MaterialDialog(requireContext()).show {
                                    message(text = it1)
                                    UIUtils.getMetirialCustomView(
                                        this,
                                        getString(com.olam.warehouse.presentation.R.string.close),
                                        "",
                                        {
                                            if(anyLotQualityList.isEmpty()){
                                             vm.lotPosition=lotItemPosition
                                             //requireActivity().onBackPressed()
                                            }
                                            dismiss() },
                                        { })
                                }
                                }

                            }
                           else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                        hideLoading()
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
                else -> {}
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.search_vega_any_lot_quality_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            activity?.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
                ?.let { searchView.setBackgroundColor(it) }
            searchView.queryHint =
                SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpQualityAdapter(anyLotQualityList)
                        } else {
                            mQualitySearchList.clear()
                            anyLotQualityList.forEach { lot ->
                                newText?.let { text ->
                                    if (lot.transactionNumber?.contains(text) == true) {
                                        mQualitySearchList.add(lot)
                                    }
                                }
                            }
                            setUpQualityAdapter(mQualitySearchList)
                        }

                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }


    private fun setUpQualityAdapter(lotQualitytList: MutableList<VegaAnyLotListData>) {
        if (lotQualitytList.size > 0) {
            binding.rvInspectiontLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvInspectiontLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvInspectiontLots.setUpAdapter(
            lotQualitytList,
            R.layout.item_vega_any_lot_quality_lot_details,
            ItemVegaAnyLotQualityLotDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.ivDelete.visible()
                bindItem.tvBatchLabel.text= getString(R.string.trans_no)
                bindItem.tvBatchNo.text = it.transactionNumber
                bindItem.tvMaterialName.text = it.materialDetail.materialName
                bindItem.tvWeightLabel.text= getString(R.string.st_location)
                bindItem.tvWeight.text = it.storageLocationDetail?.storageLocationCode
                bindItem.tvDate.text= getString(R.string.date)
                bindItem.tvInspectionLotNo.text= DateUtils.getDate(it.createdAt.toLong(),"MM/dd/yyyy")
                 bindItem.ivDelete.setOnClickListener {
                     showItemDeleteDialog(pos)
                 }
                bindItem.flItem.setOnClickListener { view ->

                    lotListReq = VegaAnyLotQualityPostRequest()
                    lotListReq.supplierCode = it.vendorDetails?.vendorCode
                    lotListReq.supplierName = it.vendorDetails?.vendorName
                    lotListReq.materialName = it.materialDetail.materialName
                    lotListReq.materialCode = it.materialDetail.materialCode
                    lotListReq.transactionNumber = it.transactionNumber
                    lotListReq.batchNumber=it.sapLotId
                    lotListReq.werks = it.plant.plantId
                    lotListReq.storageLocationCode= it.storageLocationDetail?.storageLocationCode
                    lotListReq.secretKey= it.secretId
                    callBack?.replaceQualityParamsFragment(it.id.toString(), lotListReq,typeSelect)
                }

            },
            {

            })
    }

    private fun showItemDeleteDialog(position:Int) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.login.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    itemPosition=position
                    vm.deleteTransactionListItem(anyLotQualityList[position].id.toString())
                },
                { dismiss() })
        }
    }


}
