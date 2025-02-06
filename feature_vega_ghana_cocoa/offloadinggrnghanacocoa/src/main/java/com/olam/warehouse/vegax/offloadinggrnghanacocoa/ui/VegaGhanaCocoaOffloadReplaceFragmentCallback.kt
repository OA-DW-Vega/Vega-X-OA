package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui


interface VegaGhanaCocoaOffloadReplaceFragmentCallback {
    fun replaceFragment(
        receivingType: String,
        data: Any
    )

    fun replaceFragment(
        receivingType: String,
        data: Any,
        datas: Any
    )

    fun replaceFragment(
        receivingType: String,
        data: Any,
        rejectList:HashMap<String, String>,
        acceptedBagCount: String
    )

    fun replaceFragment(
        receivingType: String,
        data: Any,
        wbDetails: Any,
        rejectList: HashMap<String, String>,
        acceptedBagCount: String
    )

    fun replaceFragment(
        receivingType: String,
        data: Any,
        wbDetails: Any,
        rejectList: HashMap<String, String>,
        acceptedBagCount: String,
        batchList: Any
    )


    fun replaceFragment(moveFrag: String)
}
