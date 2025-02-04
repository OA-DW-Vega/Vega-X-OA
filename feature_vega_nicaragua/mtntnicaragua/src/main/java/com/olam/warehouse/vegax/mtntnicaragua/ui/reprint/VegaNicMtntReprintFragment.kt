package com.olam.warehouse.vegax.mtntnicaragua.ui.reprint

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.databinding.VegaNicaraguaMtntReceiptPrintingBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicaraguaMtntReprintBinding
import com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntViewModel
import kotlinx.android.synthetic.main.item_vega_nicaragua_mtnt_reprint.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 11/20/2020.
 */
class VegaNicMtntReprintFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_mtnt_reprint
    private lateinit var binding: FragmentVegaNicaraguaMtntReprintBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var callBack: CallBack? = null
    private var bitmapPrintKeys = ArrayList<String>()
    private var mtntList = mutableListOf<VegaMtntWithLotsWithBags>()

    companion object {
        fun newInstance() = VegaNicMtntReprintFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment()

        fun replaceFragment(moveFrag: String, receivingData: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaMtntReprintBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        vm.listWBLotsWithBags.observe(viewLifecycleOwner, Observer {
            mtntList =
                it.filter { (it.mtnt.isSyncStatus == false && it.mtnt.isOfflineData == true) || (it.mtnt.isSyncStatus == true && it.mtnt.isOfflineData == false) }
                    .toMutableList()
//            vm.getGrnPrintDetails()
            setupAdapter(mtntList)
        })
        vm.getListOfMtntWithLots()
//        vm.grnPrintDetails.observe(viewLifecycleOwner, Observer {updateReceiptData(it)})
        binding.tvPrint.setOnClickListener { showConfirmDialog() }

    }

    private fun updateReceiptData(data: Resource<GenericReqAndResp<List<VegaNicaraguaMtnt>>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()

                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
        setupAdapter(mtntList)
    }


    private fun setupAdapter(itemList: MutableList<VegaMtntWithLotsWithBags>) {
        if (itemList.size > 0) {
            binding.rvTransaction.visible()
            binding.tvPrint.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvPrint.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUp(itemList, R.layout.item_vega_nicaragua_mtnt_reprint, { it, pos ->
            tvMtntTempId.text =
                if (it.mtnt.delivery?.isNotEmpty() == true) getString(R.string.obd_no) else getString(R.string.mtnt_tmpid)
            tvMtntTempIdValue.text = if (it.mtnt.delivery?.isNotEmpty() == true) it.mtnt.delivery else it.mtnt.tempId
            tvVendorValue.text = it.mtnt.transportVendor.plus(" - ").plus(it.mtnt.transportVendorID)
            tvMaterialValue.text = it.mtnt.materialName
            tvStoNoValue.text = it.mtnt.purchaseDocNum
            tvNetWeightValue.text = it.mtnt.sendingPlant
            if (it.mtnt.erdat?.isNotEmpty() == true)
                tvDateValue.text = it.mtnt.erdat.let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTimeNicaragua(
                            it2,
                            App.getAppContext()
                        )
                    }
                }
            cbMtntItem.isChecked = it.mtnt.isProgress ?: false
            cvMtntItem.setOnClickListener { view ->
                it.mtnt.isProgress = !it.mtnt.isProgress!!
                cbMtntItem.isChecked = it.mtnt.isProgress ?: false
            }
        })
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    generateBitMapKey()
                },
                { dismiss() })
        }
    }

    private fun generateBitMapKey() {
        bitmapPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            mtntList.filter { it.mtnt.isProgress == true }.forEachIndexed { index, item ->
                var view = LayoutInflater.from(context)
                    .inflate(
                        com.olam.warehouse.login.R.layout.vega_nicaragua_mtnt_receipt_printing,
                        null
                    )
                var viewBinder = VegaNicaraguaMtntReceiptPrintingBinding.bind(view)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoCopy)
                val mtnt = item.mtnt
                val lot = item.lineItems
                var storageLoss = 0.0
                item.lineItems.forEach { item1 ->
                    if (item1.lots.isEndLot == true) {
                        val loss =
                            calculateStorageLoss(
                                item1.lots.weight.toString(),
                                item1.lots.editedWeight.toString()
                            )
                        if (loss.isNotEmpty()) storageLoss += loss.toDouble()
                    }
                }

                viewBinder.tvSendingPlant.text = mtnt.plantId.plus(" - ").plus(com.olam.warehouse.master.common.utils.getPlantDetails().plantName)
                viewBinder.tvDestPlant.text = mtnt.sendingPlant
                viewBinder.tvTranVendorName.text = mtnt.transportVendor
                viewBinder.tvTransVendorCode.text = mtnt.transportVendorID
                viewBinder.tvTruckNo.text = mtnt.vehicleNumber
                viewBinder.tvDriverName.text = mtnt.driverName
                viewBinder.tvCertification.text = mtnt.certification
                viewBinder.tvRemarks.text = mtnt.remarks
                viewBinder.tvSLoss.text = storageLoss.toString().plus(" ").plus(getString(com.olam.warehouse.login.R.string.kg))
                viewBinder.tvNoOfLots.text =
                    lot.map { it.lots }.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
                if (mtnt.erdat?.isNotEmpty() == true)
                    viewBinder.tvDate.text = mtnt.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTimeNicaragua(
                                it2,
                                App.getAppContext()
                            )
                        }
                    }
                viewBinder.tvMaterial.text = mtnt.materialName
                viewBinder.tvQualityGrade.text = mtnt.qulityGradeDesc
                viewBinder.tvUOM.text = mtnt.unitsOfMeasure
                viewBinder.tvBagsQuantity.text =
                    lot.sumBy { it.bagItems.sumBy { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 } }
                        .toString()
                viewBinder.tvGrossWeight.text =
                    lot.sumByDouble { it.bagItems.sumByDouble { if (it.grossWeight.isNotEmpty()) it.grossWeight.toDouble() else 0.0 } }
                        .toString()
                viewBinder.tvNetWeight.text =
                    lot.sumByDouble { it.bagItems.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 } }
                        .toString()
                viewBinder.tvTareWeight.text = lot.sumByDouble {
                    it.bagItems.sumByDouble {
                        (if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() else 0.0)?.times(if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0)
                            ?: 0.0
                    }
                }.toString()

                //Copy Print
                viewBinder.tvSendingPlantCopy.text = mtnt.plantId.plus(" - ").plus(com.olam.warehouse.master.common.utils.getPlantDetails().plantName)
                viewBinder.tvDestPlantCopy.text = mtnt.sendingPlant
                viewBinder.tvTranVendorNameCopy.text = mtnt.transportVendor
                viewBinder.tvTransVendorCodeCopy.text = mtnt.transportVendorID
                viewBinder.tvTruckNoCopy.text = mtnt.vehicleNumber
                viewBinder.tvDriverNameCopy.text = mtnt.driverName
                viewBinder.tvCertificationCopy.text = mtnt.certification
                viewBinder.tvRemarksCopy.text = mtnt.remarks
                viewBinder.tvSLossCopy.text = storageLoss.toString().plus(" ").plus(getString(com.olam.warehouse.login.R.string.kg))
                viewBinder.tvNoOfLotsCopy.text =
                    lot.map { it.lots }.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
                if (mtnt.erdat?.isNotEmpty() == true)
                    viewBinder.tvDateCopy.text = mtnt.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTimeNicaragua(
                                it2,
                                App.getAppContext()
                            )
                        }
                    }
                viewBinder.tvMaterialCopy.text = mtnt.materialName
                viewBinder.tvQualityGradeCopy.text = mtnt.qulityGradeDesc
                viewBinder.tvUOMCopy.text = mtnt.unitsOfMeasure
                viewBinder.tvBagsQuantityCopy.text =
                    lot.sumBy { it.bagItems.sumBy { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 } }
                        .toString()
                viewBinder.tvGrossWeightCopy.text =
                    lot.sumByDouble { it.bagItems.sumByDouble { if (it.grossWeight.isNotEmpty()) it.grossWeight.toDouble() else 0.0 } }
                        .toString()
                viewBinder.tvNetWeightCopy.text =
                    lot.sumByDouble { it.bagItems.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 } }
                        .toString()
                viewBinder.tvTareWeightCopy.text = lot.sumByDouble {
                    it.bagItems.sumByDouble {
                        (if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() else 0.0)?.times(if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0)
                            ?: 0.0
                    }
                }.toString()

                bitmapPrintKeys.add(
                    bitmapToString(
                        getBitmapFromView(
                            view, Color.WHITE
                        )
                    )
                )
            }
            //showPreviewDialog()
            HandlerUtils.runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(bitmapPrintKeys))
                startActivity(Intent(activity, WifiMainActivity::class.java))
            }

        }.execute()
    }

    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }

    private fun calculateStorageLoss(weight: String, editedWeight: String): String {
        val lossValue =
            (if (weight.isNotEmpty()) weight.toDouble() else 0.0).minus(if (editedWeight.isNotEmpty()) editedWeight.toDouble() else 0.0)
                .formatTwoDigits()
        return lossValue
    }

    private fun showPreviewDialog() {

        val list = mutableListOf<String>()
        list.addAll(bitmapPrintKeys)
        val dialogFragment =
            PrintPreviewDialogFragment(list)
        activity?.supportFragmentManager?.let { dialogFragment.show(it, "signature") }
    }


    fun getPlantDetails(): Plant {
        val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
        return Gson().fromJson<Plant>(plantDetailJson)
    }

}
