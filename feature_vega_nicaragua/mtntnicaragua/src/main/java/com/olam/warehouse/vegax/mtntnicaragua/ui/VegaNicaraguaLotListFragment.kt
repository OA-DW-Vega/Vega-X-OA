package com.olam.warehouse.vegax.mtntnicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.prepareStocksToInventoryDeatils
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.AppUtils.posExtension
import com.olam.warehouse.presentation.utils.Constants.BATCH_NUMBER
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicLotListModel
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentNicaraguaMtntLotListBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.ItemNicaraguaInventoryLotBinding
import com.olam.warehouse.vegax.mtntnicaragua.utils.*
import com.olam.warehouse.vegax.mtntnicaragua.work.getDispatchQualityRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 11/12/2020.
 */
class VegaNicaraguaLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_nicaragua_mtnt_lot_list
    private lateinit var binding: FragmentNicaraguaMtntLotListBinding
    private var callBack: Callback? = null
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var vegaNicLotListModel: VegaNicLotListModel? = null
    private var mtnt: VegaNicaraguaMtnt? = null
    private var dispatchLotsList = mutableListOf<VegaNicaraguaGRNInventoryDetails>()
    private var wareHouseList = arrayListOf<String>()
    private var filteredDispatchLotsList = arrayListOf<VegaNicaraguaGRNInventoryDetails>()
    private var alreadySelected = arrayListOf<String>()
    private val mSearchList = arrayListOf<VegaNicaraguaGRNInventoryDetails>()
    private var productList = emptyList<VegaMaterial>()

    companion object {
        fun newInstance(vegaNicLotListModel: VegaNicLotListModel) =
            VegaNicaraguaLotListFragment().putArgs {
                putParcelable(MODEL_BUNDLE, vegaNicLotListModel)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    interface Callback {
        fun replaceFragment(type: String, data: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNicaraguaMtntLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        vegaNicLotListModel = arguments?.getParcelable(MODEL_BUNDLE) ?: VegaNicLotListModel()
        mtnt = vegaNicLotListModel?.mtnt
        binding.tvQualityGradeValue.text = mtnt?.qualityGrade
        if (mtnt?.certification?.isNotEmpty() == true) {
            binding.tvCertification.visible()
            binding.tvCertificationValue.visible()
            binding.tvCertificationValue.text = mtnt?.certification
        }
        alreadySelected.clear()
        vegaNicLotListModel?.selectedList?.map { it.batchNumber }?.toList()?.let { alreadySelected.addAll(it) }
    }

    private fun initUI() {
        vm.stockLotsSap.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.stockLotsOffline.observe(viewLifecycleOwner, Observer { updateUIOffline(it) })
        vegaNicLotListModel?.material?.let {
            if (isOnline()) vm.getStockListSap(it) else vm.getStockListOffline(
                it
            )
        }
        binding.btnProceed.setOnClickListener {
            sendSelectedLots()
        }
        binding.tvWareHouse.setOnClickListener { showWarehouseListDialog(wareHouseList) }
        vm.productList.observe(viewLifecycleOwner, Observer {
            productList = it
        })
        vm.getProducts()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_by_lot)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setAdapter(if (filteredDispatchLotsList.isEmpty()) dispatchLotsList else filteredDispatchLotsList)
                        } else {
                            mSearchList.clear()
                            dispatchLotsList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.lotId.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun showWarehouseListDialog(it: List<String>) {
        MaterialDialog(requireContext()).show {
            title(R.string.select_dest_wh)
            listItemsMultiChoice(items = it) { _, index, text ->
                filterWarehouseList(text.toString())
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun filterWarehouseList(locations: String) {
        val filter = locations.replace("[", "").replace("]", "").trim()
        if (filter.isEmpty() || filter.contains("All", true)) {
            setAdapter(dispatchLotsList)
            binding.tvWareHouse.text = getString(R.string.all)
        } else {
            binding.tvWareHouse.text = filter
            filteredDispatchLotsList.clear()
            val lotsList = dispatchLotsList.filter { locations.contains(it.procureLocationCode ?: "") }
            filteredDispatchLotsList.addAll(lotsList)
            setAdapter(filteredDispatchLotsList)
        }
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.clear()
            wareHouseList.add("All")
            val items = dispatchLotsList.map { it.procureLocationCode.toString() }.distinct()
            wareHouseList.addAll(items)
        }
    }

    private fun sendSelectedLots() {
        val selectedLots = dispatchLotsList.filter { it.isChecked }.filter { !alreadySelected.contains(it.lotId) }
        val tmpId = vegaNicLotListModel?.mtnt?.tempId
        if(mtnt?.merged == true && selectedLots.size==1) selectedLots.forEach { it.isEndLot = true }
       else if (mtnt?.merged == false) selectedLots.forEach { it.isEndLot = true }
        if (validateGradeAndCertification(selectedLots)) {
            if(validateBagCounts(selectedLots)) {
                vm.saveLotList(prepareLotItems(selectedLots, tmpId))
                activity?.onBackPressed()
            }else{
                activity?.toast(getString(R.string.avai_bag_count_empty))
            }
        } else {
            Toast.makeText(
                activity,
                getString(R.string.incorrect_quality_error),
                Toast.LENGTH_SHORT
            ).show()
        }
//        callBack?.replaceFragment(FRAG_ADD_LOT_BACK, prepareLotItems(selectedLots))
    }

    private fun validateBagCounts(selectedLots: List<VegaNicaraguaGRNInventoryDetails>): Boolean{
        return selectedLots.any { !it.availablebagCount.isNullOrEmpty() }
    }

    private fun validateGradeAndCertification(selectedLots: List<VegaNicaraguaGRNInventoryDetails>): Boolean {
        when (mtnt?.certification?.isNotEmpty() == true) {
            true -> return !selectedLots.any {
                !it.qualityGrade.equals(mtnt?.qualityGrade) || !it.certification.equals(
                    mtnt?.certification
                )
            }
            else -> return !selectedLots.any { !it.qualityGrade.equals(mtnt?.qualityGrade) }
        }
    }

    private fun updateUIOffline(response: List<VegaNicaraguaGRNInventoryDetails>?) {
        response?.let {
            dispatchLotsList.clear()
            val dataValue = filterLotList(it as ArrayList<VegaNicaraguaGRNInventoryDetails>)
            dispatchLotsList.addAll(dataValue)
            updateSelectLotValues()
            getWarehouseList()
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            val lotAll = prepareStocksToInventoryDeatils(dataValue, productList)
                            dispatchLotsList = filterLotList(lotAll)
                            updateSelectLotValues()
                            getWarehouseList()
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

    /*private fun updateUI(response: Resource<GenericReqAndResp<OfflineInventory>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            val lotAll = prepareGrnInventoryDetails(dataValue, productList)
                            dispatchLotsList = filterLotList(lotAll)
                            updateSelectLotValues()
                            getWarehouseList()
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
    }*/

    private fun filterLotList(lotAll: ArrayList<VegaNicaraguaGRNInventoryDetails>): ArrayList<VegaNicaraguaGRNInventoryDetails> {
        val filterList = arrayListOf<VegaNicaraguaGRNInventoryDetails>()
        if (mtnt?.vendorName?.isEmpty() == true) {
            filterList.addAll(lotAll)
            return filterList
        } else {

            lotAll.forEach {
                if (it.vendorCode?.equals(mtnt?.vendorCode) == true) filterList.add(it)
                /*when (mtnt?.vendorName?.isEmpty()) {
                    true -> {
                        *//*if (it.qualityGrade?.equals(mtnt?.qualityGrade) == true && it.certification?.equals(mtnt?.certification) == true)
                        filterList.add(it)*//*
                    }
                    else -> {
                        if (it.vendorCode?.equals(mtnt?.vendorCode) == true*//* && it.qualityGrade?.equals(mtnt?.qualityGrade) == true && it.certification?.equals(
                            mtnt?.certification
                        ) == true
                    *//*)
                            filterList.add(it)
                    }
                }*/
            }
            return filterList
        }
    }

    private fun updateSelectLotValues() {
        if (alreadySelected.isNotEmpty()) {
            dispatchLotsList.forEachIndexed { index, s ->
                dispatchLotsList[index].isChecked = alreadySelected.contains(s.lotId)
            }
        }
        setAdapter(dispatchLotsList)
        updateWeight()
        hideLoading()
    }

    private fun updateWeight() {
        var weight = 0.0
        val filter = dispatchLotsList.filter { it.isChecked }
        val count = filter.size
        for (item in filter) {
            weight = weight.plus(item.stockQty?.toDouble() ?: 0.0)
        }
        if (count == 0) weight = 0.0
        updateWeight(count.toString(), weight.formatThreeDigits())
    }

    private fun updateWeight(count: String, weight: String) {
        binding.tvLot.text = count.plus(" ").plus(getString(R.string.lot_selected))
        binding.tvLotWeight.text = weight.plus(" Kg")
//        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent().putExtra("sas", "sadasdas"))
    }

    private fun setAdapter(dispatchLotsList: MutableList<VegaNicaraguaGRNInventoryDetails>) {
        dispatchLotsList.forEachIndexed { index, it1 -> it1.possition = index }
        if (dispatchLotsList.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvLots.setUpAdapter(
            dispatchLotsList.distinctBy { Pair(it.lotId, it.lotId) }.sortedBy { it.possition }.toMutableList(),
            R.layout.item_nicaragua_inventory_lot,
            ItemNicaraguaInventoryLotBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvLotId.text = it.lotId
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocValue.text = it.procureLocationCode
                bindItem.tvQualityGradeValue.text = it.qualityGrade
                bindItem.tvCertificationValue.text = it.certification
                bindItem.tvWeightValue.text = it.stockQty.plus(" ").plus(it.uom)
                bindItem.ivSelect.isChecked = it.isChecked
                if (it.isProgress == true) bindItem.progressBar.visible() else bindItem.progressBar.gone()
                bindItem.llLotItem.setOnClickListener { view ->
                    it.isChecked = !it.isChecked
                    bindItem.ivSelect.isChecked = it.isChecked
                    updateWeight()
                    if (it.isChecked && it.qualityGrade?.isEmpty() == true && isOnline()) bindItem.progressBar.visible() else bindItem.progressBar.gone()
                    if (it.isChecked && it.qualityGrade?.isEmpty() == true && isOnline()) onGetQualityParams(
                        it.lotId,
                        it.materialCode,
                        pos
                    )
                }

                if (it.materialName?.contains("TOLLING", true) == true) {
                    bindItem.vendorLl.visible()
                    bindItem.tvVendorValue.text = it.vendorName
                }
            })
    }

    fun onGetQualityParams(batchNumber: String, materialCode: String, pos: Int) {
        val input = workDataOf(BATCH_NUMBER to batchNumber, MATERIAL to materialCode)
        val worker = getDispatchQualityRequestWorker(input, pos)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->

                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = posExtension(workInfo.tags)
                            val batchNo = workInfo.outputData.getString(LOT_ID)
                            dispatchLotsList.find { it.lotId.equals(batchNo) }.apply {
                                this?.qualityGrade = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.certification = workInfo.outputData.getString(CERTIFICATION)
                                this?.availablebagCount = workInfo.outputData.getString(BAG_COUNT)!!
                            }
                            filteredDispatchLotsList.find { it.lotId.equals(batchNo) }.apply {
                                this?.qualityGrade = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.certification = workInfo.outputData.getString(CERTIFICATION)
                                this?.availablebagCount = workInfo.outputData.getString(BAG_COUNT)!!
                            }
                            mSearchList.find { it.lotId.equals(batchNo) }.apply {
                                this?.qualityGrade = workInfo.outputData.getString(QUALITY_GRADE)
                                this?.certification = workInfo.outputData.getString(CERTIFICATION)
                                this?.availablebagCount = workInfo.outputData.getString(BAG_COUNT)!!
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
                            val position = posExtension(workInfo.tags)
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

}
