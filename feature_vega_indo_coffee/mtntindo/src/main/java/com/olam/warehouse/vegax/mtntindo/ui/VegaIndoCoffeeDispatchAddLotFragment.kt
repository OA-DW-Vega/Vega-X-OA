package com.olam.warehouse.vegax.mtntindo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchPurchaseOrder
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntindo.R
import com.olam.warehouse.vegax.mtntindo.databinding.FragmentIndoCoffeeDispatchLotSelectLayoutBinding
import com.olam.warehouse.vegax.mtntindo.ui.callback.VegaIndoCoffeeRecyclerViewItemClickListener
import com.olam.warehouse.vegax.mtntindo.utils.*
import kotlinx.android.synthetic.main.item_indo_coffee_dispatch_layout_lot_summary.view.*
import kotlinx.android.synthetic.main.item_indo_coffee_dispatch_material_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeDispatchAddLotFragment : BaseFragment(),
    VegaIndoCoffeeRecyclerViewItemClickListener {
    override val layoutResourceId: Int = R.layout.fragment_indo_coffee_dispatch_lot_select_layout

    private lateinit var binding: FragmentIndoCoffeeDispatchLotSelectLayoutBinding
    private var callBack: CallBack? = null
    private var isLoadingStarted = false
    private var listener: VegaIndoCoffeeRecyclerViewItemClickListener? = null
    private var customDialog: VegaIndoCoffeeCustomSingleSelectDialog? = null
    private var purchaseOrderList = mutableListOf<VegaEcuadorDispatchPurchaseOrder>()
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var wareHouseId = ""
    private var selectedPurchaseOrder: VegaEcuadorDispatchPurchaseOrders? = null
    private var purchaseOrder = ArrayList<VegaEcuadorDispatchPurchaseOrders>()
    private var selectedSTO = mutableListOf<VegaEcuadorDispatchPurchaseOrders>()
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var materialList = mutableListOf<VegaMaterial>()
    private var model: VegaEcuadorDispatch? = VegaEcuadorDispatch()
    private var materialCodeList = arrayListOf<String>()
    private var addedLotList = mutableListOf<VegaEcuadorDispatchLots>()
    private var plants = mutableListOf<Plant>()
    private var dispatchLotsList = arrayListOf<VegaEcuadorDispatchLots>()

    private val vm: VegaIndoCoffeeDispatchViewModel by viewModel()

    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaEcuadorDispatch, data: Any)
        fun replaceFragment(moveFrag: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaEcuadorDispatch, dispatchLotList: ArrayList<VegaEcuadorDispatchLots>) =
            VegaIndoCoffeeDispatchAddLotFragment().putArgs {
                putParcelableArrayList(DISPATCH_DATA_LIST, dispatchLotList)
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffeeDispatchLotSelectLayoutBinding.inflate(inflater)
        initExtra()
        initUI()
        listener = this
        return binding.root
    }

    private fun initExtra() {
        // getPlantDetails()
        dispatchLotsList = arguments?.getParcelableArrayList<VegaEcuadorDispatchLots>(DISPATCH_DATA_LIST)!!
        model = arguments?.getParcelable(MODEL_BUNDLE)!!
    }

    private fun updateUIWithLocalData() {
        binding.llOffline.gone()
        isLoadingStarted = model!!.isStarted
        enableProceed(model!!.isStarted && model!!.isEnded)
        enableStartLoad(model!!.isStarted && !model!!.isEnded)
        updateLoadingState(!model!!.isEnded)
        binding.tvWhValue.text = model!!.storageLocationCode.plus("-").plus(model!!.plantName)
        binding.tvstoValue.text = model!!.purchaseDocNum
        selectedSTO.clear()
        model?.purchaseOrders?.clear()
        selectedSTO =
            purchaseOrder.filter { it.purchaseDocNum == model!!.purchaseDocNum } as ArrayList<VegaEcuadorDispatchPurchaseOrders>
        setUpAdapter(selectedSTO)
        selectedSTO.let { model?.purchaseOrders?.addAll(it) }
        setUpLotsAdapter(addedLotList)
    }

    private fun getPlantDetails() {
        val plantList = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
        if (!plantList.isNullOrEmpty()) plantList.forEach { plant -> plants.add(plant) }
        val stLocation = ArrayList<String>()
        for (item in plants) {
            stLocation.add(item.plantId.plus("-").plus(item.plantName))
        }
        storageLocation.addAll(stLocation.toSet().distinct())
    }

    private fun getDestPlantDetails() {
        val data = purchaseOrder.map { it.warehouseId.toString() }
        storageLocation.addAll(data.distinct())
    }

    private fun initUI() {
        binding.tvWhValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_dest_wh)) }
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(false, getString(R.string.select_sto_no)) }
        binding.btStartLoad.setOnClickListener { checkLoadingStatus() }
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clAddInventory.setOnClickListener { moveToAddInventory() }

        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.purchaseOrderLocal.observe(viewLifecycleOwner, Observer { updatePurchaseOrderOffline(it) })
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
            if (AppUtils.isOnline()) vm.getPurchaseOrder("") else vm.getPurchaseOrderLocal("")
        })
        vm.getProducts()
        vm.lotDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        vm.lotDetailsLocal.observe(viewLifecycleOwner, Observer { updateLotInfoLocal(it) })
        binding.btAdd.setOnClickListener {
            vm.validateLot(binding.etEnterContainer.text.toString(), model?.wbTempId.toString())
        }
        /*val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rvLots.layoutManager = linearLayoutManager*/

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    binding.etEnterContainer.text.toString(),
                    materialCodeList, model?.plantId ?: ""
                )
        })

        binding.btnProceed.setOnClickListener {
            if (!validateLotWeight())
                Toast.makeText(activity, getString(R.string.lot_weight_not_added), Toast.LENGTH_SHORT)
                    .show()
            else if (validateWeight()) moveToMergeList()
        }
        binding.llOffline.setOnClickListener { moveToOfflineSummary() }
        if (dispatchLotsList.size > 0) {
            dispatchLotsList.let { addedLotList.addAll(it) }
            // vm.getPurchaseOrderLocal(model!!.storageLocationCode.toString())
        } else {
            enableStartLoad(false)
            enableProceed(false)
            /*vm.dispatchItemCountLocal.observe(this, Observer {
                if (it != null && it.size > 0) {
                    binding.llOffline.visible()
                } else {
                    binding.llOffline.gone()
                }
            })
            vm.getDispatchWithLineItemCount()*/
        }
    }

    private fun checkLoadingStatus() {
        if (!isLoadingStarted) {
            showStartLoadDialog(false)
        } else if (isLoadingStarted && addedLotList.size > 0) {
            showStartLoadDialog(true)
        } else Toast.makeText(activity, getString(R.string.add_lot_msg), Toast.LENGTH_SHORT).show()
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String) {
        if (isLoadingStarted) return
        customDialog =
            VegaIndoCoffeeCustomSingleSelectDialog(
                title,
                isWh,
                if (isWh) storageLocation else STONumbers,
                activity!!,
                listener!!
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun setUpAdapter(list: MutableList<VegaEcuadorDispatchPurchaseOrders>) {
        materialCodeList.clear()
        materialCodeList.addAll(list.map { it.materialCode }.distinct())
        binding.rvMaterial.setUp(list, R.layout.item_indo_coffee_dispatch_material_details, { it, pos ->
            materialList.forEach { item ->
                if (it.materialCode.contains(item.materialCode)) {
                    tvMaterialValue.text = item.materialName
                    it.materialName = item.materialName
                }
            }
            if (!it.openQuantity.isNullOrEmpty())
                tvStoWeightValue.text =
                    it.openQuantity?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ").plus(it.meins)
        })
    }

    private fun setUpLotsAdapter(list: MutableList<VegaEcuadorDispatchLots>) {
        binding.rvLots.setUp(list, R.layout.item_indo_coffee_dispatch_layout_lot_summary, { item, pos ->
            tvLotId.text = item.batchNumber
            tvMaterialName.text = item.materialName
            tvStorageLocationValue.text = item.storageLocationCode
            val weight = item.grossWeight?.toDouble()?.formatThreeDigits()
            tvWeightValue.text = weight.plus(" ").plus(item.unitsOfMeasure)
            etWeight.setText(item.editedWeight)
            etWeight.onChange {
                if (it.isNotEmpty()) {
                    val come: Int? = it.toDouble().compareTo(item.grossWeight?.toDouble() ?: 0.0)
                    if (come ?: 0 <= 0) {
                        item.isLowerWeight = true
                        addedLotList[pos].editedWeight = it
                    } else {
                        item.isLowerWeight = false
                        etWeight.error = getString(R.string.less_weight_error)
                    }
                } else {
                    addedLotList[pos].editedWeight = ""
                    etWeight.error = context.getString(R.string.empty_weight)
                }
            }
            cbSelectAll.isChecked = item.isChecked ?: false

            cbSelectAll.setOnCheckedChangeListener { it, isChecked ->
                item.isChecked = isChecked
                if (isChecked) {
                    etWeight.setText(weight?.replace(",", ""))
                    item.editedWeight = etWeight.text.toString()
                    etWeight.error = null
                } else {
                    etWeight.setText("")
                    item.editedWeight = etWeight.text.toString()
                }
            }
            ivClose.setOnClickListener { showConformationDialog(pos, ivClose) }
        }, {})
    }

    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    removeCurrentItem(position)
                },
                { dismiss() })
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean) {
        customDialog?.dismiss()
        if (isWh) {
            binding.tvWhValue.text = data
            wareHouseId = data
            val da = data.split("-").toTypedArray()
            STONumbers = filterSTONumber(data) as ArrayList<String>
            binding.tvstoValue.text = ""
            selectedSTO.clear()
            setUpAdapter(selectedSTO)
            enableStartLoad(false)
            if (da.size > 0) {
                model?.storageLocationCode = da[0]
                //model?.plantName = da[1]
            } else {
                model?.storageLocationCode = data
            }
        } else {
            binding.tvstoValue.text = data
            selectedSTO =
                purchaseOrder.filter { it.purchaseDocNum == data } as ArrayList<VegaEcuadorDispatchPurchaseOrders>
            setUpAdapter(selectedSTO)
            enableStartLoad(true)
            model?.purchaseOrders?.clear()
            selectedSTO.let { model?.purchaseOrders?.addAll(it) }
            selectedSTO.distinct()
            model?.purchaseDocNum = data
            model?.unitsOfMeasure = selectedSTO[0].meins
        }
    }

    private fun showStartLoadDialog(isEndLoad: Boolean) {
        showDialog(
            if (isEndLoad) getString(R.string.end_load_msg) else
                getString(R.string.start_msg),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    if (isEndLoad) {
                        if (remark.isEmpty()) Toast.makeText(
                            activity,
                            getString(R.string.enter_remark),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        else {
                            enableProceed(isEndLoad)
                            updateState(isEndLoad, remark)
                        }
                    } else {
                        updateState(isEndLoad, remark)
                    }
                }
            },
            isEndLoad
        )
    }

    private fun updateState(isEndLoad: Boolean, remark: String) {
        isLoadingStarted = true
        updateLoadingState(!isEndLoad)
        model?.remarks = remark
    }

    private fun updateLotInfoLocal(data: List<VegaEcuadorDispatchStocks>) {
        val lotList = mutableListOf<VegaEcuadorDispatchLots>()
        lotList.addAll(prepareLotsList(data as MutableList<VegaEcuadorDispatchStocks>))
        if (lotList.isNullOrEmpty()) {
            showLotNotFoundDialog()
        } else {
            when (lotList.size == 1) {
                true -> updateAdapter(lotList.get(0))
                else -> chooseOneLotDialog(lotList)
            }
        }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        val lotList = mutableListOf<VegaEcuadorDispatchLots>()
                        lotList.addAll(prepareLotsList(it1 as MutableList<VegaEcuadorDispatchStocks>))
                        when (lotList.size == 1) {
                            true -> updateAdapter(lotList.get(0))
                            else -> chooseOneLotDialog(lotList)
                        }

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

    private fun chooseOneLotDialog(lots: List<VegaEcuadorDispatchLots>) {
        val lotItem = lots.map {
            "Lot Id : ".plus(it.batchNumber).plus("\nWeight : ").plus(it.grossWeight).plus(" ").plus(it.unitsOfMeasure)
                .plus("\nStorage Location : ").plus(it.storageLocationCode)
        }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_lot)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = lotItem) { _, index, text ->
                updateAdapter(lots[index])
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun updateAdapter(lot: VegaEcuadorDispatchLots) {
        binding.etEnterContainer.setText("")
        val addedLotId = addedLotList.map { it.batchNumber }
        if (!addedLotId.contains(lot.batchNumber)) {
            if (model?.wbTempId?.isEmpty()!!) model?.wbTempId = getTmpId() else model?.wbTempId
            lot.wbTempId = model?.wbTempId!!
            model?.lotList?.add(lot)
            addedLotList.add(lot)
            vm.insertLot(lot)
            setUpLotsAdapter(addedLotList)
        }
    }

    private fun updateLoadingState(isStarted: Boolean) {
        when (isStarted) {
            true -> {
                model?.isStarted = true
                startTime = System.currentTimeMillis()
                model?.startTime = startTime.toString()
                disableAddMoreLots(true)
                binding.btStartLoad.text = resources.getString(R.string.end_loading)
                binding.btStartLoad.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_corner_violet_ofi)
                ViewCompat.setBackgroundTintList(
                    binding.btStartLoad,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.red
                        )
                    }
                )
                binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_pause_white_indo))
                //vm.saveWeighBridgeAndLotDetails()
            }
            false -> {
                model?.isEnded = true
                endTime = System.currentTimeMillis()
                model?.endTime = endTime.toString()
                val duration = endTime?.minus(startTime ?: 0)
                model?.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration ?: 0).toString()
                binding.btStartLoad.text = resources.getString(R.string.end_loading)
                binding.btStartLoad.setTextColor(getColor(com.olam.warehouse.presentation.R.color.white))
                binding.btStartLoad.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_corner_violet_ofi)
                ViewCompat.setBackgroundTintList(
                    binding.btStartLoad,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.light_grey_3
                        )
                    }
                )
                binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_pause_white_indo))
                binding.btStartLoad.isEnabled = false
            }
        }
    }

    private fun enableProceed(isEnable: Boolean) {
        binding.btnProceed.isEnabled = isEnable
        binding.btnProceed.setBackgroundColor(
            if (isEnable) getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
            else getColor(com.olam.warehouse.presentation.R.color.grey)
        )
    }

    private fun enableAddLot(value: String) {
        if (!isLoadingStarted) return
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }

    private fun enableStartLoad(isEnable: Boolean) {
        binding.btStartLoad.isEnabled = isEnable
        if (isEnable) {
            binding.btStartLoad.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_corner_violet_ofi)
            ViewCompat.setBackgroundTintList(
                binding.btStartLoad,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                }
            )
        } else {
            binding.btStartLoad.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_corner_violet_ofi)
            ViewCompat.setBackgroundTintList(
                binding.btStartLoad,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.grey
                    )
                }
            )
        }
    }

    private fun moveToAddInventory() {
        if (!isLoadingStarted) {
            Toast.makeText(activity, getString(R.string.start_loading_error), Toast.LENGTH_SHORT).show()
            return
        }
        // model = vm.dispatchWh
        model?.lotList?.clear()
        addedLotList.let { model?.lotList?.addAll(it) }
        callBack?.replaceFragment(LOT_LIST, model!!, materialCodeList)
    }

    private fun moveToMergeList() {
        model?.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")
        if (selectedSTO.isNotEmpty()) {
            model?.materialCode = selectedSTO[0].materialCode
            model?.materialName = selectedSTO[0].materialName
        }
        vm.saveProcessOrderAndLotDetails(addedLotList, model!!)
        callBack?.replaceFragment(MERGE_LIST, model!!, addedLotList as ArrayList<VegaEcuadorDispatchLots>)
    }

    private fun moveToOfflineSummary() {
        callBack?.replaceFragment(DISPATCH_OFFLINE_SUMMARY)
    }

    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        purchaseOrder.clear()
                        if (model?.materialCode.isNullOrEmpty())
                            purchaseOrderList = it1 as MutableList<VegaEcuadorDispatchPurchaseOrder>
                        else
                            purchaseOrderList =
                                it1.filter { it.materialCode == model?.materialCode } as MutableList<VegaEcuadorDispatchPurchaseOrder>
                        for (item in purchaseOrderList) {
                            purchaseOrder.addAll(item.purchaseOrders)
                        }
                        getDestPlantDetails()
                        STONumbers.clear()
                        STONumbers = filterSTONumber() as ArrayList<String>
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

    private fun updatePurchaseOrderOffline(data: List<VegaEcuadorDispatchPurchaseOrders>?) {
        purchaseOrder.clear()
        data?.forEach { purchaseOrder.add(it) }
        STONumbers.clear()
        STONumbers = filterSTONumber() as ArrayList<String>
        if (dispatchLotsList.size > 0) {
            updateUIWithLocalData()
        }
        getDestPlantDetails()
    }

    private fun fetchLotDetails(lotId: String, materialCodeList: ArrayList<String>, whId: String) {
        binding.etEnterContainer.hideKeyboard()
        if (lotId.isNotEmpty()) {
            if (AppUtils.isOnline()) {
                showLoading()
                vm.getLotDetails(lotId, materialCodeList, whId)
            } else vm.getLotDetailsLocal(lotId)
        }
    }

    private fun filterSTONumber(): List<String> {
        val sto = purchaseOrder.distinctBy { it.purchaseDocNum } as ArrayList
        val purchaseList = ArrayList<String>()
        for (item in sto) {
            purchaseList.add(item.purchaseDocNum)
        }
        return purchaseList
    }

    private fun filterSTONumber(destWarehouse: String): List<String> {
        val sto = purchaseOrder.distinctBy { it.purchaseDocNum } as ArrayList
        val purchaseList = ArrayList<String>()
        for (item in sto) {
            if (item.warehouseId.equals(destWarehouse))
                purchaseList.add(item.purchaseDocNum)
        }
        return purchaseList
    }

    private fun disableAddMoreLots(isEnable: Boolean) {
        binding.etEnterContainer.isEnabled = isEnable
        binding.clAddInventory.isEnabled = isEnable
    }

    private fun showBackConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.go_back)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.saveProcessOrderAndLotDetails(addedLotList, model!!)
                },
                { dismiss() })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    showBackConformationDialog()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showLotAlreadyExistDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.already_added)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                dismiss()
            }
        }
    }

    private fun showLotNotFoundDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.lot_not_found)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                dismiss()
            }
        }
    }

    fun updateLotList(list: ArrayList<VegaEcuadorDispatchLots>) {
        if (model?.wbTempId?.isEmpty()!!) model?.wbTempId = getTmpId() else model?.wbTempId
        val saveLot = mutableListOf<VegaEcuadorDispatchLots>()
        val addedLotId = addedLotList.map { it.batchNumber }
        val addedLotId1 = addedLotList.map { it.materialCode }
        val addedLotId2 = addedLotList.map { it.storageLocationCode }
        list.forEach {
            if (!addedLotId.contains(it.batchNumber)) {
                it.wbTempId = model?.wbTempId!!
                saveLot.add(it)
            } else if (addedLotId.contains(it.batchNumber)) {
                addedLotList.forEach { item ->
                    if (it.batchNumber.equals(item.batchNumber) && !it.materialCode.equals(item.materialCode)) {
                        it.wbTempId = model?.wbTempId!!
                        saveLot.add(it)
                    }
                }
            }
        }
        saveLot.distinct()
        //vm.saveProcessOrderAndLotDetails(saveLot, model!!)
        saveLot.let { addedLotList.addAll(it) }
        addedLotList.distinct()
        setUpLotsAdapter(addedLotList)
    }

    private fun removeCurrentItem(pos: Int) {
        vm.deleteLot(addedLotList.get(pos).batchNumber, model?.wbTempId.toString())
        addedLotList.removeAt(pos)
        //binding.rvLots.adapter?.notifyDataSetChanged()
        //binding.rvLots.adapter?.notifyItemRemoved(pos)
        //binding.rvLots.adapter?.notifyItemRangeChanged(0, addedLotList.size)
        setUpLotsAdapter(addedLotList)
    }

    private fun validateWeight(): Boolean {
        selectedSTO.forEach { sto ->
            var weightLimit = 0.0
            addedLotList.forEach { lot ->
                if (lot.materialCode.equals(sto.materialCode))
                    weightLimit = weightLimit.plus(lot.editedWeight?.toDouble()!!)
            }
            if (weightLimit.formatTwoDigits().toDouble() > sto.menge?.toDouble()!!) {
                Toast.makeText(activity, getString(R.string.sto_weight_error), Toast.LENGTH_SHORT)
                    .show()
                return false
            }
        }
        return true
    }

    private fun validateLotWeight(): Boolean {
        val selected = addedLotList.filter { !it.isLowerWeight }
        val emptyWeight = addedLotList.filter { it.editedWeight?.length ?: "" == 0 }
        return selected.isEmpty() && emptyWeight.isEmpty()
    }
}
