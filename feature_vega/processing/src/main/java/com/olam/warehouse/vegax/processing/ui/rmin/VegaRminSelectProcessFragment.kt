package com.olam.warehouse.vegax.processing.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBom
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingRminBomPost
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaRminSelectProcessBinding
import com.olam.warehouse.vegax.processing.ui.VegaProcessingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaRminSelectProcessFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_rmin_select_process
    private lateinit var binding: FragmentVegaRminSelectProcessBinding
    private var callBack: CallBack? = null
    private var materialCode: String? = ""
    private var stageFevor: String? = ""
    private var cfgNo: String? = ""
    private var materialName: String? = ""
    private val vm: VegaProcessingViewModel by viewModel()

    interface CallBack {
        fun replaceFragment(
            stageFevor: String,
            cfgNumber: String,
            materialName: String,
            materialNo: String,
            bomList: List<VegaProcessingRminBoms>
        )

        fun replaceFragment(
            stageFevor: String,
            cfgNumber: String,
            materialName: String,
            materialNo: String,
            bom: VegaProcessingRminBoms
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaRminSelectProcessFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaRminSelectProcessBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/rmin/VegaRminSelectProcessFragment").title("Processing").with(tracker)
        initUI()
    }


    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvType, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        vm.grades.observe(viewLifecycleOwner, Observer { updateGradeUI(it) })
        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
        vm.bomList.observe(viewLifecycleOwner, Observer { updateBom(it) })
        vm.fetchGrades()
        vm.fetchStages()
        binding.btnProceed.setOnClickListener { validateFields() }
    }

    private fun validateFields() {
        when {
            materialCode.isNullOrEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.select_grade))
            }
            stageFevor.isNullOrEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.select_stage))
            }
            else -> {
                vm.fetchBomList(
                    VegaProcessingRminBomPost(
                        cfgNo!!,
                        stageFevor!!,
                        materialCode!!,
                        getCurrentKey(),
                        getPlantDetails()
                    )
                )
            }
        }
    }


    private fun updateGradeUI(gradeList: List<VegaMaterial>) {
        val grade = ArrayList<VegaMaterial>()
        val gradeinit = VegaMaterial()
        gradeinit.materialName = "--Select Grade--"
        grade.add(gradeinit)
        grade.addAll(gradeList)
        val gradeListdata = grade.distinctBy { it.materialCode }
            .map { data -> data.materialName .plus(" - ")
            .plus(data.materialCode) }as ArrayList

        val gradeAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_processing_rmin_grade, gradeListdata)
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spGrade.adapter = gradeAdapter
        var defaultposition = 0
        gradeListdata.forEachIndexed { index, s -> if (s.contains("100000000939")) defaultposition = index }
        binding.spGrade.setSelection(defaultposition)
        binding.spGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                grade.forEachIndexed { index, vegaMaterial ->
                    if (vegaMaterial.materialCode.equals("100000000939")) {
                        materialCode = vegaMaterial.materialCode
                        materialName = vegaMaterial.materialName
                    }
                }
            }
        }
    }


    private fun updageStageUI(stageList: List<VegaProcessingStage>) {
        val stage = ArrayList<VegaProcessingStage>()
        val stageInitItem = VegaProcessingStage()
        stageInitItem.processName = "--Select Stage--"
        stage.add(stageInitItem)
        stage.addAll(stageList)
        val stageListdata = stage.map { data -> data.processName }
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_processing_rmin_grade, stageListdata)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStage.adapter = stageAdapter
        var defaultposition = 0
        stageListdata.forEachIndexed { index, s -> if (s.equals("RCN Drying process")) defaultposition = index }
        binding.spStage.setSelection(defaultposition)
        binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                stage.forEachIndexed { index, vegaProcessingStage ->
                    if (stageListdata[position] == vegaProcessingStage.processName) {
                        stageFevor = vegaProcessingStage.fevor
                        cfgNo = vegaProcessingStage.cfgNo
                    }
                }
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
                            moveToBomFragment(it.data?.data?.boms)
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

    private fun moveToBomFragment(bomList: List<VegaProcessingRminBoms>?) {
        when {
            bomList?.size!! > 1 -> callBack?.replaceFragment(
                stageFevor!!,
                cfgNo!!,
                materialName!!,
                materialCode!!,
                bomList
            )
            bomList.size == 1 -> callBack?.replaceFragment(
                stageFevor!!,
                cfgNo!!,
                materialName!!,
                materialCode!!,
                bomList[0]
            )
            else -> showErrorDialogWithFAQLink(
                requireContext(),
                requireContext().resources.getString(R.string.no_bom_Available)
            )
        }
    }


}

