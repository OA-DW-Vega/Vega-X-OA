package com.olam.warehouse.vegax.processingindiacoffee.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.CustomFilterDialog
import com.olam.warehouse.presentation.ui.widget.RecyclerViewItemClickListener
import com.olam.warehouse.presentation.ui.widget.VegaCommonMultipleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaMultiSelectCommonListener
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaNicaRminMaterialProcessType
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaNicaRminProcessTypeModel
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentVegaIndiaCoffeeRminSelectProcessBinding
import com.olam.warehouse.vegax.processingindiacoffee.di.injectCocoaProcessingFeature
import com.olam.warehouse.vegax.processingindiacoffee.utils.POBOM
import com.olam.warehouse.vegax.processingindiacoffee.utils.SEIVING
import com.olam.warehouse.vegax.processingindiacoffee.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeRminSelectProcessFragment : BaseFragment(), VegaMultiSelectCommonListener,
    RecyclerViewItemClickListener {

    private  var materialBasedStage: List<VegaNicaRminMaterialProcessType> = emptyList()
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_rmin_select_process
    private lateinit var binding: FragmentVegaIndiaCoffeeRminSelectProcessBinding
    private var customDialog1: VegaCommonMultipleSelectDialogWithSearch? = null
    private var callBack: CallBack? = null
    private val vm: VegaIndiaCoffeeRminViewModel by viewModel()
    private var materialCode: String = ""
    private var stageFevor: String = ""
    private var cfgNo: String? = ""
    private var materialName: String? = ""
    private var auartNo: String? = ""
    private var model: VegaCocoaRminProcessing? = null
    private var currentStage: String = ""
    private var configStage: String = ""
    private var seivingSelection = 0
    private var customDialog: CustomFilterDialog? = null
    val grade = ArrayList<VegaMaterial>()
    var gradeListData = ArrayList<String>()
    var matrialNames = arrayListOf<String>()
    var matrialCodes = arrayListOf<String>()
    private var jsonData = mutableListOf<String>()
    var processTypeList = mutableListOf<VegaNicaRminMaterialProcessType>()
    var stage = ArrayList<VegaProcessingStage>()
    private var isCompliantMaterial: Boolean = false
    private var isThirdParty: Boolean = false

    interface CallBack {
        fun replaceFragment(fragment: String, isPoSelect: Boolean, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaIndiaCoffeeRminSelectProcessFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeRminSelectProcessBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        injectCocoaProcessingFeature()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/rmin/VegaCocoaRminSelectProcessFragment")
            .title("Processing Cocoa").with(tracker)
    }

    private fun initUI() {
       // binding.clPo.visible()
        showTTDialog()
        vm.grades.observe(viewLifecycleOwner, Observer { updateGradeUI(it) })
        vm.stages.observe(viewLifecycleOwner, Observer {
             stage.addAll(it)
            updageStageUI(it)
        })
        //vm.fetchGrades()
        vm.fetchStages()
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        binding.cbPo.setOnCheckedChangeListener { it, isChecked -> updatePoSelection(isChecked, 1) }
        binding.cbExPo.setOnCheckedChangeListener { it, isChecked -> updatePoSelection(isChecked, 2) }
        binding.btnProceed.setOnClickListener { navigateToNext(currentStage) }
        //disableBomSelect("")

        if(getCurrentKey().contains("VEGA") && getCurrentKey().contains("NI"))
            vm.getShiftRemarksItems(getCurrentKey())

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer {
            updateProcessType(it)
        })

        if(getCurrentKey().contains("VEGA_NI")){
            binding.tvGrade.gone()
            binding.tvMaterial.visible()
            binding.flGrade.gone()
            binding.flMaterial.visible()
        }

        binding.spMaterial.setOnClickListener {
            var materialList= ArrayList<String>()
            var localMaterial = emptyList<VegaMaterial>()
            if(processTypeList.isNotEmpty()){
            val list=  processTypeList.filter { it.STAGE_NAME==currentStage }
                if(list.isNotEmpty()) {
                    materialList = list[0].MATERIAL_LIST.toMutableList() as ArrayList<String>
                }
            }
            if(materialList.isNotEmpty()) {
                 localMaterial = grade.filter { materialList.contains(it.materialCode) }
            }else localMaterial = grade
            gradeListData = localMaterial.distinctBy { it.materialName }.map { data -> data.materialName.toString() } as ArrayList

            spinnerClick(gradeListData)
        }

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

    private fun disable() {
        binding.clPo.gone()
        binding.tvSelectPo.gone()
        binding.tvOr.gone()
        binding.cbPo.gone()
        binding.cbExPo.gone()
        binding.tvSelectExPo.gone()
        if (getCurrentKey().split("_")[1].contains("NI"))
            enableProceed(configStage != currentStage && materialCode.isNotEmpty())
        else
            enableProceed(configStage != currentStage && matrialCodes.isNotEmpty())
    }

    private fun showMultiSelectDialog(items: ArrayList<String>) {
        customDialog1 = VegaCommonMultipleSelectDialogWithSearch(
            "Select Grade", "grade", items, requireActivity(),
            this
        )
        customDialog1?.show()
        customDialog1?.setCanceledOnTouchOutside(false)
    }

    private fun updateGradeUI(gradeList: List<VegaMaterial>) {

        /*val gradeinit = VegaMaterial()
        gradeinit.materialName = getString(R.string.select_grade)
        grade.add(gradeinit)*/
        if(isThirdParty)
            grade.addAll(gradeList.distinct().filter {it.thirdPartyFlag.equals("X", true) })
        else
            grade.addAll(gradeList.distinct().filter { if(isCompliantMaterial)it.complainceFlag.equals(Constants.COMPLAINT) else it.complainceFlag.equals(
            Constants.NON_COMPLAINT) })
        if (!getCurrentKey().split("_")[1].contains("NI"))
            gradeListData =
                grade.map { data -> data.materialName.toString().plus(" - ").plus(data.materialCode) } as ArrayList

        binding.spGrade.setOnClickListener {
            showMultiSelectDialog(gradeListData)
        }

        /*val gradeAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_IndiaCoffee_processing_rmin_grade, gradeListData)
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spGrade.adapter = gradeAdapter
        var defaultposition = 0
        gradeListData.forEachIndexed { index, s -> if (s.equals("Ivory Coast Raw Cashew Nut")) defaultposition = index }
        binding.spGrade.setSelection(defaultposition)
        binding.spGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                grade.forEachIndexed { index, vegaMaterial ->
                    if (gradeListData[position] == vegaMaterial.materialName) {
                        materialCode = vegaMaterial.materialCode
                        materialName = vegaMaterial.materialName
                        if(SEIVING != currentStage)
                            enableProceed(position > 0 && (stageFevor.isNotEmpty()*//* && SEIVING != currentStage*//* && materialCode.isNotEmpty()))
                        else
                            enableProceed(position > 0 && (stageFevor.isNotEmpty() && seivingSelection != 0 && materialCode.isNotEmpty()))
                    }
                }
            }
        }*/
    }

    private fun spinnerClick(items: ArrayList<String>) {
        customDialog = CustomFilterDialog(items, requireContext(), this)
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun updageStageUI(stageList: List<VegaProcessingStage>) {
        val stageLocal = ArrayList<VegaProcessingStage>()
        val stageInitItem = VegaProcessingStage()
        stageInitItem.processName = getString(R.string.select_stage)
        stageLocal.add(stageInitItem)
        stageLocal.addAll(stageList)
        val stageListData = stageLocal.map { data -> data.processName }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_india_coffee_processing_rmin_grade, stageListData)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStage.adapter = stageAdapter
        var defaultposition = 0
        stageListData.forEachIndexed { index, s -> if (s.equals("RCN Drying process")) defaultposition = index }
        binding.spStage.setSelection(defaultposition,false)
        binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                stageSelection(position,stageLocal)
            }
        }
    }

    private fun stageSelection(position: Int, stageLocal: MutableList<VegaProcessingStage>){
        stageLocal.forEachIndexed { index, vegaProcessingStage ->
            if (stageLocal[position].processName == vegaProcessingStage.processName) {
                stageFevor = vegaProcessingStage.fevor
                cfgNo = vegaProcessingStage.cfgNo
                auartNo = vegaProcessingStage.auart
                currentStage = vegaProcessingStage.processName ?: ""
                // disableBomSelect(currentStage)
                //enableProceed(position > 0 && SEIVING != vegaProcessingStage.processName && materialCode.isNotEmpty())
                if (position > 0) vm.getConfigItems(UserRoles.PROCESSING.role)
                if (getCurrentKey().split("_")[1].contains("NI")) {
                    binding.spMaterial.text = getString(R.string.select_grade)
                    binding.cbExPo.isChecked = false
                }
            }
        }

    }

    private fun enableProceed(enabled: Boolean) {
        binding.btnProceed.isEnabled = enabled
        if (enabled)
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        else
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
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
        }
        if (getCurrentKey().split("_")[1].contains("NI")) {
            if (stageFevor.isNotEmpty() && materialCode.isNotEmpty()) enableProceed(selected)

        } else
            if (stageFevor.isNotEmpty() && matrialCodes.isNotEmpty()) enableProceed(selected)
    }

    private fun disableBomSelect(type: String) {
        when (type) {
            SEIVING -> {
                binding.cbPo.isEnabled = true
                binding.cbExPo.isEnabled = true
            }
            else -> {
                binding.cbPo.isEnabled = false
                binding.cbExPo.isEnabled = false
            }
        }
    }

    private fun navigateToNext(stage: String) {
        when (stage) {
            configStage -> {
                callBack?.replaceFragment(
                    POBOM,
                    seivingSelection != 1,
                    VegaCocoaRminProcessing(
                        materialCode,
                        materialName ?: "",
                        stage,
                        cfgNo!!,
                        stageFevor, auartNo = auartNo,
                        materialCodes = matrialCodes,
                        materialNames = matrialNames

                    )
                )
            }
            else -> {

                callBack?.replaceFragment(
                    POBOM,
                    true,
                    VegaCocoaRminProcessing(
                        materialCode,
                        materialName ?: "",
                        stage,
                        cfgNo!!,
                        stageFevor, auartNo = auartNo,
                        materialCodes = matrialCodes,
                        materialNames = matrialNames
                    )
                )
            }
        }
    }

    override fun clickOnItem(data: Int, isWH: Boolean) {
        grade.forEachIndexed { index, vegaMaterial ->
            if (gradeListData[data] == vegaMaterial.materialName) {
                binding.spGrade.text = vegaMaterial.materialName
                materialCode = vegaMaterial.materialCode
                materialName = vegaMaterial.materialName

                binding.spMaterial.text = materialName


                if (configStage != currentStage)
                    enableProceed(stageFevor.isNotEmpty() && materialCode.isNotEmpty())
                else
                    enableProceed(stageFevor.isNotEmpty() && seivingSelection != 0 && materialCode.isNotEmpty())

            }
        }
        if (customDialog != null) {
            customDialog!!.dismiss()
        }
    }

    override fun clickOnItem(data: List<String>, isSelected: Boolean) {
        matrialNames.clear()
        matrialCodes.clear()
        data.forEach {
            val item = it.split(" - ")
            matrialNames.add(
                item.dropLast(item.size - (item.size - 1)).toString().replace("[", "").replace("]", "")
                    .replace(",", " ")
            )
            matrialCodes.add(item.last())
        }
        //  materialCode = matrialCodes.toString()
        binding.spGrade.text = matrialNames.toString().replace("[", "").replace("]", "")
        if (configStage != currentStage)
            enableProceed(stageFevor.isNotEmpty() && matrialCodes.isNotEmpty())
        else
            enableProceed(stageFevor.isNotEmpty() && seivingSelection != 0 && matrialCodes.isNotEmpty())

        if (customDialog1 != null) {
            customDialog1!!.dismiss()
        }

    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        val gson = Gson()
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains("RMIN_PROCESS_STAGE_LIST")) {
                val rminStageList = gson.fromJson(it, VegaNicaRminProcessTypeModel::class.java)
                processTypeList= rminStageList.RMIN_PROCESS_STAGE_LIST.toMutableList()
            }
        }
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
                        isComplaint, isThirdPartym,isFarmerLessTransaction ->
                    if(isComplaint){ isCompliantMaterial = true}
                    else if(isThirdPartym){ isThirdParty = true}
                    else {isCompliantMaterial = false }
                    vm.fetchGrades()


                },
                { dismiss() },
                false)
        }

    }

}
