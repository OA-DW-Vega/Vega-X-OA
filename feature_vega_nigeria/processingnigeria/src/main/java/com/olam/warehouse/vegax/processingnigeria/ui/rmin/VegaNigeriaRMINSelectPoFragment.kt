package com.olam.warehouse.vegax.processingnigeria.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.CustomFilterDialog
import com.olam.warehouse.presentation.ui.widget.RecyclerViewItemClickListener
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentVegaNigeriaRminSelectPoBinding
import com.olam.warehouse.vegax.processingnigeria.databinding.ItemBomLayoutBinding
import com.olam.warehouse.vegax.processingnigeria.databinding.ItemVegaNigeriaFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processingnigeria.di.injectNigeriaProcessingFeature
import com.olam.warehouse.vegax.processingnigeria.utils.POBOM
import com.olam.warehouse.vegax.processingnigeria.utils.RMIN_GRADES
import com.olam.warehouse.vegax.processingnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaRMINSelectPoFragment : BaseFragment(), RecyclerViewItemClickListener {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_rmin_select_po
    private lateinit var binding: FragmentVegaNigeriaRminSelectPoBinding

    private var callBack: CallBack? = null
    private val vm: VegaNigeriaRminViewModel by viewModel()
    private var materialCode: String = ""
    private var stageFevor: String = ""
    private var cfgNo: String? = ""
    private var materialName: String? = ""
    private var auartNo: String? = ""
    private var model: VegaCoffeeRminProcessing? = null
    private var currentStage: String = ""
    private var customDialog: CustomFilterDialog? = null
    private var stageValue = ""
    val grade = ArrayList<VegaMaterial>()
    var gradeListData = ArrayList<String>()
    private var poList = ArrayList<VegaFgrnProcessingOrder>()
    private var poListSort = mutableListOf<VegaFgrnProcessingOrder>()
    private var configStage: String = ""
    private var seivingSelection = 0
    private var bomList1 = listOf<VegaProcessingRminBoms>()
    private var isCompliantMaterial: Boolean = false
    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing, flag: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaNigeriaRMINSelectPoFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaRminSelectPoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        injectNigeriaProcessingFeature()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingnigeria/ui/rmin/VegaNigeriaRMINSelectPoFragment")
            .title("Processing Coffee").with(tracker)
    }

    private fun initUI() {
        showTTDialog()
        vm.grades.observe(viewLifecycleOwner, Observer { updateGradeUI(it) })
        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
        //vm.fetchGrades()
        vm.fetchStages()
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
        binding.cbPo.setOnCheckedChangeListener { it, isChecked -> updatePoSelection(isChecked, 1) }
        binding.cbExPo.setOnCheckedChangeListener { it, isChecked ->
            updatePoSelection(
                isChecked,
                2
            )
        }
        //vm.bomList.observe(viewLifecycleOwner, Observer { updateBom(it) })
    }

    private var isAutoPo: Boolean = false
    private var isExistPo: Boolean = false
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        binding.clPo.visible()
        isAutoPo = false
        isExistPo = false
        var configItemList = configItems?.filter { it.processStageName.equals(currentStage) }
        configItemList?.forEach {
            when (it.process) {
                ConfigItems.AUTO_PO.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            isAutoPo = it.processStageName.equals(currentStage)
                            configStage = it.processStageName
                        }
                        it.applicable?.contains("N")!! -> isAutoPo = false
                    }
                }
                ConfigItems.SELECT_PO.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            isExistPo = it.processStageName.equals(currentStage)
                            configStage = it.processStageName
                        }
                        it.applicable?.contains("N")!! -> isExistPo = false
                    }
                }
            }
        }
        when {
            isAutoPo && isExistPo -> {
                binding.tvSelectPo.visible()
                binding.tvOr.visible()
                binding.cbPo.visible()
                binding.cbExPo.visible()
                binding.tvSelectExPo.visible()
                binding.clPo.visible()
                enableProceed(!configStage.equals("null") && configStage != currentStage && materialCode.isNotEmpty())
            }
            isAutoPo -> {
                disable()
                binding.tvSelectPo.visible()
                binding.cbPo.visible()
                binding.clPo.visible()
                enableProceed(!configStage.equals("null") && configStage != currentStage && materialCode.isNotEmpty())
            }
            isExistPo -> {
                disable()
                binding.cbExPo.visible()
                binding.tvSelectExPo.visible()
                binding.clPo.visible()
                enableProceed(!configStage.equals("null") && configStage != currentStage && materialCode.isNotEmpty())
            }
            else -> disable()
        }

    }

    private fun enableProceed(enabled: Boolean) {
        if (enabled) {
            binding.rvPoList.visibility = View.VISIBLE
        } else {
            binding.rvPoList.visibility = View.GONE
        }
    }

    private fun disable() {
        binding.clPo.gone()
        binding.tvSelectPo.gone()
        binding.tvOr.gone()
        binding.cbPo.gone()
        binding.cbExPo.gone()
        binding.tvSelectExPo.gone()
        enableProceed(configStage != currentStage && materialCode.isNotEmpty())
    }

    private fun updatePoSelection(selected: Boolean, position: Int) {
        if (selected) {
            when (position) {
                1 -> {
                    binding.cbExPo.isChecked = false
                    seivingSelection = 1
                }
                2 -> {
                    binding.cbPo.isChecked = false
                    seivingSelection = 2
                }
            }

            if (seivingSelection != 1) {
                binding.textGrade.visibility = View.GONE
                binding.frameGrade.visibility = View.GONE
                vm.fetchFgrnPoDetailsList(auartNo ?: "", cfgNo ?: "", stageFevor)
            } else {
                binding.textGrade.visibility = View.VISIBLE
                binding.frameGrade.visibility = View.VISIBLE
                binding.spGrade.setOnClickListener { spinnerClick(gradeListData) }
                /*vm.fetchBomList(
                    VegaNigeriaRminBomPost(
                        model?.cgfNo ?: "",
                        model?.foreverNo ?: "",
                        model?.materialCode ?: "",
                        getCurrentKey(),
                        getPlantDetails()
                    )
                )*/
            }
        }
        if (stageFevor.isNotEmpty() && materialCode.isNotEmpty()) enableProceed(selected)
    }

    private fun spinnerClick(items: ArrayList<String>) {
        customDialog = CustomFilterDialog(items, requireContext(), this)
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: Int, isWH: Boolean) {
        grade.forEachIndexed { index, vegaMaterial ->
            if (gradeListData[data] == vegaMaterial.materialName) {
                binding.spGrade.text = vegaMaterial.materialName
                materialCode = vegaMaterial.materialCode
                materialName = vegaMaterial.materialName
                if (configStage != currentStage)
                    enableProceed(stageFevor.isNotEmpty() && materialCode.isNotEmpty())
                else
                    enableProceed(stageFevor.isNotEmpty() && seivingSelection != 0 && materialCode.isNotEmpty())


                navigateToNext(currentStage)
            }
        }
        if (customDialog != null) {
            customDialog!!.dismiss()
        }
    }

    private fun navigateToNext(stage: String) {
        when (stage) {
            configStage -> {
                callBack?.replaceFragment(
                    POBOM,
                    VegaCoffeeRminProcessing(
                        materialCode,
                        materialName ?: "",
                        stage,
                        cfgNo!!,
                        stageFevor, auartNo = auartNo
                    ),
                    seivingSelection != 1
                )
            }
            else -> {

                callBack?.replaceFragment(
                    POBOM,
                    VegaCoffeeRminProcessing(
                        materialCode,
                        materialName ?: "",
                        stage,
                        cfgNo!!,
                        stageFevor, auartNo = auartNo
                    ),
                    true
                )
            }
        }
    }

    private fun updateGradeUI(gradeList: List<VegaMaterial>) {
        grade.addAll(gradeList.distinct().filter { if(isCompliantMaterial)it.complainceFlag.equals(Constants.COMPLAINT) else it.complainceFlag.equals(Constants.NON_COMPLAINT) })
        gradeListData = grade.distinctBy { it.materialName }
            .map { data -> data.materialName.toString() } as ArrayList
    }

    private fun updageStageUI(stageList: List<VegaProcessingStage>) {
        val stage = ArrayList<VegaProcessingStage>()
        val stageInitItem = VegaProcessingStage()
        stageInitItem.processName = getString(R.string.select_stage)
        stage.add(stageInitItem)
        stage.addAll(stageList)
        val stageListData = stage.map { data -> data.processName }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_nigeria_processing_rmin_grade, stageListData)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStage.adapter = stageAdapter
        var defaultposition = 0
        stageListData.forEachIndexed { index, s -> if (s.equals("RCN Drying process")) defaultposition = index }
        binding.spStage.setSelection(defaultposition)
        binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                stage.forEachIndexed { index, vegaProcessingStage ->
                    if (stageListData[position] == vegaProcessingStage.processName) {
                        stageFevor = vegaProcessingStage.fevor
                        cfgNo = vegaProcessingStage.cfgNo
                        auartNo = vegaProcessingStage.auart
                        currentStage = vegaProcessingStage.processName ?: ""
                        stageValue = stageListData[position] ?: ""
                        // disableBomSelect(currentStage)
                        //enableProceed(position > 0 && SEIVING != vegaProcessingStage.processName && materialCode.isNotEmpty())
                        if (position > 0) vm.getConfigItems(UserRoles.PROCESSING.role)
                        //if (position > 0) vm.fetchFgrnPoDetailsList(auartNo ?: "", cfgNo ?: "", stageFevor)
                    }
                }
            }
        }
    }

    private fun navigateToNext(model: VegaCoffeeRminProcessing, stage: String) {
        when (stage) {
            configStage -> {

                callBack?.replaceFragment(
                    RMIN_GRADES, model, seivingSelection != 1,
                )
                /*callBack?.replaceFragment(
                    POBOM,
                    seivingSelection != 1,
                    VegaCocoaRminProcessing(
                        materialCode,
                        materialName ?: "",
                        stage,
                        cfgNo!!,
                        stageFevor, auartNo = auartNo
                    )
                )*/
            }
            else -> {

                callBack?.replaceFragment(
                    RMIN_GRADES, model, true
                )
                /*callBack?.replaceFragment(
                    POBOM,
                    true,
                    VegaCocoaRminProcessing(
                        materialCode,
                        materialName ?: "",
                        stage,
                        cfgNo!!,
                        stageFevor, auartNo = auartNo
                    )
                )*/
            }
        }


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

    private fun updateBomList(boms: List<VegaProcessingRminBoms>?) {
        boms?.forEach { it.baseMaterialCode = model?.materialCode }

        var data = boms
        binding.tvList.text = getString(R.string.select_bom)
        when {
            data?.isEmpty() == true -> {
                binding.tvError.text = getString(R.string.bom_not_avail)
                binding.tvError.visibility = View.VISIBLE
                binding.rvPoList.visibility = View.GONE
            }
            else -> {
                binding.tvError.visibility = View.GONE
                binding.rvPoList.visibility = View.VISIBLE
            }
        }

        /* if (boms?.size == 1) {
             model?.bom = boms[0].materialCode ?: ""
             model?.materialCode = bomList1[0].materialCode ?: ""
             model?.baseMaterialCode = boms[0].baseMaterialCode
             model?.baseMaterialName = model?.materialName
             var currentStage =  model?.stage.toString().split("-")[1]
             //model?.versionId = boms[0].versionId
             model?.versionId = currentStage
             activity?.supportFragmentManager?.popBackStackImmediate()
             callBack?.replaceAddLotFragment(ADDLOT, false, model!!,seivingSelection != 1)

         } else {*/
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
                bindItem.clMain.setOnClickListener { view ->
                    model?.bom = it.cfgno
                    model?.poNumber = ""
                    model?.baseMaterialCode = it.baseMaterialCode
                    model?.materialCode = bomList1[pos].materialCode.toString()
                    model?.baseMaterialName = model?.materialName
                    model?.versionId = it.versionId
                    callBack?.replaceFragment(
                        RMIN_GRADES, model!!, seivingSelection != 1,
                    )
                    // callBack?.replaceAddLotFragment(ADDLOT, false, model!!,seivingSelection != 1)
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
        //}
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
                                var material = grade.map { it.materialCode }
                                poList.addAll(it.filter { material.contains(it.materialCode?.takeLast(12)) })
                                poList.forEachIndexed { index, s ->
                                    when (s.unitsOfMeasure) {
                                        "KG" ->
                                            poList[index].netWeight = poList[index].netWeight.toString()
                                        "MT" ->
                                            poList[index].netWeight = poList[index].netWeight.toString()
                                    }
                                }
                                /*val data =
                                    it.filter { item -> item.materialCode!!.contains(model?.materialCode.toString()) }
                                poList.addAll(data)*/
                                updatePoListDetails(poList)
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

    private fun updatePoListDetails(poList: List<VegaFgrnProcessingOrder>) {
        var data = poList
        binding.tvList.text = getString(R.string.rmin_po_details)
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

        binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
        binding.rvPoList.setUpAdapter(
            data as MutableList,
            R.layout.item_vega_nigeria_fgrn_po_details,
            ItemVegaNigeriaFgrnPoDetailsBinding::inflate,
            { item, pos, bindItem ->

                bindItem.tvPoNo.text = item.processOrderNo
                bindItem.tvMaterialName.text = item.materialName
                bindItem.tvWeight.text = item.netWeight.plus(" ").plus(item.unitsOfMeasure)
                val times = item.startDate?.split('(', ')')
                bindItem.tvDate.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }

            },
            {
                model = VegaCoffeeRminProcessing()
                model?.auartNo = auartNo
                model?.foreverNo = stageFevor
                model?.cgfNo = cfgNo ?: ""
                model?.poNumber = this.processOrderNo
                model?.baseMaterialCode = this.materialCode
                model?.baseMaterialName = this.materialName
                model?.stage = stageValue
                model?.vendor = vendor
                navigateToNext(model ?: VegaCoffeeRminProcessing(), currentStage)
            })
    }

    private fun showTTDialog() {
        MaterialDialog(requireContext()).show {
            cancelOnTouchOutside(false)
            message(com.olam.warehouse.login.R.string.select_procurement_type)
            UIUtils.getTTDialogOnlyForMaterial(
                this,
                "",
                "",

                {
                        isComplaint, isThirdParty, isFarmerLessTransaction->  if(isComplaint) isCompliantMaterial = true else false
                    vm.fetchGrades()


                },
                { dismiss() },
                false)
        }

    }
}
