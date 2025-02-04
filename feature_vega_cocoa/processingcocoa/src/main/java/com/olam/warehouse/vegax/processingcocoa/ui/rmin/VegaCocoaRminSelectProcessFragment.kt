package com.olam.warehouse.vegax.processingcocoa.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.CustomFilterDialog
import com.olam.warehouse.presentation.ui.widget.RecyclerViewItemClickListener
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcocoa.R
import com.olam.warehouse.vegax.processingcocoa.databinding.FragmentVegaCocoaRminSelectProcessBinding
import com.olam.warehouse.vegax.processingcocoa.di.injectCocoaProcessingFeature
import com.olam.warehouse.vegax.processingcocoa.utils.POBOM
import com.olam.warehouse.vegax.processingcocoa.utils.SEIVING
import com.olam.warehouse.vegax.processingcocoa.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */
class VegaCocoaRminSelectProcessFragment : BaseFragment(), RecyclerViewItemClickListener {

    override val layoutResourceId = R.layout.fragment_vega_cocoa_rmin_select_process
    private lateinit var binding: FragmentVegaCocoaRminSelectProcessBinding

    private var callBack: CallBack? = null
    private val vm: VegaCocoaRminViewModel by viewModel()
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

    interface CallBack {
        fun replaceFragment(fragment: String, isPoSelect: Boolean, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaCocoaRminSelectProcessFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCocoaRminSelectProcessBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        injectCocoaProcessingFeature()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/rmin/VegaCocoaRminSelectProcessFragment")
            .title("Rmin Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        vm.grades.observe(viewLifecycleOwner, Observer { updateGradeUI(it) })
        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
        vm.fetchGrades()
        vm.fetchStages()
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        binding.cbPo.setOnCheckedChangeListener { it, isChecked -> updatePoSelection(isChecked, 1) }
        binding.cbExPo.setOnCheckedChangeListener { it, isChecked -> updatePoSelection(isChecked, 2) }
        binding.btnProceed.setOnClickListener { navigateToNext(currentStage) }
        //disableBomSelect("")
    }

    private var isAutoPo: Boolean = false
    private var isExistPo: Boolean = false
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        binding.clPo.visible()
        configItems?.forEach {
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
        enableProceed(configStage != currentStage && materialCode.isNotEmpty())
    }

    private fun updateGradeUI(gradeList: List<VegaMaterial>) {

        /*val gradeinit = VegaMaterial()
        gradeinit.materialName = getString(R.string.select_grade)
        grade.add(gradeinit)*/
        grade.addAll(gradeList.distinct())
        gradeListData = grade.distinctBy { it.materialName }.map { data -> data.materialName.toString() } as ArrayList
        binding.spGrade.setOnClickListener { spinnerClick(gradeListData) }
        /*val gradeAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_cocoa_processing_rmin_grade, gradeListData)
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
        customDialog = CustomFilterDialog(items, context!!, this)
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun updageStageUI(stageList: List<VegaProcessingStage>) {
        val stage = ArrayList<VegaProcessingStage>()
        val stageInitItem = VegaProcessingStage()
        stageInitItem.processName = getString(R.string.select_stage)
        stage.add(stageInitItem)
        stage.addAll(stageList)
        val stageListData = stage.map { data -> data.processName }
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_cocoa_processing_rmin_grade, stageListData)
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
                        // disableBomSelect(currentStage)
                        //enableProceed(position > 0 && SEIVING != vegaProcessingStage.processName && materialCode.isNotEmpty())
                        if (position > 0) vm.getConfigItems(UserRoles.PROCESSING.role)
                    }
                }
            }
        }
    }

    private fun enableProceed(enabled: Boolean) {
        binding.btnProceed.isEnabled = enabled
        if (enabled)
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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
        if (stageFevor.isNotEmpty() && materialCode.isNotEmpty()) enableProceed(selected)
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
                        stageFevor, auartNo = auartNo
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
                        stageFevor, auartNo = auartNo
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
}
