package com.olam.warehouse.vegax.offloadingcameroon.ui.supplier

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentVegaCameroonOffloadingWeightEntryBinding
import com.olam.warehouse.vegax.offloadingcameroon.utils.BAG_MATERIAL
import com.olam.warehouse.vegax.offloadingcameroon.utils.MATERIAL_NAME
import com.olam.warehouse.vegax.offloadingcameroon.utils.UNITS_OF_MEASURE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger

/**
 * Created by Keerthi Santhanam on 22/6/2020.
 */
class VegaCameroonOffloadingWeightEntryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_vega_cameroon_offloading_weight_entry
    private val vm: TransactionViewModel by viewModel()
    private lateinit var binding: FragmentVegaCameroonOffloadingWeightEntryBinding
    private var callBack: CallBackAddBags? = null
    private var material: String = ""
    private var uom: String = ""
    var bagMaterial = VegaEcuadorOffloadingBagMaterial()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var tareWeight: String = "0"
    private var palletTarweight: String = "0"
    private var tareWeight1: String = "0"
    private var bagWeight1: Double = 0.0
    private var bagWeight2: Double = 0.0
    private var grossWeight: Double = 0.0

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private lateinit var mInputStream: InputStream
    private lateinit var mOutputStream: OutputStream
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight: String = ""

    companion object {
        fun newInstance(bundle: Bundle) = VegaCameroonOffloadingWeightEntryFragment().putArgs {
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
        binding = FragmentVegaCameroonOffloadingWeightEntryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("offloadingcameroon/ui/supplier/VegaCameroonOffloadingWeightEntryFragment")
            .title("Vega_Cameroon/Offloading")
            .with(tracker)
    }

    @SuppressLint("MissingPermission")
    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        val bundle = arguments?.getBundle("DATA")
        material = bundle?.getString(MATERIAL_NAME).toString()
        uom = bundle?.getString(UNITS_OF_MEASURE).toString()
        bagMaterial = bundle?.getParcelable(BAG_MATERIAL) ?: VegaEcuadorOffloadingBagMaterial()
        palletTarweight = bundle?.getString(Constants.PALLET_AVG).toString()

        binding.tvLotValue.text = material

        if (bagMaterial.grossWeight.isNotEmpty() && !bagMaterial.grossWeight.equals("0")) {
            binding.etWeight.setText(bagMaterial.grossWeight)
            binding.etEnterWeight.text =
                bagMaterial.netWeight.toDouble().formatThreeDigits().replace(",", "")
            binding.tvSelectType.text = bagMaterial.bagType
            binding.tvSelectType1.text = bagMaterial.bagType1
            binding.etNoBags.setText(if (bagMaterial.bagCount.equals("0")) "" else bagMaterial.bagCount)
            binding.etNoBags1.setText(if (bagMaterial.bagCount1.equals("0")) "" else bagMaterial.bagCount1)
            tareWeight = bagMaterial.tareWeight.toString()
            tareWeight1 = bagMaterial.tareWeight1.toString()
            if (bagMaterial.grossWeight != "0") grossWeight = bagMaterial.grossWeight.toDouble()
            if (bagMaterial.bagCount != "0") bagWeight1 =
                bagMaterial.bagCount.toInt().times(((bagMaterial.tareWeight) ?: "0.0").toDouble())
            if (bagMaterial.bagCount1 != "0") bagWeight2 =
                (bagMaterial.bagCount1 ?: "0").toInt()
                    .times((bagMaterial.tareWeight1 ?: "0").toDouble())
            updateProceed(true)
        }
        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()
        binding.btAdd.setOnClickListener { validate() }
        binding.etNoBags.onChange {
            if (binding.tvSelectType.text.toString().isEmpty()) {
                showSnack(getString(com.olam.warehouse.login.R.string.select_bag_type))
                bagWeight1 = 0.0
            } else if (it.isEmpty()) {
                bagWeight1 = 0.0
            } else {
                val data =
                    bagTypeList.filter { it.bagType.equals(binding.tvSelectType.text.toString()) }
                val bagcount = it.toInt()
                bagWeight1 = bagcount.times((data[0].tareWeight ?: "0").toDouble())
                bagMaterial.tareWeight = data[0].tareWeight
            }
            updateBagCount()
            /*  when {
                  text.isEmpty() -> {
                      binding.ivCountClose.gone()
                  }
                  else -> {
                      binding.ivCountClose.visible()

                      val grossWeight =
                          if (binding.etWeight.text.toString().isEmpty()) 0.0 else binding.etWeight.text.toString().toDouble()
                      binding.etEnterWeight.text = grossWeight
                          .minus(text.toInt().times(tareWeight.toDouble()).plus(palletTarweight.toDouble()))
                          .formatThreeDigits().replace(",", "")
                          .toString()
                  }
              }*/
        }
        binding.etNoBags1.onChange {
            if (binding.tvSelectType1.text.toString().isEmpty()) {
                showSnack(getString(com.olam.warehouse.login.R.string.select_bag_type))
                bagWeight2 = 0.0
            } else if (it.isEmpty()) {
                bagWeight2 = 0.0
            } else {
                val data =
                    bagTypeList.filter { it.bagType.equals(binding.tvSelectType1.text.toString()) }
                val bagcount1 = it.toInt()
                bagWeight2 = bagcount1.times((data[0].tareWeight ?: "0").toDouble())
                bagMaterial.tareWeight1 = data[0].tareWeight
            }
            updateBagCount()
        }

        binding.tvSelectType.setOnClickListener { showBagTypeDialog(0, bagTypeList) }
        binding.tvSelectType1.setOnClickListener { showBagTypeDialog(1, bagTypeList) }

        binding.etWeight.onChange { text ->
            when {
                text.isEmpty() -> {
                    updateProceed(false)
                    binding.ivGrossWeight.gone()
                    grossWeight = 0.0
                    updateBagCount()
                }
                else -> {
                    if(text.toDouble() <= 0)
                    {
                        updateProceed(false)
                        binding.ivGrossWeight.gone()
                        grossWeight = 0.0
                    }
                    else
                    {
                        binding.ivGrossWeight.visible()
                        // if (binding.etNoBags.text.toString().isNotEmpty() && binding.tvSelectType.text.toString().isNotEmpty()) {
                        updateProceed(true)
                        grossWeight = text.toDouble()

                    }
                    updateBagCount()
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
            updateProceed(false)
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

    private fun showBagTypeDialog(bag: Int, data: List<VegaPackageMaterial>) {

        val bagTypes = data.map { data1 -> data1.bagType }
        MaterialDialog(requireContext()).show {
            title(com.olam.warehouse.login.R.string.bag_types)
            listItemsSingleChoice(items = bagTypes) { _, index, text ->
                when (bag) {
                    0 -> {
                        binding.tvSelectType.setText(text, TextView.BufferType.EDITABLE)
                        bagMaterial.bagType = data[index].bagType
                        // bagMaterial.bagMaterialCode = data[index].bagMaterialCode
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
                        // bagMaterial.bagMaterialCode1 = data[index].bagMaterialCode

                    }
                }

            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.login.R.string.ok),
                    true
                )
            )
        }
    }
    /*private fun showBagTypeDialog(data: List<VegaPackageMaterial>) {
        val bagTypes = data.map { data1 -> data1.bagType }
        MaterialDialog(requireContext()).show {
            title(com.olam.warehouse.presentation.R.string.select_bag_type)
            listItemsSingleChoice(items = bagTypes) { _, index, text ->
                binding.tvSelectType.text = text
                bagMaterial.bagType = data[index].bagType
                bagMaterial.unitsOfMeasure = data[index].unitsOfMeasure
                if (bagMaterial.tareWeight?.isEmpty()!!) {
                    bagMaterial.tareWeight = data[index].tareWeight
                    when (bagMaterial.unitsOfMeasure) {
                        "KG" -> tareWeight = bagMaterial.tareWeight!!.toDouble().formatThreeDigits()
                        "MT" -> tareWeight = convertMtToKg(bagMaterial.tareWeight!!.toDouble().formatThreeDigits())

                    }
                } else {
                    when (bagMaterial.unitsOfMeasure) {
                        "KG" -> tareWeight =
                            data[index].tareWeight?.toDouble()?.formatThreeDigits().toString()
                        "MT" -> tareWeight =
                            convertMtToKg(data[index].tareWeight?.toDouble()?.formatThreeDigits().toString())

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
    }*/

    private fun validate() {

        when {
            binding.etWeight.text.isNullOrEmpty() && binding.etWeight.text.toString().toBigInteger() == BigInteger.ZERO -> showSnack(getString(R.string.enter_gross))
            binding.etEnterWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_net))
            binding.etEnterWeight.text.toString().toDouble() <=0 -> showSnack(getString(R.string.enter_valid_net))
            binding.tvSelectType.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_bag_type))
            binding.etNoBags.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_bag_number))
            else -> moveBackToPallet()
        }
    }

    private fun moveBackToPallet() {
        bagMaterial.grossWeight = binding.etWeight.text.toString()
        bagMaterial.netWeight = binding.etEnterWeight.text.toString()
        bagMaterial.palletAverage = palletTarweight
        bagMaterial.bagCount =
            if (binding.etNoBags.text.toString()
                    .isEmpty()
            ) "0" else binding.etNoBags.text.toString()
        bagMaterial.bagCount1 =
            if (binding.etNoBags1.text.toString()
                    .isEmpty()
            ) "0" else binding.etNoBags1.text.toString()
        activity?.onBackPressed()
        callBack?.updateBagWeight(bagMaterial)
    }

    /* @SuppressLint("MissingPermission")
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

     @SuppressLint("MissingPermission")
     private fun getPairedDevices() {
         val pairedMac = PreferenceHelper.get(BT_MAC, "")
         if (pairedMac.isBlank()) {
             showPairedDeviceDialog()
         } else {
             showLoading()
             try {
                 val device = mBTAdapter.getRemoteDevice(pairedMac)
                 mBTSocket = device.createRfcommSocketToServiceRecord(mUUID)
                 mBTSocket?.let {

                     if (it.isConnected) it.close()
                     it.connect()
                     if (it.isConnected) {
                         hideLoading()
                         activity?.toast("Weighscale connected successfully")
                         try {
                             DoAsync {
                                 mInputStream = it.inputStream
                                 var data = ""
                                 var bytes: Int
                                 while (true) {
                                     try {

                                         val buffer = ByteArray(1024)
                                         mInputStream.read(buffer, 0, buffer.size)
                                         data = String(buffer, 0, buffer.size, Charsets.UTF_8)
                                         val dataArr = data.split("\r\n", "\r", "\n", " ")
                                         dataArr.forEach {
                                             data = Regex("[^0-9.]").replace(it, "")
                                             mWeight = data
                                             runOnUiThread {
                                                 try {
                                                     if (mWeight.toDouble() > 0 && !mWeight.equals(
                                                             binding.etWeight.text
                                                         )
                                                     ) {
                                                         binding.etWeight.setText(mWeight.toString())
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
                 hideLoading()
             }
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
             positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok))) {
                 dismiss()
             }
         }
     }

     override fun onDestroy() {
         super.onDestroy()
         try {
             mBTSocket?.close()
         } catch (e: Exception) {
             e.printStackTrace()
         }
     }*/

    @SuppressLint("MissingPermission")
    fun updateBtWeight(btValue: String) {
        mWeight = btValue
        /* try {
             if (btValue.toDouble() > 0 && !btValue.equals(binding.etWeight.text)) {
                 binding.etWeight.setText(btValue)
 //                grossWeight = btValue.toDouble()
 //                updateProceed(true)
 //                updateBagCount()
             }
         } catch (e: Exception) {
             e.printStackTrace()
         }*/
        // binding.etWeight.isEnabled = !mBTAdapter.isEnabled
    }

    private fun updateBagCount() {
        binding.etEnterWeight.text = grossWeight
            .minus(bagWeight1.plus(bagWeight2))
            .formatThreeDigits().replace(",", "")
    }
}
