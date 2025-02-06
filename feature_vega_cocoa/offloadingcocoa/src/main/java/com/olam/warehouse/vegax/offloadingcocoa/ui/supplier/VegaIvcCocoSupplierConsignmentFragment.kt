package com.olam.warehouse.vegax.offloadingcocoa.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.common.VegaTrackTraceFragment
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaQualityWBDetail
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCoffeeSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentIvcCocoSupplierConsignmentLayoutBinding
import com.olam.warehouse.vegax.offloadingcocoa.databinding.ItemOffloadingBagDetailsBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcocoa.utils.MTNR_WEIGHSCALE_SUMMARY
import com.olam.warehouse.vegax.offloadingcocoa.utils.OFFLOAD_DATA
import com.olam.warehouse.vegax.offloadingcocoa.utils.SUPPLIER_APPEND
import com.olam.warehouse.vegax.offloadingcocoa.utils.WB01
import com.olam.warehouse.vegax.offloadingcocoa.utils.WEIGHBRIDGE_WEIHSCALE
import com.olam.warehouse.vegax.offloadingcocoa.utils.WEIGHSCALE
import com.olam.warehouse.vegax.offloadingcocoa.utils.WS01
import com.olam.warehouse.vegax.offloadingcocoa.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIvcCocoSupplierConsignmentFragment : BaseFragment(), VegaSingleSelectListener,
    VegaCoffeeSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_ivc_coco_supplier_consignment_layout
    private lateinit var binding: FragmentIvcCocoSupplierConsignmentLayoutBinding
    private var callBack: VegaCoCoaOffloadReplaceFragmentCallback? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var addWeightPosition: Int = 0
    private val vm: VegaCoCoaOffloadingViewModel by viewModel()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var selectedReceivingLocation = VegaCustomStLocation()
    private var batchList = mutableListOf<VegaCoCoaReceiveLots>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var editLotId: String = ""
    private var materialCode: String = ""
    private var offloadData = VegaCoCoaReceiving()
    private var isNewTruckCall = true
    private var materialProduct = ArrayList<VegaMaterial>()
    private var batchInfo = VegaCoCoaReceiveLots()
    private var OriginList = mutableListOf<VegaQualitative>()
    private var Departmentlist = mutableListOf<VegaQualitative>()
    private var yearFilter = mutableListOf<String>()
    private var year = ""
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var bagList = arrayListOf<VegaCoCoaOffloadingBagMaterial>()

    /*Track & Trace*/
    var ttProcurementType = ""
    var ttDirectFarmerDataList = java.util.ArrayList<TrackTraceFarmerModel>()
    var ttFarmerlessTransactionDetails = TrackTraceTransactionIdDetails()
    var ttIndirectSourceLotDetails = TrackTraceSourceLotDetails()
    var ttDbFarmerList = mutableListOf<VegaTrackTraceFarmerData>()
    var isTTComplaint = false
    var ttMaterialCode= ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoCoaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance(offload: VegaCoCoaReceiving, procurementType: String) = VegaIvcCocoSupplierConsignmentFragment().putArgs {
            putParcelable(OFFLOAD_DATA, offload)
            putString(Constants.PROCUREMENT_TYPE, procurementType)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        offloadData = arguments?.getParcelable(OFFLOAD_DATA)!!
        binding = FragmentIvcCocoSupplierConsignmentLayoutBinding.inflate(layoutInflater)
        isNewTruckCall = offloadData.weighBridgeId.isBlank()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcoffee/ui/mtnr/VegaCoffeeMtnrTypeSelectFragment").title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
        if (isNewTruckCall) {
            clearAllValues()
        }
        ttProcurementType = arguments?.getString(Constants.PROCUREMENT_TYPE)?:""

        if (offloadData.materialCode?.length == 18)
            ttMaterialCode = offloadData.materialCode?.takeLast(12).toString()
        else ttMaterialCode = offloadData.materialCode.toString()

        if(ttProcurementType.isNotEmpty()) {
            loadTTFragment()
        }
        enableProceed()
        vm.getLocations()
        vm.getCustomLocations()
        vm.getSuppliers()
        if (offloadData.imageString.equals(WEIGHBRIDGE_WEIHSCALE)) {
            binding.tvyear.gone()
            binding.tvYearValue.gone()
           
        } else {
            binding.tvyear.gone()
            binding.tvYearValue.gone()
        }
        binding.tvWhValue.setOnClickListener {
            showSingleSelectDialog(
                true,
                getString(R.string.select_material),
                false,
                false, false, false
            )
        }

        binding.tvstoValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_supplier),
                true, false, false, false
            )
        }
        binding.tvReceivingValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_receive_loc),
                false, false, false, false
            )
        }
       
//        vm.getPOList()
        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            updateWeighbridgeValueUI(it)
        })
        if (AppUtils.isOnline())
            vm.getWeighBridgeIdDetail(offloadData.weighBridgeId)

        if (offloadData.materialCode?.length != 18)
            materialCode = "000000".plus(offloadData.materialCode)
        else
            materialCode = offloadData.materialCode!!
        /*vm.getQualityParams(materialCode, false, offloadData.weighBridgeId)
        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateoriginlist(it)
        })*/
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it/*.filter { it.storageLocationType.equals("P") }*/.toMutableList()
            val receive = it
            if (!isNewTruckCall) {
                val item = receive.singleOrNull { offloadData.dstorageLocationCode == it.procureLocationCode }
                offloadData.dstorageLocationName = item?.procureLocationName ?: ""
                binding.tvReceivingValue.text =
                    offloadData.storageLocationCode.plus("-").plus(offloadData.dstorageLocationName)
            }
            if (customLocationList.size == 1) {
                binding.tvReceivingValue.text =
                    customLocationList[0].procureLocationCode.plus("-").plus(customLocationList[0].procureLocationName)
                offloadData.storageLocationCode
                vm.vegaCoCoaReceivingData.storageLocationCode = customLocationList[0].procureLocationCode
                vm.vegaCoCoaReceivingData.storageLocationName = customLocationList[0].procureLocationName
                offloadData.storageLocationCode = customLocationList[0].procureLocationCode
                offloadData.storageLocationName = customLocationList[0].procureLocationName
            }



            if (!isNewTruckCall) {
                updateDateFromList()
            }
        })

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()
        })

        binding.tvDriverNoValue.onChange { enableProceed() }
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.btProceed.setOnClickListener { moveToSummary() }
       

        vm.product.observe(viewLifecycleOwner, Observer {
            materialProduct.clear()
            materialProduct.addAll(it)
        })
        vm.productName.observe(viewLifecycleOwner, Observer {
            materialProduct.clear()
            materialProduct.addAll(it)
        })

        if (ttMaterialCode.isNotEmpty())
            vm.getProductByName(offloadData.materialCode?.takeLast(12).toString())
        else
            vm.getProducts()

        binding.tvYearValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_year),
                false,
                false, false, false, true
            )
        }
        updateYearValues()

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()

        })
        vm.getMaterials()
        binding.llBagDetails.tvBagType.setOnClickListener {
            when{
                bagList.groupBy { it.bagType }.size>=3 -> context?.toast(getString(R.string.max_two_bags))
                else ->{
                    showSingleSelectDialog(
                        false,
                        getString(R.string.select_material),
                        false,
                        true, false, false
                    )
                }
            }
        }
        binding.llBagDetails.etBagCount.onChange {
            if(it.isEmpty())binding.llBagDetails.tvAdd.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorSecondaryGrey))
            else binding.llBagDetails.tvAdd.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        }
        binding.llBagDetails.tvAdd.setOnClickListener {
            when{
                bagList.groupBy { it.bagType }.size>=3 -> context?.toast(getString(R.string.max_two_bags))
                binding.llBagDetails.tvBagType.text.isEmpty() || binding.llBagDetails.etBagCount.text.isEmpty() ->showSnack(getString(R.string.enter_bag_details))
                else ->{
                    val bagMaterial = VegaCoCoaOffloadingBagMaterial()
                    bagMaterial.id = Random.nextInt()
                    bagMaterial.batchNumber = offloadData.materialCode.plus(offloadData.supplierCode)
                        .plus(offloadData.weighBridgeType)
                    bagMaterial.message = getString(R.string.stored_locally)
                    bagMaterial.mtnNumber = offloadData.weighBridgeId
                    bagMaterial.bagType = binding.llBagDetails.tvBagType.text.toString()
                    bagMaterial.bagCount = binding.llBagDetails.etBagCount.text.toString()
                    val currentBagItem = bagTypeList.filter { Regex("[^A-Za-z0-9 ]").replace(it.bagType.replace("\\s".toRegex(), ""), "").equals(Regex("[^A-Za-z0-9 ]").replace(bagMaterial.bagType.replace("\\s".toRegex(), ""), ""), true) }
                    if(currentBagItem.isNotEmpty())bagMaterial.tareWeight = currentBagItem[0].tareWeight
                    vm.saveBagDetails(bagMaterial)
                    binding.llBagDetails.tvBagType.text = ""
                    binding.llBagDetails.etBagCount.setText("")
                    vm.vegaCoCoaReceivingData.weighBridgeId = offloadData.weighBridgeId
                }
            }
        }
        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.getBagItems("","",offloadData.weighBridgeId)
    }

    private fun loadTTFragment(){
        if(ttProcurementType.isNotEmpty()) {
            val bundle = Bundle()
            bundle.putString(
                Constants.PROCUREMENT_TYPE, ttProcurementType
            )
            bundle.putParcelable(Constants.OFFLOADING,offloadData)
            displayFragment(VegaTrackTraceFragment.newInstance(bundle), false)
            if (ttProcurementType.equals(Constants.DIRECT)) {
                binding.tvsto.gone()
                binding.tvstoValue.gone()
            }
        }
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            "",
            allowStateLoss = true,
            containerViewId = R.id.flTTContainer,
            allowBackStack = flag
        )
    }
    fun replaceFragment(
    fragment: Fragment,
    tag: String?,
    allowStateLoss: Boolean = false,
    @IdRes containerViewId: Int,
    @AnimRes enterAnimation: Int = 0,
    @AnimRes exitAnimation: Int = 0,
    @AnimRes popEnterAnimation: Int = 0,
    @AnimRes popExitAnimation: Int = 0,
    allowBackStack: Boolean = false
    ) {
        val ft = activity?.supportFragmentManager
            ?.beginTransaction()
            ?.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        when {
            allowBackStack -> {
                ft?.add(containerViewId, fragment, tag)
                ft?.addToBackStack(tag)
            }
            else -> ft?.replace(containerViewId, fragment, tag)
        }
        if (!activity?.supportFragmentManager?.isStateSaved!!) {
            ft?.commit()
        } else if (allowStateLoss) {
            ft?.commitAllowingStateLoss()
        }
    }

    private fun updateBagItems(items: List<VegaCoCoaOffloadingBagMaterial>?) {
        items?.let {
            bagList.clear()
            bagList.addAll(it)
            setUpAdapter(bagList)
        }
    }

    private fun setUpAdapter(bagList: java.util.ArrayList<VegaCoCoaOffloadingBagMaterial>) {
        /*binding.llBagDetails.clBagDetails.forEachIndexed { index, view ->
            view.isEnabled = bagList.size<2
        }*/
        binding.llBagDetails.rvBagItems.setUpAdapter(
            bagList,
            R.layout.item_offloading_bag_details,
            ItemOffloadingBagDetailsBinding::inflate,
            { it, pos, bindingItem ->
                bindingItem.tvBagType.text = it.bagType
                bindingItem.tvBagCount.text = it.bagCount
                bindingItem.tvDelete.setOnClickListener { view->
                    vm.deleteBagDetailsById(it.id.toString())
                }
                //updateBagDetailsAsQuality(it, pos)

            })
    }

    private fun updateYearValues() {
        val current: Int = Calendar.getInstance().get(Calendar.YEAR)
        binding.tvYearValue.text = current.toString()
        year = current.toString()
        for (i in current.minus(1)..current) {
            yearFilter.add(i.toString())
        }
    }

    private fun getLocationName(storageLocationName: String?): String {
        val location = ""
        if (storageLocationName != null) {
            val item = wareHouseList.singleOrNull { it.storageLocationCode == storageLocationName.trim() }
            return item?.storageLocationName ?: ""
        }
        return location
    }

    private fun updateDateFromList() {
//        binding.tvWhValue.text = offloadData.materialCode.plus("-").plus(offloadData.materialName)
        binding.tvstoValue.text = offloadData.supplierCode.plus("-").plus(offloadData.supplierName)
        /*binding.tvReceivingValue.text =
            offloadData.dstorageLocationCode.plus("-").plus(offloadData.dstorageLocationName)*/
        //enableDisableHeaderItem(false)
        vm.vegaCoCoaReceivingData = offloadData
        //vm.getOBDDetailsSupIndo(offloadData.tempWBId.toString())
    }

    /*private fun updateoriginlist(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    OriginList.clear()
                    Departmentlist.clear()
                    it.forEach { item ->
                        if (item.qualityParameter.materialCode == materialCode && item.qualityParameter.nameChar == "CI_COFFEE_TRANS_ORIGIN") {
                            item.qualitative?.forEach { item1 ->
                                OriginList.add(item1)
                            }
                        }
                        if (item.qualityParameter.materialCode == materialCode && item.qualityParameter.nameChar == "CI_COFFEE_TRANS_DEPT") {
                            item.qualitative?.forEach { item1 ->
                                Departmentlist.add(item1)
                            }
                        }
                    }
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()

                    value.addAll(it)

                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }*/

    private fun moveToSummary() {
        vm.vegaCoCoaReceivingData = offloadData
        vm.vegaCoCoaReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.vegaCoCoaReceivingData.driverName = binding.tvDriverNameValue.text.toString()
        vm.vegaCoCoaReceivingData.truckDriverName = binding.tvDriverNameValue.text.toString()
        vm.vegaCoCoaReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
        vm.vegaCoCoaReceivingData.operatorName = binding.etOperatorName.text.toString()
        vm.vegaCoCoaReceivingData.imageString = offloadData.imageString
        vm.vegaCoCoaReceivingData.plantId = getPlantDetails().plantId
        vm.vegaCoCoaReceivingData.weighBridgeType = offloadData.weighBridgeType
        vm.vegaCoCoaReceivingData.imageString = offloadData.imageString
        vm.vegaCoCoaReceivingData.bagList = bagList
        validateInputs()
    }

    private fun showSingleSelectDialog(
        isWh: Boolean,
        title: String,
        isVendor: Boolean,
        isPoList: Boolean,
        isOrgin: Boolean, isDepartment: Boolean,
        isYear: Boolean = false
    ) {
        var list = ArrayList<String>()
        if (isVendor) {
            list.addAll(supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) })
        } else if (isWh) {
            if (isTTComplaint) {
                list = materialProduct.filter { it.complainceFlag == Constants.COMPLAINT }
                    .map { data -> data.materialCode.plus("-").plus(data.materialName) } as ArrayList<String>
            } else if (!isTTComplaint) {
                list = materialProduct.filter { it.complainceFlag == Constants.NON_COMPLAINT }
                    .map { data -> data.materialCode.plus("-").plus(data.materialName) } as ArrayList<String>
            }
            if(list.isEmpty()){
                list = materialProduct.map { data -> data.materialCode.plus("-").plus(data.materialName) } as ArrayList<String>
            }
        } else if (isPoList) {
            //Bag Details
            list.clear()
            val bagTypeQualitative = bagTypeList
            val bagTypes = bagTypeQualitative.map { data1 -> data1.bagType }
            list.addAll(bagTypes.sortedBy { it }.distinct())

        } else if (isYear) {
            list.addAll(yearFilter)
        } else if (isOrgin) {
            list = OriginList.map {
                it.charValue
            } as ArrayList<String>


        } else if (isDepartment) {
            list = Departmentlist.map {
                it.charValue
            } as ArrayList<String>


        } else {
            list = customLocationList.map {
                it.procureLocationCode.plus(" - ").plus(it.procureLocationName)
            } as ArrayList<String>
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh,
                isVendor,
                isPoList,
                list,
                requireActivity(),
                this,
                isSupplier = isYear,
                supplierListener = this,
                isOrigin = isOrgin,
                isDepartment = isDepartment
            )

        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


    private fun enableDisableItem(flag: Boolean) {
        enableDisableHeaderItem(flag)
        binding.tvTruckNoValue.isEnabled = flag
        binding.tvDriverNameValue.isEnabled = flag
        binding.tvDriverNoValue.isEnabled = flag
    }

    private fun enableDisableHeaderItem(flag: Boolean) {
        binding.tvWhValue.isEnabled = flag
        binding.tvstoValue.isEnabled = flag
        binding.tvReceivingValue.isEnabled = flag
        binding.tvWhValue.isClickable = flag
        binding.tvstoValue.isClickable = flag
        binding.tvReceivingValue.isClickable = flag
    }

    private fun setValueEmpty(fragment: Fragment?) {
        binding.tvReceivingValue.text = ""
        binding.tvTruckNoValue.setText("")
        binding.tvDriverNameValue.setText("")
        binding.tvDriverNoValue.setText("")
        when (fragment) {
            is VegaIvcCocoSupplierConsignmentFragment -> binding.tvstoValue.text = ""
        }
    }

    private fun validateProceed() {
        vm.vegaCoCoaReceivingData.wsGate = if (offloadData.imageString.equals(WEIGHSCALE)) WS01 else WB01
        vm.vegaCoCoaReceivingData.batchNumber = offloadData.batchNumber
        /*batchInfo.batch = vm.vegaCoCoaReceivingData.materialCode.plus(vm.vegaCoCoaReceivingData.supplierCode)
            .plus(vm.vegaCoCoaReceivingData.weighBridgeType)*/
        batchInfo.materialNumber = vm.vegaCoCoaReceivingData.materialCode ?: ""
        batchInfo.materialName = vm.vegaCoCoaReceivingData.materialName
        //batchInfo.mtnNumber = batchInfo.batch
        /* vm.vegaCoCoaReceivingData.delivery =
                 vm.vegaCoCoaReceivingData.materialCode.plus(vm.vegaCoCoaReceivingData.supplierCode)
                         .plus(vm.vegaCoCoaReceivingData.weighBridgeType)
         vm.vegaCoCoaReceivingData.commonPrimaryId =
                 vm.vegaCoCoaReceivingData.materialCode.plus(vm.vegaCoCoaReceivingData.supplierCode)
                         .plus(vm.vegaCoCoaReceivingData.weighBridgeType)
         batchInfo.delivery = vm.vegaCoCoaReceivingData.materialCode.plus(vm.vegaCoCoaReceivingData.supplierCode)
                 .plus(vm.vegaCoCoaReceivingData.weighBridgeType)*/
        /* if (vm.vegaCoCoaReceivingData.weighBridgeId.isNullOrEmpty()) {
             vm.vegaCoCoaReceivingData.weighBridgeId = getTmpId()
             vm.vegaCoCoaReceivingData.tempWBId = getTmpId()
         }*/
        //vm.saveMtnrReceivingLots(vm.vegaCoCoaReceivingData, VegaCoCoaReceiveLots())
//        callBack?.replaceFragment(ADD_WEIGHT, vm.vegaCoCoaReceivingData)

        /*Track & Trace*/
        if(ttProcurementType.equals(Constants.DIRECT)){
            vm.vegaCoCoaReceivingData.ttFarmerList = getTTFarmerDataList()
            vm.vegaCoCoaReceivingData.eudrStatus = isTTComplaint

        } else if(ttProcurementType.equals(Constants.IN_DIRECT)) {
            vm.vegaCoCoaReceivingData.sourceLotId = ttIndirectSourceLotDetails?.sourceLotId.toString()
            vm.vegaCoCoaReceivingData.eudrStatus = ttIndirectSourceLotDetails?.isEudrComplaintFlag ?: false
        } else if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)){
            vm.vegaCoCoaReceivingData.farmerLessTransactionId = ttFarmerlessTransactionDetails.dwTransactionId.toString()
            vm.vegaCoCoaReceivingData.eudrStatus = ttFarmerlessTransactionDetails?.compliantFlag ?: false
        }

        if(ttMaterialCode.isNotEmpty() && !ttMaterialCode.equals(binding.tvWhValue.text.toString().split("-")[0].takeLast(12))){
            showMaterialMismatchDialog()
        } else {
            callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoCoaReceivingData)
        }
    }

    private fun getTTFarmerDataList(): java.util.ArrayList<TrackTraceFarmerModel> {
        if(ttDirectFarmerDataList.isNotEmpty() && ttDirectFarmerDataList.size > 0) {
            if (AppUtils.isOnline()) {
                if(ttDirectFarmerDataList.size==1){
                    if(ttDirectFarmerDataList.get(0).farmerWeight.isNullOrEmpty() || ttDirectFarmerDataList.get(0).farmerWeight=="0"){
                        ttDirectFarmerDataList.get(0).farmerWeight = vm.vegaCoCoaReceivingData.netWeight
                    }
                }
                 ttDirectFarmerDataList.forEach {
                     it.uom = vm.vegaCoCoaReceivingData.unitsOfMeasure
                 }
                return ttDirectFarmerDataList
            } else{
                ttDirectFarmerDataList.forEach {
//                    it.tmpWbId = vm.vegaCoCoaReceivingData.w
                    if(it.farmerWeight.isNullOrEmpty())it.farmerWeight = vm.vegaCoCoaReceivingData.netWeight
                    it.uom = vm.vegaCoCoaReceivingData.unitsOfMeasure
                }
                return ttDirectFarmerDataList
            }
        }
        return ttDirectFarmerDataList
    }

    private fun showRemarkDialog() {

        showDialog(getString(R.string.end_load_msg), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else {
                    val endTime = System.currentTimeMillis()
                    vm.vegaCoCoaReceivingData.endTime = endTime.toString()
                    val duration = endTime.minus(vm.vegaCoCoaReceivingData.startTime?.toLong() ?: 0)
                    vm.vegaCoCoaReceivingData.remarks = remark
                    vm.vegaCoCoaReceivingData.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration).toString()
                    batchList.forEach {
                        it.delivery = vm.vegaCoCoaReceivingData.delivery
                        vm.saveMtnrReceivingLots(vm.vegaCoCoaReceivingData, it)
                    }
                    //callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoCoaReceivingData)
                }
            }

        }, true, vm.vegaCoCoaReceivingData.remarks.toString())
    }


    fun updateAddWeight(weight: String) {
        val split = weight.split(" ")
        val come: Int? = split[0].toDouble().compareTo(batchList[addWeightPosition].weight?.toDouble() ?: 0.0)
        batchList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
        batchList[addWeightPosition].editedWeight = split[0]
        batchList[addWeightPosition].editedUOM = split[1]
    }

    private fun validateInputs() {
        when {
            vm.vegaCoCoaReceivingData.vehicleNumber.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            vm.vegaCoCoaReceivingData.driverName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver))
            vm.vegaCoCoaReceivingData.supplierCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_trans_vendor))
            vm.vegaCoCoaReceivingData.storageLocationCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_loc))
            vm.vegaCoCoaReceivingData.operatorName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_operator))
            vm.vegaCoCoaReceivingData.bagList.isEmpty() -> showSnack(getString(R.string.error_valid_bags))

                /*vm.vegaCoCoaReceivingData.purchaseDocNum.isNullOrEmpty() -> {
                    if (offloadData.imageString.equals(WEIGHBRIDGE_WEIHSCALE))
                        validateProceed()
                    else
                        showSnack(getString(R.string.select_po_error))
                }
                vm.vegaCoCoaReceivingData.declaredBagCount.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_bagcount))
                vm.vegaCoCoaReceivingData.declaredWeight.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_declared))
                binding.etTruckBagcount.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_bagcount))
                binding.etTruckDeclaredweight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_declared))*/
            else -> validateProceed()
        }
    }

    private fun showMaterialMismatchDialog(){
        MaterialDialog(requireContext()).show {
            message(text = getString(R.string.change_from_material))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.yes),
                getString(com.olam.warehouse.presentation.R.string.no),
                {
                    dismiss()
                    offloadData.batchNumber=""
                    offloadData.isDelete = true
                    callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoCoaReceivingData)
                },
                { dismiss()
                })

        }
    }

    private fun enableProceed() {
        var enable = false
        enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty()
                    && binding.tvReceivingValue.text.isNotEmpty() /*&& binding.etTruckBagcount.text.isNotEmpty() && binding.etTruckDeclaredweight.text.isNotEmpty() && !binding.tvOrigin.text.equals(
                    " "
                ) && !binding.tvDestinationLabel.text.equals(" ")*/)
        /* if (offloadData.imageString.equals(WEIGHSCALE)) {
             enable =
                 (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                         && binding.tvTruckNoValue.text.toString().isNotEmpty()
                         && binding.tvReceivingValue.text.isNotEmpty() *//*&& binding.etTruckBagcount.text.isNotEmpty() && binding.etTruckDeclaredweight.text.isNotEmpty() && !binding.tvOrigin.text.equals(
                    " "
                ) && !binding.tvDestinationLabel.text.equals(" ")*//*)

        } else if (offloadData.imageString.equals(WEIGHBRIDGE_WEIHSCALE)) {
            enable =
                (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                        && binding.tvTruckNoValue.text.toString().isNotEmpty()
                        && binding.tvReceivingValue.text.isNotEmpty() && binding.etTruckBagcount.text.isNotEmpty() && binding.etTruckDeclaredweight.text.isNotEmpty())
        }*/

        if (enable) {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btProceed.isEnabled = enable
        binding.btSave.isEnabled = enable
    }

    private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
//                binding.tvWhValue.text =
//                    response.data?.data?.materialCode.plus("-").plus(response.data?.data?.materialName)
                /*binding.tvstoValue.text =
                    response.data?.data?.supplierCode.plus("-").plus(response.data?.data?.supplierName)*/
                /*binding.tvReceivingValue.text =
                    response.data?.data?.storageLocationCode.plus("-")
                        .plus(getLocationName(response.data?.data?.storageLocationCode))*/
                val driverName =
                    if (response.data?.data?.truckDriverName.isNullOrBlank()) response.data?.data?.driverName else response.data?.data?.truckDriverName
                binding.tvDriverNameValue.setText(driverName, TextView.BufferType.EDITABLE)
                binding.tvDriverNoValue.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                binding.tvTruckNoValue.setText(response.data?.data?.vehicleNumber)
                offloadData.vehicleNumber = response.data?.data?.vehicleNumber
                offloadData.driverName = response.data?.data?.truckDriverName
                offloadData.truckDriverName = response.data?.data?.truckDriverName
                offloadData.contactNumber = response.data?.data?.contactNumber
                offloadData.storageLocationCode = response.data?.data?.storageLocationCode
                offloadData.storageLocationName = getLocationName(response.data?.data?.storageLocationCode)
                offloadData.materialCode = response.data?.data?.materialCode
                offloadData.materialName = response.data?.data?.materialName
                offloadData.purchaseDocNum = response.data?.data?.purchaseDocNum
                offloadData.purchaseDocDesc = response.data?.data?.purchaseDocDesc
               // offloadData.purchaseDocQty = response.data?.data?.purchaseDocQty
                offloadData.unitsOfMeasure = response.data?.data?.unitsOfMeasure ?: ""
                binding.llBagDetails.tvBagType.text = response.data?.data?.bagType
                binding.llBagDetails.etBagCount.setText(response.data?.data?.bagCount?.trim())
                if(binding.llBagDetails.tvBagType.text.isNotEmpty())binding.llBagDetails.tvAdd.performClick()
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
    override fun clickOnItem(
        data: String,
        isWh: Boolean,
        isVendor: Boolean,
        isGrade: Boolean,
        isSupplier: Boolean,
        isOrigin: Boolean,
        isDepartment: Boolean
    ) {
        customDialog?.dismiss()
        enableProceed()
        if (isWh) {
            binding.tvWhValue.text = data
            val split = data.split("-")
            val material = materialProduct.single { it.materialCode == split[0] }
            offloadData.materialCode = material.materialCode
            offloadData.materialName = material.materialName.toString()
            //binding.tvUom.text = material.unitsOfMeasure.toString()
            offloadData.unitsOfMeasure = material.unitsOfMeasure.toString()
            vm.vegaCoCoaReceivingData.materialCode = offloadData.materialCode
            vm.vegaCoCoaReceivingData.materialName = offloadData.materialName
            if (isNewTruckCall) {
                if (offloadData.materialCode?.length != 18)
                    materialCode = "000000".plus(offloadData.materialCode)
                else
                    materialCode = offloadData.materialCode!!
                /*vm.getQualityParams(materialCode, false, offloadData.weighBridgeId)
                vm.qualitylist.observe(viewLifecycleOwner, Observer {
                    updateoriginlist(it)
                })*/
            }
        } else if (isVendor) {
            val split = data.split("-")
            binding.tvstoValue.text = data
            offloadData.supplierCode = SUPPLIER_APPEND.plus(split[0]).trim()
            offloadData.supplierName = split[1]
            vm.vegaCoCoaReceivingData.supplierCode = offloadData.supplierCode
            vm.vegaCoCoaReceivingData.supplierName = offloadData.supplierName
            vm.vegaCoCoaReceivingData.weighBridgeType = offloadData.weighBridgeType
            /* if (!isNewTruckCall) {
                 vm.getOBDDetails(
                     vm.vegaCoCoaReceivingData.materialCode.plus(vm.vegaCoCoaReceivingData.supplierCode)
                         .plus(vm.vegaCoCoaReceivingData.weighBridgeType)
                 )
             }*/
        }  else if (isSupplier) {
            year = data
            binding.tvYearValue.text = data
        }
        else if(isGrade){
            binding.llBagDetails.tvBagType.text = data
        }
        else {
            val split = data.split("-")
            binding.tvReceivingValue.text = data
            vm.vegaCoCoaReceivingData.storageLocationCode = split[0].trim()
            vm.vegaCoCoaReceivingData.storageLocationName = split[1].trim()
            offloadData.storageLocationCode = split[0].trim()
            offloadData.storageLocationName = split[1].trim()
            selectedReceivingLocation = customLocationList.single { it.procureLocationCode == split[0].trim() }
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {

    }

    private fun resetMaterial(){
        binding.tvstoValue.text = ""
        binding.tvWhValue.text = ""
        offloadData.materialCode = ""
        offloadData.materialName = ""
    }

    private fun clearAllValues() {

        binding.tvWhValue.text = ""
        binding.tvstoValue.text = ""
        binding.tvYearValue.text = ""
        binding.tvReceivingValue.text = ""
//        binding.tvPOValue.text = ""
        binding.tvTruckNoValue.setText(" ")
        binding.tvDriverNameValue.setText(" ")
        binding.tvDriverNoValue.setText(" ")
//        binding.etTruckBagcount.setText(" ")
//        binding.etTruckDeclaredweight.setText(" ")
//        binding.tvOrigin.text = " "
//        binding.tvDestinationLocation.text = " "
    }

    /*Track & Trace*/
    fun updateFarmerListDetails(farmerList: java.util.ArrayList<TrackTraceFarmerModel>){
        resetMaterial()
        binding.tvWhValue.text=""
        ttDirectFarmerDataList.clear()
        ttDirectFarmerDataList.addAll(farmerList)
        if(ttDirectFarmerDataList.isNotEmpty()) {
            offloadData.supplierCode = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(0)
            offloadData.supplierName = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(1)
            var result = ttDirectFarmerDataList.any { it.isComplaint == 0 }
            if(result){
                isTTComplaint = false
            }else{
                isTTComplaint = true
            }
        } else {
            isTTComplaint = false
        }
    }

    /*Track & Trace*/
    fun isComplaint(status:Int){
        if(status == 0){
            isTTComplaint = false
        } else {
            isTTComplaint = true
        }
    }

    /*Track & Trace*/
    fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails){
        resetMaterial()
        isTTComplaint = sourceLotDetails?.isEudrComplaintFlag?:false
        this.ttIndirectSourceLotDetails = sourceLotDetails
        if(sourceLotDetails.vendorCode?.isNotEmpty() == true){
            var checkSupplier = supplierList.filter { it.vendorCode.equals(sourceLotDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
            } else {
                var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                val split = data.split("-")
                binding.tvstoValue.text = data
                offloadData.supplierCode = SUPPLIER_APPEND.plus(split[0]).trim()
                offloadData.supplierName = split[1]
            }
        } else {
            showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
        }
    }

    fun updateFarmerlessTransactionDetails(transactionIdDetails: TrackTraceTransactionIdDetails){
        resetMaterial()
        isTTComplaint = transactionIdDetails?.compliantFlag?:false
        this.ttFarmerlessTransactionDetails = transactionIdDetails
        binding.tvstoValue.text = ""
        if(transactionIdDetails.vendorCode?.isNotEmpty() == true){
            var checkSupplier = supplierList.filter { it.vendorCode.equals(transactionIdDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the transaction id is not available in SAP, do you want to continue?")
            } else {
                var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                binding.tvstoValue.text = data
                offloadData.supplierCode = data.split("-")[0].trim()
                offloadData.supplierName = data.split("-")[1].trim()
            }
        } else {
            showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
        }
    }

}

