package com.olam.warehouse.vegax.processingghana.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnMaerialBatch
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingghana.databinding.FragmentVegaGhanaFgrnSiftSelectionBinding
import com.olam.warehouse.vegax.processingghana.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingghana.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.processingghana.utils.VEGA_STAGE
import com.olam.warehouse.vegax.processingghana.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaFgrnShiftSelectionFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_fgrn_sift_selection
    private lateinit var binding: FragmentVegaGhanaFgrnSiftSelectionBinding
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()
    private val materialList = arrayListOf<VegaGhanaFgrnBagMaterialWithId>()
    private val batchList = arrayListOf<VegaCoffeeFgrnMaerialBatch>()
    var radioButton: RadioButton? = null
    private val isRmin: Boolean = true
    private var isBagConsumptionNeeded = true
    private var vegaStage = VegaProcessingStage()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String, vegaStage: VegaProcessingStage )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems, vegaStage: VegaProcessingStage) = VegaGhanaFgrnShiftSelectionFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putParcelable(VEGA_STAGE, vegaStage)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaFgrnSiftSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaSesameFgrnShiftSelectionFragment")
            .title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        vegaStage = arguments?.getParcelable(VEGA_STAGE) ?: VegaProcessingStage()

        binding.btnProceed.setOnClickListener {
            val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
            radioButton = view?.findViewById(intSelectButton)

            val shift = radioButton?.text.toString()
            if (shift == "null") {
                fgrnItem.shiftSelection = ""
            } else {
                fgrnItem.shiftSelection = shift.split(" ")[0]
            }
            val remark = binding.etOperator.text.toString()
            if (remark != null)
                fgrnItem.remarks = remark
            else {
                fgrnItem.remarks = ""
            }
            vm.updateFgrnShiftStatus(
                fgrnItem.fgrnId,
                fgrnItem.shiftSelection.toString(),
                fgrnItem.remarks.toString()
            )
            fgrnItem.materialBatchList = batchList
            callBack?.replaceFgrnFragment(FRAG_SUMMARY, fgrnItem, "", vegaStage)
        }
        binding.rgShift.setOnCheckedChangeListener { group, checkedId ->
            radioButton = view?.findViewById(checkedId)!!
        }

        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getFgrnItems(fgrnItem.fgrnId)

//        vm.poGradeList.observe(viewLifecycleOwner, Observer { checkWithGrade(it) })
//        vm.stockList.observe(viewLifecycleOwner, Observer { updateStockUI(it) })

        binding.etOperator.onChange {
            val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
            if (intSelectButton > 0) {
                radioButton = view?.findViewById(intSelectButton)!!
            }
        }
    }

    private fun updateStockUI(response: Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val dataList = it.data?.data

                            if (dataList?.size!! > 0) {
                                materialList.forEach { item ->
                                    val batchItem = VegaCoffeeFgrnMaerialBatch()
                                    batchItem.bagId = item.fgrnIdMaterial
                                    batchItem.materialCode = item.bagMaterialCode
                                    batchItem.materialName =
                                        dataList.filter { it.materialCode.equals(item.bagMaterialCode) }
                                            .get(0).materialName
                                            ?: ""
                                    batchItem.batchList =
                                        dataList.filter { it.materialCode.equals(item.bagMaterialCode) }
                                    batchItem.batchList.forEachIndexed { index, vegaCocoaRminLots ->
                                        if (vegaCocoaRminLots.batchNumber.equals(
                                                item.batchNumber
                                            )
                                        ) batchItem.selection = index
                                    }
                                    batchList.add(batchItem)
                                }
                            }
                            updateAdapter(batchList)
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

    private fun updateAdapter(batchList1: ArrayList<VegaCoffeeFgrnMaerialBatch>) {
        isBagConsumptionNeeded = batchList1.size > 0
    }

    private fun checkWithGrade(response: Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                val rminMaterial = it.map { item -> item.materialCode }
                                gradesWithBags.forEach { it1 ->
                                    it1.bagItems?.forEach { it2 ->
                                        if (materialList.map { it.bagMaterialCode }
                                                .contains(it2.bagMaterialCode)) return@forEach
                                        if (rminMaterial.contains(it2.bagMaterialCode)) {
                                            val bagId = VegaGhanaFgrnBagMaterialWithId()
                                            bagId.fgrnIdMaterial = it2.fgrnId
                                            bagId.batchNumber = it2.batchNumber
                                            bagId.bagMaterialCode = it2.bagMaterialCode
                                            materialList.add(bagId)
                                        }
                                    }
                                }
                                val datList = arrayListOf<String>()
                                datList.addAll(materialList.map { it.bagMaterialCode }.distinct())
                                if (materialList.size > 0) vm.getStockList(datList)
                                isBagConsumptionNeeded = materialList.size > 0
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

    private fun updateUI(fgrnItems: VegaCoffeeFgrnItemWithGrades) {
        if (materialList.size == 0) {
            gradesWithBags = fgrnItems.gradeItems?.toList() ?: emptyList()
            when (fgrnItems.fgrnItems.shiftSelection) {
                getString(R.string.shift_a).split(" ")[1] -> binding.rbA.isChecked = true
                getString(R.string.shift_b).split(" ")[1] -> binding.rbB.isChecked = true
            }
            binding.etOperator.setText(fgrnItems.fgrnItems.remarks)
//            vm.getPoGrades(fgrnItem.processOrderNo, isRmin)
        }
    }

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }

}
