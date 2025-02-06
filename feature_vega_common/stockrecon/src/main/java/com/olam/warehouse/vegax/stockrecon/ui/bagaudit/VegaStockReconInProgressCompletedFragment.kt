package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.StorageLocation
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.model.VegaStockReconBagDetails
import com.olam.warehouse.master.vega.model.VegaStockReconIdDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconCreateReconIdReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconUpdateStatus
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentInprogressCompletedReconBinding
import com.olam.warehouse.vegax.stockrecon.databinding.ItemReconInprogressCompletedListBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.AUDIT_TYPE
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.PLANT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.RECON_ID_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.STATUS_CANCEL
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_ALL_AUDIT_LIST
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_ADD_LOT
import com.olam.warehouse.vegax.stockrecon.utils.STORAGE_LOCATION_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.getColor
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaStockReconInProgressCompletedFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_inprogress_completed_recon
    private lateinit var binding: FragmentInprogressCompletedReconBinding
    private val vm: VegaStockReconViewModel by viewModel()

    private var callBack: VegaStockCallbackListener? = null
    private var plant = Plant();
    private var storageLocation = StorageLocation()
    private var auditType: String = ""
    var inProgressReconList: List<VegaStockReconIdDetails> = emptyList()
    var completedReconList: List<VegaStockReconIdDetails> = emptyList()


    companion object {
        fun newInstance(bundle: Bundle) = VegaStockReconInProgressCompletedFragment().putArgs {
            putBundle(BUNDLE_DATA, bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentInprogressCompletedReconBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }


    private fun initUI() {
        getArgumentsData()
        clickListener()
        observer()
    }


    private fun clickListener() {
        binding.rlInprogress.setOnClickListener {
            /*on clicking inProgress tab, loading inProgress recon list*/
            if (inProgressReconList.size > 0) {
                setUpAdapter(inProgressReconList as MutableList<VegaStockReconIdDetails>)
                enableStartReconButton(false, true)
            } else {
                enableStartReconButton(true, true)
            }
        }
        binding.rlCompleted.setOnClickListener {
            /*on clicking completed tab, loading completed recon list*/
            if (completedReconList.size > 0) {
                setUpAdapter(completedReconList as MutableList<VegaStockReconIdDetails>)
                enableStartReconButton(false, false)
            } else {
                enableStartReconButton(true, false)
            }
        }
        binding.btnStartRecon.setOnClickListener {
            /*ReconID creation API call*/
            vm.getReconId(prepareReconIdReq())
        }
    }

    /*ReconID creation request data*/
    private fun prepareReconIdReq(): VegaStockReconCreateReconIdReq {
        var reconIdRequest = VegaStockReconCreateReconIdReq()
        reconIdRequest.plant = plant.plantId
        reconIdRequest.storageLocation =
            if (storageLocation.storageLocationCode.equals("null")) "" else storageLocation.storageLocationCode
        reconIdRequest.reconType = auditType
        return reconIdRequest
    }

    private fun observer() {
        var storageLocationCode =
            if (storageLocation.storageLocationCode.equals("null")) "" else storageLocation.storageLocationCode

        /*api call to fetch inProgress/Completed Recon list*/
        vm.getStockReconInprogressCompletedList(plant.plantId, storageLocationCode)
        vm.stockReconInprogressList.observe(viewLifecycleOwner, Observer { updateStockReconInfo(it) })

        /*ReconID creation api response*/
        vm.reconId.observe(viewLifecycleOwner, Observer { updateReconIdInfo(it) })

        /*ReconID Deletion response*/
        vm.updatedReconIdStatus.observe(viewLifecycleOwner, Observer { reconCancelStatus(it) })

    }

    private fun updateStockReconInfo(response: Resource<GenericReqAndResp<List<VegaStockReconIdDetails>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        /*After getting response from reconlist api, filtering the inprogress and completed list*/
                        inProgressReconList = data.filter { it.status.equals("in_progress", ignoreCase = true) }
                        completedReconList = data.filter { it.status.equals("completed", ignoreCase = true) }
                        if (inProgressReconList?.size!! > 0) {
                            /*if InProgress list is available,
                            then you should now allow user to create another recon,
                            so diable the start recon button*/
                            enableStartReconButton(false, true)
                            setUpAdapter(inProgressReconList as MutableList<VegaStockReconIdDetails>)
                        } else {
                            enableStartReconButton(true, true)
                        }
                    }
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun updateReconIdInfo(response: Resource<GenericReqAndResp<VegaStockReconIdDetails>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        /*After successfully creating ReconID, move to add lot page*/
                        moveToAddLotPage(data)
                    }
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun reconCancelStatus(response: Resource<GenericReqAndResp<VegaStockReconUpdateStatus>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    inProgressReconList = emptyList()
                    /*once inprogress recon deleted successfully,
                    enable the start recon button to allow the user to start new recon*/
                    enableStartReconButton(true, true)
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun enableStartReconButton(stockReconButtonStatus: Boolean, isInProgress: Boolean) {
        if (stockReconButtonStatus) {
            binding.btnStartRecon.isEnabled = true
            binding.btnStartRecon.visible()
            binding.tvEmpty.visible()
            binding.mRecyclerView.gone()
            binding.btnStartRecon.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnStartRecon.isEnabled = false
            binding.tvEmpty.gone()
            binding.mRecyclerView.visible()
            binding.btnStartRecon.visible()
            binding.btnStartRecon.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }

        if (isInProgress) {
            binding.rlCompleted.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
            binding.rlInprogress.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.rlCompleted.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            binding.rlInprogress.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
            /*in completed tab, there is no start new recon button*/
            binding.btnStartRecon.gone()
        }
    }

    private fun getArgumentsData() {
        val bundle = arguments?.getBundle(BUNDLE_DATA)
        auditType = bundle?.getString(AUDIT_TYPE).toString()
        plant = bundle?.getParcelable<Plant>(PLANT_DETAILS) as Plant
        storageLocation = bundle.getParcelable<StorageLocation>(STORAGE_LOCATION_DETAILS) as StorageLocation
    }

    private fun setUpAdapter(data: MutableList<VegaStockReconIdDetails>) {
        binding.mRecyclerView.setUpAdapter(
            data,
            R.layout.item_recon_inprogress_completed_list,
            ItemReconInprogressCompletedListBinding::inflate,
            { it, pos, bindingItem ->
                val item = data.get(pos)
                bindingItem.tvReconIdValue.setText(item.id.toString())
                bindingItem.tvRecTypeValue.setText(item.reconType)
                bindingItem.tvTotalLotsValue.setText(item.totalNoOfLots.toString())
//                bindingItem.tvRecStatusValue.setText(item.status)
                bindingItem.tvRecDateValue.setText(item.createdAt?.split(" ")?.get(0).toString())
                if (item.status.equals("in_progress", true)) {
                    bindingItem.ivClose.visible()
                } else {
                    bindingItem.ivClose.gone()
                }
                bindingItem.ivClose.setOnClickListener { showDeleteReconConfirmationDialog(item.id) }
            }, itemClick = {
                if (this.status.equals("in_progress", true)) {
                    var bagAuditDetails = VegaStockReconBagDetails()
                    bagAuditDetails.reconIdDetails = this
                    moveToAuditListPage(bagAuditDetails)
                }
            })
    }

    private fun showDeleteReconConfirmationDialog(reconId: String) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_recon_confirm_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.updateStockReconIdStatus(
                        reconId,
                        STATUS_CANCEL
                    )
                },
                { dismiss() })
        }
    }

    private fun moveToAddLotPage(data: VegaStockReconIdDetails) {
        val bundle = Bundle()
        bundle.putParcelable(RECON_ID_DETAILS, data)
        bundle.putParcelable(PLANT_DETAILS, plant)
        callBack?.replaceFragment(STOCK_RECON_ADD_LOT, bundle)
    }

    private fun moveToAuditListPage(bagAuditDetails: VegaStockReconBagDetails) {
        bagAuditDetails.plant = plant
        callBack?.replaceFragment(STOCK_ALL_AUDIT_LIST, bagAuditDetails)
    }

}
