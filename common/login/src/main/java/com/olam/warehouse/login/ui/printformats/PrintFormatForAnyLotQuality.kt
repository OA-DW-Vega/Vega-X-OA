package com.olam.warehouse.login.ui.printformats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ItemAnyLotQualityParamBinding
import com.olam.warehouse.login.databinding.VegaAnyLotQualityParamsListBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.model.VegaBatchDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone


fun printForAnyLotQualityParams(intent: Intent, context: Context): ArrayList<String> {

    val qualityList = intent.getParcelableArrayListExtra(UIUtils.ANY_LOT_QUALITY_LIST) ?: java.util.ArrayList<VegaBatchDetails>()
    val printKeys = ArrayList<String>()
    try {
        val view = LayoutInflater.from(context).inflate(R.layout.vega_any_lot_quality_params_list, null)
        val viewBinder = VegaAnyLotQualityParamsListBinding.bind(view)

        var qualityParams = qualityList.filterNot {
            it.atnam.equals("ZNGCOCOA_DIS_MOULD") || it.atnam.equals("ZNGCOCOA_DIS_BW") ||
                    it.atnam.equals("ZNGCOCOA_DIS_ON_OTHERS") || it.atnam.equals("ZNGCOCOA_DIS_BS")
        } as ArrayList<VegaBatchDetails?>
        var i = 0
        viewBinder.rvApproveQuality.setUpAdapter(
            qualityParams.toMutableList(),
            R.layout.item_any_lot_quality_param,
            ItemAnyLotQualityParamBinding::inflate,
            { item, pos, bindingItem ->
                i++
                if (i % 2 == 0) {
                    this.setBackgroundColor(context.getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                } else {
                    this.setBackgroundColor(context.getColor(com.olam.warehouse.presentation.R.color.white))
                }

                bindingItem.tvQualityNameApprove.text = item?.desc
                bindingItem.tvUnitApprove.text = item?.atwtb?.replace(",",".")
            })

        viewBinder.rvApproveQuality.smoothScrollToPosition(qualityParams.size-1);
        printKeys.add(
            bitmapToString(
                getBitmapFromView(
                    view, Color.WHITE
                )
            )
        )
    }catch (e:Exception){
        e.printStackTrace()
        // Toast.makeText(context, "error2", Toast.LENGTH_SHORT).show()
    }
    return printKeys
}
