package com.olam.warehouse.vegax.processingindiacoffee.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeProcessingRminBomPost
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentPoBomSelectLayoutBinding
import com.olam.warehouse.vegax.processingindiacoffee.databinding.ItemBomLayoutBinding
import com.olam.warehouse.vegax.processingindiacoffee.di.injectCocoaProcessingFeature
import com.olam.warehouse.vegax.processingindiacoffee.utils.ADDLOT
import com.olam.warehouse.vegax.processingindiacoffee.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingindiacoffee.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeRminPoBomSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_po_bom_select_layout
    private lateinit var binding: FragmentPoBomSelectLayoutBinding

    private var materialCodes = arrayListOf<String>()
    private var materialNames = arrayListOf<String>()
    private var callBack: CallBack? = null
    private val vm: VegaIndiaCoffeeRminViewModel by viewModel()
    private var title: String? = ""
    private var isPoSelection: Boolean = true
    private var model: VegaCocoaRminProcessing? = null
    private val mSearchList = mutableListOf<VegaFgrnProcessingOrder>()
    private var poList = ArrayList<VegaFgrnProcessingOrder>()
    private var materialCode1: String = ""
    private var bomList1 = listOf<VegaProcessingRminBoms>()
    private var isFromSearch= false

    interface CallBack {
        fun replaceAddLotFragment(
            fragment: String,
            flag: Boolean,
            model: VegaCocoaRminProcessing,
            isPoSelection: Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(title: String, isPo: Boolean, model: VegaCocoaRminProcessing) =
            VegaIndiaCoffeeRminPoBomSelectFragment().putArgs {
                putString("title", title)
                putBoolean("isPo", isPo)
                putParcelable(MODEL_BUNDLE, model)
            }

        const val SEARCH_HINT_TEXT = "Search Po Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentPoBomSelectLayoutBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        title = arguments?.getString("title")
        isPoSelection = arguments?.getBoolean("isPo") ?: false
        model = arguments?.getParcelable(MODEL_BUNDLE)
        materialCode1 = model?.materialCode.toString()
        materialCodes = model?.materialCodes ?: ArrayList()
        materialNames = model?.materialNames ?: ArrayList()

    }

    private fun initUI() {
        binding.tvTitle.text = title
        if (isPoSelection) {
            vm.fetchFgrnPoDetailsList(model?.auartNo ?: "", model?.cgfNo ?: "", model?.foreverNo ?: "")
            binding.tvTitle.text = getString(R.string.select_po)
        } else {
            vm.fetchBomList(
                VegaIndiaCoffeeProcessingRminBomPost(
                    model?.cgfNo ?: "",
                    model?.foreverNo ?: "",
                    model?.materialCode ?: "",
                    model?.materialCodes ?: ArrayList(),
                    getCurrentKey(),
                    getPlantDetails()
                )
            )
            binding.tvTitle.text = getString(R.string.select_bom)
        }
        vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
        vm.bomList.observe(viewLifecycleOwner, Observer { updateBom(it) })
        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.deleteAllLot(
                model?.poNumber ?: "",
                model?.cgfNo ?: "",
                model?.bom ?: "",
                model?.materialName ?: ""
            )
        }

        vm.vendor.observe(viewLifecycleOwner, Observer { vendorList->
            poList.forEach { po->
              val list= vendorList.filter { it.vendorCode == po.vendor }
                if(list.isNotEmpty()){
                    po.vendorName = list[0].vendorName
                }
            }
            hideLoading()
            setupAdapterPo(poList)
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        injectCocoaProcessingFeature()
        //initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/rmin/VegaCocoaRminPoBomSelectFragment")
            .title("Processing Cocoa").with(tracker)
    }

    private fun updateBom(response: Resource<GenericReqAndResp<VegaProcessingRminBom>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            bomList1 = it.data?.data?.boms!!
                            updateBomList(it.data?.data?.boms)
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun updateFgrnPoUI(response: Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                poList.clear()
                                it.forEach { item ->
                                    if (getCurrentKey().split("_")[1]
                                            .contains("NI")
                                    ) {
                                        var currentStage = model?.stage.toString()
                                        if (item.materialName?.equals(currentStage) == true) {
                                            val rmindata = item.rmin?.filter { rmin ->
                                                rmin.materialCode!!.contains(model?.materialCode.toString())
                                            }
                                            rmindata?.forEach { it1 ->
                                                item.materialCode = it1.materialCode
                                                item.materialName = it1.materialName
                                                poList.add(item)
                                            }
                                        }
                                        // poList.add(item)

                                    } else {

                                        /*Dwall- 5156 : fix, For Re-Proceesing-CHIK, there are 2 hifen,
                                        * so the condition fails because split condition is [1], so we changed the split condition to [size -1]*/
                                        var stageName = model?.stage.toString().split("-")
                                        var currentStage = stageName[stageName.size - 1]

                                        val rmindata =
                                            item.rmin?.filter { rmin ->
                                                var mat=rmin.materialCode.toString().replaceRange(0,6,"")
                                                model?.materialCodes?.contains(mat) == true
                                            }

                                        if(model?.stage.toString().contains("Re-processing") || model?.stage.toString().contains("Reprocessing")){
                                           // poList.add(item)
                                            if(rmindata?.isNotEmpty() == true){
                                                item.materialCode = rmindata[0].materialCode
                                                item.materialName = rmindata[0].materialName
                                                poList.add(item)
                                            }

                                        }else if(item.versionId?.equals(currentStage) == true) {
                                            if(rmindata?.isNotEmpty() == true){
                                                item.materialCode = rmindata[0].materialCode
                                                item.materialName = rmindata[0].materialName
                                                poList.add(item)
                                            }                                        }

                                    }
                                }

                                updateDetails(poList)
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


    private fun setupAdapterPo(poList: List<VegaFgrnProcessingOrder>){
        if (poList.size == 1 && !isFromSearch) {
            isFromSearch= false
            model?.poNumber = poList[0].processOrderNo
            model?.baseMaterialCode = poList[0].materialCode
            model?.baseMaterialName = poList[0].materialName
            model?.vendorCode = poList[0].vendor.toString()
            activity?.supportFragmentManager?.popBackStackImmediate()
            callBack?.replaceAddLotFragment(ADDLOT, false, model!!, isPoSelection)

        } else {

            //val dataList = poList.filter { !it.rminList.isNullOrEmpty() } as MutableList<VegaFgrnProcessingOrder>
            var data = poList
            when {
                data.isEmpty() -> {
                    binding.tvError.text = getString(R.string.process_not_avail)
                    binding.tvError.visibility = View.VISIBLE
                    binding.rvPoList.visibility = View.GONE
                }
                else -> {
                    binding.tvError.visibility = View.GONE
                    binding.rvPoList.visibility = View.VISIBLE
                    data = data.sortedByDescending {
                        it.startDate?.split('(', ')')?.get(1)?.let { it1 ->
                            DateUtils.getUTCDateTime(
                                it1,
                                App.getAppContext()
                            )
                        }
                    } as MutableList<VegaFgrnProcessingOrder>
                }
            }

            binding.rvPoList.setUpAdapter(
                data as MutableList,
                R.layout.item_bom_layout,
                ItemBomLayoutBinding::inflate,
                { item, pos, bindItem ->
                    if (pos % 2 == 0) {
                        bindItem.clMain.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    } else {
                        bindItem.clMain.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    }
                    bindItem.tvBom.text = context.getString(R.string.pono)
                    bindItem.tvInputGrade.text = context.getString(R.string.weight_in_process)
                    bindItem.tvOutputGrade.text = context.getString(R.string.dry_start_date)

                    if(item.vendorName?.isNotEmpty() == true) {
                        bindItem.tvVendor.visible()
                        bindItem.tvVendorName.visible()
                        bindItem.tvVendorName.text = item.vendorName
                    }

                    bindItem.tvBomValue.text = item.processOrderNo
                    /*val weightToProcess = weightToProcess(item.rminList, item.rfgrnList)
                    val weight = DecimalFormat("#########.###").format(weightToProcess).toString()*/
                    bindItem.tvInputGradeValue.text =
                        item.netWeight.plus(" ").plus(item.unitsOfMeasure.toString())
                    val times = item.startDate?.split('(', ')')
                    bindItem.tvOutputValue.text = times?.get(1).let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTime(
                                it2,
                                App.getAppContext()
                            )
                        }
                    }

                }, {
                    model?.poNumber = this.processOrderNo
                    model?.baseMaterialCode = poList[0].materialCode
                    model?.baseMaterialName = poList[0].materialName
                    model?.vendorCode = this.vendor.toString()
                    model?.weightToProcess =
                        this.netWeight.plus(" ").plus(this.unitsOfMeasure.toString())
                    callBack?.replaceAddLotFragment(ADDLOT, false, model!!, isPoSelection)
                })
        }

    }


    private fun updateDetails(poList: List<VegaFgrnProcessingOrder>) {
        if(getCurrentKey().contains("VEGA_NI") && poList.isNotEmpty()){
             if(poList.get(0).materialName?.contains("Toll",true) == true){
                 showLoading()
                 vm.getVendors()
             }else setupAdapterPo(poList)
        }else setupAdapterPo(poList)

    }

    private fun weightToProcess(rminList: List<VegaProcessingList>?, rfgrnList: List<VegaProcessingList>?): Double {
        val rminWeight: Double = when {
            !rminList.isNullOrEmpty() -> {
                when {
                    rminList[0].netWeight.toString().isEmpty() -> 0.0
                    else -> rminList.sumOf { it.netWeight!!.toDouble() }
                }
            }
            else -> 0.0
        }
        val fgrnWeight: Double = when {
            !rfgrnList.isNullOrEmpty() -> {
                when {
                    rfgrnList[0].netWeight.toString().isEmpty() -> 0.0
                    else -> rfgrnList.sumOf { it.netWeight!!.toDouble() }
                }
            }
            else -> 0.0
        }
        return rminWeight - fgrnWeight
    }

    private fun updateBomList(boms: List<VegaProcessingRminBoms>?) {
        boms?.forEach { it.baseMaterialCode = model?.materialCode }
        if (boms?.size == 1) {
            model?.bom = boms[0].materialCode
            model?.materialCode = bomList1[0].materialCode
            model?.baseMaterialCode = boms[0].baseMaterialCode
            model?.baseMaterialName = model?.materialName
            var currentStage = ""
            if (model?.stage!!.contains("Re-processing", true))
                currentStage = model?.stage.toString().split("-")[2]
            else
                currentStage = model?.stage.toString().split("-")[1]
            if (getCurrentKey().split("_")[1].contains("NI")) {
                model?.versionId = boms[0].versionId
            } else {
                model?.versionId = currentStage
            }
            activity?.supportFragmentManager?.popBackStackImmediate()
            callBack?.replaceAddLotFragment(ADDLOT, false, model!!, isPoSelection)

        } else {
            val bomList = boms?.toMutableList() ?: mutableListOf()
            binding.rvPoList.setUpAdapter(
                bomList,
                R.layout.item_bom_layout,
                ItemBomLayoutBinding::inflate,
                { it, pos, bindItem ->
                    if (pos % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    }
                    bindItem.tvBom.text = context.getString(R.string.b_o_m)
                    bindItem.tvInputGrade.text = context.getString(R.string.input_grade)
                    bindItem.tvOutputGrade.text = context.getString(R.string.output_grade)
                    bindItem.tvBomValue.text = it.cfgno
                    bindItem.tvInputGradeValue.text = model?.materialName
                    bindItem.tvOutputValue.text = it.materialName.toString()

                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        if (model?.materialCode!!.isNotEmpty()) {
                            for ((kcode, kval) in model?.materialCode!!.withIndex()) {
                                if (it.inputMaterialCode!!.equals(kval))
                                    bindItem.tvInputGradeValue.text =
                                        model?.materialNames!!.get(kcode).toString()
                            }
                        }

                    } else {
                        if (model?.materialCodes!!.size > 0) {
                            for ((kcode, kval) in model?.materialCodes!!.withIndex()) {
                                if (it.inputMaterialCode!!.equals(kval))
                                    bindItem.tvInputGradeValue.text =
                                        model?.materialNames!!.get(kcode).toString()
                            }
                        }
                    }

                    bindItem.clMain.setOnClickListener { view ->
                        model?.bom = it.cfgno
                        model?.poNumber = ""
                        model?.baseMaterialCode = it.baseMaterialCode
                        model?.materialCode = bomList1[pos].materialCode.toString()
                        model?.baseMaterialName = model?.materialName
                        model?.versionId = it.versionId
                        callBack?.replaceAddLotFragment(ADDLOT, false, model!!, isPoSelection)
                    }
                }, {
                    /* model?.bom = this.cfgno
                 model?.poNumber = ""
                 model?.baseMaterialCode = this.baseMaterialCode
                 model?.materialCode = this.materialCode ?: ""
                 model?.baseMaterialName = model?.materialName
                 model?.versionId = this.versionId
                 callBack?.replaceAddLotFragment(ADDLOT, false, model!!,isPoSelection)*/
                })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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
                            updateDetails(poList)
                        } else {
                            mSearchList.clear()
                            poList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.processOrderNo.contains(text) || qtyWb.vendorName?.contains(text,true) == true) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            isFromSearch= true
                            setupAdapterPo(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }
}
