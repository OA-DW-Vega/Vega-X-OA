package com.olam.warehouse.vegax.mtntnicaragua.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 11/19/2020.
 */

@Parcelize
data class VegaNicLotListModel(
    var selectedList: ArrayList<VegaNicDispatchLots> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var material: ArrayList<String> = ArrayList(),
    var isThirdParty: Boolean = false,
    var mtnt: VegaNicaraguaMtnt? = VegaNicaraguaMtnt()
) : Parcelable

@Parcelize
data class VegaDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityGrade: String? = "",
    var qualityParams: Quality = Quality(),
    var certification: String? = ""
) : Parcelable


@Parcelize
data class VegaMtnrReprintList(
    var date: String = "",
    var id: Int = 0,
    var materialName: String = "",
    var moduleNo: String? = "",
    var transactionNo: String? = "",
    var fileType: String = "",
    var isProgress: Boolean = false
) : Parcelable

@Parcelize
data class Quality(
    var NISACOS: String = ""
) : Parcelable
