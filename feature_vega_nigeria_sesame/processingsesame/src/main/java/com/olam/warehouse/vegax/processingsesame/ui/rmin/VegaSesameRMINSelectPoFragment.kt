package com.olam.warehouse.vegax.processingsesame.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.CustomFilterDialog
import com.olam.warehouse.presentation.ui.widget.RecyclerViewItemClickListener
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.databinding.FragmentVegaSesameRminSelectPoBinding
import com.olam.warehouse.vegax.processingsesame.di.injectSesameProcessingFeature
import com.olam.warehouse.vegax.processingsesame.utils.RMIN_GRADES
import com.olam.warehouse.vegax.processingsesame.utils.convertMtToKg
import kotlinx.android.synthetic.main.item_vega_sesame_fgrn_po_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaSesameRMINSelectPoFragment : BaseFragment(), RecyclerViewItemClickListener {

    override val layoutResourceId = R.layout.fragment_vega_sesame_rmin_select_po
    private lateinit var binding: FragmentVegaSesameRminSelectPoBinding

    private var callBack: CallBack? = null
    private val vm: VegaSesameRminViewModel by viewModel()
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

    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaSesameRMINSelectPoFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaSesameRminSelectPoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        injectSesameProcessingFeature()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/rmin/VegaCocoaRMINSelectPoFragment")
            .title("Processing Coffee").with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvType, it, false)
        }
        vm.grades.observe(viewLifecycleOwner, Observer { updateGradeUI(it) })
        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
        vm.fetchGrades()
        vm.fetchStages()
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems() })
        vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
    }

    private fun updateConfigItems() {
        /*Nothing Implemented*/
    }

    private fun updateGradeUI(gradeList: List<VegaMaterial>) {
        grade.addAll(gradeList.distinct())
        gradeListData = grade.distinctBy { it.materialName }.map { data -> data.materialName.toString() } as ArrayList
    }

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
        var defaultposition = 0
        stageListData.forEachIndexed { index, s -> if (s.equals("RCN Drying process")) defaultposition = index }
        binding.spStage.setSelection(defaultposition)
        binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {/*Nothing Selected*/}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                stage.forEachIndexed { index, vegaProcessingStage ->
                    if (stageListData[position] == vegaProcessingStage.processName) {
                        stageFevor = vegaProcessingStage.fevor
                        cfgNo = vegaProcessingStage.cfgNo
                        auartNo = vegaProcessingStage.auart
                        currentStage = vegaProcessingStage.processName ?: ""
                        stageValue = stageListData[position] ?: ""
                        if (position > 0) vm.fetchFgrnPoDetailsList(auartNo ?: "", cfgNo ?: "", stageFevor)
                    }
                }
            }
        }
    }

    private fun navigateToNext(model: VegaCoffeeRminProcessing) {

        callBack?.replaceFragment(
            RMIN_GRADES, model
        )
    }

    override fun clickOnItem(data: Int, isWh: Boolean) {
        grade.forEachIndexed { index, vegaMaterial ->
            if (gradeListData[data] == vegaMaterial.materialName) {
                materialCode = vegaMaterial.materialCode
                materialName = vegaMaterial.materialName

            }
        }
        if (customDialog != null) {
            customDialog!!.dismiss()
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
                                poList.addAll(it)
                                poList.forEachIndexed { index, s ->
                                    poList[index].netWeight = convertMtToKg(
                                        poList[index].netWeight.toString(),
                                        poList[index].unitsOfMeasure.toString()
                                    )
                                }
                                /*val data =
                                    it.filter { item -> item.materialCode!!.contains(model?.materialCode.toString()) }
                                poList.addAll(data)*/
                                updatePoListDetails(poList)
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

    private fun updatePoListDetails(poList: List<VegaFgrnProcessingOrder>) {
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

        binding.tvToatalNo.text = getString(R.string.total_pos_0).plus(" ").plus(poList.size)
        binding.rvPoList.setUp(data as MutableList, R.layout.item_vega_sesame_fgrn_po_details, { item, pos ->

            tvPoNo.text = item.processOrderNo
            tv_material_name.text = item.materialName
            tvWeight.text = item.netWeight.plus(" ").plus("KG")
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
            model = VegaCoffeeRminProcessing()
            model?.auartNo = auartNo
            model?.foreverNo = stageFevor
            model?.cgfNo = cfgNo ?: ""
            model?.poNumber = this.processOrderNo
            model?.baseMaterialCode = this.materialCode
            model?.baseMaterialName = this.materialName
            model?.stage = stageValue
            model?.vendor = vendor
            navigateToNext(model ?: VegaCoffeeRminProcessing())
        })
    }
}
