package com.olam.warehouse.vegax.offloadingghana.ui.supplier

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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.databinding.FragmentVegaOffloadingGhanaWeightEntryBinding
import com.olam.warehouse.vegax.offloadingghana.utils.BAG_MATERIAL
import com.olam.warehouse.vegax.offloadingghana.utils.MATERIAL_NAME
import com.olam.warehouse.vegax.offloadingghana.utils.UNITS_OF_MEASURE
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by Keerthi Santhanam on 22/6/2020.
 */
class VegaGhanaOffloadingWeightEntryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_vega_offloading_ghana_weight_entry
    private val vm: TransactionViewModel by viewModel()
    private lateinit var binding: FragmentVegaOffloadingGhanaWeightEntryBinding
    private var callBack: CallBackAddBags? = null
    private var material: String = ""
    private var uom: String = ""
    var bagMaterial = VegaEcuadorOffloadingBagMaterial()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var tareWeight: String = "0"
    private var palletTarweight: String = "0"

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private lateinit var mInputStream: InputStream
    private lateinit var mOutputStream: OutputStream
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight = 0.0

    companion object {
        fun newInstance(bundle: Bundle) = VegaGhanaOffloadingWeightEntryFragment().putArgs {
            putBundle("DATA", bundle)
        }
    }

    interface CallBackAddBags {
        fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBackAddBags
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaOffloadingGhanaWeightEntryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        //initBt()
    }

    private fun initUI() {
        val bundle = arguments?.getBundle("DATA")
        material = bundle?.getString(MATERIAL_NAME).toString()
        uom = bundle?.getString(UNITS_OF_MEASURE).toString()
        bagMaterial = bundle?.getParcelable(BAG_MATERIAL) ?: VegaEcuadorOffloadingBagMaterial()

        binding.tvLotValue.text = material

        if (bagMaterial.grossWeight.isNotEmpty() && !bagMaterial.grossWeight.equals("0")) {
            binding.etWeight.setText(bagMaterial.grossWeight)
            binding.etEnterWeight.text = bagMaterial.netWeight.toDouble().formatThreeDigits().replace(",", "")
            binding.tvSelectType.text = bagMaterial.bagType
            binding.etNoBags.setText(if (bagMaterial.bagCount.equals("0")) "" else bagMaterial.bagCount)
            tareWeight = bagMaterial.tareWeight.toString()
            updateProceed(true)
        }
        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList()

            if(bagTypeList.size == 1){
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

                if(binding.etWeight.text.toString().isNotEmpty()) {
                    binding.etEnterWeight.text =
                        binding.etWeight.text.toString().toDouble()
                            .minus(
                                noBag.times(tareWeight.toDouble()).plus(
                                    palletTarweight.toDouble()
                                )
                            ).formatThreeDigits()
                }
            }
        })
        vm.getMaterials()
        binding.btAdd.setOnClickListener { validate() }
        binding.etNoBags.onChange { text ->
            when {
                text.isEmpty() -> {
                    binding.ivCountClose.gone()
                }
                else -> {
                    binding.ivCountClose.visible()
                    /* if (binding.etWeight.text.toString().isNotEmpty() && binding.tvSelectType.text.toString().isNotEmpty()) {
                         updateProceed(true)*/
                    val grossWeight =
                        if (binding.etWeight.text.toString().isEmpty()) 0.0 else binding.etWeight.text.toString().toDouble()
                    binding.etEnterWeight.text = grossWeight
                        .minus(text.toInt().times(tareWeight.toDouble()).plus(palletTarweight.toDouble()))
                        .formatThreeDigits().replace(",", "")
                        .toString()
                }
            }
        }

        binding.tvSelectType.setOnClickListener { showBagTypeDialog(bagTypeList) }

        binding.etWeight.onChange { text ->
            when {
                text.isEmpty() -> {
                    updateProceed(false)
                    binding.ivGrossWeight.gone()
                }
                else -> {
                    binding.ivGrossWeight.visible()
                    updateProceed(true)
                    val noBag =
                        if (binding.etNoBags.text.toString().isEmpty()) 0 else binding.etNoBags.text.toString().toInt()
                    binding.etEnterWeight.text = text.toDouble()
                        .minus(
                            noBag.times(tareWeight.toDouble()).plus(
                                palletTarweight.toDouble()
                            )
                        ).formatThreeDigits()
                        .toString()
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

        binding.tvPullWeight.setOnClickListener {
            binding.etWeight.error = null
            binding.etWeight.setText(mWeight.toString())
        }
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

    private fun showBagTypeDialog(data: List<VegaPackageMaterial>) {
        val bagTypes = data.map { data1 -> data1.bagType }
            MaterialDialog(requireContext()).show {
                title(com.olam.warehouse.presentation.R.string.select_bag_type)
                listItemsSingleChoice(items = bagTypes) { _, index, text ->
                    binding.tvSelectType.text = text
                    bagMaterial.bagType = data[index].bagType
                    bagMaterial.unitsOfMeasure = data[index].unitsOfMeasure
                    if (bagMaterial.tareWeight?.isEmpty()!!) {
                        bagMaterial.tareWeight = data[index].tareWeight
                        var tWeight = bagMaterial.tareWeight!!.toDouble()
                        when (bagMaterial.unitsOfMeasure) {
                            "KG" -> tareWeight = tWeight.toString()
//                            .toDouble().div(1000).formatNDigits(4)
                            "MT" -> tareWeight = tWeight.toString()
                        }
                    } else {
                        var tWeightE = data[index].tareWeight!!.toDouble()
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

                    if(binding.etWeight.text.toString().isNotEmpty()) {
                        binding.etEnterWeight.text =
                            binding.etWeight.text.toString().toDouble()
                                .minus(
                                    noBag.times(tareWeight.toDouble()).plus(
                                        palletTarweight.toDouble()
                                    )
                                ).formatThreeDigits()
                    }

                }
                positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }

    }

    private fun validate() {
        when {
            binding.etWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_gross))
            binding.etEnterWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_net))
            binding.tvSelectType.text.isNullOrEmpty() || binding.etNoBags.text.isNullOrEmpty() -> {
                if (binding.tvSelectType.text.isNotEmpty() || binding.etNoBags.text?.isNotEmpty()!!) showSnack(
                    getString(
                        R.string.enter_bag_type
                    )
                )
                else moveBackToPallet()
            }
            else -> moveBackToPallet()
        }

    }

    private fun moveBackToPallet() {
        bagMaterial.grossWeight = binding.etWeight.text.toString()
        bagMaterial.netWeight = binding.etEnterWeight.text.toString()
        bagMaterial.bagCount =
            if (binding.etNoBags.text.toString().isEmpty()) "0" else binding.etNoBags.text.toString()
        activity?.onBackPressed()
        callBack?.updateBagWeight(bagMaterial)
    }

    private fun initBt() {
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

    private fun getPairedDevices() {
        val pairedMac = PreferenceHelper.get(UIUtils.BT_MAC, "")
        if (pairedMac.isBlank()) {
            showPairedDeviceDialog()
        } else {
            showLoading()
            DoAsync {
                try {
                    val device = mBTAdapter.getRemoteDevice(pairedMac)
                    mBTSocket = device.createRfcommSocketToServiceRecord(mUUID)
                    mBTSocket?.let {
                        if (it.isConnected) it.close()
                        it.connect()
                        if (it.isConnected) {
                            try {
                                mInputStream = it.inputStream
                                val buffer = ByteArray(1024)
                                var bytes: Int
                                HandlerUtils.runOnUiThread {
                                    hideLoading()
                                    activity?.toast(getString(R.string.weigh_scale_connected))
                                }
                                while (true) {
                                    try {
                                        bytes = mInputStream.read(buffer)
                                        val data = String(buffer, 0, bytes)
                                        mWeight = data.toDouble()
                                    } catch (e: IOException) {
                                        Log.d("GinngBluetoothReadData", e.message ?: "")
                                        break
                                    }
                                }

                            } catch (e: Exception) {
                                Log.d("GinngBluetoothReadData", e.message ?: "")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("GinngBluetoothConnect", e.message ?: "")
                    mBTSocket?.close()
                }
            }.execute()
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
}
