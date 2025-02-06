package com.olam.warehouse.vegax.offloadingcocoa.ui.transaction

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentVegaCocoaOffloadingNwSummaryBinding
import com.olam.warehouse.vegax.offloadingcocoa.databinding.ItemMtnrSummaryLotCardLayoutBinding
import com.olam.warehouse.vegax.offloadingcocoa.databinding.ItemVegaCocoaOffloadNoWeighmentLotBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcocoa.utils.EDIT_LOT
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.math.absoluteValue

class VegaCocoaNoWeighmentMtnrTransactionSummaryFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_vega_cocoa_offloading_nw_summary
    private lateinit var binding: FragmentVegaCocoaOffloadingNwSummaryBinding
    private var callBack: VegaCoCoaOffloadReplaceFragmentCallback? = null
    private var summaryObj: VegaCoCoaReceiving? = null
    private val vm: VegaCoCoaOffloadingViewModel by viewModel()
    private var batchList = mutableListOf<VegaCoCoaReceiveLots>()
    private var bagList = arrayListOf<VegaCoCoaOffloadingBagMaterial>()
    private var transit: Double = 0.0

    companion object {
        fun newInstance(data: VegaCoCoaReceiving) = VegaCocoaNoWeighmentMtnrTransactionSummaryFragment().putArgs {
            putParcelable("summaryData", data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoCoaOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCocoaOffloadingNwSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("offloadingcocoa/ui/transaction/VegaCocoaNoWeighmentMtnrTransactionSummaryFragment")
            .title("Transaction CoCoa")
            .with(tracker)
    }

    private fun initUi() {
        binding.tvTruckValue.text = summaryObj?.supplierName
        binding.tvDestValue.text =
            summaryObj?.storageLocationCode.plus("-").plus(summaryObj?.storageLocationName)
        binding.tvDateValue.text = summaryObj?.vehicleNumber
        binding.tvDriverNameValue.text = summaryObj?.truckDriverName
        binding.tvStoNoValue.text =
            summaryObj?.purchaseDocNum.plus("-").plus(summaryObj?.purchaseDocDesc)
        binding.tvMaterialName.text = summaryObj?.materialName
        binding.tvStoWeightValue.text =
            summaryObj?.netWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                .plus(summaryObj?.unitsOfMeasure)
        binding.btProceed.text = getString(com.olam.warehouse.presentation.R.string.ok)
        binding.btProceed.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val linearLayoutManager = activity?.let { LinearLayoutManager(it) }
        linearLayoutManager?.orientation = LinearLayoutManager.VERTICAL
        binding.rvLots.layoutManager = linearLayoutManager
        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer { updateOBDDetails(it) })
        vm.getOBDDetails(summaryObj?.delivery!!)

        if (summaryObj?.imagePath?.isNotEmpty()!!) {
            binding.tvSnap.visible()
            binding.ivTicket.setImageBitmap(setScaledBitmap(summaryObj?.imagePath!!))
        }
    }

    fun setScaledBitmap(imagePath: String): Bitmap? {
        try {
            val imageViewWidth = 347
            val imageViewHeight = 413

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(imagePath, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null

    }

    private fun updateOBDDetails(data: VegaCoCoaReceivingMtnrWithLots?) {
        batchList.clear()
        if (data != null) {
            if (data.lineItems.size > 0) {
                data.lineItems.forEach {

                    /*var edWeight = 0.0
                    val bags = data.lineItems.filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1) {
                        if (vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true) vm.vegaCoffeeReceivingData.startTime =
                            DateUtils.getCurrentTimeInMills().toString()
                    }
                    if (bags.size > 0) edWeight = bags[0].bagItem.sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()*/
                    batchList.add(it.lots)
                    bagList.addAll(
                        it.bagItem.filter { it2 -> it2.mtnNumber.equals(it.lots.mtnNumber) }.filter { it1 ->
                            it1.batchNumber.equals(
                                it.lots.batch
                            )
                        }
                    )
                }
                setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
            }
        } else {
            setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
        }
    }

    private fun setUpAdapter(list: ArrayList<VegaCocoaNoWeighmentLot>) {
        val lots = list
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_vega_cocoa_offload_no_weighment_lot,
            ItemVegaCocoaOffloadNoWeighmentLotBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.batchNumber
                bindItem.cbEndLot.visibility = View.GONE
                bindItem.tvEndLot.visibility = View.GONE
                if (item.isEndLot == true) {
                    bindItem.tvStorageLoss.visibility = View.VISIBLE
                    bindItem.tvStorageLossValue.visibility = View.VISIBLE
                } else {
                    bindItem.tvStorageLoss.visibility = View.GONE
                    bindItem.tvStorageLossValue.visibility = View.GONE
                }
                bindItem.tvStLocationValue.text = item.storageLocationCode
                bindItem.tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(item.unitOfMeasure)
                bindItem.tvScaleGradeValue.text = item.materialName
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits()
                bindItem.tvScaleDispatchValue.text = editedWeight
                bindItem.tvDispatchUOMValue.text = item.weightToDispatchUOM
                bindItem.ivScaleClose.visibility = View.GONE
                bindItem.tvAddWeight.visibility = View.GONE
                if (!item.vendorName.isNullOrEmpty()) {
                    bindItem.tvVendor.visibility = View.VISIBLE
                    bindItem.tvVendorValue.visibility = View.VISIBLE
                    bindItem.tvVendorValue.text = item.vendorName
                }
                val lossOrGain = calculateLoss(
                    item.editedWeight ?: "", item.weight ?: "", item.unitOfMeasure ?: "",
                    item.weightToDispatchUOM ?: ""
                )
                bindItem.tvStorageLossValue.text = lossOrGain.plus(
                    " "
                ).plus(item.weightToDispatchUOM)
                /* tvStorageLoss.text =
                 (if (lossOrGain.toDouble() < 0) getString(R.string.storage_gain) else getString(R.string.storage_loss))*/
            })
    }

    private fun calculateLoss(editedWeight: String, weight: String, uom: String, wUom: String): String {
        var loss = ""
        if (uom == wUom) {
            loss = weight.toDouble().minus(editedWeight.toDouble()).toString()
        } else if (uom == "MT" && wUom == "KG") {
            loss = (weight.toDouble() * 1000).minus(editedWeight.toDouble()).toString()
        }
        return loss
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun setUpAdapter(list: List<VegaCoCoaReceiveLots>) {
        val lots = list as MutableList
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_mtnr_summary_lot_card_layout,
            ItemMtnrSummaryLotCardLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.batch
                bindItem.tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                bindItem.tvScaleGradeValue.text = item.materialName
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits()
                bindItem.tvScaleDispatchValue.text = editedWeight
                bindItem.tvstorageValue.text =
                    summaryObj?.storageLocationCode.plus("-").plus(summaryObj?.storageLocationName)
                /*if (item.editedWeight.toString().toDouble() < 0) {
                    tvStTransitValue.text = getString(R.string.transit_loss)
                    transit = editedWeight.toDouble().plus(item.weight!!.toDouble())
                } else {
                    tvStTransitValue.text = getString(R.string.transit_gain)
                    transit = item.weight!!.toDouble().minus(editedWeight.toDouble())
                }*/

                bindItem.tvstorage.visibility = View.VISIBLE
                bindItem.tvstorageValue.visibility = View.VISIBLE

                if (item.weight!!.toDouble() > editedWeight.toDouble()) {
                    bindItem.tvStTransit.text = getString(R.string.transit_loss)
                    transit = item.weight!!.toDouble().minus(editedWeight.toDouble())
                } else {
                    bindItem.tvStTransit.text = getString(R.string.transit_gain)
                    transit = editedWeight.toDouble().minus(item.weight!!.toDouble())
                }

                bindItem.tvStTransitValue.text =
                    transit.absoluteValue.formatThreeDigits().plus(" ").plus(item.uom).toString()
                bindItem.tvDispatchUOMValue.text = item.uom
                bindItem.tvAddWeight.visibility = View.GONE
                if (summaryObj?.isSynced!!) {
                    bindItem.ivEdit.visibility = View.GONE
                } else {
                    bindItem.ivEdit.visibility = View.VISIBLE
                }

                if (summaryObj?.syncStatusMsg?.isNotEmpty()!!) {
                    if (!summaryObj?.transitLossDocNo.isNullOrEmpty())
                        bindItem.ivEdit.visibility = View.GONE
                }
                bindItem.ivEdit.setOnClickListener { showLotEditDialog(item, it) }
            })
    }

    private fun showLotEditDialog(lot: VegaCoCoaReceiveLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    //activity?.onBackPressed()
                    Log.e("Json", Gson().toJson(lot))
                    callBack?.replaceFragment(EDIT_LOT, summaryObj as VegaCoCoaReceiving)
                },
                { dismiss() })
        }
    }

}
