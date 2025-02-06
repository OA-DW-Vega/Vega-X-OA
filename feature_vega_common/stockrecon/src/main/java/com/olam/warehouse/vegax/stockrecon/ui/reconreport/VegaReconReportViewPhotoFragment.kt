package com.olam.warehouse.vegax.stockrecon.ui.reconreport

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportBundleData
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportPhotoViewBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.net.URL

class VegaReconReportViewPhotoFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_recon_report_photo_view
    private lateinit var binding: FragmentReconReportPhotoViewBinding
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()
    var bundleData = VegaReconReportBundleData()
    var reconReportImageByteArray: ByteArray = byteArrayOf()


    companion object {
        fun newInstance(bundleData: VegaReconReportBundleData) = VegaReconReportViewPhotoFragment().putArgs {
            putParcelable(BUNDLE_DATA, bundleData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReconReportPhotoViewBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        initExtra()
//        updateUI()
        setUIValues()
        clickListener()
        observer()

    }

    private fun initExtra() {
        bundleData = arguments?.getParcelable<VegaReconReportBundleData>(BUNDLE_DATA) as VegaReconReportBundleData
    }

    private fun clickListener() {
        binding.tvPrint.setOnClickListener {
            printReportData()
        }
    }

    private fun setUIValues() {
        binding.tvReconIdValue.setText("Rec Id : ${bundleData.selectedReconDetails?.id}")
    }

    private fun observer() {
        if (bundleData.isReport) {
            bundleData.selectedReconDetails?.id?.let { vm.getReconReportImage(it) }
            vm.reconReportImage.observe(viewLifecycleOwner, Observer { loadReconReportImage(it) })
        } else {
            bundleData.selectedReconDetails?.id?.let { reconId ->
                bundleData.auditDetails?.id?.let { auditId ->
                    vm.getAuditImage(
                        reconId,
                        auditId
                    )
                }
            }
            vm.auditImage.observe(viewLifecycleOwner, Observer { loadAuditImage(it) })
        }
    }

    private fun loadReconReportImage(response: Resource<GenericReqAndResp<String>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        if(data.isNotEmpty()) {
                            reconReportImageByteArray = Base64.decode(data, Base64.DEFAULT)
                            Glide.with(requireActivity()).asBitmap().load(reconReportImageByteArray).fitCenter()
                                .error(com.olam.warehouse.presentation.R.drawable.ic_error)
                                .listener(object : RequestListener<Bitmap> {
                                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                                        return false
                                    }
                                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                        binding.tvPrint.visible()
                                        return false
                                    }
                                })
                                .into(binding.ivPhoto)
                        } else {
                            binding.tvEmpty.visible()
                        }
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

    private fun loadAuditImage(response: Resource<GenericReqAndResp<String>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        if(data.isNotEmpty()) {
                            val imageByteArray: ByteArray = Base64.decode(data, Base64.DEFAULT)
                            Glide.with(requireActivity()).asBitmap().load(imageByteArray).fitCenter()
                                .error(com.olam.warehouse.presentation.R.drawable.ic_error)
                                .into(binding.ivPhoto)
                        } else {
                            binding.tvEmpty.visible()
                        }
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


    /*This is methods is to print report image*/
    private fun printReportData() {
        showLoading()
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
//            /*The below lines to download text file from the report URL*/
//            var url = URL(bundleData.selectedReconDetails?.reportUrl)
//            /*The below lines to read all bytes from downloaded file*/
//            val bytes = url.readBytes()

            /*loading recon report image byte array into print page*/
            if (reconReportImageByteArray.size > 0) {
                var bitmap = BitmapFactory.decodeByteArray(reconReportImageByteArray, 0, reconReportImageByteArray.size)
                var tallyPrintKeys = ArrayList<String>()
                /*Adding bitmpa into tallyprintkeys*/
                tallyPrintKeys.add(bitmapToString(bitmap))
                val gson = GsonUtils()
                /*save these tallyPrintKeys in sharedPref
            & navigating to wifi activity for print option purpose*/
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                val intent = Intent(requireContext(), WifiMainActivity::class.java)
                startActivity(intent)
                hideLoading()
            }
        }

    }

    /*converting bitmap to string*/
    fun bitmapToString(bitmap: Bitmap): String {
        try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b: ByteArray = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        } catch (e: OutOfMemoryError) {
            return ""
        }
    }

   /* fun getBitmap(lot: String): Bitmap? {
        val content = lot
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }*/

    /*private fun updateUI() {
     *//*In this fragment, we are loading 2 types of images, like report image and normal pic,
        so due to this report flag, we are loading corresponding images*//*
        if (bundleData.isReport) {
            if (bundleData.selectedReconDetails?.reportUrl?.isNotEmpty() == true) {
                Glide.with(requireActivity()).load(bundleData.selectedReconDetails?.reportUrl).fitCenter()
                    .error(com.olam.warehouse.presentation.R.drawable.ic_error)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            hideLoading()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            hideLoading()
                            *//*Print option is needed for report type image*//*
                            binding.tvPrint.visible()
                            return false
                        }
                    })
                    .into(binding.ivPhoto)

            } else {
                binding.tvEmpty.visible()
            }
        } else {
            if (bundleData.auditDetails?.imageUrl?.isNotEmpty() == true) {
                showLoading()
                Glide.with(requireActivity()).load(bundleData.auditDetails?.imageUrl)
                    .fitCenter()
                    .error(com.olam.warehouse.presentation.R.drawable.ic_error)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            hideLoading()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            hideLoading()
                            return false
                        }
                    })
                    .into(binding.ivPhoto)
            } else {
                binding.tvEmpty.visible()
            }
        }

    }*/
}

