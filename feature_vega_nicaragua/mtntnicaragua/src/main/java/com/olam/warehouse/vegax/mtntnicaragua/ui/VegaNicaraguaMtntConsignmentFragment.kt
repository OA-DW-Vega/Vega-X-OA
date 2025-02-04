package com.olam.warehouse.vegax.mtntnicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.modal.OfflineInventory
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.prepareGrnInventoryDetails
import com.olam.warehouse.master.common.utils.prepareStocksToInventoryDeatils
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicPurchaseOrderModel
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentNicMtntConsignmentLayoutBinding
import com.olam.warehouse.vegax.mtntnicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 11/11/2020.
 */
class VegaNicaraguaMtntConsignmentFragment : BaseFragment(), VegaSingleSelectCommonListener {

    override val layoutResourceId = R.layout.fragment_nic_mtnt_consignment_layout
    private lateinit var binding: FragmentNicMtntConsignmentLayoutBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var callBack: Callback? = null

    private var supplierList = mutableListOf<VegaVendor>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var purchaseOrderList = mutableListOf<VegaNicPurchaseOrderModel>()
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var selectedPurchaseOrder: VegaCocoaPurchaseOrders? = null
    private var selectedPurchaseOrderList: ArrayList<VegaCocoaPurchaseOrders>? = null
    private var materials: ArrayList<VegaCocoaPurchaseOrders>? = null
    private var purchaseOrder = ArrayList<VegaCocoaPurchaseOrders>()
    private var materialModelList: ArrayList<VegaCoffeePurchaseOrderMaterialModel> = ArrayList()
    private var productList = emptyList<VegaMaterial>()
    private var gradeList = mutableListOf<VegaQualitative>()
    private var certicateList = mutableListOf<VegaQualitative>()
    private var certicateFilterList = mutableListOf<VegaQualitative>()
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private var dispatchLotsList = mutableListOf<VegaNicaraguaGRNInventoryDetails>()

    companion object {
        fun newInstance(vegaNicaraguaMtnt: VegaNicaraguaMtnt) = VegaNicaraguaMtntConsignmentFragment().putArgs {
            putParcelable(MODEL_BUNDLE, vegaNicaraguaMtnt)
        }
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
        binding = FragmentNicMtntConsignmentLayoutBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        val mtnt = arguments?.getParcelable(MODEL_BUNDLE) ?: VegaNicaraguaMtnt()
        vm.mtnt = mtnt
        updateValues()
    }

    private fun initUI() {
        binding.btnProceed.setOnClickListener { validateProceed() }
        binding.tvWhValue.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_sending_plant), SENDING_PLANT)
        }
        binding.tvstoValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                getString(R.string.select_sto),
                STO_NO
            )
        }
        binding.tvMaterialValue.setOnClickListener {
            if (binding.tvstoValue.text.toString()
                    .isNotEmpty()
            ) showSingleSelectDialog(getString(R.string.select_material), MATERIAL)
        }

        binding.tvVendorNameValue.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_vendor), VENDORNAME)
        }

        binding.tvQualityGradeValue.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_grade), QUALITY_GRADE)
        }

        binding.tvCertificationValue.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_certification), CERTIFICATION)
        }

        binding.tvVendorValue.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_trans_vendor), VENDOR)
        }

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            if (it != null)
                supplierList = it as MutableList
        })
        vm.getSuppliers()
        vm.getAllProduct()
        vm.allProduct.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                productList = it
            }
            if (!isOnline()) vm.getPurchaseOrderOffline()
        })
        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.purchaseOrderOffline.observe(viewLifecycleOwner, Observer { updatePurchaseOrderOffline(it) })
        if (isOnline()) vm.getPurchaseOrder("")

        vm.grade.observe(viewLifecycleOwner, Observer {
            gradeList = it.toMutableList()
        })
        vm.certification.observe(viewLifecycleOwner, Observer {
            certicateList = it.toMutableList()
            certicateFilterList = it.toMutableList()
        })

        vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
            materialQualityGradeList = it.toMutableList()
        })
//        vm.stockLots.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.stockLotsSap.observe(viewLifecycleOwner, Observer { updateSapUI(it) })
        vm.stockLotsOffline.observe(viewLifecycleOwner, Observer { updateUIOffline(it) })
        vm.weighBridgeLotsWithBags.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                disableHeaderField(it.lineItems.map { it.lots }.isEmpty())
            }
        })
    }

    private fun disableHeaderField(flag: Boolean) {
        binding.tvWhValue.isEnabled = flag
        binding.tvstoValue.isEnabled = flag
        binding.tvMaterialValue.isEnabled = flag
        binding.tvVendorNameValue.isEnabled = flag
        binding.tvQualityGradeValue.isEnabled = flag
        binding.tvCertificationValue.isEnabled = flag
    }

    private fun updatePurchaseOrderOffline(data: List<VegaCocoaPurchaseOrders>?) {
        data?.let {
            purchaseOrder = it as ArrayList<VegaCocoaPurchaseOrders>
            val location =
                purchaseOrder.map { it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId)) }.distinct()
            storageLocation.addAll(location)
            if (vm.mtnt.tempId.isNotEmpty()) getData()
        }
    }

    private fun updateSapUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            val lotAll = prepareStocksToInventoryDeatils(dataValue, productList)
                            dispatchLotsList = lotAll
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


    private fun updateUI(response: Resource<GenericReqAndResp<OfflineInventory>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            dispatchLotsList = prepareGrnInventoryDetails(dataValue, productList)
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

    private fun updateUIOffline(response: List<VegaNicaraguaGRNInventoryDetails>?) {
        response?.let {
            dispatchLotsList.clear()
            val dataValue = it
            dispatchLotsList.addAll(dataValue)
        }
    }

    private fun updateValues() {
        if (vm.mtnt.tempId.isNotEmpty()) {
            binding.tvWhValue.text = vm.mtnt.sendingPlant
            binding.tvstoValue.text = vm.mtnt.purchaseDocNum
            binding.tvStoQuantityValue.text = vm.mtnt.soWeight.plus(" ").plus(vm.mtnt.soUOM)
            binding.tvMaterialValue.text = vm.mtnt.materialName
            binding.tvVendorNameValue.text = vm.mtnt.vendorName
            binding.tvQualityGradeValue.text = vm.mtnt.qualityGrade
            binding.tvCertificationValue.text = vm.mtnt.certification
            binding.tvVendorValue.text = vm.mtnt.transportVendor
            binding.tvTruckNoValue.setText(vm.mtnt.vehicleNumber)
            binding.tvDriverNameValue.setText(vm.mtnt.driverName)
            binding.tvDriverNoValue.setText(vm.mtnt.contactNumber)

            vm.getMtntWithLots(vm.mtnt.tempId)
        }
    }

    private fun getData() {
        if (!vm.mtnt.sendingPlant.isNullOrEmpty()) getPurchaseOrderByStorageLocation(vm.mtnt.sendingPlant
                ?: "-".split("-")[0])
        getWeight(vm.mtnt.purchaseDocNum.toString())
        getQualityGrades()
    }

    private fun getQualityGrades() {
        /*  binding.tvGradeLabel.visible()
          binding.tvGrade.visible()
          receivingData.grade = ""*/
        vm.getGrades(vm.mtnt.materialCode.toString())
        vm.getCertification(vm.mtnt.materialCode.toString())
        vm.getMaterialQualityGrades(vm.mtnt.materialCode.toString())
    }

    private fun validateProceed() {
        when {
            binding.tvWhValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_send_plant))
            binding.tvstoValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_sto_no))
            binding.tvMaterialValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_material))
            binding.tvStoQuantityValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_sto_quantity))
            binding.tvQualityGradeValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_quality_grade))
//            binding.tvVendorNameValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_vendor_name))
            binding.tvTruckNoValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_truck_no))
            binding.tvDriverNameValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_driver_name))
            binding.tvVendorValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_trans_vendor))
            else -> {
                prepareTruckWb()
                vm.saveWeighBridgeDetails()
                callBack?.replaceFragment(FRAG_ADD_LOT, vm.mtnt)
            }
        }
    }

    private fun prepareTruckWb() {
        if (vm.mtnt.tempId.isEmpty()) {
            val randomDouble = "TMP".plus(Random.nextLong().toString())
            vm.mtnt.tempId = randomDouble
        }
        vm.mtnt.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
        vm.mtnt.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
        vm.mtnt.weighBridgeType = MTNT_WEIGHSCALE
        vm.mtnt.netWeight = selectedPurchaseOrder?.menge
        vm.mtnt.unitsOfMeasure = selectedPurchaseOrder?.meins
        vm.mtnt.plantId = selectedPurchaseOrder?.plantId ?: ""
        vm.mtnt.recStorageLocationCode = selectedPurchaseOrder?.storageLocationCode
        val recplant = vm.mtnt.sendingPlant?.split("-")
        if (recplant?.isNotEmpty() == true)
            vm.mtnt.recPlantId = recplant.get(0).toString()
        vm.mtnt.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.mtnt.driverName = binding.tvDriverNameValue.text.toString()
        vm.mtnt.contactNumber = binding.tvDriverNoValue.text.toString()
        // vm.mtnt.erdat = getCurrentTimeInMills().toString()
    }

    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaNicPurchaseOrderModel>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        purchaseOrderList = it1 as MutableList<VegaNicPurchaseOrderModel>
                    }
                    filterSTONumber()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun filterSTONumber() {
        for (item in purchaseOrderList) {
            purchaseOrder.addAll(item.purchaseOrders)
        }
        val location = purchaseOrder.map { it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId)) }.distinct()
        storageLocation.addAll(location)
        if (vm.mtnt.tempId.isNotEmpty()) getData()
    }

    private fun getMTNTPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }

    private fun showSingleSelectDialog(title: String, currentFalg: String) {
        var list = ArrayList<String>()
        when (currentFalg) {
            SENDING_PLANT -> list = storageLocation
            STO_NO -> list = STONumbers
            MATERIAL -> {
                val matList = ArrayList<String>()
                val item = materialModelList.map { it.materialName.toString() }
                matList.addAll(item)
                list = matList
            }
            VENDORNAME -> {
                /* dispatchLotsList.forEach { item ->
                     item.vendorName = supplierList.find { it.vendorCode.equals(item.vendorCode) }?.vendorName
                 }*/
                list.clear()
                list.addAll(if (dispatchLotsList.size > 0) dispatchLotsList.map {
                    it.vendorCode.plus("-").plus(it.vendorName)
                }
                    .distinct() else ArrayList())
            }
            QUALITY_GRADE -> {
                val gradeListFilter = mutableListOf<VegaQualitative>()
//                gradeListFilter.addAll(gradeList)
                materialQualityGradeList.forEach { qualityGrade ->
                    gradeListFilter.addAll(gradeList.filter {
                        it.charValue.split(" ").get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
                    })
                }
                list = gradeListFilter.map { it.charValue.plus("-").plus(it.descValue) } as ArrayList<String>
            }
            CERTIFICATION -> {
                list = certicateFilterList.map { it.charValue } as ArrayList<String>
            }
            VENDOR -> list = supplierList.map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFalg,
                list,
                activity!!,
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            SENDING_PLANT -> {
                binding.tvWhValue.text = data/*.split("-")[0]*/
                val da = purchaseOrder.filter { it.warehouseId.equals(data.split("-")[0]) }
                //vm.getPurchaseOrder(da[0])
                vm.mtnt.sendingPlant = data
                if (da.size > 0) {
                    vm.mtnt.storageLocationCode = da[0].storageLocationCode
                    vm.mtnt.storageLocationName = da[0].storageLocationName
                }
                getPurchaseOrderByStorageLocation(data.split("-")[0])
                binding.tvstoValue.text = ""
                binding.tvMaterialValue.text = ""
                binding.tvStoQuantityValue.text = ""
                binding.tvVendorNameValue.text = ""
                binding.tvQualityGradeValue.text = ""
                binding.tvCertificationValue.text = ""
            }
            STO_NO -> {
                getWeight(data)
                selectedPurchaseOrder?.storageLocationName = vm.mtnt.storageLocationName
                binding.tvstoValue.text = data
                enableProceed()
                binding.tvMaterialValue.text = ""
                binding.tvStoQuantityValue.text = ""
                binding.tvVendorNameValue.text = ""
                binding.tvQualityGradeValue.text = ""
                binding.tvCertificationValue.text = ""
            }
            VENDOR -> {
                binding.tvVendorValue.text = data
                vm.mtnt.transportVendor = data.split("-")[1]
                vm.mtnt.transportVendorID = data.split("-")[0]
                enableProceed()
            }
            MATERIAL -> {
                binding.tvMaterialValue.text = data
                vm.mtnt.materialName = data

                val material = materialModelList.filter { it.materialName.equals(data) }
                if (material.size > 0) {
                    vm.mtnt.materialCode = material[0].materialCode
                    vm.mtnt.soWeight = material[0].soWeight
                    vm.mtnt.soUOM = material[0].uom
                    binding.tvStoQuantityValue.text = material[0].soWeight.plus(" ").plus(material[0].uom)
                    if (vm.mtnt.materialCode.toString().startsWith("0000005")) {
                        if (AppUtils.isOnline()) vm.getStockListSap(arrayListOf(vm.mtnt.materialCode.toString())) else vm.getStockListOffline(
                            arrayListOf(vm.mtnt.materialCode.toString())
                        )
                        binding.tvVendorName.visible()
                        binding.tvVendorNameValue.visible()
                    } else {
                        binding.tvVendorName.gone()
                        binding.tvVendorNameValue.gone()
                        vm.mtnt.vendorCode = ""
                        vm.mtnt.vendorName = ""
                    }
                }
                val filteItem = selectedPurchaseOrderList?.filter { it.materialCode.equals(vm.mtnt.materialCode) }
                if (filteItem?.isNotEmpty() == true) selectedPurchaseOrder = filteItem[0]
                vm.mtnt.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
                vm.mtnt.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
                vm.mtnt.unitsOfMeasure = selectedPurchaseOrder?.meins
                vm.mtnt.netWeight = selectedPurchaseOrder?.meins
                vm.mtnt.plantId = selectedPurchaseOrder?.menge
                getQualityGrades()
                enableProceed()
                binding.tvVendorNameValue.text = ""
                binding.tvQualityGradeValue.text = ""
                binding.tvCertificationValue.text = ""
            }
            VENDORNAME -> {
                binding.tvVendorNameValue.text = data
                /* val qtyGrades = dispatchLotsList.filter { it.vendorCode.toString().equals(data.split("-")[0]) }
                     .map { it.qualityGrade }
                 val grList = gradeList.filter { qtyGrades.contains(it.charValue) }
                 gradeList = grList.toMutableList()*/
                vm.mtnt.vendorCode = data.split("-")[0]
                vm.mtnt.vendorName = data.split("-")[1]
            }
            QUALITY_GRADE -> {
                //if( vm.mtnt.vendorCode?.isNotEmpty()==true) {
                val certificate = dispatchLotsList.filter {
                    it.qualityGrade.toString().equals(data.split("-")[0])
                }
                    .map { it.certification }
                val crList = certicateList.filter { certificate.contains(it.charValue) }
                certicateFilterList = /*crList.toMutableList()*/certicateList
                //}
                vm.mtnt.qualityGrade = data.split("-")[0]
                vm.mtnt.qulityGradeDesc = data.split("-")[1]
                binding.tvQualityGradeValue.text = data
                binding.tvCertificationValue.text = ""
                vm.mtnt.certification = ""
                var gradeData = materialQualityGradeList.filter {
                    it.gradeCode == vm.mtnt.qualityGrade?.split(" ")
                        ?.get(vm.mtnt.qualityGrade?.split(" ")!!.size - 1) ?: 0
                }.get(0)
                // vm.mtnt.bagTareWeight=gradeData.tareWeight
                vm.mtnt.bagType = gradeData.bagType
            }
            CERTIFICATION -> {
                vm.mtnt.certification = data
                binding.tvCertificationValue.text = data
            }
        }
    }

    private fun getPurchaseOrderByStorageLocation(warehouseId: String) {
        STONumbers.clear()
        val orderList = purchaseOrder.filter { it.warehouseId == warehouseId.trim() }
        STONumbers.addAll(orderList.map { it.purchaseDocNum }.toSet().toList())

    }

    private fun getWeight(purchaseId: String) {
        materialModelList.clear()
        materials =
            purchaseOrder.filter { s -> s.purchaseDocNum == purchaseId } as ArrayList<VegaCocoaPurchaseOrders>
        materials?.forEach {
            val materialModel = VegaCoffeePurchaseOrderMaterialModel()
            val mat = productList.filter { it1 -> it.materialCode.contains(it1.materialCode) }
            if (mat.size > 0) materialModel.materialName = mat[0].materialName
            materialModel.materialCode = it.materialCode
            materialModel.soNumber = it.purchaseDocNum
            materialModel.soWeight = it.menge
            materialModel.uom = it.meins
            materialModel.purchaseOrderNum = it.purchaseDocNum
            materialModel.purchaseOrderDesc = it.purchaseDocDesc ?: ""
            materialModelList.add(materialModel)
        }
        selectedPurchaseOrderList = materials
    }

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvVendorValue.text.isNotEmpty()
                    )
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    fun onBackRefreshed() {
        vm.getMtntWithLots(vm.mtnt.tempId)
    }
}
