package com.olam.warehouse.vegax.sweepingcocoa.ui.summary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.sweepingcocoa.R
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingPost
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingResponse
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingSummary
import com.olam.warehouse.vegax.sweepingcocoa.databinding.FragmentPhysicalInventorySummaryBinding
import com.olam.warehouse.vegax.sweepingcocoa.ui.CallBack
import com.olam.warehouse.vegax.sweepingcocoa.ui.VegaCocoaSweepingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaSweepingSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_physical_inventory_summary
    private val vm: VegaCocoaSweepingViewModel by viewModel()
    private lateinit var binding: FragmentPhysicalInventorySummaryBinding
    private var callBack: CallBack? = null
    private var summary: VegaSweepingSummary? = null
    private var netWeight: Double = 0.0

    companion object {
        fun newInstance(bundle: Bundle) = VegaSweepingSummaryFragment().putArgs {
            putBundle(Constants.LOT_INFO, bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhysicalInventorySummaryBinding.inflate(inflater)
        val bundle = arguments?.getBundle(Constants.LOT_INFO)
        summary = bundle?.getParcelable(Constants.LOT_INFO)
        initUI()
        updateWeightText()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("sweepingcocoa/ui/summary/VegaSweepingSummaryFragment")
            .title("sweeping Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        binding.tvMaterialValue.text = summary?.lot?.materialName
        binding.tvStockValue.text = summary?.lot?.weight.plus(" ").plus(summary?.lot?.unitOfMeasure)
        binding.tvStorageValue.text = summary?.lot?.storageLocationCode
        binding.tvGrossValue.text = summary?.grossWeight
        binding.tvNoPalletValue.text = summary?.palletCount
        binding.tvNoBagsValue.text = summary?.noOfBags
        binding.tvPalletTareValue.text = summary?.avgPalletCount.plus(" KG")
        binding.tvBagsTareValue.text = summary?.tareWeight.plus(" KG")
        binding.tvNetWeightValue.text = summary?.netWeight
        binding.tvLotValue.text = summary?.lot?.batchNumber
        updateTotalWeightValues()
        binding.btProceed.setOnClickListener { showConformationDialog() }

        vm.sweepingPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaSweepingResponse>>?) {
        response?.let {
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    moveToSuccessPage(response.data?.data?.batchNumber, response.data?.data?.msg.toString())
                    updateDataToDB(response.data?.data?.msg.toString(), 4)
                    // vm.deleteTruckAndLotsData(summaryObj?.purchaseOrder?.weighBridgeId ?: "")
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), response.error.toString())
                    updateDataToDB(response.error.toString(), 3)
                }
            }
        }
    }

    private fun updateDataToDB(msg: String, status: Int) {
        vm.updateDataToDB(msg, status, summary?.lot?.batchNumber.toString())
    }

    private fun moveToSuccessPage(batchNumber: String?, message: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, message)
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.lot_no).plus(batchNumber))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_sweeping)
            UIUtils.getMetirialCustomView(
                this,
                view.context.getString(R.string.proceed),
                view.context.getString(R.string.cancel),
                {
                    postSweeping()
                },
                { dismiss() })
        }
    }

    private fun postSweeping() {
        summary?.lot?.weight = calculateTotalWeight()
        summary?.lot?.let { VegaSweepingPost(getCurrentKey(), it) }?.let { vm.postSweeping(it) }
    }

    private fun calculateNetWeight(): String {
        val palletWeight = summary?.palletCount?.toInt() ?: 1.times(summary?.avgPalletCount?.toInt() ?: 1)
        val bagTareWeight = summary?.noOfBags?.toInt() ?: 1.times(summary?.tareWeight?.toInt() ?: 1)
        netWeight = summary?.grossWeight?.replace("KG", "")?.toDouble() ?: 0.0.minus((palletWeight.plus(bagTareWeight)))
        return netWeight.toString()
    }

    private fun calculateTotalWeight(): String {
        /*return summary?.lot?.weight?.toDouble()?.plus(summary?.netWeight?.replace("KG", "")?.trim()?.toDouble()!!)
            ?.formatThreeDigits()
            .toString()*/
        return when (summary?.lot?.unitOfMeasure) {
            "MT" -> summary?.lot?.weight?.toDouble()?.plus(
                summary?.netWeight?.replace(
                    "KG",
                    ""
                )?.trim()?.toDouble()?.div(1000) ?: 0.0
            )?.formatThreeDigits().toString()
            "KG" -> summary?.lot?.weight?.toDouble()?.plus(
                summary?.netWeight?.replace("KG", "")?.trim()?.toDouble()
                    ?: 0.0
            )?.formatThreeDigits().toString()
            else -> summary?.lot?.weight?.toDouble()?.plus(
                summary?.netWeight?.replace(
                    "KG",
                    ""
                )?.trim()?.toDouble()?.div(1000) ?: 0.0
            )?.formatThreeDigits().toString()
        }
    }

    private fun updateWeightText() {
        binding.clNet.tvStockTotal.text = getString(R.string.stock)
        binding.clNet.tvNetWeightTotal.text = getString(R.string.net_weight)
        binding.clNet.tvTotalWeight.text = getString(R.string.total_weight)
        binding.clNet.plus.text = "  +  "
    }

    private fun updateTotalWeightValues() {
        var netWeight = 0.0
        var toatalWeight = 0.0
        var uom: String = "MT"
        binding.clNet.tvStockTotalValue.text = summary?.lot?.weight.plus(" ").plus(summary?.lot?.unitOfMeasure)
        when (summary?.lot?.unitOfMeasure) {
            "MT" -> {
                uom = "MT"
                netWeight = summary?.netWeight?.replace("KG", "")?.trim()?.toDouble()?.div(1000) ?: 0.0
            }
            "KG" -> {
                uom = "KG"
                netWeight = summary?.netWeight?.replace("KG", "")?.trim()?.toDouble() ?: 0.0
            }
        }
        binding.clNet.tvNetWeightTotalValue.text = netWeight.toString().plus(" ").plus(uom)
        binding.clNet.tvTotalWeightValue.text = calculateTotalWeight().plus(uom)
    }
}
