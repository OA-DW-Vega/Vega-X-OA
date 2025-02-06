package com.olam.warehouse.vegax.pilemanagementnigeria.ui

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
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.pilemanagementnigeria.R
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.VegaNigeriaPilePostRequest
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.VegaNigeriaPileSuccessResponse
import com.olam.warehouse.vegax.pilemanagementnigeria.databinding.FragmentPileManagementSummaryNigeriaBinding
import com.olam.warehouse.vegax.pilemanagementnigeria.databinding.ItemPileManagementSummaryNigeriaBinding
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.MATERIAL_CODE_SEQUENCE
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.PILE_LIST
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.PILE_MANAGEMENT_SUMMARY
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaPileManagementSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_pile_management_summary_nigeria
    private lateinit var binding: FragmentPileManagementSummaryNigeriaBinding
    private var callBack: CallBack? = null
    private var alreadySelected = arrayListOf<VegaCocoaDispatchLots>()
    private val vm: VegaNigeriaPileManagementViewModel by viewModel()
    private var materialList = arrayListOf<VegaCocoaDispatchLots>()
    private lateinit var vegaPileSelectionModel: VegaCocoaDispatchLots
    private var pileSelection = VegaCocoaDispatchLots()
    private var storageLocationCode: String? = ""
    private var materialCode: String? = ""
    private var plantId: String? = ""
    private var totalweight: Double = 0.0

    interface CallBack

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }


    companion object {
        fun newInstance(
            materialList: ArrayList<String>, alreadySelected: ArrayList<VegaCocoaDispatchLots>,
            pileSelectionList: VegaCocoaDispatchLots
        ) =
            VegaNigeriaPileManagementSummaryFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelableArrayList(PILE_MANAGEMENT_SUMMARY, alreadySelected)
                putParcelable(PILE_LIST, pileSelectionList)
            }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPileManagementSummaryNigeriaBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("pilemanagement/ui//VegaCoffeePileManagementSummaryFragment")
            .title("PileMangement")
            .with(tracker)
        initUI()
    }

    private fun initUI() {

        vm.postPile.observe(viewLifecycleOwner, Observer { updateSuccessUI(it) })
        pileSelection =
            arguments?.getParcelable<VegaCocoaDispatchLots>(PILE_LIST) as VegaCocoaDispatchLots
        setupPileCardView(pileSelection)
        alreadySelected =
            arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(PILE_MANAGEMENT_SUMMARY) as ArrayList
        alreadySelected.forEach {
            storageLocationCode = it.storageLocationCode
            materialCode = it.materialCode
            plantId = it.plantId
            totalweight += it.editedWeight?.toDouble()!!
        }
        materialList =
            arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<VegaCocoaDispatchLots>
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
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    private fun postDelivery() {
        if (pileSelection.batchNumber.length == 6) {
            pileSelection.storageLocationCode = storageLocationCode
            pileSelection.weight = totalweight.toString()
            pileSelection.materialCode = materialCode!!
            pileSelection.plantId = plantId
        }
        alreadySelected.forEach {
            it.weight = it.editedWeight
            it.editedWeight = it.weight
        }

        //val count= PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").toInt()
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
                    pileSelection.postingDate = postingDate
                    if (pileSelection.materialCode.length < 13) {
                        pileSelection.materialCode =
                            MATERIAL_CODE_SEQUENCE.plus(pileSelection.materialCode)
                    }
                    val postPileRequest = VegaNigeriaPilePostRequest(
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        lots = alreadySelected,
                        pileDetails = pileSelection
                    )
                    vm.getPostPile(postPileRequest)
                }
            })
        } else {
            if(pileSelection.materialCode.length<13) {
                pileSelection.materialCode = MATERIAL_CODE_SEQUENCE.plus(pileSelection.materialCode)
            }
            val postPileRequest = VegaNigeriaPilePostRequest(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                lots = alreadySelected,
                pileDetails = pileSelection
            )
            vm.getPostPile(postPileRequest)
        }
    }

    private fun updateSuccessUI(response: Resource<GenericReqAndResp<VegaNigeriaPileSuccessResponse>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            movetoSuccess(it.data?.data?.pileNo)
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

    private fun movetoSuccess(pileNo: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.pile_created))
        intent.putExtra(AppUtils.SUB_TITLE, "Pile No: ".plus(pileNo))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupAdapter(data: MutableList<VegaCocoaDispatchLots>) {

        binding.rvLotList.setUpAdapter(
            data,
            R.layout.item_pile_management_summary_nigeria,
            ItemPileManagementSummaryNigeriaBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvStLocation.text = it.materialName
                bindItem.tvGradeValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.tvWeightToProcessValue.text =
                    it.editedWeight.plus(" ").plus(it.unitOfMeasure)
                bindItem.ivSelect.setOnClickListener { /*callBack?.editLot(LOT_LIST, true, alreadySelected)*/  activity?.onBackPressed() }
            })
    }

    private fun setupPileCardView(data: VegaCocoaDispatchLots) {
        vegaPileSelectionModel = data
        if (data.batchNumber.length == 6) {
            binding.tvMaterialValue.text = "-"
            binding.tvStageValue.text = "-"
            binding.tvProceesValue.text = "-"
            binding.tvBomValue.text = "-"
        } else {
            binding.tvMaterialValue.text = data.batchNumber
            binding.tvStageValue.text = data.vendor
            binding.tvProceesValue.text = data.weight
            binding.tvBomValue.text = data.materialName
        }
    }
}





