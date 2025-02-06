package com.olam.warehouse.vegax.processing.ui.fgrn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingFgrnResponse
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaFgrnSummaryBinding
import com.olam.warehouse.vegax.processing.databinding.ItemVegaFgrnSummaryBinding
import com.olam.warehouse.vegax.processing.ui.VegaProcessingViewModel
import com.olam.warehouse.vegax.processing.utils.LOT_DETAILS_LIST
import com.olam.warehouse.vegax.processing.utils.PO_DETAILS
import com.olam.warehouse.vegax.processing.utils.prepareFgrnPostRequest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaFgrnSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_fgrn_summary
    private lateinit var binding: FragmentVegaFgrnSummaryBinding
    private var callBack: CallBack? = null
    private var poOrderDetails: VegaFgrnProcessingOrder? = null
    private var dispatchLotsList: List<VegaDispatchLots>? = null
    private val vm: VegaProcessingViewModel by viewModel()

    interface CallBack {
        fun replaceFragment(fragment: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(poOrder: VegaFgrnProcessingOrder, lotDetails: String) = VegaFgrnSummaryFragment().putArgs {
            putParcelable(PO_DETAILS, poOrder)
            putString(LOT_DETAILS_LIST, lotDetails)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaFgrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/fgrn/VegaFgrnSummaryFragment").title("Processing").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.summary, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        vm.postFgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
        binding.btnConfirm.setOnClickListener { movetoSuccess() }
        poOrderDetails = arguments?.getParcelable(PO_DETAILS)!!
        dispatchLotsList =
            Gson().fromJson<List<VegaDispatchLots>>(arguments?.getString(LOT_DETAILS_LIST) ?: "")
        updateDetails(dispatchLotsList!!)
        binding.tvPono.text = poOrderDetails?.processOrderNo

    }

    private fun movetoSuccess() {
        showConfirmDialog()
    }


    private fun updateDetails(poList: List<VegaDispatchLots>) {

        val data = poList as MutableList<VegaDispatchLots>
        binding.rvFgrnSummary.setUpAdapter(
            data,
            R.layout.item_vega_fgrn_summary,
            ItemVegaFgrnSummaryBinding::inflate,
            { item, pos, bindingItem ->
                bindingItem.tvBatchNo.text = item.batchNumber
                bindingItem.tvStorageLocation.text = item.storageLocationCode
                bindingItem.tvOutputMaterial.text = item.materialName
                bindingItem.tvWeight.text = item.weight.plus(" ").plus(item.unitOfMeasure)
                bindingItem.tvNoOfBags.text = item.noOfBags
            },
            {
                //showConfirmDialog()
            })
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_fgrn)
            getMetirialCustomView(this, getString(R.string.confirm), getString(R.string.cancel), {
                val postReq = prepareFgrnPostRequest(poOrderDetails!!, dispatchLotsList!!)
                vm.postFgrn(postReq)
            }, { dismiss() })
        }
    }
    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaProcessingFgrnResponse>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data
                            moveToSuccessPage(success?.get(0))
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


    private fun moveToSuccessPage(processRmin: VegaProcessingFgrnResponse?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.fgrn_saved))
        intent.putExtra(AppUtils.SUB_TITLE, processRmin?.messages?.get(0)?.message)
        startActivity(intent)
        requireActivity().finish()
    }

}
