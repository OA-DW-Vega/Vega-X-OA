package com.olam.warehouse.vegax.grnnigeria.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnigeria.R
import com.olam.warehouse.vegax.grnnigeria.databinding.FragmentGrntNigeriaSummaryBinding
import com.olam.warehouse.vegax.grnnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 3/9/2020.
 */
class VegaGrntNigeriaSummaryFragment : BaseFragment() {

    private var gateEntryData = VegaGateEntry()
    private var postData = mutableListOf<VegaGateEntry>()
    private var grnPrice: String = ""
    private val vm: VegaNigeriaGrnViewModel by viewModel()
    private lateinit var binding: FragmentGrntNigeriaSummaryBinding
    override val layoutResourceId = R.layout.fragment_grnt_nigeria_summary

    companion object {
        fun newInstance(
            gateEntryData: VegaGateEntry, grnPrice: String
        ) = VegaGrntNigeriaSummaryFragment().putArgs {
            putParcelable(GRNT_DATA, gateEntryData)
            putString(GRN_PRICE, grnPrice)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentGrntNigeriaSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentryNigeria/ui/VegaGateEntrySummaryFragment")
            .title("Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        gateEntryData = arguments?.getParcelable(GRNT_DATA)!!
        grnPrice = arguments?.getString(GRN_PRICE)!!
        if (gateEntryData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
        } else {
            binding.llSupplier.gone()
        }

        binding.tvMaterialValue.text =
            gateEntryData.materialCode.plus("-").plus(gateEntryData.materialName)
        binding.tvSupplier.text = gateEntryData.supplierName
        binding.tvProcurementTypeValue.text = gateEntryData.procurementType
        binding.tvPlant.text = gateEntryData.plantId.plus("-").plus(gateEntryData.plantName)
        binding.tvNetWeightValue.text = gateEntryData.netWeight.plus(" KG")
        binding.tvGRNPriceValue.text = gateEntryData.grossWeight.plus(" NGN")
        binding.tvReceivingPlantValue.text = gateEntryData.receivingPlant

        if (gateEntryData.imagePath?.isNotEmpty()!!) {
            binding.photoView.visible()
            binding.photoView.setImageBitmap(setScaledBitmap())
        }

        binding.btnConfirm.setOnClickListener { showConfirmDialog() }
        binding.tvViewImage.setOnClickListener {
        }

        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })

    }

    private fun setScaledBitmap(): Bitmap? {
        try {
            val imageViewWidth = 100
            val imageViewHeight = 100

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(gateEntryData.imagePath, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(gateEntryData.imagePath, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grnt)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postGRNT()
                },
                { dismiss() })
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.grnNumber)
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun postGRNT() {
        postData.clear()
        var sampleID = generateSampleID()
        gateEntryData.challan = sampleID.toString()
        gateEntryData.wsGate = "WS01"
        gateEntryData.wsType = "WS"
        // postData.add(gateEntryData)
        postData.forEachIndexed { index, vegaReceiving ->
            vegaReceiving.item = index.inc().toString()
        }
        if (AppUtils.isOnline()) {
            gateEntryData.batchNumber = generateBatchId()
            gateEntryData.item = "00001"
            var grnList: ArrayList<VegaNigeriaCocoaGrnData> = ArrayList()
            var grnData = VegaNigeriaCocoaGrnData(
                generateBatchId(),
                "",
                "",
                "",
                gateEntryData.item,
                gateEntryData.materialCode,
                gateEntryData.netWeight,
                gateEntryData.plantId,
                gateEntryData.grossWeight,
                "",
                "",
                gateEntryData.storageLocationCode,
                gateEntryData.supplierCode,
                gateEntryData.unitsOfMeasure,
                //receivingData.weighBridgeId,
                "",
                gateEntryData.weighBridgeType,
                "",
                UNIT_Z01
            )
            grnList.add(grnData)

            var qualityDetails = ArrayList<OffloadingGhanaCocoaQualityDetails>()
            var cameroonOffloadingQualityDetails = GhanaCocoaOffloadingQualityDetails(
                gateEntryData.materialCode,
                gateEntryData.batchNumber,
                qualityDetails
            )
            var lotDetails = ArrayList<GhanaCocoaOffloadingQualityDetails>()
            lotDetails.add(cameroonOffloadingQualityDetails)

            postData.add(gateEntryData)

            postData.forEach {
                it.challan = gateEntryData.challan
                it.tmpWbId = ""
                it.weighBridgeId = ""
                it.direction = ""
                it.batchNumber = generateBatchId()
                it.erdat = DateUtils.getCurrentTimeInMills().toString()
            }

            vm.postNigeriaCocoa(
                VegaNigeriaCocoaOffloadingPost(
                    batchNumber = generateBatchId(),
                    whReceiptNum = "",
                    cascara = "",
                    certificate = "",
                    encodedImageContent = "",
                    errorMessage = "",
                    exchangeRate = "",
                    grade = "",
                    grnData = grnList,
                    grnFlag = true,
                    grnNumber = "",
                    grnType = "",
                    humedad = "",
                    imageUploadMsg = "",
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    weighDetails = postData,
                    lotDetails = lotDetails
                )
            )
        }


    }

    private fun generateBatchId(): String {
        var batchId = ""
        val yearFormat = SimpleDateFormat("yy") // Just the year, with 2 digits
        val monthFormat = SimpleDateFormat("MM") // Just the month, with 2 digits
        val dateFormat = SimpleDateFormat("dd") // Just the date, with 2 digits

        val formattedDate = dateFormat.format(Calendar.getInstance().time)
        val formattedMonth = monthFormat.format(Calendar.getInstance().time)
        val formattedYear = yearFormat.format(Calendar.getInstance().time)

        batchId = formattedYear.plus(formattedMonth).plus(formattedDate)
            .plus(gateEntryData.storageLocationCode)
        return batchId
    }

    private fun generateSampleID(): Long {
        var sampleID = createRandomInteger(1000000000, 9999999999L, Random)
//        getRandomNumber(1000000000,9999999999L)
        return sampleID
    }

    private fun prepareSuccessData(grnNumber: String?) {
        hideLoading()
        moveToSuccessPage(grnNumber)
    }

    private fun moveToSuccessPage(grnNumber: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_entry))

//        intent.putExtra(
//            AppUtils.SUB_TITLE,
//            getString(R.string.sample_id_created).plus("\n ").plus(getString(R.string.weigh_bridge_id_is)).plus(" ")
//                .plus(wbId)
//        )
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.grnt_number_text).plus(" ")
                .plus(grnNumber)
        )

        val lotlist = ArrayList<VegaCoffeeSalesLots>()
        lotlist.add(
            VegaCoffeeSalesLots(
                "",
                gateEntryData.challan.toString(),
                gateEntryData.materialCode.toString(),
                gateEntryData.materialName.toString(),
                "",
                "",
                "",
                "",
                "",
                gateEntryData.unitsOfMeasure,
                "",
                gateEntryData.approximateWeight
            )
        )

//        intent.putExtra(UIUtils.FROM_Nigeria_GATEENTRY_COCOA, true)
//        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
//        intent.putExtra(AppUtils.PRINT_ENABLE, true)

        startActivity(intent)
        requireActivity().finish()
    }
}
