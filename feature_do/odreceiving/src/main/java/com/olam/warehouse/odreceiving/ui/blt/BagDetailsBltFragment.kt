package com.olam.warehouse.odreceiving.ui.blt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.FragmentBagDetailsBltBinding
import com.olam.warehouse.odreceiving.databinding.FragmentDoBagListBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.utils.ATTACH_NEW_QR
import com.olam.warehouse.odreceiving.utils.MISSING_BAG
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.CountryCode
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class BagDetailsBltFragment : BaseFragment() {

    private var receivingData: DOReceiving? = null
    private var totalScannedBags: Int? = 0
    private var totalScannedWeight: String? = "0.0"
    private lateinit var doTxnDetail: DOTxnDetail
    private var mDoBag = mutableListOf<DOBag>()
    private lateinit var binding: FragmentDoBagListBinding
    override val layoutResourceId = R.layout.fragment_do_bag_list
    private val vm: DOReceivingViewModel by viewModel()

    companion object {
        fun newInstance(
            data: DOTxnDetail,
            totalScannedBags: Int,
            totalScannedWeight: String,
            receivingData: DOReceiving
        ) = BagDetailsBltFragment().putArgs {
            putParcelable("doTxnDetail", data)
            putInt("totalScannedBags", totalScannedBags)
            putString("totalScannedWeight", totalScannedWeight)
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    private fun initExtras() {
        doTxnDetail = arguments?.getParcelable<DOTxnDetail>("doTxnDetail")!!
        totalScannedWeight = arguments?.getString("totalScannedWeight")
        totalScannedBags = arguments?.getInt("totalScannedBags")
        receivingData = arguments?.getParcelable(RECEIVING_DATA)
       // Log.i("doTxnDetailMaterialCode", doTxnDetail.materialName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        showCustomLoading()
        setHasOptionsMenu(true)
        binding = FragmentDoBagListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odquality/ui/blt/BagDetailsBltFragment").title("OD/Receiving")
            .with(tracker)
    }

    private fun initUI() {
        // var transcationId= doTxnDetail!!.transactionDetails?.get(0)?.transactionId

        /*if(transcationId==null) {
            binding.tvTxnId.text = doTxnDetail.lotTransactionId
            vm.getDOBags(doTxnDetail.lotTransactionId, true)
        }else{
            binding.tvTxnId.text = transcationId
            vm.getDOBags(transcationId, true)
        }*/
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
        }
        vm.getAllDOBagsInfo()
        vm.doBagsInfo.observe(viewLifecycleOwner, Observer { doBags ->
            if (!doBags.isNullOrEmpty()) {
                mDoBag.clear()
                mDoBag.addAll(doBags.toMutableList())

                setUpAdapter(doTxnDetail.transactionDetails)
                /*if (doTxnDetail.transactionDetails != null && doTxnDetail.transactionDetails!!.isNotEmpty()) {
                    scannedBagCount = doTxnDetail.transactionDetails!![0].noOfBags!! - missingBagCount
                }*/

                /*     var scannedBagCount = doBags.filter { it.isScanned }.count()
                     var scannedBagWeight = 0.0
                     doBags.filter { it.isScanned }.forEach { bag ->
                         if (bag.weight != null && bag.weight != "")
                             scannedBagWeight += bag.weight!!.toDouble()
                     }

                     val missingBagCount = doBags.filter { !it.isScanned }.count()
                     var missedBagWeight: Double = receivingData?.netWeight!! - scannedBagWeight*/

                /*var totalWeight = 0.0
                if (doTxnDetail.transactionDetails!![0].noOfBags != null) {
                    for (i in 0 until doTxnDetail.transactionDetails!![0].noOfBags!!) {
                        if (doTxnDetail.transactionDetails!![0].bagList[i].weight != null) {
                            totalWeight += doTxnDetail.transactionDetails!![0].bagList[i].weight!!.toDouble()
                        }
                    }
                }*/

//                scannedBagWeight = totalWeight - missedBagWeight

                /*if (doTxnDetail.transactionDetails != null && doTxnDetail.transactionDetails!!.isNotEmpty()) {
                    doTxnDetail.transactionDetails!![0].bagList.filter {
                        !isBagAvailableInDOBags(doBags, it)
                    }.forEach { bagObj ->
                        if (bagObj.weight != null && bagObj.weight != "") {
                            scannedBagWeight += bagObj.weight!!.toDouble()
                        }
                    }

                    *//*for (i in 0 until doTxnDetail.transactionDetails!![0].noOfBags!!) {
                        val bagObj = doTxnDetail.transactionDetails!![0].bagList[i]

                        if (!isBagAvailableInDOBags(doBags, bagObj)) {
                            if (bagObj.weight != null && bagObj.weight != "") {
                                scannedBagWeight += bagObj.weight!!.toDouble()
                            }
                        }
                    }*//*
                }*/

                //binding.tvScannedBagValue.text = scannedBagCount.toString()
                //binding.tvScannedBagWeight.text = "${scannedBagWeight.formatThreeDigits()} KG"
                //binding.tvMissingBagWeight.text = "${missedBagWeight.formatThreeDigits()} KG"
                //binding.tvMissedBagValue.text = missingBagCount.toString()
            } else {
                /*if (doTxnDetail.transactionDetails != null && doTxnDetail.transactionDetails!!.isNotEmpty()) {

                    binding.tvScannedBagValue.text = totalScannedBags.toString()
                    binding.tvScannedBagWeight.text = totalScannedWeight ?: "0 KG"

                    *//*var totalWeight = 0.0
                    if (doTxnDetail.transactionDetails!![0].noOfBags != null) {
                        for (i in 0 until doTxnDetail.transactionDetails!![0].noOfBags!!) {
                            if (doTxnDetail.transactionDetails!![0].bagList[i].weight != null) {
                                totalWeight += doTxnDetail.transactionDetails!![0].bagList[i].weight!!.toDouble()
                            }
                        }
                    }*//*

                    binding.tvMissedBagValue.text = (doTxnDetail.transactionDetails!![0].noOfBags!! - totalScannedBags!!).toString()
                    binding.tvMissingBagWeight.text = (receivingData?.netWeight!! - totalScannedWeight!!.toDouble()).toString()

                    *//*val scannedBagCount =
                        if (doTxnDetail.transactionDetails!![0].noOfBags != null) doTxnDetail.transactionDetails!![0].noOfBags else 0
                    binding.tvScannedBagValue.text = scannedBagCount.toString()

                    var missingBagCount = 0
                    var scannedBagWeight: String? = null
                    var missedBagWeight: String? = null
                    for (i in 0 until doTxnDetail.transactionDetails!![0].noOfBags!!) {
                        if (doTxnDetail.transactionDetails!![0].bagList[i].bagMissed) {
                            missingBagCount += 1
                            missedBagWeight = doTxnDetail.transactionDetails!![0].bagList[i].weight
                        } else {
                            scannedBagWeight = doTxnDetail.transactionDetails!![0].bagList[i].weight
                        }
                    }*//*
                }*/
            }
        })
        /*binding.btnAttachQrCode.setOnClickListener {
            callBack?.replaceFragment(ATTACH_NEW_QR)
        }
        binding.btnMarkMissingBag.setOnClickListener {
            callBack?.replaceFragment(MISSING_BAG)
        }*/
    }

    private fun isBagAvailableInDOBags(doBags: List<DOBag>, bag: Bag) : Boolean {
        var bagFound: Bag? = null
        doBags.forEach {
            if (it.bagQrCode == bag.bagQrCode) {
                bagFound = bag
                return@forEach
            }
        }
        return bagFound != null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/blt/BagDetailsBltFragment").title("OD/Receiving")
            .with(tracker)

    }

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            moveFrag: String, transactionId: String
        )
    }

    private fun setUpAdapter(
        data: MutableList<TransactionDetail>? = null,
        dataOffline: List<DOTxnDetail>? = null
    ) {
        if (data != null) {
            //val transaction1 = data as MutableList<DOTransactionDetail>
            binding.rvTransaction.setUpAdapter(
                data,
                R.layout.fragment_bag_details_blt,
                FragmentBagDetailsBltBinding::inflate,
                { it, pos, bindingItem ->
                    var transcationId = it.transactionId
                    bindingItem.tvTxnId.text = it.transactionId
                    //vm.getDOBags(it.transactionId, true)
                    bindingItem.btnAttachQrCode.setOnClickListener {
                        callBack?.replaceFragment(ATTACH_NEW_QR, transcationId)
                    }
                    bindingItem.btnMarkMissingBag.setOnClickListener {
                        callBack?.replaceFragment(MISSING_BAG, transcationId)
                    }
                    //var scannedBagCount: Int =0
                    //var missingBagCount:Int  =0
                    //var missedBagWeight: Double=0.0
                    var scannedBagWeight = 0.0
                    var scannedBagCount =
                        mDoBag.filter { it.transactionId.contains(transcationId) }
                            .filter { it.isScanned }.count()
                    mDoBag.filter { it.transactionId.contains(transcationId) }
                        .filter { it.isScanned }.forEach { bag ->
                            if (bag.weight != null && bag.weight != "")
                                scannedBagWeight += bag.weight!!.toDouble()
                        }

                    var missingBagCount =
                        mDoBag.filter { it.transactionId.contains(transcationId) }
                            .filter { !it.isScanned }.count()
                    var missedBagWeight: Double = it.netWeight.toDouble() - scannedBagWeight

                    bindingItem.tvScannedBagValue.text = scannedBagCount.toString()
                    var countryCode = PreferenceHelper.get(Constants.COUNTRY_CODE, "")
                    if (countryCode.equals(CountryCode.GUATEMALA.code) || countryCode.equals(
                            CountryCode.HONDURAS.code
                        )
                    ) {
                        bindingItem.tvScannedBagWeight.text =
                            "${scannedBagWeight.formatThreeDigits()} QQ"
                        bindingItem.tvMissingBagWeight.text =
                            "${missedBagWeight.formatThreeDigits()} QQ"
                    } else {
                        bindingItem.tvScannedBagWeight.text =
                            "${scannedBagWeight.formatThreeDigits()} KG"
                        bindingItem.tvMissingBagWeight.text =
                            "${missedBagWeight.formatThreeDigits()} KG"
                    }

                    bindingItem.tvMissedBagValue.text = missingBagCount.toString()
                }, {

                })
        }
    }
}


