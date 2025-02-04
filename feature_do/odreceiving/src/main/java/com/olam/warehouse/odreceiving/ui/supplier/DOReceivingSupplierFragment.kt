package com.olam.warehouse.odreceiving.ui.supplier

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.model.TransactionListPojo
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.dorigin.entity.DOBag
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOTransactionDetail
import com.olam.warehouse.master.dorigin.entity.DOTxnDetail
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingSupplierBltBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.ui.blt.ScannerActivityBlt
import com.olam.warehouse.odreceiving.ui.offline.DOReceivingOfflineSummaryActivity
import com.olam.warehouse.odreceiving.ui.weigh.DOReceivingWeighActivity
import com.olam.warehouse.odreceiving.utils.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ReceivingType
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class DOReceivingSupplierFragment : BaseFragment() {

    private lateinit var doTxnDetail: DOTxnDetail
    private val vm: DOReceivingViewModel by viewModel()
    private val receivingData = DOReceiving()
    private var mLotId = ""

    private lateinit var binding: FragmentDoReceivingSupplierBltBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_supplier_blt
    private var callBack: CallBack? = null
    private var fromScan: Boolean? = null
    interface CallBack {
        fun replaceFragment(
            moveFrag: String
        )
    }

    companion object {
        fun newInstance() = DOReceivingSupplierFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoReceivingSupplierBltBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initUI()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/supplier/DOReceivingSupplierFragment")
            .title("OD/Receiving")
            .with(tracker)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    mLotId = it
                    validateTnxId(it.length, isFromScan = true)
                }
            }
        }
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnStartWeigh, it, true)
        }
        vm.transaction.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })
        Log.d("test_werks:", PreferenceHelper.get(Constants.WERKS, ""))
        Log.d("test_Plantdetails", getPlantDetails().plantId + " " + getPlantDetails().plantName)
        vm.doBags.observe(viewLifecycleOwner, Observer { doBags ->
            saveBag(doTxnDetail, doBags)
        })
        vm.product.observe(viewLifecycleOwner, Observer { materials ->
            doTxnDetail.let { doDetails ->
                val hasMaterialId =
                    materials.any { material -> material.materialCode == doDetails.materialId }
                if (hasMaterialId) {
                    val materialName =
                        materials.first { material -> material.materialCode == doDetails.materialId }
                            .materialName
                    val uom =
                        materials.first { material -> material.materialCode == doDetails.materialId }
                            .unitsOfMeasure
                    val supplierName = doDetails.supplierName
                    receivingData.materialCode = doDetails.materialId
                    receivingData.materialName = materialName
                    receivingData.supplierCode = doDetails.supplierId
                    receivingData.supplierName = supplierName
                    receivingData.txnId = doDetails.lotTransactionId
                    receivingData.emptyBagWeight = doDetails.emptyBagWeight
                    receivingData.bagType = doDetails.bagType
                    receivingData.netWeight = doDetails.netweight?.toDoubleOrNull()!!

                    //                    receivingData.doWeightThreshold = doDetails.weightThreshold
                    receivingData.creationDate = doDetails.creationDate

                    if (uom != null) {
                        receivingData.uom = uom
                    }
                    binding.tvProduct.text = materialName
                    binding.tvSupplier.text = supplierName
                    saveInDB(doTxnDetail)
                } else {
                    showSnack(getString(R.string.error_material))
                }
            }
        })
//        vm.transactionOffline.observe(viewLifecycleOwner, Observer { updateOfflineUI(it) })

        vm.receiveWithLineItemLocal.observeOnce(this, Observer {
            if (it != null) {
                updateOfflineLayerUI(it)
            }
        })
        vm.getReceivingWithLineItem()

        binding.btnEnter.setOnClickListener {
            mLotId = binding.etEnterTransId.text.toString()
            validateTnxId(mLotId.length, isFromScan = false)
        }
        binding.tvDispatchDetails.setOnClickListener { moveToDispatchDetails() }
        binding.btnStartWeigh.setOnClickListener { validateInputs() }
        binding.btnScanLot.setOnClickListener { moveToScan() }
        binding.btnSelectLot.setOnClickListener { moveToTransactionList() }
        binding.llQualityOffline.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    DOReceivingOfflineSummaryActivity::class.java
                )
            )
        }
    }

    private fun saveInDB(it: DOTxnDetail) {
        doTxnDetail = it
        vm.getDOBags(it.lotTransactionId)
    }

    /*private fun saveInDB(it: DOTxnDetail?) {
        doTxnDetail = it!!
        var transactionDetail = DOTransactionDetail()
        transactionDetail.lotTransactionId = doTxnDetail.lotTransactionId
        transactionDetail.materialId = doTxnDetail.materialId
        transactionDetail.materialName = doTxnDetail.materialName
        transactionDetail.weightUOM = doTxnDetail.weightUOM
        transactionDetail.isLot = doTxnDetail.isLot
        transactionDetail.netweight = doTxnDetail.netweight
        transactionDetail.supplierId = doTxnDetail.supplierId
        transactionDetail.supplierName = doTxnDetail.supplierName
        if (doTxnDetail.transactionDetails != null && doTxnDetail.transactionDetails!!.size > 0) {
            transactionDetail.noOfBags = doTxnDetail.transactionDetails!![0]?.noOfBags
        }
        transactionDetail.unitsOfMeasure = doTxnDetail.unitsOfMeasure
        transactionDetail.isMapped = doTxnDetail.isMapped
        transactionDetail.displaySupplierId = doTxnDetail.displaySupplierId
        transactionDetail.creationDate = doTxnDetail.creationDate

        vm.saveTransactionDetail(transactionDetail = transactionDetail)
        if (doTxnDetail.transactionDetails != null && doTxnDetail.transactionDetails!!.size > 0) {

            var txnDetail = doTxnDetail.transactionDetails!![0]
            txnDetail.bagList?.let {
                for (i in 0 until txnDetail.bagList.size) {
                    val doBag = DOBag()
                    doBag.lotTransactionId = txnDetail.transactionId
                    doBag.bagQrCode = txnDetail.bagList[i].bagQrCode
                    doBag.weight = txnDetail.bagList[i].weight
                    doBag.invalidQrCode = txnDetail.bagList[i].invalidQrCode
                    doBag.bagMissed = txnDetail.bagList[i].bagMissed
//            doBag.bagMissed = true

//            var bag = Bag()
//            bag.bagMissed = doBag.bagMissed
//            bag.bagQrCode = doBag.bagQrCode
//            bag.invalidQrCode = doBag.invalidQrCode
//            bag.weight = doBag.weight
//
//            bagList.add(bag)
                    vm.saveBagDetail(bag = doBag)
                }
            }
        }


//        doTxnDetail.transactionDetails = mutableListOf(TransactionDetail(transactionDetail.noOfBags, transactionDetail.lotTransactionId, bagList))
    }*/

    private fun saveBag(
        it: DOTxnDetail,
        doBags: List<DOBag>
    ) {
        doTxnDetail = it
        val transactionDetail = DOTransactionDetail()
        transactionDetail.lotTransactionId = it.lotTransactionId
        transactionDetail.materialId = it.materialId
        transactionDetail.materialName = it.materialName
        transactionDetail.weightUOM = receivingData.uom
        transactionDetail.isLot = it.isLot
        transactionDetail.netweight = it.netweight
        transactionDetail.supplierId = it.supplierId
        transactionDetail.supplierName = it.supplierName
        if (it.transactionDetails != null && it.transactionDetails!!.size > 0) {
            transactionDetail.noOfBags = it.transactionDetails!![0].noOfBags
        }
        transactionDetail.unitsOfMeasure = receivingData.uom
        transactionDetail.isMapped = it.isMapped
        transactionDetail.displaySupplierId = it.displaySupplierId
        transactionDetail.creationDate = it.creationDate

        vm.saveTransactionDetail(transactionDetail = transactionDetail)
        if (it.transactionDetails != null && it.transactionDetails!!.size > 0) {

            val txnDetail = it.transactionDetails!!
            for (k in 0 until txnDetail.size) {
                txnDetail.get(k).bagList.let {
                    for (i in 0 until txnDetail.get(k).bagList.size) {
                        var doBag = DOBag()
                        doBag.transactionId = txnDetail.get(k).transactionId
                        doBag.lotTransactionId = doTxnDetail.lotTransactionId
                        doBag.bagQrCode = txnDetail.get(k).bagList[i].bagQrCode
                        doBag.weight = txnDetail.get(k).bagList[i].weight
                        doBag.invalidQrCode = txnDetail.get(k).bagList[i].invalidQrCode
                        doBag.bagMissed = txnDetail.get(k).bagList[i].bagMissed
                        doBag.isReplaced = false
                        doBag.isScanned = false
                        doBag.newQrCode = 0
                        doBag.oldQrCode = 0
                        vm.saveBagDetail(bag = doBag)
                        //            doBag.bagMissed = true

                        //            var bag = Bag()
                        //            bag.bagMissed = doBag.bagMissed
                        //            bag.bagQrCode = doBag.bagQrCode
                        //            bag.invalidQrCode = doBag.invalidQrCode
                        //            bag.weight = doBag.weight
                        //
                        //            bagList.add(bag)
                        /*if (doBags?.isNullOrEmpty()) {
                                    vm.saveBagDetail(bag = doBag)
                                } else {
                                    if (!doBags.contains(doBag))
                                        vm.saveBagDetail(bag = doBag)
                                }*/
                    }
                }
            }
        }
//        doTxnDetail.transactionDetails = mutableListOf(TransactionDetail(transactionDetail.noOfBags, transactionDetail.lotTransactionId, bagList))
    }

    private fun moveToDispatchDetails() {
        binding.etEnterTransId.error = null
        dismissSnack()
        callBack?.replaceFragment(DISPATCH_DETAILS)
    }

    private fun updateOfflineLayerUI(data: List<DOReceivingWithLineItems>?) {
        data?.let { receiving ->
            when {
                receiving.isNotEmpty() -> binding.llQualityOffline.visible()
                else -> binding.llQualityOffline.gone()
            }
        }
    }

    private fun clearData() {
        binding.tvProduct.text = ""
        binding.tvSupplier.text = ""
    }

    private fun updateUI(response: Resource<GenericReqAndResp<DOTxnDetail>>?) {
        // binding.etEnterTransId.text.clear()
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.let { resp ->
                        when (resp.success) {
                            true -> populateData(resp.data)
                            else -> {
                                mLotId = ""
                                showDialog(resp.message)
                            }
                        }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    clearData()
                    mLotId = ""
                    showDialog(it.error.toString())
                }
            }
        }
    }

    private fun updateOfflineUI(data: DOTxnDetail?) {
        hideLoading()
        if (data != null) {
            if (data.isMapped == 0) {
                populateData(data)
            } else {
                requireContext().toast(getString(R.string.transaction_already_mapped))
            }
        } else {
            binding.tvProduct.text = ""
            binding.tvSupplier.text = ""
            mLotId = ""
            requireContext().toast(getString(R.string.transaction_not_found))
        }
    }

    private fun updateOfflineUI(data: DOTransactionDetail?) {
        hideLoading()
        // binding.etEnterTransId.text.clear()
        if (data != null) {
            if (data.isMapped == 0) {
                //  populateData(data)
            } else {
                requireContext().toast(getString(R.string.transaction_already_mapped))
            }
        } else {
            requireContext().toast(getString(R.string.transaction_not_found))
        }
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivityBlt::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    private fun moveToTransactionList() {
        dismissSnack()
        callBack?.replaceFragment(TRANSACTION_LIST)
    }

    private fun populateData(data: DOTransactionDetail?) {
        vm.product.observe(viewLifecycleOwner, Observer { materials ->
            data?.let { doDetails ->
                val hasMaterialId =
                    materials.any { material -> material.materialCode == doDetails.materialId }
                if (hasMaterialId) {
                    val materialName = doDetails.materialName
//                    val materialName =
//                        materials.first { material -> material.materialCode == doDetails.materialId }
//                            .materialName
                    val uom =
                        materials.first { material -> material.materialCode == doDetails.materialId }
                            .unitsOfMeasure
                    val supplierName = doDetails.supplierName
                    receivingData.materialCode = doDetails.materialId
                    receivingData.materialName = materialName
                    receivingData.supplierCode = doDetails.supplierId
                    receivingData.supplierName = supplierName
                    receivingData.txnId = doDetails.lotTransactionId
                    receivingData.doWeightThreshold = doDetails.weightThreshold
                    receivingData.creationDate = doDetails.creationDate
//                    receivingData.date = doDetails.creationDate

                    if (uom != null) {
                        receivingData.uom = uom
                    }
                    binding.tvProduct.text = materialName
                    binding.tvSupplier.text = supplierName

                } else {
                    showSnack(getString(R.string.error_material))
                }
            }
        })
        vm.getProducts()
    }

    private fun populateData(data: DOTxnDetail?) {
        doTxnDetail = data ?: DOTxnDetail()
        vm.getProducts()
    }

    private fun validateTnxId(count: Int, isFromScan: Boolean) {
        fromScan = isFromScan
        // bag qr
        if (isFromScan) {
            when {
                count != 0 -> fetchTnxDetails(isFromScan)
                isFromScan -> requireContext().toast(getString(R.string.scan_valid_qr_code))
                else -> showTnxError()
            }
        } else {
            when {
                count == 12 || count == 13 -> fetchTnxDetails(isFromScan)
                isFromScan -> requireContext().toast("Scan a valid tnx/lot id")
                else -> showTnxError()
            }
        }
    }

    private fun showTnxError() {
        binding.etEnterTransId.error = getString(R.string.error_valid_tnx_id)
        binding.etEnterTransId.requestFocus()
    }

    private fun fetchTnxDetails(fromScan: Boolean) {
        binding.etEnterTransId.hideKeyboard()
        showLoading()
        if (isOnline()) {
            if (fromScan) {
                // bag qr detail
                vm.getTransactionDetail(mLotId, "0")
            } else {
                // transaction detail
                vm.getTransactionDetail("0", mLotId)
            }
        } else {
//            vm.getTransactionDetailOffline(mLotId)

            val str: String = PreferenceHelper.get("txnData", "")
            if (str != "") {
                val txnMasterPojo = Gson().fromJson(str, TransactionListPojo::class.java)
                var txnFound: DOTxnDetail? = null
                if (fromScan) {
                    txnMasterPojo?.list?.forEach {
                        if (it.transactionDetails != null) {
                            for (txn in it.transactionDetails!!) {
                                if (txn.bagList != null && txn.bagList.isNotEmpty()) {
                                    val bags = txn.bagList
                                    for (bag in bags) {
                                        if (bag.bagQrCode.toString() == mLotId) {
                                            txnFound = it
                                            return@forEach
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    txnMasterPojo?.list?.forEach {
                        if (it.lotTransactionId == mLotId) {
                            txnFound = it
                            return@forEach
                        }
                    }
                }

                updateOfflineUI(txnFound)

            }
        }
    }

    private fun moveToWeighPage() {
        receivingData.wsGate = WS01
        //receivingData.uom = UOM
        receivingData.posnr = POSNR
        receivingData.wtype = ReceivingType.SUPPLIER.type
        receivingData.wtype = ReceivingType.SUPPLIER.type
        val intent = Intent(requireContext(), DOReceivingWeighActivity::class.java)
        intent.putExtra(RECEIVING_DATA, receivingData)
        intent.putExtra("doTxnDetail", doTxnDetail)
        startActivity(intent)
    }

    private fun validateInputs() {
        val count = mLotId.length
        if (fromScan != null && fromScan!!) {
            when {
                mLotId.isEmpty() || count == 0 -> showSnack(getString(R.string.scan_valid_qr_code))
                receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_product))
                receivingData.supplierName.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_supplier))
                receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_material_code_blank))
                receivingData.supplierCode.isNullOrEmpty() -> showSnack(getString(R.string.error_supplier_code_blank))
                else -> moveToWeighPage()
            }
        } else {
            when {
                mLotId.isEmpty() || !(count == 12 || count == 13) -> showSnack(getString(R.string.message_valid_txn_id))
                receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_product))
                receivingData.supplierName.isNullOrEmpty() -> showSnack(getString(R.string.message_valid_supplier))
                receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_material_code_blank))
                receivingData.supplierCode.isNullOrEmpty() -> showSnack(getString(R.string.error_supplier_code_blank))
                else -> moveToWeighPage()
            }
        }
    }
}
