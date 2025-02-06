package com.olam.warehouse.vegax.processingcoffee.ui.rmin

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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRminItemWithGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcoffee.R
import com.olam.warehouse.vegax.processingcoffee.data.domain.model.VegaCoffeeProcessingRminResponse
import com.olam.warehouse.vegax.processingcoffee.databinding.FragmentCoffeeRminSummaryBinding
import com.olam.warehouse.vegax.processingcoffee.databinding.ItemRminMaterialSummaryBinding
import com.olam.warehouse.vegax.processingcoffee.databinding.ItemSummaryLotBinding
import com.olam.warehouse.vegax.processingcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeRminSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_coffee_rmin_summary
    private lateinit var binding: FragmentCoffeeRminSummaryBinding
    private var callBack: CallBack? = null
    private val vm: VegaCoffeeRminViewModel by viewModel()
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
            VegaCoffeeRminSummaryFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCoffeeRminSummaryBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcoffee/ui/rmin/VegaCocoaRMINSelectPoFragment")
            .title("IVC/Coffee/Processing/RMIN Summary").with(tracker)
    }

    private fun initExtra() {
        model = arguments?.getParcelable("model")
    }

    private fun initUI() {
        binding.tvPoNumberValue.text = model?.poNumber
        binding.tvStageValue.text = model?.stage
        vm.getRminLive(model?.rminId ?: "")
        vm.rminItemData.observe(viewLifecycleOwner, Observer { if (it != null) updateItems(it) })
        binding.ivEdit.setOnClickListener {
            callBack?.replaceFragment(SHIFT, true, "", model!!)
        }
        binding.btnProceed.setOnClickListener {
            if (model?.lotList.isNullOrEmpty()) {
                showSnack(getString(R.string.please_assign_lot))
            } else showConfirmDialog()
        }
        vm.postRmin.observe(viewLifecycleOwner, Observer { updateUI(it) })
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
        binding.rvMaterialList.setUpAdapter(
            grade,
            R.layout.item_rmin_material_summary,
            ItemRminMaterialSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialValue.text = it.materialName
                bindItem.tvStageValue.text = it.weightToProcess
            },
            {})
    }

    private fun setupAdapter() {
        val list = mutableListOf<VegaCoffeeRminLots>()
        list.addAll(model?.lotList ?: mutableListOf())
        binding.rvLotList.setUpAdapter(
            list,
            R.layout.item_summary_lot,
            ItemSummaryLotBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvScaleLotValue.text = it.batchNumber
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvScaleGradeValue.text = it.materialName
                val weight = it.weight?.toDouble()?.formatThreeDigits()
                bindItem.tvScaleWeightValue.text = weight.plus(" ").plus(it.unitOfMeasure)
                bindItem.tvScaleDispatchValue.text =
                    it.editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                        .plus(it.unitOfMeasure)
                bindItem.ivScaleClose.setOnClickListener {
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
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    val postReq = prepareRminPostRequest(model, fgrnItemWithGrades)
                    vm.postRmin(postReq)
                },
                { dismiss() })
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCoffeeProcessingRminResponse>>>) {
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


    private fun moveToSuccessPage(success: List<VegaCoffeeProcessingRminResponse>?, serverMessage: String) {
        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.rmin_saved))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")).plus(" ").plus(serverMessage)
        )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateItems(data: VegaCoffeeRminItemWithGrades) {
        fgrnItemWithGrades = data
        gradeList =
            Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(fgrnItemWithGrades.processing.gradeListDetails ?: "")
        noOfGradeItems = gradeList.size
        initGradeList()
    }

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}
