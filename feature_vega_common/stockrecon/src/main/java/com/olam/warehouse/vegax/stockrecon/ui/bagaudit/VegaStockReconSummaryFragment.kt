package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.model.VegaStockReconBagDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataResp
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentVegaStockAuditSummaryBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_ALL_AUDIT_LIST
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream

class VegaStockReconSummaryFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_stock_audit_summary
    private lateinit var binding: FragmentVegaStockAuditSummaryBinding
    private var bagAuditDetails: VegaStockReconBagDetails = VegaStockReconBagDetails()
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()


    companion object {
        fun newInstance(bagAuditDetails: VegaStockReconBagDetails) = VegaStockReconSummaryFragment().putArgs {
            putParcelable(BUNDLE_DATA, bagAuditDetails)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaStockAuditSummaryBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        getArgumentsData()
        setUIValues()
        clickListener()
        observer()
    }

    private fun clickListener() {
        binding.tvProceed.setOnClickListener {
            submitAuditDataConfirmationDialog()
        }
    }

    private fun observer() {
        vm.postDataResp.observe(viewLifecycleOwner, Observer { postAuditResponseInfo(it) })
    }

    private fun setUIValues() {
        binding.tvLotValue.setText("Lot No : ".plus(bagAuditDetails.selectedLots.batchNumber))
        binding.tvMaterialValue.setText(bagAuditDetails.selectedLots.materialName)
        binding.tvSystemWeightValue.setText(bagAuditDetails.selectedLots.weight.plus(bagAuditDetails.selectedLots.unitOfMeasure))
        binding.tvBagTypeValue.setText(bagAuditDetails.selectedLots.bagType)
        binding.tvWeightLossValue.setText(bagAuditDetails.weightLoss.plus(bagAuditDetails.selectedLots.unitOfMeasure))
        binding.tvBagsCountValue.setText(bagAuditDetails.selectedLots.totalNoOfBags)
        binding.tvHalfBagsCountValue.setText(bagAuditDetails.totalHalfBackCount)
        binding.tvFullBagsCountValue.setText(bagAuditDetails.totalFullBackCount)
        binding.tvDamagedBagValue.setText(bagAuditDetails.bagDamaged)
        binding.tvDamagedBagCountValue.setText(bagAuditDetails.damagedBagCount)
        binding.tvSpillageValue.setText(bagAuditDetails.spillage)
        binding.tvRemarksValue.setText(bagAuditDetails.remarks)
        binding.tvSystemWtValue.setText(bagAuditDetails.selectedLots.weight.plus(bagAuditDetails.selectedLots.unitOfMeasure))
        binding.tvStockAuditWtValue.setText(
            (bagAuditDetails.stockAuditWeight?.toDouble()?.formatThreeDigits()
                .toString()).plus(bagAuditDetails.selectedLots.unitOfMeasure)
        )
        binding.tvWeightLossFinalValue.setText(bagAuditDetails.weightLoss.plus(bagAuditDetails.selectedLots.unitOfMeasure))
        if (bagAuditDetails.imageString?.isNotEmpty() == true) {
            binding.photoImageView.visible()
            binding.photoImageView.setImageBitmap(setScaledBitmap())
        }
    }

    /*prepare audit data for post api*/
    private fun prepareAuditData(): VegaStockReconPostAuditDataReq {
        var request = VegaStockReconPostAuditDataReq()
        request.reconId = bagAuditDetails.reconIdDetails.id
        request.storageLocation = bagAuditDetails.selectedLots.storageLocationCode
        request.material = bagAuditDetails.selectedLots.materialCode
        request.lotNumber = bagAuditDetails.selectedLots.batchNumber
        request.sysNoOfBags = bagAuditDetails.selectedLots.totalNoOfBags
        request.bagType1 = bagAuditDetails.bagType1
        request.bagType2 = bagAuditDetails.bagType2
        request.bagType3 = bagAuditDetails.bagType3
        request.totalNoOfBags1 = bagAuditDetails.selectedLots.totalNoOfBags
        request.bagWeight1 = bagAuditDetails.bagWeight1
        request.bagWeight2 = bagAuditDetails.bagWeight2
        request.bagWeight3 = bagAuditDetails.bagWeight3
        request.noOfHalfBags1 = bagAuditDetails.halfBagCount1?.toString()
        request.noOfFullBags1 = bagAuditDetails.fullBagCount1?.toString()
        request.noOfHalfBags2 = bagAuditDetails.halfBagCount2?.toString()
        request.noOfFullBags2 = bagAuditDetails.fullBagCount2?.toString()
        request.noOfHalfBags3 = bagAuditDetails.halfBagCount3?.toString()
        request.noOfFullBags3 = bagAuditDetails.fullBagCount3?.toString()
        request.noOfDamagedBags = bagAuditDetails.damagedBagCount?.toString()
        request.systemNetWeight = bagAuditDetails.selectedLots.weight
        request.stockAuditWeight = bagAuditDetails.stockAuditWeight
        request.weightGainLoss = bagAuditDetails.weightLoss
        request.unitOfMeasure = bagAuditDetails.selectedLots.unitOfMeasure.toString()
        request.remarks = bagAuditDetails.remarks.toString()
        request.imageUrl = bagAuditDetails.imageString.toString()
        /*converting image into byte array*/
        val charset = Charsets.UTF_8
        val byteArray =
            getBase64FromFile(request.imageUrl.toString())?.toByteArray(charset)
        request.imageUrl = if (byteArray == null) "" else byteArray?.let { it1 -> String(it1) }.toString()
        request.bagDamaged = if (bagAuditDetails.bagDamaged.equals("Yes", ignoreCase = true)) true else false
        request.spillage = if (bagAuditDetails.spillage.equals("Yes", ignoreCase = true)) true else false
        return request
    }

    fun getBase64FromFile(path: String?): String? {
        var bmp: Bitmap? = null
        var baos: ByteArrayOutputStream? = null
        var baat: ByteArray? = null
        var encodeString: String? = null
        try {
            bmp = BitmapFactory.decodeFile(path)
            baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            baat = baos.toByteArray()
            encodeString = Base64.encodeToString(baat, Base64.DEFAULT)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return encodeString
    }


    private fun getArgumentsData() {
        bagAuditDetails = arguments?.getParcelable<VegaStockReconBagDetails>(BUNDLE_DATA) as VegaStockReconBagDetails
    }

    private fun postAuditResponseInfo(response: Resource<GenericReqAndResp<VegaStockReconPostAuditDataResp>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        moveToAuditListPage()
                    }
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    fun setScaledBitmap(): Bitmap? {
        try {
            val imageViewWidth = 347
            val imageViewHeight = 413

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(bagAuditDetails.imageString, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(bagAuditDetails.imageString, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null

    }

    private fun submitAuditDataConfirmationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.submit_audit_data_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    /*post audit data api*/
                    vm.postAuditData(prepareAuditData())
                },
                { dismiss() })
        }
    }

    private fun moveToAuditListPage() {
        callBack?.replaceFragment(STOCK_ALL_AUDIT_LIST, bagAuditDetails)
    }

}
