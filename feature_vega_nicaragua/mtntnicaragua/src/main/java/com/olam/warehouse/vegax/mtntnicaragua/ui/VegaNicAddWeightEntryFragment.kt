package com.olam.warehouse.vegax.mtntnicaragua.ui

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
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentNicMtntWeighscaleAddWeightBinding
import com.olam.warehouse.vegax.mtntnicaragua.utils.getColor
import kotlinx.android.synthetic.main.item_vega_nic_mtnt_add_bag.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 11/12/2020.
 */
class VegaNicAddWeightEntryFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_nic_mtnt_weighscale_add_weight
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private lateinit var binding: FragmentNicMtntWeighscaleAddWeightBinding
    private var material: String = ""
    private var uom: String = ""
    var bagMaterial = VegaNicaraguaWeighmentBagMaterial()
    private var bagList = arrayListOf<VegaNicaraguaWeighmentBagMaterial>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var tareWeight: String = "0"
    private var mtntLot: VegaNicDispatchLots? = null

    private var callBack: CallBackAddBags? = null

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private lateinit var mInputStream: InputStream
    private lateinit var mOutputStream: OutputStream
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight = 0.0
    private var bagType = ""

    companion object {
        fun newInstance(vegaNicDispatchLots: VegaNicDispatchLots) = VegaNicAddWeightEntryFragment().putArgs {
            putParcelable("DATA", vegaNicDispatchLots)
        }
    }

    interface CallBackAddBags {
        fun updateBagWeight(bagMaterial: VegaNicaraguaWeighmentBagMaterial)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBackAddBags
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNicMtntWeighscaleAddWeightBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        //initBt()
    }

    private fun initUI() {
        mtntLot = arguments?.getParcelable("DATA") ?: VegaNicDispatchLots()

        /* if (bagMaterial.grossWeight.isNotEmpty() && !bagMaterial.grossWeight.equals("0")) {
             binding.etWeight.setText(bagMaterial.grossWeight)
             binding.etEnterWeight.text = bagMaterial.netWeight.toDouble().formatThreeDigits().replace(",", "")
             binding.tvSelectType.text = bagMaterial.bagType
             binding.etNoBags.setText(if (bagMaterial.bagCount.equals("0")) "" else bagMaterial.bagCount)
             tareWeight = bagMaterial.tareWeight.toString()
             updateProceed(true)
         }*/
        bagType = mtntLot?.bagType.toString()
        binding.tvLotValue.text = mtntLot?.truckNo
        binding.btProceed.setOnClickListener { activity?.onBackPressed() }
        binding.btSave.setOnClickListener { activity?.onBackPressed() }
        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.getBagItems(mtntLot?.batchNumber.toString(), mtntLot?.tempIdWithBatch.toString())

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
            var filteredBagList = bagTypeList.filter { it.bagType == bagType }
            if (filteredBagList.size > 0) {
                var data = filteredBagList[0]
                binding.tvSelectType.text = data.bagType
                binding.tvSelectType.isEnabled = false
                bagMaterial.bagType = data.bagType
                bagMaterial.unitsOfMeasure = data.unitsOfMeasure
                if (bagMaterial.tareWeight?.isEmpty()!!) {
                    bagMaterial.tareWeight = data.tareWeight
                    tareWeight = bagMaterial.tareWeight!!.toDouble().formatThreeDigits()
                } else {
                    tareWeight = data.tareWeight?.toDouble()?.formatThreeDigits().toString()
                    bagMaterial.tareWeight = tareWeight
                }
                val noBag =
                    if (binding.etNoBags.text.toString().isEmpty()) 0 else binding.etNoBags.text.toString().toInt()

                if (binding.etWeight.text.toString().isNotEmpty()) {
                    binding.etEnterWeight.text =
                        binding.etWeight.text.toString().toDouble()
                            .minus(
                                noBag.times(tareWeight.toDouble())
                            ).formatThreeDigits()
                }
            }
        })
        vm.getMaterials()
        binding.btAddWeight.setOnClickListener { validate() }
        binding.etNoBags.onChange { text ->
            when {
                text.isEmpty() -> {
                    binding.ivCountClose.gone()
                }
                else -> {
                    binding.ivCountClose.visible()
                    val grossWeight =
                        if (binding.etWeight.text.toString().isEmpty()) 0.0 else binding.etWeight.text.toString()
                            .toDouble()
                    binding.etEnterWeight.text = grossWeight
                        .minus(text.toInt().times(tareWeight.toDouble()))
                        .formatThreeDigits().replace(",", "")
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
                    if (text.startsWith(".")) {
                        if (text.length > 1) {
                            val value = "0".plus(text)
                            binding.etEnterWeight.text = value.toDouble()
                                .minus(
                                    noBag.times(tareWeight.toDouble())
                                ).formatThreeDigits()
                        }
                    } else {
                    binding.etEnterWeight.text = text.toDouble()
                        .minus(
                            noBag.times(tareWeight.toDouble())
                        ).formatThreeDigits()
                }
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

    private fun updateBagItems(bagItems: List<VegaNicaraguaWeighmentBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
            if (bagItems.size > 0) {
                enableSave(true)
                enableProceed(true)
                updateTotalWeights()
            }
            setUpAdapter(bagList)
        }

    }

    private fun setUpAdapter(bagList: ArrayList<VegaNicaraguaWeighmentBagMaterial>) {
        val bagList1 = arrayListOf<VegaNicaraguaWeighmentBagMaterial>()
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            val dat = bagList.sortedByDescending { it.status }
            bagList1.addAll(dat)
        } else {
            binding.tvNoWeight.visible()
            binding.rvWeight.gone()
        }
        binding.rvWeight.setUp(bagList1.asReversed(), R.layout.item_vega_nic_mtnt_add_bag, { it, pos ->
            if (pos % 2 == 0) {
                clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
            } else {
                clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
            }
            tvSno.text = pos.plus(1).toString()
            tvBag.text = it.bagCount
            tvGrossWeight.text = it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus("KG")
            val avgAvlue = it.palletAverage?.toDouble()?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
            tvTarWeight.text = avgAvlue?.formatThreeDigits().plus(" ").plus("KG")
            tvNetWeight.text =
                it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ").plus("KG")
            ivEdit.setOnClickListener { view ->
                val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                popupMenu.menuInflater.inflate(com.olam.warehouse.login.R.menu.transaction_menu, popupMenu.menu)
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible = false
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible = false
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible = false
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible = true
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible = true
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        com.olam.warehouse.login.R.id.action_edit1 -> {
                            it.netWeight = it.grossWeight.toDouble().minus(avgAvlue).formatThreeDigits()
                            moveBagAddWeight(it)
                        }
                        com.olam.warehouse.login.R.id.action_delete -> {
                            bagList.remove(it)
                            binding.rvWeight.adapter?.notifyItemRemoved(pos)
                            vm.deleteBagDetails(it)
                            if (bagList.size == 0) {
                                enableProceed(false)
                                binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                            }
                        }
                    }
                    true
                }
                popupMenu.show()
            }
        })
    }

    private fun moveBagAddWeight(it: VegaNicaraguaWeighmentBagMaterial) {
        bagMaterial = it
        binding.tvSelectType.text = bagMaterial.bagType
        binding.etNoBags.setText(bagMaterial.bagCount)
        binding.etWeight.setText(bagMaterial.grossWeight)
        binding.etEnterWeight.text = bagMaterial.netWeight.toString()
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
                    tareWeight = bagMaterial.tareWeight!!.toDouble().formatThreeDigits()
                } else {
                    tareWeight = data[index].tareWeight?.toDouble()?.formatThreeDigits().toString()
                    bagMaterial.tareWeight = tareWeight
                }
                val noBag =
                    if (binding.etNoBags.text.toString().isEmpty()) 0 else binding.etNoBags.text.toString().toInt()

                if (binding.etWeight.text.toString().isNotEmpty()) {
                    binding.etEnterWeight.text =
                        binding.etWeight.text.toString().toDouble()
                            .minus(
                                noBag.times(tareWeight.toDouble())
                            ).formatThreeDigits()
                }

            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun validate() {
        when {
            binding.etWeight.text.isNullOrEmpty() -> showSnack(getString(com.olam.warehouse.login.R.string.enter_gross))
            binding.etEnterWeight.text.isNullOrEmpty() -> showSnack(getString(com.olam.warehouse.login.R.string.enter_net))
            !validateExceedLotWeight() -> showSnack(getString(R.string.less_weight_error))
            binding.tvSelectType.text.isNullOrEmpty() || binding.etNoBags.text.isNullOrEmpty() -> {
                if (binding.tvSelectType.text.isNotEmpty() || binding.etNoBags.text?.isNotEmpty()!!) showSnack(
                        getString(
                                com.olam.warehouse.login.R.string.enter_bag_type
                        )
                )
                else addBagsToLot()
            }
            else -> addBagsToLot()
        }

    }

    private fun validateExceedLotWeight(): Boolean {
        val lotWeight = mtntLot?.weight?.toDouble() ?: 0.0
        val addedWeight = (bagList.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }).plus(if (bagMaterial.tmpWbId.isEmpty()) binding.etEnterWeight.text.toString().toDouble() else 0.0)
        return when {
            mtntLot?.unitOfMeasure.equals("kg", true) -> addedWeight <= lotWeight
            mtntLot?.unitOfMeasure.equals("MT", true) -> addedWeight.div(1000) <= lotWeight
            else -> true
        }
    }

    private fun addBagsToLot() {
        bagMaterial.grossWeight = binding.etWeight.text.toString()
        bagMaterial.netWeight = binding.etEnterWeight.text.toString()
        bagMaterial.tareWeight = tareWeight
        bagMaterial.bagCount = if (binding.etNoBags.text.toString().isEmpty()) "0" else binding.etNoBags.text.toString()
        if (mtntLot?.batchNumber?.isNotEmpty() == true) bagMaterial.batchNumber = mtntLot?.batchNumber.toString()
        updateBagWeight(bagMaterial)
        clearValue()
    }

    private fun clearValue() {
        binding.etWeight.setText("")
        binding.etEnterWeight.text = ""
        binding.etNoBags.setText("")
        //binding.tvSelectType.text = ""
    }

    fun updateBagWeight(bagMaterial: VegaNicaraguaWeighmentBagMaterial) {
        var isExistValue = false
        var pos: Int = 0
        bagList.forEachIndexed { index, it ->
            if (it.id == bagMaterial.id) {
                isExistValue = true
                pos = index
            }
        }
        if (!isExistValue) {
            bagMaterial.id = Random.nextInt()
            bagMaterial.status = bagList.size
        } else {
            bagList.removeAt(pos)
        }
        bagList.add(bagMaterial)
        var startTime = ""
        if (bagList.size == 1) startTime = DateUtils.getCurrentTimeInMills().toString()
        bagList.forEach { material ->
            material.message = getString(R.string.stored_locally)
            material.tmpWbId = mtntLot?.tempIdWithBatch.toString()
            material.materialCode = mtntLot?.materialCode.toString()
            vm.saveBagDetails(material)
        }
        this.bagMaterial = VegaNicaraguaWeighmentBagMaterial()
        enableSave(true)
        enableProceed(true)
        updateTotalWeights()
    }

    private fun updateTotalWeights() {
        var grossWeight = 0.0
        var tareWeight = 0.0
        val netWeight: Double
        for (item in bagList) {
            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
        }
        netWeight = grossWeight.minus(tareWeight)
        binding.clNet.tvGrossWeightValue.text = grossWeight.formatThreeDigits().plus(" KG")
        binding.clNet.tvTareWeightValue.text = tareWeight.formatThreeDigits().plus(" KG")
        binding.clNet.tvNetWeightValue.text = netWeight.formatThreeDigits().plus(" KG")
    }

    private fun updateProceed(flag: Boolean) {
        binding.btAddWeight.isEnabled = flag
        when (flag) {
            false -> {
                binding.btAddWeight.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.btAddWeight.context,
                        com.olam.warehouse.presentation.R.color.grey
                    )
                )
            }
            true -> {
                binding.btAddWeight.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.btAddWeight.context,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                )
            }
        }
    }

    private fun enableSave(enable: Boolean) {
        binding.btSave.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }

    private fun enableProceed(enable: Boolean) {
        binding.btProceed.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }

    //Bluetooth
    @SuppressLint("MissingPermission")
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
                                    activity?.toast(getString(com.olam.warehouse.login.R.string.weigh_scale_connected))
                                }
                                while (true) {
                                    try {
                                        bytes = mInputStream.read(buffer)
                                        val data = String(buffer, 0, bytes)
                                        mWeight = data.toDouble()
                                    } catch (e: IOException) {
                                        Log.d("WeighBluetoothReadData", e.message ?: "")
                                        break
                                    }
                                }

                            } catch (e: Exception) {
                                Log.d("WeighBluetoothReadData", e.message ?: "")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("WeighBluetoothReadData", e.message ?: "")
                    mBTSocket?.close()
                }
            }.execute()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showPairedDeviceDialog() {
        mPairedDevices.addAll(mBTAdapter.bondedDevices)
        val pairedDeviceName = mBTAdapter.bondedDevices.map { it.name }
        MaterialDialog(requireContext()).show {
            title(text = getString(com.olam.warehouse.login.R.string.please_select_the_weighing_scale))
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
}
