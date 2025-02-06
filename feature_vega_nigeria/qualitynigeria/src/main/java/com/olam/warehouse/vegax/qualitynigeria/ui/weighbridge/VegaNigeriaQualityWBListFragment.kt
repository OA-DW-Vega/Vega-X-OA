package com.olam.warehouse.vegax.qualitynigeria.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.veganigeria.model.VegaNigeriaCocoPortPlantIdModel
import com.olam.warehouse.master.veganigeria.utils.PORT_PLANTLIST
import com.olam.warehouse.master.veganigeria.utils.portPlantIdList
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.DIRECTIONIN
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitynigeria.R
import com.olam.warehouse.vegax.qualitynigeria.databinding.FragmentVegaNigeriaQualityWeighBridgeListBinding
import com.olam.warehouse.vegax.qualitynigeria.ui.VegaNigeriaQualityViewModel
import com.olam.warehouse.vegax.qualitynigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaNigeriaQualityWBListFragment : BaseFragment() {

    private var mAdapter = VegaNigeriaQualityWBListAdapter { moveBagdetail(it) }
    private val vm: VegaNigeriaQualityViewModel by viewModel()
    private lateinit var mListener: OnWeighBridgeListener
    private lateinit var binding: FragmentVegaNigeriaQualityWeighBridgeListBinding
    override val layoutResourceId = R.layout.fragment_vega_nigeria_quality_weigh_bridge_list
    private var weightmentType: String? = ""
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private var selectedPlantId = ""
    private var plantList = mutableListOf<Plant>()

    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var offlineDataList = mutableListOf<VegaQualityWBDetails>()
    var portPlantsList = mutableListOf<String>()
    var jsonData = mutableListOf<String>()
    var allQualityWBDetails: List<VegaQualityWBDetails>? = null

    interface OnWeighBridgeListener {
        fun onWeighBridgeClick(
            wbDetails: VegaQualityWBDetails?,
            juteBagDetails : List<VegaQualityWBDetails>?,
            copiedWbid: String,
            copiedMaterial: String,
            selectedPlantId: String
        )

        fun setQualityWBList(it: List<VegaQualityWBDetails>?)
        fun onQualityOfflineClick()
    }

    companion object {
        fun newInstance() = VegaNigeriaQualityWBListFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaQualityWeighBridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExtra()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualityecuador/ui/weighbridge/VegaEcuadorQualityWBListFragment - $weightmentType")
            .title("Ecuador Quality").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_nigeria_quality_menu, menu)
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
                            mAdapter.addItems(mQualityWBList, selectedPlantId)
                        } else {
                            mSearchList.clear()
                            mQualityWBList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            mAdapter.addItems(mSearchList, selectedPlantId)
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

        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("COCO")) {
            vm.qualityOfflineList.observe(viewLifecycleOwner, Observer { enableOfflineLabel(it) })
            vm.getQualityOfflineListCount()

            vm.getPortPlantsId(getCurrentKey())
            vm.portItems.observe(viewLifecycleOwner, Observer {
                updatePortPlantList(it)
            })
        }


        /*if (isOnline()) {
            vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
            vm.getWeighBridgeDataOnline()
        } else {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
            vm.getWeighBridgeDetail()
        }*/
        vm.qcweighBridge.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        binding.llQualityOffline.setOnClickListener { mListener.onQualityOfflineClick() }
        binding.ivSortDownUp.setOnClickListener {
            val data1 = mQualityWBList.filter { it.wbTempId.contains("TMP") }
            val data2 = mQualityWBList.filter { !it.wbTempId.contains("TMP") }
            mQualityWBList.clear()
            mQualityWBList.addAll(data2.asReversed())
            mQualityWBList.addAll(data1)
            mAdapter.addItems(mQualityWBList, selectedPlantId)
        }

        binding.btnGo.setOnClickListener {
            if (selectedPlantId.isNotEmpty()) {
                if (isOnline()) vm.getQCWeighBridgeList(selectedPlantId)
                /*if (isOnline()) {
                    vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
                    vm.getWeighBridgeDataOnline()
                } else {
                    vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
                    vm.getWeighBridgeDetail()
                }*/
            } else
                Toast.makeText(context, "Select Plant to continue", Toast.LENGTH_SHORT).show()
        }

        plantList = getMultiPlantList() as MutableList<Plant>
        var ids = plantList.map { it.plantId }
        updatePlantListUI(ids as ArrayList<String>)

    }

    private fun initExtra() {
        arguments?.let {
            weightmentType = it.getString(WEIGHBRIDGE_LIST_TYPE)
            copiedWbid = it.getString(COPIED_WBID).toString()
            copiedMaterial = it.getString(COPIED_MATERIAL).toString()
        }

        when (weightmentType) {
            SUPPLIER -> {
                binding.tvType.text =
                    getString(R.string.quality_analysis).plus(" - ")
                        .plus(getString(R.string.supplier))
            }
            MTNR -> {
                binding.tvType.text = getString(R.string.quality_analysis).plus(" - ").plus(MTNR)
            }
        }
    }

    private fun updatePlantListUI(plantList: ArrayList<String>) {
        binding.spPlantSelection.isEnabled = true
        var plantIdList = ArrayList<String>()
        plantIdList.add(getString(R.string.select_plant_id))
        plantIdList.addAll(plantList)
        val stageAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.item_vega_nigeria_quality_plant_select,
                plantIdList
            )
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spPlantSelection.adapter = stageAdapter
        binding.spPlantSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position > 0) {
                        selectedPlantId = plantIdList[position]
//                    validateLot(selectedPlantId)
                    }
//                binding.spPlantSelection.setSelection(0)
                }
            }
    }

    private fun updatePortPlantList(miscellaneous: List<VegaCocoaMiscellaneous>) {
        portPlantsList.clear()
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        jsonData.forEach {
            if (it.contains(PORT_PLANTLIST)) {
                val plantId = gson.fromJson(it, VegaNigeriaCocoPortPlantIdModel::class.java)
                portPlantsList.addAll(plantId.PORT_PLANTLIST)
                portPlantIdList = portPlantsList as ArrayList<String>
            }
        }
    }


    private fun moveBagdetail(wbDetails: VegaQualityWBDetails?) {
        var juteBagDetail = allQualityWBDetails?.filter { it.weighBridgeId.equals(wbDetails?.weighBridgeId) }?.filter { it.materialCode.equals(
            JUTE_BAG_MATERIAL_CODE) }
        mListener.onWeighBridgeClick(wbDetails, juteBagDetail, copiedWbid, copiedMaterial, selectedPlantId)
    }

    fun updateAdapter(mQualityWBList: MutableList<VegaQualityWBDetails>) {
        mQualityWBList.let { data ->
            binding.rvWeighbridge.let {
                //if (mAdapter.itemCount != mQualityWBList.size) {
                if (data.size > 0 && isValidDataAvailable(data)) {
                    mAdapter.addItems(data, selectedPlantId)
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

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                /* val data = response.data?.data?.filter { value -> !value.qcStatus!!.contains("X") }
                    ?.filter { it.grnNumber.isresponse.data = {GenericReqAndResp@21126} GenericReqAndResp(data=[VegaQualityWBDetails(wbTempId=, weighBridgeId=226370000001, mtnNumber=, weighBridgeType=STO, direction=IN, item=00001, plant=6370, gateEntry=, purchaseDocNum=6902001358, batchNumber=, purchaseDocDesc=00001, salesDocNum=, materialCode=000000100000037665, deliveryItem=000010, delivery=8583966887, materialName=NGA Mid Bush Bag Cocoa Bean, customerNum=, supplierCode=, supplierName=, mergedBatchNumber=, receivedWeight=null, grnModel=, procurementType=, grntNumber=, autoTransfer=, recStorageLocation=, qualityDetails=[], isSyncStatus=false, isErrorStatus=true, isOfflineData=false, isNotWBID=false, status=1, message=, netWeight=1.000, erdat=/Date(1649808000000)/, ertim=PT08H27M58S, challan=, qcStatus=, bagWeight=0.000, bagCount=1, bagType=BIG BAG, bagMaterialCode=, grossWeight=1.000, unitsOfMeasure=MT, finalApproval=Q, appName=, LOBM_UDCODE=, vehicleNumber=ted, grnNumber=, unitPrice=0.00, storageLocationCode=1001, vendorDeclaredWeight=null, origin=null, department=null,… ViewNotEmpty() }*/
                val data = response.data?.data
                var weighTypedata: List<VegaQualityWBDetails>? = null
                /*for filteration purpose, if just bags are available*/
                allQualityWBDetails = data?.filter { wb -> wb.direction == DIRECTIONIN }
                    ?.filter { value -> value.weighBridgeType == STO }
                    ?.filter { value -> !value.netWeight.equals("0.000") }

                when (weightmentType) {
                    SUPPLIER -> {
                        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("CASH")){
                            weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
                                ?.filter { value -> value.weighBridgeType == PROCURE }
                                ?.filter { value -> !value.netWeight.equals("0.000") }
                                ?.filter { value -> value.qcStatus .equals("") }
                        }else {
                            weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
                                ?.filter { value -> value.weighBridgeType == PROCURE }
                                ?.filter { value -> !value.netWeight.equals("0.000") }
                                ?.filter { value -> !value.qcStatus.equals("D") }
                                ?.filter { value -> !value.qcStatus.equals("X") }
                        }
                    }
                    MTNR -> {
                        weighTypedata = data?.filter { wb -> wb.direction == DIRECTIONIN }
                            ?.filter { value -> value.weighBridgeType == STO }
                            ?.filter { value -> !value.netWeight.equals("0.000") }
                            ?.filter { value -> !value.materialCode.equals(JUTE_BAG_MATERIAL_CODE, ignoreCase = true)  }
                    }
                }
                weighTypedata?.let { it1 ->
                    var data1 = listOf<VegaQualityWBDetails>()
                    var filteredList = arrayListOf<VegaQualityWBDetails>()
                    data1 =
                        if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                            it.materialCode.equals(copiedMaterial)
                        } else it1
                    if (data1.isNotEmpty()) {
                        data1.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }
                                    .contains(wb.weighBridgeId)) {
                                filteredList.add(wb)
                            }
                        }
                        mListener.setQualityWBList(filteredList)
                        mQualityWBList.clear()
                        mQualityWBList = filteredList
                        mAdapter.addItems(filteredList, selectedPlantId)
                        binding.tvNoData.gone()
                        binding.rvWeighbridge.visible()
                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighbridge.gone()
                    }

                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun enableOfflineLabel(response: List<VegaQualityWBDetails>) {
        response.let {
            if (it.isNotEmpty()) {
                binding.llQualityOffline.visible()
                offlineDataList = response as MutableList<VegaQualityWBDetails>
            } else {
                binding.llQualityOffline.gone()
            }
        }
    }


    private fun updateUI(response: List<VegaQualityWBDetails>?) {
        response?.let { qualityDetail ->
            val offlineData = qualityDetail.filter { value -> value.isNotWBID }.filter { it.wbTempId.contains("TMP") }
            val data =
                qualityDetail.filter { value -> !value.qcStatus!!.contains("X") }.filter { it.grnNumber.isNotEmpty() }
            var weighTypedata: List<VegaQualityWBDetails>? = null
            when (weightmentType) {
                SUPPLIER -> {
                    weighTypedata = data.filter { wb -> wb.direction == DIRECTIONIN }
                        .filter { value -> value.weighBridgeType == PROCURE }
                }
                MTNR -> {
                    weighTypedata = data.filter { wb -> wb.direction == DIRECTIONIN }
                        .filter { value -> value.weighBridgeType == STO }
                        .filter { value -> !value.netWeight.equals("0.000") }
                }
            }
            weighTypedata?.let { it1 ->
                var data2 = arrayListOf<VegaQualityWBDetails>()
                var data1 = listOf<VegaQualityWBDetails>()
                var filteredList = arrayListOf<VegaQualityWBDetails>()
                data1 = if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                    it.materialCode.equals(copiedMaterial)
                } else it1
                data2.addAll(data1.sortedBy { it.weighBridgeId })
                data2.addAll(offlineData)
                if (data2.isNotEmpty()) {
                    data2.forEach { wb ->
                        if (!offlineDataList.map { it.weighBridgeId }.contains(wb.weighBridgeId)) {
                            filteredList.add(wb)
                        }
                    }
                    mListener.setQualityWBList(filteredList)
                    mQualityWBList.clear()
                    mQualityWBList = filteredList
                    mAdapter.addItems(filteredList, selectedPlantId)
                    binding.tvNoData.gone()
                    binding.rvWeighbridge.visible()
                } else {
                    binding.tvNoData.visible()
                    binding.rvWeighbridge.gone()
                }

            }
        }
    }
}
