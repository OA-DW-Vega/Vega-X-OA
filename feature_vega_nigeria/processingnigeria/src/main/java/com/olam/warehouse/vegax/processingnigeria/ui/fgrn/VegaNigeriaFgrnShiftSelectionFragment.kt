package com.olam.warehouse.vegax.processingnigeria.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
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
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaRMINShiftModel
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentVegaNigeriaFgrnSiftSelectionBinding
import com.olam.warehouse.vegax.processingnigeria.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingnigeria.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.processingnigeria.utils.JSON_SHIFT_DETAILS_LIST_EN
import com.olam.warehouse.vegax.processingnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaFgrnShiftSelectionFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_fgrn_sift_selection
    private lateinit var binding: FragmentVegaNigeriaFgrnSiftSelectionBinding
    private val vm: VegaNigeriaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()
    private val materialList = arrayListOf<VegaNigeriaFgrnBagMaterialWithId>()
    private val batchList = arrayListOf<VegaCoffeeFgrnMaerialBatch>()
    var radioButton: RadioButton? = null
    private val isRmin: Boolean = true
    private var isBagConsumptionNeeded = true
    private var shiftList = mutableListOf<String>()
    private var jsonData = mutableListOf<String>()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems) = VegaNigeriaFgrnShiftSelectionFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaFgrnSiftSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingnigeria/ui/fgrn/VegaCoffeeFgrnShiftSelectionFragment")
            .title("Processing Coffee")
            .with(tracker)
    }
    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        vm.getShiftRemarksItems(getCurrentKey())
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateShiftAndRemarks(it) })
        binding.btnProceed.setOnClickListener {

//            validateField()
            val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
            radioButton = view?.findViewById(intSelectButton)

            val shift = radioButton?.text.toString()
            if (shift == "null") {
                fgrnItem.shiftSelection = ""
            } else {
                fgrnItem.shiftSelection = shift
            }
            val remark = binding.etOperator.text.toString()
            if (remark != null)
                fgrnItem.remarks = remark
            else {
                fgrnItem.remarks = ""
            }
//            fgrnItem.shiftSelection = radioButton.text.toString().split(" ")[1]
//            fgrnItem.remarks = binding.etOperator.text.toString()

            vm.updateFgrnShiftStatus(
                fgrnItem.fgrnId,
                fgrnItem.shiftSelection.toString(),
                fgrnItem.remarks.toString()
            )
            fgrnItem.materialBatchList = batchList
//            callBack?.replaceFgrnFragment( FRAG_SUMMARY, fgrnItem, "")
            callBack?.replaceFgrnFragment(FRAG_SUMMARY, fgrnItem, "")
//            callBack?.replaceFgrnFragment(if (isBagConsumptionNeeded) FRAG_BAG_CONSUMP else FRAG_SUMMARY, fgrnItem, "")
        }
        binding.rgShift.setOnCheckedChangeListener { group, checkedId ->
            radioButton = view?.findViewById(checkedId)!!
//            if (radioButton.text.toString().isNotEmpty()) {
////                enableDisableBtn(true)
//                shift = radioButton.text.toString().split(" ")[1]
//            }
//            else {
////                enableDisableBtn(false)
//                shift = ""
//            }
        }

        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getFgrnItems(fgrnItem.fgrnId)

        vm.poGradeList.observe(viewLifecycleOwner, Observer { checkWithGrade(it) })
        vm.stockList.observe(viewLifecycleOwner, Observer { updateStockUI(it) })

        binding.etOperator.onChange {
            val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
            if (intSelectButton > 0) {
                radioButton = view?.findViewById(intSelectButton)!!
//                if (it.isNotEmpty()){
////                enableDisableBtn(true)
//                    remark = binding.etOperator.text.toString()
//            }
//            else {
////                enableDisableBtn(false)
//                remark = ""
//            }
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

    private fun updateAdapter() {
        if (shiftList.size > 0) {
            binding.rbA.text = shiftList[0]
            binding.rbB.text = shiftList[1]
            binding.rbC.text = shiftList[2]
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
                                            val bagId = VegaNigeriaFgrnBagMaterialWithId()
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

    private fun updateShiftAndRemarks(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()

        jsonData.forEach {
            if (it.contains(JSON_SHIFT_DETAILS_LIST_EN)) {
                val shift = gson.fromJson(it, VegaNigeriaRMINShiftModel::class.java)
                shiftList.addAll(shift.SHIFT_DETAILS_LIST_EN)
            }
        }
        updateAdapter()
    }

    private fun updateUI(fgrnItems: VegaCoffeeFgrnItemWithGrades) {
        if (materialList.size == 0) {
            gradesWithBags = fgrnItems.gradeItems?.toList() ?: emptyList()
            when (fgrnItems.fgrnItems.shiftSelection) {
                getString(R.string.shift_a).split(" ")[1] -> binding.rbA.isChecked = true
                getString(R.string.shift_b).split(" ")[1] -> binding.rbB.isChecked = true
//                getString(R.string.shift_c).split(" ")[1] -> binding.rbC.isChecked = true
            }
            binding.etOperator.setText(fgrnItems.fgrnItems.remarks)
            vm.getPoGrades(fgrnItem.processOrderNo, isRmin)
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

//    private fun validateField() {
//
//        val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
//        radioButton = view?.findViewById(intSelectButton)!!
//        val shift = radioButton.text.toString().split(" ")[1]
//        val remark = binding.etOperator.text.toString()
//        when {
////            shift.toString().isEmpty() -> showSnack(getString(R.string.choose_shift))
////            remark.toString().isEmpty() -> showSnack(getString(R.string.choose_remarks))
//
//            shift.isEmpty() -> fgrnItem.shiftSelection = "N/A"
//            remark.isEmpty() -> fgrnItem.remarks = "N/A"
//
//
//            else -> {
//                fgrnItem.shiftSelection = shift
//                fgrnItem.remarks = remark
//                vm.updateFgrnShiftStatus(
//                    fgrnItem.fgrnId,
//                    fgrnItem.shiftSelection.toString(),
//                    fgrnItem.remarks.toString()
//                )
//                fgrnItem.materialBatchList = batchList
//                callBack?.replaceFgrnFragment(FRAG_SUMMARY, fgrnItem, "")
//
//            }
//        }
//
//        fgrnItem.materialBatchList = batchList
//        callBack?.replaceFgrnFragment(FRAG_SUMMARY, fgrnItem, "")
//
//
//    }
}
