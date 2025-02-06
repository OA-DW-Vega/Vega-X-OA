package com.olam.warehouse.vegax.pileindiacoffee.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.pileindiacoffee.R
import com.olam.warehouse.vegax.pileindiacoffee.databinding.FragmentIndiaCoffeePileManagementSummaryBinding
import com.olam.warehouse.vegax.pileindiacoffee.databinding.ItemIndiaCoffeePileManagementSummaryBinding
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.model.VegaCoffeePilePostRequest
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.model.VegaCoffeePileResponse
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.model.VegaPileSelectionModel
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.PILE_LIST
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.PILE_MANAGEMENT_SUMMARY
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeePileManagementSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_india_coffee_pile_management_summary
    private lateinit var binding: FragmentIndiaCoffeePileManagementSummaryBinding
    private var callBack: CallBack? = null
    private var alreadySelected = arrayListOf<VegaCocoaDispatchLots>()
    private val vm: VegaIndiaCoffeePileManagementViewModel by viewModel()
    private var materialList = arrayListOf<VegaCocoaDispatchLots>()
    private lateinit var vegaPileSelectionModel: VegaPileSelectionModel
    private var pileSelection = VegaPileSelectionModel()

    interface CallBack {
        fun editLot(paramsFrag: String, isEdit: Boolean, alreadySelected: ArrayList<VegaCocoaDispatchLots>)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            materialList: ArrayList<String>, alreadySelected: ArrayList<VegaCocoaDispatchLots>,
            pileSelectionList: VegaPileSelectionModel
        ) =
            VegaIndiaCoffeePileManagementSummaryFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelableArrayList(PILE_MANAGEMENT_SUMMARY, alreadySelected)
                putParcelable(PILE_LIST, pileSelectionList)
            }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIndiaCoffeePileManagementSummaryBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("pilemanagement/ui//VegaCoffeePileManagementSummaryFragment").title("PileMangement")
            .with(tracker)
        initUI()
    }

    private fun initUI() {

        vm.postPile.observe(viewLifecycleOwner, Observer { updateSuccessUI(it) })
        pileSelection = arguments?.getParcelable<VegaPileSelectionModel>(PILE_LIST) as VegaPileSelectionModel
        setupPileCardView(pileSelection)
        alreadySelected = arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(PILE_MANAGEMENT_SUMMARY) as ArrayList
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<VegaCocoaDispatchLots>
        setupAdapter(alreadySelected)

        binding.btnProceed.setOnClickListener {
            showConformationDialog()
        }

    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_piles)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    private fun postDelivery() {
        pileSelection.unitOfMeasure = alreadySelected[0].unitOfMeasure
        pileSelection.materialName = alreadySelected[0].materialName
        pileSelection.weight = weightTransfer()
        if (!pileSelection.vendor.isNullOrBlank()) {
            val data = pileSelection.vendor?.split("-")
            if (!data.isNullOrEmpty()) {
                pileSelection.vendor = data[0]
                pileSelection.vendorName = data[1]
            }
        }

        alreadySelected.forEach { item ->
            if (item.batchNumber.equals(item.batchNumber) && item.materialCode.equals(item.materialCode)){
                item.weight =item.editedWeight
            }
        }


        val postPileRequest = VegaCoffeePilePostRequest(
            key = getCurrentKey(),
            plant = getPlantDetails(),
            lots = alreadySelected,
            pileDetails = pileSelection
        )
        vm.getPostPile(postPileRequest)
    }

    private fun updateSuccessUI(response: Resource<GenericReqAndResp<VegaCoffeePileResponse>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            vm.getCreatePile(true)
                            movetoSuccess(it.data?.data)
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun movetoSuccess(data: VegaCoffeePileResponse?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.pile_created, data?.pileNo))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupAdapter(data: MutableList<VegaCocoaDispatchLots>) {
        binding.tvGrnValue.text = weightTransfer().plus(" ").plus(data[0].unitOfMeasure)
        binding.tvProceesValue.text =
            if (pileSelection.weight.isNullOrEmpty() || pileSelection.weight.equals("0.0")) weightTransfer().plus(
                " "
            )
                .plus(data[0].unitOfMeasure) else pileSelection.weight.plus(" ")
                .plus(data[0].unitOfMeasure)
        binding.rvLotList.setUpAdapter(
            data,
            R.layout.item_india_coffee_pile_management_summary,
            ItemIndiaCoffeePileManagementSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                binding.tvBomValue.text = it.materialName
                bindItem.tvStLocation.text = it.materialName
                bindItem.tvGradeValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.tvWeightToProcessValue.text =
                    it.editedWeight.plus(" ").plus(it.unitOfMeasure)
                bindItem.ivSelect.setOnClickListener { /*callBack?.editLot(LOT_LIST, true, alreadySelected)*/  activity?.onBackPressed() }
            })
    }

    private fun setupPileCardView(data: VegaPileSelectionModel) {
        vegaPileSelectionModel = data
        binding.tvMaterialValue.text = data.batchNumber
        binding.tvStageValue.text = if (data.vendor.isNullOrEmpty()) " --- " else data.vendor
        /*binding.tvProceesValue.text =
            if (data.weight.isNullOrEmpty() || data.weight.equals("0.0")) weightTransfer().plus(" ")
                .plus(data.unitOfMeasure) else data.weight.plus(" ").plus(data.unitOfMeasure)
        binding.tvBomValue.text = data.materialName*/
    }

    private fun weightTransfer(): String {
        var weight = 0.0
        alreadySelected.forEach {
            weight = weight.plus(it.editedWeight.toString().toDouble())
        }
        return weight.toString()
    }
}
