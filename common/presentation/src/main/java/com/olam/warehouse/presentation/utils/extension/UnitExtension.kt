package com.olam.warehouse.presentation.utils.extension

fun weightConverter(bagUom: String?, receivingUom: String?, tareWeight: Double?): Double? {
    return when {
        bagUom.equals("KG", true) && receivingUom.equals("MT", true) -> tareWeight?.div(1000)
        bagUom.equals("MT", true) && receivingUom.equals("KG", true) -> tareWeight?.times(1000)

        bagUom.equals("LB", true) && receivingUom.equals("QQS", true) -> tareWeight?.div(101.4127233)
        bagUom.equals("QQS", true) && receivingUom.equals("LB", true) -> tareWeight?.times(101.4127233)

        else -> tareWeight
    }
}
