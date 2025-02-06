package com.olam.warehouse.vegax.dispatchnigeria.ui.weighscale

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentWorkflowDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.WorkflowFields
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.presentation.BuildConfig
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.dispatchnigeria.R
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.*
import com.olam.warehouse.vegax.dispatchnigeria.databinding.FragmentNigeriaCocoaMtntWsSummaryBinding
import com.olam.warehouse.vegax.dispatchnigeria.databinding.ItemNigeriaCocoaMaterialLayoutBinding
import com.olam.warehouse.vegax.dispatchnigeria.databinding.ItemNigeriaCocoaMtntLotSummaryBinding
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntLotRemoveListener
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntViewModel
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaReplaceFragmentCallback
import com.olam.warehouse.vegax.dispatchnigeria.utils.JSON_PROCESS_TYPE_LIST
import com.olam.warehouse.vegax.dispatchnigeria.utils.UNIT_KG
import com.olam.warehouse.vegax.dispatchnigeria.utils.UNIT_MT
import com.olam.warehouse.vegax.dispatchnigeria.utils.convertKgToMT
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaNigeriaCocoaMtntWeighScaleSummaryFragment : BaseFragment(),
    VegaNigeriaCocoaMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_nigeria_cocoa_mtnt_ws_summary
    private lateinit var binding: FragmentNigeriaCocoaMtntWsSummaryBinding
    private var callBack: VegaNigeriaCocoaReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaNigeriaCocoaMtntViewModel by viewModel()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private lateinit var vegaCoffeeDeliveryPost: VegaNigeriaCocoaMtntDeliveryPost
    private lateinit var vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost
    private var vegaCocoaMtntWithLots: VegaCocoaMtntWithLots? = null
    var isPostCreated: Boolean = false
    private var thirdPartyMaterials: List<VegaCoffeeThirdPartyMaterialDetail>? = null
    private var grnProcessType :String? =""
    private var jsonData = mutableListOf<String>()
    private var deliveryDetails = ArrayList<VegaNigeriaCocoaMtntDispatchLotsMerge>()
    private var model: VegaEcuadorDispatch = VegaEcuadorDispatch()
    private var workFlowData: WorkflowFields? = null

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB?) =
            VegaNigeriaCocoaMtntWeighScaleSummaryFragment().putArgs {
                putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaCocoaReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaCocoaMtntWsSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        vm.wsdeliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {
        workFlowData = getCurrentWorkflowDetails(getPlantDetails().plantId, "27")
        vm.getWeighBridgeWithLotAndMaterial(summaryObj?.weighBridgeId ?: "")
        vm.weighBridgeWithLotsSource.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.dispatchWh = it.dispatch
                vm.lotList.clear()
                vm.lotList.addAll(it.lineItems)
                vm.materialModelList.clear()
                vm.materialModelList.addAll(it.materialList)
                vegaCocoaMtntWithLots = it
                updateLocalDbData(it)
            }
        })

        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer  {
            thirdPartyMaterials = it
        })
        vm.getThirdPartyMaterials()

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        binding.header.tvTruckNoValue.text = summaryObj?.vehicleNumber
        binding.tvRemarkValue.text = summaryObj?.remarks
        binding.header.tvDestValue.text =
            summaryObj?.recPlantId.plus("-").plus(summaryObj?.plantName)
        binding.header.tvDateValue.text = summaryObj?.erdat
        binding.header.tvStoNumberValue.text =
            summaryObj?.purchaseDocNum.plus("-").plus(summaryObj?.purchaseDocDesc)
        binding.header.tvContactValue.text = summaryObj?.driverPhoneNumber
        binding.header.tvDriverNameValue.text = summaryObj?.driverName

        binding.btProceed.setOnClickListener { showConformationDialog() }
        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        binding.tvRemarkValue.text = summaryObj?.remarks

        if (summaryObj?.imagePath?.isNotEmpty()!! && summaryObj?.imagesList?.size!! > 0) {
            binding.photoView.visible()
            binding.photoView.setImageBitmap(summaryObj?.imagesList?.get(0)?.let { setScaledBitmap(it) })

            if (summaryObj?.imagesList?.size!! > 1) {
                binding.photoView1.visible()
                binding.photoView1.setImageBitmap(setScaledBitmap(summaryObj?.imagesList!!.get(1)))
            }
            if (summaryObj?.imagesList?.size!! > 2) {
                binding.photoView2.visible()
                binding.photoView2.setImageBitmap(setScaledBitmap(summaryObj?.imagesList!!.get(2)))
            }

            if (summaryObj?.imagesList?.size!!> 3) {
                binding.photoView3.visible()
                binding.photoView3.setImageBitmap(setScaledBitmap(summaryObj?.imagesList!!.get(3)))
            }

            if (summaryObj?.imagesList?.size!! > 4) {
                binding.photoView4.visible()
                binding.photoView4.setImageBitmap(setScaledBitmap(summaryObj?.imagesList!!.get(4)))
            }

            if (summaryObj?.imagesList?.size!! > 5) {
                binding.photoView5.visible()
                binding.photoView5.setImageBitmap(setScaledBitmap(summaryObj?.imagesList!!.get(5)))
            }

         /*   if (summaryObj?.imagesList?.size!! > 1 && summaryObj?.imagesList?.size!! >= 2) {
                binding.photoView1.visible()
                binding.photoView1.setImageBitmap(setScaledBitmap(summaryObj?.imagesList!!.get(1)))
            }
            if (summaryObj?.imagesList?.size!! > 2 && summaryObj?.imagesList?.size == 3) {
                binding.photoView2.visible()
                binding.photoView2.setImageBitmap(
                    summaryObj?.imagesList?.get(2)?.let { setScaledBitmap(it) })
            }
            */
        }
    }

    private fun updateLocalDbData(weighBridge: VegaCocoaMtntWithLots) {
        dispatchLotsList.clear()
        if (weighBridge.lineItems.size == 1) {
            binding.tvText.visibility = View.GONE
        } else
            binding.tvText.visibility = View.VISIBLE
        setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
        calculateAndUpdateWeightToDispatch()
        vm.bagItems.observe(viewLifecycleOwner, Observer { getBagList(it) })
        vm.getBagItems()
    }

    private fun setScaledBitmap(imagePath: String): Bitmap? {
        try {
            val imageViewWidth = 100
            val imageViewHeight = 100

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor =calculateInSampleSize(bmOptions,imageViewWidth,imageViewHeight)
                //Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inSampleSize = scaleFactor
            bmOptions.inJustDecodeBounds = false

            return BitmapFactory.decodeFile(imagePath, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }



    private fun setUpMaterialAdapter() {
        binding.header.rvMaterialList.setUpAdapter(
            vm.materialModelList,
            R.layout.item_nigeria_cocoa_material_layout,
            ItemNigeriaCocoaMaterialLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvStoWeightValue.text =
                    it.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus(it.uom)
                bindItem.tvDispatchWeightValue.text = it.dispatchWeight.plus(it.uom)
            })
    }

    private fun setUpAdapter(list: List<VegaCocoaDispatchLots>) {
        val lots = list as MutableList
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_nigeria_cocoa_mtnt_lot_summary,
            ItemNigeriaCocoaMtntLotSummaryBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocation.text = item.storageLocationCode
                val weight = item.weight?.toDouble()
                val enteredWeight = item.editedWeight?.toDouble()
                val totalLoss = weight?.minus(enteredWeight!!)
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.unitOfMeasure)
                bindItem.tvGradeValue.text = item.materialName
                bindItem.tvEditedWeight.text =
                    item.editedWeight?.toDouble()?.formatThreeDigits().toString().plus(" ")
                        .plus(item.unitOfMeasure)

                if (item.isEndLot == true) {
                    bindItem.linearWeightLoss.visibility = View.VISIBLE
                    bindItem.tvTotalWeightLoss.text =
                        totalLoss?.formatThreeDigits().plus(" ").plus(item.unitOfMeasure)
                } else {
                    bindItem.linearWeightLoss.visibility = View.GONE
                    bindItem.tvTotalWeightLoss.text = "NA"
                }


                bindItem.tvUnit.visibility = View.GONE
                bindItem.tvEditedWeight.visibility = View.VISIBLE
                bindItem.ivClose.setImageDrawable(bindItem.ivClose.context.getDrawable(R.drawable.ic_nigeriacoco_edit_gray))
                bindItem.cbSelectAll.visibility = View.GONE
                bindItem.tvSelectAll.visibility = View.GONE
                bindItem.cbEndLot.visibility = View.GONE
                bindItem.tvEndLot.visibility = View.GONE
                bindItem.etWeight.visibility = View.GONE
                bindItem.ivClose.setOnClickListener { itemRemoved(item) }
            })
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        var processTypeList = mutableListOf<VegaNigeriaCocoaMtntProcessType>()
        jsonData.forEach {
            if (it.contains(JSON_PROCESS_TYPE_LIST)) {
                val processType = gson.fromJson(it, VegaNigeriaCocoaMtntProcessTypeModel::class.java)
                grnProcessType = processType.PROCESS_TYPE_LIST[0].MTNT
            }
        }

    }

    private fun generateBatchNumber():String {
        var productTypeCode = ""

        thirdPartyMaterials?.forEach { item ->
            if (item.materialName.toString().contains(dispatchLotsList[0].materialName.toString(), true)) {
                productTypeCode = item.typeCode.toString()
            }
        }
        var year = Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(1)
        return dispatchLotsList[0].plantId?.takeLast(2).plus(dispatchLotsList[0].storageLocationCode?.takeLast(1))
            .plus(year).plus(grnProcessType).plus(productTypeCode.takeLast(1))
    }

    private fun postDelivery() {
        var count = 0
        if (PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").isNullOrEmpty()) {
            count = 0
        } else {
            count = PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").toInt()
        }
        if (count != 0 && DateUtils.isFirstDaysOfMonth(requireContext(), count)) {
            UIUtils.showPostingDateDialog(requireContext(), count, object : UIUtils.DialogClick {
                override fun onPositive(date: String) {
                    var postingDate =
                        DateUtils.oneFormatToOtherFormat(date, "dd-MMM-yyyy", "yyyy-MM-dd'T'HH:mm")
                    var batchId = generateBatchNumber()
                    vegaCocoaDeliveryPost = VegaNigeriaCocoaMtntMergedDeliveryPost(
                        getCurrentKey(),
                        getPlantDetails(),
                        false,
                        false,
                        false,
                        false,
                        summaryObj?.vehicleNumber,
                        summaryObj?.driverPhoneNumber,
                        summaryObj?.driverName,
                        prepareMergedDeliveryList(postingDate),
                        notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                        nextWorkFlowRole = workFlowData?.workflowRole,
                        navId = workFlowData?.workflowId,
                        currentWorkFlowRole = workFlowData?.module,
                        environment = BuildConfig.BUILD_TYPE
                    )
                    vm.postWeighScaleDeliveryDetails123(vegaCocoaDeliveryPost)
                }
            })
        }else {
            var batchId = generateBatchNumber()
            vegaCocoaDeliveryPost = VegaNigeriaCocoaMtntMergedDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                false,
                false,
                false,
                false,
                summaryObj?.vehicleNumber,
                summaryObj?.driverPhoneNumber,
                summaryObj?.driverName,
                prepareMergedDeliveryList(""),
                notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                nextWorkFlowRole = workFlowData?.workflowRole,
                navId = workFlowData?.workflowId,
                currentWorkFlowRole = workFlowData?.module,
                environment = BuildConfig.BUILD_TYPE
            )
            vm.postWeighScaleDeliveryDetails123(vegaCocoaDeliveryPost)
        }

    }
    private fun prepareMergedDeliveryList(postingDate:String) : List<VegaNigeriaCocoaMtntDispatchLotsMerge> {
        var list = ArrayList<VegaNigeriaCocoaMtntDispatchLotsMerge>()
        var lotList = ArrayList<VegaEcuadorDispatchLots>()

        val deliveryDetail = VegaNigeriaCocoaMtntDispatchLotsMerge()
       // deliveryDetail.batchNumber = generateBatchNumber()
        deliveryDetail.delivery = false
        deliveryDetail.deliveryId = ""
        deliveryDetail.mergeStatus = ""
        deliveryDetail.netWeight= "0"
        deliveryDetail.message = ""
        deliveryDetail.pgi = false
        deliveryDetail.picking = false
        var isbagMateriaExists = false
        if (dispatchLotsList.size > 0) {
            var vegaCocoaDispatchLots = dispatchLotsList.get(0)
            deliveryDetail.batchNumber = summaryObj?.batchNumber.toString()
            var bagItems =
                bagList.filter { it.batchNumber == vegaCocoaDispatchLots.batchNumber && it.baseMaterial == vegaCocoaDispatchLots.materialCode }
            val materialItem= vm.materialModelList.map { it.materialCode }

            bagItems.forEach { it1 ->
               /* it1.unitsOfMeasure = UNIT_KG
                it1.batchNumber = summaryObj?.batchNumber.toString()
                it1.grossWeight = convertMtToKg(summaryObj?.grossWeight, UNIT_MT).toString()
                it1.netWeight = convertMtToKg(summaryObj?.netWeight.toString(), UNIT_MT).toString()
                it1.tareWeight = convertMtToKg(it1?.tareWeight.toString(), UNIT_MT).toString()*/
                if(materialItem.contains(it1.bagMaterialCode)||materialItem.contains(it1.bagMaterialCode1))isbagMateriaExists= true
                it1.unitsOfMeasure = UNIT_MT
                it1.batchNumber = summaryObj?.batchNumber.toString()
                it1.grossWeight = summaryObj?.grossWeight.toString()
                it1.netWeight = summaryObj?.netWeight.toString()
            }
            var lot = VegaEcuadorDispatchLots()
            lot.batchNumber = summaryObj?.batchNumber.toString()
            lot.materialCode = vegaCocoaDispatchLots.materialCode
            lot.materialName = vegaCocoaDispatchLots.materialName
            lot.noOfBags = vegaCocoaDispatchLots.noOfBags
            lot.encodedImageContent = summaryObj?.encodedImageContent.toString()
            lot.storageLocationCode = vegaCocoaDispatchLots.storageLocationCode
            lot.plantId = vegaCocoaDispatchLots.plantId
            lot.plantName = vegaCocoaDispatchLots.plantName
            if ((vegaCocoaDispatchLots.unitOfMeasure.toString()).equals(UNIT_MT)) {
                lot.unitsOfMeasure = vegaCocoaDispatchLots.unitOfMeasure
                lot.editedWeight = summaryObj?.netWeight.toString()
                lot.netWeight= summaryObj?.netWeight.toString()
               /* lot.editedWeight = convertKgToMT(
                    summaryObj?.netWeight.toString(),
                    vegaCocoaDispatchLots.unitOfMeasure.toString())
                lot.netWeight = convertKgToMT(
                    summaryObj?.netWeight.toString(),
                    vegaCocoaDispatchLots.unitOfMeasure.toString())
                lot.grossWeight = convertKgToMT(
                    summaryObj?.grossWeight,
                    vegaCocoaDispatchLots.unitOfMeasure.toString())*/
                lot.grossWeight = summaryObj?.grossWeight
            } else if ((vegaCocoaDispatchLots.unitOfMeasure.toString()).equals(UNIT_KG)) {
                lot.unitsOfMeasure = UNIT_MT
                lot.editedWeight = convertKgToMT(
                    summaryObj?.netWeight.toString(),
                    vegaCocoaDispatchLots.unitOfMeasure.toString()
                )
                lot.netWeight = convertKgToMT(
                    summaryObj?.netWeight.toString(),
                    vegaCocoaDispatchLots.unitOfMeasure.toString()
                )
                lot.grossWeight = convertKgToMT(
                    summaryObj?.grossWeight,
                    vegaCocoaDispatchLots.unitOfMeasure.toString()
                )

            }
            lot.recPlantId = summaryObj?.recPlantId
            lot.recStorageLocationCode = summaryObj?.recStorageLocationCode
            lot.startTime = summaryObj?.startTime.toString()
            lot.endTime = summaryObj?.endTime.toString()
            lot.turnAroundTime = summaryObj?.turnAroundTime.toString()
            lot.remarks = summaryObj?.remarks.toString()
            lot.wbTempId = vegaCocoaDispatchLots.weighBridgeId
            lot.endLotFlag = vegaCocoaDispatchLots.isEndLot!!
            lot.postingDate = postingDate
            var bagTareWeight = 0.0
            bagItems.forEach { item1 ->
                bagTareWeight = bagTareWeight.plus(
                    item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!
                )
            }

            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCocoaSweepingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                lot.bagList = bagItems
            } else {
                lot.grossWeight = summaryObj?.grossWeight
            }

            vm.materialModelList.forEach {
                if (vegaCocoaDispatchLots.materialCode == it.materialCode) {
                    lot.purchaseDocNum = it.purchaseOrderNum
                    lot.purchaseDocDesc = it.purchaseOrderDesc
                }
            }
            lotList.add(lot)
//            val bagMaterialItem =  vm.materialModelList.filter { it.uom.equals("EA")&& it.materialCode.startsWith("0000002") }
//            val bagMaterial = bagMaterialItem.map { it.materialCode }
//            var bagLotItem = VegaCocoaDispatchLots()
//            vm.lotList.forEach {
//                if(bagMaterial.contains(it.materialCode)) {
//                    isbagMateriaExists = true
//                    bagLotItem = it
//                }
//            }
//            if(isbagMateriaExists){
//                val lot1= lot.copy()
//                //val filterBagList =lot1.bagList.filter { it.bagMaterialCode.isNotEmpty()|| it.bagMaterialCode1?.isNotEmpty() == true }
//                var filterBagList =
//                    bagList.filter { it.batchNumber == bagLotItem.batchNumber && it.baseMaterial == bagLotItem.materialCode }
//                if(filterBagList.isNotEmpty()) {
//                    lot1.bagList = filterBagList
//                    lot1.netWeight =
//                        filterBagList.sumOf { it.bagCount.toInt().plus(it.bagCount1?.toInt() ?: 0) }
//                            .toString()
//                    if ( bagMaterialItem.isNotEmpty()) {
//                        lot1.unitsOfMeasure =  bagMaterialItem.get(0).uom
//                        lot1.materialCode = bagMaterialItem.get(0).materialCode
//                    }
//                }
//                lot1.purchaseDocDesc = "00002"
//                lotList.add(lot1)
//            }
            // }


            if(isbagMateriaExists){
                val lot1= lot.copy()
                val bagMaterialItem =  vm.materialModelList.filter { it.uom.equals("EA")&& it.materialCode.startsWith("0000002") }
                val filterBagList = bagList.filter { it.bagMaterialCode.isNotEmpty()|| it.bagMaterialCode1?.isNotEmpty() == true }
                lot1.bagList= filterBagList
                lot1.materialCode= if (filterBagList.get(0).bagMaterialCode.isNotEmpty()) filterBagList.get(0).bagMaterialCode else filterBagList.get(0).bagMaterialCode1
                lot1.batchNumber= vegaCocoaDispatchLots.oldBatchNumber?:""
                lot1.mergedBatchNumber= vegaCocoaDispatchLots.binMergedBatchNumber
                lot1.materialName= if (filterBagList.get(0).bagMaterialCode.isNotEmpty()) filterBagList.get(0).bagType else filterBagList.get(0).bagType1
                lot1.unitsOfMeasure= if(bagMaterialItem.isNotEmpty()) bagMaterialItem.get(0).uom else  "EA"
                lot1.netWeight= filterBagList.sumOf {
                    it.bagCount.toInt().plus(it.bagCount1?.toInt() ?: 0)
                }.toString()
                lot1.grossWeight= filterBagList.sumOf {
                    it.bagCount.toInt().plus(it.bagCount1?.toInt() ?: 0)
                }.toString()
                lot1.purchaseDocDesc ="00002"
                lotList.add(lot1)
                }
        }
        deliveryDetail.lots = lotList
        list.add(deliveryDetail)
        return list
    }

    private fun calculateAndUpdateWeightToDispatch() {
        val materialWeightMap = HashMap<String, String>()
        dispatchLotsList.forEach {
            val data = materialWeightMap[it.materialCode]
            val editWeight =
                if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble()
            if (data != null) {
                val sum = data.toDouble().plus(editWeight!!)
                materialWeightMap[it.materialCode] = sum.toString()
            } else materialWeightMap[it.materialCode] = editWeight.toString()
        }
        vm.materialModelList.forEach { it.dispatchWeight = materialWeightMap[it.materialCode] }
        setUpMaterialAdapter()
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    private fun showConformationBackNav() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_back)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.updateSyncStatus(vegaCocoaMtntWithLots!!)
                    activity?.finish()
                },
                { dismiss() })
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>) {

        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let {
                    if (true) {
                        prepareSuccessData(
                            response.data?.data?.message ?: "",
                            true,
                            response.data?.message.toString()
                        )
                    } else {
                        model.binFormation = response.data?.data?.binFormation!!
                        model.delivery = response.data?.data?.delivery!!
                        model.pgi = response.data?.data?.pgi!!
                        model.picking = response.data?.data?.picking!!
                    }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun prepareSuccessData(deliveryId: String, syncStatus: Boolean, msg: String) {
        model.status = if (syncStatus) Status.MTNT_COMPLETED else Status.SYNC_PENDING
        model.wbTempId = if (!syncStatus) deliveryId else ""
        model.isSynced = syncStatus
        model.syncStatusMsg = msg
        model.deliveryId = deliveryId
        moveToSuccessPage(deliveryId)
    }

    private fun moveToSuccessPage(deliveryId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        vm.deleteBagDetails()
        if (AppUtils.isOnline()) {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch)
            )
            intent.putExtra(AppUtils.SUB_TITLE, deliveryId)
        } else {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch_offline)
            )
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun prepareSuccessMessage(data: List<VegaNigeriaCocoaMergedDeliveryPostResponse>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                message = ""
            }
            return message
        }
        return ""
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }

    private fun showUpdateRemarkDialog() {
        showDialog(
            getString(R.string.update_remark_title),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    summaryObj?.remarks = remark
                    binding.tvRemarkValue.text = remark
                    vm.updateRemarks(remark, true, summaryObj?.weighBridgeId ?: "")
                }
            },
            true, summaryObj?.remarks ?: ""
        )
    }

    override fun itemRemoved(item: VegaCocoaDispatchLots) {
        activity?.onBackPressed()
    }

    fun backNav() {
        showConformationBackNav()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}
