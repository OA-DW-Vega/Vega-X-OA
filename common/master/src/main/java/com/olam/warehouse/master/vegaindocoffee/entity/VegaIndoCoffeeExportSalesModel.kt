package com.olam.warehouse.master.vegaindocoffee.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 4/27/2021.
 */


@Entity
@Parcelize
data class IndoExporSalesMaterialList(
    @PrimaryKey
    var salesOrderId: String = "",
    var salesItemNum: String? = "",
    var materialNumber: String? = "",
    var materialDesc: String? = "",
    var plantId: String? = "",
    var vkorg: String? = "",
    var vtweg: String? = "",
    var Spart: String? = "",
    var soldToPartyName: String? = "",
    var createdDate: String? = "",
    var menge: String? = "",
    var meins: String? = "",
    var openQuantity: String? = "",
    var soldToPartyCode: String? = "",
    var shipToPartyName: String? = "",
    var shipToPartyCode: String? = "",
    @Ignore
    var isView: Boolean? = false
) : Parcelable


@Entity
@Parcelize
data class VegaIndoCoffeeExportSalesOrder(
    @PrimaryKey
    var tmpId: String = "",
    var saleOrderId: String? = "",
    var salesType: String? = "",
    var salesOrderDesc: String? = "",
    var customerId: String? = "",
    var customerName: String? = "",
    var openQuantity: String? = "",
    var unitOfMeasure: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var salesItem: String? = "",
    var createdDate: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var turnAroundTime: String? = "",
    var isSynced: Boolean? = false,
    var isOffline: Boolean? = false,
    var isTransStatus: Boolean? = false,
    var status: Int? = 1,
    var syncStatusMsg: String? = "",
    var message: String? = "",
    var erdat: String? = "",
    var remarks: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    @Ignore
    var isProgress: Boolean? = false,
    @Ignore
    var isView: Boolean? = false,
    var deliveryFlag: Boolean? = false,
    var pickingFlag: Boolean? = false,
    var pgiFlag: Boolean? = false,
    var containerFlag: Boolean? = false,
    var plantName: String? = "",
    @Ignore
    var containerList: ArrayList<VegaCoffeeExportSalesContainer> = ArrayList()
) : Parcelable


@Parcelize
class VegaIndoCoffeeExportSalesOrderModelCommon(
    var salesOrderId: String = "",
    var salesOrderList: List<IndoExporSalesMaterialList> = emptyList()
) : Parcelable
