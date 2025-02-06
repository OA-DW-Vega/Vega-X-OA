package com.olam.warehouse.vegax.bagissuenigeriacocoa.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bagissuenigeriacocoa.R
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagIssue
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagIssuePost
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagManagementResp
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagWeighDetails
import com.olam.warehouse.vegax.bagissuenigeriacocoa.databinding.FragmentNigeriaCocoaBagIssueSummaryBinding
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.BAG_ISSUE_DATA
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.BAG_ISSUE_FRAG
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.BATCHNUMBER
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.UOM
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaCocoaBagIssueSummaryFragment : BaseFragment() {

    private var bagIssueData = VegaNigeriaCocoaBagIssue()
    private var postData = mutableListOf<VegaNigeriaCocoaBagIssue>()

    private val vm: VegaNigeriaCocoaBagIssueViewModel by viewModel()
    private lateinit var binding: FragmentNigeriaCocoaBagIssueSummaryBinding
    override val layoutResourceId = R.layout.fragment_nigeria_cocoa_bag_issue_summary
    private var screenType: String? = ""

    companion object {
        fun newInstance(
            bagIssueData: VegaNigeriaCocoaBagIssue
        ) = VegaNigeriaCocoaBagIssueSummaryFragment().putArgs {
            putParcelable(BAG_ISSUE_DATA, bagIssueData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaCocoaBagIssueSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("bagissueindiacoffee/ui/VegaIndiaCoffeeBagIssueSummaryFragment").title("Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        bagIssueData = arguments?.getParcelable(BAG_ISSUE_DATA)!!
        screenType = bagIssueData.screenType
        binding.tvProduct.text = bagIssueData.materialName
        binding.tvSupplier.text = bagIssueData.supplierName

        binding.tvStorageLocation.text =
            bagIssueData.storageLocationCode.plus("-").plus(bagIssueData.storageLocationName)
        binding.tvGatePassNo.text = bagIssueData.gatePassNum
        binding.llGatePass.isVisible = !bagIssueData.gatePassNum.isNullOrEmpty()
        binding.tvBagIssued.text = bagIssueData.bagIssued
        binding.tvBagIssuedBefore.text =
            if (bagIssueData.currentBalance?.isNotEmpty() == true) bagIssueData.currentBalance?.toDouble()?.toInt()
                .toString() else bagIssueData.currentBalance
        if (bagIssueData.currentBalance.isNullOrEmpty()) bagIssueData.currentBalance = "0"
        if (bagIssueData.bagIssued.isNullOrEmpty()) bagIssueData.bagIssued = "0"
        if (screenType.equals(BAG_ISSUE_FRAG)) {
            binding.tvBagIssuedAfter.text =
                ((bagIssueData.currentBalance?.toDouble()?.toLong() ?: 0) - (bagIssueData.bagIssued?.toLong()
                    ?: 0)).toString()
        } else {
            binding.tvQtyBagsIssued.text = getString(R.string.qty_of_bags_returned)
            binding.tvBagIssueBeforeLabel.text = getString(R.string.balance_stock_before_return)
            binding.tvBagIssueAfterLabel.text = getString(R.string.balance_stock_after_return)
            binding.tvBagIssuedAfter.text =
                ((bagIssueData.currentBalance?.toDouble()?.toLong() ?: 0) + (bagIssueData.bagIssued?.toLong()
                    ?: 0)).toString()
        }


        binding.btnConfirm.setOnClickListener { showConfirmDialog() }

        vm.bagIssue.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(if (screenType.equals(BAG_ISSUE_FRAG)) R.string.confirm_bag_issue else R.string.confirm_bag_retn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postBagIssue()
                },
                { dismiss() })
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaNigeriaCocoaBagManagementResp>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> data?.data?.data?.let { it1 -> prepareSuccessData(it1) }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun postBagIssue() {
        postData.clear()
        postData.add(bagIssueData)
        val lotDetails = emptyList<VegaNigeriaCocoaBagIssue>()
        val weighDetails = emptyList<VegaNigeriaCocoaBagWeighDetails>()
        if (AppUtils.isOnline()) {
            vm.postBagIssueData(
                VegaNigeriaCocoaBagIssuePost(
                    lotDetails,
                    weighDetails,
                    BATCHNUMBER,
                    bagIssueData.materialCode,
                    bagIssueData.materialName,
                    bagIssueData.bagIssued,
                    if (bagIssueData.screenType.equals(BAG_ISSUE_FRAG)) true else false,
                    bagIssueData.supplierCode,
                    bagIssueData.supplierName,
                    bagIssueData.storageLocationCode,
                    UOM,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    getPlantDetails(),
                    getCurrentKey()
                )
            )
        }
    }


    private fun prepareSuccessData(data: VegaNigeriaCocoaBagManagementResp) {
        hideLoading()
        moveToSuccessPage(data)
    }

    private fun moveToSuccessPage(data: VegaNigeriaCocoaBagManagementResp) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if(screenType.equals(BAG_ISSUE_FRAG)) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_entry))
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.bag_successfully_issued,bagIssueData.supplierName,data.documentNumber)
            )
        } else {
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_bag_return))
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.bag_successfully_returned,bagIssueData.supplierName,data.documentNumber)
            )
        }
        intent.putExtra(UIUtils.FROM_NIGERIA_COCOA_BAG_MGMT, true)
        intent.putExtra(AppUtils.BAG_MGMT, prepareBagMgmtData(data))


        startActivity(intent)
        requireActivity().finish()
    }

    private fun prepareBagMgmtData(data: VegaNigeriaCocoaBagManagementResp): com.olam.warehouse.master.common.model.VegaNigeriaCocoaBagIssue {
        var bagMgmtData = com.olam.warehouse.master.common.model.VegaNigeriaCocoaBagIssue()
        bagMgmtData.materialName = bagIssueData.materialName
        bagMgmtData.supplierName = bagIssueData.supplierName
        bagMgmtData.storageLocationCode = bagIssueData.storageLocationCode
        bagMgmtData.storageLocationName = bagIssueData.storageLocationName
        bagMgmtData.gatePassNum = bagIssueData.gatePassNum
        bagMgmtData.bagIssued = bagIssueData.bagIssued
        bagMgmtData.currentBalance = bagIssueData.currentBalance
        bagMgmtData.screenType = bagIssueData.screenType
        bagMgmtData.documentNumber = data.documentNumber
        return bagMgmtData
    }
}
