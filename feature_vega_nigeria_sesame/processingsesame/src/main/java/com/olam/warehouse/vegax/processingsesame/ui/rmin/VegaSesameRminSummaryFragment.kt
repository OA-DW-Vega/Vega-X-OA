package com.olam.warehouse.vegax.processingsesame.ui.rmin

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
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRminItemWithGrades
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.data.domain.model.VegaSesameProcessingRminResponse
import com.olam.warehouse.vegax.processingsesame.databinding.FragmentSesameRminSummaryBinding
import com.olam.warehouse.vegax.processingsesame.utils.*
import kotlinx.android.synthetic.main.item_sesame_rmin_material_summary.view.*
import kotlinx.android.synthetic.main.item_sesame_summary_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaSesameRminSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_sesame_rmin_summary
    private lateinit var binding: FragmentSesameRminSummaryBinding
    private var callBack: CallBack? = null
    private val vm: VegaSesameRminViewModel by viewModel()
    private var model: VegaCoffeeRminProcessing? = null
    private var gradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var noOfGradeItems: Int = 0
    private var fgrnItemWithGrades = VegaCoffeeRminItemWithGrades()
    private var lotList = VegaCoffeeRminLots()

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
            VegaSesameRminSummaryFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSesameRminSummaryBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable("model")
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
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
        binding.rvMaterialList.setUp(grade, R.layout.item_sesame_rmin_material_summary, { it, pos ->
            tvMaterialValue.text = it.materialName
            tvStageValue.text = it.weightToProcess
        }, {})
    }

    private fun setupAdapter() {
        lotList = VegaCoffeeRminLots()
        val list = mutableListOf<VegaCoffeeRminLots>()
        model?.lotList?.forEach {
            it.remarks = convertKgToMT(it.weight, it.unitOfMeasure)!!
        }
        list.addAll(model?.lotList ?: mutableListOf())
        binding.rvLotList.setUp(list, R.layout.item_sesame_summary_lot, { it, pos ->
            tvScaleLotValue.text = it.batchNumber
            tvStLocationValue.text = it.storageLocationCode
            tvScaleGradeValue.text = it.materialName

            val weight = it.weight?.toDouble()
            val enteredWeight = it.editedWeight?.toDouble()
            val totalLoss = weight?.minus(enteredWeight!!)
            tvScaleWeightValue.text = weight?.formatThreeDigits().plus(" ").plus("KG")
            tvScaleDispatchValue.text =
                it.editedWeight?.toDouble()?.formatThreeDigits().plus(" ").plus("KG")

//            tvTotalWeightLossValue.text = totalLoss?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)

            if(it.isEndLot == true){
                tvTotalWeightLossValue.text = totalLoss?.formatThreeDigits().plus(" ").plus("KG")
            }
            else
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
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    val count = PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").toInt()
                    if (count != 0 && DateUtils.isFirstDaysOfMonth(context, count)) {
                        UIUtils.showPostingDateDialog(context, count, object : UIUtils.DialogClick {
                            override fun onPositive(date: String) {
                                var postingDate =
                                    DateUtils.oneFormatToOtherFormat(
                                        date,
                                        "dd-MMM-yyyy",
                                        "yyyy-MM-dd'T'HH:mm:ss"
                                    )
                                val postReq =
                                    prepareRminPostRequest(model, fgrnItemWithGrades, postingDate)
                                vm.postRmin(postReq)
                            }
                        })
                    } else {

                        val postReq = prepareRminPostRequest(model, fgrnItemWithGrades, "")
                        vm.postRmin(postReq)
                    }

                },
                { dismiss() })
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaSesameProcessingRminResponse>>>) {
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


    private fun moveToSuccessPage(success: List<VegaSesameProcessingRminResponse>?, serverMessage: String) {
        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        vm.deleteLotDetails()
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.rmin_saved))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")).plus(serverMessage)
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
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}
