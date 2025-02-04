package com.olam.warehouse.vegax.forwardponicaragua.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.forwardponicaragua.R
import com.olam.warehouse.vegax.forwardponicaragua.databinding.FragmentVegaNicaraguaForwardPoPricingBinding
import com.olam.warehouse.vegax.grnecuador.utils.FORWARD_PO_CREATION_PREVIEW_FRAG
import com.olam.warehouse.vegax.grnecuador.utils.covertToDouble
import com.olam.warehouse.vegax.grnecuador.utils.getTmpId
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class VegaNicaraguaForwardPOPricingFragment : BaseFragment() {

    private var callBack: Callback? = null
    private lateinit var binding: FragmentVegaNicaraguaForwardPoPricingBinding
    private var receivingData = VegaNicaraguaForwardPODetails()
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var filteredpricedetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaForwardPOPricingFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_forward_po_pricing

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaForwardPoPricingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("FORWORDPO/ui/VegaNicaraguaForwordPOPricingFragment")
            .title("FORWORD PO Pricing DETAILS ")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        arguments?.let {
            receivingData=it.getParcelable(UIUtils.RECEIVING_DATA)!!
            priceDetails=it.getParcelableArrayList<VegaNicaraguaGrnPriceDetails>(UIUtils.YIELD_DATA)!!
            filteredpricedetails=it.getParcelableArrayList<VegaNicaraguaGrnPriceDetails>(UIUtils.FILTEREDPRICEDETAILS)!!
        }
    }

    private fun initUI() {
        updateMandatory()

        if (!receivingData.netWeight.isNullOrEmpty()) {
            binding.etEnterWeight.setText(receivingData.netWeight!!)
            binding.etEnterPricePerUnit.setText(receivingData.pricePerUnit)
            updateTotalPrice()
        }

        binding.tvExchangeRate.text = receivingData.exchangeRate

        binding.btnProceed.setOnClickListener {
            if (validateDetails()) {
                receivingData.netWeight = binding.etEnterWeight.text.toString()
                receivingData.pricePerUnit = binding.etEnterPricePerUnit.text.toString()
                receivingData.tempId = if (receivingData.tempId.isEmpty()) getTmpId() else receivingData.tempId
                receivingData.receiptNetPrice=binding.etEnterPricePerQQ.text.toString()


                val srcDf: DateFormat = SimpleDateFormat("dd/MM/yyyy")

                // parse the date string into Date object

                // parse the date string into Date object
                val date = srcDf.parse(binding.tvDate.text.toString())
                val destDf: DateFormat = SimpleDateFormat("yyyyMMdd")
                var formatteddate = destDf.format(date)
                receivingData.deliveryDate  = formatteddate.toString()
                var data = Bundle()
                data.putParcelable(UIUtils.RECEIVING_DATA, receivingData)
                data.putParcelableArrayList(UIUtils.YIELD_DATA, priceDetails)
                data.putParcelableArrayList(UIUtils.FILTEREDPRICEDETAILS, filteredpricedetails)

                callBack?.replaceFragment(
                    FORWARD_PO_CREATION_PREVIEW_FRAG, data
                )
            }
        }

        binding.etEnterWeight.onChange { s ->

            if (covertToDouble(s) >= 0.0) {

                updateTotalPrice()
            }

        }
        binding.etEnterPricePerUnit.onChange { s ->

            if (covertToDouble(s) >= 0.0) {
                updateTotalPrice()
            }

        }
        binding.tvDate.text = DateUtils.getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        binding.tvDate.setOnClickListener { getDatePickerDialog() }
    }

    private fun updateTotalPrice()
    {
        binding.tvTotalPrice.text =
            formatString(covertToDouble(binding.etEnterWeight.text.toString()) * covertToDouble(binding.etEnterPricePerUnit.text.toString()))
        receivingData.netPrice = String.format(
            Locale.ENGLISH,
            "%.2f",
            covertToDouble(binding.etEnterWeight.text.toString()) * covertToDouble(binding.etEnterPricePerUnit.text.toString())
        )
    }

    private fun validateDetails():Boolean
    {
        var flag=true
        if(binding.etEnterWeight.text.toString().isNullOrEmpty()||covertToDouble(binding.etEnterWeight.text.toString())<=0.0)
        {
            flag=false
            binding.etEnterWeight.error=getString(R.string.weight_error_info)
        }
        if(binding.etEnterPricePerUnit.text.toString().isNullOrEmpty()||covertToDouble(binding.etEnterPricePerUnit.text.toString())<=0.0)
        {
            binding.etEnterPricePerUnit.error=getString(R.string.price_per_unit_error_info)
            flag=false
        }
        if(binding.tvDate.text.toString().isEmpty())
        {
            binding.tvDate.error=getString(R.string.deliveryterm_error_info)
            flag=false
        }
        if(binding.etEnterPricePerQQ.text.toString().isNullOrEmpty()||covertToDouble(binding.etEnterPricePerQQ.text.toString())<=0.0)
        {
            binding.etEnterPricePerQQ.error=getString(R.string.price_per_QQ_error_info)
            flag=false
        }
        return flag
    }

    private fun updateMandatory() {
        binding.tvWeight.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.weight)) { mandatoryStars() } }
        binding.tvPricePerUnit.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.price_per_unit)) { mandatoryStars() } }
        binding.tvDeliveryterm.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.deliveryterm)) { mandatoryStars() } }
        binding.tvPricePerQQ.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.price_per_QQ)) { mandatoryStars() } }
    }

    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return getString(R.string.c_doller) + " " + strFormat.format(this).replace(",", "")
    }
    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateTxt = binding.tvDate.text.split("/")
        cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        val DATE_FORMAT = "dd/MM/yyyy"
        val UTC = "UTC"

        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val sdf = SimpleDateFormat(DATE_FORMAT, LocaleHelper.getLocale(it))
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    binding.tvDate.text = sdf.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

    }
}
