package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.printformats.stockReconPrintRecipt
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.model.VegaStockReconBagDetails
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.master.vega.model.VegaStockReconIdDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPrintRecipt
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconUpdateStatus
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentAuditListBinding
import com.olam.warehouse.vegax.stockrecon.databinding.ItemAuditListBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.AUDIT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.PLANT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.RECON_ID_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.STATUS_COMPLETE
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_ADD_LOT
import com.olam.warehouse.vegax.stockrecon.utils.getMaterialNameFromMaterialList
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaStockReconAllAuditListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_audit_list
    private lateinit var binding: FragmentAuditListBinding
    private val vm: VegaStockReconViewModel by viewModel()
    private var callBack: VegaStockCallbackListener? = null
    private var bagAuditDetails: VegaStockReconBagDetails = VegaStockReconBagDetails()
    var auditList = ArrayList<VegaStockReconGetAllAuditData>()
    var vegaPackingMaterialList = mutableListOf<VegaMaterial>()
    private var printKeys = ArrayList<String>()


    /*using this audit delete selection id, we are deleting audit data and update adapter*/
    var auditDeleteSelectionId = ""

    companion object {
        fun newInstance(bagAuditDetails: VegaStockReconBagDetails) = VegaStockReconAllAuditListFragment().putArgs {
            putParcelable(BUNDLE_DATA, bagAuditDetails)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAuditListBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        getArgumentData()
        clickListener()
        observer()
    }

    private fun clickListener() {
        binding.tvCompleteAudit.setOnClickListener {
            /*Below condition is to check audilist is empty or not*/
            if (auditList.size > 0) {
                showCompleteAuditConfirmationDialog()
            } else {
                showSnack(getString(R.string.empty_audit_warning_msg))
            }
        }
        binding.tvAuditAnotherLot.setOnClickListener {
            /*Below condition: we should not allow user to audit more then 50 lot*/
            if (auditList.size <= 50) {
                if (auditList.size == 0) {
                    showAuditAnotherLotConfirmationDialog(R.string.audit_lot)
                } else {
                    showAuditAnotherLotConfirmationDialog(R.string.audit_another_lot)
                }
            } else {
                showSnack(getString(R.string.audit_max_limit_warning_msg))
            }
        }
    }

    private fun enableEmptyMsg(status: Boolean) {
        if (status) {
            binding.tvEmpty.visible()
            binding.tvAuditAnotherLot.setText("Add Lot")
            binding.mRecyclerView.gone()
        } else {
            binding.tvEmpty.gone()
            binding.tvAuditAnotherLot.setText("Add Another Lot")
            binding.mRecyclerView.visible()
        }
    }

    private fun updateLotsValue() {
        binding.tvTotalLotsValue.setText(auditList.size.toString())
        /*If we have multiple audit, we have calculate all weight of audit
        * & show total weight value*/
        var weightLossSum =
            auditList.sumOf {
                if (it.weightGainLoss?.isNotEmpty() == true) it.weightGainLoss?.toDouble() ?: 0.0 else 0.0
            }.formatThreeDigits()
        binding.tvTotalWtLossValue.setText(
            weightLossSum.toString().plus(if (auditList.isNotEmpty() == true) auditList.get(0).unitOfMeasure else "")
        )
    }

    private fun observer() {
        /*fetching all audit list from api*/
        vm.fetchAllAuditData(bagAuditDetails.reconIdDetails.id)
        vm.allAuditData.observe(viewLifecycleOwner, Observer { updateUI(it) })

        /*fetching all materials list, to upadate material name in audit list*/
        vm.getVegaMaterials()
        vm.vegaMaterials.observe(
            viewLifecycleOwner,
            Observer { vegaPackingMaterialList = it as MutableList<VegaMaterial> })

        /*complete recon response*/
        vm.updatedReconIdStatus.observe(viewLifecycleOwner, Observer { allAuditComplteStatus(it) })

        /*delete audit response*/
        vm.deleteAuditData.observe(viewLifecycleOwner, Observer { deleteAuditDataStatus(it) })

        /*upload overall recon and audit report image response*/
        vm.printReceiptStatus.observe(viewLifecycleOwner, Observer { postReceiptCompleteStatus(it) })
    }

    private fun allAuditComplteStatus(response: Resource<GenericReqAndResp<VegaStockReconUpdateStatus>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    /*after getting success in complete audit,
                    we have to send print report image through this api*/
                    vm.postPrintReceipt(preparePostPrintData())
//                    moveToSuccessPage()
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun postReceiptCompleteStatus(response: Resource<GenericReqAndResp<VegaStockReconPrintRecipt>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    /*after getting print report image success,
                    we have to move to success activity*/
                    moveToSuccessPage()
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun preparePostPrintData(): VegaStockReconPrintRecipt {
        /*collecting all data needed for post report print receipt data api*/
        var printReceiptData = VegaStockReconPrintRecipt()

        printKeys = stockReconPrintRecipt(
            bagAuditDetails,
            auditList, requireActivity()
        )
        if (printKeys.isNotEmpty()) {
            printReceiptData.id = bagAuditDetails.reconIdDetails.id
            printReceiptData.storageLocation = bagAuditDetails.reconIdDetails.storageLocation
            printReceiptData.warehouse = bagAuditDetails.reconIdDetails.plant
            printReceiptData.reconType = bagAuditDetails.reconIdDetails.reconType
            printReceiptData.reportUrl = printKeys.get(0)
        }
        return printReceiptData
    }

    private fun deleteAuditDataStatus(response: Resource<GenericMessage>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    /*after deleting audit, update the adapter*/
                    updateAdapter()
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun updateAdapter() {
        /*after getting sucess from delete api, using auditDeleteSelectionId,
        remove that data from local list and update the adapter*/
        var deletedList = auditList.singleOrNull { it.id == auditDeleteSelectionId }
        auditList.remove(deletedList)
        if (auditList.size > 0) {
            enableEmptyMsg(false)
            setAdapter(auditList)
        } else {
            enableEmptyMsg(true)
            updateLotsValue()
        }
    }

    private fun moveToSuccessPage() {
        var sampleList = mutableListOf<VegaStockReconGetAllAuditData>();
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.recon_success)
        )

        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.recon_id).plus(bagAuditDetails.reconIdDetails.id))
        intent.putExtra(AppUtils.STOCK_RECON_CARD, true)
        intent.putParcelableArrayListExtra(AppUtils.AUDIT_DETAILS, auditList)
        intent.putExtra(AppUtils.BAG_DETAILS, bagAuditDetails)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun getArgumentData() {
        bagAuditDetails = arguments?.getParcelable<VegaStockReconBagDetails>(BUNDLE_DATA) as VegaStockReconBagDetails
    }

    private fun setAdapter(list: MutableList<VegaStockReconGetAllAuditData>) {
        updateLotsValue()
        binding.mRecyclerView.setUpAdapter(
            list,
            R.layout.item_audit_list,
            ItemAuditListBinding::inflate,
            { it, pos, bindingItem ->
                val item = list.get(pos)
                bindingItem.tvLotNoValue.setText(item.lotNumber)
                /*the material value is not available in api, so we get from material list*/
                bindingItem.tvMaterialValue.setText(item.material?.let { it1 ->
                    getMaterialNameFromMaterialList(
                        vegaPackingMaterialList,
                        it1
                    )
                })
                bindingItem.tvSystemWeighValue.setText(item.systemNetWeight.plus(item.unitOfMeasure))
                bindingItem.tvWeightLossValue.setText(item.weightGainLoss.plus(item.unitOfMeasure))
                bindingItem.tvNoOfBagsValue.setText(item.sysNoOfBags)
                bindingItem.tvTypeOfBagsValue.setText(
                    getBagType(item)
                )
                bindingItem.ivClose.setOnClickListener {
                    /*setting the value of auditDeleteSelectionId,
                    using this id only, we will delete audit in local list*/
                    auditDeleteSelectionId = list.get(pos).id.toString()
                    list.get(pos).reconId?.let { reconId ->
                        list.get(pos).id?.let { auditId ->
                            showDeleteAuditConfirmationDialog(reconId, auditId)
                        }
                    }
                }
            }, itemClick = {

            })
    }

    private fun getBagType(auditDetails: VegaStockReconGetAllAuditData): String {
        /*in some case, we have multiple bag,
        so checking the bag one by one and setting the bag value*/
        var bagType = ""
        if (auditDetails.bagType1?.isNotEmpty() == true) {
            bagType = auditDetails?.bagType1 ?: ""
        }
        if (auditDetails.bagType2?.isNotEmpty() == true) {
            bagType = auditDetails.bagType1.plus(",").plus(auditDetails.bagType2)
        }
        if (auditDetails.bagType3?.isNotEmpty() == true) {
            bagType = auditDetails.bagType1.plus(",").plus(auditDetails.bagType2).plus(",").plus(auditDetails.bagType3)
        }
        return bagType
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        /*getting audit list from api, and stored locally*/
                        auditList = data as ArrayList<VegaStockReconGetAllAuditData>
                        if (auditList.size > 0) {
                            enableEmptyMsg(false)
                            setAdapter(auditList)
                        } else {
                            enableEmptyMsg(true)
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

    private fun moveToAddLotPage(data: VegaStockReconIdDetails) {
        /*you can create another audit from this page too,
        so we are navigating to add lot page*/
        val bundle = Bundle()
        bundle.putParcelable(PLANT_DETAILS, bagAuditDetails.plant)
        bundle.putParcelable(RECON_ID_DETAILS, data)
        bundle.putParcelableArrayList(AUDIT_DETAILS, auditList as ArrayList<out Parcelable>)
        callBack?.replaceFragment(STOCK_RECON_ADD_LOT, bundle)
    }

    private fun showDeleteAuditConfirmationDialog(reconId: String, auditId: String) {
        MaterialDialog(requireContext()).show {
            message(R.string.audit_delete_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    /*deleting audit*/
                    vm.deleteAuditData(reconId, auditId)
                },
                { dismiss() })
        }
    }

    private fun showCompleteAuditConfirmationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.audit_complete_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    /*completing recon api*/
                    vm.updateStockReconIdStatus(
                        bagAuditDetails.reconIdDetails.id,
                        STATUS_COMPLETE
                    )
                },
                { dismiss() })
        }
    }

    private fun showAuditAnotherLotConfirmationDialog(msg: Int) {
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    moveToAddLotPage(bagAuditDetails.reconIdDetails)
                },
                { dismiss() })
        }
    }
}
