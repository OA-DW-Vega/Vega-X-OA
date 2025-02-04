package com.olam.warehouse.vegax.createmapar.utils

import com.google.gson.Gson
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.fromJson

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
const val CREATE_MAP = "create_map"
const val RESOLVE_MAP = "resolve_map"
const val MODE = "mode"
const val LOT_LIST = "lot_list"
const val LOT_ID = "lot_ID"
const val LOT = "lot"
const val ANCHOR_NODES = "anchor_nodes"
const val ANCHOR_IDS = "anchor_ids"

fun getPlantDetails(): Plant {
    val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
    return Gson().fromJson<Plant>(plantDetailJson)
}