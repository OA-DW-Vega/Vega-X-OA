package com.olam.warehouse.vegax.processingnigeria.ui.rmin

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
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaProcessingRminResponse
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentNigeriaRminSummaryBinding
import com.olam.warehouse.vegax.processingnigeria.utils.*
import kotlinx.android.synthetic.main.item_nigeria_summary_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNigeriaRminSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_nigeria_rmin_summary
    private lateinit var binding: FragmentNigeriaRminSummaryBinding
    private var callBack: CallBack? = null
    private val vm: VegaNigeriaRminViewModel by viewModel()
    private var model: VegaCoffeeRminProcessing? = null
    private var gradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var noOfGradeItems: Int = 0
    private var fgrnItemWithGrades = VegaCoffeeRminItemWithGrades()

    interface CallBack {
        fun replaceFragment(fragment: String, flag: Boolean, fgrnMaterialCode: String, model: VegaCoffeeRminProcessing)
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
            VegaNigeriaRminSummaryFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNigeriaRminSummaryBinding.inflate(layoutInflater)
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
        binding.cvCard.tvBomValue.text = model?.bom
        if (model?.weightToProcess.toString().contains("MT")) {
            binding.cvCard.tvProceesValue.text =
                (model?.weightToProcess.toString().split(" ")[0]).plus(" ").plus("KG")
        } else {
            binding.cvCard.tvProceesValue.text = model?.weightToProcess
        }
        binding.tvShiftValue.text = model?.shift
        binding.tvProcessValue.text = model?.remark

        if (model?.poNumber?.isNotEmpty()!!) {
            binding.cvCard.tvBomValue.invisible()
            binding.cvCard.tvBom.invisible()
            binding.cvCard.tvPoNo.text = getString(R.string.po_numaber)
            binding.cvCard.tvPoNoValue.text = model?.poNumber
            vm.getRminLive(model?.rminId ?: "")
            vm.rminItemData.observe(
                viewLifecycleOwner,
                Observer { if (it != null) updateItems(it) })
        } else {
            binding.cvCard.tvBomValue.visible()
            binding.cvCard.tvBom.visible()
            binding.cvCard.tvPoNo.text = getString(R.string.po_number)
            binding.cvCard.tvPoNoValue.text = model?.poQuantity.plus(" KG")
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

    private fun updateItems(data: VegaCoffeeRminItemWithGrades) {
        fgrnItemWithGrades = data
        if (fgrnItemWithGrades.processing.gradeListDetails.isNullOrEmpty()) {
        } else {
            gradeList =
                Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(
                    fgrnItemWithGrades.processing.gradeListDetails ?: ""
                )
            noOfGradeItems = gradeList.size
            //  initGradeList()
        }
    }

    private fun setupAdapter() {
        val list = mutableListOf<VegaCoffeeRminLots>()
        list.addAll(model?.lotList ?: mutableListOf())
        binding.rvLotList.setUp(list, R.layout.item_nigeria_summary_lot, { it, pos ->
            tvScaleLotValue.text = it.batchNumber
            tvStLocationValue.text = it.storageLocationCode
            tvScaleGradeValue.text = it.materialName

            val weight = it.weight?.toDouble()
            val enteredWeight = it.editedWeight?.toDouble()
            val totalLoss = weight?.minus(enteredWeight!!)
            tvScaleWeightValue.text =
                weight?.formatThreeDigits().plus(" ").plus(it.weightToDispatchUOM)
            tvScaleDispatchValue.text =
                it.editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                    .plus(it.weightToDispatchUOM)

//            tvTotalWeightLossValue.text = totalLoss?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)

            if (it.isEndLot == true) {
                tvTotalWeightLossValue.text =
                    totalLoss?.formatThreeDigits().plus(" ").plus(it.weightToDispatchUOM)
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
                        vm.postRmin(postReq)
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

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaNigeriaProcessingRminResponse>>>) {
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


    private fun moveToSuccessPage(
        success: List<VegaNigeriaProcessingRminResponse>?,
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

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}
