package com.olam.warehouse.vegax.localsalesecuador.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeePendingSalesOrderWithLots
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.localsalesecuador.R
import com.olam.warehouse.vegax.localsalesecuador.databinding.FragmentEcuadorCocoaSalesTypesBinding
import com.olam.warehouse.vegax.localsalesecuador.utils.SALES_PENDING
import com.olam.warehouse.vegax.localsalesecuador.utils.SALES_TYPE_ANTICIPATED
import com.olam.warehouse.vegax.localsalesecuador.utils.SALES_TYPE_WEIGHBRIDGE
import com.olam.warehouse.vegax.localsalesecuador.utils.SALES_TYPE_WEIGHSCALE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaEcuadorCocoaSalesSelectSaleTypeFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_ecuador_cocoa_sales_types
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentEcuadorCocoaSalesTypesBinding
    private val vm: VegaEcuadorCocoaSalesViewModel by viewModel()

    interface CallBack {
        fun replaceFragment(fragment: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaEcuadorCocoaSalesSelectSaleTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEcuadorCocoaSalesTypesBinding.inflate(layoutInflater)
        /*vm.getConfigItems(UserRoles.SALES.role)
        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })*/

        return binding.root
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.SALES_ANTICIPATED.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> binding.clAnticipated.visibility = View.VISIBLE
                        it.applicable?.contains("N")!! -> binding.clAnticipated.visibility = View.GONE
                    }
                }

                ConfigItems.SALES_WEIGHBRIDGE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> binding.clWeighBridge.visibility = View.VISIBLE
                        it.applicable?.contains("N")!! -> binding.clWeighBridge.visibility = View.GONE
                    }
                }
                ConfigItems.SALES_WEIGHSCALE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> binding.clWeightScale.visibility = View.VISIBLE
                        it.applicable?.contains("N")!! -> binding.clWeightScale.visibility = View.GONE
                    }
                }
            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/localsalesecuador/ui/VegaCoffeeSalesSelectSaleTypeFragment").title("Dispatch Sales")
            .with(tracker)
    }

    private fun initUI() {
//        binding.clWeightScale.visibility = View.VISIBLE
//        binding.clAnticipated.visibility = View.VISIBLE
        binding.clWeighBridge.visibility = View.VISIBLE
        binding.clWeighBridge.setOnClickListener { moveToWeightBridge() }
//        binding.clWeightScale.setOnClickListener { moveToWeighScale() }
//        binding.clAnticipated.setOnClickListener { moveToAnticipated() }
        vm.dispatchPendingSales.observe(viewLifecycleOwner, Observer { updateUIPendingList(it) })
    }

    private fun moveToWeightBridge() {
        callBack?.replaceFragment(SALES_TYPE_WEIGHBRIDGE)
    }

    private fun moveToWeighScale() {
        vm.getPendingList(SALES_TYPE_WEIGHSCALE)
    }

    private fun updateUIPendingList(it: List<VegaCoffeePendingSalesOrderWithLots>) {
        val list = it.map { it1 -> it1.salesOrder }
        if (list.size > 0) callBack?.replaceFragment(SALES_PENDING)
        else callBack?.replaceFragment(SALES_TYPE_WEIGHSCALE)
    }

    private fun moveToAnticipated() {
        callBack?.replaceFragment(SALES_TYPE_ANTICIPATED)

        /* val content = "12345678910"
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
         binding.ivQrCode.setImageBitmap(bitmap)
         val data = getBitmapFromView(binding.clWeightScale.rootView)*/
    }

    fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
