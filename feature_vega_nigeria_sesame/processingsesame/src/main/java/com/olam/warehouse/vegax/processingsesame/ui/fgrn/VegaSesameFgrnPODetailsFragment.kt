package com.olam.warehouse.vegax.processingsesame.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.databinding.FragmentVegaSesameFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processingsesame.databinding.ItemVegaSesameFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processingsesame.utils.FRAG_GRADES
import com.olam.warehouse.vegax.processingsesame.utils.FRAG_PENDING
import com.olam.warehouse.vegax.processingsesame.utils.convertMtToKg
import com.olam.warehouse.vegax.processingsesame.utils.weightToProcess
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.DecimalFormat

class VegaSesameFgrnPODetailsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_sesame_fgrn_po_details
    private lateinit var binding: FragmentVegaSesameFgrnPoDetailsBinding
    private val vm: VegaSesameFgrnViewModel by viewModel()
    private var poList = mutableListOf<VegaCoffeeFgrnItems>()
    private var poListSort = mutableListOf<VegaCoffeeFgrnItems>()
    private val mSearchList = arrayListOf<VegaCoffeeFgrnItems>()
    private var currentPo = VegaCoffeeFgrnItems()
    private var stageFevor: String = ""
    private var cfgNo: String = ""
    private var auart: String = ""
    private var callBack: CallBack? = null
    private var vegaStage = VegaProcessingStage()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String, vegaStage: VegaProcessingStage)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaSesameFgrnPODetailsFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search Po Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaSesameFgrnPoDetailsBinding.inflate(layoutInflater)
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
        context?.let {
            getActionBtnChangedView(binding.tvHeadPoDetail, it, false)
        }
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

    private fun moveToNext(offlineFgrnList: List<VegaCoffeeFgrnItems>) {
        when (getCurrentFragment()) {
            is VegaSesameFgrnPODetailsFragment -> when (offlineFgrnList.size > 0) {
                true -> callBack?.replaceFgrnFragment(FRAG_PENDING, currentPo, "",vegaStage)
                false -> callBack?.replaceFgrnFragment(FRAG_GRADES, currentPo, "",vegaStage)
            }
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
    }*/

    private fun updageStageUI(stageList: List<VegaProcessingStage>) {
        val stage = ArrayList<VegaProcessingStage>()
        val stageInitItem = VegaProcessingStage()
        stageInitItem.processName = getString(R.string.select_stage)
        stage.add(stageInitItem)
        stage.addAll(stageList)
        val stageListData = stage.map { data -> data.processName }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_sesame_processing_rmin_grade, stageListData)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStage.adapter = stageAdapter
        /*var defaultposition = 0
        stageListData.forEachIndexed { index, s -> if (s.equals("RCN Drying process")) defaultposition = index }
        binding.spStage.setSelection(defaultposition)*/
        binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {/*Nothing to select*/}
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
                            vm.fetchFgrnPoDetailsList(stageFevor, cfgNo, auart)
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
                                poList = datalist.asReversed()
                                poList.forEachIndexed { index, s ->
                                    poList[index].netWeight = convertMtToKg(
                                        poList[index].netWeight.toString(),
                                        poList[index].unitsOfMeasure.toString()
                                    )
                                }
                                updateAdapter(poList)
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
        binding.rvPoList.setUpAdapter(
            poList,
            R.layout.item_vega_sesame_fgrn_po_details,
            ItemVegaSesameFgrnPoDetailsBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvPoNo.text = item.processOrderNo
                bindItem.tvMaterialName.text = item.materialName
                val weightToProcess =
                    weightToProcess(item.rminList, item.rfgrnList, item.unitsOfMeasure)
                val weight = convertMtToKg(
                    DecimalFormat("#########.###").format(weightToProcess).toString(),
                    item.unitsOfMeasure.toString()
                )
                bindItem.tvWeight.text = weight.plus(" ").plus("KG")
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
                val weight = convertMtToKg(
                    DecimalFormat("#########.###").format(weightToProcess).toString(),
                    this.unitsOfMeasure.toString()
                )
                currentPo.weight = weight
                currentPo.cfgNo = currentPo1.cfgNo
                currentPo.stageFevor = currentPo1.stageFevor
                currentPo.auart = currentPo1.auart
                currentPo.unitsOfMeasure = this.unitsOfMeasure
                currentPo.rfgrnTotal =
                    convertMtToKg(currentPo.rfgrnTotal!!, this.unitsOfMeasure.toString())

                vm.fetchOfflineFgrnList(this.processOrderNo)
            })
    }
}
