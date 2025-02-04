package com.olam.warehouse.vegax.processingghana.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaGhanaProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineRmin
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.databinding.FragmentVegaGhanaFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processingghana.utils.FGRN_OFFLINE
import com.olam.warehouse.vegax.processingghana.utils.FRAG_GRADES
import com.olam.warehouse.vegax.processingghana.utils.FRAG_PENDING
import com.olam.warehouse.vegax.processingghana.utils.weightToProcess
import kotlinx.android.synthetic.main.item_vega_ghana_fgrn_po_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.DecimalFormat

class VegaGhanaFgrnPODetailsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_fgrn_po_details
    private lateinit var binding: FragmentVegaGhanaFgrnPoDetailsBinding
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private var poList = mutableListOf<VegaCoffeeFgrnItems>()
    private var offlineList = VegaCoffeeFgrnItems()
    private var poListSort = mutableListOf<VegaCoffeeFgrnItems>()
    private val mSearchList = arrayListOf<VegaCoffeeFgrnItems>()
    private var currentPo = VegaCoffeeFgrnItems()
    private var stageFevor: String = ""
    private var cfgNo: String = ""
    private var auart: String = ""
    private var callBack: CallBack? = null
    private var vegaStage = VegaProcessingStage()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var storageList = mutableListOf<VegaCoffeeFgrnItems>()

    interface CallBack {
        fun replaceFgrnFragment(
            fragment: String,
            model: VegaCoffeeFgrnItems,
            id: String,
            vegaStage: VegaProcessingStage
        )

        fun replaceFragment(fragment: String, vegaStage: VegaProcessingStage)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaGhanaFgrnPODetailsFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search Po Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaFgrnPoDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaSesameFgrnPODetailsFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
        vm.fetchStages()
        vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
        vm.offlineFgrn.observe(viewLifecycleOwner, Observer { moveToNext(it) })
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.toMutableList()
        })
        vm.getCustomLocations()

        vm.offlineFgrnItemLocal.observe(this, Observer {
            if (!it.isNullOrEmpty())
                binding.llOffline.isVisible = true
        })
        vm.getOfflineFgrnItem()
        vm.getOfflineRminItem()
        vm.offlineRminItemLocal.observe(this, Observer {
            if (!it.isNullOrEmpty())
                prepareData(it)
            //Toast.makeText(context, "inside", Toast.LENGTH_SHORT).show()
        })
        vm.processOrderItemLocal.observe(viewLifecycleOwner, Observer {
            customLocationList.forEach { item ->
                var tempList = it.filter { item.procureLocationCode == it.storageLocationCode!! }
                tempList.forEach {
                    offlineList = preparePOList(it)
                    poList.add(offlineList)
                }
            }
            updateAdapter(poList)
        })

        binding.tvSort.setOnClickListener {
            if (poList.isNotEmpty()) {
                poList.let { poListSort = it }
                poListSort = poListSort.asReversed()
                poList = poListSort
                updateAdapter(poList)
            }
        }

        binding.llOffline.setOnClickListener { moveToOfflineSummary() }
    }

    private fun moveToOfflineSummary() {
        callBack?.replaceFragment(FGRN_OFFLINE, vegaStage)
    }

    private fun preparePOList(poOfflineList: VegaGhanaProcessingOrder): VegaCoffeeFgrnItems {
        var offline = VegaCoffeeFgrnItems()
        offline.netWeight = poOfflineList.netWeight
        offline.auart = poOfflineList.auart
        offline.processOrderNo = poOfflineList.processOrderNo
        offline.materialCode = poOfflineList.materialCode
        offline.materialName = poOfflineList.materialName
        offline.meins = poOfflineList.unitsOfMeasure!!
        offline.plant = poOfflineList.plant
        offline.startDate = poOfflineList.startDate
        offline.storageLocationCode = poOfflineList.storageLocationCode
        offline.vendor = poOfflineList.vendor
        offline.unitsOfMeasure = poOfflineList.unitsOfMeasure
        offline.rfgrnTotal = poOfflineList.rfgrnTotal
        offline.rminTotal = poOfflineList.rminTotal
        return offline
    }

    private fun moveToNext(offlineFgrnList: List<VegaCoffeeFgrnItems>) {
        if (isOnline()) {
            when (getCurrentFragment()) {
                is VegaGhanaFgrnPODetailsFragment -> when (offlineFgrnList.size > 0) {
                    true -> callBack?.replaceFgrnFragment(FRAG_PENDING, currentPo, "", vegaStage)
                    false -> callBack?.replaceFgrnFragment(FRAG_GRADES, currentPo, "", vegaStage)
                }
            }
        } else {
            callBack?.replaceFgrnFragment(FRAG_GRADES, currentPo, "", vegaStage)
        }

    }

    fun getCurrentFragment(): Fragment {
        return activity?.supportFragmentManager?.findFragmentById(R.id.flProcessing)!!
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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
    }*/

    private fun updageStageUI(stageList: List<VegaProcessingStage>) {
        val stage = ArrayList<VegaProcessingStage>()
        val stageInitItem = VegaProcessingStage()
        stageInitItem.processName = getString(R.string.select_stage)
        stage.add(stageInitItem)
        stage.addAll(stageList)
        val stageListData = stage.map { data -> data.processName }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_ghana_processing_rmin_grade, stageListData)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStage.adapter = stageAdapter

        if(stageListData.size == 2){
            var preSelected = stageListData[1]
            binding.spStage.setSelection(stageListData.indexOf(preSelected))
            stage.forEachIndexed { index, vegaProcessingStage ->
                if (preSelected == vegaProcessingStage.processName) {
                    vegaStage = vegaProcessingStage
                    stageFevor = vegaProcessingStage.fevor
                    cfgNo = vegaProcessingStage.cfgNo
                    auart = vegaProcessingStage.auart.toString()
                    currentPo.stageFevor = vegaProcessingStage.fevor
                    currentPo.cfgNo = vegaProcessingStage.cfgNo
                    currentPo.auart = vegaProcessingStage.auart
                    if (AppUtils.isOnline()) {
                        vm.fetchFgrnPoDetailsList(stageFevor, cfgNo, auart)
                    } else {
                        vm.getProcessOrders()
                    }
                }
            }
        }
        else {
            /*var defaultposition = 0
        stageListData.forEachIndexed { index, s -> if (s.equals("RCN Drying process")) defaultposition = index }
        binding.spStage.setSelection(defaultposition)*/
            binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    stage.forEachIndexed { index, vegaProcessingStage ->
                        if (stageListData[position] == vegaProcessingStage.processName) {
                            vegaStage = vegaProcessingStage
                            stageFevor = vegaProcessingStage.fevor
                            cfgNo = vegaProcessingStage.cfgNo
                            auart = vegaProcessingStage.auart.toString()
                            currentPo.stageFevor = vegaProcessingStage.fevor
                            currentPo.cfgNo = vegaProcessingStage.cfgNo
                            currentPo.auart = vegaProcessingStage.auart
                            if (!vegaProcessingStage.processName.equals(getString(R.string.select_stage)))
                                if (AppUtils.isOnline()) {
                                    vm.fetchFgrnPoDetailsList(stageFevor, cfgNo, auart)
                                } else {
                                    vm.getProcessOrders()
                                }
                        }
                    }
                }
            }
        }
    }

    private fun updateFgrnPoUI(response: Resource<GenericReqAndResp<List<VegaCoffeeFgrnItems>>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                val datalist =
                                    it.filter { !it.rminList.isNullOrEmpty() } as MutableList<VegaCoffeeFgrnItems>
                                storageList = datalist.asReversed()
                                storageList.forEachIndexed { index, s ->
                                    storageList[index].netWeight = storageList[index].netWeight.toString()
                                }
                                customLocationList.forEach { item ->
                                    var tempList = storageList.filter { item.procureLocationCode == it.storageLocationCode!! }
                                    poList.addAll(tempList)
                                }
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

    private fun updateAdapter(poList: MutableList<VegaCoffeeFgrnItems>) {
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
        binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
        binding.rvPoList.setUp(poList, R.layout.item_vega_ghana_fgrn_po_details, { item, pos ->
            tvPoNo.text = item.processOrderNo
            tv_material_name.text = item.materialName
            val weightToProcess =
                weightToProcess(item.rminList, item.rfgrnList, item.unitsOfMeasure)
            val weight = DecimalFormat("#########.###").format(weightToProcess).toString()
            tvWeight.text = item.netWeight.plus(" ").plus("MT")
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
            val weightToProcess = weightToProcess(
                this.rminList,
                this.rfgrnList,
                this.unitsOfMeasure
            )
            val weight = DecimalFormat("#########.###").format(weightToProcess).toString()
            currentPo.weight = currentPo.netWeight
            currentPo.cfgNo = currentPo1.cfgNo
            currentPo.stageFevor = currentPo1.stageFevor
            currentPo.auart = currentPo1.auart
            currentPo.unitsOfMeasure = this.unitsOfMeasure
            currentPo.rfgrnTotal = currentPo.rfgrnTotal!!

            vm.fetchOfflineFgrnList(this.processOrderNo)
        })
    }

    fun prepareData(quality: List<VegaGhanaOfflineRmin>) {
        quality.forEach {
            it.rminLot.forEach { it1 ->
                val qualityParams = VegaCoffeeFgrnItems()
                qualityParams.processOrderNo = it1.rminTempId
                qualityParams.netWeight = it1.netWeight
                qualityParams.materialName = it1.materialName
                qualityParams.materialCode = it1.materialCode
                qualityParams.rminId = it1.rminTempId
                qualityParams.rfgrnTotal = "0.0"
                qualityParams.startDate = it1.startTime
                poList.add(qualityParams)
            }
        }

    }
}
