package com.olam.warehouse.vegax.processingcocoa.ui.rmin

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
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcocoa.R
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaProcessingRMINLotDetails
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaProcessingRminResponse
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaRminProcessingPost
import com.olam.warehouse.vegax.processingcocoa.databinding.FragmentRminSummaryBinding
import com.olam.warehouse.vegax.processingcocoa.utils.ADDLOT
import com.olam.warehouse.vegax.processingcocoa.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingcocoa.utils.SHIFT
import com.olam.warehouse.vegax.processingcocoa.utils.prepareRminCreatePoRequest
import kotlinx.android.synthetic.main.item_lot_rmin_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaRminSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_rmin_summary
    private lateinit var binding: FragmentRminSummaryBinding

    private var callBack: CallBack? = null
    private val vm: VegaCocoaRminViewModel by viewModel()
    private var model: VegaCocoaRminProcessing? = null

    interface CallBack {
        fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/rmin/VegaCocoaRminSummaryFragment")
            .title("Rmin Cocoa")
            .with(tracker)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    companion object {
        fun newInstance(model: VegaCocoaRminProcessing) =
            VegaCocoaRminSummaryFragment().putArgs {
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
        binding.cvCard.tvMaterialValue.text = model?.materialName
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

    private fun setupAdapter() {
        val list = mutableListOf<VegaCocoaRminLots>()
        list.addAll(model?.lotList ?: mutableListOf())
        binding.rvLotList.setUp(list, R.layout.item_lot_rmin_summary, { it, _ ->
            tvLotId.text = it.batchNumber
            tvStLocation.text = it.storageLocationCode
            tvGradeValue.text = it.materialName
            val weight = it.weight?.toDouble()?.formatThreeDigits()
            tvWeightValue.text = weight.plus(" ").plus(it.unitOfMeasure)
            tvWeightToProcessValue.text =
                it.editedWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
            ivSelect.setOnClickListener { callBack?.replaceFragment(ADDLOT, true, model!!) }
        }, {})
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_processing)
            getMetirialCustomView(
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

    private fun preparePostData(): VegaCocoaRminProcessingPost {
        val postModel = VegaCocoaRminProcessingPost()
        postModel.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        postModel.outputMaterialCode = model?.materialCode
        postModel.plant = getPlantDetails()
        postModel.remarks = model?.remark
        postModel.shiftType = model?.shift
        postModel.processingStage = model?.stage
        postModel.rmin = true
        val lots = model?.lotList
        val processList = mutableListOf<VegaCocoaProcessingRMINLotDetails>()
        lots?.forEach {
            val processLot = VegaCocoaProcessingRMINLotDetails()
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

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaProcessingRminResponse>>>) {
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


    private fun moveToSuccessPage(success: List<VegaCocoaProcessingRminResponse>?) {
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
