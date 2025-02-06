package com.olam.warehouse.login.ui.printformats

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ItemPrintLotCardPreviewBinding
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.extension.gone


fun generateSecretQRCode(intent: Intent, context: SuccessActivity): ArrayList<String> {
   val secretId = intent.getStringExtra(AppUtils.SECRET_ID)

    var printKeys = ArrayList<String>()
    DoAsync {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_print_lot_card_preview, null)
        val viewBinder = ItemPrintLotCardPreviewBinding.bind(view)
        if(secretId?.isNotEmpty() == true){
            viewBinder.ivPreview.setImageBitmap(getBitmap(secretId))
        }
        viewBinder.tvLot.text = context.getString(R.string.secret_id)
        viewBinder.tvLotValue.text = secretId?:""
        viewBinder.tvMaterial.gone()
        viewBinder.tvMaterialValue.gone()
        viewBinder.tvWeight.gone()
        viewBinder.tvWeightValue.gone()


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
