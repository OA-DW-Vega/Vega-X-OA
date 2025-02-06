package com.olam.warehouse.vegax.processingindo.ui.rmin

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
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.processingindo.R
import com.olam.warehouse.vegax.processingindo.data.domain.model.VegaProcessingIndoRMINLotDetails
import com.olam.warehouse.vegax.processingindo.data.domain.model.VegaProcessingIndoRminProcessingPost
import com.olam.warehouse.vegax.processingindo.data.domain.model.VegaProcessingIndoRminResponse
import com.olam.warehouse.vegax.processingindo.databinding.FragmentProcessingIndoRminSummaryBinding
import com.olam.warehouse.vegax.processingindo.databinding.ItemProcessingIndoLotRminSummaryBinding
import com.olam.warehouse.vegax.processingindo.utils.ADDLOT
import com.olam.warehouse.vegax.processingindo.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingindo.utils.SHIFT
import com.olam.warehouse.vegax.processingindo.utils.prepareRminCreatePoRequest
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaProcessingIndoRminSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_processing_indo_rmin_summary
    private lateinit var binding: FragmentProcessingIndoRminSummaryBinding

    private var callBack: CallBack? = null
    private val vm: VegaProcessingIndoRminViewModel by viewModel()
    private var model: VegaCocoaRminProcessing? = null

    interface CallBack {
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
            VegaProcessingIndoRminSummaryFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProcessingIndoRminSummaryBinding.inflate(layoutInflater)
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
        binding.cvCard.tvMaterialValue.text = model?.materialNames.toString().replace("[","").replace("]","")
        binding.cvCard.tvStageValue.text = model?.stage
        binding.cvCard.tvPoNoValue.text = model?.poNumber
        binding.cvCard.tvBomValue.text = model?.bom
        binding.cvCard.tvProceesValue.text = model?.weightToProcess
        binding.tvShiftValue.text = model?.shift
        binding.tvProcessValue.text = model?.remark
        if (model?.poNumber?.isNotEmpty()!!) {
            binding.cvCard.tvBomValue.invisible()
            binding.cvCard.tvBom.invisible()
            binding.cvCard.tvPoNoValue.visible()
            binding.cvCard.tvPoNo.visible()
        } else {
            binding.cvCard.tvBomValue.visible()
            binding.cvCard.tvBom.visible()
            binding.cvCard.tvPoNoValue.invisible()
            binding.cvCard.tvPoNo.invisible()
        }

        binding.ivEdit.setOnClickListener { callBack?.replaceFragment(SHIFT, true, model!!) }
        binding.btnProceed.setOnClickListener { showConformationDialog() }
        vm.postRmin.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.createPo.observe(viewLifecycleOwner, Observer { updateBomUI(it) })

        vm.getLotList(model?.cgfNo ?: "", model?.poNumber ?: "", model?.bom ?: "", model?.materialName ?: "")
        vm.vegaCocoaRminLotItems.observe(viewLifecycleOwner, Observer {
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
            R.layout.item_processing_indo_lot_rmin_summary,
            ItemProcessingIndoLotRminSummaryBinding::inflate,
            { it, _, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvStLocation.text = it.storageLocationCode
                bindItem.tvGradeValue.text = it.materialName
                val weight = it.weight?.toDouble()?.formatThreeDigits()
                bindItem.tvWeightValue.text = weight.plus(" ").plus(it.unitOfMeasure)
                bindItem.tvWeightToProcessValue.text =
                    it.editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                        .plus(it.unitOfMeasure)
                bindItem.ivSelect.setOnClickListener {
                    callBack?.replaceFragment(
                        ADDLOT,
                        true,
                        model!!
                    )
                }
            },
            {})
    }

    private fun showConformationDialog() {
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
    }

    private fun preparePostData(): VegaProcessingIndoRminProcessingPost {
        val postModel = VegaProcessingIndoRminProcessingPost()
        postModel.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        postModel.outputMaterialCode = model?.materialCode
        postModel.plant = getPlantDetails()
        postModel.remarks = model?.remark
        postModel.shiftType = model?.shift
        postModel.processingStage = model?.stage
        postModel.rmin = true
        val lots = model?.lotList
        val processList = mutableListOf<VegaProcessingIndoRMINLotDetails>()
        lots?.forEach {
            val processLot = VegaProcessingIndoRMINLotDetails()
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
            processList.add(processLot)
        }
        postModel.processingLotDtls = processList
        return postModel
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaProcessingIndoRminResponse>>>) {
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
                            //vm.dele
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


    private fun moveToSuccessPage(success: List<VegaProcessingIndoRminResponse>?) {
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
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }

        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.rmin_saved))
        intent.putExtra(AppUtils.SUB_TITLE, message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")))
        startActivity(intent)
        requireActivity().finish()
    }
}
