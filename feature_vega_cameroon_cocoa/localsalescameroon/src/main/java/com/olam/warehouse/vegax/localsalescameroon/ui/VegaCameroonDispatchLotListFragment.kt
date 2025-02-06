package com.olam.warehouse.vegax.localsalescameroon.ui

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
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
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
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.localsalescameroon.R
import com.olam.warehouse.vegax.localsalescameroon.databinding.FragmentCameroonSalesLotListBinding
import com.olam.warehouse.vegax.localsalescameroon.databinding.ItemCameroonSalesLotListDetailBinding
import com.olam.warehouse.vegax.localsalescameroon.utils.*
import com.olam.warehouse.vegax.localsalescameroon.work.getDispatchQualityRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 8/22/2020.
 */
class VegaCameroonDispatchLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_cameroon_sales_lot_list
    private lateinit var binding: FragmentCameroonSalesLotListBinding
    private val vm: VegaCameroonSalesViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCoffeeSalesLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaCoffeeSalesLots>()
    private var wareHouseList = mutableListOf<String>()
    private var weight: Double? = 0.0
    private var count = 0
    private var alreadySelected = mutableListOf<VegaCoffeeSalesLots>()
    private val mSearchList = mutableListOf<VegaCoffeeSalesLots>()
    private var isMultipleAdd = true
    private var materialList = arrayListOf<String>()
    private var listener: CallBack? = null
    private var model: VegaCoffeeSalesOrder? = null
    private var currentKey = getCurrentKey()
    private var qualityGradeOfItem: String? = ""
    private var qualityCertificateOfItem: String? = ""
    private var batch_no:String? = ""

    interface CallBack {
        fun addedLots(
            lots: ArrayList<VegaCoffeeSalesLots>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeSalesOrder, materialList: ArrayList<String>, isMultipleAdd: Boolean = true) =
            VegaCameroonDispatchLotListFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelable(MODEL_BUNDLE, model)
                putBoolean("multipleAdd", isMultipleAdd)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentCameroonSalesLotListBinding.inflate(layoutInflater)
        if (currentKey.split("_")[1].contains("NI")) {
            binding.tvQualityGrade.visibility = View.VISIBLE
            binding.tvQualityGradeValue.visibility = View.VISIBLE
            binding.tvCertification.visibility = View.VISIBLE
            binding.tvCertificationValue.visibility = View.VISIBLE
        }
        initExtra()
        initUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("localsalescameroon/ui/VegaCameroonDispatchLotListFragment")
            .title("Vega_Cameroon/Local Sales").with(tracker)
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        alreadySelected.addAll(model?.lotList ?: mutableListOf())
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>
        isMultipleAdd = arguments?.getBoolean("multipleAdd") ?: true
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        if (materialList.size > 0) {
            vm.getStockList(materialList)
        }
        if(currentKey.split("_")[1].contains("NI")){
            binding.tvQualityGradeValue.text = grade
            binding.tvCertificationValue.text = if (certificate!!.isEmpty()) "NA" else certificate
        }
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()

                            dispatchLotsList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaCoffeeSalesLots> else mutableListOf()
                            if (getCurrentKey().split("_")[1].contains("NI")) {
                                dispatchLotsList =
                                    dispatchLotsList.filter { it.storageLocationCode.equals("WR01") } as MutableList<VegaCoffeeSalesLots>
                            }
                            updateSelectLotValues()
                            getWarehouseList()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                    hideLoading()
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
        val filter = mutableListOf<VegaCoffeeSalesLots>()
        filter.addAll(dispatchLotsList)
        if (isMultipleAdd)
            if (alreadySelected.isNotEmpty()) {
                filter.forEachIndexed { index, s ->
                    alreadySelected.forEach { item ->
                        if (s.batchNumber.equals(item.batchNumber) && s.materialCode.equals(item.materialCode) && s.storageLocationCode.equals(
                                item.storageLocationCode
                            )
                        )
                            dispatchLotsList[index].isAdded = true
                    }
                }
            }
    }


    private fun setupAdapter(data: MutableList<VegaCoffeeSalesLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_cameroon_sales_lot_list_detail,
            ItemCameroonSalesLotListDetailBinding::inflate,
            { it, pos, bindItem ->

                if (currentKey.split("_")[1].contains("NI")) {
                    bindItem.headingLayout.visibility = View.VISIBLE
                    bindItem.valueLayout.visibility = View.VISIBLE
                    bindItem.tvGradeQuality.visibility = View.VISIBLE
                    bindItem.tvGradeQualityValue.visibility = View.VISIBLE
                    bindItem.tvCertificateQuality.visibility = View.VISIBLE
                    bindItem.tvCerticationQualityValue.visibility = View.VISIBLE
                }

                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocationValue.text = it.storageLocationCode
                when (it.unitOfMeasure) {
                    "KG" -> {
                        bindItem.tvWeightValue.text =
                            it.weight?.toDouble()?.formatThreeDigits().plus(" ")
                                .plus(it.unitOfMeasure)
                    }
                    "MT" -> {
                        bindItem.tvWeightValue.text =
                            convertMtToKg(it.weight.toString()).toDouble().formatThreeDigits()
                                .plus(" ")
                                .plus("KG")
                    }
                }
                bindItem.ivSelect.isChecked = it.isAdded ?: false

                if (currentKey.split("_")[1].contains("NI")) {
                    if (qualityGradeOfItem?.isNotEmpty() == true && qualityCertificateOfItem?.isNotEmpty() == true && batch_no.equals(
                            it.batchNumber
                        )
                    ) {
                        qualityGradeOfItem = qualityGradeOfItem.toString().trim()
                        bindItem.tvGradeQualityValue.text = qualityGradeOfItem
                        qualityCertificateOfItem = qualityCertificateOfItem.toString().trim()
                        bindItem.tvCerticationQualityValue.text = qualityCertificateOfItem
                    }
                }

                if (currentKey.split("_")[1].contains("NI")) {
                    bindItem.llLotItem.setOnClickListener { view ->
                        it.isAdded = !it.isAdded!!
                        bindItem.ivSelect.isChecked = it.isAdded ?: false
                        if (it.isAdded!! && qualityGradeOfItem?.isEmpty() == true
                            && qualityCertificateOfItem?.isEmpty() == true && AppUtils.isOnline()
                        )
                            onGetQualityParams(it.batchNumber, it.materialCode, pos)
                        if (!isMultipleAdd) {
                            sendSelectedLots()
                        }

                    }
                } else {
                    bindItem.llLotItem.setOnClickListener { view ->
                        it.isAdded = !it.isAdded!!
                        bindItem.ivSelect.isChecked = it.isAdded ?: false

                        if (!isMultipleAdd) {
                            sendSelectedLots()
                        }
                    }

                    bindItem.ivSelect.setOnClickListener { view ->
                        it.isAdded = !it.isAdded!!
                        bindItem.ivSelect.isChecked = it.isAdded ?: false
                        if (!isMultipleAdd) {
                            sendSelectedLots()
                        }
                    }
                }

            })
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
                                qualityGradeOfItem =
                                    workInfo.outputData.getString(QUALITY_GRADE)?.trim()
                                qualityCertificateOfItem =
                                    workInfo.outputData.getString(CERTIFICATION)?.trim()
                            }
                            filteredDispatchLotsList.find { it.batchNumber.equals(batchNo) }.apply {
                                qualityGradeOfItem =
                                    workInfo.outputData.getString(QUALITY_GRADE)?.trim()
                                qualityCertificateOfItem =
                                    workInfo.outputData.getString(CERTIFICATION)?.trim()
                            }
                            mSearchList.find { it.batchNumber.equals(batchNo) }.apply {
                                qualityGradeOfItem =
                                    workInfo.outputData.getString(QUALITY_GRADE)?.trim()
                                qualityCertificateOfItem =
                                    workInfo.outputData.getString(CERTIFICATION)?.trim()
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

    private fun removeChecked(item: Int, list: MutableList<VegaCoffeeSalesLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        setupAdapter(list)
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaCoffeeSalesLots::storageLocationCode).toSet())
            if (getCurrentKey().split("_")[1].contains("NI")) {
                wareHouseList = wareHouseList.filter { it.equals("WR01") ||  it.equals(getString(R.string.all)) } as MutableList<String>
            }
            updateWareHouseSpinner()
        } else {
            setupAdapter(dispatchLotsList)
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_cameroon_dispatch_wh, wareHouseList)
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

        if(currentKey.split("_")[1].contains("NI")){
            if(validateGrade(data)){
                listener?.addedLots(data)
            }
            else{
                Toast.makeText(
                    activity,
                    getString(R.string.incorrect_quality_grade_and_certificate),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        else{
            listener?.addedLots(data)
        }
    }

    private fun validateGrade(selectedLots: List<VegaCoffeeSalesLots>): Boolean {
        return qualityGradeOfItem.equals(grade)
                && qualityCertificateOfItem.equals(certificate)
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
