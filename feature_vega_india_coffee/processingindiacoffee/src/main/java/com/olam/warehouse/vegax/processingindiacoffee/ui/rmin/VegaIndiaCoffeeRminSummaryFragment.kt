package com.olam.warehouse.vegax.processingindiacoffee.ui.rmin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminData
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminItems
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminProcessLotDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeProcessingCreatePoReq
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeProcessingRMINLotDetails
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeProcessingRminResponse
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeRminProcessingPost
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentRminSummaryBinding
import com.olam.warehouse.vegax.processingindiacoffee.databinding.ItemLotRminSummaryBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.ADDLOT
import com.olam.warehouse.vegax.processingindiacoffee.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingindiacoffee.utils.SHIFT
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaIndiaCoffeeRminSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_rmin_summary
    private lateinit var binding: FragmentRminSummaryBinding

    private var callBack: CallBack? = null
    private val vm: VegaIndiaCoffeeRminViewModel by viewModel()
    private var model: VegaCocoaRminProcessing? = null
    private val plantId = getPlantDetails().plantId
    private var plantList = ArrayList<String>()

    interface CallBack {
        fun replaceAddLotFragment(
            fragment: String,
            flag: Boolean,
            model: VegaCocoaRminProcessing,
            isPoSelection: Boolean
        )

        fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    companion object {
        fun newInstance(model: VegaCocoaRminProcessing) =
            VegaIndiaCoffeeRminSummaryFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRminSummaryBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable("model")
    }

    private fun initUI() {
        binding.cvCard.tvBomValue.text = model?.bom
        if (getCurrentKey().split("_")[1].contains("NI"))
            binding.cvCard.tvMaterialValue.text = model?.materialName.toString()
        else
        binding.cvCard.tvMaterialValue.text = model?.materialNames.toString().replace("[","").replace("]","")
        binding.cvCard.tvStageValue.text = model?.stage
        binding.cvCard.tvBomValue.text = model?.bom
        binding.cvCard.tvProceesValue.text = model?.weightToProcess
        binding.tvShiftValue.text = model?.shift
        binding.tvProcessValue.text = model?.remark
        if (model?.poNumber?.isNotEmpty()!!) {
            binding.cvCard.tvBomValue.invisible()
            binding.cvCard.tvBom.invisible()
            binding.cvCard.tvPoNo.text = getString(R.string.po_numaber)
            binding.cvCard.tvPoNoValue.text = model?.poNumber
        } else {
            binding.cvCard.tvBomValue.visible()
            binding.cvCard.tvBom.visible()
            binding.cvCard.tvPoNo.text = getString(R.string.po_number)
            binding.cvCard.tvPoNoValue.text = model?.poQuantity.plus(" KG")
        }

        binding.ivEdit.setOnClickListener { callBack?.replaceFragment(SHIFT, true, model!!) }
        binding.btnProceed.setOnClickListener { showConformationDialog() }
        vm.postRmin.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.createPo.observe(viewLifecycleOwner, Observer { updateBomUI(it) })

        vm.getLotList(model?.cgfNo ?: "", model?.poNumber ?: "", model?.bom ?: "", model?.materialName ?: "")
        vm.vegaIndiaCoffeeRminLotItems.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                model?.lotList?.clear()
                model?.lotList?.addAll(it)
                setupAdapter()
            }
        })
    }

    private fun updateBomUI(response: Resource<GenericReqAndResp<VegaProcessingRminPo>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data
                            moveToSuccessPage(success)
                            vm.deleteAllLot(
                                model?.poNumber ?: "",
                                model?.cgfNo ?: "",
                                model?.bom ?: "",
                                model?.materialName ?: ""
                            )
                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun setupAdapter() {
        val list = mutableListOf<VegaCocoaRminLots>()
        list.addAll(model?.lotList ?: mutableListOf())
        binding.rvLotList.setUpAdapter(
            list,
            R.layout.item_lot_rmin_summary,
            ItemLotRminSummaryBinding::inflate,
            { it, _, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvStLocation.text = it.storageLocationCode
                bindItem.tvGradeValue.text = it.materialName
                val weight = it.weight?.toDouble()?.formatThreeDigits()
                bindItem.tvWeightValue.text = weight.plus(" ").plus(it.unitOfMeasure)
                bindItem.tvWeightToProcessValue.text =
                    it.editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                        .plus(it.unitOfMeasure)
                if (getCurrentKey().split("_")[1].contains("NI")) {
                    bindItem.ivSelect.setOnClickListener {
                        callBack?.replaceAddLotFragment(
                            ADDLOT,
                            true,
                            model!!,
                            true
                        )
                    }
                } else bindItem.ivSelect.setOnClickListener {
                    callBack?.replaceFragment(
                        ADDLOT,
                        true,
                        model!!
                    )
                }
            }, {})
    }

    /*private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_processing)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (model?.poNumber?.isNotEmpty()!!) {
                        vm.postRmin(preparePostData())
                    } else {
                        val poRequest = prepareRminCreatePoRequest(
                            model?.foreverNo!!,
                            model?.cgfNo!!,
                            model?.materialName!!,
                            model?.materialCode!!,
                            model!!,
                            model?.lotList!!
                        )
                        vm.createPoRequest(poRequest)
                    }
                },
                { dismiss() })
        }
    }*/

    private fun preparePostData(): VegaIndiaCoffeeRminProcessingPost {
        val postModel = VegaIndiaCoffeeRminProcessingPost()
        postModel.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        postModel.outputMaterialCode = model?.materialCode
        postModel.poQuantity = model?.poQuantity
        postModel.plant = getPlantDetails()
        postModel.remarks = model?.remark
        postModel.shiftType = model?.shift
        postModel.processingStage = model?.stage
        postModel.rmin = true
        val lots = model?.lotList
        val processList = mutableListOf<VegaIndiaCoffeeProcessingRMINLotDetails>()
        lots?.forEach {
            val processLot = VegaIndiaCoffeeProcessingRMINLotDetails()
            processLot.batchNumber = it.batchNumber
            processLot.materialCode = it.materialCode
            processLot.plant = it.plantId
            processLot.netWeight = if (it.editedWeight?.isNotEmpty()!!) it.editedWeight else it.weight
            val storeCode = it.storageLocationCode?.split("-")
            processLot.storageLocationCode = storeCode?.get(0)
            processLot.unitsOfMeasure = it.unitOfMeasure
            processLot.bagCount = it.noOfBags
            processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
            processLot.deliveryItem = it.deliveryItem
            processLot.menge = if (it.editedWeight?.isNotEmpty()!!) it.editedWeight else it.weight
            processLot.movementType = model?.bwart
            processLot.phase = model?.phase
            processLot.processOrderNum = it.poNumber
            processLot.rsnum = model?.rsnum
            processLot.rspos = model?.rspos
            processLot.xchpf = model?.xchpf
            processLot.resource = model?.resource
            processLot.shiftType = model?.shift ?: ""
            processLot.remarks = model?.remark ?: ""
            if (getCurrentKey().split("_")[1].contains("NI")) {
                processLot.startTime = DateUtils.getCurrentTimeInMills().toString()
                processLot.postingDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyy-MM-dd'T'HH:mm:ss")
                //processLot.startTime = System.currentTimeMillis().toString()
            }
            processList.add(processLot)
        }
        postModel.processingLotDtls = processList
        return postModel
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaIndiaCoffeeProcessingRminResponse>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data
                            moveToSuccessPage(success)
                            vm.deleteAllLot(
                                model?.poNumber ?: "",
                                model?.cgfNo ?: "",
                                model?.bom ?: "",
                                model?.materialName ?: ""
                            )
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


    private fun moveToSuccessPage(success: List<VegaIndiaCoffeeProcessingRminResponse>?) {
        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.rmin_saved))
        intent.putExtra(AppUtils.SUB_TITLE, message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun moveToSuccessPage(processRmin: VegaProcessingRminPo?) {
        val data = processRmin?.autoPoResponse?.map { it.batchNumber }
        val msg = processRmin?.autoPoResponse?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item ->
            if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "")
        }

        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.rmin_saved))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            message.plus(" ").plus(data.toString().replace("[", "").replace("]", ""))
        )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_processing)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        vm.configItems.observe(viewLifecycleOwner, Observer {
                            updateConfigItems(it)
                        })
                        vm.getConfigItems(UserRoles.PROCESSING.role)
                    } else {

                        if (model?.poNumber?.isNotEmpty()!!) {
                            vm.postRmin(preparePostData())
                        } else {
                            val poRequest = prepareRminCreatePoRequest(
                                model?.foreverNo!!,
                                model?.cgfNo!!,
                                model?.materialName!!,
                                model?.materialCode!!,
                                model!!,
                                model?.lotList!!
                            )
                            vm.createPoRequest(poRequest)
                        }
                    }
                },
                { dismiss() })
        }
    }

    fun prepareRminCreatePoRequest(
        stageFevor: String, cfgNumber: String,
        materialName: String,
        materialNo: String,
        model: VegaCocoaRminProcessing,
        lotDetails: ArrayList<VegaCocoaRminLots>
    ): VegaIndiaCoffeeProcessingCreatePoReq {
        val poReq = VegaIndiaCoffeeProcessingCreatePoReq()
        poReq.cfgNo = cfgNumber
        poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        poReq.processingStage = stageFevor
        poReq.outputMaterialCode = materialNo
        poReq.materialCode = model.baseMaterialCode
        poReq.plant = getPlantDetails()
        poReq.rmin = true
        poReq.versionId = model.versionId
        poReq.poQuantity = model.poQuantity
        val processList = mutableListOf<VegaIndiaCoffeeProcessingRMINLotDetails>()
        lotDetails.forEach {
            val processLot = VegaIndiaCoffeeProcessingRMINLotDetails()
            processLot.batchNumber = it.batchNumber
            processLot.materialCode = it.materialCode
            processLot.plant = it.plantId
            processLot.netWeight =
                if (it.editedWeight?.isNotEmpty()!!) it.editedWeight else it.weight
            val storeCode = it.storageLocationCode?.split("-")
            processLot.storageLocationCode = storeCode?.get(0)
            processLot.unitsOfMeasure = it.unitOfMeasure
            processLot.bagCount = it.noOfBags
            processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
            processLot.deliveryItem = it.deliveryItem
            processLot.menge = if (it.editedWeight?.isNotEmpty()!!) it.editedWeight else it.weight
            processLot.movementType = model.bwart
            processLot.phase = model.phase
            processLot.processOrderNum = it.poNumber
            processLot.rsnum = model.rsnum
            processLot.rspos = model.rspos
            processLot.xchpf = model.xchpf
            processLot.resource = model.resource
            processLot.shiftType = model.shift ?: ""
            processLot.remarks = model.remark ?: ""
            processList.add(processLot)
        }
        poReq.processingLotDtls = processList
        return poReq
    }


    fun prepareLotList(lotList: VegaCocoaRminLots, tempId: String): VegaNicOfflineRminLots {
        val rminLots = VegaNicOfflineRminLots()
        rminLots.batchNumber = lotList.batchNumber
        rminLots.rminTempId = tempId
        rminLots.storageLocationCode = lotList.storageLocationCode
        rminLots.materialName = lotList.materialName
        rminLots.weight = lotList.weight
        rminLots.editedWeight = lotList.editedWeight
        rminLots.lotId = lotList.remarks   //lot id
        rminLots.vendorCode = lotList.shift    //  vendor code
        rminLots.warehouseId = lotList.kor.toString()  // warehouseId
        return rminLots
    }


    fun prepareOfflineRminBomRequest(
        list: VegaIndiaCoffeeProcessingCreatePoReq,
        id: String
    ): VegaNicOfflineRminData {
        val rminList = VegaNicOfflineRminData()
        rminList.rminTempId = id
        rminList.outputMaterialCode = list.outputMaterialCode
        rminList.plant = list.plant.toString()
        rminList.processingStage = list.processingStage
        rminList.operatorName = list.cfgNo
        rminList.rmin = true
        rminList.shiftType = ""
        rminList.versionId = list.versionId
        return rminList
    }

    fun prepareOfflineRminBomLotDetails(
        list: VegaIndiaCoffeeProcessingRMINLotDetails,
        id: String,
        po: String,
        stage: String,
        materialName: String
    ): VegaNicOfflineRminProcessLotDetails {
        val processList = VegaNicOfflineRminProcessLotDetails()
        processList.rminTempId = id
        processList.batchNumber = list.batchNumber!!
        processList.bagCount = if (list.bagCount.isNullOrEmpty()) "0" else list.bagCount
        processList.confText = list.confText
        processList.deliveryItem = list.deliveryItem
        processList.materialCode = list.materialCode
        processList.menge = list.menge
        processList.movementType = list.movementType
        processList.netWeight = list.netWeight
        processList.grossWeight = ""
        processList.startTime = DateUtils.getCurrentTimeInMills().toString()
        processList.endTime = ""
        processList.phase = list.phase
        processList.plant = list.plant
        processList.bagType = ""
        processList.year = ""
        processList.processOrderNum = list.processOrderNum
        processList.rsnum = list.rsnum
        processList.rspos = list.rspos
        processList.storageLocationCode = list.storageLocationCode
        processList.unitsOfMeasure = list.unitsOfMeasure
        processList.xchpf = list.xchpf
        processList.huno = list.unitsOfMeasure
        processList.huwt = "0.0"
        processList.huno2 = list.unitsOfMeasure
        processList.huwt2 = "0"
        processList.nohu1 = "0"
        processList.nohu2 = "0"
        processList.bagMaterialCode = ""
        processList.endLotFlag = false
        processList.vendorCode = ""
        processList.storageLossFlag = false
        processList.poNo = po
        processList.stage = stage
        processList.materialName = materialName
        return processList
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val dryingPlants = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }
        plantList.clear()
        dryingPlants.forEach {
            var plantLists = it.value
            plantList = plantLists?.split(",") as ArrayList<String>
        }
        if (plantList.contains(plantId)) {

            if (model?.poNumber?.isNotEmpty()!!) {

                var poRequest = prepareRminCreatePoRequest(
                    model?.foreverNo!!,
                    model?.cgfNo!!,
                    model?.materialName!!,
                    model?.materialCode!!,
                    model!!,
                    model?.lotList!!
                )

                var tempId = "TMP_RMIN_".plus(Random.nextLong().toString())
                var qualityLotDetails = VegaNicOfflineRminProcessLotDetails()
                var qualityDetails = prepareOfflineRminBomRequest(poRequest, tempId)
                var rminLots = VegaNicOfflineRminLots()
                rminLots.materialName = model?.materialName!!
                var rminItem = VegaNicOfflineRminItems()
                poRequest.processingLotDtls.forEach {
                    qualityLotDetails = prepareOfflineRminBomLotDetails(
                        it, tempId, model?.poNumber!!,
                        model?.stage!!, model?.materialName!!
                    )
                }
                model?.lotList?.forEach {
                    rminLots = prepareLotList(it, tempId)
                    rminLots.poNo = model?.poNumber!!
                    vm.saveOfflineRminSelectedLots(rminLots)
                }
                vm.saveOfflineRminDetails(qualityDetails)
                vm.saveOfflineRminLotDetails(qualityLotDetails)
                vm.deleteAllLot(
                    model?.poNumber ?: "",
                    model?.cgfNo ?: "",
                    model?.bom ?: "",
                    model?.materialName ?: ""
                )
                moveToOfflineSuccessPage()
            }

        } else {

            if (model?.poNumber?.isNotEmpty()!!) {
                vm.postRmin(preparePostData())
            } else {
                val poRequest = prepareRminCreatePoRequest(
                    model?.foreverNo!!,
                    model?.cgfNo!!,
                    model?.materialName!!,
                    model?.materialCode!!,
                    model!!,
                    model?.lotList!!
                )
                vm.createPoRequest(poRequest)
            }
        }

    }

    private fun moveToOfflineSuccessPage() {
//        var updatedWeight = lotWeight.toDouble() - weight.toDouble()
        //vm.updateStockDetails(updatedWeight.toString(), batchNo)
        //vmdeleteLotDetails()
        //vmdeleteAllRminLots()
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(
            AppUtils.TITLE,
            requireContext().resources.getString(R.string.offline_rmin_saved)
        )
        startActivity(intent)
        requireActivity().finish()
    }

}
