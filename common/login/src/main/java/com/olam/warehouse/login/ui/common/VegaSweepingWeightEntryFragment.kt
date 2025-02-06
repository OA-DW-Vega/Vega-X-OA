package com.olam.warehouse.login.ui.common

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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentWeightEntryBinding
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.work.convertKgToMTNigeria
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class VegaSweepingWeightEntryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_weight_entry
    private val vm: TransactionViewModel by viewModel()
    private lateinit var binding: FragmentWeightEntryBinding
    private var callBack: CallBackAddBags? = null
    private var material: String = ""
    private var manualEntryDisable: Boolean = false
    private var lotId: String = ""
    private var title: String = ""
    var bagMaterial = VegaCocoaSweepingBagMaterial()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var tareWeight: String = "0"
    private var tareWeight1: String = "0"
    private var palletTarweight: String = "0"
    private var bagWeight1: Double = 0.0
    private var bagWeight2: Double = 0.0
    private var grossWeight: Double = 0.0
    private var isExtend = false
    private var isValueChanged = false

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private lateinit var mInputStream: InputStream
    private lateinit var mOutputStream: OutputStream
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight = ""
    private var isBagNeedLimit = true
    private var batchNumber: String = ""
    private var hideLot: Boolean = false
    private var defaulted: Boolean? = false

    companion object {
        fun newInstance(bundle: Bundle, isLimited: Boolean, lotHide: Boolean = false) =
            VegaSweepingWeightEntryFragment().putArgs {
                putBundle("DATA", bundle)
                putBoolean("isLimitBagSize", isLimited)
                putBoolean("isLotHide", lotHide)
            }
    }

    interface CallBackAddBags {
        fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial)
    }

    interface CallBackIndiaCoffeeAddBags {
        fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBackAddBags
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWeightEntryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
//        initBt()
    }

    private fun initUI() {
        updateProceed(true)
        binding.fabBt.shrink()
        val bundle = arguments?.getBundle("DATA")
        lotId = bundle?.getString(Constants.SELECTED_STOCKS_ID).toString()
        material = bundle?.getString(Constants.MATERIAL_NUMBER).toString()
        manualEntryDisable = bundle?.getBoolean(Constants.DISABLE_MANUAL_ENTRY, false)?:false
        palletTarweight = bundle?.getString(Constants.PALLET_AVG).toString()
        title = bundle?.getString(Constants.TITLE).toString()
        isBagNeedLimit = arguments?.getBoolean("isLimitBagSize", true) ?: true
        hideLot = arguments?.getBoolean("isLotHide", false) ?: false
        binding.tvLot.text = bundle?.getString(Constants.LOT_LABEL) ?: getString(R.string.po_no)
        bagMaterial =
            bundle?.getParcelable(Constants.BAG_MATERIAL) ?: VegaCocoaSweepingBagMaterial()
        batchNumber = bundle?.getString(Constants.BATCH_NUMBER, "") ?: ""
        val uom = bundle?.getString(Constants.UOM)
        defaulted = bundle?.getBoolean(Constants.DEFAULT)
        if (getCurrentKey().contains("IV_COCO") || getCurrentKey().contains("GH_CASH") || getCurrentKey().contains(
                "VEGA_ID"
            )|| getCurrentKey().contains("EC_COCO") || getCurrentKey().contains("VEGA_NI")
        ) {
            binding.tvSelectType1.gone()
            binding.etNoBags1.gone()
        }
        if (uom == "MT") {
            binding.tvWeightUOM.text = uom
            binding.tvNetWeightUOM.text = uom
            binding.tvLot.text = getString(R.string.lot_id_new)
        }else if (uom == "QE") {
            binding.tvWeightUOM.text = uom
            binding.tvNetWeightUOM.text = uom
        }
        val lable = bundle?.getString(Constants.MATERIAL_LABEL)
        if (hideLot) {
            binding.tvLot.visibility = View.GONE
            binding.tvLotValue.visibility = View.GONE
        }
        if (!lable.isNullOrEmpty()) {
            binding.tvDefaultLotLbl.text = lable
        }
        when (material.isNotEmpty()) {
            true -> {
                binding.clMaterial.visible()
                // binding.tvLot.text = getString(R.string.po_no)
            }
            else -> binding.clMaterial.gone()
        }
        binding.tvMaterial.text = material

        // binding.etWeight.isEnabled = !mBTAdapter.isEnabled
        if (/*bagMaterial.bagType.isNotEmpty() && */bagMaterial.grossWeight.isNotEmpty() && !bagMaterial.grossWeight.equals(
                "0"
            )/* && bagMaterial.bagCount.isNotEmpty()*/) {
            binding.etWeight.setText(bagMaterial.grossWeight)
            binding.etEnterWeight.text =
                (if (bagMaterial.netWeight.isNotEmpty()) bagMaterial.netWeight.toDouble() else 0.0).formatThreeDigits()
                    .replace(",", "")
            binding.tvSelectType.text = bagMaterial.bagType
            binding.tvSelectType1.text = bagMaterial.bagType1
            binding.etNoBags.setText(if (bagMaterial.bagCount.equals("0")) "" else bagMaterial.bagCount)
            binding.etNoBags1.setText(if (bagMaterial.bagCount1.equals("0")) "" else bagMaterial.bagCount1)

            if (uom == "MT") {
                tareWeight =  if (bagMaterial.unitsOfMeasure.equals("KG")) convertKgToMTNigeria(bagMaterial.tareWeight.toString().trim()) else bagMaterial.tareWeight.toString().trim()
                tareWeight1 =  if (bagMaterial.unitsOfMeasure.equals("KG")) convertKgToMTNigeria(bagMaterial.tareWeight1.toString().trim()) else bagMaterial.tareWeight1.toString().trim()
            }else{
                tareWeight = bagMaterial.tareWeight.toString()
                tareWeight1 = bagMaterial.tareWeight1.toString()
            }


            if (bagMaterial.grossWeight != "0") grossWeight =
                if (bagMaterial.grossWeight.isNotEmpty()) bagMaterial.grossWeight.toDouble() else 0.0
            if (bagMaterial.bagCount != "0") bagWeight1 = bagMaterial.bagCount.toInt().times(
                (if (bagMaterial.tareWeight?.isNotEmpty() == true) (bagMaterial.tareWeight)?.toDouble() else 0.0)
                    ?: 0.0
            )
            if (bagMaterial.bagCount1 != "0") bagWeight2 = bagMaterial.bagCount1!!.toInt().times(
                (if (bagMaterial.tareWeight1?.isNotEmpty() == true) (bagMaterial.tareWeight1)?.toDouble() else 0.0)
                    ?: 0.0
            )
            updateProceed(true)
        }

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
            if (bagTypeList.size == 1) {
                binding.tvSelectType.setText(bagTypeList[0].bagType, TextView.BufferType.EDITABLE)
                bagMaterial.bagType = bagTypeList[0].bagType
                bagMaterial.bagMaterialCode = bagTypeList[0].bagMaterialCode
            }
            /* if (defaulted!!) {
                bagTypeList.forEach {
                     binding.tvSelectType.text = it.bagType
                     bagMaterial.bagType = it.bagType
                     bagMaterial.unitsOfMeasure = it.unitsOfMeasure
                     if (bagMaterial.tareWeight?.isEmpty()!!) {
                         bagMaterial.tareWeight = it.tareWeight
                         var tWeight = bagMaterial.tareWeight!!.toDouble()
                         when (bagMaterial.unitsOfMeasure) {
                             "KG" -> tareWeight = tWeight.toString()
 //                            .toDouble().div(1000).formatNDigits(4)
                             "MT" -> tareWeight = tWeight.toString()
                         }
                     } else {
                         var tWeightE = it.tareWeight!!.toDouble()
                         when (bagMaterial.unitsOfMeasure) {
                             "KG" -> tareWeight =
                                 tWeightE.toString()
 //                                ?.toDouble()?.div(1000)?.formatNDigits(4)!!
                             "MT" -> tareWeight =
                                 tWeightE.toString()
                         }
                         bagMaterial.tareWeight = tareWeight
                     }
                     var noBag =
                         if (binding.etNoBags.text.toString().isEmpty()) 0 else binding.etNoBags.text.toString().toInt()
                 noBag= if(binding.etNoBags1.text.toString().isEmpty()) 0 else binding.etNoBags1.text.toString().toInt()
                     if (binding.etWeight.text.toString().isNotEmpty()) {
                         binding.etEnterWeight.text =
                             binding.etWeight.text.toString().toDouble()
                                 .minus(
                                     noBag.times(tareWeight.toDouble()).plus(
                                         palletTarweight.toDouble()
                                     )
                                 ).formatThreeDigits()
                     }
                 }
               *//*  if (bagTypeList.size == 1) {
                    binding.tvSelectType.text = bagTypeList[0].bagType
                    bagMaterial.bagType = bagTypeList[0].bagType
                    bagMaterial.unitsOfMeasure = bagTypeList[0].unitsOfMeasure
                    if (bagMaterial.tareWeight?.isEmpty()!!) {
                        bagMaterial.tareWeight = bagTypeList[0].tareWeight
                        var tWeight = bagMaterial.tareWeight!!.toDouble()
                        when (bagMaterial.unitsOfMeasure) {
                            "KG" -> tareWeight = tWeight.toString()
//                            .toDouble().div(1000).formatNDigits(4)
                            "MT" -> tareWeight = tWeight.toString()
                        }
                    } else {
                        var tWeightE = bagTypeList[0].tareWeight!!.toDouble()
                        when (bagMaterial.unitsOfMeasure) {
                            "KG" -> tareWeight =
                                tWeightE.toString()
//                                ?.toDouble()?.div(1000)?.formatNDigits(4)!!
                            "MT" -> tareWeight =
                                tWeightE.toString()
                        }
                        bagMaterial.tareWeight = tareWeight
                    }
                    val noBag =
                        if (binding.etNoBags.text.toString().isEmpty()) 0 else binding.etNoBags.text.toString().toInt()
                    if (binding.etWeight.text.toString().isNotEmpty()) {
                        binding.etEnterWeight.text =
                            binding.etWeight.text.toString().toDouble()
                                .minus(
                                    noBag.times(tareWeight.toDouble()).plus(
                                        palletTarweight.toDouble()
                                    )
                                ).formatThreeDigits()
                    }
                }*//*
            }*/
        })
        vm.getMaterials()
        binding.tvLotValue.text = lotId
        binding.tvTitle.text = title
        binding.btAdd.setOnClickListener { validate() }
        binding.etNoBags.onChange {
            isValueChanged = true
            if (binding.tvSelectType.text.toString().isEmpty()) {
                showSnack(getString(R.string.select_bag_type))
                bagWeight1 = 0.0
            } else if (it.isEmpty()) {
                bagWeight1 = 0.0
            } else {
                val data =
                    bagTypeList.filter { it.bagType.equals(binding.tvSelectType.text.toString()) }
                val bagcount = it.toInt()
                if (uom == "MT") {
                    var tareWeightBag =
                        if ((data[0].unitsOfMeasure).equals("KG")) convertKgToMTNigeria(
                            (data[0].tareWeight).toString().trim()
                        ) else (data[0].tareWeight).toString().trim()
                    bagWeight1 =
                        ((bagcount.times((tareWeightBag).toDouble())).formatThreeDigits()).toDouble()
                    bagMaterial.tareWeight = tareWeightBag
                }else{
                    bagWeight1 = bagcount.times((data[0].tareWeight)?.toDouble() ?: 0.0)
                    bagMaterial.tareWeight = data[0].tareWeight
                }
            }
            updateBagCount()
            /*  when {
                  text.isEmpty() -> {
                      binding.ivCountClose.gone()
                      //updateProceed(false)
                  }
                  else -> {
                      binding.ivCountClose.visible()
                      *//* if (binding.etWeight.text.toString().isNotEmpty() && binding.tvSelectType.text.toString().isNotEmpty()) {
                         updateProceed(true)*//*
                    if (isBagNeedLimit) {
                        if (text.toInt() > 40) {
                            showSnack(getString(R.string.max_bag_count))
                        } else updateBagCount(text)
                    } else {
                        updateBagCount(text)
                    }
                    // }
                }
            }*/
        }
        binding.etNoBags1.onChange {
            isValueChanged = true
            if (binding.tvSelectType1.text.toString().isEmpty()) {
                showSnack(getString(R.string.select_bag_type))
                bagWeight2 = 0.0
            } else if (it.isEmpty()) {
                bagWeight2 = 0.0
            } else {
                val data =
                    bagTypeList.filter { it.bagType.equals(binding.tvSelectType1.text.toString()) }
                val bagcount = it.toInt()
                if (data.isNotEmpty()) {
                    if (uom == "MT") {
                        var tareWeightBag =
                            if ((data[0].unitsOfMeasure).equals("KG")) convertKgToMTNigeria(
                                (data[0].tareWeight).toString().trim()
                            ) else (data[0].tareWeight).toString().trim()
                        bagWeight2 =
                            ((bagcount.times((tareWeightBag).toDouble())).formatThreeDigits()).toDouble()
                        bagMaterial.tareWeight1 = tareWeightBag
                    }else{
                        bagWeight2 = bagcount.times((data[0].tareWeight)?.toDouble() ?: 0.0)
                        bagMaterial.tareWeight1 = data[0].tareWeight
                    }
                }
            }
            updateBagCount()
        }

        binding.tvSelectType.setOnClickListener { showBagTypeDialog(0, bagTypeList) }
        binding.tvSelectType1.setOnClickListener { showBagTypeDialog(1, bagTypeList) }

        binding.etWeight.onChange { text ->
            isValueChanged = true
            when {
                text.isEmpty() -> {
                    updateProceed(false)
                    binding.ivGrossWeight.gone()
                    grossWeight = 0.0
                    updateBagCount()
                }
                else -> {
                    binding.ivGrossWeight.visible()
                    // if (binding.etNoBags.text.toString().isNotEmpty() && binding.tvSelectType.text.toString().isNotEmpty()) {
                    updateProceed(true)
                    if (!text.equals(".")) {
                        grossWeight = text.toDouble()
                        updateBagCount()
                    }

                    /*var noBag =
                           if (binding.etNoBags.text.toString().isEmpty()) 0 else binding.etNoBags.text.toString().toInt()
                           if (binding.etNoBags1.text.toString().isEmpty()) 0 else binding.etNoBags1.text.toString().toInt()
                    binding.etEnterWeight.text = text.toDouble()
                        .minus(
                            noBag.times(tareWeight.toDouble()).plus(
                                palletTarweight.toDouble()
                            )
                        ).formatThreeDigits()*/
                    //.toString()
                    //}
                }

            }
        }

        binding.ivGrossWeight.setOnClickListener {
            binding.etWeight.setText("")
            binding.ivGrossWeight.gone()
        }

        binding.ivCountClose.setOnClickListener {
            binding.etNoBags.setText("")
            binding.ivCountClose.gone()
        }
        binding.ivCountClose1.setOnClickListener {
            binding.etNoBags1.setText("")
            binding.ivCountClose1.gone()
        }

        binding.tvPullWeight.setOnClickListener {
            binding.etWeight.error = null
            binding.etWeight.setText(mWeight.toString())
            if (binding.etWeight.text.isNullOrEmpty())
                //context?.sendBroadcast(Intent(Constants.IS_BT_CONNECTED))
                context?.sendBroadcast(
                    Intent(Constants.IS_BT_CONNECTED).apply {
                        setPackage(context?.packageName)
                    }
                )
        }
        if (mBTAdapter.isEnabled) binding.fabBt.visible()
        binding.fabBt.setOnClickListener {
            isExtend = !isExtend
            if (isExtend) {
                binding.fabBt.extend()
                //context?.sendBroadcast(Intent(Constants.SWITCH_BT_DEVICE))
                context?.sendBroadcast(
                    Intent(Constants.SWITCH_BT_DEVICE).apply {
                        setPackage(context?.packageName)
                    }
                )
            } else {
                binding.fabBt.shrink()
            }
        }
        if(manualEntryDisable){
            binding.etWeight.isEnabled = false
        }
    }

    private fun updateBagCount() {
        /*val grossWeight =
             if (binding.etWeight.text.toString().isEmpty()) 0.0
             else binding.etWeight.text.toString()
                 .toDouble()*/
        /* binding.etEnterWeight.text = grossWeight
            .minus(text.toInt().times(tareWeight.toDouble()).plus(palletTarweight.toDouble()))
            .formatThreeDigits().replace(",", "")
            .toString()*/
        binding.etEnterWeight.text = grossWeight
            .minus(
                bagWeight1.plus(bagWeight2)
                    .plus(if (palletTarweight.isNotEmpty()) palletTarweight.toDouble() else 0.0)
            )
            .formatThreeDigits().replace(",", "")
    }
    private fun updateProceed(flag: Boolean) {
        when (flag) {
            false -> {
                binding.btAdd.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.btAdd.context,
                        com.olam.warehouse.presentation.R.color.grey
                    )
                )
                binding.btAdd.isEnabled = false
            }
            true -> {
                binding.btAdd.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.btAdd.context,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                )
                binding.btAdd.isEnabled = true
            }
        }
    }

    private fun showBagTypeDialog(bag: Int, data: List<VegaPackageMaterial>) {

        val bagTypes = data.map { data1 -> data1.bagType }
        MaterialDialog(requireContext()).show {
            title(R.string.bag_types)
            listItemsSingleChoice(items = bagTypes) { _, index, text ->
                when (bag) {
                    0 -> {
                        binding.tvSelectType.setText(text, TextView.BufferType.EDITABLE)
                        bagMaterial.bagType = data[index].bagType
                        bagMaterial.bagMaterialCode = data[index].bagMaterialCode
                        isValueChanged = true
                        /* val noBag =
                               if (binding.etNoBags.text.toString().isEmpty()) 0 else binding.etNoBags.text.toString().toInt()
                           bagMaterial.bagType = data[index].bagType
                           bagMaterial.unitsOfMeasure = data[index].unitsOfMeasure
                           bagMaterial.tareWeight = data[index].tareWeight
                           bagMaterial.bagMaterialCode = data[index].bagMaterialCode
                           tareWeight = bagMaterial.tareWeight.toString()
                           binding.etEnterWeight.text =
                               (if (binding.etWeight.text.toString().isNotEmpty()) binding.etWeight.text.toString()
                                   .toDouble() else 0.0)
                                   .minus(
                                       noBag.times(tareWeight.toDouble()).plus(
                                           palletTarweight.toDouble()
                                       )
                                   ).formatThreeDigits().toString()*/
                    }
                    1 -> {
                        binding.tvSelectType1.setText(text, TextView.BufferType.EDITABLE)
                        bagMaterial.bagType1 = data[index].bagType
                        bagMaterial.bagMaterialCode1 = data[index].bagMaterialCode
                        isValueChanged = true
                        /* val   noBag= if (binding.etNoBags1.text.toString().isEmpty()) 0 else binding.etNoBags1.text.toString().toInt()
                            bagMaterial.bagType = data[index].bagType
                            bagMaterial.unitsOfMeasure = data[index].unitsOfMeasure
                            bagMaterial.tareWeight = data[index].tareWeight
                            bagMaterial.bagMaterialCode = data[index].bagMaterialCode
                            tareWeight = bagMaterial.tareWeight.toString()
                            binding.etEnterWeight.text =
                                (if (binding.etEnterWeight.text.toString().isNotEmpty()) binding.etEnterWeight.text.toString()
                                    .toDouble() else 0.0)
                                    .minus(
                                        noBag.times(tareWeight.toDouble()).plus(
                                            palletTarweight.toDouble()
                                        )
                                    ).formatThreeDigits().toString()*/
                    }
                }
                //binding.tvSelectType.text = text

                /*if (bagMaterial.tareWeight?.isEmpty()!!) {
                    bagMaterial.tareWeight = data[index].tareWeight
                    when (bagMaterial.unitsOfMeasure) {
                        "EA" -> tareWeight =
                            (bagMaterial.tareWeight!!.toDouble().div(248.58)).div(1000).formatThreeDigits()
                        "KG" -> tareWeight = bagMaterial.tareWeight!!.toDouble().div(1000).formatThreeDigits()
                        "MT" -> tareWeight = bagMaterial.tareWeight!!.toDouble().formatThreeDigits()
                    }
                }
                else {
                    when (bagMaterial.unitsOfMeasure) {
                        "EA" -> tareWeight =
                            (data[index].tareWeight?.toDouble()?.div(248.58))?.div(1000)?.formatThreeDigits().toString()
                        "KG" -> tareWeight =
                            data[index].tareWeight?.toDouble()?.div(1000)?.formatThreeDigits().toString()
                        "MT" -> tareWeight = data[index].tareWeight?.toDouble()?.formatThreeDigits().toString()
                    }
                    bagMaterial.tareWeight = tareWeight
                }*/
                //if (binding.etWeight.text.toString().isNotEmpty() && binding.etNoBags.text.toString().isNotEmpty()) {


                //  updateProceed(true)
                //}
            }
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.ok),
                "",
                {
                    dismiss()
                },
                { dismiss() })
            //positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun validate() {
        when {
            binding.etWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_gross))
            binding.etEnterWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_net))
            binding.tvSelectType.text.isNotEmpty() || binding.etNoBags.text?.isNotEmpty() == true -> {
                if (binding.tvSelectType.text.isEmpty()) showSnack(
                    getString(R.string.enter_bag_type)
                ) else if (binding.etNoBags.text?.isEmpty()!!) {
                    showSnack(getString(R.string.enter_bag_weight))
                }
               else {
                    when {
                        binding.etNoBags.text.toString().isNotEmpty() -> {
                            if (isBagNeedLimit && binding.etNoBags.text.toString().toInt() > 40 && !getCurrentKey().split("_")[1].contains("NI"))
                                showSnack(getString(R.string.max_bag_count))
                            else  moveBackToPallet()
                        }
                        else -> moveBackToPallet()
                    }


                }
            }
           else -> {
                when {
                    binding.etNoBags.text.toString().isNotEmpty() -> {
                        if (isBagNeedLimit && binding.etNoBags.text.toString().toInt() > 40)
                            showSnack(getString(R.string.max_bag_count))
                        else moveBackToPallet()
                    }
                    else -> moveBackToPallet()
                }
            }
        }


    }

    private fun moveBackToPallet() {
        if (!isValueChanged)
            activity?.onBackPressed()
        else {
            bagMaterial.grossWeight = binding.etWeight.text.toString()
            bagMaterial.netWeight = binding.etEnterWeight.text.toString()
            bagMaterial.bagCount = if (binding.etNoBags.text.toString()
                    .isEmpty()
            ) "0" else binding.etNoBags.text.toString()
            bagMaterial.bagCount1 = if (binding.etNoBags1.text.toString()
                    .isEmpty()
            ) "0" else binding.etNoBags1.text.toString()
            if (batchNumber.isNotEmpty()) bagMaterial.batchNumber = batchNumber
            activity?.onBackPressed()
            callBack?.updateBagWeight(bagMaterial)
        }
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            getPairedDevices()
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
                mBTSocket?.close()
                val device = mBTAdapter.getRemoteDevice(pairedMac)
                mBTSocket = device.createRfcommSocketToServiceRecord(mUUID)
                /* if(mBTSocket==null) {
                     showPairedDeviceDialog()
                     return
                 }*/
                mBTSocket?.let {

                    if (it.isConnected) it.close()
                    it.connect()
                    if (it.isConnected) {
                        //  runOnUiThread {
                        hideLoading()
                        activity?.toast("Weighscale connected successfully")
                        //}
                        try {
                            DoAsync {
                                mInputStream = it.inputStream
                                var data = ""
                                var bytes: Int
                                while (true) {
                                    try {
                                        val buffer = ByteArray(1024)
                                        /*bytes = */mInputStream.read(buffer, 0, buffer.size)
                                        data = String(buffer, 0, buffer.size, Charsets.UTF_8)
                                        val dataArr = data.split("\r\n", "\r", "\n", " ")
//                                            runOnUiThread { toast("WeightTest1: $data") }
                                        dataArr.forEach {
                                            data = Regex("[^0-9.]").replace(it, "")
                                            mWeight = data
                                            try {
                                                if (mWeight.toDouble() > 0) data = ""
                                            } catch (e: java.lang.Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    } catch (e: IOException) {
                                        Log.d("GinngBluetoothReadData", e.message ?: "")
                                        break
                                    }
                                }
                            }.execute()
                        } catch (e: Exception) {
                            Log.d("GinngBluetoothReadData", e.message ?: "")
                        }
                    }

                }
            } catch (e: Exception) {
                Log.d("GinngBluetoothConnect", e.message ?: "")
                mBTSocket?.close()
            }
            //}.execute()
        }
    }

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
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok))) {
                dismiss()
            }
        }
    }

    fun updateBtWeight(btValue: String) {
        mWeight = btValue
        try {
            if (btValue.toDouble() > 0 && !btValue.equals(binding.etWeight.text)) {
                binding.etWeight.setText(btValue)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //binding.etWeight.isEnabled = !mBTAdapter.isEnabled
    }
}
