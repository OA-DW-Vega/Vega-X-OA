package com.olam.warehouse.vegax.pilesesame.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.pilesesame.R
import com.olam.warehouse.vegax.pilesesame.databinding.FragmentPileManagementLotListSesameBinding
import com.olam.warehouse.vegax.pilesesame.databinding.ItemPileManagementLotListCardSesameBinding
import com.olam.warehouse.vegax.pilesesame.utils.*
import com.olam.warehouse.vegax.pilesesame.work.getDispatchQualityRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaSesamePileManagementLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_pile_management_lot_list_sesame
    private lateinit var binding: FragmentPileManagementLotListSesameBinding
    private val vm: VegaSesamePileManagementViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var lotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private val mSearchList = mutableListOf<VegaCocoaDispatchLots>()
    private var materialList = arrayListOf<String>()
    private var listener: VegaSesamePileManagementAddLotListener? = null
    private var vendorcode: String = ""
    private var alreadySelected = mutableListOf<VegaCocoaDispatchLots>()
    private var currentKey = getCurrentKey()
    private var qualityGradeOfItem: String? = ""
    private var batch_no: String? = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? VegaSesamePileManagementAddLotListener
    }

    companion object {

        fun newInstance(
            model: ArrayList<VegaCocoaDispatchLots>,
            materialList: ArrayList<String>,
            fromVendorList: String,
            fromGradeList: String
        ) =
            VegaSesamePileManagementLotListFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelableArrayList(MODEL_BUNDLE, model)
                putString(PILE_SELECT, fromVendorList)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentPileManagementLotListSesameBinding.inflate(layoutInflater)
        if (currentKey.split("_")[1].contains("NI")) {
            binding.tvQualityGrade.visibility = View.VISIBLE
            binding.tvQualityGradeValue.visibility = View.VISIBLE
        }
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>
        vendorcode = arguments?.getString(PILE_SELECT) ?: ""
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        alreadySelected =
            arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(MODEL_BUNDLE) as ArrayList
        vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        //vm.stocksLocal.observe(viewLifecycleOwner, Observer { updateLocalUI(it) })
        if (materialList.size > 0) {
            vm.getStockList(materialList)
        }

        if (currentKey.split("_")[1].contains("NI")) {
            binding.tvQualityGradeValue.text = grade
        }

        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            lotsList.clear()
                            dispatchLotsList.clear()
                            /*val filteredDispatchStocksList = mutableListOf<VegaCocoaDispatchLots>()
                            val dataValue = it.data?.data!!
                            dataValue.let { item -> filteredDispatchStocksList.addAll(item) }*/
                            lotsList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaCocoaDispatchLots> else mutableListOf()
                            lotsList.forEach {
                                if (vendorcode != "" && grade != "") {
                                    if (it.vendor == vendorcode)
                                        dispatchLotsList.add(it)
                                } else
                                    dispatchLotsList = lotsList
                            }
                            updateSelectLotValues()
                            getWarehouseList()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
//                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateSelectLotValues() {
        val filter = mutableListOf<VegaCocoaDispatchLots>()
        filter.addAll(dispatchLotsList)
        val map = alreadySelected.map { it.batchNumber to it.batchNumber }
        if (alreadySelected.isNotEmpty()) {
            filter.forEachIndexed { index, s ->
                if (map.contains(s.batchNumber to s.batchNumber)) {
                    dispatchLotsList[index].isAdded = true
                    dispatchLotsList[index].qualityGrade = grade
                }
            }
        }
        hideLoading()
        updateWeight()
        setupAdapter(dispatchLotsList)
    }


    private fun setupAdapter(data: MutableList<VegaCocoaDispatchLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_pile_management_lot_list_card_sesame,
            ItemPileManagementLotListCardSesameBinding::inflate,
            { it, pos, bindItem ->

                if (currentKey.split("_")[1].contains("NI")) {
                    bindItem.headingLayout.visibility = View.VISIBLE
                    bindItem.valueLayout.visibility = View.VISIBLE
                    bindItem.tvGradeQuality.visibility = View.VISIBLE
                    bindItem.tvGradeQualityValue.visibility = View.VISIBLE
                    bindItem.ticketLayout.visibility= View.VISIBLE
                    bindItem.ticketValueLayout.visibility= View.VISIBLE
                }

                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocation.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.ivSelect.isChecked = it.isAdded
                if (it.qualityGrade?.isNotEmpty() == true && batch_no.equals(it.batchNumber)) {
                    qualityGradeOfItem = qualityGradeOfItem.toString().trim()
                    bindItem.tvGradeQualityValue.text = it.qualityGrade

                    if(it.ticketNumber?.isNotEmpty() == true)
                        bindItem.tvTicketQualityValue.text = it.ticketNumber

                }


                if (currentKey.split("_")[1].contains("NI")) {
                    if (it.isProgress == true) bindItem.progressBar.visible() else bindItem.progressBar.gone()
                    bindItem.llLotItem.setOnClickListener { view ->
                        it.isAdded = !it.isAdded
                        bindItem.ivSelect.isChecked = it.isAdded
                        updateWeight()
                        if (it.isAdded && it.qualityGrade?.isEmpty() == true && AppUtils.isOnline()) bindItem.progressBar.visible() else bindItem.progressBar.gone()
                        if(it.isAdded && it.qualityGrade?.isEmpty() == true && AppUtils.isOnline()) onGetQualityParams(it.batchNumber, it.materialCode, pos)
                    }
                } else {
                    bindItem.llLotItem.setOnClickListener { view ->
                        it.isAdded = !it.isAdded
                        bindItem.ivSelect.isChecked = it.isAdded
                    }

                    bindItem.ivSelect.setOnClickListener { view ->
                        it.isAdded = !it.isAdded
                        bindItem.ivSelect.isChecked = it.isAdded
                    }

                }
            })
    }

    private fun updateWeight() {
        var weight = 0.0
        val filter = dispatchLotsList.filter { it.isAdded }
        val count = filter.size
        for (item in filter) {
            weight = weight.plus(item.weight?.toDouble()?.formatThreeDigits()?.toDouble() ?: 0.0).formatThreeDigits().toDouble()
        }
        if (count == 0) weight = 0.0
        binding.tvLot.text = count.toString().plus(" ").plus(getString(R.string.lot_selected))
        //binding.tvLotWeight.text = weight.toString().plus(" Kg")
    }

    private fun onGetQualityParams(batchNumber: String, materialCode: String, pos: Int) {
        val input = workDataOf(Constants.BATCH_NUMBER to batchNumber, MATERIAL_CODE to materialCode)
        val worker = getDispatchQualityRequestWorker(input, pos)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->

                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = AppUtils.posExtension(workInfo.tags)
                            val batchNo = workInfo.outputData.getString(LOT_ID)
                            batch_no = batchNo

                            dispatchLotsList.find { it.batchNumber.equals(batchNo) }.apply {
                                qualityGradeOfItem = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.qualityGrade = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.ticketNumber = workInfo.outputData.getString(TICKET_NUMBER)
                            }
                            filteredDispatchLotsList.find { it.batchNumber.equals(batchNo) }.apply {
                                qualityGradeOfItem = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.qualityGrade = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.ticketNumber = workInfo.outputData.getString(TICKET_NUMBER)
                            }
                            mSearchList.find { it.batchNumber.equals(batchNo) }.apply {
                                qualityGradeOfItem = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.qualityGrade = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.ticketNumber = workInfo.outputData.getString(TICKET_NUMBER)
                            }
                            try {
                                dispatchLotsList[position].isProgress = false
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            }
                            try {
                                filteredDispatchLotsList[position].isProgress = false
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            }
                            try {
                                mSearchList[position].isProgress = false
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            }
                            binding.rvLots.adapter?.notifyItemChanged(position)
                        }
                        WorkInfo.State.FAILED -> {
                            val position = AppUtils.posExtension(workInfo.tags)
                            try {
                                dispatchLotsList[position].isProgress = false
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            }
                            try {
                                filteredDispatchLotsList[position].isProgress = false
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            }
                            try {
                                mSearchList[position].isProgress = false
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            }
                            binding.rvLots.adapter?.notifyItemChanged(position)
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {
                        }
                    }
                }

            })
    }

    private fun removeChecked(item: Int, list: MutableList<VegaCocoaDispatchLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        setupAdapter(list)
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaCocoaDispatchLots::storageLocationCode).toSet())
            updateWareHouseSpinner()
        } else {
            setupAdapter(dispatchLotsList)
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_sesame_pile_management_wh, wareHouseList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spWareHouse.adapter = stageAdapter
        val defaultposition = 0
        binding.spWareHouse.setSelection(defaultposition)
        binding.spWareHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filteredDispatchLotsList.clear()
                if (position > 0) {
                    val storageLocation = wareHouseList[position]
                    val lotsList = dispatchLotsList.filter { it.storageLocationCode == storageLocation }
                    filteredDispatchLotsList.addAll(lotsList)
                    setupAdapter(filteredDispatchLotsList)
                } else {
                    setupAdapter(dispatchLotsList)
                    filteredDispatchLotsList.clear()
                }
            }
        }
    }

    private fun sendSelectedLots() {
        val data = dispatchLotsList.filter { it.isAdded == true } as ArrayList
        if (currentKey.split("_")[1].contains("NI")) {
            if (validateGrade(data)) {
                listener?.addedLots(data)
            } else {
                Toast.makeText(
                        activity,
                        getString(R.string.incorrect_grade_quality),
                        Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            listener?.addedLots(data)
        }

    }

    private fun validateGrade(selectedLots: List<VegaCocoaDispatchLots>): Boolean {
        return !selectedLots.any { !it.qualityGrade.equals(grade) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(R.string.search_by_lots)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setupAdapter(if (filteredDispatchLotsList.isEmpty()) dispatchLotsList else filteredDispatchLotsList)
                        } else {
                            mSearchList.clear()
                            (if (filteredDispatchLotsList.isEmpty()) dispatchLotsList else filteredDispatchLotsList).forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.batchNumber.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setupAdapter(mSearchList)
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
