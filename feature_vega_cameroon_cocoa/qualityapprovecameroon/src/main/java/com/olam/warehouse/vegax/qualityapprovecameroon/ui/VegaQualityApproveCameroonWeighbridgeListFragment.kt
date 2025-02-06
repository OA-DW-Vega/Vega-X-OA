package com.olam.warehouse.vegax.qualityapprovecameroon.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovecameroon.R
import com.olam.warehouse.vegax.qualityapprovecameroon.databinding.FragmentVegaQualtyApproveCameroonWeighbridgeListBinding
import com.olam.warehouse.vegax.qualityapprovecameroon.databinding.ItemVegaWeighbridgeQualtyApproveCameroonBinding
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.OnFragmentQualityApproveCameroonInteractionListener
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.VegaQualityApproveCameroonViewModel
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.quality.VegaBcApproveCameroonReadFragment
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.quality.VegaQualityApproveCameroonFragment
import com.olam.warehouse.vegax.qualityapprovecameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
class VegaQualityApproveCameroonWeighbridgeListFragment : BaseFragment() {

    private var weighBridgeDetails: List<VegaQualityWBDetails>? = mutableListOf()
    private lateinit var roleData: List<UserRole>
    private val vm: VegaQualityApproveCameroonViewModel by viewModel()
    private var weighBridgeList = mutableListOf<VegaQualityApproveCameroonWeighBridge>()
    private val mSearchList = mutableListOf<VegaQualityApproveCameroonWeighBridge>()
    private var mListener: OnFragmentQualityApproveCameroonInteractionListener? = null
    private var approveWeighBridgeId = VegaQualityApproveCameroonWeighBridge()
    private lateinit var binding: com.olam.warehouse.vegax.qualityapprovecameroon.databinding.FragmentVegaQualtyApproveCameroonWeighbridgeListBinding
    private var selectedPlantId = ""
    private var plantList = mutableListOf<Plant>()

    override val layoutResourceId = R.layout.fragment_vega_qualty_approve_cameroon_weighbridge_list

    companion object {
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaQualtyApproveCameroonWeighbridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("qualityapprovecameroon/ui/weighbridge/VegaQualityApproveCameroonWeighbridgeListFragment")
            .title("Vega_Cameroon/Approve")
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
                                    if (qtyWb.wbid?.contains(text)!!) {
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
        if (context is OnFragmentQualityApproveCameroonInteractionListener) {
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
        if (isRead) {
            binding.tvTruckno.text = getString(R.string.bc_quality_approval)
        }
        vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.qcweighBridge.observe(viewLifecycleOwner, Observer { updateQcUI(it) })

        binding.ivSortDownUp.setOnClickListener {
            if (weighBridgeList.isNotEmpty()) {
                val data = weighBridgeList
                weighBridgeList = data.asReversed()
                setUpAdapter(weighBridgeList)
            }
        }
        binding.btnGo.setOnClickListener {
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

        //Multi Plant selection
        //Should be moved to observer of plant list

        plantList = getMultiPlantList() as MutableList<Plant>
        var ids = plantList.map { it.plantId }
        updatePlantListUI(ids as ArrayList<String>)

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

    private fun updatePlantListUI(plantList: ArrayList<String>) {
        binding.spPlantSelection.isEnabled = true
        var plantIdList = ArrayList<String>()
        plantIdList.add(getString(R.string.select_plant_id))
        plantIdList.addAll(plantList)
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_cameroon_approval_plant_select, plantIdList)
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
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }

    private fun updateQcUI(data: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    weighBridgeDetails =
                        it.data?.data

                    weighBridgeDetails = weighBridgeDetails?.filter { it.qcStatus == "R" || it.qcStatus == "D" }
                    /*?.filter { it.qcStatus.equals("X") }
                    ?.filter { it.netWeight.isNotEmpty() }
                    ?.filter { !it.netWeight.equals("0.000") }*/
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
    }

    private fun setUpAdapter(data: List<VegaQualityApproveCameroonWeighBridge>?) {
        if (data != null) {
            var weighBridgeList1 = mutableListOf<VegaQualityApproveCameroonWeighBridge>()

            if (!(data.size > 0)) {
                weighBridgeList1 = mutableListOf<VegaQualityApproveCameroonWeighBridge>()
            } else {

//                weighBridgeList1 = data.sortedByDescending {
//                    it.wbid
//                    /* it.erdat?.split('(', ')')?.get(1)?.let { it1 ->
//                         DateUtils.getUTCDateTime(
//                             it1,
//                             App.getAppContext()
//                         )
//                     }*/
//                } as MutableList<VegaQualityApproveCameroonWeighBridge>
                weighBridgeList1 = data as MutableList<VegaQualityApproveCameroonWeighBridge>
//                    it.wbid
//                    /* it.erdat?.split('(', ')')?.get(1)?.let { it1 ->
//                         DateUtils.getUTCDateTime(
//                             it1,
//                             App.getAppContext()
//                         )
//                     }*/
//                } as MutableList<VegaQualityApproveCameroonWeighBridge>
//            }
            }
            var count = 0
            binding.rvWeighBridgeId.setUpAdapter(
                weighBridgeList1.asReversed(),
                R.layout.item_vega_weighbridge_qualty_approve_cameroon,
                com.olam.warehouse.vegax.qualityapprovecameroon.databinding.ItemVegaWeighbridgeQualtyApproveCameroonBinding::inflate,
                { it, pos, bindItem ->
                    it.unitPrice = it.unitPrice.toString().replace("\\s".toRegex(), "")
                    it.basePrice = it.unitPrice.toString().replace("\\s".toRegex(), "")
                    bindItem.tvMaterialValue.text = it.materialName
//            tvTruckNo.text = it.vehicleNumber
                    bindItem.tvSupplierName.text = it.supplierName
                    bindItem.tvWeight.text = it.grnQty?.replace(" ", "").plus(it.meins)
                    GRN_WEIGHT = bindItem.tvWeight.text as String
                    bindItem.tvWeighBridgeId.text = it.wbid
                    if (it.qchar == "D")
                        bindItem.tvNegotiateLabel.visibility = View.VISIBLE
                    else
                        bindItem.tvNegotiateLabel.visibility = View.GONE

//            val times = it.erdat?.split('(', ')')
                    bindItem.tvDate.text = it.year
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
                    this.plantId = this.werks
                    args.putParcelable(APPROVE_DATA, approveWeighBridgeId)
                    var plantDetails = plantList.filter { it.plantId == selectedPlantId }.single()
                    args.putParcelable(APPROVE_PLANT_DATA, plantDetails)
                    var wbDetails = weighBridgeDetails?.filter { it.weighBridgeId == this.wbid }
//            var fragment = VegaQualityApproveCameroonFragment()
//            fragment.arguments = args
//            mListener?.onFragmentInteraction(fragment)

                    if (isRead) {
                        var fragment = VegaBcApproveCameroonReadFragment()
                        fragment.arguments = args
                        mListener?.onFragmentInteraction(fragment)
                    } else {
                        var fragment = VegaQualityApproveCameroonFragment()
                        args.putParcelable(APPROVE_WB_DATA, wbDetails?.single())
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
