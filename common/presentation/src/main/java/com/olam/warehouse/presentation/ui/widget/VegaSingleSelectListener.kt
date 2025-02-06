package com.olam.warehouse.presentation.ui.widget

interface VegaSingleSelectListener {
    fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean = false, isGrade: Boolean)
}

interface VegaSingleSelectCommonListener {
    fun clickOnItem(data: String, currentFlag: String)
}

interface VegaCoffeeSingleSelectListener {
    fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean = false, isGrade: Boolean, isSupplier: Boolean,isOrigin:Boolean,isDepartment:Boolean)
}

interface VegaNicaraguaSingleSelectListener {
    fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean = false, isGrade: Boolean, isLocation: Boolean)
}


interface VegaMultiSelectCommonListener {
    fun clickOnItem(data: List<String>, isSelected: Boolean)
}
