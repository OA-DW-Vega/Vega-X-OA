package com.olam.warehouse.odquality.ui.weighbridge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.WeighBridgeListPojo
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.data.domain.model.QrBag
import com.olam.warehouse.odquality.data.domain.model.QualityBag
import com.olam.warehouse.odquality.data.domain.model.QualityTxnDetail
import com.olam.warehouse.odquality.data.domain.model.SelectedBags
import com.olam.warehouse.odquality.databinding.FragmentDoQualityWeighBridgeBagBinding
import com.olam.warehouse.odquality.databinding.ItemDoQualityWeighBridgeBagBinding
import com.olam.warehouse.odquality.ui.DOQualityActivity
import com.olam.warehouse.odquality.ui.DOQualityViewModel
import com.olam.warehouse.odquality.ui.blt.ScannerActivityBltQuality
import com.olam.warehouse.odquality.utils.SELECTED_QUALITY
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class DOQualityWeighBridgeBagFragment : BaseFragment() {

    private var bags: MutableList<QualityBag> = mutableListOf()

    //    private var selectedQrCodes = mutableSetOf<String>()
    private var selectedQrCodesBag = mutableSetOf<QrBag>()

    override val layoutResourceId: Int = R.layout.fragment_do_quality_weigh_bridge_bag
    private lateinit var binding: FragmentDoQualityWeighBridgeBagBinding
    private var doQualityWBDetails: DOQualityWBDetails? = null
    private val vm: DOQualityViewModel by viewModel()

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
//            map: MutableMap<String, MutableSet<String>>
            map: MutableMap<String, MutableSet<QrBag>>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    companion object {
        fun newInstance(bundle: Bundle?) = DOQualityWeighBridgeBagFragment().putArgs {
            putParcelable(SELECTED_QUALITY, bundle)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoQualityWeighBridgeBagBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odquality/ui/weighbridge/DOQualityWeighBridgeBagFragment")
            .title("OD/Quality")
            .with(tracker)
        initUI()
        initExtra()
    }

    private fun initExtra() {
        doQualityWBDetails = arguments?.getParcelable(SELECTED_QUALITY)

        val selectedBagsStr = PreferenceHelper.get("selectedBags", "default")
        if (selectedBagsStr != "default") {
            val selectedBags = Gson().fromJson(selectedBagsStr, SelectedBags::class.java)
            val bagSet = selectedBags.map[doQualityWBDetails?.weighBridgeId]

            bagSet?.forEach {
                if (it.isSelected!!) {
                    selectedQrCodesBag.add(QrBag(it.qrCode, true))
                } else {
                    selectedQrCodesBag.add(QrBag(it.qrCode, false))
                }
                val qualityBag = QualityBag()
                qualityBag.bagQrCode = it.qrCode
                bags.add(qualityBag)
            }
            binding.rvWeighbridge.adapter?.notifyDataSetChanged()
        }

        vm.qualityWeighBridge.observe(viewLifecycleOwner, Observer {
            hideLoading()
            Log.i("resp", it.toString())

            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { list ->
                        var qualityTxnDetail: MutableList<QualityTxnDetail>? = null
                        for (listItem in list) {
                            if (listItem.wbid == doQualityWBDetails?.weighBridgeId) {
                                listItem.transactionBagDetails.let { qtyTxnDetail ->
                                    qualityTxnDetail = qtyTxnDetail
                                }
                                break
                            }
                        }

                        if (qualityTxnDetail != null) {
                            updateUI(qualityTxnDetail!!)
                        } else {
                            activity?.toast(getString(R.string.no_data_sampling))
                        }
                        if (bags.isNotEmpty()) {
                            initRecyclerView()
                        }
                        binding.rvWeighbridge.adapter?.notifyDataSetChanged()
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(),it.error.toString())
                   // activity?.toast(it.error!!)
                    if (bags.isNotEmpty()) {
                        initRecyclerView()
                    }
                }
                else -> {}
            }
        })

        if (isOnline()) {
            showLoading()
            vm.getQualityWeighBridgeBagDetail(PreferenceHelper.get(Constants.WERKS, ""))
        } else {
            val str: String = PreferenceHelper.get("txnData", "")
            val wbStr: String = PreferenceHelper.get("wbData", "")

            if (wbStr != "") {
                val wbPojo = Gson().fromJson(wbStr, WeighBridgeListPojo::class.java)
                Log.i("wbPojo", wbPojo.toString())
                wbPojo?.list?.forEach {
                    val qtyList = mutableListOf<QualityTxnDetail>()
//                    if (it?.transactionDetails != null && it?.transactionDetails!!.size > 0) {
                    if (doQualityWBDetails?.weighBridgeId == it.weighBridgeId) {
                        it.transactionBagDetails?.let { txnDetails ->
                            for (txnDetail in txnDetails) {
                                val qtyTxnDetail = QualityTxnDetail()
                                val qtyBagsList = mutableListOf<QualityBag>()

                                for (bag in txnDetail.bagList) {
                                    val qualityBag = QualityBag()
                                    qualityBag.bagQrCode = bag.bagQrCode!!
                                    qualityBag.weight = bag.weight
                                    qualityBag.isBagMissed = bag.bagMissed
                                    qualityBag.isInvalidQrCode = bag.invalidQrCode

                                    if (!qtyBagsList.contains(qualityBag)) {
                                        qtyBagsList.add(qualityBag)
                                    }
                                }

                                qtyTxnDetail.bagList = qtyBagsList
                                qtyTxnDetail.noOfBags = txnDetail.noOfBags ?: 0
                                qtyTxnDetail.transactionId = txnDetail.transactionId

                                if (!qtyList.contains(qtyTxnDetail))
                                    qtyList.add(qtyTxnDetail)
                            }

                            if (qtyList.isNotEmpty()) {
                                updateUI(qtyList)
                                return@forEach
                            } else {
                                activity?.toast(getString(R.string.no_data_sampling))
                            }
                        }
                    }
//                    }
                }
            }

            //activity?.toast("end")

            /*if (str != "") {
                val transPojo = Gson().fromJson(str, TransactionListPojo::class.java)
//                Log.i("transPojo", transPojo.toString())
                transPojo?.list?.forEach {
                    val qtyList = mutableListOf<QualityTxnDetail>()

                    if (it?.transactionDetails != null && it?.transactionDetails!!.size > 0) {
                        Log.i("doQualityWBDetails?.challan", doQualityWBDetails?.challan)
                        Log.i("doQualityWBDetails?.challanId", it?.lotTransactionId)

                        if (doQualityWBDetails?.challan == it?.lotTransactionId) {
                            it?.transactionDetails?.let { txnDetails ->
                                for (txnDetail in txnDetails) {
                                    val qtyTxnDetail = QualityTxnDetail()
                                    val qtyBagsList = mutableListOf<QualityBag>()

                                    for (bag in txnDetail.bagList) {
                                        val qualityBag = QualityBag()
                                        qualityBag.bagQrCode = bag.bagQrCode.toString()
                                        qualityBag.weight = bag.weight
                                        qualityBag.isBagMissed = bag.bagMissed
                                        qualityBag.isInvalidQrCode = bag.invalidQrCode

                                        if (!qtyBagsList.contains(qualityBag)) {
                                            qtyBagsList.add(qualityBag)
                                        }
                                    }

                                    qtyTxnDetail.bagList = qtyBagsList
                                    qtyTxnDetail.noOfBags = txnDetail.noOfBags ?: 0
                                    qtyTxnDetail.transactionId = txnDetail.transactionId

                                    if (!qtyList.contains(qtyTxnDetail))
                                        qtyList.add(qtyTxnDetail)
                                }
                                updateUI(qtyList)
                                return@forEach
                            }
                        }
                    }
                }
            }*/
        }
    }

    private fun updateUI(transactionBagDetails: MutableList<QualityTxnDetail>) {
        Log.i("transactionBagDetails", transactionBagDetails.toString())
        if (transactionBagDetails != null && transactionBagDetails.size > 0
            && transactionBagDetails[0] != null
            && transactionBagDetails[0].bagList != null
            && transactionBagDetails[0].bagList.size > 0
        ) {
            transactionBagDetails.forEach { transDetails ->
                transDetails.bagList.forEach { qtyBag ->
                    if (!bags.contains(qtyBag)) {
                        bags.add(qtyBag)
                    }
                }
            }

            if (bags.isNotEmpty())
                initRecyclerView()
            binding.rvWeighbridge.adapter?.notifyDataSetChanged()
        }
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.topLayout, it, false)
            getActionBtnChangedView(binding.btnSave, it, true)
        }
        binding.btnSave.setOnClickListener {
//            val map = hashMapOf<String, MutableSet<String>>()
            val map = hashMapOf<String, MutableSet<QrBag>>()
            map[doQualityWBDetails?.weighBridgeId!!] = selectedQrCodesBag

            val selectedBags = SelectedBags()
            selectedBags.map = map
            val selectedBagsStr = Gson().toJson(selectedBags)
            PreferenceHelper.save("selectedBags", selectedBagsStr)

//            val selectedMap = hashMapOf<String, MutableSet<String>>()
//            val qrCodes: MutableSet<String> = hashSetOf()
//            selectedQrCodesBag.forEach {
//                qrCodes.add(it.qrCode)
//            }
//            selectedMap[doQualityWBDetails?.weighBridgeId!!] = qrCodes

//            callBack?.replaceFragment(WEIGHBRIDGE_LIST, map)

            activity?.finish()
            val intent = Intent(requireActivity(), DOQualityActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        binding.btnNewBagForSample.setOnClickListener {
            moveToScannerActivity()
        }

        if (bags.isNotEmpty())
            initRecyclerView()
    }

    private fun initRecyclerView() {
        var qrCode = ""
        binding.rvWeighbridge.setUpAdapter(
            bags,
            R.layout.item_do_quality_weigh_bridge_bag,
            ItemDoQualityWeighBridgeBagBinding::inflate,
            { it, pos, bindingItem ->
                qrCode = it.bagQrCode
                bindingItem.tvQrCode.text = qrCode


                bindingItem.chkBox.isChecked = doQualityWBDetails?.missedQrCodes?.contains(qrCode)!!

                for (bag in selectedQrCodesBag) {
                    if (qrCode == bag.qrCode && bag.isSelected!!) {
                        bindingItem.chkBox.isChecked = true
                        break
                    }
                }
                bindingItem.chkBox.setOnClickListener {
                    val qrCode1 = bags[pos].bagQrCode
                    it.isSelected = bindingItem.chkBox.isChecked
                    if (bindingItem.chkBox.isChecked) {
                        selectedQrCodesBag.remove(QrBag(qrCode1, false))
                        selectedQrCodesBag.add(QrBag(qrCode1, true))
                        Log.i("selectedQrCodes", selectedQrCodesBag.toString())
                    } else {
                        selectedQrCodesBag.remove(QrBag(qrCode1, true))
                        selectedQrCodesBag.add(QrBag(qrCode1, false))
                        Log.i("selectedQrCodes", selectedQrCodesBag.toString())
                    }
                }
            }, {})
    }

    private fun moveToScannerActivity() {
        val intent = Intent(requireContext(), ScannerActivityBltQuality::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    validateTnxId(it, it.length, isFromScan = true)
                }
            }
        }
    }

    private fun validateTnxId(newQrCode: String, count: Int, isFromScan: Boolean) {
        when {
            count != 0 -> attachNewQr(newQrCode)
            isFromScan -> requireContext().toast(getString(R.string.scan_valid_qr_code))
            else -> requireContext().toast(getString(R.string.enter_qr_code))
        }
    }

    private fun attachNewQr(newQrCode: String) {
        if (!isQrCodeIsExists(newQrCode)) {
//            selectedQrCodes.add(newQrCode)

            selectedQrCodesBag.add(QrBag(newQrCode, false))

            var qualityBag = QualityBag()
            qualityBag.bagQrCode = newQrCode
            bags.add(qualityBag)
            if (binding.rvWeighbridge.adapter != null) {
                binding.rvWeighbridge.adapter?.notifyDataSetChanged()
            } else {
                initRecyclerView()
            }
        } else {
            activity?.toast(getString(R.string.qr_code_alerady_scanned))
        }
    }
    private fun isQrCodeIsExists(qrCode: String) : Boolean {
        var isQrCodeExist = false
        for (bag in bags) {
            if (bag.bagQrCode == qrCode) {
                isQrCodeExist = true
                break
            }
        }
        return isQrCodeExist
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
