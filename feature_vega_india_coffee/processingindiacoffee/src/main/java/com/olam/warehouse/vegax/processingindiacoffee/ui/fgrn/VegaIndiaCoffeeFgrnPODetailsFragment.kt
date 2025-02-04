package com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaOfflineRmin
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentVegaIndiaCoffeeFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.*
import kotlinx.android.synthetic.main.item_vega_india_coffee_fgrn_po_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.DecimalFormat

class VegaIndiaCoffeeFgrnPODetailsFragment : BaseFragment() {

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
        } else {
            binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
            vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
            vm.fetchStages()
            vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
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
            searchView.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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
                        if (!vegaProcessingStage.processName.equals(getString(R.string.select_stage)))
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

                                        var currentStage = currentStage.split("-")[1]
                                        if (item.versionId?.equals(currentStage) == true) {
                                            if (!item.rminList.isNullOrEmpty()) {
                                                item.rminList?.filter { !item.rminList.isNullOrEmpty() } as MutableList<VegaCocoaFgrnItems>
                                                poList.add(item)
                                            }
                                        }
                                    }

                                }
                                if (getCurrentKey().split("_")[1].contains("NI") && plantList.contains(
                                        plantId
                                    )
                                ) {
                                    var poDetailsList = poList
                                    var po = poLists.map { it.processOrderNo }.distinct()
                                    poDetailsList =
                                        poDetailsList.filter { po.contains(it.processOrderNo) } as MutableList<VegaCocoaFgrnItems>
                                    updateAdapter(poDetailsList)

                                } else
                                    updateAdapter(poList)
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
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
        binding.rvPoList.setUp(
            poList,
            R.layout.item_vega_india_coffee_fgrn_po_details,
            { item, pos ->
                tvPoNo.text = item.processOrderNo
                if (currentKey.split("_")[1].contains("NI")) {
                    tv_material_lbl.text = "Stage"
                    tv_weight_lbl.visibility = View.GONE
                    tvWeight.visibility = View.GONE
                }
                tv_material_name.text = item.materialName
                val weightToProcess =
                    weightToProcess(item.rminList, item.rfgrnList, item.unitsOfMeasure)
                if (getCurrentKey().split("_")[1].contains("NI") && plantList.contains(plantId)) {
                    val weight = item.netWeight.plus(" ").plus(item.unitsOfMeasure)
                    tvWeight.text = weight
                } else {
                    val weight = DecimalFormat("#########.###").format(weightToProcess).toString()
                    tvWeight.text = weight.plus(" ").plus(item.unitsOfMeasure)
                }
                val times = item.startDate?.split('(', ')')
                tvDate.text = times?.get(1).let { it1 ->
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
                if (getCurrentKey().split("_")[1].contains("NI") && plantList.contains(plantId)) {
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
                    currentPo.weight = weight
                    vm.fetchOfflineFgrnList(this.processOrderNo)
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
            vm.offlineRminItemLocal.observe(this, Observer {
                if (!it.isNullOrEmpty()) {
                    prepareData(it)
                }
            })
            vm.poDetailList.observe(viewLifecycleOwner, Observer {
                updateFgrnPoUI(it)
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
            vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
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
}
