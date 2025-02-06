package com.olam.warehouse.vegax.offloadingindiacoffee.ui

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
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingindiacoffee.R
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.IndiaCoffeeOffloadingQualityPostResponse
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.VegaIndiaCoffeeOffloadingQualityPost
import com.olam.warehouse.vegax.offloadingindiacoffee.databinding.FragmentVegaIndiaCoffeeOffloadingSummaryBinding
import com.olam.warehouse.vegax.offloadingindiacoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class VegaIndiaCoffeeOffloadingBagSummaryFragment : BaseFragment() {

    private var offloadingData = VegaOffloadingTrucks()
    private var offloadingParamList = mutableListOf<VegaQualityParameter>()
    private var qualityPostList = arrayListOf<VegaOffloadingTrucks>()

    private val vm: VegaOffloadingViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeOffloadingSummaryBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_offloading_summary

    companion object {
        fun newInstance(
            offloadingData: VegaOffloadingTrucks,
            offloadingParamList: ArrayList<VegaQualityParameter>
        ) = VegaIndiaCoffeeOffloadingBagSummaryFragment().putArgs {
            putParcelable(OFFLOADING_DATA, offloadingData)
            putParcelableArrayList(OFFLOADING_PARAM_DATA, offloadingParamList)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeOffloadingSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloading/ui/VegaOffloadingBagSummaryFragment").title("Offloading").with(tracker)
        initUI()
    }

    private fun initUI() {
        offloadingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        offloadingParamList =
            arguments?.getParcelableArrayList<VegaQualityParameter>(OFFLOADING_PARAM_DATA)!!

        binding.tvType.text = if (offloadingData.weighBridgeType == PROCURE) SUPPLIER else MTNR
        binding.tvTruckID.text = getString(R.string.truck_id).plus(offloadingData.vehicleNumber)
        binding.tvWeighBridgeId.text = offloadingData.weighBridgeId
        binding.tvBatchNo.text = offloadingData.batchNumber
        binding.tvSupplierName.text =
            offloadingData.supplierCode.plus("-").plus(offloadingData.supplierName)
        binding.tvWeight.text = offloadingData.netWeight.plus(offloadingData.unitsOfMeasure)
        binding.tvBagsCount.text = offloadingData.bagCount

        binding.tvSelectBagType.text = offloadingData.bagType
        binding.tvTotalBags.text = offloadingData.bagCount
        offloadingData.qualityDetails.forEach {
            if (it.nameChar.equals("Z_ACCEPTED_BAGS")) {
                binding.tvAcceptedBags.text = it.qualityParameterValue
            }
            if (it.nameChar.equals("Z_LOT_MERGE")) {
                binding.tvSelectLotToMerge.text = it.qualityParameterValue
            }
            if (it.nameChar.equals("Z_REJECTED_BAGS")) {
                binding.tvDamagedBags.text = it.qualityParameterValue
            }
        }
        binding.tvTotalBags.text = offloadingData.bagCount
        /*binding.tvAcceptedBags.text = offloadingData.acceptedBags
        binding.tvDamagedBags.text = offloadingData.damagedBags
        binding.tvTotalBags.text = offloadingData.totalBags
        binding.tvSelectLotToMerge.text = offloadingData.lotToMerge*/

        if (offloadingData.weighBridgeType == PROCURE) {
            binding.tvdifference.text = SUPPLIER
        } else {
            binding.tvdifference.visibility = View.GONE
            binding.tvSupplierName.visibility = View.GONE
            binding.tvdifference.text = WAREHOUSE
        }
        binding.btnConfirm.setOnClickListener { showConfirmDialog() }

        vm.quality.observe(viewLifecycleOwner, Observer { updateUI(it) })
        when {
            offloadingData.weighBridgeType != PROCURE -> {
                binding.tvBagsCount.visibility = View.GONE
                binding.tvNoofBagsLabel.visibility = View.GONE
            }
            else -> {
                binding.tvBagsCount.visibility = View.VISIBLE
                binding.tvNoofBagsLabel.visibility = View.VISIBLE
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_offloading)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    postQuality(offloadingParamList)
                },
                { dismiss() })
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(it.data?.data?.currentWbid, it.data?.data?.charg)
                            vm.updateDB(it.data?.data?.currentWbid)
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
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

    private fun moveToSuccessPage(currentWbid: String?, charg: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.offloading_saved)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.offloading_saved_offline)
        )
        if (charg?.isNotEmpty()!!)
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.new_lot_id_created).plus(charg))
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id).plus(currentWbid))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun postQuality(offloadingParamList: MutableList<VegaQualityParameter>) {
        val offloadingParams = offloadingParamList.filter { it.qualityParameterValue!!.isNotEmpty() }
        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            offloadingData.qualityDetails = offloadingParams as List<VegaOffloadingParameter>
            offloadingData.plant = getPlantDetails().plantId
            offloadingData.appName = "QC"
            offloadingData.qualityFlag=false
            qualityPostList.add(offloadingData)
            vm.postQualityParams(
                VegaIndiaCoffeeOffloadingQualityPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )
        } else {
            offloadingParamList.forEach {
                it.wbid = offloadingData.weighBridgeId.toString()
                vm.saveQualityData(prepareDOQualityData(it), offloadingData.batchNumber.toString())
            }
            moveToSuccessPage(offloadingData.weighBridgeId, "")

        }

    }




}
