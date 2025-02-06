package com.olam.warehouse.vegax.qualityofanylot.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityofanylot.R
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.*
import com.olam.warehouse.vegax.qualityofanylot.databinding.FragmentVegaAnyLotQualityLotListBinding
import com.olam.warehouse.vegax.qualityofanylot.databinding.ItemVegaAnyLotQualityLotDetailsBinding
import com.olam.warehouse.vegax.qualityofanylot.utils.CREATE_QUALITY_LOT
import com.olam.warehouse.vegax.qualityofanylot.utils.LOT_ID
import com.olam.warehouse.vegax.qualityofanylot.utils.PARAMS_FRAG
import com.olam.warehouse.vegax.qualityofanylot.utils.TYPE_SELECT
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaAnyLotQualityLotListFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_any_lot_quality_lot_list
    private val vm: VegaAnyLotQualityViewModel by viewModel()
    private lateinit var binding: FragmentVegaAnyLotQualityLotListBinding
    private var callBack: CallBack? = null
    private var sortList = mutableListOf<VegaCocoaDispatchLots>()
    private var sortQualitytList = mutableListOf<VegaAnyLotListData>()
    private var lotQualityList = mutableListOf<VegaCocoaDispatchLots>()
    var anyLotQualityList = mutableListOf<VegaAnyLotListData>()
   // private var lotWithTransList = mutableListOf<LotWithTransDetails>()
    private lateinit var LotList: VegaAnyLotQualityPostRequest
    private var batchNo: String = ""
    private lateinit var searchView: SearchView

    //private var mAdapter = VegaCocoaLotQualityLotListAdapter { moveBagdetail(it) }
    private val mSearchList: MutableList<VegaCocoaDispatchLots> = mutableListOf()
    private val mQualitySearchList: MutableList<VegaAnyLotListData> = mutableListOf()
    var typeSelect=""
     var SEARCH_HINT_TEXT = "Search by Secret ID"

    interface CallBack {

        fun replaceQualityFragment(
            id: String,
            item: VegaAnyLotQualityPostRequest,
            type: String)

        fun replaceLotListToTransactionFragment(
            type: String,
            list:List<VegaAnyLotListData>,
            pos:Int
        )

    }

    companion object {
        fun newInstance(type:String) = VegaAnyLotQualityLotListFragment().putArgs {
            putString(TYPE_SELECT,type)
           // putString(LOT_ID,lotId)
        }
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
        binding = FragmentVegaAnyLotQualityLotListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        typeSelect= arguments?.getString(TYPE_SELECT,"").toString()
        //batchNo= arguments?.getString(LOT_ID,"").toString()
    }




    private fun initUI() {

        vm.getAllSavedQualityLotList(getPlantDetails().plantId,batchNo)

        vm.savedQualityLotList.observe(viewLifecycleOwner, Observer {
            updateQualityLot(it)

        })


        binding.ivSortDownUp.setOnClickListener {
            if (lotQualityList.isNotEmpty()) {
                lotQualityList.let { sortList = it }
                sortList = sortList.asReversed()
                lotQualityList = sortList
                setUpAdapter(lotQualityList)
            }

            if(anyLotQualityList.isNotEmpty()){
                anyLotQualityList.let { sortQualitytList = it }
                sortQualitytList = sortQualitytList.asReversed()
                anyLotQualityList = sortQualitytList
                setUpQualityAdapter(anyLotQualityList)
            }
        }

        binding.ivScan.setOnClickListener{
            moveToScan()
        }

    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    batchNo=it
                    vm.getAllSavedQualityLotList(getPlantDetails().plantId,"")

                }
            }
        }
    }

    private fun updateQualityLot(response: Resource<GenericReqAndResp<List<VegaAnyLotListData>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    try{
                    when (it.data?.success) {
                        true -> {
                           anyLotQualityList.clear()
                            anyLotQualityList =
                               if (it.data?.data?.size!! > 0) it.data?.data as MutableList<VegaAnyLotListData> else mutableListOf()

                          anyLotQualityList=  anyLotQualityList.asReversed()
                            if(batchNo.isNotEmpty() && batchNo.contains("SCRID")){
                                anyLotQualityList= anyLotQualityList.filter { it.secretId==batchNo } as MutableList<VegaAnyLotListData>
                            }else if(batchNo.isNotEmpty()){
                                anyLotQualityList= anyLotQualityList.filter { it.sapLotId==batchNo } as MutableList<VegaAnyLotListData>
                            }

                            setUpQualityAdapter(anyLotQualityList)
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
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_any_lot_quality_menu, menu)
        try {
            val search = menu.findItem(R.id.search)
            searchView = search?.actionView as SearchView
            activity?.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
                ?.let { searchView.setBackgroundColor(it) }
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if(typeSelect== CREATE_QUALITY_LOT) {
                            if (newText?.isEmpty() == true) {
                                setUpAdapter(lotQualityList)
                            } else {
                                mSearchList.clear()
                                lotQualityList.forEach { lot ->
                                    newText?.let { text ->
                                        if (lot.secretId?.contains(text) == true) {
                                            mSearchList.add(lot)
                                        }
                                    }
                                }
                                setUpAdapter(mSearchList)
                            }
                        }else{
                            if (newText?.isEmpty() == true) {
                                setUpQualityAdapter(anyLotQualityList)
                            } else {
                                mQualitySearchList.clear()
                                anyLotQualityList.forEach { lot ->
                                    newText?.let { text ->
                                        if (lot.sapLotId?.contains(text) == true) {
                                            mQualitySearchList.add(lot)
                                        }
                                    }
                                }
                                setUpQualityAdapter(mQualitySearchList)
                            }
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun setUpQualityAdapter(lotList: MutableList<VegaAnyLotListData>) {
        val lotGroupList = lotList.groupBy { it.sapLotId }
        val lotIds = mutableListOf<String>()
        lotGroupList.forEach { (key, _) -> lotIds.add(key.toString()) }
        if (lotList.size > 0) {
            binding.rvInspectiontLots.visible()
            binding.tvNoData.gone()
            if(lotList.get(0).secretId?.isEmpty() == true) {
                SEARCH_HINT_TEXT = getString(R.string.search_by_lot_id)
                searchView.queryHint = SEARCH_HINT_TEXT
            }
        } else {
            binding.rvInspectiontLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvInspectiontLots.setUpAdapter(
            lotIds,
            R.layout.item_vega_any_lot_quality_lot_details,
            ItemVegaAnyLotQualityLotDetailsBinding::inflate,
            { it1, pos, bindItem ->
                val it = lotGroupList[it1]
                if(it?.get(0)?.secretId?.isEmpty() == true) {
                    bindItem.tvBatchNo.text = it1
                    bindItem.tvMaterialName.text = it.get(0).materialDetail.materialName
                    bindItem.tvWeightLabel.text = getString(R.string.st_location)
                    bindItem.tvWeight.text = it.get(0).storageLocationDetail?.storageLocationCode
                    bindItem.tvDate.text = getString(R.string.date)
                    bindItem.tvInspectionLotNo.text = it.get(0).createdAt.toLong()
                        .let { it2 -> DateUtils.getDate(it2, "MM/dd/yyyy") }
                }else{
                    bindItem.tvBatchNo.text = it?.get(0)?.secretId
                    bindItem.tvBatchLabel.text = getString(R.string.secret_id)
                    bindItem.tvMaterialName.gone()
                    bindItem.tvMaterial.gone()
                    bindItem.tvWeightLabel.gone()
                    bindItem.tvWeight.gone()
                    bindItem.tvDate.gone()
                    bindItem.tvInspectionLotNo.gone()
                }

                bindItem.flItem.setOnClickListener { view ->
                    it?.let { it2 -> callBack?.replaceLotListToTransactionFragment(typeSelect,it2,pos) }
                }

            },
            {

            })
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
            R.layout.item_vega_any_lot_quality_lot_details,
            ItemVegaAnyLotQualityLotDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvBatchNo.text = it.batchNumber
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                bindItem.tvDate.text= getString(R.string.st_location)
                bindItem.tvInspectionLotNo.text=it.storageLocationCode
                bindItem.flItem.setOnClickListener { view ->
                    val item = it
                    LotList = VegaAnyLotQualityPostRequest()
                    LotList.supplierCode = item.vendor
                    LotList.supplierName = item.vendorName
                    LotList.materialName = bindItem.tvMaterialName.text.toString()
                    LotList.materialCode = item.materialCode
                    LotList.werks = item.plantId
                    LotList.batchNumber= item.batchNumber
                    LotList.storageLocationCode= item.storageLocationCode
                    LotList.secretKey= item.secretId
                    callBack?.replaceQualityFragment(batchNo, LotList,typeSelect)
                }

            },
            {

            })
    }

    fun updateFragment() {
        if(typeSelect== CREATE_QUALITY_LOT) {
            vm.getAllStockList()
        }else{
            vm.getAllSavedQualityLotList(getPlantDetails().plantId,batchNo)
        }
    }
}
