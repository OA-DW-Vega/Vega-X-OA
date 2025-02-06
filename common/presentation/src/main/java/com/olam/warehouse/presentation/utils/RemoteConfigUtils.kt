package com.olam.warehouse.presentation.utils

/**
 * Created by Baskaran Kannan on 7/29/2020.
 */

//Base
const val BASE_PATH = "base_apk_path"
const val PROD_BASE_PATH = "prod_base_apk_path"

//Cashew
//UAT
const val CASHEW_RECEIVING_PATH = "cashew_receiving_apk_path"
const val CASHEW_QUALITY_PATH = "cashew_quality_apk_path"
const val CASHEW_OFFLOADING_PATH = "cashew_offloading_apk_path"
const val CASHEW_PROCESSING_PATH = "cashew_processing_apk_path"
const val CASHEW_DISPATCH_PATH = "cashew_dispatch_apk_path"
const val CASHEW_GATEENTRY_PATH = "cashew_gateentry_apk_path"
const val CASHEW_GATEENTRY_APPROVAL_PATH = "cashew_gateentry_approval_apk_path"
const val CASHEW_APPROVE_PATH = "cashew_approve_apk_path"
const val CASHEW_INVENTORY_PATH = "cashew_inventory_apk_path"
//PROD
const val PROD_CASHEW_RECEIVING_PATH = "prod_cashew_receiving_apk_path"
const val PROD_CASHEW_QUALITY_PATH = "prod_cashew_quality_apk_path"
const val PROD_CASHEW_OFFLOADING_PATH = "prod_cashew_offloading_apk_path"
const val PROD_CASHEW_PROCESSING_PATH = "prod_cashew_processing_apk_path"
const val PROD_CASHEW_DISPATCH_PATH = "prod_cashew_dispatch_apk_path"
const val PROD_CASHEW_GATEENTRY_PATH = "prod_cashew_gateentry_apk_path"
const val PROD_CASHEW_GATEENTRY_APPROVAL_PATH = "prod_cashew_gateentry_approval_apk_path"
const val PROD_CASHEW_APPROVE_PATH = "prod_cashew_approve_apk_path"
const val PROD_CASHEW_INVENTORY_PATH = "prod_cashew_inventory_apk_path"

//Cocoa
//UAT
const val COCOA_MTNT_PATH = "cocoa_mtnt_apk_path"
const val COCOA_SALES_PATH = "cocoa_sales_apk_path"
const val COCOA_PROCESSING_PATH = "cocoa_processing_apk_path"
const val COCOA_INVENTORY_PATH = "cocoa_inventory_apk_path"
const val COCOA_SWEEPING_PATH = "cocoa_sweeping_apk_path"
//PROD
const val PROD_COCOA_MTNT_PATH = "prod_cocoa_mtnt_apk_path"
const val PROD_COCOA_SALES_PATH = "prod_cocoa_sales_apk_path"
const val PROD_COCOA_PROCESSING_PATH = "prod_cocoa_processing_apk_path"
const val PROD_COCOA_INVENTORY_PATH = "prod_cocoa_inventory_apk_path"
const val PROD_COCOA_SWEEPING_PATH = "prod_cocoa_sweeping_apk_path"

//Coffee
//UAT
const val COFFEE_INVENTORY_PATH = "coffee_inventory_apk_path"
const val COFFEE_PROCESSING_PATH = "coffee_processing_apk_path"
const val COFFEE_PPQ_PATH = "coffee_ppq_apk_path"
const val COFFEE_CONTAINER_PATH = "coffee_container_apk_path"

//Prod
const val PROD_COFFEE_INVENTORY_PATH = "prod_coffee_inventory_apk_path"
const val PROD_COFFEE_PROCESSING_PATH = "prod_coffee_processing_apk_path"
const val PROD_COFFEE_PPQ_PATH = "prod_coffee_ppq_apk_path"
const val PROD_COFFEE_CONTAINER_PATH = "prod_coffee_container_apk_path"

//ECUADOR
//UAT
const val ECUADOR_APPROVE_PATH = "ecuador_approve_apk_path"
const val ECUADOR_DISPATCH_PATH = "ecuador_dispatch_apk_path"
const val ECUADOR_GRN_PATH = "ecuador_grn_apk_path"
const val ECUADOR_INVENTORY_PATH = "ecuador_inventory_apk_path"
const val ECUADOR_OFFLOADING_PATH = "ecuador_offloading_apk_path"
const val ECUADOR_QUALITY_PATH = "ecuador_quality_apk_path"

//PROD
const val PROD_ECUADOR_APPROVE_PATH = "prod_ecuador_approve_apk_path"
const val PROD_ECUADOR_DISPATCH_PATH = "prod_ecuador_dispatch_apk_path"
const val PROD_ECUADOR_GRN_PATH = "prod_ecuador_grn_apk_path"
const val PROD_ECUADOR_INVENTORY_PATH = "prod_ecuador_inventory_apk_path"
const val PROD_ECUADOR_OFFLOADING_PATH = "prod_ecuador_offloading_apk_path"
const val PROD_ECUADOR_QUALITY_PATH = "prod_ecuador_quality_apk_path"

fun getUatFeatureList(): List<String> {
    return listOf<String>(
        BASE_PATH,
        CASHEW_RECEIVING_PATH,
        CASHEW_QUALITY_PATH,
        CASHEW_OFFLOADING_PATH,
        CASHEW_PROCESSING_PATH,
        CASHEW_DISPATCH_PATH,
        CASHEW_GATEENTRY_PATH,
        CASHEW_APPROVE_PATH,
        CASHEW_INVENTORY_PATH,
        COCOA_MTNT_PATH,
        COCOA_SALES_PATH,
        COCOA_PROCESSING_PATH,
        COCOA_INVENTORY_PATH,
        COCOA_SWEEPING_PATH,
        ECUADOR_DISPATCH_PATH,
        ECUADOR_GRN_PATH,
        ECUADOR_INVENTORY_PATH,
        ECUADOR_OFFLOADING_PATH,
        ECUADOR_QUALITY_PATH,
        COFFEE_INVENTORY_PATH,
        COFFEE_PROCESSING_PATH,
        COFFEE_PPQ_PATH
    )
}

fun getProdFeatureList(): List<String> {
    return listOf<String>(
        PROD_BASE_PATH,
        PROD_CASHEW_RECEIVING_PATH,
        PROD_CASHEW_QUALITY_PATH,
        PROD_CASHEW_OFFLOADING_PATH,
        PROD_CASHEW_PROCESSING_PATH,
        PROD_CASHEW_DISPATCH_PATH,
        PROD_CASHEW_GATEENTRY_PATH,
        PROD_CASHEW_APPROVE_PATH,
        PROD_CASHEW_INVENTORY_PATH,
        PROD_COCOA_MTNT_PATH,
        PROD_COCOA_SALES_PATH,
        PROD_COCOA_PROCESSING_PATH,
        PROD_COCOA_INVENTORY_PATH,
        PROD_COCOA_SWEEPING_PATH,
        PROD_ECUADOR_DISPATCH_PATH,
        PROD_ECUADOR_GRN_PATH,
        PROD_ECUADOR_INVENTORY_PATH,
        PROD_ECUADOR_OFFLOADING_PATH,
        PROD_ECUADOR_QUALITY_PATH
    )
}
