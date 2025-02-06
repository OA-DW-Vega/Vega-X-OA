package com.olam.warehouse.vegax.qualityindiacoffee.ui

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
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.utils.prepareVegaQualityList
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindiacoffee.R
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPost
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPostResponse
import com.olam.warehouse.vegax.qualityindiacoffee.databinding.FragmentVegaIndiaCoffeeQualitySummaryBinding
import com.olam.warehouse.vegax.qualityindiacoffee.databinding.ItemVegaIndiaCoffeeQualitySummaryBinding
import com.olam.warehouse.vegax.qualityindiacoffee.utils.SUMMARY_DATA
import com.olam.warehouse.vegax.qualityindiacoffee.utils.SUMMARY_PARAM_DATA
import com.olam.warehouse.vegax.qualityindiacoffee.utils.getColor
import com.olam.warehouse.vegax.qualityindiacoffee.utils.prepareVegaQualityData
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class VegaIndiaCoffeeQualitySummaryFragment : BaseFragment() {

    private var offloadingData = VegaQualityWBDetails()
    private var offloadingParamList = mutableListOf<VegaQualityParameter>()
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()

    private val vm: VegaIndiaCoffeeQualityViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeQualitySummaryBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_quality_summary

    companion object {
        fun newInstance(
            offloadingData: VegaQualityWBDetails,
            offloadingParamList: ArrayList<VegaQualityParameter?>
        ) = VegaIndiaCoffeeQualitySummaryFragment().putArgs {
            putParcelable(SUMMARY_DATA, offloadingData)
            putParcelableArrayList(SUMMARY_PARAM_DATA, offloadingParamList)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeQualitySummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/VegaIndiaCoffeeQualitySummaryFragment").title("Summary").with(tracker)
        initUI()
    }

    private fun initUI() {
        offloadingData = arguments?.getParcelable(SUMMARY_DATA)!!
        offloadingParamList = arguments?.getParcelableArrayList<VegaQualityParameter>(SUMMARY_PARAM_DATA)!!
        setUpAdapter(offloadingParamList)

        //binding.tvType.text = if (offloadingData.weighBridgeType == PROCURE) SUPPLIER else MTNR
        binding.tvBatchNo.text = getString(R.string.batch_no).plus(" : ").plus(offloadingData.batchNumber)
        binding.tvWBID.text = getString(R.string.weigh_bridge).plus(" : ").plus(offloadingData.weighBridgeId)
        binding.btnConfirm.setOnClickListener { showConfirmDialog() }

        vm.quality.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_quality)
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

    private fun updateUI(response: Resource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>) {
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
            getString(R.string.quality_success)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.quality_success_offline)
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
            offloadingData.qualityDetails = prepareVegaQualityList(
                offloadingParams,
                arrayListOf()
            )
            offloadingData.plant = getPlantDetails().plantId
            offloadingData.appName = "QC"
            offloadingData.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")
            qualityPostList.add(offloadingData)

        /*    offloadingData?.batchNumber = offloadingData.batchNumber
            offloadingData?.finalApproval = offloadingData.finalApproval
            qualityPostList.clear()
            offloadingData?.let {
                it.finalApproval = "R"
                qualityPostList.add(it)
            }*/

            vm.postQualityParams(
                VegaIndiaCoffeeQualityPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )
        } else {
            offloadingParamList.forEach {
                it.wbid = offloadingData.weighBridgeId.toString()
                vm.saveQualityData(prepareVegaQualityData(it), offloadingData.batchNumber.toString())
            }
            moveToSuccessPage(offloadingData.weighBridgeId, "")

        }

    }


    private fun setUpAdapter(data: MutableList<VegaQualityParameter>) {
        var list = mutableListOf<VegaQualityParameter>()
        data.let { list = it }
        binding.rvBagDetailSummary.setUpAdapter(
            list,
            R.layout.item_vega_india_coffee_quality_summary,
            ItemVegaIndiaCoffeeQualitySummaryBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    bindItem.linearRow.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
                }
                bindItem.tvItemName.text =
                    if (!it.qualityParamLabel.isNullOrEmpty()) it.qualityParamLabel else it.descrChar
                bindItem.tvItemValue.text =
                    if (!it.qualityParameterValue.isNullOrEmpty()) it.qualityParameterValue else "-"
            })
    }

}
