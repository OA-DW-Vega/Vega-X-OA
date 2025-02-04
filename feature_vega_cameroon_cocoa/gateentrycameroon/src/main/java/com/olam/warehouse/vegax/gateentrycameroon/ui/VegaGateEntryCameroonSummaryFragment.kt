package com.olam.warehouse.vegax.gateentrycameroon.ui

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
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
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
import com.olam.warehouse.vegax.gateentrycameroon.R
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonPost
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonResponse
import com.olam.warehouse.vegax.gateentrycameroon.databinding.FragmentGateEntryCameroonSummaryBinding
import com.olam.warehouse.vegax.gateentrycameroon.utils.GATE_ENTRY_DATA
import com.olam.warehouse.vegax.gateentrycameroon.utils.GATE_ENTRY_PLANT_DETAILS
import com.olam.warehouse.vegax.gateentrycameroon.utils.PROCURE
import com.olam.warehouse.vegax.gateentrycameroon.utils.createRandomInteger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 3/9/2020.
 */
class VegaGateEntryCameroonSummaryFragment : BaseFragment() {

    private var gateEntryData = VegaGateEntry()
    private var postData = mutableListOf<VegaGateEntry>()
    private var plantDetails = Plant()

    private val vm: VegaGateEntryCameroonViewModel by viewModel()
    private lateinit var binding: FragmentGateEntryCameroonSummaryBinding
    override val layoutResourceId = R.layout.fragment_gate_entry_cameroon_summary

    companion object {
        fun newInstance(
            gateEntryData: VegaGateEntry,
            plantDetails: Plant
        ) = VegaGateEntryCameroonSummaryFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
            putParcelable(GATE_ENTRY_PLANT_DETAILS, plantDetails)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGateEntryCameroonSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentrycameroon/ui/VegaGateEntryCameroonSummaryFragment")
            .title("Vega_Cameroon/Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        plantDetails = arguments?.getParcelable(GATE_ENTRY_PLANT_DETAILS)!!
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
        binding.tvPlant.text = gateEntryData.plantId
        binding.tvWeight.text = gateEntryData.approximateWeight.plus(" KG")
        binding.tvMtntNumber.text = gateEntryData.mtnCode
        binding.tvNoOfBags.text = gateEntryData.tempBagCount
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

    private fun updateUI(data: Resource<GenericReqAndResp<VegaGateEntryCameroonResponse>>?) {
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
        var sampleID = generateSampleID()
        gateEntryData.challan = sampleID.toString()
        postData.add(gateEntryData)
        postData.forEachIndexed { index, vegaReceiving -> vegaReceiving.item = index.inc().toString() }
        if (AppUtils.isOnline()) {
            vm.postGateEntryData(VegaGateEntryCameroonPost(getCurrentKey(), plantDetails, postData))
        }
    }

    private fun generateSampleID(): Long {
        var sampleID = createRandomInteger(1000000000, 9999999999L, Random)
        return sampleID
    }

    private fun prepareSuccessData(wbId: String?) {
        hideLoading()
        moveToSuccessPage(wbId)
    }

    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_entry))

        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.weigh_bridge_id_is).plus(" ")
                .plus(wbId)
        )

        val lotlist = ArrayList<VegaCoffeeSalesLots>()
        lotlist.add(
            VegaCoffeeSalesLots(
                "",
                gateEntryData.challan.toString(),
                gateEntryData.materialCode.toString(),
                gateEntryData.materialName.toString(),
                "",
                "",
                "",
                "",
                "",
                gateEntryData.unitsOfMeasure,
                "",
                gateEntryData.approximateWeight
            )
        )

        startActivity(intent)
        requireActivity().finish()
    }
}
