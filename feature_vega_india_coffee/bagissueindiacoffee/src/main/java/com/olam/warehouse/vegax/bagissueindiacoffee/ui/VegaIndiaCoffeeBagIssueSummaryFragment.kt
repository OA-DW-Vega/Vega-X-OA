package com.olam.warehouse.vegax.bagissueindiacoffee.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bagissueindiacoffee.R
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssue
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssuePost
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssueResponse
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagWeighDetails
import com.olam.warehouse.vegax.bagissueindiacoffee.databinding.FragmentIndiaCoffeeBagIssueSummaryBinding
import com.olam.warehouse.vegax.bagissueindiacoffee.utils.BAG_ISSUE_DATA
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 3/9/2020.
 */
class VegaIndiaCoffeeBagIssueSummaryFragment : BaseFragment() {

    private var bagIssueData = VegaIndiaCoffeeBagIssue()
    private var postData = mutableListOf<VegaIndiaCoffeeBagIssue>()

    private val vm: VegaIndiaCoffeeBagIssueViewModel by viewModel()
    private lateinit var binding: FragmentIndiaCoffeeBagIssueSummaryBinding
    override val layoutResourceId = R.layout.fragment_india_coffee_bag_issue_summary

    companion object {
        fun newInstance(
            bagIssueData: VegaIndiaCoffeeBagIssue
        ) = VegaIndiaCoffeeBagIssueSummaryFragment().putArgs {
            putParcelable(BAG_ISSUE_DATA, bagIssueData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentIndiaCoffeeBagIssueSummaryBinding.inflate(layoutInflater)
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


        binding.tvProduct.text = bagIssueData.materialName
        binding.tvSupplier.text = bagIssueData.supplierName
        
        binding.tvStorageLocation.text =
            bagIssueData.storageLocationCode.plus("-").plus(bagIssueData.storageLocationName)
        binding.tvGatePassNo.text = bagIssueData.gatePassNum
        binding.tvBagIssued.text = bagIssueData.bagIssued
        binding.tvBagIssuedBefore.text = bagIssueData.currentBalance
        if(bagIssueData.currentBalance.isNullOrEmpty()) bagIssueData.currentBalance = "0"
        if(bagIssueData.bagIssued.isNullOrEmpty()) bagIssueData.bagIssued = "0"
        binding.tvBagIssuedAfter.text = ((bagIssueData.currentBalance?.toDouble()?.toLong() ?: 0) - (bagIssueData.bagIssued?.toLong()
            ?: 0)).toString()


        binding.btnConfirm.setOnClickListener { showConfirmDialog() }

        vm.bagIssue.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_bag_issue)
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

    private fun updateUI(data: Resource<GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData()
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun postBagIssue() {
        postData.clear()
        postData.add(bagIssueData)
        val lotDetails = emptyList<VegaIndiaCoffeeBagIssue>()
        val weighDetails = emptyList<VegaIndiaCoffeeBagWeighDetails>()
        if (AppUtils.isOnline()) {
           vm.postBagIssueData(VegaIndiaCoffeeBagIssuePost(
               lotDetails,
               weighDetails,
               "",
               bagIssueData.materialCode,
               bagIssueData.materialName,
               bagIssueData.bagIssued,
               bagIssueData.supplierCode,
               bagIssueData.supplierName,
               bagIssueData.storageLocationCode,
               bagIssueData.unitsOfMeasure,
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


    private fun prepareSuccessData() {
        hideLoading()
        moveToSuccessPage()
    }

    private fun moveToSuccessPage() {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_entry))

        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.bag_successfully_issued)
        )

        startActivity(intent)
        requireActivity().finish()
    }
}
