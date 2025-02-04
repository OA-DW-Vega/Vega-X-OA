package com.olam.warehouse.vegax.weighment.ui.truckin

import android.content.Context
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
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.showErrorDialog
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighment.R
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeReceivingPost
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeReceivingResponse
import com.olam.warehouse.vegax.weighment.databinding.FragmentVegaIndiaCoffeeTruckinSummaryBinding
import com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeReceivingViewModel
import com.olam.warehouse.vegax.weighment.utils.DIRECTIONIN
import com.olam.warehouse.vegax.weighment.utils.PROCURE
import com.olam.warehouse.vegax.weighment.utils.RECEIVING_DATA
import com.olam.warehouse.vegax.weighment.utils.getTmpId
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeTruckInSummaryFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var receivingData = VegaReceiving()
    private var postData = mutableListOf<VegaReceiving>()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: VegaIndiaCoffeeReceivingViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeTruckinSummaryBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_truckin_summary

    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaIndiaCoffeeTruckInSummaryFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeTruckinSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckin/VegaTruckInSummaryFragment").title("Receiving").with(tracker)
        initUI()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        if (receivingData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
            binding.llTransportVendor.gone()
        } else {
            binding.llSupplier.gone()
            binding.llMtnr.visible()
            binding.llTransportVendor.visible()
        }
        enableProceedBtn(receivingData.status)
        binding.tvProduct.text = receivingData.materialName
        binding.tvSupplier.text = receivingData.supplierCode.plus("-").plus(receivingData.supplierName)
        binding.tvLocation.text = receivingData.storageLocationCode.plus("-").plus(receivingData.storageLocationName)
        binding.tvWarehouse.text = receivingData.supplierName
        binding.tvSto.text = receivingData.delivery ?: receivingData.mtnCode
        binding.tvTruckNo.text = receivingData.vehicleNumber
        binding.tvDriverName.text = receivingData.driverName
        binding.tvTruckWeight.text = receivingData.grossWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        binding.tvPhoneNo.text = receivingData.contactNumber
        binding.tvTransVendor.text = receivingData.transportVendorName
        binding.tvBagType.text = receivingData.bagType
        binding.btnConfirm.setOnClickListener { showConfirmDialog() }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId, true, it.data?.message.toString())
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialog(requireContext(), "${it.error}")
                    prepareErrorData(false, it.error.toString())
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_truck_in_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postReceiving()
                },
                { dismiss() })
        }
    }

    private fun postReceiving() {
        postData.clear()
        postData.add(receivingData)
        postData.forEachIndexed { index, vegaReceiving -> vegaReceiving.item = index.inc().toString() }
        if (AppUtils.isOnline()) {
            vm.postReceivingData(VegaIndiaCoffeeReceivingPost(getCurrentKey(), getPlantDetails(), postData))
        } else {
            // prepareSuccessData(getTmpId(), false)
            prepareSuccessData(
                if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId,
                false,
                "Data cached offline"
            )
        }
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
        receivingData.weighBridgeId = if (syncStatus) wbId ?: "" else ""
//        receivingData.tmpWbId = wbId ?: ""
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = msg
        receivingData.truckDirection = DIRECTIONIN
        vm.saveReceiving(receivingData)
        /*if (!syncStatus) {
            postData.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(postData)
        }*/
        moveToSuccessPage(wbId)
    }

    private fun prepareErrorData(syncStatus: Boolean, msg: String) {
//        receivingData.weighBridgeId = wbId ?: ""
//        receivingData.tmpWbId = wbId ?: ""
        receivingData.status = Status.SYNC_ERROR
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = msg
        receivingData.truckDirection = DIRECTIONIN
        vm.saveReceiving(receivingData)
        /*if (!syncStatus) {
            postData.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(postData)
        }*/
    }

    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_in)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_in_offline)
        )
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
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
            Status.RECEVING_COMPLETED -> {
                binding.btnConfirm.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnConfirm,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
            }
        }
    }
}
