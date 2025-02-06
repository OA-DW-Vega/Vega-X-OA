package com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.OnFragmentQualityApproveListener
import com.olam.warehouse.vegax.qualityapproveindiacoffee.R
import com.olam.warehouse.vegax.qualityapproveindiacoffee.databinding.FragmentVegaQualtyApproveIndiaCoffeeWeighbridgeListBinding
import com.olam.warehouse.vegax.qualityapproveindiacoffee.databinding.ItemVegaWeighbridgeQualtyApproveIndiaCoffeeBinding
import com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.VegaQualityApproveIndiaCoffeeViewModel
import com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.quality.VegaBcApproveIndiaCoffeeReadFragment
import com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.quality.VegaQualityApproveIndiaCoffeeFragment
import com.olam.warehouse.vegax.qualityapproveindiacoffee.utils.APPROVE_DATA
import com.olam.warehouse.vegax.qualityapproveindiacoffee.utils.APPROVE_WB_DATA
import com.olam.warehouse.vegax.qualityapproveindiacoffee.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaQualityApproveIndiaCoffeeWeighbridgeListFragment : BaseFragment() {

    private var weighBridgeDetails: List<VegaQualityWBDetails>? = mutableListOf()
    private lateinit var roleData: List<UserRole>
    private val vm: VegaQualityApproveIndiaCoffeeViewModel by viewModel()
    private var weighBridgeList = mutableListOf<VegaQualityWBDetails>()
    private val mSearchList = mutableListOf<VegaQualityWBDetails>()
    private var mListener: OnFragmentQualityApproveListener? = null
    private var approveWeighBridgeId = VegaQualityWBDetails()
    private lateinit var binding: FragmentVegaQualtyApproveIndiaCoffeeWeighbridgeListBinding
    private var isRead = false
    private var selectedPlantId = ""
    private var plantList = mutableListOf<Plant>()

    override val layoutResourceId = R.layout.fragment_vega_qualty_approve_india_coffee_weighbridge_list

    companion object {
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaQualtyApproveIndiaCoffeeWeighbridgeListBinding.inflate(layoutInflater)
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
                                    if (qtyWb.weighBridgeId.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            /*if(mSearchList != null) {
                                if (mSearchList.size > 0) {
                                    setUpAdapter(mSearchList)
                                }
                            }*/
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
        if (context is OnFragmentQualityApproveListener) {
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
        roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(PreferenceHelper.get(Constants.CURRENT_KEY, "")) }

        roleData.forEach { rol ->
            when (UserRoles.valueOfEnum(rol.roleName.trim())) {
                UserRoles.PCH -> {
                    isRead = true
                }
                else -> {}
            }
        }

        vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getWeighBridgeDataOnline()




        //vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
        //vm.qcweighBridge.observe(viewLifecycleOwner, Observer { updateQcUI(it) })

        binding.ivSortDownUp.setOnClickListener {
            val data = weighBridgeList
          //  weighBridgeList = data.asReversed()
            setUpAdapter(weighBridgeList)
        }

       /* binding.btnGo.setOnClickListener {
            if (selectedPlantId.isNotEmpty()) {
                if (AppUtils.isOnline()) {
                    if (isRead)
                        vm.getWeighBridgeList(selectedPlantId)
                    else
                        vm.getQCWeighBridgeList(selectedPlantId)
                }
            } else
                Toast.makeText(context, "Select Plant to continue", Toast.LENGTH_SHORT).show()
        }
*/
        //Multi Plant selection
        //Should be moved to observer of plant list

        plantList = getMultiPlantList() as MutableList<Plant>
//        var ids = plantList.map { it.plantId }
        //updatePlantListUI(ids as ArrayList<String>)

        /*vm.multiPlant.observe(viewLifecycleOwner, Observer {
            plantList = it.toMutableList()
            var ids = plantList.map { it.plantId }
            updatePlantListUI(ids as ArrayList<String>)

        })
        vm.getMultiPlantList()*/

        /* var plantList = arrayListOf<Plant>()
         plantList.add(Plant("2741", "", "", "", "", CountryDetail("", 0, "")))
         var ids = plantList.map { it.plantId }
         updatePlantListUI(ids as ArrayList<String>)*/
    }

   /* private fun updatePlantListUI(plantList: ArrayList<String>) {
        binding.spPlantSelection.isEnabled = true
        var plantIdList = ArrayList<String>()
        plantIdList.add(getString(R.string.select_plant_id))
        plantIdList.addAll(plantList)
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_india_coffee_approval_plant_select, plantIdList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spPlantSelection.adapter = stageAdapter
        binding.spPlantSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    selectedPlantId = plantIdList[position]
//                    validateLot(selectedPlantId)
                }
//                binding.spPlantSelection.setSelection(0)
            }
        }
    }*/

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge = if(getCurrentKey().contains("VEGA_IV") && getCurrentKey().contains("COCO")){
                        it.data?.data?.filter { it.qcStatus=="R" && it.weighBridgeType == "PROCURE" }
                    }else{ it.data?.data?.filter { it.qcStatus?.isNotEmpty() == true && it.weighBridgeType == "PROCURE" && !it.qcStatus.equals("X")} }

                    /*?.filter { it.qcStatus.equals("X") }
                    ?.filter { it.netWeight.isNotEmpty() }
                    ?.filter { !it.netWeight.equals("0.000") }*/
                    if (weighBridge?.size!! > 0) {
                        weighBridgeList = weighBridge as MutableList<VegaQualityWBDetails>

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

   /* private fun updateQcUI(data: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    weighBridgeDetails =
                        it.data?.data

                    weighBridgeDetails = weighBridgeDetails?.filter { it.qcStatus == "R" || it.qcStatus == "D" }
                    *//*?.filter { it.qcStatus.equals("X") }
                    ?.filter { it.netWeight.isNotEmpty() }
                    ?.filter { !it.netWeight.equals("0.000") }*//*
                    if (weighBridgeDetails?.size!! > 0) {
                        weighBridgeList =
                            prepareQcWBList(weighBridgeDetails!!) as MutableList<VegaQualityApproveCameroonWeighBridge>

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
    }*/

    private fun setUpAdapter(data: List<VegaQualityWBDetails>?) {
        if (data != null) {
            var weighBridgeList1 = mutableListOf<VegaQualityWBDetails>()

            if (!(data.size > 0)) {
                weighBridgeList1 = mutableListOf<VegaQualityWBDetails>()
            } else {
                weighBridgeList1 = data as MutableList<VegaQualityWBDetails>
                /*weighBridgeList1 = data.sortedByDescending {
                    it.weighBridgeId
                    *//* it.erdat?.split('(', ')')?.get(1)?.let { it1 ->
                         DateUtils.getUTCDateTime(
                             it1,
                             App.getAppContext()
                         )
                     }*//*
                } as MutableList<VegaQualityWBDetails>*/
            }
            var count = 0
            weighBridgeList1.reverse()
            binding.rvWeighBridgeId.setUpAdapter(
                weighBridgeList1,
                R.layout.item_vega_weighbridge_qualty_approve_india_coffee,
                ItemVegaWeighbridgeQualtyApproveIndiaCoffeeBinding::inflate,
                { it, pos, bindItem ->
                    it.unitPrice = it.unitPrice.toString().replace("\\s".toRegex(), "")
                    bindItem.tvMaterialValue.text = it.materialName
//            tvTruckNo.text = it.vehicleNumber
                    bindItem.tvSupplierName.text = it.supplierCode.plus("-").plus(it.supplierName)
                    bindItem.tvWeight.text = it.netWeight?.replace(" ", "").plus(it.unitsOfMeasure)
                    bindItem.tvWeighBridgeId.text = it.weighBridgeId

                    if ("null" != it.erdat) {
                        val times = it.erdat?.split('(', ')')
                        bindItem.tvDate.text = times?.get(1)?.let { it1 ->
                            DateUtils.getUTCDateTime(
                                it1,
                                App.getAppContext()
                            )
                        }
                    }

//            val times = it.erdat?.split('(', ')')
                    //tvDate.text = it.year
                    /* times?.get(1).let { it1 ->
             it1?.let { it2 ->
                 DateUtils.getUTCDateTime(
                     it2,
                     App.getAppContext()
                 )
             }
         }*/

                    count++
                },
                {

                    /* val fragment = VegaQualityApproveCameroonDetailsFragment()
             val args = Bundle()
             approveWeighBridgeId = this
             args.putParcelable(APPROVE_DATA, approveWeighBridgeId)
             fragment.arguments = args
             mListener?.onFragmentInteraction(fragment)*/

                    val args = Bundle()
                    approveWeighBridgeId = this
                    //this.plantId = this.werks
                    args.putParcelable(APPROVE_DATA, approveWeighBridgeId)
                    //var plantDetails = plantList.filter { it.plantId == selectedPlantId }.single()
                    //args.putParcelable(APPROVE_PLANT_DATA, plantDetails)
                    val wbDetails =
                        weighBridgeList1.filter { it.weighBridgeId == this.weighBridgeId }
                            .filter { it.materialCode == this.materialCode }
                    //if(wbDetails.size>0) wbDetails.get(0) else wbDetails?.single())
//            var fragment = VegaQualityApproveCameroonFragment()
//            fragment.arguments = args
//            mListener?.onFragmentInteraction(fragment)

                    if (isRead) {
                        val fragment = VegaBcApproveIndiaCoffeeReadFragment()
                        fragment.arguments = args
                        mListener?.onFragmentInteraction(fragment)
                    } else {
                        val fragment = VegaQualityApproveIndiaCoffeeFragment()
                        args.putParcelable(APPROVE_WB_DATA, wbDetails.single())
                        fragment.arguments = args
                        mListener?.onFragmentInteraction(fragment)
                    }

                    /* roleData.forEach { rol ->
                when (UserRoles.valueOfEnum(rol.roleName.trim())) {
                    UserRoles.PCH -> {
                        isRead = true
//                        fragment = VegaBcApproveCameroonReadFragment()
                    }
                }
            }
            if(isRead){
                var fragment = VegaBcApproveCameroonReadFragment()
                fragment.arguments = args
                mListener?.onFragmentInteraction(fragment)
            }
            else {
                var fragment = VegaQualityApproveCameroonFragment()
                fragment.arguments = args
                mListener?.onFragmentInteraction(fragment)
            }*/
                })
        }

    }
}
