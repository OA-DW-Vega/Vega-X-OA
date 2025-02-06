package com.olam.warehouse.vegax.processingecuador.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnItemWithGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingecuador.R
import com.olam.warehouse.vegax.processingecuador.data.domain.model.VegaEcuadorProcessingFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingecuador.data.domain.model.VegaEcuadorProcessingFgrnMaterialBatch
import com.olam.warehouse.vegax.processingecuador.databinding.FragmentVegaEcuadorProcessingFgrnSiftSelectionBinding
import com.olam.warehouse.vegax.processingecuador.databinding.ItemVegaEcuadorProcessingFgrnSelectBatchBinding
import com.olam.warehouse.vegax.processingecuador.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaEcuadorProcessingFgrnSiftSelectionFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ecuador_processing_fgrn_sift_selection
    private lateinit var binding: FragmentVegaEcuadorProcessingFgrnSiftSelectionBinding
    private val vm: VegaEcuadorProcessingFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCocoaFgrnItems()
    private var gradesWithBags = listOf<VegaCocoaFgrnGradesWithBagItems>()
    private val materialList = arrayListOf<VegaEcuadorProcessingFgrnBagMaterialWithId>()
    private val batchList = arrayListOf<VegaEcuadorProcessingFgrnMaterialBatch>()
    lateinit var radioButton: RadioButton
    private val isRmin: Boolean = true
    private var isRound: Boolean = false
    private var isIndexweighmenttype: Boolean = false

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems) = VegaEcuadorProcessingFgrnSiftSelectionFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorProcessingFgrnSiftSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnSiftSelectionFragment")
            .title("Processing IndiaCoffee")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCocoaFgrnItems()
        isRound = arguments?.getBoolean(ROUND_OFF) ?: false
        isIndexweighmenttype = arguments?.getBoolean(INDEX_WEIGHTMENT_TYPE) ?: false
        binding.btnProceed.setOnClickListener {
           // if (getCurrentKey().split("_")[1].contains("NI")) {
                val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
                if (intSelectButton != -1) {
                    radioButton = view?.findViewById(intSelectButton)!!
                    fgrnItem.shiftSelection = radioButton.text.toString()
//                        .split(" ")[1]
                }
                if (binding.etOperator.text.toString().isNotEmpty()) {
                    fgrnItem.operatorName = binding.etOperator.text.toString()
                }
                vm.updateFgrnShiftStatus(
                    fgrnItem.fgrnId,
                    fgrnItem.shiftSelection.toString(),
                    fgrnItem.operatorName.toString()
                )
                callBack?.replaceFgrnFragment(FRAG_SUMMARY, fgrnItem, "")
           // }
        /*else {
                val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
                radioButton = view?.findViewById(intSelectButton)!!
                fgrnItem.shiftSelection = radioButton.text.toString()
//                    .split(" ")[1]
                fgrnItem.operatorName = binding.etOperator.text.toString()
                vm.updateFgrnShiftStatus(
                    fgrnItem.fgrnId,
                    fgrnItem.shiftSelection.toString(),
                    fgrnItem.operatorName.toString()
                )
                callBack?.replaceFgrnFragment(FRAG_SUMMARY, fgrnItem, "")*/
           // }
        }

        binding.rgShift.setOnCheckedChangeListener { group, checkedId ->
            radioButton = view?.findViewById(checkedId)!!
            var isAdded = batchList.any { it.isValueAdded }
            if (batchList.size == 0) isAdded = true
               // enableDisableBtn(true)
            /* } else {
                 if (radioButton.text.toString().isNotEmpty() && binding.etOperator.text.toString()
                         .isNotEmpty() && isAdded
                 )
                     enableDisableBtn(true)
                 else
                     enableDisableBtn(false)
             }*/
        }
        /*vm.fgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.fetchFgrnItem(fgrnItem.fgrnId)*/

        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getFgrnItems(fgrnItem.fgrnId)

        vm.poGradeList.observe(viewLifecycleOwner, Observer { checkWithGrade(it) })
        vm.stockList.observe(viewLifecycleOwner, Observer { updateStockUI(it) })

        binding.etOperator.onChange {
            val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
            if (intSelectButton > 0) {
                radioButton = view?.findViewById(intSelectButton)!!
                var isAdded = batchList.any { it.isValueAdded }
                if (batchList.size == 0) isAdded = true
               // enableDisableBtn(true)
           // else {
                   /* if (it.isNotEmpty() && radioButton.text.toString().isNotEmpty() && isAdded)
                        enableDisableBtn(true)
                    else
                        enableDisableBtn(false)
                }*/
            }
        }
    }

    private fun updateStockUI(response: Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val dataList = it.data?.data

                            if (dataList?.size!! > 0) {
                                materialList.forEach { item ->
                                    val batchItem = VegaEcuadorProcessingFgrnMaterialBatch()
                                    batchItem.bagId = item.fgrnIdMaterial
                                    batchItem.materialCode = item.bagMaterialCode
                                    var _matname = dataList.filter { it.materialCode.equals(item.bagMaterialCode) }
                                    batchItem.materialName =
                                        if (_matname.size > 0) _matname.get(0).materialName ?: "" else " "
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

    private fun updateAdapter(batchList1: ArrayList<VegaEcuadorProcessingFgrnMaterialBatch>) {
        if (batchList1.size > 0) binding.clBatch.visible() else binding.clBatch.gone()
        binding.rvBatchSelection.setUpAdapter(
            batchList1,
            R.layout.item_vega_ecuador_processing_fgrn_select_batch,
            ItemVegaEcuadorProcessingFgrnSelectBatchBinding::inflate,
            { it, pos, bindItem ->
                if (!it.materialName.isNullOrEmpty()) {
                    bindItem.tvMaterial.text = it.materialName
                    val list = ArrayList<VegaCocoaRminLots>()
                    val rminLot = VegaCocoaRminLots()
                    rminLot.batchNumber = context.getString(R.string.choose_batch)
                    list.add(rminLot)
                    list.addAll(it.batchList)
                    val lots = list.map { it.batchNumber }
                    val stageAdapter =
                        ArrayAdapter(
                            requireContext(),
                            R.layout.item_vega_ecuador_processing_rmin_grade,
                            lots
                        )
                    stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
                    bindItem.spLot.adapter = stageAdapter
                    bindItem.spLot.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {
                            }

                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                position: Int,
                                p3: Long
                            ) {
                                if (position > 0) {
                                    vm.updateBatchToBagDetails(
                                        it.materialCode,
                                        lots[position],
                                        it.bagId
                                    )
                                    batchList[pos].isValueAdded = true
                                } else {
                                    batchList.forEach { it.isValueAdded = false }
                                }
                                val intSelectButton: Int = binding.rgShift.checkedRadioButtonId
                                if (intSelectButton > 0) {
                                    radioButton = view?.findViewById(intSelectButton)!!
                                    val isAdded = batchList.any { it.isValueAdded }

                                    //enableDisableBtn(true)
                                    /*else {
                                    if (binding.etOperator.text.toString()
                                            .isNotEmpty() && radioButton.text.toString()
                                            .isNotEmpty() && isAdded
                                    )
                                        enableDisableBtn(true)
                                    else
                                        enableDisableBtn(false)
                                }*/
                                }
                            }
                        }
                    bindItem.spLot.setSelection(it.selection + 1)
                }
            },
            {

            })
    }

    private fun checkWithGrade(response: Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>?) {
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
                                            val bagId = VegaEcuadorProcessingFgrnBagMaterialWithId()
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

    private fun updateUI(fgrnItems: VegaCocoaFgrnItemWithGrades) {
        if (materialList.size == 0) {
            gradesWithBags = fgrnItems.gradeItems?.toList() ?: emptyList()
            when (fgrnItems.fgrnItems.shiftSelection) {
                getString(R.string.shift_morning) -> binding.rbA.isChecked = true
                getString(R.string.shift_night) -> binding.rbB.isChecked = true
//                getString(R.string.shift_c).split(" ")[1] -> binding.rbC.isChecked = true
            }
            binding.etOperator.setText(fgrnItems.fgrnItems.operatorName)
            vm.getPoGrades(fgrnItem.processOrderNo, isRmin)
        }
    }

   /* private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }*/
}
