package com.olam.warehouse.vegax.weighment.ui.truckin.mtnt

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighment.R
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeMtntPost
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeMtntResponse
import com.olam.warehouse.vegax.weighment.databinding.FragmentVegaIndiaCoffeeTruckinMtntSummaryBinding
import com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeMtntViewModel
import com.olam.warehouse.vegax.weighment.utils.DIRECTIONIN
import com.olam.warehouse.vegax.weighment.utils.MTNT_DATA
import com.olam.warehouse.vegax.weighment.utils.getTmpId
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeTruckInMtntSummaryFragments : BaseFragment() {

    private var mtntData = VegaMtnt()
    private var mMtntList = mutableListOf<VegaMtnt>()

    private val vm: VegaIndiaCoffeeMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeTruckinMtntSummaryBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_truckin_mtnt_summary

    companion object {
        fun newInstance(mtntData: VegaMtnt) = VegaIndiaCoffeeTruckInMtntSummaryFragments().putArgs {
            putParcelable(MTNT_DATA, mtntData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeTruckinMtntSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckin/mtnt/VegaTruckInMtntSummaryFragments").title("Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        mtntData = arguments?.getParcelable(MTNT_DATA)!!

        enableProceedBtn(mtntData.status)
        binding.tvTruckNo.text = mtntData.vehicleNumber
        binding.tvDriverName.text = mtntData.driverName
        binding.tvPhoneNo.text = mtntData.contactNumber
        binding.tvProduct.text = mtntData.materialName
        binding.tvTransVendor.text = mtntData.transportVendorName
        binding.tvDestinationWh.text = mtntData.recStorageLocationCode
        binding.tvDispatchWh.text = mtntData.storageLocationCode

        binding.tvTruckWeight.text = mtntData.tareWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        binding.tvDispatchWeight.text = mtntData.approximateWeight.plus(" ").plus(mtntData.unitsOfMeasure)

        binding.btnConfirm.setOnClickListener { showConfirmDialog() }

        vm.indiaCoffeeMtnt.observe(viewLifecycleOwner, Observer { updateUI(it) })

    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaIndiaCoffeeMtntResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(
                    it.data?.data?.wbId,
                    it.data?.data?.delivery,
                    true,
                    it.data?.message.toString()
                )
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    prepareErrorData( false, it.error.toString())
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    //requireContext().toast("${it.error}")
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_truck_in_message)
            getMetirialCustomView(this, getString(R.string.confirm), getString(R.string.cancel), {
                postReceiving()
            }, { dismiss() })
        }
    }

    private fun postReceiving() {
        mMtntList.clear()
        mMtntList.add(mtntData)
        mMtntList.forEachIndexed { index, vegaMtnt ->
            vegaMtnt.item = index.inc().toString()
        }
        if (AppUtils.isOnline()) {
            vm.postMtntData(VegaIndiaCoffeeMtntPost(getCurrentKey(), getPlantDetails(), mMtntList))
        } else {
            prepareSuccessData(
                if (mtntData.tmpWbId.isEmpty()) getTmpId() else mtntData.tmpWbId,
                mtntData.delivery,
                false, "Cached offline"
            )
        }
    }

    private fun prepareSuccessData(wbId: String?, delivery: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
        mtntData.weighBridgeId = if (syncStatus) wbId ?: "" else ""
//        mtntData.tmpWbId = wbId ?: ""
        mtntData.truckDirection = DIRECTIONIN
        mtntData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        mtntData.isSynced = syncStatus
        mtntData.syncStatusMsg = msg
        vm.saveMtnt(mtntData)
        /* if (!syncStatus) {
             mMtntList.forEach { it.tmpWbId = wbId ?: "" }
             vm.saveMtntLineItems(mMtntList)
         }*/
        moveToSuccessPage(wbId, delivery)
    }

    private fun prepareErrorData(syncStatus: Boolean, msg: String) {
        hideLoading()
        mtntData.truckDirection = DIRECTIONIN
        mtntData.status = Status.SYNC_ERROR
        mtntData.isSynced = syncStatus
        mtntData.syncStatusMsg = msg
        vm.saveMtnt(mtntData)
    }

    private fun moveToSuccessPage(wbId: String?, delivery: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_in)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_in_offline)
        )
        if (!delivery.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.weigh_bridge_id_is).plus(wbId).plus("\n").plus(
                    getString(R.string.delivery_id_is).plus(delivery)
                )
            )
        else intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun enableProceedBtn(status: Status) {

        when (status) {
            Status.SYNC_PENDING -> {
                binding.btnConfirm.isEnabled = true
                ViewCompat.setBackgroundTintList(
                    binding.btnConfirm,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
            }
            Status.MTNT_COMPLETED -> {
                binding.btnConfirm.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnConfirm,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
            }
        }
    }
}
