package com.olam.warehouse.presentation.utils

/**
 * Created by SangiliPandian C on 15-11-2019.
 */
object Constants {
    const val CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE"
    const val CONNECTIVITY_TYPE = "CONNECTIVITY_TYPE"
    const val NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED"
    const val IS_BT_CONNECTED = "IS_BT_CONNECTED"
    const val SWITCH_BT_DEVICE = "SWITCH_BT_DEVICE"
    const val SESSION_EXPIRED = "SESSION_EXPIRED"
    const val LAST_SYNCED_TIME_IN_MILLS = "last_master_sync"
    const val ÏS_LOGGED_OUT = "logged_out"

    const val mDelay = 3000L
    const val CONNECT_TIMEOUT = 20L
    const val READ_TIMEOUT = 20L

    const val BASE_LOCAL_URL = "https://digitalauth.olamnet.com:8443/auth/"

    //    const val BASE_APPCENTER_URL = "https://appcenter-filemanagement-distrib3ede6f06e.azureedge.net/"
    const val BASE_APPCENTER_URL = "https://api.appcenter.ms/v0.1/apps/"

    const val ROOM_SCHEMA_VERSION = 42
    const val ROOM_SCHEMA_VERSION_OD = 14
    const val ROOM_SCHEMA_VERSION_COTTON = 2
    const val ROOM_SCHEMA_VERSION_APP_DATABASE = 3
    const val LOCAL = "local"
    const val APPCENTER = "appcenter"
    const val APPCENTER_URL = "appcenter_url"
    const val KEYCLOAK_URL = "cloak_url"
    const val BASE = "base"
    const val BASE_OD = "base_od"
    const val TRUCK_MANAGE = "truck_manage"
    const val DATABASE = "AppDB"
    const val VEGADATABASE = "VegaDB"
    const val DODATABASE = "DODB"
    const val COTTONDATABASE = "COTTONDB"
    const val CLIENT_ID = "oauth2-app-client"
    const val CLIENT_SECRET = "admin"
    const val SCANNED_ID = "SCANNED_ID"
    const val GRANT_TYPE = "password"
    const val KEYCLOAK_RESET_PASSWORD_URL = "cloak_reset_pass_url"
    const val REPORT_POWER_BI_URL = "report_power_bi_url"
    const val AZURE_URL = "azure_url"

    //UAT
    const val APPCENTER_SECRET_NICA_UAT = "bfadc52c-5245-4b02-b146-76a9a6991fe8"
    const val APPCENTER_SECRET_COFF_UAT = "1cf77a95-63a6-4989-83b9-f2b571494c87"
    const val APPCENTER_SECRET_COCO_UAT = "ec35c1cb-6782-4e71-be18-bb2a7bb7f832"
    const val APPCENTER_SECRET_ECUA_UAT = "c4f33609-b79f-4b2f-80e0-e749e76b91c8"

    //PROD
    const val APPCENTER_SECRET_NICA_PROD = "0657a1f0-f8e5-4de6-9574-9fb9552424d1"
    const val APPCENTER_SECRET_COFF_PROD = "703c256c-c233-4a6a-8ebd-b4a20646c185"
    const val APPCENTER_SECRET_COCO_PROD = "95e8d1aa-cb25-4a7a-9149-74602289818a"
    const val APPCENTER_SECRET_ECUA_PROD = "9e84b08b-0061-448b-8934-83794f3577ec"
    const val APPCENTER_SECRET_SESAME_PROD = "9b51041d-6feb-487f-bbb1-c62405fa45df"
    const val APPCENTER_SECRET_GHANA_CASHEW_PROD = "a7bd43d2-3ad3-40ab-9a66-8219b66f2e16"
    const val APPCENTER_SECRET_NG_CASHEW_PROD = "7426bb5e-e5fa-47e8-96ef-8340b7fa2815"
    const val APPCENTER_SECRET_NG_COCOA_PROD = "d64b3d57-645e-455b-8261-611755b5f490"
    const val APPCENTER_SECRET_CM_COCO_PROD = "357a88bd-1965-4e67-82c7-f15f04f53d4f"
    const val APPCENTER_SECRET_GH_COCO_PROD = "b0c3ad67-017f-4e98-9fc8-711b417f4235"
    const val APPCENTER_SECRET_IVC_CASHEW_PROD = "3d6ddad6-0d9e-4986-bbb6-6b9e9758d20c"
    const val APPCENTER_SECRET_IVC_COTTON = "750929d3-4fe4-47d8-9a4f-dd4d0c189b3d"
    const val APPCENTER_SECRET_INDO_COFFEE = "62ff508e-4a81-4973-99b0-8ee5ad4f0cda"
    const val APPCENTER_SECRET_NICA_PROD_SANITY = "a780754e-8b8b-4baa-9738-2f6f8e5753a3"
    const val APPCENTER_SECRET_TOGO_COTTON = "544d6409-7d80-472f-a348-fdf2b5db0128"
    const val APPCENTER_SECRET_INCOFFEE_PROD = "11c6f692-3239-4622-958e-6d58761f43e8"


    //    const val APPCENTER_SECRET_UAT = "3724591a-f5d8-42b8-8c53-8c2511646a2e"
    const val APPCENTER_SECRET_UAT = "2e605eb4-5067-42de-8577-842a05dc816c"
    const val APPCENTER_SECRET_PROD_OD = "bec494dd-e493-4440-88ea-7b48390af9e0"
    const val APPCENTER_SECRET_UAT_OD = "5470ba22-2ede-40f7-99ae-e784e2bc2f63"
    const val GRANT_TYPE_REFRESH = "refresh_token"
    const val AppCenterApiKeyName = "X-API-Token"

    //    const val AppCenterFullAccessTokenUat = "c062cca69740902adb2557d6cdfb94fbad5d0236"
    const val AppCenterFullAccessTokenUat = "badc56b168ee57b5c1174d59c4fc11988b38c413"
    const val AppCenterFullAccessTokenProd = "92912a13ad357356ad662f8bca6a039030ca6736"

    //Dynatrace Instrumentation
    const val applicationId_Prod = "b908cd2b-550b-43cf-a26e-cb90d12ecc88"
    const val beconUrl_prod =
        "https://eok851.dynatrace-managed.com:9999/mbeacon/8f745a3a-ca48-40b4-aec4-a663c087c19f"

    const val applicationId_uat = "986e79de-a0f1-4df1-8570-37ca4e6b60c1"
    const val beconUrl_uat =
        "https://eok851.dynatrace-managed.com:9999/mbeacon/82203a30-a89c-4afb-8009-c475586c3488"

    //Preferance
    const val ACCESS_TOKEN = "access_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val WAREHOUSE_ID = "WarehouseId"
    const val WERKS = "werks"
    const val USER_NAME = "UserName"
    const val CL_Fetch_UserID = "Cl-Fetch-Userid"
    const val CL_Fetch_Commodity = "Cl-Fetch-Commodity"
    const val CL_Fetch_OperatingUnit = "Cl-Fetch-Operatingunit"
    const val CL_Fetch_Origin = "Cl-Fetch-Origin"
    const val SAME_USER_NAME = "SameUserName"
    const val USER_ROLE = "UserRole"
    const val USER_ROLES = "UserRoles"
    const val RELEASEID = "releaseid"
    const val PASS_WORD = "password"
    const val AUTHORIZATION = "Authorization"
    const val CONTENT_TYPE = "Content-Type"
    const val ACCEPT = "Accept"
    const val X_FRAME_OPTIONS = "X-Frame-Options"
    const val IS_LOGGED_IN = "is_logged_in"
    const val IS_NETWORK_AVAILABLE = "is_network_available"
    const val WEIGH_BRIDGE_ID = "weighbridgeId"
    const val ISWEIGH_BRIDGE_ID = "isweighbridgeIsEmpty"
    const val USER_ONLINE = "user_online"
    const val SCAN_QR = 1
    const val DELIVERY_NUMBER = "deliveryNumber"
    const val DELIVERY_WEIGHT = "deliveryWeight"
    const val SELECTED_STOCKS = 2
    const val SELECTED_STOCKS_LIST = "selectedStocks"
    const val IS_STOCK_FETCHED = "is_stock_fetched"
    const val SELECTED_STOCKS_ID = "selectedStocksId"
    const val IS_SALE = "isSale"
    const val MATERIAL_NUMBER = "materialnumber"
    const val COUNTRY_CODE = "country_code"
    const val PLANT_DETAILS = "plant_details"
    const val PLANT_LIST = "plant_list"
    const val NOTIFIY_MODULE_LIST = "notificaion_module_list"
    const val MTNT_PLANT_LIST = "mtnt_plant_list"
    const val TRANS_LIST = "trans_list"
    const val MULTI_PLANT_LIST = "multiPlantList"
    const val RECEPTION_TYPES = "reception_types"
    const val COMPANY_CODES = "company_codes"
    const val PRODUCTS = "products"
    const val BACK_OFFICE = "back_offices"
    const val KEYS = "keys"
    const val SELECTED_KEYS = "selected_keys"
    const val CURRENT_KEY = "current_key"
    const val SALE_UNIT = "sale_unit"
    const val RECEPTION_TYPE = "reception_type"
    const val PRODUCT = "product"
    const val COMPANY_CODE = "company_code"
    const val PURCHASE_ORG = "purchase_org"
    const val WAREHOUSE_LOCATION = "warehouse_location"
    const val LAST_SYNC = "last_sync"
    const val isRoundOff ="roundoff"
    const val TRANS_LAST_SYNC = "trans_last_sync"
    const val TRANS_FIRST_SYNC = "trans_first_sync"
    const val TITLE = "title"
    const val PALLET_AVG = "pallet_avg"
    const val BATCH_NUMBER = "batch_number"
    const val PALLET_WEIGHT = "pallet_weight"
    const val PALLET_COUNT = "pallet_count"
    const val PALLET_COUNT_EDIT = "pallet_count_edit"
    const val PALLET_EDIT = "pallet_edit"
    const val PALLET_ADDED = "pallet_added"
    const val BAG_MATERIAL = "bag_material"
    const val MATERIAL_LABEL = "lable"
    const val LOT_LABEL = "lot_lable"
    const val LANGUAGE = "languageCode"
    const val WAREHOUSE_LOCATION_CODE = "warehouse_location_code"
    const val DELETE_OCP = "delete"
    const val IS_SECURITY_PIN = "security_pin"
    const val IS_VALID_ENTITY = "valid_entity"
    const val IS_DEVICE_PIN = "device_pin"
    const val QUICK_PIN = "quick_pin"
    const val KEYCLOAK_ID = "keycloak_id"
    const val START_SYNC = "start_sync"
    const val START_SYNC_MTNT = "start_sync_mtnt"
    const val PTBF_INVOICE = "ptbf_invoice"
    const val UOM = "from_ghana"
    const val DEFAULT = "from_ghana_default_value"
    const val SAP_CLOSURE_DAY_COUNT = "sapClosureDaycount"
    const val RESET_PASSWORD = "reset_password"
    const val PLANT_MATERIAL_LIST = "plant_material_list"

    // Do Integration
    const val DO_CLIENT_ID = 6
    const val DO_LANGUAGE_ISO_CODE = "en"
    const val BASE_DO = "base_do"
    const val DO_CLIENT = "do_client"
    const val TRUCK_MANAGE_CLIENT = "truck_manage_client"
    const val APPCENTER_CLIENT = "appcenter_client"
    const val APPCENTER_URL_CLIENT = "appcenter_url_client"
    const val KEYCLOAK_URL_CLIENT = "keycloak_url_client"
    const val DO_BEARER_TOKEN = "do_bearer_token"
    const val VEGA_DO_INTERFACE = "_VEGA_DO_INTERFACE"
    const val LOT_SEQUENCE = "lot_sequence"
    const val INVOICE_SEQUENCE = "invoice_sequence"
    const val GRN_SEQUENCE = "grn_sequence"
    const val MTNT_SEQUENCE = "mtnt_sequence"
    const val MTNR_SEQUENCE = "mtnr_sequence"
    const val PO_SEQUENCE = "po_sequence"
    const val LAST_SYNC_TIME = "last_sync_time"
    const val LOGIN_AT = "login_at"
    const val VIRTUAL = "ROLE_VIRTUAL"
    const val BITMAP_KEYS = "bitmap_keys"
    const val LOT_CARD = "lot_card"
    const val IS_EDIT_TRANS = "is_edit_trans"
    const val IS_EDIT_TRANS_VALUE_CHANGED = "is_edit_trans_value_changed"
    const val PROCUREMENT = "ROLE_PROCUREMENT"
    const val FIRST_TIME_OPENED = "first_time_opened"
    const val NAV_MODULE = "nav_module"
    const val WEIGHBRIDGETYPE = "WEIGHBRIDGE_TYPE"
    const val NAV_BUNDLE = "nav_bundle"
    const val ISDIRECT = "IS_DIRECT"
    const val TALLY_SEQUENCE = "tally_sheet_sequence"
    const val PILE_SEQUENCE = "pile_sequence"
    const val CROP_YEAR = "crop_year"
    const val CROP_FINANCIAL_YEAR = "crop_financial_year"
    const val TALLY_SHEET = "TALLYSHEET"
    const val NEW_PILE = "new_pile"
    const val FGRN_BATCH_SEQUENCE = "fgrn_batch_sequence"
    const val FGRN_TALLY_SEQUENCE = "fgrn_tally_sequence"
    const val FGRN = "FGRN"
    const val MTNR = "MTNR"
    const val FGRN_TICKET = "FGRN_TICKET"
    const val PILE = "PILE"
    const val NOTIFICATION_LIST = "NOTIFICATION_LIST"
    const val MTNT_BATCH_NUMBER = "mtnt_batch_number"
    const val MTNT_VENDOR_DETAIL="mtnt_vendor"
    const val FIRST_TIME_OPENED_OD = "first_time_opened_od"
    const val OFFLOADING = "OFFLOADING"
    const val QUALITY_APPROVAL = "Quality Approval"
    const val TRANSACTIONID = "transaction_id"
    const val WEIGHMENT_TYPE = "weighment type"
    const val MT_NR = "MTN-R"
    const val SUPPLIER = "Supplier"
    const val ADMIN_USER = "admin_user"
    const val NOTIFICATION_CONFIG = "notification config"
    const val DEFAULT_FORWARD_PO = "DEFAULT_FORWARD_PO"


    //port
    const val SEAL_ID = "seal_id"
    //IVCOTTONSTORAGEID
    const val STORAGEID = "storage_id"


    // Name of Notification Channel for verbose notifications of background work
    @JvmField
    val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
        "Verbose WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"

    @JvmField
    val NOTIFICATION_TITLE: CharSequence = "Download Started"
    const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    const val NOTIFICATION_ID = 1

    const val ACTIVITY_SCAN_REQUEST_CODE = 1

    ////////////// HTTP StatusCode //////////////////
    const val UN_AUTHORIZED = 401
    const val NOT_CREATED = 201
    const val LOT_INFO = "lot"

    //Bluetooth
    const val MESSAGE_STATE_CHANGE: Int = 1
    const val MESSAGE_READ = 2
    const val MESSAGE_WRITE = 3
    const val MESSAGE_DEVICE_NAME = 4
    const val MESSAGE_TOAST = 5
    const val TOAST = "toast"

    const val STATE_NONE = 0
    const val STATE_LISTEN = 1
    const val STATE_CONNECTING = 2
    const val STATE_CONNECTED = 3
    const val DEVICE_NAME = "device_name"

    const val ADD_TASK_REQUEST = 1
    const val SCOPE = "offline_access"
    const val DBVEGA = "db_pin"
    const val DVBGA = "vegax123#"
    const val DVBGAOD = "v5g1x123#"
    const val OFI = "OFI"
    const val OGA = "OGA"
    const val CURRENT_ORIGIN_KEY = "CURRENT_ORIGIN_KEY"
    const val IS_BT = "is_bt"
    const val IS_BT_DEVICE = "is_bt_device"
    const val ALL = "All"
    const val ERROR_MSG = "error_msg"
    const val IS_LOGIN = "is_login"
    const val IS_SET_DEFAULT_SIZE = "is_set_default_size"
    const val PROCURE = "PROCURE"
    const val DISABLE_MANUAL_ENTRY = "disable_manual_entry"
    const val PROCUREMENT_TYPE = "procurement_type"
    const val COMPLAINT_TYPE = "complaint_type"
    const val DIRECT = "Direct"
    const val COMPLAINT = "Compliant"
    const val NON_COMPLAINT = "Non-Compliant"
    const val IN_DIRECT = "Indirect"
    const val TRACK_TRACE = "Track_Trace"
    const val TRACK_TRACE_THIRD_PARTY = "Third party"
    const val TRACK_TRACE_FARMERLESS_TRANSACTION = "FarmerLess Transaction"
    const val TRACK_TRACE_FARMERLESS_TRANSACTION_ID = "FarmerLess transaction Id"
    const val SOURCE_LOT = "Source_Lot"
    const val TRANS_ID = "Transaction Id"
    const val SOURCE_LOT_SYNC = "Source_Lot_Sync"
    const val TRANS_ID_SYNC = "Transaction_Id_Sync"
    const val UNKNOWN_ATTRIBUTE = "Unknown Attribute"
    const val NO_DATA_VALUE = "NA"
    const val SOURCE_LOT_PARAMS = "SOURCE_LOT"
    const val COMPLAINCE_PARAMS = "COMPLIANCE"
    const val EUDR_QP_VALUE = "EUDR Compliant"
    const val ATTR_UNKNOWN_QP_VALUE = "Attribute Unknown"
    const val EUDR_STATUS_COLON = "EUDR Status : "
    const val PURCHASE_TYPE = "purchase_type"
    const val FTDC_LIST = "ftdc_list"
    const val CROP_LIMIT = "Crop Limit"
    const val FARMER = "Farmer"
    const val VENDOR = "Vendor"
    const val ARAB = "ARAB"
    const val ROBU = "ROBU"
    const val DISABLE_THIRD_PARTY = "disable_third_party"
    const val FARMERLESS_TRANSACTION_ID_PREFIX = "FT_ID-"

}
