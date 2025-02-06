package com.olam.warehouse.vegax.invoicenicaragua.data.domain.model

import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo

/**
 * Created by Baskaran Kannan on 10/9/2020.
 */
class VegaNicaraguaInvoiceReprintModel(
    var tempId: String = "",
    var vendor: VegaVendor = VegaVendor(),
    var grnData: GrnDetails = GrnDetails(),
    var priceInfo: VegaNicaraguaInvoicePriceInfo = VegaNicaraguaInvoicePriceInfo()
)
