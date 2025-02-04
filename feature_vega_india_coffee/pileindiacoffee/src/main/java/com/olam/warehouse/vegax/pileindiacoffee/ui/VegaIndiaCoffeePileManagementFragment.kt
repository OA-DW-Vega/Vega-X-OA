package com.olam.warehouse.vegax.pileindiacoffee.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.System.DATE_FORMAT
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.pileindiacoffee.R
import com.olam.warehouse.vegax.pileindiacoffee.databinding.FragmentIndiaCoffeePileManagementBinding
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.model.VegaCoffeeThirdPartyPileLotListModel
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.INVENTORY_FRAG
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.PILE_MANAGEMENT_EDIT
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.PILE_MANAGEMENT_SELECTION
import kotlinx.android.synthetic.main.item_india_coffee_pile_management.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaIndiaCoffeePileManagementFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_india_coffee_pile_management
    private lateinit var binding: FragmentIndiaCoffeePileManagementBinding
    private var callBack: CallBack? = null
    private var vendorList = mutableListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var materialsList = ArrayList<String>()
    private var materialArrayList = ArrayList<String>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private val vm: VegaIndiaCoffeePileManagementViewModel by viewModel()
    private var materialCode: String = ""
    private var materialName: String = ""
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var fromVendorList = ArrayList<VegaVendor>()
    private var fromVendor = VegaVendor()
    private var fromMaterial = VegaMaterial()
    private var moreWeightBatches = ""
    private var isReceived = false
    private var isAlreadyLoading = false
    private var dispatchType = ""
    private var isEditLot = false
    private var alreadySelected = arrayListOf<VegaCocoaDispatchLots>()
    private var isThirdPartyMaterial: Boolean = false

    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCocoaDispatchLots, materialArrayList: ArrayList<String>)
        fun replaceFragment(lots: ArrayList<VegaCocoaDispatchLots>)
        fun replaceFragment(
            fragment: String, model: ArrayList<VegaCocoaDispatchLots>, materialArrayList: String,
            fromVendorList: String
        )

        fun replaceFragment(
            receivingType: String,
            data: Any
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }


    companion object {
        fun newInstance(
            edit: Boolean,
            alreadySelected: ArrayList<VegaCocoaDispatchLots>
        ) = VegaIndiaCoffeePileManagementFragment().putArgs {
            putBoolean("EDIT_LOT", edit)
            putParcelableArrayList(PILE_MANAGEMENT_EDIT, alreadySelected)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIndiaCoffeePileManagementBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("pilemanagement/ui//VegaCoffeePileManagementFragment").title("PileMangement")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        // alreadySelected = arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(PILE_MANAGEMENT_EDIT) as ArrayList
        isEditLot = arguments?.getBoolean("EDIT_LOT") ?: false
        vm.supplier.observe(viewLifecycleOwner, Observer {
            vendorList = it.toMutableList()
        })
        vm.getSuppliers()

        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it.toMutableList()
        })
        vm.getProducts()

        binding.tvMaterialDropdown.setOnClickListener {
            materialArrayList.clear()
            showSingleMaterialSelectDialog(materialList)
            //showMaterialDialog(materialList)
        }
        binding.tvVendorDropdown.setOnClickListener {
            showSingleSelectDialog()
        }

        binding.clScan.setOnClickListener { moveToScan() }

        binding.clInventory.setOnClickListener {

            callBack?.replaceFragment(
                INVENTORY_FRAG,
                VegaCoffeeThirdPartyPileLotListModel(
                    selectedList = vm.lotsList,
                    isMultipleAdd = false,
                    material = getMaterialList(), vendorCode = fromVendor.vendorCode
                )
            )
        }

        binding.btProceed.setOnClickListener {
            if (isEditLot) activity?.onBackPressed() else validateProceed()
        }

        binding.btAdd.setOnClickListener { vm.validateLot(binding.etEnterContainer.text.toString()) }

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })


        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    binding.etEnterContainer.text.toString()
                )
        })
    }

    private fun fetchLotDetails(lotId: String) {
        binding.etEnterContainer.hideKeyboard()
        binding.etEnterContainer.setText("")
        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(lotId, getMaterialList(), getPlantDetails().plantId)
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        when (it1.size == 1) {
                            true -> updateAdapter(it1 as ArrayList<VegaCocoaDispatchLots>)
                            else -> chooseOneLotDialog(it1)
                        }

                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun chooseOneLotDialog(lots: List<VegaCocoaDispatchLots>) {
        val lotItem = lots.map {
            getString(com.olam.warehouse.login.R.string.lot_no).plus(" : ").plus(it.batchNumber).plus("\n")
                .plus(getString(R.string.weight)).plus(" : ").plus(it.weight).plus(" ").plus(it.unitOfMeasure)
                .plus("\n").plus(getString(R.string.storage_location)).plus(" : ").plus(it.storageLocationCode)
        }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_lot)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = lotItem) { _, index, text ->
                updateAdapter(lots)
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun updateAdapter(list: List<VegaCocoaDispatchLots>) {
        binding.etEnterContainer.setText("")
        if (vm.lotsList.isNotEmpty())
            vm.removeLot(vm.lotsList[0].batchNumber)
        list.forEach {
            it.apply {
                weightToDispatchUOM = unitOfMeasure
                vendorWithTransferType = vm.model.vendorWithTransferType
            }
        }
        vm.addLoTInDB(list)
        vm.lotsList.addAll(list)
        setUpAdapter(list as ArrayList<VegaCocoaDispatchLots>)
        enableProceed(true)
    }

    private fun getCurrentDate(type: String) {
        val stamp = Timestamp(System.currentTimeMillis())
        val date = Date(stamp.time)
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        when (type) {
            "START" -> vm.salesOrder.startTime = DateUtils.getCurrentTimeInMills().toString()
            "END" -> vm.salesOrder.endTime = DateUtils.getCurrentTimeInMills().toString()
        }
    }

    private fun enableProceed(enable: Boolean) {
        binding.btProceed.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
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


    private fun validateProceed() {
        if (vm.lots.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    movetoPileSelection()
                } else Toast.makeText(
                    activity,
                    getString(R.string.lot_more_weight).plus(" - ").plus(moreWeightBatches),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                activity,
                getString(R.string.zero_weight_error),
                Toast.LENGTH_SHORT
            ).show()
        } else Toast.makeText(
            activity,
            getString(R.string.please_add_lot),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun movetoPileSelection() {
        callBack?.replaceFragment(
            PILE_MANAGEMENT_SELECTION,
            vm.lots,
            vm.lots[0].materialCode,
            binding.tvVendorDropdown.text.toString()
        )
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            vm.lots.filter { it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals("") }
        return emptyWeight.isEmpty()
    }

    private fun validateLotWeight(): Boolean {
        val selected = vm.lots.filter { !it.isLowerWeight }
        moreWeightBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            dispatchLotsList.addAll(it.data?.data!!)
                            filterFromVendor()
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

    private fun filterFromVendor() {
        fromVendorList.clear()
        val fromVendor = dispatchLotsList.map { it.vendor ?: "" }.toSet().toList()
        vendorList.forEach {
            if (fromVendor.toString().contains(it.vendorCode)) {
                fromVendorList.add(it)
            }
        }
        hideLoading()
    }

    private fun getMaterialList(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add(fromMaterial.materialCode)
        return list
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    private fun showSingleSelectDialog() {
        val list = ArrayList<String>()
        if (isThirdPartyMaterial) {
            list.addAll(fromVendorList.map { it.vendorCode.plus("-").plus(it.vendorName) })
        } else {
            list.addAll(vendorList.map { it.vendorCode.plus("-").plus(it.vendorName) })
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                getString(R.string.select_vendor),
                false, true, false,
                list,
                activity!!,
                this,isOrigin = false,isDepartment = false
            )

        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun showSingleMaterialSelectDialog(materialList: List<VegaMaterial>) {
        val list = ArrayList<String>()
        list.addAll(materialList.map { it.materialCode.plus("-").plus(it.materialName) })
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                getString(R.string.select_material),
                true, false, false,
                list,
                activity!!,
                this,isOrigin = false,isDepartment = false
            )


        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isMaterial: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        val code = data.split("-")
        if (isVendor) {
            binding.tvVendorDropdown.text = data
            binding.tvVendorDropdown.hideKeyboard()
            fromVendor = vendorList.single { it.vendorCode == code[0] }

        }
        if (isMaterial) {
            binding.tvMaterialDropdown.text = data
            binding.tvMaterialDropdown.hideKeyboard()
            fromMaterial = materialList.single { it.materialCode == code[0] }
            materialArrayList.add(materialCode)
            isThirdPartyMaterial = fromMaterial.thirdPartyFlag.equals("X")
            if (isThirdPartyMaterial) {
                vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })
                vm.getStockList(getMaterialList())
            }
        }
    }

    fun updateLotList(lots: ArrayList<VegaCocoaDispatchLots>) {
        setUpAdapter(lots)
    }

    private fun setUpAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
        val lots = list
        binding.rvLots.setUp(lots, R.layout.item_india_coffee_pile_management, { item, pos ->
            tvLotId.text = item.batchNumber
            tvStLocationValue1.text = item.storageLocationCode
            tvGradeValue.text = item.materialName
            tvWeightValue.text = item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(item.unitOfMeasure)
            cbSelectAll.isChecked = item.isChecked
            etWeight.setText(item.editedWeight)
            llSelectAll.setOnClickListener { view ->
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    cbSelectAll.isChecked = true
                    item.editedWeight = item.weight?.toDouble()?.formatThreeDigits()

                } else {
                    cbSelectAll.isChecked = false
                    item.editedWeight = "0"
                }
                etWeight.setText(item.editedWeight)
                vm.lots.remove(item)
                vm.lots.add(item)
            }

            ivClose.setOnClickListener { view -> showConformationDialog(pos, view) }
            etWeight.onChange {
                if (it.isNotEmpty()) {
                    val editVal: String?
                    val dot = it.get(0).toString()
                    editVal = if (dot == ".") {
                        if (it.length == 1) "0.0" else "0".plus(it)
                    } else it

                    item.editedWeight = editVal
                    val come: Int? = editVal.toDouble().compareTo(item.weight?.toDouble() ?: 0.0)
                    if (come ?: 0 <= 0) {
                        item.isLowerWeight = true
                        etWeight.error = null
                        vm.lots.forEach { item ->
                            if (item.batchNumber.equals(item.batchNumber) && item.materialCode.equals(item.materialCode)){
                                item.editedWeight =editVal
                                //item.weight =editVal
                            }
                        }
                    } else {
                        item.isLowerWeight = false
                        etWeight.error = etWeight.context.getString(R.string.less_weight_error)
                    }
                } else {
                    item.editedWeight = "0"
                }
                vm.lots.remove(item)
                vm.lots.add(item)
            }
        })

    }

    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.removeLot(vm.lots[position].batchNumber)
                    vm.lots.clear()
                    setUpAdapter(vm.lots)
                },
                { dismiss() })
        }
    }


}
