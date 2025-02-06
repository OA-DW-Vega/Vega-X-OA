package com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaOfflineRmin
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentVegaIndiaCoffeeFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processingindiacoffee.databinding.ItemVegaIndiaCoffeeFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.DecimalFormat

class VegaIndiaCoffeeFgrnPODetailsFragment : BaseFragment(), VegaSingleSelectCommonListener {

    override val layoutResourceId = R.layout.fragment_vega_india_coffee_fgrn_po_details
    private lateinit var binding: FragmentVegaIndiaCoffeeFgrnPoDetailsBinding
    private val vm: VegaIndiaCoffeeFgrnViewModel by viewModel()
    private var poList = mutableListOf<VegaCocoaFgrnItems>()
    private var poListSort = mutableListOf<VegaCocoaFgrnItems>()
    private val mSearchList = arrayListOf<VegaCocoaFgrnItems>()
    private var currentPo = VegaCocoaFgrnItems()
    private var stageFevor: String = ""
    private var cfgNo: String = ""
    private var currentStage: String = ""
    private var auart: String = ""
    private var callBack: CallBack? = null
    private var poLists = mutableListOf<VegaCoffeeFgrnItems>()
    private val plantId = getPlantDetails().plantId
    private var plantList = ArrayList<String>()
    private var poListsSort = mutableListOf<VegaCoffeeFgrnItems>()
    private var currentKey = getCurrentKey()
   // private var isVendor = false
    private var isVendorRequired = false
    private var supplierList = mutableListOf<VegaVendor>()
    private var selectedVendor = ""
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null



    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaIndiaCoffeeFgrnPODetailsFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search Po Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaIndiaCoffeeFgrnPoDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnPODetailsFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        if (currentKey.split("_")[1].contains("NI")) {
            vm.getConfigItems(UserRoles.PROCESSING.role)
            vm.configItems.observe(viewLifecycleOwner, Observer {
                updateConfigItems(it)
            })
            vm.getSuppliers("NI01")
            vm.filteredsupplier.observe(viewLifecycleOwner, Observer {
                supplierList = it.toMutableList()
                //updateVendorUI(it)
            })
        } else {
            binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
            vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
            vm.fetchStages()
            vm.offlineFgrn.observe(viewLifecycleOwner, Observer { moveToNext(it) })
            binding.tvSort.setOnClickListener {
                if (poList.isNotEmpty()) {
                    poList.let { poListSort = it }
                    poListSort = poListSort.asReversed()
                    poList = poListSort
                    updateAdapter(poList)
                }
            }
        }

        binding.tvVendor.setOnClickListener {
            showSingleSelectDialog( getString(R.string.enter_vendor_name), VENDOR)
        }
        vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })


    }

    private fun updateVendorUI(list: List<VegaVendor>) {
        val vendor = ArrayList<String>()
        var listVendor = list.map { it.vendorCode.plus(" - ").plus(it.vendorName) }
       var firstText = getString(R.string.select_vendo_r)
        vendor.add(firstText)
        vendor.addAll(listVendor)
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_india_coffee_processing_rmin_grade, vendor)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
      //  binding.spVendor.adapter = stageAdapter
//        binding.spVendor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(p0: AdapterView<*>?) {}
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
//                if (position > 0) {
//                    selectedVendor = vendor[position].split("-")[0]
//                    isVendor = true
//                } else isVendor = false
//            }
//        }
    }

    private fun moveToNext(offlineFgrnList: List<VegaCocoaFgrnItems>) {
        when (getCurrentFragment()) {
            is VegaIndiaCoffeeFgrnPODetailsFragment -> when (offlineFgrnList.size > 0) {
                true -> callBack?.replaceFgrnFragment(FRAG_PENDING, currentPo, "")
                false -> callBack?.replaceFgrnFragment(FRAG_GRADES, currentPo, "")
            }
        }

    }

    fun getCurrentFragment(): Fragment {
        return activity?.supportFragmentManager?.findFragmentById(R.id.flProcessing)!!
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
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
                            updateAdapter(poList)
                        } else {
                            mSearchList.clear()
                            poList.forEach { po ->
                                newText?.let { text ->
                                    if (po.processOrderNo.contains(text, true)) {
                                        mSearchList.add(po)
                                    }
                                }
                            }
                            updateAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun updageStageUI(stageList: List<VegaProcessingStage>) {
        val stage = ArrayList<VegaProcessingStage>()
        val stageInitItem = VegaProcessingStage()
        stageInitItem.processName = getString(R.string.select_stage)
        stage.add(stageInitItem)
        stage.addAll(stageList)
        val stageListData = stage.map { data -> data.processName }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_india_coffee_processing_rmin_grade, stageListData)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStage.adapter = stageAdapter
        /*var defaultposition = 0
        stageListData.forEachIndexed { index, s -> if (s.equals("RCN Drying process")) defaultposition = index }
        binding.spStage.setSelection(defaultposition)*/
        binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                stage.forEachIndexed { index, vegaProcessingStage ->
                    if (stageListData[position] == vegaProcessingStage.processName) {
                        stageFevor = vegaProcessingStage.fevor
                        cfgNo = vegaProcessingStage.cfgNo
                        auart = vegaProcessingStage.auart.toString()
                        currentPo.stageFevor = vegaProcessingStage.fevor
                        currentPo.cfgNo = vegaProcessingStage.cfgNo
                        currentPo.auart = vegaProcessingStage.auart
                        currentStage = vegaProcessingStage.processName ?: ""
                        if (currentKey.split("_")[1].contains("NI")) {
                        if( vegaProcessingStage.processName?.contains("Toll",true) == true){
                            isVendorRequired = true
                            binding.tvVendor.visible()
                            binding.tvVendorTitle.visible()
                        }else {
                            isVendorRequired = false
                            binding.tvVendor.gone()
                            binding.tvVendorTitle.gone()
                        }
                        }
                        if (!vegaProcessingStage.processName.equals(getString(R.string.select_stage)) && !isVendorRequired)
                            vm.fetchFgrnPoDetailsList(stageFevor, cfgNo, auart)
                    }
                }
            }
        }
    }

    private fun updateFgrnPoUI(response: Resource<GenericReqAndResp<List<VegaCocoaFgrnItems>>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                poList.clear()
                                it.forEach { item ->
                                    if (getCurrentKey().split("_")[1].contains("NI")) {
                                        //var currentStage = currentStage.split("")[1]
                                        if (item.materialName?.equals(currentStage) == true) {
                                            if (!item.rminList.isNullOrEmpty()) {
                                                item.rminList?.filter { !item.rminList.isNullOrEmpty() } as MutableList<VegaCocoaFgrnItems>
                                                poList.add(item)
                                            }
                                            else {
                                                if (plantList.contains(plantId)) {
                                                    poList.add(item)
                                                }
                                            }

                                        }
                                    } else {
                                        if(currentStage.contains("Re-processing") || currentStage.contains("Reprocessing")){
                                            filterPO(item)
                                        }else {
                                            var currentStage = currentStage.split("-")[currentStage.split("-").size - 1]
                                            if (item.versionId?.equals(currentStage) == true) {
                                                filterPO(item)
                                            }
                                        }
                                    }

                                }
                                if (getCurrentKey().split("_")[1].contains("NI") &&
                                    plantList.contains(plantId)) {
                                    var poDetailsList = poList
                                    var po = poLists.map { it.processOrderNo }.distinct()
                                    poDetailsList =
                                        poDetailsList.filter { po.contains(it.processOrderNo) } as MutableList<VegaCocoaFgrnItems>
                                    if(selectedVendor.isNotEmpty()){
                                       poDetailsList= poDetailsList.filter { it.vendor == selectedVendor } as MutableList<VegaCocoaFgrnItems>
                                    }

                                    updateAdapter(poDetailsList)

                                } else {
/*
                                    if (currentKey.split("_")[1].contains("NI")) {
                                        if (isVendorRequired) {
                                            binding.tvVendor.visible()
                                            binding.tvVendorTitle.visible()
                                        }
                                        else {
                                            binding.tvVendor.visibility = View.GONE
                                            binding.tvVendorTitle.visibility = View.GONE
                                        }

                                    }
*/
                                    if(selectedVendor.isNotEmpty() && getCurrentKey().contains("VEGA_NI")){
                                        poList= poList.filter { it.vendor == selectedVendor } as MutableList<VegaCocoaFgrnItems>
                                    }

                                    updateAdapter(poList)
                                }
                            }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
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

    private fun filterPO(item: VegaCocoaFgrnItems) {
        if (!item.rminList.isNullOrEmpty()) {
            item.rminList?.filter { !item.rminList.isNullOrEmpty() } as MutableList<VegaCocoaFgrnItems>
            poList.add(item)
        }

    }

    private fun updateAdapter(poList: MutableList<VegaCocoaFgrnItems>) {
        //var poList1 = mutableListOf<VegaCocoaFgrnItems>()
        if (poList.isEmpty()) {
            binding.rvPoList.gone()
            binding.tvNoData.visible()
        } else {
            when (poList.size > 0) {
                true -> {
                    binding.rvPoList.visible()
                    binding.tvNoData.gone()
                }
                else -> {
                    binding.rvPoList.gone()
                    binding.tvNoData.visible()
                }
            }
        }
        binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
        binding.rvPoList.setUpAdapter(
            poList,
            R.layout.item_vega_india_coffee_fgrn_po_details,
            ItemVegaIndiaCoffeeFgrnPoDetailsBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvPoNo.text = item.processOrderNo
                if (currentKey.split("_")[1].contains("NI")) {
                    bindItem.tvMaterialLbl.text = "Stage"
                    bindItem.tvWeightLbl.visibility = View.GONE
                    bindItem.tvWeight.visibility = View.GONE
                }
                bindItem.tvMaterialName.text = item.materialName
                val weightToProcess =
                    weightToProcess(item.rminList, item.rfgrnList, item.unitsOfMeasure)
                if (getCurrentKey().split("_")[1].contains("NI") && plantList.contains(plantId)) {
                    val weight = item.netWeight.plus(" ").plus(item.unitsOfMeasure)
                    bindItem.tvWeight.text = weight
                } else {
                    val weight = DecimalFormat("#########.###").format(weightToProcess).toString()
                    bindItem.tvWeight.text = weight.plus(" ").plus(item.unitsOfMeasure)
                }
                val times = item.startDate?.split('(', ')')
                bindItem.tvDate.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }

            }, {
                val currentPo1 = currentPo.copy()
                currentPo = this
                val weightToProcess =
                    weightToProcess(this.rminList, this.rfgrnList, this.unitsOfMeasure)
                val weight = DecimalFormat("#########.###").format(weightToProcess).toString()
                currentPo.cfgNo = currentPo1.cfgNo
                currentPo.stageFevor = currentPo1.stageFevor
                currentPo.auart = currentPo1.auart
                currentPo.unitsOfMeasure = this.unitsOfMeasure
                currentPo.processOrderNo = this.processOrderNo
                currentPo.rminList= this.rminList
                if (getCurrentKey().split("_")[1].contains("NI") && plantList.contains(plantId)) {
                    //currentPo.vendor = this.vendor
                    currentPo.vendor =
                        if (selectedVendor.isNotEmpty()) selectedVendor else this.vendor
                    currentPo.materialName = this.materialName
                    currentPo.weight = this.netWeight.plus(" ").plus(this.unitsOfMeasure)
                    if (poLists.isNotEmpty()) {
                        poLists.forEach {
                            if (it.processOrderNo == this.processOrderNo) {
                                currentPo.materialCode = it.materialCode
                                currentPo.stageFevor = it.materialName
                            }
                        }
                    }
                    callBack?.replaceFgrnFragment(FRAG_RMIN_lOTS, currentPo, "")
                } else {
                    //milling location tolling material vendor checking
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        if (isVendorRequired && selectedVendor.isEmpty()) showSnack(getString(R.string.vendor_error))
                        else {
                            currentPo.vendor =
                                if (selectedVendor.isNotEmpty()) selectedVendor else this.vendor
                            currentPo.weight = weight
                            vm.fetchOfflineFgrnList(this.processOrderNo)
                        }
                    } else {
                        currentPo.weight = weight
                        vm.fetchOfflineFgrnList(this.processOrderNo)
                    }
                }
                // callBack?.replaceFragment(GRADESELECTION, this, "")
            })
    }

    fun prepareData(quality: List<VegaNicaraguaOfflineRmin>) {
        quality.forEach {
            it.rminLot.forEach { it1 ->
                val qualityParams = VegaCoffeeFgrnItems()
                qualityParams.processOrderNo = it1.processOrderNum.toString()
                qualityParams.netWeight = it1.netWeight
                qualityParams.materialName = it1.materialName
                qualityParams.materialCode = it1.materialCode
                qualityParams.rminId = it1.rminTempId
                qualityParams.rfgrnTotal = "0.0"
                qualityParams.startDate = it1.startTime?.let { it ->
                    DateUtils.getUTCDateTime(
                        it,
                        App.getAppContext()
                    )
                }
                //qualityParams.startDate = it1.startTime
                poLists.add(qualityParams)
            }
        }
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        plantList.clear()
        var dryingPlants = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }
        var plantLists = ""
        dryingPlants.forEach {
            plantLists = it.value?.trim() ?: ""
            if (plantLists.isNotEmpty()) {
                plantList = plantLists.split(",") as ArrayList<String>
            }
        }
        if (plantList.contains(plantId)) {
            binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
            vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
            vm.fetchStages()
            vm.getOfflineRminItem()
            vm.offlineRminItemLocal.observe(viewLifecycleOwner, Observer {
                if (!it.isNullOrEmpty()) {
                    prepareData(it)
                }
            })
            // vm.offlineFgrn.observe(viewLifecycleOwner, Observer { moveToNex(it) })
            binding.tvSort.setOnClickListener {
                if (poList.isNotEmpty()) {
                    poList.let { poListSort = it }
                    poListSort = poListSort.asReversed()
                    poList = poListSort
                    updateAdapter(poList)
                }
            }
        } else {
            binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
            vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
            vm.fetchStages()
            vm.offlineFgrn.observe(viewLifecycleOwner, Observer { moveToNext(it) })
            binding.tvSort.setOnClickListener {
                if (poList.isNotEmpty()) {
                    poList.let { poListSort = it }
                    poListSort = poListSort.asReversed()
                    poList = poListSort
                    updateAdapter(poList)
                }
            }
        }
    }

    private fun showSingleSelectDialog(title: String,currentFalg: String) {
        val list = ArrayList<String>()

        when(currentFalg) {
            VENDOR -> {
                val listVendor = supplierList.map { it.vendorCode.plus(" - ").plus(it.vendorName) }
                list.addAll(listVendor)
            }
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFalg,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        binding.tvVendor.text= data
        selectedVendor = data.split("-")[0].trim()

        if(!currentStage.equals(getString(R.string.select_stage)))
            vm.fetchFgrnPoDetailsList(stageFevor, cfgNo, auart)

    }

}
