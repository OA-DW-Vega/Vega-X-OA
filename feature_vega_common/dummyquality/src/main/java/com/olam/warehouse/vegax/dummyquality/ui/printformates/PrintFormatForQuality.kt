package com.olam.warehouse.vegax.dummyquality.ui.printformates

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.vegax.dummyquality.R
import com.olam.warehouse.vegax.dummyquality.data.domain.model.QualityParametersValueList
import com.olam.warehouse.vegax.dummyquality.databinding.ItemQualityBinding
import com.olam.warehouse.vegax.dummyquality.databinding.VegaQualityTallySheetBinding

fun generateQualitySheet(vendorName:String,
                         materialName:String,
                         transactionNumber:String,
    wbdetails:  VegaQualityWBDetails, paramList: ArrayList<QualityParametersValueList>, context: Context): ArrayList<String> {
    var printKeys = ArrayList<String>()
    var weighBridgeDetails = VegaQualityWBDetails()
    var qualityParameterList = arrayListOf<QualityParametersValueList>()
    weighBridgeDetails =wbdetails
    qualityParameterList = paramList
    DoAsync {
        val view =
            LayoutInflater.from(context).inflate(R.layout.vega_quality_tally_sheet, null)
        val viewBinder = VegaQualityTallySheetBinding.bind(view)
        viewBinder.tvVendorName.text = vendorName
        viewBinder.tvMaterialName.text = materialName
        viewBinder.tvTransactionNumber.text = transactionNumber
        viewBinder.tvTransactionDate.text = DateUtils.getDate(DateUtils.getCurrentTimeInMills(), "dd/MM/yyyy")
        viewBinder.rvQuality.setUpAdapter(
            qualityParameterList.toMutableList(),
            R.layout.item_quality,
            ItemQualityBinding::inflate,
            { it, pos, bindingItem ->
                bindingItem.tvLabel.text = it.descrChar
                bindingItem.tvValue.text = it.qualityParameterValue
            })
        printKeys.add(
            bitmapToString(
                getBitmapFromView(
                    view, Color.WHITE
                )
            )
        )

    }.execute()
    return printKeys
}
