package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighscale

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceivingMtn
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentVegaGhanaCocoaMtnrWeighScaleListBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGhanaCocoaMtnrWSListFragment : BaseFragment() {

    private var mAdapter = VegaGhanaCocoaMtnrWSListAdapter { moveBagdetail(it) }
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()
    private lateinit var mListener: OnWeighScaleListener
    private lateinit var binding: FragmentVegaGhanaCocoaMtnrWeighScaleListBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_cocoa_mtnr_weigh_scale_list
    private var weightmentType: String? = ""
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private var batchNo: String? = ""
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var allBatchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var allOBDList = mutableListOf<VegaReceivingMtn>()
    private var jsonData = mutableListOf<String>()
    private var storageLocation = ArrayList<String>()


    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var offlineDataList = mutableListOf<VegaQualityWBDetails>()

    interface OnWeighScaleListener {
        fun onWeighScaleClick(
            wbDetails: VegaQualityWBDetails?
        )

        fun setQualityWBList(it: List<VegaQualityWBDetails>?)
//        fun onQualityOfflineClick()
    }

    companion object {
        fun newInstance() = VegaGhanaCocoaMtnrWSListFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighScaleListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaCocoaMtnrWeighScaleListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExtra()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("qualitysesame/ui/weighbridge/VegaNigeriaSesameQualityWBListFragment - $weightmentType")
            .title("Ecuador Quality").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_ghana_cocoa_mtnr_menu, menu)
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
                            mAdapter.addItems(mQualityWBList)
                        } else {
                            mSearchList.clear()
                            mQualityWBList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.vehicleNumber?.contains(text)!! || qtyWb.delivery?.contains(
                                            text
                                        )!!
                                    ) {
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

    private fun initUI() {
        binding.rvWeighbridge.layoutManager = LinearLayoutManager(this.context)
        binding.rvWeighbridge.adapter = mAdapter

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

//        vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
//        vm.getWeighBridgeDataOnline()

        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })


//        binding.llQualityOffline.setOnClickListener { mListener.onQualityOfflineClick() }
        binding.ivSortDownUp.setOnClickListener {
            val data1 = mQualityWBList.filter { it.wbTempId.contains("TMP") }
            val data2 = mQualityWBList.filter { !it.wbTempId.contains("TMP") }
            mQualityWBList.clear()
            mQualityWBList.addAll(data2.asReversed())
            mQualityWBList.addAll(data1)
            mAdapter.addItems(mQualityWBList)
        }

    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains(JSON_RECEVING_LOCATION_LIST)) {
                var receiveLocation =
                    (JSONObject(it).getJSONArray(JSON_RECEVING_LOCATION_LIST).get(0)).toString().split(",")
                receiveLocation.forEach { it1 ->
                    storageLocation.add(
                        ((it1.split(":")[0]).replace("{", "").replace("\"", "")).plus(" - ")
                            .plus((it1.split(":")[1]).replace("}", "").replace("\"", ""))
                    )
                }
            }
        }
        vm.fetchWarehouseWithMtns()
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {

            wareHouseList = data.data.storageLocationLst.distinct().toMutableList()
            allOBDList = data.data.mtns.toMutableList()
            allBatchList = data.data.batchDetails.toMutableList()
            allBatchList.forEachIndexed { index, s ->
                allBatchList[index].weight = allBatchList[index].weight.toString()
            }
            var reprocessingPlants = storageLocation.map { it.split("-")[0].trim() }
            var list = allBatchList.filter { it.supplyingPlantId in reprocessingPlants }

            var weighTypedata = ArrayList<VegaQualityWBDetails>()
            list.forEach { item ->
                var weighbridge = VegaQualityWBDetails()
                weighbridge.materialName = item.materialName
                weighbridge.materialCode =item.materialNumber
                weighbridge.storageLocation = item.storageLocationCode
                weighbridge.storageLocationCode = item.storageLocationCode.toString()
                weighbridge.plant = item.supplyingPlantId.plus(" - ").plus(item.supplyingPlantName)
                weighbridge.batchNumber = item.batch
                var obdList = allOBDList.filter { it.mtnNumber.equals(item.mtnNumber) }.filter { it.posnr.equals(item.posnr) }
                if(obdList.size>0){
                    weighbridge.weighBridgeId = obdList.get(0).mtntWbid
                    weighbridge.gateEntry = obdList.get(0).gateEntry
                }
                weighbridge.delivery = item.mtnNumber
                weighbridge.purchaseDocNum = item.purchaseOrder
                weighbridge.purchaseDocDesc = item.ebelp
                weighbridge.deliveryItem = item.posnr
                weighbridge.unitsOfMeasure = item.uom
                if (item.uom == "BAG")
                    weighbridge.bagCount = item.weight
                weighbridge.netWeight = item.weight
                weighTypedata.add(weighbridge)
            }


            weighTypedata.let { it1 ->
                var data1 = listOf<VegaQualityWBDetails>()
                var filteredList = arrayListOf<VegaQualityWBDetails>()
                data1 =
                    if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                        it.materialCode.equals(copiedMaterial)
                    } else it1
                if (data1.isNotEmpty()) {
                    //                    data1.forEach { wb ->
                    //                        if (!offlineDataList.map { it.weighBridgeId }.contains(wb.weighBridgeId)) {
                    //                            filteredList.add(wb)
                    //                        }
                    //                    }
                    mListener.setQualityWBList(data1)
                    mQualityWBList.clear()
                    mQualityWBList = data1 as MutableList<VegaQualityWBDetails>
                    mAdapter.addItems(data1)
                    binding.tvNoData.gone()
                    binding.rvWeighbridge.visible()
                } else {
                    binding.tvNoData.visible()
                    binding.rvWeighbridge.gone()
                }

            }
        }
    }

    private fun initExtra() {
        arguments?.let {
            weightmentType = MTNR
            copiedWbid = it.getString(COPIED_WBID).toString()
            copiedMaterial = it.getString(COPIED_MATERIAL).toString()
        }
    }

    private fun moveBagdetail(wbDetails: VegaQualityWBDetails?) {
        mListener.onWeighScaleClick(wbDetails)
    }

    fun updateAdapter(mQualityWBList: MutableList<VegaQualityWBDetails>) {
        mQualityWBList.let { data ->
            binding.rvWeighbridge.let {
                //if (mAdapter.itemCount != mQualityWBList.size) {
                if (data.size > 0 && isValidDataAvailable(data)) {
                    mAdapter.addItems(data)
                    binding.tvNoData.gone()
                    binding.rvWeighbridge.visible()
                } else {
                    binding.tvNoData.visible()
                    binding.rvWeighbridge.gone()
                }
            }
            //}
        }
    }

    private fun isValidDataAvailable(weighbridge: MutableList<VegaQualityWBDetails>): Boolean {
        return weighbridge.any { data -> !data.qcStatus!!.contains("X") }
    }


}
