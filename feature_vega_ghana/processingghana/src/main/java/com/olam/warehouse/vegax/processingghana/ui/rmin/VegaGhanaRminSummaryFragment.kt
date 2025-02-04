package com.olam.warehouse.vegax.processingghana.ui.rmin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRminItemWithGrades
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminItems
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminLots
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingRminResponse
import com.olam.warehouse.vegax.processingghana.databinding.FragmentGhanaRminSummaryBinding
import com.olam.warehouse.vegax.processingghana.utils.*
import kotlinx.android.synthetic.main.item_ghana_rmin_material_summary.view.*
import kotlinx.android.synthetic.main.item_ghana_summary_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random

class VegaGhanaRminSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_rmin_summary
    private lateinit var binding: FragmentGhanaRminSummaryBinding
    private var callBack: CallBack? = null
    private val vm: VegaGhanaRminViewModel by viewModel()
    private var model: VegaCoffeeRminProcessing? = null
    private var gradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var noOfGradeItems: Int = 0
    private var fgrnItemWithGrades = VegaCoffeeRminItemWithGrades()
    private var lotList = VegaCoffeeRminLots()
    private var materialName: String = ""
    private var batchNo: String = ""
    private var weight: String = ""
    private var lotWeight: String = ""

    interface CallBack {
        fun replaceFragment(
            fragment: String,
            flag: Boolean,
            fgrnMaterialCode: String,
            model: VegaCoffeeRminProcessing
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        //menu.clear()
    }

    companion object {
        fun newInstance(model: VegaCoffeeRminProcessing) =
            VegaGhanaRminSummaryFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGhanaRminSummaryBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable("model")
    }

    private fun initUI() {
        binding.tvPoNumberValue.text = model?.poNumber
        binding.tvStageValue.text = model?.stage
        if (model?.gradeList?.size != 0) {
            vm.getRminLive(model?.rminId ?: "")
            vm.rminItemData.observe(
                viewLifecycleOwner,
                Observer { if (it != null) updateItems(it) })
        }
        binding.ivEdit.setOnClickListener {
            callBack?.replaceFragment(SHIFT, true, "", model!!)
        }
        binding.btnProceed.setOnClickListener {
            if (model?.lotList.isNullOrEmpty()) {
                showSnack(getString(R.string.please_assign_lot))
            } else showConfirmDialog()
        }
        vm.postRmin.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.createPo.observe(viewLifecycleOwner, Observer { updateBomUI(it) })
        vm.getLotList(model?.poNumber ?: "", "")
        binding.tvShiftValue.text = model?.shift
        binding.tvProcessValue.text = model?.remark
        vm.vegaCoffeeRminLotItems.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                model?.lotList?.clear()
                model?.lotList?.addAll(it)
                setupAdapter()
                enableDisableBtn(true)
            } else {
                enableDisableBtn(false)
            }
        })

    }

    private fun initGradeList() {
        val grade = gradeList as MutableList
        binding.rvMaterialList.setUp(grade, R.layout.item_ghana_rmin_material_summary, { it, pos ->
            tvMaterialValue.text = it.materialName
            materialName = it.materialName
            tvStageValue.text = it.weightToProcess
        }, {})
    }

    private fun setupAdapter() {
        lotList = VegaCoffeeRminLots()
        val list = mutableListOf<VegaCoffeeRminLots>()
        model?.lotList?.forEach {
            it.remarks = it.weight!!
        }
        list.addAll(model?.lotList ?: mutableListOf())
        binding.rvLotList.setUp(list, R.layout.item_ghana_summary_lot, { it, pos ->
            tvScaleLotValue.text = it.batchNumber
            batchNo = it.batchNumber
            weight = it.editedWeight!!
            lotWeight = it.weight!!
            tvStLocationValue.text = it.storageLocationCode
            tvScaleGradeValue.text = it.materialName

            val weight = it.weight?.toDouble()
            val enteredWeight = it.editedWeight?.toDouble()
            val totalLoss = weight?.minus(enteredWeight!!)
            tvScaleWeightValue.text = weight?.formatThreeDigits().plus(" ").plus("MT")
            tvScaleDispatchValue.text =
                it.editedWeight?.toDouble()?.formatThreeDigits().plus(" ").plus("MT")

//            tvTotalWeightLossValue.text = totalLoss?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)

            if (it.isEndLot == true) {
                tvTotalWeightLossValue.text = totalLoss?.formatThreeDigits().plus(" ").plus("MT")
            } else
                tvTotalWeightLossValue.text = "NA"

            ivScaleClose.setOnClickListener {
                callBack?.replaceFragment(
                    EDITLOT,
                    true,
                    list[pos].fgrnIdMaterialCode,
                    model!!
                )
            }
        }, {})
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.rmin_post_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (model?.poNumber?.isNotEmpty()!!) {
                        val postReq = prepareRminPostRequest(model, fgrnItemWithGrades)

                        if (isOnline()) {
                            vm.postRmin(postReq)
                        } else {
                            var tempId = "TMP_RMIN_".plus(Random.nextLong().toString())
                            var qualityLotDetails = VegaGhanaOfflineRminProcessLotDetails()
                            var qualityDetails = prepareOfflineRminPostRequest(postReq, tempId)
                            var rminLots = VegaGhanaOfflineRminLots()
                            var rminItem = VegaGhanaOfflineRminItems()
                            postReq.processingLotDtls.forEach {
                                qualityLotDetails = prepareOfflineRminProcessLotDetails(
                                    it, tempId, model?.poNumber!!,
                                    model?.stage!!, materialName
                                )
                            }
                            model?.lotList?.forEach {
                                rminLots = prepareLotList(it, tempId)
                                vm.saveOfflineRminSelectedLots(rminLots)
                            }
                            gradeList.forEach {
                                rminItem = prepareItems(it, tempId)
                                vm.saveOfflineRminItem(rminItem)
                            }
                            vm.saveOfflineRminDetails(qualityDetails)
                            vm.saveOfflineRminLotDetails(qualityLotDetails)
                            moveToOfflineSuccessPage()
                        }
                    } else {

                        var poRequest = prepareRminCreatePoRequest(
                            model?.foreverNo!!,
                            model?.cgfNo!!,
                            model?.materialName!!,
                            model?.materialCode!!,
                            model!!,
                            model?.lotList!!
                        )
                        if (AppUtils.isOnline())
                            vm.createPoRequest(poRequest)
                        else {
                            var tempId = "TMP_RMIN_".plus(Random.nextLong().toString())
                            var qualityLotDetails = VegaGhanaOfflineRminProcessLotDetails()
                            var qualityDetails = prepareOfflineRminBomRequest(poRequest, tempId)
                            var rminLots = VegaGhanaOfflineRminLots()
                            rminLots.materialName = model?.materialName!!
                            var rminItem = VegaGhanaOfflineRminItems()
                            poRequest.processingLotDtls.forEach {
                                qualityLotDetails = prepareOfflineRminBomLotDetails(
                                    it, tempId, model?.poNumber!!,
                                    model?.stage!!, model?.materialName!!
                                )
                            }
                            model?.lotList?.forEach {
                                rminLots = prepareLotList(it, tempId)
                                vm.saveOfflineRminSelectedLots(rminLots)
                            }
                            vm.saveOfflineRminDetails(qualityDetails)
                            vm.saveOfflineRminLotDetails(qualityLotDetails)
                            moveToOfflineSuccessPage()
                        }
                    }
                },
                { dismiss() })
        }
    }

    private fun moveToOfflineSuccessPage() {
        var updatedWeight = lotWeight.toDouble() - weight.toDouble()
        vm.updateStockDetails(updatedWeight.toString(), batchNo)
        vm.deleteLotDetails()
        vm.deleteAllRminLots()
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(
            AppUtils.TITLE,
            requireContext().resources.getString(R.string.offline_rmin_saved)
        )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaGhanaProcessingRminResponse>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data
                            moveToSuccessPage(success, it.data?.message ?: "")
                            vm.updateSyncStatus(
                                model!!
                            )
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
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun moveToSuccessPage(
        success: List<VegaGhanaProcessingRminResponse>?,
        serverMessage: String
    ) {
        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        vm.deleteLotDetails()
        data?.forEach { item ->
            if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "")
        }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.rmin_saved))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            message.plus(" ").plus(data.toString().replace("[", "").replace("]", ""))
                .plus(serverMessage)
        )
        startActivity(intent)
        requireActivity().finish()
    }
    private fun moveToSuccessPage_autoPO(success: VegaProcessingRminPo?, serverMessage: String) {
        val data = success?.autoPoResponse?.get(0)?.batchNumber
        val msg = success?.autoPoResponse?.get(0)?.messages?.get(0)?.message
        var message = ""
        vm.deleteLotDetails()
        data?.forEach { item ->
            if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "")
        }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.rmin_saved))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            message.plus(" ").plus(data.toString().replace("[", "").replace("]", ""))
                .plus(serverMessage)
        )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateItems(data: VegaCoffeeRminItemWithGrades) {
        fgrnItemWithGrades = data
        gradeList =
            Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(
                fgrnItemWithGrades.processing.gradeListDetails ?: ""
            )
        noOfGradeItems = gradeList.size
        initGradeList()
    }

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
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
}
