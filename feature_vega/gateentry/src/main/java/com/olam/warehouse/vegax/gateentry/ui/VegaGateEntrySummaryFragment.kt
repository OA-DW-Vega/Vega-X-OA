package com.olam.warehouse.vegax.gateentry.ui

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
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentry.R
import com.olam.warehouse.vegax.gateentry.data.domain.model.VegaGateEntryPost
import com.olam.warehouse.vegax.gateentry.data.domain.model.VegaGateEntryResponse
import com.olam.warehouse.vegax.gateentry.databinding.FragmentGateEntrySummaryBinding
import com.olam.warehouse.vegax.gateentry.utils.GATE_ENTRY_DATA
import com.olam.warehouse.vegax.gateentry.utils.PROCURE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


/**
 * Created by Baskaran Kannan on 3/9/2020.
 */
class VegaGateEntrySummaryFragment : BaseFragment() {

    private var gateEntryData = VegaGateEntry()
    private var postData = mutableListOf<VegaGateEntry>()

    private val vm: VegaGateEntryViewModel by viewModel()
    private lateinit var binding: FragmentGateEntrySummaryBinding
    override val layoutResourceId = R.layout.fragment_gate_entry_summary

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaGateEntrySummaryFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGateEntrySummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentry/ui/VegaGateEntrySummaryFragment").title("Gate Entry").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        if (gateEntryData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
        } else {

            binding.llSupplier.gone()
            binding.llMtnr.visible()
        }

        binding.tvProduct.text = gateEntryData.materialName
        binding.tvSupplier.text = gateEntryData.supplierName
        binding.tvSupplierZone.text = gateEntryData.supplierZone
        binding.tvObdNumber.text = gateEntryData.delivery
        binding.tvDispatchWarehouse.text = gateEntryData.supplierName
        binding.tvTruckNo.text = gateEntryData.vehicleNumber
        binding.tvDriverName.text = gateEntryData.driverName
        binding.tvPhoneNo.text = gateEntryData.contactNumber
        binding.tvReceivingLocation.text =
            gateEntryData.storageLocationCode.plus("-").plus(gateEntryData.storageLocationName)
        binding.tvWeight.text = gateEntryData.approximateWeight
        binding.tvDate.text = gateEntryData.erdat?.let { it1 ->
            DateUtils.getUTCDateTime(
                it1,
                App.getAppContext()
            )
        }

        binding.btnConfirm.setOnClickListener { showConfirmDialog() }

        vm.gateEntry.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_gate_entry)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    postGateEntry()
                },
                { dismiss() })
        }
    }


    private fun updateUI(data: Resource<GenericReqAndResp<VegaGateEntryResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId)
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun postGateEntry() {
        postData.clear()
        postData.add(gateEntryData)
        postData.forEachIndexed { index, vegaReceiving -> vegaReceiving.item = index.inc().toString() }
        if (AppUtils.isOnline()) {
            vm.postGateEntryData(VegaGateEntryPost(getCurrentKey(), getPlantDetails(), postData))
        }
    }

    private fun prepareSuccessData(wbId: String?) {
        hideLoading()
        moveToSuccessPage(wbId)
    }

    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_gate_entry))
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
        startActivity(intent)
        requireActivity().finish()
    }
}
