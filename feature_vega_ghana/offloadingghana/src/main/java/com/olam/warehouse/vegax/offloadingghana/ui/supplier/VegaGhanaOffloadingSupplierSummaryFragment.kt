package com.olam.warehouse.vegax.offloadingghana.ui.supplier

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
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.databinding.FragmentVegaOffloadingGhanaSupplierSummaryBinding
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingghana.utils.OFFLOADING_DATA
import com.olam.warehouse.vegax.offloadingghana.utils.OFFLOADING_POST_BAG_DATA
import com.olam.warehouse.vegax.offloadingghana.utils.OFFLOADING_POST_DATA
import com.olam.warehouse.vegax.offloadingghana.utils.getTmpId
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 25/6/2020.
 */
class VegaGhanaOffloadingSupplierSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_vega_offloading_ghana_supplier_summary
    private val vm: VegaGhanaOffloadingViewModel by viewModel()
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private lateinit var binding: FragmentVegaOffloadingGhanaSupplierSummaryBinding
    private var callBack: CallBack? = null
    var isQuality = false
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

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            mReceiving: ArrayList<VegaReceiving>,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        ) = VegaGhanaOffloadingSupplierSummaryFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_DATA, mReceiving)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaOffloadingGhanaSupplierSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingsesame/ui/supplier/VegaSesameOffloadingSupplierSummaryFragment")
            .title("Ecuador Offloading").with(tracker)
        initUI()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        mReceiving = arguments?.getParcelableArrayList<VegaReceiving>(OFFLOADING_POST_DATA)!!
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(OFFLOADING_POST_BAG_DATA)!!
        binding.tvLotValue.text = receivingData.materialName
        if (receivingData.purchaseDocNum.isNullOrEmpty()) {
            binding.tvProcurementTypeValue.text = getString(R.string.spot_purchase)
            binding.tvPONumber.gone()
            binding.tvPONumberValue.gone()
        } else {
            binding.tvProcurementTypeValue.text = getString(R.string.fixed_purchase)
            binding.tvPONumberValue.text = receivingData.purchaseDocNum
        }
        binding.tvProductValue.text = receivingData.product
        binding.tvSupplierValue.text = receivingData.supplierName
        binding.tvReceivingWHValue.text = receivingData.receivingWH
//        binding.tvWRNoValue.text = receivingData.challan
        binding.tvTruckNoValue.text = receivingData.truckNo
        binding.tvDriverNameValue.text = receivingData.driverName
        binding.tvDrivePhoneNoValue.text = receivingData.phoneNo

        binding.tvGrossValue.text = receivingData.grossWeight.plus(" ").plus("MT")
        binding.tvNoBagsValue.text = receivingData.bagCount
        binding.tvBagsTareValue.text = receivingData.tareWeight.plus(" ").plus("MT")
        binding.tvNetWeightValue.text = receivingData.netWeight.plus(" ").plus("MT")
        updateTotalWeightValues()
        binding.btProceed.setOnClickListener { showConfirmDialog() }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })

        val roleData =
            Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                .filter { key ->
                    key.roleKey.equals(
                        PreferenceHelper.get(
                            Constants.CURRENT_KEY,
                            ""
                        )
                    )
                }
        roleData.forEach { rol -> if (rol.roleName.equals(UserRoles.QUALITY)) isQuality = true }
    }

    private fun updateTotalWeightValues() {
        binding.clNet.tvGrossWeightValue.text = binding.tvGrossValue.text
        binding.clNet.tvTareWeightValue.text = binding.tvBagsTareValue.text
        binding.clNet.tvNetWeightValue.text = binding.tvNetWeightValue.text
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(
                    it.data?.data?.wbId!!,
                    true,
                    it.data?.message.toString(),
                    it.data?.data?.encodedImageContent
                )
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    prepareErrorData(receivingData.weighBridgeId, false, it.error.toString())
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_offloading)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    postReceiving()
                },
                { dismiss() })
        }
    }
    private fun postReceiving() {
        if (AppUtils.isOnline()) {
            vm.postEcuadorOffloadingData(VegaReceivingPost(getCurrentKey(), getPlantDetails(), mReceiving))
        } else {
            var weighbridgedetails: VegaReceivingResponse = VegaReceivingResponse()
            weighbridgedetails.wbId =
                if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId
            receivingData.isOffline = true
            prepareSuccessData(
                weighbridgedetails.wbId!!,
                false,
                "Data cached offline",
                ""
            )
        }
    }

    private fun prepareSuccessData(
        wbid: String,
        syncStatus: Boolean,
        msg: String,
        encodedImageContent: String?
    ) {
        hideLoading()
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.tmpWbId = if (!syncStatus) wbid else ""
        receivingData.isSynced = syncStatus
        receivingData.direction = "IN"
        receivingData.weighBridgeType = "PROCURE"
        receivingData.syncStatusMsg = msg
        if (!syncStatus) {
            receivingData.item = "00001"
            receivingData.weighBridgeId = wbid.toString()
            vm.saveOffloading(receivingData)
            bagList.forEach { it.tmpWbId = wbid }
            vm.saveReceivingLineItems(bagList)
        }

        moveToSuccessPage(wbid, encodedImageContent, receivingData)
    }

    private fun prepareErrorData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
        receivingData.status = Status.SYNC_ERROR
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = msg
        vm.saveOffloading(receivingData)
        /*if (!syncStatus) {
            mReceiving.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(mReceiving)
        }*/
    }

    private fun moveToSuccessPage(
        wbId: String?,
        encodedImageContent: String?,
        weighbridgedetails: VegaReceiving
    ) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            vm.clearBagDetails()
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_offloading)
            )
            intent.putExtra(UIUtils.FROM_GHANA, true)
            intent.putExtra("Weighbridgetype", "Supplier")
            intent.putExtra(UIUtils.RECEIVING_DATA, weighbridgedetails)

//            intent.putExtra(
//                AppUtils.PRINT_ENABLE,
//                (encodedImageContent != null && encodedImageContent != "")
//            )
//            val tallyKeys = ArrayList<String>()
//            tallyKeys.add(encodedImageContent ?: "")
//            intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
            if (receivingData.weighBridgeId.contains("TMP")) {
                vm.updateWBToQualityAndGrnTable(receivingData.weighBridgeId, wbId.toString())
            }
        } else {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_offloading_offline)
            )
            intent.putExtra(UIUtils.FROM_GHANA, true)
            intent.putExtra("Weighbridgetype", "Supplier")
            intent.putExtra(UIUtils.RECEIVING_DATA, weighbridgedetails)
        }
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(" ").plus(wbId))
        startActivity(intent)
        requireActivity().finish()
    }

}
