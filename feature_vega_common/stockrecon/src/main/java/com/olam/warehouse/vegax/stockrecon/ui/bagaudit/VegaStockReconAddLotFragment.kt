package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.master.vega.model.VegaStockReconIdDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentVegaStockAuditAddLotBinding
import com.olam.warehouse.vegax.stockrecon.databinding.ItemAddedLotListBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.AUDIT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.PLANT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.RECON_ID_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.SELECTED_LOTS
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_BAG_AUDIT
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_LOT_LIST
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaStockReconAddLotFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_stock_audit_add_lot
    private lateinit var binding: FragmentVegaStockAuditAddLotBinding
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()
    var selectedLotList = ArrayList<VegaDispatchLots>()
    var reconIdDetails = VegaStockReconIdDetails()
    var auditList = mutableListOf<VegaStockReconGetAllAuditData>()
    private var plant = Plant();


    companion object {
        fun newInstance(bundle: Bundle) = VegaStockReconAddLotFragment().putArgs {
            putBundle(BUNDLE_DATA, bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaStockAuditAddLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        getArgumentsData()
        setUIValues()
        clickListener()
        observer()
    }

    private fun observer() {
        /*The below observer will check,
        entered batch number is already existing or not with db,
        and fetch corresponding batch from api*/
        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.etContainer.setText("")
            } else fetchLotDetails(
                binding.etContainer.text.toString()
            )
        })

        /*lot details response from API*/
        vm.lotDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })

    }


    private fun clickListener() {
        val bundle = Bundle()
        bundle.putParcelable(RECON_ID_DETAILS, reconIdDetails)
        bundle.putParcelableArrayList(AUDIT_DETAILS, auditList as ArrayList<out Parcelable>)
        binding.clInventory.setOnClickListener {
            /*we should not allow user to select multiple lots,
            * in audit, only one lot is possible*/
            if (selectedLotList.size == 0) {
                callBack?.replaceFragment(STOCK_RECON_LOT_LIST, bundle)
            } else showSnack(getString(R.string.select_more_lot_warning_msg))
            /*context?.toast(getString(R.string.select_more_lot_warning_msg))*/
        }
        binding.btnAudit.setOnClickListener { validateToProceed() }
        binding.btAdd.setOnClickListener {
            /*we should not allow user to select multiple lots,
            * in audit, only one lot is possible*/
            if (selectedLotList.size == 0) {
                vm.validateLot(binding.etContainer.text.toString())
            } else showSnack(getString(R.string.select_more_lot_warning_msg))/*context?.toast(getString(R.string.select_more_lot_warning_msg))*/
        }
        binding.clScan.setOnClickListener {
            /*we should not allow user to select multiple lots,
            * in audit, only one lot is possible*/
            if (selectedLotList.size == 0) {
                moveToScan()
            } else showSnack(getString(R.string.select_more_lot_warning_msg))/*context?.toast(getString(R.string.select_more_lot_warning_msg))*/
        }
    }

    private fun setUIValues() {
        binding.tvAuditTypeValue.text = reconIdDetails.reconType
        binding.tvWarehouseValue.text = reconIdDetails.plant.plus(" - ").plus(plant.plantName)
        binding.tvLocationValue.text =
            if (reconIdDetails.storageLocation.equals("null")) "-" else reconIdDetails.storageLocation
    }

    private fun getArgumentsData() {
        val bundle = arguments?.getBundle(BUNDLE_DATA)
        plant = bundle?.getParcelable<Plant>(PLANT_DETAILS) as Plant
        reconIdDetails =
            bundle?.getParcelable<VegaStockReconIdDetails>(RECON_ID_DETAILS) as VegaStockReconIdDetails
        auditList = bundle?.getParcelableArrayList<VegaStockReconGetAllAuditData>(AUDIT_DETAILS) ?: ArrayList()
    }

    private fun setAddedLotAdapter(list: ArrayList<VegaDispatchLots>) {
        binding.rvLots.setUpAdapter(list,
            R.layout.item_added_lot_list,
            ItemAddedLotListBinding::inflate,
            { item, pos, bindingItem ->
                val data = list.get(pos)
                bindingItem.tvLotNoValue.text = data.batchNumber
                bindingItem.tvLocationValue.text = data.storageLocationCode
                bindingItem.tvPlantValue.text = data.plantId
                bindingItem.tvWeightValue.text = data.weight.plus(data.unitOfMeasure)
                bindingItem.ivClose.setOnClickListener {
                    showConfirmationDialog(pos, bindingItem.ivClose)
                }

            },
            itemClick = {

            })
    }

    fun updateLotList(list: ArrayList<VegaDispatchLots>) {
        /*if the selected lot does not contain bag,
        we should not allow them,
        because without bag details we cannot do any audit*/
        if (list.get(0).bagType?.isEmpty() == true) {
            showSnack(getString(R.string.bag_details_not_available_msg))
            return
        }
        /*if user select the different location lot,
        * we should not allow, because the user have to audit only for the selected location*/
        if (reconIdDetails.storageLocation.isNotEmpty() == true && !reconIdDetails.storageLocation.equals(list.get(0).storageLocationCode)) {
            showSnack(getString(R.string.storage_location_does_not_match_msg))
            return
        }
        selectedLotList.clear()
        selectedLotList.addAll(list)
        setAddedLotAdapter(selectedLotList)

    }

    private fun validateToProceed() {
        /*the below condition is to check empty lots*/
        if (selectedLotList.size > 0) {
            /*we have only one lot, thats why we are sending get(0)*/
            val bundle = Bundle()
            bundle.putParcelable(PLANT_DETAILS, plant)
            bundle.putParcelable(RECON_ID_DETAILS, reconIdDetails)
            bundle.putParcelable(SELECTED_LOTS, selectedLotList.get(0))
            callBack?.replaceFragment(STOCK_RECON_BAG_AUDIT, bundle)
        } else {
            showSnack(getString(R.string.empty_lots_warning))
        }

    }

    private fun showConfirmationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message(R.string.confirm_remove)
            positiveButton(text = UIUtils.getSpannedText(view.context.getString(R.string.proceed), true)) {
                /*after getting successful delete response, remove from list and update the adapter*/
                selectedLotList.removeAt(position)
                binding.rvLots.adapter?.notifyDataSetChanged()
            }
            negativeButton(
                text = UIUtils.getSpannedText(
                    view.context.getString(com.olam.warehouse.presentation.R.string.cancel), false
                )
            ) {
                dismiss()
            }
        }
    }

    private fun moveToScan() {
        /*for scanner batch number option*/
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*getting batch number from qr code, while scanning*/
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    fetchLotDetails(it)
                }
            }
        }
    }

    private fun fetchLotDetails(lotId: String) {
        /*fetching lot details from api, in case if user entered or scanned a batch number*/
        showLoading()
        if (AppUtils.isOnline()) reconIdDetails.plant?.let { vm.getLotDetails(lotId, it) }
    }

    private fun showLotAlreadyExistDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.already_added)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok), true
                )
            ) {
                dismiss()
            }
        }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        when (it1.size == 1) {
                            true -> updateLotList(it1 as ArrayList<VegaDispatchLots>)

//                            true -> setAddedLotAdapter(it1 as ArrayList<VegaDispatchLots>)
                            else -> chooseOneLotDialog(it1)
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

    /*in some case, we have multiple lots for same batchnumber,
    * in that case, we will show dialog box to user select the lots*/
    private fun chooseOneLotDialog(lots: List<VegaDispatchLots>) {
        val lotItem = lots.map {
            getString(com.olam.warehouse.login.R.string.lot_no).plus(" : ").plus(it.batchNumber).plus("\n")
                .plus("Weight").plus(" : ").plus(it.weight).plus(" ").plus(it.unitOfMeasure).plus("\n")
                .plus("Storage Location").plus(" : ").plus(it.storageLocationCode)
        }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_lot)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = lotItem) { _, index, text ->
                setAddedLotAdapter(lots as ArrayList<VegaDispatchLots>)
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }


}
