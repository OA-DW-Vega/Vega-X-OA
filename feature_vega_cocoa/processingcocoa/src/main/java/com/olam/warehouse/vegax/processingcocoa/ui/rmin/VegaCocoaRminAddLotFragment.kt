package com.olam.warehouse.vegax.processingcocoa.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcocoa.R
import com.olam.warehouse.vegax.processingcocoa.databinding.FragmentAddLotRminBinding
import com.olam.warehouse.vegax.processingcocoa.utils.*
import kotlinx.android.synthetic.main.item_lot_info.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaRminAddLotFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_add_lot_rmin
    private lateinit var binding: FragmentAddLotRminBinding

    private var callBack: CallBack? = null
    private val vm: VegaCocoaRminViewModel by viewModel()
    private var model: VegaCocoaRminProcessing? = null
    private var addedLotList = mutableListOf<VegaCocoaRminLots>()
    private var fromSummary: Boolean? = false
    private var weightToProcess = 0.0
    private var previousValue = 0.0
    private var isEditableLot: Boolean = false
    private var isMultipleLot: Boolean = false
    private val isRmin: Boolean = true

    interface CallBack {
        fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(fromSummary: Boolean, model: VegaCocoaRminProcessing) =
            VegaCocoaRminAddLotFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putBoolean(FROM_SUMMARY, fromSummary)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentAddLotRminBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        fromSummary = arguments?.getBoolean(FROM_SUMMARY)
//        vm.getLotList(model?.cgfNo ?: "", model?.poNumber ?: "", model?.bom ?: "", model?.materialName ?: "")
        vm.vegaCocoaRminWithItems.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                //model?.rminId = if (it.rminItem.rminId.isEmpty()) getTmpId() else it.rminItem.rminId
                if (it.lots?.isNotEmpty()!!) updateAdapter(it.lots as ArrayList<VegaCocoaRminLots>)
            } else {
                model?.rminId = getTmpId()
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/rmin/VegaCocoaRminAddLotFragment")
            .title("Rmin Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        binding.cvCard.tvBomValue.text = model?.bom
        binding.cvCard.tvMaterialValue.text = model?.materialName
        binding.cvCard.tvStageValue.text = model?.stage
        binding.cvCard.tvPoNoValue.text = model?.poNumber
        //binding.cvCard.tvProceesValue.text = model?.weightToProcess

        if (model?.poNumber?.isNotEmpty()!!) {
            binding.cvCard.tvBomValue.invisible()
            binding.cvCard.tvBom.invisible()
            binding.cvCard.tvPoNoValue.visible()
            binding.cvCard.tvPoNo.visible()
        }
        else {
            binding.cvCard.tvBomValue.visible()
            binding.cvCard.tvBom.visible()
            binding.cvCard.tvPoNoValue.invisible()
            binding.cvCard.tvPoNo.invisible()
        }

        binding.btnAddLot.setOnClickListener { navigateToLotList() }
        vm.lotList.observe(viewLifecycleOwner, Observer { enableProceed(it.isNotEmpty()) })
        binding.btnProceed.setOnClickListener {
            if (addedLotList.size > 0) {
                if (validateLotWeight()) {
                    model?.lotList?.clear()
                    model?.weightToProcess = binding.cvCard.tvProceesValue.text.toString()
                    model?.lotList?.addAll(vm.lotList.value ?: ArrayList())
                    //addedLotList.forEach { it.rminId = model?.rminId.toString() }
                    vm.saveWeighBridgeAndLotDetails(addedLotList, model!!)
                    callBack?.replaceFragment(if (fromSummary == true) SUMMARY else SHIFT, isMultipleLot, model!!)
                }
                else Toast.makeText(
                    activity,
                    getString(R.string.less_weight_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                Toast.makeText(
                    activity,
                    getString(R.string.please_add_lot),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        //vm.lotList.value = model?.lotList
        //addedLotList = model?.lotList ?: mutableListOf()
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.PROCESSING.role)
        vm.poGradeList.observe(viewLifecycleOwner, Observer { updateFgrnGradeUI(it) })
        if (model?.poNumber?.isNotEmpty()!!) vm.getPoGrades(model?.poNumber.toString(), isRmin)
    }

    private fun updateFgrnGradeUI(response: Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                val gradeList =
                                    it.filter { item -> item.materialCode.contains(model?.materialCode.toString()) }
                                if (gradeList.size > 0) {
                                    model?.bwart = gradeList[0].bwart
                                    model?.rsnum = gradeList[0].rsNum
                                    model?.rspos = gradeList[0].rsPos
                                    model?.phase = gradeList[0].phase
                                    model?.xchpf = gradeList[0].xchpf
                                    model?.resource = gradeList[0].resource
                                }
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

    private fun setUpAdapter(list: MutableList<VegaCocoaRminLots>) {
        binding.rvList.setUp(list, R.layout.item_lot_info, { item, pos ->
            tvLotId.text = item.batchNumber
            tvStLocation.text = item.storageLocationCode
            tvGradeValue.text = item.materialName
            val weight = item.weight?.toDouble()?.formatThreeDigits()
            tvWeightValue.text = weight.plus(" ").plus(item.unitOfMeasure)
            if (isEditableLot) {
                clSelectAllWeight.visibility = View.VISIBLE
            }
            else {
                etWeight.isEnabled = false
                item.editedWeight = item.weight
                clSelectAllWeight.visibility = View.GONE
            }

            etWeight.setText(item.editedWeight)
            etWeight.onChange {
                if (it.isNotEmpty()) {
                    // item.editedWeight = it
                    val come: Int? = it.toDouble().compareTo(item.weight?.toDouble() ?: 0.0)
                    if (come ?: 0 <= 0) {
                        item.isLowerWeight = true
                        addedLotList[pos].editedWeight = it
                        updateWeightToProcess()
                        //vm.saveWeighBridgeAndLotDetails(vm.lotList.value as MutableList<VegaCocoaRminLots>)
                    }
                    else {
                        item.isLowerWeight = false
                        etWeight.error = getString(R.string.less_weight_error)
                    }
                    //item.editedWeight = it
                }
                else {
                    addedLotList[pos].editedWeight = ""
                    updateWeightToProcess()
                    //vm.saveWeighBridgeAndLotDetails(vm.lotList.value as MutableList<VegaCocoaRminLots>)
                    etWeight.error = context.getString(R.string.empty_weight)/*getString(R.string.less_weight_error)*/
                }
            }
            cbSelectAll.isChecked = item.isChecked ?: false

            cbSelectAll.setOnCheckedChangeListener { it, isChecked ->
                // etWeight.isEnabled = !isChecked
                item.isChecked = isChecked
                if (isChecked) {
                    etWeight.setText(weight?.replace(",", ""))
                    item.editedWeight = etWeight.text.toString()
                }
                else {
                    etWeight.setText("")
                    item.editedWeight = etWeight.text.toString()
                }
                updateWeightToProcess()
            }
            /*tvWeightupdate.setOnClickListener {
                vm.lotList.value!![pos].editedWeight = etWeight.text.toString()
                updateWeightToProcess()
                vm.saveWeighBridgeAndLotDetails(vm.lotList.value as MutableList<VegaCocoaRminLots>)
            }*/
            ivClose.setOnClickListener { showConformationDialog(pos, ivClose) }
            updateWeightToProcess()
        }, {})
    }

    private fun updateWeightToProcess() {
        if (!isEditableLot) calculateWtp(vm.lotList.value!!)
        else binding.cvCard.tvProceesValue.text =
            addedLotList.sumByDouble { if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble()!! }
                .formatThreeDigits().plus(" ").plus(if (addedLotList.size > 0) addedLotList[0].unitOfMeasure else "MT")
    }

    private fun removeCurrentItem(pos: Int) {
        vm.deleteLot(addedLotList.get(pos).batchNumber, addedLotList.get(pos).cgfNo)
        addedLotList.removeAt(pos)
        //vm.lotList.value?.removeAt(pos)
        binding.rvList.adapter?.notifyItemRemoved(pos)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun navigateToLotList() {
        model?.lotList?.clear()
        vm.lotList.value?.let { model?.lotList?.addAll(it) }
        callBack?.replaceFragment(Lot_List, isMultipleLot, model!!)
    }

    fun updateLotList(list: ArrayList<VegaCocoaRminLots>) {
        //addedLotList.clear()
        //vm.lotList.value?.clear()
        for (item in list) {
            item.cgfNo = model?.cgfNo ?: ""
            item.BOMNumber = model?.bom ?: ""
            item.poNumber = model?.poNumber ?: ""
            item.stage = model?.stage ?: ""
            item.baseMaterialCode = model?.materialName ?: ""
        }
        val saveLot = mutableListOf<VegaCocoaRminLots>()
        val addedLotId = addedLotList.map { it.batchNumber }
        list.forEach {
            if (!addedLotId.contains(it.batchNumber))
                saveLot.add(it)
        }
        //vm.deleteAllLot(model?.poNumber ?: "", model?.cgfNo ?: "", model?.bom ?: "", model?.materialName ?: "")
        saveLot.forEach { it.rminId = model?.rminId.toString() }
        vm.saveWeighBridgeAndLotDetails(saveLot, model!!)
    }

    private fun enableProceed(enable: Boolean) {
        binding.btnProceed.isEnabled = enable
    }

    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    removeCurrentItem(position)
                    updateWeightToProcess()
                },
                { dismiss() })
        }
    }

    private fun validateLotWeight(): Boolean {
        val selected = addedLotList.filter { !it.isLowerWeight }
        val emptyWeight = addedLotList.filter { it.editedWeight?.length ?: "" == 0 }
        return selected.isEmpty() && emptyWeight.isEmpty()
    }

    private fun updateAdapter(list: ArrayList<VegaCocoaRminLots>) {
        val addedBatch = addedLotList.map { it.batchNumber }
        list.forEach {
            if (!addedBatch.contains(it.batchNumber)) addedLotList.add(it)
        }
        addedLotList.distinct()
        vm.lotList.postValue(addedLotList as ArrayList<VegaCocoaRminLots>)
        setUpAdapter(addedLotList)
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_WEIGHT_EDITABLE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isEditableLot = true
                        it.applicable?.contains("N")!! -> isEditableLot = false
                    }
                }
                ConfigItems.LOT_SELECTION_MULTI.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isMultipleLot = true
                        it.applicable?.contains("N")!! -> {
                            isMultipleLot = false
                        }
                    }
                }
            }
        }
        vm.getRminWithLotList(model?.cgfNo ?: "", model?.poNumber ?: "", model?.bom ?: "", model?.materialName ?: "")

    }

    private fun calculateWtp(list: ArrayList<VegaCocoaRminLots>) {
        weightToProcess = 0.0
        list.forEach { weightToProcess += it.weight?.toDouble() ?: 0.0 }
        binding.cvCard.tvProceesValue.text =
            weightToProcess.toString().toDouble().formatThreeDigits().plus(" ")
                .plus(if (list.size > 0) list[0].unitOfMeasure else "MT")
    }

    fun saveLotDetails() {
        /* addedLotList.forEach { it.rminId = model?.rminId.toString() }
         vm.saveWeighBridgeAndLotDetails(addedLotList, model!!)*/
    }
}
