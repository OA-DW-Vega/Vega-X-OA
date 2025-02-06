package com.olam.warehouse.odreceiving.ui.weigh

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.data.domain.model.DOWeighScale
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingWeighScaleBinding
import com.olam.warehouse.odreceiving.databinding.ItemDoWeighScaleBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.ui.blt.ScannerActivityBlt
import com.olam.warehouse.odreceiving.ui.summary.DOReceivingSummaryActivity
import com.olam.warehouse.odreceiving.utils.*
import com.olam.warehouse.presentation.adapter.KadapterNew
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.CountryCode
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

//import org.matomo.sdk.Tracker
//import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingWeighScaleFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private var isValidateTxnIdCalled: Boolean = false
    private var qrCodeCopyEntered: String? = null
    private var qrCodeEntered: String? = null
    private var qrCodeNew: Int = 0
    private var currentQrCode: String = ""
    private var adapter: KadapterNew<DOWeighScale, ItemDoWeighScaleBinding>? = null
    private var totalScannedWeight: String = "0.0"
    private var totalScannedBags: Int = 0
    private var doTxnDetail: DOTxnDetail? = null
    private var tareWeight: Double = 0.0
    private var isFirstTimeCalled: Boolean = true
    private var bagsCount: Int = 0
    private var mLotId = ""
    private var isBltEnabled: Boolean = false
    private var scanLevelId: Int = 0
    private val vm: DOReceivingViewModel by viewModel()
    private val mWeighs = mutableListOf<DOWeighScale>()
    private val mReceiving = mutableListOf<DOReceiving>()
    private var receivingData = DOReceiving()
    private var packageMaterial: DOPackageMaterial? = null
    private var postData: java.util.ArrayList<DOReceivingLineItem>? = null
    var countryCode = PreferenceHelper.get(Constants.COUNTRY_CODE, "")
    private var isOneScanProcessCompleted = true
    private var storageLocationCode=""
    private var storageLocationName=""
    private var storageLocationList= arrayListOf<DOStorageLocation>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    var locationNames= arrayListOf<String>()
    private var MAX_BAG_COUNT = 0

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private var mInputStream: InputStream? = null
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight: String = ""

    private lateinit var binding: FragmentDoReceivingWeighScaleBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_weigh_scale

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoReceivingWeighScaleBinding.inflate(layoutInflater)
//        isFirstTimeCalled = false
        return binding.root


    }

    override fun onResume() {
        super.onResume()
        Log.i("onResume", "Called")

        initUI()
    }

    companion object {
        fun newInstance(
            data: DOReceiving,
            postData: ArrayList<DOReceivingLineItem>?,
            doTxnDetail: DOTxnDetail
        ) =
            DOReceivingWeighScaleFragment().putArgs {
                putParcelable(RECEIVING_DATA, data)
                putParcelableArrayList(RECEIVING_POST_DATA, postData)
                putParcelable("doTxnDetail", doTxnDetail)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/weigh/DOReceivingWeighScaleFragment")
            .title("OD/Receiving")
            .with(tracker)
        //initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        if (adapter != null && mWeighs.isNotEmpty()) {
            setUpRecyclerView()
            binding.rvWeight.adapter = adapter
        }

        binding.etTotalWeight.addDecimalLimiter(3)

        binding.btnPullBagWeight.setOnClickListener {
            initBt()
            binding.etTotalWeight.error = null
            binding.etTotalWeight.setText(mWeight.toString())
        }

        binding.tvStoValue.setOnClickListener{
            showSingleSelectDialog(getString(R.string.select_storage_loc), STORAGE_LOCATION)
        }

        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        doTxnDetail = arguments?.getParcelable("doTxnDetail")!!


        postData = arguments?.getParcelableArrayList(RECEIVING_POST_DATA)
        vm.getAllDOBagsInfo()
        vm.doBagsInfo.observe(viewLifecycleOwner, observer)

        vm.getLocations()
        vm.location.observe(viewLifecycleOwner, Observer {
            storageLocationList= it.toMutableList() as ArrayList<DOStorageLocation>
            if (it.isNullOrEmpty()) {
                binding.txtStorageLoc.gone()
                binding.cusStorageLayout.gone()
            }else{

                it.forEach {
                    locationNames.add(it.storageLocationCode+"-"+it.storageLocationName)
                }
            }
        })



        /*if(scanLevelId==1 || scanLevelId==3){*/
        // vm.getDOBags(doTxnDetail?.lotTransactionId!!, true)
        // vm.doBagsInfo.observe(viewLifecycleOwner, observer)

        /*}else if(scanLevelId==2){
            vm.getAllDOBagsInfo()
            vm.doBags.observe(viewLifecycleOwner, observer)
        }*/

        setHeaderView()
        binding.btnAdd.setOnClickListener {
            if(countryCode.equals(CountryCode.GUATEMALA.code) && storageLocationList.isNotEmpty() && storageLocationCode.isEmpty()){
                showSnack(getString(R.string.select_storage_loc))
            }else {
                addItemToAdapter()
            }
        }
        binding.btnConfirm.setOnClickListener {
            if (isBltEnabled && (scanLevelId == 1)) {
                vm.getAllDOBagsInfo()
                vm.doBagsInfo.observeOnce(viewLifecycleOwner, Observer { doBags ->

                    for (doBag in doBags) {
                        if (isBltEnabled && (scanLevelId == 1 || scanLevelId == 2)) {
                            if (doBags?.isNullOrEmpty()!!) {
                                moveToSummaryPage()
                                break
                            } else {
                                val missedBagCount = doBags.filter { it.bagMissed }.count()
                                if (bagsCount == MAX_BAG_COUNT) {
                                    moveToSummaryPage(false)
                                    break
                                } else {
                                    activity?.toast(getString(R.string.mismatch_bags))
                                    break
                                }
                            }
                        }
                    }
                })
            } else if (isBltEnabled && scanLevelId == 2) {
                vm.getAllDOBagsInfo()
                vm.doBagsInfo.observeOnce(viewLifecycleOwner, Observer { doBags ->

                    for (doBag in doBags) {
                        if (isBltEnabled && (scanLevelId == 1 || scanLevelId == 2)) {
                            if (doBags?.isNullOrEmpty()!!) {
                                moveToSummaryPage()
                                break
                            } else {
                                val missedBagCount = doBags.filter { it.bagMissed }.count()
                                if (bagsCount == MAX_BAG_COUNT) {
                                    moveToSummaryPage(false)
                                    break
                                } else {
                                    activity?.toast(getString(R.string.mismatch_bags))
                                    break
                                }
                            }
                        }
                    }
                })
            } else if (isBltEnabled && scanLevelId == 3) {
                val list = mutableListOf<Bag>()
                list.clear()
                vm.getAllDOBagsInfo()
                vm.doBagsInfo.observeOnce(viewLifecycleOwner, Observer { doBags ->
                    var mDoBags = doBags.filter { it.lotTransactionId.contains(doTxnDetail!!.lotTransactionId) }
                    for (doBag in mDoBags) {
                        if (isBltEnabled && scanLevelId == 3) {
                            if (mDoBags.isNullOrEmpty()) {
                                moveToSummaryPage()
                                break
                            } else {
                                val missedBagCount = mDoBags.filter { it.bagMissed }.count()
                                if ((missedBagCount + bagsCount) == MAX_BAG_COUNT) {
                                    moveToSummaryPage(false)
                                    break
                                } else {
                                    activity?.toast(getString(R.string.mismatch_bags))
                                    break
                                }
                            }
                        }
                    }
                })
            } else {
                moveToSummaryPage()
            }
        }
        binding.tvLotTxn.setOnClickListener { moveToLotTransactionBagDetailsPage() }
        binding.btnScanBag.setOnClickListener { moveToScanningActivity() }
        var material = listOf<String>()
        vm.material.observe(viewLifecycleOwner, Observer {

            material = it.map { data -> data.bagType }
            val materialAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, material)
            binding.spMaterial.adapter = materialAdapter
            binding.spMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    packageMaterial = it[position]
                }
            }
            if (receivingData.tmpWbId.isNotEmpty()) {
                postData?.forEach { item ->
                    addReceiving(
                        item.grossWeight,
                        item.bagCount.toString(),
                        item.bagWeight.toString()
                    )
                }
            }
        })
        vm.getMaterials()
    }

    private fun moveToScanningActivity() {
        if (isOneScanProcessCompleted) {
            if (bagsCount < MAX_BAG_COUNT) {
                val intent = Intent(requireContext(), ScannerActivityBlt::class.java)
                startActivityForResult(intent, Constants.SCAN_QR)
            }
        } else {
            activity?.toast(getString(R.string.enter_weight_details))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    mLotId = it
                    validateTnxId(it, isFromScan = true)
                }
            }
        }

        if (requestCode == UIUtils.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            getPairedDevices()
        }
    }

    @SuppressLint("LongLogTag")
    private var observer: Observer<List<DOBag>> = Observer { doBags ->
        if (isValidateTxnIdCalled) {
            var isValidQrCode = false
            var isAlreadyMissedBag = false
            var isAlreadyReplaced = false
            var newQrCode = 0

            Log.i("doBagsList", doBags.toString())

            for (it in doBags) {
                Log.i("doBagsEnteredCurrentCode", qrCodeEntered.toString())
                Log.i("doBagsIT", it.toString())

                if (it.isReplaced && (it.bagQrCode.toString().contains(qrCodeEntered.toString())
                            || (it.newQrCode.toString().contains(qrCodeEntered.toString()) && it.newQrCode != 0) )) {
                    Log.i("doBagsReplaceLoop", "Inside")
                    if (it.newQrCode.toString().contains(qrCodeEntered.toString()) && it.newQrCode != 0) {
                        Log.i("doBagsReplaceLoopOne", "Inside")
                        isAlreadyReplaced = true
                        isValidQrCode = true
                        qrCodeCopyEntered = it.bagQrCode.toString()
                        newQrCode = qrCodeEntered?.toInt()!!
                    } else if (it.bagQrCode.toString().contains(qrCodeEntered.toString())) {
                        Log.i("doBagsReplaceLoopTwo", "Inside")
                        isAlreadyReplaced = false
                        isValidQrCode = true
                        qrCodeCopyEntered = it.bagQrCode.toString()
                    } else {
//                        isValidQrCode = false
                        Log.i("doBagsReplaceLoopThree", "Inside")
                    }
                    break
                } else if (it.bagMissed && it.bagQrCode.toString().contains(qrCodeEntered.toString())) {
                    Log.i("doBagsBagMissedLoop", "Inside")
                    isValidQrCode = true
                    isAlreadyMissedBag = true
                    qrCodeCopyEntered = it.bagQrCode.toString()
                    break
                } else if (it.bagQrCode.toString().contains(qrCodeEntered.toString())) {
                    Log.i("doBagsNonScannedLoop", "Inside")
                    isValidQrCode = true
                    isAlreadyReplaced = true
                    qrCodeCopyEntered = it.bagQrCode.toString()
                    break
                } else if(it.newQrCode.toString().contains(qrCodeEntered.toString())) {
                    isValidQrCode = true
                    isAlreadyReplaced = false
                    qrCodeCopyEntered = it.bagQrCode.toString()
                    newQrCode = qrCodeEntered?.toInt()!!
                } else {
                    Log.i("doBagsContinueLoop", "Inside")
                    continue
                }
            }

            isValidateTxnIdCalled = false

            // new logic
            if (isValidQrCode) {
                Log.i("doBagsValidQrCode", "Inside")
                qrCodeNew = 0
                if (isAlreadyMissedBag) {
                    Log.i("doBagsValidQrCodeMissedToast", "Inside")
                    activity?.toast(getString(R.string.already_marked_as_missing))
                } else if (isAlreadyReplaced) {
                    Log.i("doBagsValidQrCodeAlreadyReplaced", "Inside")
                    if (newQrCode != 0) {
                        Log.i("doBagsValidQrCodeAlreadyReplacedOne", "Inside")
                        qrCodeNew = newQrCode
                        // do scanning process of replaced bag
                        if (!vm.getQr().contains(qrCodeCopyEntered)) {
                            Log.i("doBagsValidQrCodeAlreadyReplacedOneOne", "Inside")
                            vm.addQr(qrCodeCopyEntered.toString())
                            addBag(qrCodeEntered!!, qrCodeCopyEntered!!)
                        } else {
                            Log.i("doBagsValidQrCodeAlreadyReplacedOneTwo", "Inside")
                            activity?.toast(getString(R.string.qr_code_alerady_scanned))
                        }
                    } else {
                        Log.i("doBagsValidQrCodeAlreadyReplacedTwo", "Inside")
                        if (!vm.getQr().contains(qrCodeCopyEntered)) {
                            Log.i("doBagsValidQrCodeAlreadyReplacedTwoOne", "Inside")
                            vm.addQr(qrCodeCopyEntered.toString())
                            addBag(qrCodeEntered!!, qrCodeCopyEntered!!)
                        } else {
                            Log.i("doBagsValidQrCodeAlreadyReplacedTwoTwo", "Inside")
                            activity?.toast(getString(R.string.qr_code_alerady_scanned))
                        }
                    }
                } else {
                    Log.i("doBagsValidQrCodeAlreadyReplacedToast", "Inside")
                    activity?.toast(getString(R.string.qr_code_already_replaced))
                }
            } else {
                Log.i("doBagsValidQrCodeInvalidToast", "Inside")
                activity?.toast(getString(R.string.invalid_qr))
            }
        }
    }

    private fun validateTnxId(qrCode: String, isFromScan: Boolean) {
        qrCodeCopyEntered = qrCode
        qrCodeEntered = qrCode

        var qrCodeCopy = qrCode
        when {
            qrCode.isNotEmpty() -> {
                if (doTxnDetail != null && doTxnDetail!!.transactionDetails != null && doTxnDetail!!.transactionDetails!![0].bagList != null) {
                    var isValidQrCode = false
                    isValidateTxnIdCalled = true
//                    vm.getDOBags(doTxnDetail?.lotTransactionId!!, true)

                    /*vm.getDOBags(doTxnDetail?.lotTransactionId!!, true)
                    vm.doBags.observeOnce(viewLifecycleOwner, Observer { doBags ->
                        var isAlreadyMissedBag = false
                        var isAlreadyReplaced = false
                        var newQrCode = 0

                        for (it in doBags) {
                            if (it.isReplaced && (it.bagQrCode == qrCode.toInt() || (it.newQrCode == qrCode.toInt() && it.newQrCode != 0) )) {
                                if (it.newQrCode == qrCode.toInt() && it.newQrCode != 0) {
                                    isAlreadyReplaced = true
                                    isValidQrCode = true
                                    qrCodeCopy = it.bagQrCode.toString()
                                    newQrCode = qrCode.toInt()
                                }
                                else if (it.bagQrCode == qrCode.toInt()) {
                                    isAlreadyReplaced = false
                                    isValidQrCode = true
                                } else {
                                    isValidQrCode = false
                                }
                                break
                            } else if (it.bagMissed && it.bagQrCode == qrCode.toInt()) {
                                isValidQrCode = true
                                isAlreadyMissedBag = true
                                break
                            } else if (it.bagQrCode == qrCode.toInt()) {
                                isValidQrCode = true
                                isAlreadyReplaced = true
                                break
                            } else {
                                continue
                            }
                        }

                        if (isValidQrCode) {
                            qrCodeNew = 0
                            if (isAlreadyMissedBag) {
                                activity?.toast("This bag QR code is already marked as missing")
                            } else if (isAlreadyReplaced) {
                                if (!vm.scannedQrCodes.contains(qrCode)) {
                                    vm.addQr(qrCode)
                                    if (newQrCode != 0) {
                                        qrCodeNew = newQrCode
                                    }
                                    addBag(qrCodeCopy, qrCode)
                                } else {
                                    activity?.toast("This QR code is already scanned/entered")
                                }
                            } else {
                                activity?.toast("This bag QR code is already replace with other QR code")
                            }
                        } else {
                            activity?.toast("Invalid QR code")
                        }
                    })*/
                }
            }
            isFromScan -> requireContext().toast(getString(R.string.scan_valid_qr_code))
            else -> requireContext().toast(getString(R.string.enter_qr_code))
        }
    }

    private fun addBag(qrCode: String, currentQrCodeCopy: String) {
        isOneScanProcessCompleted = false
        binding.tvSupplier.text = getString(R.string.scanned_bags)
        binding.tvTotal.text = bagsCount.toString().plus("/$MAX_BAG_COUNT")
        binding.etNoOfBags.setText("1")

        if (bagsCount == MAX_BAG_COUNT) {
            binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
        }

//        if (qrCodeNew != 0)
//            currentQrCode = qrCode
//        else
        currentQrCode = currentQrCodeCopy

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            totalScannedBags: Int,
            totalScannedWeight: String,
            tagName: String,
            receivingData: DOReceiving
        )
    }

    private fun moveToLotTransactionBagDetailsPage() {
        dismissSnack()
        callBack?.replaceFragment(BAG_DETAILS, totalScannedBags, totalScannedWeight, "DOReceivingWeighScaleFragment::class.simpleName", receivingData)
    }

    private fun moveToSummaryPage(shouldAlertShown: Boolean? = true) {
        if (shouldAlertShown!!) {
            if (mReceiving.isEmpty()) {
                requireContext().toast(getString(R.string.message_weight_to_proceed))
                return
            }
        }
        if (receivingData.mtnCode.isNullOrEmpty()) {
            val intent = Intent(requireContext(), DOReceivingSummaryActivity::class.java)
            intent.putExtra(RECEIVING_DATA, receivingData)
            intent.putExtra("isBltEnabled", isBltEnabled)
            intent.putExtra("scanLevelId", scanLevelId)
            intent.putExtra("doTxnDetail", doTxnDetail)
            mReceiving.forEachIndexed { index, doReceiving -> doReceiving.item = index.inc().toString() }
            intent.putParcelableArrayListExtra(RECEIVING_POST_DATA, ArrayList(mReceiving))
            startActivity(intent)
        } else {
            mReceiving.forEachIndexed { index, doReceiving -> doReceiving.item = index.inc().toString() }
            val intent = Intent().putParcelableArrayListExtra(RECEIVING_POST_DATA, ArrayList(mReceiving))
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }
    }


    private fun addItemToAdapter() {
        val totalWeight = binding.etTotalWeight.text.toString()
        val noOfBags = binding.etNoOfBags.text.toString()

        if (isBltEnabled) {
            if (scanLevelId == 1 || scanLevelId == 2) {
                when {
                    noOfBags.isEmpty() || noOfBags.toInt() <= 0 -> {
                        binding.etNoOfBags.requestFocus()
                        binding.etNoOfBags.error = getString(R.string.error_bag)
                    }
                    totalWeight.isEmpty() || totalWeight.toDouble() <= 0 -> {
                        binding.etTotalWeight.requestFocus()
                        binding.etTotalWeight.error = getString(R.string.error_weight)
                    }
                    else -> {
                        addReceiving(totalWeight.toDouble(), noOfBags, "")
                    }
                }
            } else {
                if (scanLevelId == 3) {
                    when {
                        totalWeight.isEmpty() || totalWeight.toDouble() <= 0 -> {
                            binding.etTotalWeight.requestFocus()
                            binding.etTotalWeight.error = getString(R.string.error_weight)
                        }
                        else -> {
                            if (!isOneScanProcessCompleted) {
                                addReceiving(totalWeight.toDouble(), "0", "")
                            }
                        }
                    }
                }
            }
        } else {
            when {
                noOfBags.isEmpty() || noOfBags.toInt() <= 0 -> {
                    binding.etNoOfBags.requestFocus()
                    binding.etNoOfBags.error = getString(R.string.error_bag)
                }
                totalWeight.isEmpty() || totalWeight.toDouble() <= 0 -> {
                    binding.etTotalWeight.requestFocus()
                    binding.etTotalWeight.error = getString(R.string.error_weight)
                }
                else -> {
                    addReceiving(totalWeight.toDouble(), noOfBags, "")
                }
            }
        }

    }

    private fun addReceiving(wt: Double, noOfBags: String, tWeight: String?) {
        if (isBltEnabled && (scanLevelId == 3 || scanLevelId == 2 || scanLevelId == 1) ) {
            if ((noOfBags.toInt() + bagsCount) > MAX_BAG_COUNT) {
                val excess = MAX_BAG_COUNT - (noOfBags.toInt() + bagsCount)
                requireContext().toast(getString(R.string.no_of_bags_exceed) + " Excess Bag count: " + Math.abs(excess))
            } else {
                val receiving = receivingData.copy()

                if (scanLevelId == 1 || scanLevelId == 2) {
//                    binding.tvTotal.text = bagsCount.toString()
                    binding.tvTotal.text = bagsCount.toString().plus("/$MAX_BAG_COUNT")
                    tareWeight = 0.0
                    if (receivingData.emptyBagWeight != null && receivingData.emptyBagWeight != "")
                        tareWeight = noOfBags.toInt() * receivingData.emptyBagWeight!!.toDouble()
                    val netWeight = wt - tareWeight
                    val totalEnteredWt = vm.getNetWeight(mReceiving) + netWeight
                    if (netWeight <= 0) {
                        requireContext().toast(getString(R.string.net_weight_validate_msg))
                    } else if (receivingData.netWeight < totalEnteredWt) {
                        requireContext().toast(getString(R.string.total_weight_validate_msg))
//                        vm.getQr().remove(currentQrCode)
                    }else {
                        bagsCount += noOfBags.toInt()
                        receivingData.bagCount = bagsCount.toString()
//                        binding.tvTotal.text = bagsCount.toString()
                        binding.tvTotal.text = bagsCount.toString().plus("/$MAX_BAG_COUNT")
                        receiving.bagCount = noOfBags
//                        receiving.bagType = packageMaterial?.bagType
                        receiving.tareWeight = (noOfBags.toInt() * receivingData.emptyBagWeight!!.toDouble()).toString()

                        receiving.grossWeight = wt
                        if (countryCode.equals(CountryCode.HONDURAS.code)||countryCode.equals(CountryCode.GUATEMALA.code)) {
                            var mTare = String.format("%.2f", receiving.tareWeight?.toDouble())
                            var mGrossWeight = String.format("%.2f", receiving.grossWeight.toDouble())
                            var mNetWeight =
                                if (mGrossWeight.isNotEmpty() && mTare.isNotEmpty()) mGrossWeight.toDouble()
                                    .minus(
                                        mTare.toDouble()
                                    ) else 0.0
                            receiving.netWeight = mNetWeight.formatThreeDigits().toDouble()
                            receiving.tareWeight = mTare.toDouble().formatThreeDigits()
                            receiving.grossWeight = mGrossWeight.toDouble()
                            receiving.bagWeight = receiving.tareWeight!!.toDouble()
                            receiving.storageLocationName=storageLocationName
                            receiving.storageLocationCode=storageLocationCode
                        }else if(countryCode.equals(CountryCode.COLUMBIA.code)){
                            var mTare = String.format("%.2f", receiving.tareWeight?.toDouble())
                            var mGrossWeight = String.format("%.2f", receiving.grossWeight.toDouble())
                            var mNetWeight =
                                if (mGrossWeight.isNotEmpty() && mTare.isNotEmpty()) mGrossWeight.toDouble()
                                    .minus(
                                        mTare.toDouble()
                                    ) else 0.0
                            receiving.netWeight = mNetWeight.roundToInt().toDouble()
                            receiving.tareWeight = mTare.toDouble().roundToInt().toString()
                            receiving.grossWeight = mGrossWeight.toDouble().roundToInt().toDouble()
                            receiving.bagWeight = receiving.tareWeight!!.toDouble()
                        }else{
                            receiving.bagWeight = tareWeight
                            receiving.netWeight = netWeight
                        }
                        mReceiving.add(receiving)
                        mWeighs.add(DOWeighScale(receivingData.wsGate, "${noOfBags.toInt()}", wt.toString()))

                        setHeaderView()
                        setUpRecyclerView()
                        binding.etNoOfBags.text.clear()
                        binding.etTotalWeight.text.clear()
                        binding.etTotalWeight.hideKeyboard()
                    }
                } else {
                    tareWeight = 0.0
                    if (receivingData.emptyBagWeight != null && receivingData.emptyBagWeight != "")
                        tareWeight = 1 * receivingData.emptyBagWeight!!.toDouble()
                    val netWeight = wt - tareWeight
                    val totalEnteredWt = vm.getNetWeight(mReceiving) + netWeight
                    if (netWeight <= 0) {
                        requireContext().toast(getString(R.string.net_weight_validate_msg))
//                        vm.getQr().remove(currentQrCode)
                    } else if (receivingData.netWeight < totalEnteredWt) {
                        requireContext().toast(getString(R.string.total_weight_validate_msg))
//                        vm.getQr().remove(currentQrCode)
                    } else {
                        bagsCount += 1
                        binding.tvTotal.text = bagsCount.toString().plus("/$MAX_BAG_COUNT")
                        receiving.bagCount = "1"
//                        receiving.bagType = packageMaterial?.bagType
                        receiving.tareWeight = tareWeight.toString()
                        receiving.bagWeight = tareWeight
                        receiving.grossWeight = wt
                        if (countryCode.equals(CountryCode.HONDURAS.code)||countryCode.equals(CountryCode.GUATEMALA.code)) {
                            var mTare = String.format("%.2f", receiving.tareWeight?.toDouble())
                            var mGrossWeight = String.format("%.2f", receiving.grossWeight.toDouble())
                            var mNetWeight =
                                if (mGrossWeight.isNotEmpty() && mTare.isNotEmpty()) mGrossWeight.toDouble()
                                    .minus(
                                        mTare.toDouble()
                                    ) else 0.0
                            receiving.netWeight = mNetWeight.formatThreeDigits().toDouble()
                            receiving.tareWeight = mTare.toDouble().formatThreeDigits()
                            receiving.grossWeight = mGrossWeight.toDouble()
                            receiving.bagWeight = receiving.tareWeight!!.toDouble()
                        } else if(countryCode.equals(CountryCode.COLUMBIA.code)){
                            var mTare = String.format("%.2f", receiving.tareWeight?.toDouble())
                            var mGrossWeight = String.format("%.2f", receiving.grossWeight.toDouble())
                            var mNetWeight =
                                if (mGrossWeight.isNotEmpty() && mTare.isNotEmpty()) mGrossWeight.toDouble()
                                    .minus(
                                        mTare.toDouble()
                                    ) else 0.0
                            receiving.netWeight = mNetWeight.roundToInt().toDouble()
                            receiving.tareWeight = mTare.toDouble().roundToInt().toString()
                            receiving.grossWeight = mGrossWeight.toDouble().roundToInt().toDouble()
                            receiving.bagWeight = receiving.tareWeight!!.toDouble()
                        } else {
                            receiving.netWeight = netWeight
                        }
                        mReceiving.add(receiving)
                        mWeighs.add(
                            DOWeighScale(
                                receivingData.wsGate,
                                "1",
                                wt.toString(),
                                currentQrCode,
                                qrCodeNew.toString()
                            )
                        )

                        val doBag = DOBag()
                        doBag.apply {
                            var transactionDetail = doTxnDetail!!.transactionDetails
                            transactionDetail?.forEachIndexed { index, baginfo ->
                                var mTransaction_id =
                                    baginfo.bagList.filter { currentQrCode.equals(it.bagQrCode.toString()) }
                                if (mTransaction_id.size > 0)
                                    transactionId = baginfo.transactionId
                            }
                            /*for (k in 0 until transactionDetail!!.size) {
                                transactionDetail.get(k).bagList?.let {
                                    for (i in 0 until transactionDetail.get(k).bagList.size) {
                                        if(currentQrCode.toInt()==transactionDetail.get(k).bagList[i].bagQrCode){
                                            transactionId = transactionDetail.get(k).transactionId
                                        }
                                    }
                                }}*/
                            bagMissed = false
                            if (currentQrCode != "") {
                                bagQrCode = currentQrCode.toInt()
                            }
                            lotTransactionId = doTxnDetail!!.lotTransactionId
                            invalidQrCode = false
                            isScanned = true
                            weight = wt.toString()
                            oldQrCode = 0
                            newQrCode = qrCodeNew
                            isReplaced = false
                        }

                        vm.deleteAndSaveBagDetail(doBag)
                        Log.i("ScannedBagsReplaceAdd", vm.getQr().toString())
                        isOneScanProcessCompleted = true
                        setHeaderView()
                        setUpRecyclerView()
                        binding.etNoOfBags.text.clear()
                        binding.etTotalWeight.text.clear()
                        binding.etTotalWeight.hideKeyboard()
//                        vm.deleteBagDetail(doBag)
//                        vm.saveBagDetail(doBag)

                        if (bagsCount == MAX_BAG_COUNT) {
                            binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
                            binding.btnConfirm.isEnabled = true
                        }
                    }
                }
            }
        } else {
            // NON BLT
            val receiving = receivingData.copy()
            var tareWeight = 0.0
            if (tWeight?.isNotEmpty()!!) {
                tareWeight = tWeight.toDouble()
            } else {
                tareWeight = if (packageMaterial?.unitsOfMeasure.equals(receivingData.uom)) {
                    noOfBags.toInt() * (packageMaterial?.tareWeight?.toDouble()?.formatThreeDigits()?.toDouble() ?: 0.0)
                } else {
                    if (countryCode.equals(CountryCode.GUATEMALA.code)) {
                        noOfBags.toInt() * weightConverter(
                            packageMaterial?.unitsOfMeasure,
                            receivingData.uom,
                            packageMaterial?.tareWeight?.toDouble()?.formatThreeDigits()?.toDouble() ?: 0.0
                        )?.toDouble()?.formatThreeDigits()?.toDouble()!!
                    }else{
                        noOfBags.toInt() * weightConverter(
                            packageMaterial?.unitsOfMeasure,
                            receivingData.uom,
                            packageMaterial?.tareWeight?.toDouble() ?: 0.0
                        )!!
                    }
                }
            }
            val netWeight = wt - tareWeight

            if (netWeight <= 0) {
                requireContext().toast(getString(R.string.net_weight_validate_msg))
            } else {
                receiving.bagCount = noOfBags
                receiving.bagType = packageMaterial?.bagType
                receiving.tareWeight = tareWeight.toString()
                receiving.bagWeight = tareWeight
                receiving.grossWeight = wt
                if (countryCode.equals(CountryCode.HONDURAS.code)||countryCode.equals(CountryCode.GUATEMALA.code)) {
                    var mTare = String.format("%.2f", receiving.tareWeight?.toDouble())
                    var mGrossWeight = String.format("%.2f", receiving.grossWeight.toDouble())
                    var mNetWeight = if (mGrossWeight.isNotEmpty() && mTare.isNotEmpty()) mGrossWeight.toDouble()
                        .minus(
                            mTare.toDouble()
                        ) else 0.0
                    receiving.netWeight = mNetWeight.formatThreeDigits().toDouble()
                    receiving.tareWeight = mTare.toDouble().formatThreeDigits()
                    receiving.grossWeight = mGrossWeight.toDouble()
                    receiving.bagWeight = receiving.tareWeight!!.toDouble()
                    receiving.storageLocationName=storageLocationName
                    receiving.storageLocationCode=storageLocationCode

                } else if(countryCode.equals(CountryCode.COLUMBIA.code)){
                    var mTare = String.format("%.2f", receiving.tareWeight?.toDouble())
                    var mGrossWeight = String.format("%.2f", receiving.grossWeight.toDouble())
                    var mNetWeight = if (mGrossWeight.isNotEmpty() && mTare.isNotEmpty()) mGrossWeight.toDouble()
                        .minus(
                            mTare.toDouble()
                        ) else 0.0
                    receiving.netWeight = mNetWeight.roundToInt().toDouble()
                    receiving.tareWeight = mTare.toDouble().roundToInt().toString()
                    receiving.grossWeight = mGrossWeight.toDouble().roundToInt().toDouble()
                    receiving.bagWeight = receiving.tareWeight!!.toDouble()
                }else{
                    receiving.netWeight = netWeight
                }

                mReceiving.add(receiving)
                mWeighs.add(DOWeighScale(receivingData.wsGate, "${noOfBags.toInt()}", wt.toString()))

                setHeaderView()
                setUpRecyclerView()
                binding.etNoOfBags.text.clear()
                binding.etTotalWeight.text.clear()
                binding.etTotalWeight.hideKeyboard()

            }
        }
    }

    /*private fun addReceiving(wt: Double, noOfBags: String, tWeight: String?) {
        if (isBltEnabled && (scanLevelId == 3 || scanLevelId == 2 || scanLevelId == 1) ) {

            val receiving = receivingData.copy()

            if (scanLevelId == 1 || scanLevelId == 2) {
                binding.tvTotal.text = bagsCount.toString()
                tareWeight = 0.0
                if (receivingData.emptyBagWeight != null && receivingData.emptyBagWeight != "")
                    tareWeight = noOfBags.toInt() * receivingData.emptyBagWeight!!.toDouble()
                val netWeight = wt - tareWeight
                if (netWeight <= 0) {
                    requireContext().toast(getString(R.string.net_weight_validate_msg))
                } else {
                    bagsCount += noOfBags.toInt()
                    receivingData.bagCount = bagsCount.toString()
                    binding.tvTotal.text = bagsCount.toString()
                    receiving.bagCount = noOfBags
//                        receiving.bagType = packageMaterial?.bagType
                    receiving.tareWeight = (noOfBags.toInt() * receivingData.emptyBagWeight!!.toDouble()).toString()
                    receiving.bagWeight = tareWeight
                    receiving.grossWeight = wt
                    receiving.netWeight = netWeight
                    mReceiving.add(receiving)
                    mWeighs.add(DOWeighScale(receivingData.wsGate, "${noOfBags.toInt()}", wt.toString()))

                    setHeaderView()
                    setUpRecyclerView()
                    binding.etNoOfBags.text.clear()
                    binding.etTotalWeight.text.clear()
                    binding.etTotalWeight.hideKeyboard()
                }
            } else {
                // bag level BLT
                if ((noOfBags.toInt() + bagsCount) > MAX_BAG_COUNT) {
                    requireContext().toast(getString(R.string.no_of_bags_exceed))
                } else {
                    tareWeight = 0.0
                    if (receivingData.emptyBagWeight != null && receivingData.emptyBagWeight != "")
                        tareWeight = 1 * receivingData.emptyBagWeight!!.toDouble()
                    val netWeight = wt - tareWeight
                    val totalEnteredWt = vm.getNetWeight(mReceiving) + netWeight
                    if (netWeight <= 0) {
                        requireContext().toast(getString(R.string.net_weight_validate_msg))
//                        vm.getQr().remove(currentQrCode)
                    } else if (receivingData.netWeight < totalEnteredWt) {
                        requireContext().toast(getString(R.string.total_weight_validate_msg))
//                        vm.getQr().remove(currentQrCode)
                    } else {
                        bagsCount += 1
                        binding.tvTotal.text = bagsCount.toString().plus("/$MAX_BAG_COUNT")
                        receiving.bagCount = "1"
//                        receiving.bagType = packageMaterial?.bagType
                        receiving.tareWeight = tareWeight.toString()
                        receiving.bagWeight = tareWeight
                        receiving.grossWeight = wt
                        receiving.netWeight = netWeight
                        mReceiving.add(receiving)
                        mWeighs.add(DOWeighScale(receivingData.wsGate, "1", wt.toString(), currentQrCode, qrCodeNew.toString()))

                        val doBag = DOBag()
                        doBag.apply {
                            lotTransactionId = doTxnDetail?.lotTransactionId!!
                            bagMissed = false
                            if (currentQrCode != "") {
                                bagQrCode = currentQrCode.toInt()
                            }
                            invalidQrCode = false
                            isScanned = true
                            weight = wt.toString()
                            oldQrCode = 0
                            newQrCode = qrCodeNew
                            isReplaced = false
                        }

                        vm.deleteAndSaveBagDetail(doBag)
                        Log.i("ScannedBagsReplaceAdd", vm.getQr().toString())
                        isOneScanProcessCompleted = true
                        setHeaderView()
                        setUpRecyclerView()
                        binding.etNoOfBags.text.clear()
                        binding.etTotalWeight.text.clear()
                        binding.etTotalWeight.hideKeyboard()
//                        vm.deleteBagDetail(doBag)
//                        vm.saveBagDetail(doBag)

                        if (bagsCount == MAX_BAG_COUNT) {
                            binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
                            binding.btnConfirm.isEnabled = true
                        }
                    }
                }
            }
        } else {
            // Non BLT
            val receiving = receivingData.copy()
            var tareWeight = 0.0
            if (tWeight?.isNotEmpty()!!) {
                tareWeight = tWeight.toDouble()
            } else {
                tareWeight = if (packageMaterial?.unitsOfMeasure.equals(receivingData.uom)) {
                    noOfBags.toInt() * (packageMaterial?.tareWeight?.toDouble() ?: 0.0)
                } else {
                    noOfBags.toInt() * weightConverter(
                        packageMaterial?.unitsOfMeasure,
                        receivingData.uom,
                        packageMaterial?.tareWeight?.toDouble() ?: 0.0
                    )!!
                }
            }
            val netWeight = wt - tareWeight
            if (netWeight <= 0) {
                requireContext().toast(getString(R.string.net_weight_validate_msg))
            } else {
                receiving.bagCount = noOfBags
                receiving.bagType = packageMaterial?.bagType
                receiving.tareWeight = tareWeight.toString()
                receiving.bagWeight = tareWeight
                receiving.grossWeight = wt
                receiving.netWeight = netWeight
                mReceiving.add(receiving)
                mWeighs.add(DOWeighScale(receivingData.wsGate, "${noOfBags.toInt()}", wt.toString()))

                setHeaderView()
                setUpRecyclerView()
                binding.etNoOfBags.text.clear()
                binding.etTotalWeight.text.clear()
                binding.etTotalWeight.hideKeyboard()
            }
        }
    }*/

    @SuppressLint("LongLogTag")
    private fun setHeaderView() {
        if (isFirstTimeCalled) {
            binding.llContent.visibility = View.GONE
        }

        binding.btnScanBag.visibility = View.GONE
        binding.btnPullBagWeight.visibility = View.GONE
        vm.getSAPMaterials()
        vm.sapMaterial.observe(viewLifecycleOwner, Observer { sapMaterials ->
            if (sapMaterials != null && sapMaterials.isNotEmpty()) {
                for (sapMaterial in sapMaterials) {
                    if (sapMaterial.materialCode == receivingData.materialCode && sapMaterial.bltEnabled != null && sapMaterial.bltEnabled!!) {
                        Log.i("SapMaterialCode", sapMaterial.materialCode)
                       // Log.i("SapMaterialCodeReceivingData", receivingData.materialCode)
                        isBltEnabled = sapMaterial.bltEnabled!!
                        scanLevelId = if (sapMaterial.scanLevelId != null) sapMaterial.scanLevelId!! else 0
                        binding.bagTypeLayout.visibility = View.GONE
                        Log.i("SapMaterialCodeReceivingData", isBltEnabled.toString())
                        Log.i("SapMaterialCodeReceivingData", scanLevelId.toString())

                        if (isOnline() && isFirstTimeCalled) {
                            vm.getTransactionDetail("0", receivingData.txnId!!)
                            vm.transaction.observe(viewLifecycleOwner, Observer {
                                isFirstTimeCalled = false
                                if (it?.status == Resource.Status.SUCCESS) {
                                    binding.llContent.visibility = View.VISIBLE
                                    if (it.data?.data?.transactionDetails != null && it.data?.data?.transactionDetails!!.isNotEmpty()) {
                                        MAX_BAG_COUNT = 0
                                        if (scanLevelId == 3 || scanLevelId == 2) {
                                            val txnDetail = it.data?.data?.transactionDetails
                                            MAX_BAG_COUNT = txnDetail?.sumOf {
                                                it.noOfBags?.toInt() ?: 0
                                            } ?: 0
                                            /*for (i in 0 until txnDetail!!.size) {
                                                MAX_BAG_COUNT = MAX_BAG_COUNT?.plus(txnDetail[i].noOfBags!!)
                                            }*/
                                        } else {
                                            MAX_BAG_COUNT = it.data?.data?.transactionDetails!![0].noOfBags!!
                                        }

                                        //MAX_BAG_COUNT = it?.data?.data?.transactionDetails!![0].noOfBags!!
                                    } else {
                                        MAX_BAG_COUNT = 0
                                    }
//                                        MAX_BAG_COUNT = 1

                                    if (scanLevelId == 1) {
                                        binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
//                                        binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString()
//                                        binding.tvTotal.text = bagsCount.toString().plus("/$MAX_BAG_COUNT")
                                        binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
                                    } else if (scanLevelId == 2) {
                                        binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
//                                        binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString()
//                                        binding.tvTotal.text = bagsCount.toString().plus("/$MAX_BAG_COUNT")
                                        binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
                                    } else if (scanLevelId == 3) {
                                        binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
                                        if (bagsCount == MAX_BAG_COUNT) {
                                            binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
                                        } else {
                                            binding.headerLayout.setBackgroundColor(getColor(R.color.red))
                                        }
                                    }

                                    binding.spMaterial.visibility = View.GONE
                                    binding.bagTypeLayout.visibility = View.GONE
                                    binding.txtSelectBagType.visibility = View.GONE
                                }
//                            binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString().plus(" ").plus(getString(R.string.bags_total))
//                                .plus(" ").plus(mWeighs.map { it.weight.toDouble() }.sum().format()).plus(" ").plus(receivingData.uom)
//                            binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString()
                            })
                        } else {
                            // Offline
                            binding.llContent.visibility = View.VISIBLE
                            var txnFound: DOTxnDetail? = doTxnDetail
                            if (txnFound?.transactionDetails != null && txnFound.transactionDetails!!.isNotEmpty()) {
                                MAX_BAG_COUNT = 0
                                if (scanLevelId == 3 || scanLevelId == 2) {
                                    val txnDetail = txnFound.transactionDetails!!
                                    MAX_BAG_COUNT = txnDetail.sumOf { it.noOfBags?.toInt() ?: 0 }
                                    /*for (i in 0 until txnDetail!!.size) {
                                        MAX_BAG_COUNT = MAX_BAG_COUNT?.plus(txnDetail[i].noOfBags!!)
                                    }*/
                                } else {
                                    MAX_BAG_COUNT = txnFound.transactionDetails!![0].noOfBags!!
                                }
                            } else {
                                MAX_BAG_COUNT = 0
                            }

                            if (scanLevelId == 1) {
                                binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
//                                    binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString()
                                binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
                            } else if (scanLevelId == 2) {
                                binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
//                                    binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString()
                                binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
                            } else if (scanLevelId == 3) {
                                binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
                                if (bagsCount == MAX_BAG_COUNT) {
                                    binding.headerLayout.setBackgroundColor(getColor(R.color.dark_green))
                                } else {
                                    binding.headerLayout.setBackgroundColor(getColor(R.color.red))
                                }
                            }

                            binding.spMaterial.visibility = View.GONE
                            binding.bagTypeLayout.visibility = View.GONE
                            binding.txtSelectBagType.visibility = View.GONE
                        }

                        if (sapMaterial.scanLevelId == 1) {
                            binding.tableLayout.visibility = View.VISIBLE
                            binding.tvLotTransactionIdLabel.text = getString(R.string.transaction_id)
                            binding.tvLotTxn.visibility = View.GONE
                            binding.tvSupplier.text = getString(R.string.total_no_of_bags)
                            binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
//                            binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString()
//                            binding.tvSupplier.text = receivingData.supplierName
//                            binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString().plus(" ").plus(getString(R.string.bags_total))
//                                .plus(" ").plus(mWeighs.map { it.weight.toDouble() }.sum().format()).plus(" ").plus(receivingData.uom)

                            binding.spMaterial.visibility = View.GONE
                            binding.bagTypeLayout.visibility = View.GONE
                            binding.txtSelectBagType.visibility = View.GONE

                            binding.etNoOfBags.isEnabled = true
                            binding.btnConfirm.isEnabled = true
                            binding.btnPullBagWeight.visibility = View.VISIBLE
                            break
                        } else if (sapMaterial.scanLevelId == 2) {
                            binding.tableLayout.visibility = View.VISIBLE
                            binding.tvLotTransactionIdLabel.text = getString(R.string.lot_id)
                            binding.tvLotTxn.visibility = View.GONE
                            binding.tvSupplier.text = getString(R.string.total_no_of_bags)
//                            binding.tvSupplier.text = receivingData.supplierName
                            binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
//                            binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString()
//                            binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString().plus(" ").plus(getString(R.string.bags_total))
//                                .plus(" ").plus(mWeighs.map { it.weight.toDouble() }.sum().format()).plus(" ").plus(receivingData.uom)

                            binding.spMaterial.visibility = View.GONE
                            binding.bagTypeLayout.visibility = View.GONE
                            binding.txtSelectBagType.visibility = View.GONE

                            binding.etNoOfBags.isEnabled = true
                            binding.btnConfirm.isEnabled = true
                            binding.btnPullBagWeight.visibility = View.VISIBLE
                            break
                        } else if (sapMaterial.scanLevelId == 3) {
                            binding.tableLayout.visibility = View.VISIBLE
                            binding.btnScanBag.visibility = View.VISIBLE
                            binding.tvLotTxn.visibility = View.VISIBLE
                            binding.tvSupplier.text = getString(R.string.scanned_bags)
                            binding.btnConfirm.isEnabled = true
                            binding.btnAdd.isEnabled = true

                            binding.etNoOfBags.isEnabled = false
                            receivingData.bagCount = bagsCount.toString()
                            binding.btnPullBagWeight.visibility = View.VISIBLE
                            break
                        }
                    } else {
                        // BLT not enabled
                        if (!isBltEnabled && sapMaterials.last().materialCode == sapMaterial.materialCode) {
                            binding.llContent.visibility = View.VISIBLE

                            binding.tableLayout.visibility = View.GONE
                            binding.tvSupplier.text =  receivingData.supplierName
                            binding.headerLayout.setBackgroundColor(getColor(R.color.orange))
                            binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString().plus(" ").plus(getString(R.string.bags_total))
                                .plus(" ").plus(mWeighs.map { it.weight.toDouble() }.sum().format()).plus(" ").plus(receivingData.uom)

                            binding.spMaterial.visibility = View.VISIBLE
                            binding.bagTypeLayout.visibility = View.VISIBLE
                            binding.txtSelectBagType.visibility = View.VISIBLE
                        }
                    }
                }


            } /*else {
                binding.tvSupplier.text = receivingData.supplierName
                binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString().plus(" ").plus(getString(R.string.bags_total))
                    .plus(" ").plus(mWeighs.map { it.weight.toDouble() }.sum().format()).plus(" ").plus(receivingData.uom)
            }*/

//        binding.tvSupplier.text = receivingData.supplierName
//        binding.tvSupplier.text = "Total No of Bags:"
//        binding.tvTotal.text = mWeighs.map { it.name.toInt() }.sum().toString()
//        binding.tvTotal.text =
//            mWeighs.map { it.name.toInt() }.sum().toString().plus(" ").plus(getString(R.string.bags_total))
//                .plus(" ").plus(mWeighs.map { it.weight.toDouble() }.sum().format()).plus(" ").plus(receivingData.uom)

            binding.tvUom.text = receivingData.uom

            // table values
            binding.tvLotTransactionIdValue.text = receivingData.txnId.toString()
            binding.tvProductValue.text = receivingData.materialName.toString()
            binding.tvSupplierValue.text = receivingData.supplierName.plus("/").plus(receivingData.supplierCode)
            binding.tvWeightValue.text = receivingData.netWeight.toString() + " " + receivingData.uom


            /*var noOfBags = "1"
            var tareWeight = if (packageMaterial?.unitsOfMeasure.equals(receivingData.uom)) {
                noOfBags.toInt() * (packageMaterial?.tareWeight?.toDouble() ?: 0.0)
            } else {
                noOfBags.toInt() * weightConverter(
                    packageMaterial?.unitsOfMeasure,
                    receivingData.uom,
                    packageMaterial?.tareWeight?.toDouble() ?: 0.0
                )!!
            }*/


            binding.tvWeightEmptyBagValue.text = receivingData.emptyBagWeight + " " + receivingData.uom
            binding.tvDateValue.text = receivingData.creationDate.toString()
        })
    }

    @SuppressLint("SetTextI18n", "LongLogTag")
    private fun setUpRecyclerView() {
        var count = -1
        adapter = binding.rvWeight.setUpAdapter(
            mWeighs,
            R.layout.item_do_weigh_scale,
            ItemDoWeighScaleBinding::inflate,
            { it, pos, bindingItem ->
                count++
                val weigh = it
                bindingItem.tvBag.text = weigh.name
                bindingItem.tvWeight.text =
                    weigh.weight.toDouble().formatThreeDigits().plus(" ").plus(receivingData.uom)

                // not used now
                if (isBltEnabled && scanLevelId == 3) {
                    totalScannedBags = bagsCount
                    totalScannedWeight = weigh.weight.toDouble().formatThreeDigits()
                }

                bindingItem.ivDelete.tag = count
                bindingItem.ivDelete.setOnClickListener {
                    val alertDialog =
                        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.delete))
                            .setPositiveButton(getString(R.string.yes)) { p0, p1 ->
                                val selectedWeigh = weigh
                                mWeighs.remove(weigh)
                                mReceiving.removeAt(bindingItem.ivDelete.tag.toString().toInt())

                                if (scanLevelId == 1 || scanLevelId == 2 || !isBltEnabled) {
                                    bagsCount -= weigh.name.toInt()
                                }

                                if (isBltEnabled && scanLevelId == 3) {
                                    bagsCount -= 1
                                    binding.tvTotal.text = "$bagsCount/$MAX_BAG_COUNT"
                                    binding.headerLayout.setBackgroundColor(getColor(R.color.red))

                                    vm.getQr().remove(selectedWeigh.qrCode!!)
                                    vm.getQr().remove(selectedWeigh.newQrCode!!)

//                            vm.deleteBagDetail(doTxnDetail?.lotTransactionId, currentQrCode)
                                    val doBag = DOBag().apply {
                                        isReplaced = false // give value true
                                        isScanned = false
                                        newQrCode = 0 // give new qr code
                                        oldQrCode = 0
                                        invalidQrCode = false
                                        bagQrCode = selectedWeigh.qrCode.toInt()
                                        bagMissed = false
                                        lotTransactionId = doTxnDetail!!.lotTransactionId
                                        var transactionDetail = doTxnDetail!!.transactionDetails
                                        transactionDetail?.forEachIndexed { index, baginfo ->
                                            var mTransaction_id =
                                                baginfo.bagList.filter { currentQrCode.equals(it.bagQrCode.toString()) }
                                            if (mTransaction_id.size > 0)
                                                transactionId = baginfo.transactionId
                                        }
                                        /*for (k in 0 until transactionDetail!!.size) {
                                    transactionDetail.get(k).bagList?.let {
                                        for (i in 0 until transactionDetail.get(k).bagList.size) {
                                            if(selectedWeigh.qrCode!!.toInt()==transactionDetail.get(k).bagList[i].bagQrCode){
                                                transactionId = transactionDetail.get(k).transactionId
                                            }
                                        }
                                    }}*/
                                    }
                                    vm.deleteAndSaveBagDetail(doBag)
                                    Log.i(
                                        "ScannedBagsReplaceDelete",
                                        vm.getQr()
                                            .toString() + ", " + selectedWeigh.qrCode + "," + selectedWeigh.newQrCode
                                    )
//                            vm.deleteBagDetail(doBag)
//                            vm.saveBagDetail(bag = doBag)
                                }

                                setHeaderView()
                                setUpRecyclerView()
                                p0.dismiss()
                            }.setNegativeButton(getString(R.string.cancel)) { p0, p1 ->
                                p0.cancel()
                            }.create()

                    alertDialog.show()
                }
            })
    }

    // Bluetooth code
    @SuppressLint("MissingPermission")
    private fun initBt() {
        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
        when {
            mBTAdapter.isEnabled -> getPairedDevices()
            else -> startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                UIUtils.REQUEST_ENABLE_BT
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getPairedDevices() {
        val pairedMac = PreferenceHelper.get(UIUtils.BT_MAC, "")
        if (pairedMac.isBlank()) {
            showPairedDeviceDialog()
        } else {
            showLoading()
            // doAsync {
            try {
                val device = mBTAdapter.getRemoteDevice(pairedMac)
                mBTSocket = device.createRfcommSocketToServiceRecord(mUUID)
                mBTSocket?.let {

                    if (it.isConnected) it.close()
                    it.connect()
                    if (it.isConnected) {
                        //  runOnUiThread {
                        hideLoading()
                        activity?.toast(getString(R.string.weigh_scale_connected))
                        //}
                        try {
                            DoAsync {
                                mInputStream = it.inputStream
                                var data = ""
                                var bytes: Int
                                while (true) {
                                    try {
//                                        val re = Regex("\\d{4}\\.\\d{2}")
//                                        val strArr = arrayOf("\r\n", "\r", "\n", " ")
                                        val buffer = ByteArray(1024)
                                        /*bytes = */mInputStream?.read(buffer, 0, buffer.size)
                                        data = String(buffer, 0, buffer.size, Charsets.UTF_8)
                                        val dataArr = data.split("\r\n", "\r", "\n", " ")
//                                            runOnUiThread { toast("WeightTest1: $data") }
                                        dataArr.forEach {
                                            data = Regex("[^0-9.]").replace(it, "")
                                            mWeight = data
                                            runOnUiThread {
                                                try {
                                                    if (mWeight.toDouble() > 0 && !mWeight.equals(
                                                            binding.etTotalWeight.text
                                                        )
                                                    ) {
                                                        binding.etTotalWeight.setText(mWeight.toString())
                                                    }
                                                } catch (e: java.lang.Exception) {
                                                    e.printStackTrace()
                                                }

                                            }
                                            try {
                                                if (mWeight.toDouble() > 0) data = ""
                                            } catch (e: java.lang.Exception) {
                                                e.printStackTrace()
                                            }
                                            //runOnUiThread { toast("Weight1: $mWeight") }
                                        }
                                    } catch (e: IOException) {
                                        Log.d("GinngBluetoothReadData", e.message ?: "")
                                        /*runOnUiThread {
                                            toast("Error1: " + e.message.toString())
                                        }*/
                                        break
                                    }
                                }
                            }.execute()
                        } catch (e: Exception) {
                            Log.d("GinngBluetoothReadData", e.message ?: "")
                            /*runOnUiThread {
                                toast("Error2: " + e.message.toString())
                            }*/

                        }
                    }

                }
            } catch (e: Exception) {
                Log.d("GinngBluetoothConnect", e.message ?: "")
                /*runOnUiThread {
                    toast("Error3: " + e.message.toString())
                }*/
                mBTSocket?.close()
                hideLoading()
            }
            //}.execute()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showPairedDeviceDialog() {
        mPairedDevices.addAll(mBTAdapter.bondedDevices)
        val pairedDeviceName = mBTAdapter.bondedDevices.map { it.name }
        MaterialDialog(requireContext()).show {
            title(text = getString(R.string.please_select_the_weighing_scale))
            listItemsSingleChoice(items = pairedDeviceName) { dialog, index, text ->
                if (mPairedDevices.isNotEmpty()) {
                    PreferenceHelper.save(UIUtils.BT_MAC, mPairedDevices[index].address)
                    getPairedDevices()
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok))) {
                dismiss()
            }
        }
    }


    private fun showSingleSelectDialog(title: String, currentFlag: String) {
        val list = java.util.ArrayList<String>()
        when (currentFlag) {
            STORAGE_LOCATION -> {
                list.addAll(locationNames)
            }
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFlag,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            mBTSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun clickOnItem(data: String, currentFlag: String) {
       when(currentFlag){
           STORAGE_LOCATION->{
               binding.tvStoValue.setText(data)
               customDialog?.dismiss()
               var dataCodeName=data.split("-")
               storageLocationCode= dataCodeName[0]
               storageLocationName=dataCodeName[1]
           }
       }
    }
}
