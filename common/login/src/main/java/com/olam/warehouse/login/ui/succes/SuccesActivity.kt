package com.olam.warehouse.login.ui.succes

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.text.Html
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.*
import com.olam.warehouse.login.ui.FrequentlyAskedQActivity
import com.olam.warehouse.login.ui.common.VegaCommonModuleNavigation
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.printformats.entrySheetCMCocoPrint
import com.olam.warehouse.login.ui.printformats.generateLotCardPrint
import com.olam.warehouse.login.ui.printformats.generatePeruGrnTallySheet
import com.olam.warehouse.login.ui.printformats.generateSecretQRCode
import com.olam.warehouse.login.ui.printformats.printForAnyLotQualityParams
import com.olam.warehouse.login.ui.printformats.nigeriaCocoaBagMgmtPrintRecipt
import com.olam.warehouse.login.ui.printformats.offloadingPrint
import com.olam.warehouse.login.ui.printformats.qualityApprovalDDNCMPrintFormat
import com.olam.warehouse.login.ui.printformats.stockReconPrintRecipt
import com.olam.warehouse.login.ui.printformats.ticketPrint
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaStockReconBagDetails
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnPost
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.VegaNicaPileDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPost
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoiceGrnInventoryModal
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import com.olam.warehouse.master.veganigeria.model.VegaSesamePpqInspectionLotDetails
import com.olam.warehouse.navigation.features.VegaCommonSplitLotNavigation
import com.olam.warehouse.presentation.ui.BluetoothService
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.EUDR_STATUS
import com.olam.warehouse.presentation.utils.AppUtils.FAILURE
import com.olam.warehouse.presentation.utils.AppUtils.IVC_GRN_RECEIPT
import com.olam.warehouse.presentation.utils.AppUtils.IVC_OFFLOAD_RECEIPT
import com.olam.warehouse.presentation.utils.AppUtils.SUB_TITLE
import com.olam.warehouse.presentation.utils.AppUtils.TITLE
import com.olam.warehouse.presentation.utils.Constants.IS_SET_DEFAULT_SIZE
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.min


/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class SuccessActivity : HomeBaseActivity() {

    private lateinit var binding: ActivitySuccessBinding
    override val layoutResourceId = R.layout.activity_success
    private var isPrintNeed: Boolean = false
    private var fromCoffee = false
    private var fromSesame = false
    private var fromSesameGrn = false
    private var fromSesameProcessing = false
    private var fromNicarguaCoffee = false
    private var fromNicarguaUpcountry = false
    private var fromCameroonCocoaGateEntry = false
    private var fromCameroonCocoaQA = false
    private var fromNigeriaCocoaQA = false
    private var fromNigeriaCocoaBagMgmt = false
    private var fromCameroonCocoaOffloading = false
    private var fromCameroonCocoafgrn = false
    private var fromCameroonCocoaaddContainer = false
    private var nicaraguaMtntScanObdNumber = false
    private var fromAnyLotQuality = false
    private var nicaraguaMtntTruckNo = ""
    private var nicaraguaMtntObdNumber = ""
    private var isGhana = false
    private var isDOGhana = false
    private var tallyPrintKeys = ArrayList<String>()
    private var tallyPrintKeysNew = ArrayList<String>()
    private var lotList = ArrayList<VegaCoffeeSalesLots>()
    private var whList = ArrayList<String>()
    private var grnDoc = ArrayList<String>()
    private var mould: Double = 0.000
    private var slaty: Double = 0.000
    private var fromCoffeeGrn = false
    private var printTicket = false
    private var printFgrnTallySheet = false
    private var printLocalSalesTallySheet = false
    private var fgrnBatch = "AB21000078"
    private var ismergedlot: Boolean = false
    private var isSplitdlot: Boolean = false
    private var isSecretId: Boolean = false
    private var splitLotItems = arrayListOf<VegaCoffeeLot>()
    private var  receivingData_splitlot  = VegaReceiving()
    private var errorHyperlinkMsg=""
    private var fgrnReq = VegaCocoaProcessingFgrnPost()
    private var ppqReq = VegaSesamePpqInspectionLotDetails()
    private var bagdetails = VegaStockReconBagDetails()
    private var auditDetails = ArrayList<VegaStockReconGetAllAuditData>()
    private var eudrStatus:String = ""


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
    }

    override fun onBackPressed() {
        if (fromCoffee) showLotPrintExitDialog()
        else if(isSplitdlot) showSplitLotExitDialog()
        else {
            moveToHomePage()
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    moveToHomePage()
                }
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initUI() {
        val title = intent?.getStringExtra(TITLE)
        val subTitle = intent?.getStringExtra(SUB_TITLE)
        eudrStatus = intent?.getStringExtra(EUDR_STATUS)?:""
        isPrintNeed = intent?.getBooleanExtra(AppUtils.PRINT_ENABLE, false) ?: false
        tallyPrintKeys = intent?.getStringArrayListExtra(AppUtils.TALLY_SHEETS) ?: ArrayList()
        tallyPrintKeysNew = intent?.getStringArrayListExtra(AppUtils.TALLY_SHEETS_NEW) ?: ArrayList()
        lotList = intent?.getParcelableArrayListExtra(AppUtils.LOT_CARD) ?: ArrayList()
        whList = intent?.getStringArrayListExtra(AppUtils.WH_RECEIPT) ?: ArrayList()
        grnDoc = intent?.getStringArrayListExtra(AppUtils.GRN_DOCUMENT) ?: ArrayList()

        fromAnyLotQuality= intent?.getBooleanExtra(UIUtils.ANY_LOT_QUALITY_PRINT,false)?:false
        isSecretId= intent?.getBooleanExtra(AppUtils.IS_SECRET_ID_QR,false)?:false
        fromCoffee = intent?.getBooleanExtra("fromcoffee", false) ?: false
        fromSesame = intent?.getBooleanExtra("fromsesame", false) ?: false
        fromSesameGrn = intent?.getBooleanExtra("fromsesamegrn", false) ?: false
        fromSesameProcessing = intent?.getBooleanExtra("fromsesameprocessing", false) ?: false
        fromNicarguaCoffee = intent?.getBooleanExtra(UIUtils.FROM_NICARAGUA_COFFEE, false) ?: false
        fromNicarguaUpcountry = intent?.getBooleanExtra(UIUtils.FROM_NICARAGUA_COFFEE, false) ?: false
        fromCameroonCocoaGateEntry =
            intent?.getBooleanExtra(UIUtils.FROM_CAMEROON_GATEENTRY_COCOA, false) ?: false
        fromCameroonCocoaQA =
            intent?.getBooleanExtra(UIUtils.FROM_CAMEROON_COCOA_QA, false) ?: false
        fromNigeriaCocoaQA = intent?.getBooleanExtra(UIUtils.FROM_NIGERIA_COCOA_QA, false) ?: false
        fromNigeriaCocoaBagMgmt = intent?.getBooleanExtra(UIUtils.FROM_NIGERIA_COCOA_BAG_MGMT, false) ?: false
        fromCameroonCocoaOffloading =
            intent?.getBooleanExtra(UIUtils.FROM_CAMEROON_COCOA_OFFLOADING, false) ?: false
        fromCameroonCocoafgrn =
            intent?.getBooleanExtra(UIUtils.FROM_CAMEROON_COCOA_FGRN, false) ?: false
        fromCameroonCocoaaddContainer =
            intent?.getBooleanExtra(UIUtils.FROM_CAMEROON_COCOA_ADD_CONTAINER, false) ?: false
        nicaraguaMtntScanObdNumber =
            intent?.getBooleanExtra(UIUtils.NICARAGUA_MTNT_SCAN_OBD_NUMBER, false) ?: false
        nicaraguaMtntTruckNo =
            intent?.getStringExtra(UIUtils.NICARAGUA_MTNT_TRUCK_NO) ?: "trucknumber"
        nicaraguaMtntObdNumber =
            intent?.getStringExtra(UIUtils.NICARAGUA_MTNT_OBD_NUMBER) ?: "obdnumber"
        isGhana = intent?.getBooleanExtra(UIUtils.FROM_GHANA, false) ?: false
        isDOGhana = intent?.getBooleanExtra("from_do_ghana_cash", false) ?: false
        fromCoffeeGrn = intent?.getBooleanExtra(UIUtils.FROM_NIC_MTNR_GRN, false) ?: false
        printTicket = intent?.getBooleanExtra(UIUtils.PRINT_TICKET, false) ?: false
        printFgrnTallySheet = intent?.getBooleanExtra(UIUtils.PRINT_FGRN_TALLYSHEET, false) ?: false
        fgrnBatch = intent?.getStringExtra(UIUtils.DISPATCH_BATCH) ?: "AB21000056"
        printLocalSalesTallySheet =
            intent?.getBooleanExtra(UIUtils.PRINT_LOCAL_SALES_TALLY_SHEET, false) ?: false
        //lotList = intent?.getParcelableExtra<VegaReceiving>(UIUtils.LOT_DETAILS_LOCAL_SALES) ?: VegaCameroonS()

        if(eudrStatus.isNotEmpty()){
            updateEudrStatusFlag()
        }
        if (intent?.hasExtra(FAILURE) == true) {
            binding.tvHyperLink.visible()
            binding.animationFailView.visible()
            binding.animationView.gone()
            errorHyperlinkMsg= intent?.getStringExtra(AppUtils.MSG).toString()
        } else {
            binding.tvHyperLink.gone()
            binding.animationFailView.gone()
            binding.animationView.visible()
        }
        if (isGhana) {
            binding.btnWithquality.visibility = View.VISIBLE
        }
        if (isDOGhana) {
            binding.btnWithquality.visibility = View.VISIBLE
        }
        if (intent?.hasExtra(AppUtils.STOCK_RECON_CARD) == true) {
            binding.btnStockPrint.visible()
        }

        binding.tvTitle.text = title
        binding.tvSubTitle.text = subTitle
        binding.btnOk.setOnClickListener {
            if (fromCoffee) showLotPrintExitDialog()
            else if(isSplitdlot) showSplitLotExitDialog()
            else moveToHomePage() }

        if (printTicket) {
            binding.printFgrnTallySheet.visible()
            binding.printFgrnTallySheet.text = getString(R.string.sample_ticket)
            binding.printTicket.visible()
            binding.btnPrint.gone()
        }
        binding.printTicket.setOnClickListener {
            if (getCurrentKey().split("_")[1].contains("NI"))
                if(intent.hasExtra(UIUtils.PILE_PRINT_DETAILS)){
                    createTicketCardPileNicaragua()
                }else createTicketCardBitMapNicaragua()
            else
                createTicketCardBitMap()
        }
        if (printFgrnTallySheet) {
            if(getCurrentKey().contains("VEGA") && getCurrentKey().contains("NI")){
                binding.printFgrnTallySheet.gone()
                binding.printTicket.visible()
            }else{
                binding.btnPrint.gone()
                binding.printFgrnTallySheet.visible()
            }
        }
        binding.printFgrnTallySheet.setOnClickListener {
            generateFgrnTallySheetBitMap()
        }

        if (printLocalSalesTallySheet) {
            binding.btnPrint.visibility = View.GONE
            binding.printLocalSalesTallySheet.visible()
        }
        binding.printLocalSalesTallySheet.setOnClickListener {
            printLocalSalesTallysheet()
        }

        if (fromCoffee) {
            binding.btnPrint.text = getString(R.string.print_lot_card)
        }
        if (fromSesame) {
            binding.btnPrint.text = getString(R.string.print_lot_card)
            binding.btnWHPrint.text = getString(R.string.print_wh_receipt)
            binding.btnWHPrint.visibility = if (isPrintNeed) View.VISIBLE else View.GONE
        }
        if (fromSesameProcessing || fromCameroonCocoaOffloading || fromCameroonCocoafgrn) {
            binding.btnPrint.text = getString(R.string.print_lot_card)
            binding.btnWHPrint.text = getString(R.string.print_preview)
            binding.btnWHPrint.visibility = if (isPrintNeed) View.VISIBLE else View.GONE
        }
        if (fromSesameGrn) {
            binding.btnPrint.text = getString(R.string.print_grn)
        }
        if (fromCameroonCocoaGateEntry) {
            binding.btnPrint.text = getString(R.string.print_secret_code)
        }
        if (fromCameroonCocoaaddContainer) {
            binding.btnPrint.text = getString(R.string.print_container_id)
        }

        binding.btSplitLot.setOnClickListener {
            VegaCommonSplitLotNavigation.dynamicStart?.let {
                it.putExtra(UIUtils.LOT_DETAIL, splitLotItems)
                it.putExtra(UIUtils.RECEIVING_DATA, receivingData_splitlot)
                startActivity(it)
            }
        }

        binding.tvHyperLink.setOnClickListener {
            val intent = Intent(this, FrequentlyAskedQActivity::class.java)
            intent.putExtra(Constants.ERROR_MSG, errorHyperlinkMsg)
            startActivity(intent)

        }
        /*if (fromCameroonCocoaQA) {
            binding.btnPrint.text = getString(R.string.print_lot_card)
            binding.btnWHPrint.text = getString(R.string.print_grn)
            binding.btnWHPrint.visibility = if (isPrintNeed) View.VISIBLE else View.GONE
        }*/
/*        if(fromCameroonCocoaOffloading){
            binding.btnPrint.text = getString(R.string.print_lot_card)
            binding.btnWHPrint.text = getString(R.string.print)
            binding.btnWHPrint.visibility = if (isPrintNeed) View.VISIBLE else View.GONE
        }*/
        binding.btnPrint.setOnClickListener {
            //fromCameroonCocoaQA
            if (fromCoffee || fromSesame || fromSesameProcessing || fromCameroonCocoaGateEntry || fromCameroonCocoaOffloading || fromCameroonCocoafgrn || fromCameroonCocoaaddContainer) {
                // bitmapKey.add(key)
                printerModule()
            } else if (fromSesameGrn) {
                showPreviewDialog()
            }else if(fromAnyLotQuality){
                binding.btnPrint.text= getString(R.string.download_pdf)
                printQualityParams()

            } else if(fromNigeriaCocoaBagMgmt){
                DoAsync {
                    tallyPrintKeys = nigeriaCocoaBagMgmtPrintRecipt(intent, this)
                    runOnUiThread {
                        showPreviewDialog()
                    }
                }.execute()
            }else
                showPreviewDialog()
        }
        if (nicaraguaMtntScanObdNumber) {
            binding.btnObdScan.visibility = View.GONE
            binding.btnObdScan.setOnClickListener {
                val i: Intent = Intent(this, NicaraguaMtntObdScan::class.java)
                i.putExtra("truckNo", nicaraguaMtntTruckNo)
                i.putExtra("Obdnumber", nicaraguaMtntObdNumber)
                startActivity(i)
            }
        }

        /*binding.printLotCard.setOnClickListener {
            if (fromSesameProcessing) {
                printerModule()
            }
        }*/
        binding.btnWHPrint.setOnClickListener {
            if(fromCameroonCocoaOffloading){
                tallyPrintKeys = intent?.getStringArrayListExtra(AppUtils.TALLY_SHEETS) ?: ArrayList()
            }
            if (fromSesame) {
                // bitmapKey.add(key)
                showPreviewDialog()
            } else if (fromSesameProcessing || fromCameroonCocoaOffloading || fromCameroonCocoafgrn) {
                showPreviewDialog()
            }
//  ftvcontractnumber          else if (fromCameroonCocoaQA) {
//                showGRNDocPreviewDialog()
//            }

//            else
//                showPreviewDialog()
        }
        binding.btnWithpoPrint.setOnClickListener {
            DoAsync {

                var view = LayoutInflater.from(this)
                    .inflate(R.layout.vega_nicaragua_forwardpo_receipt_printing, null)
                val receivingData =
                    intent?.getParcelableExtra<VegaNicaraguaForwardPoPost>(UIUtils.RECEIVING_DATA)
                        ?: VegaNicaraguaForwardPoPost()
                val pricegrosskg = intent?.getStringExtra(UIUtils.NETWEIGHT)
                var viewBinder = VegaNicaraguaForwardpoReceiptPrintingBinding.bind(view)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                viewBinder.tvvendor.text =
                    receivingData.vendorName + "\n" + receivingData.vendorAddress + "\n" + receivingData.taxNumber
                /* var separated: Array<String>
             if(receivingData.docDate!!.contains("-"))
             {
                  separated = receivingData.docDate.toString().split("-").toTypedArray()

             }else
             {
               var datestringformated= DateUtils.getFormatedDate(receivingData.docDate.toString())
                separated = datestringformated.spli-t("/").toTypedArray()

                }
             val localDate: LocalDate = LocalDate.of(separated[2].trim().toInt(),separated[1].trim().toInt(),separated[0].trim().toInt())
             val spanishLocale = Locale("es", "ES")
             val dateInSpanish: String =
                 localDate.format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy", spanishLocale))*/
                viewBinder.tvcontractdate.text =
                    DateUtils.convertDateToSpanish(receivingData.docDate.toString())
                var deliverydate =
                    DateUtils.convertDateToSpanish(receivingData.deliveryDate.toString())
                viewBinder.tvdeliveryterm.text = deliverydate.replace("-", " de ")
                viewBinder.tvQuantity.text =
                    pricegrosskg + " " + resources.getString(R.string.qq) + " " + resources.getString(
                        R.string.equivalente
                    ) + " " + receivingData.netWeight + " " + resources.getString(
                        R.string.kg
                    )
                viewBinder.tvcontractnumber.text = receivingData.poSequenceNumber
                viewBinder.tvProductName.text = receivingData.materialName
                viewBinder.tvQualityGrade.text = receivingData.qualityGradeDesc
                viewBinder.tvpricegrosskg.text =
                    resources.getString(R.string.c_doller) + " " + receivingData.pricePerUnit
                viewBinder.tvnetpricexQQPOA.text =
                    resources.getString(R.string.c_doller).plus(" ")
                        .plus(receivingData.receiptNetPrice)
                viewBinder.tvplaceOfDelivery.text =
                    resources.getString(R.string.s_buying_unit) + " " + receivingData.plant?.plantName
                viewBinder.tvdata2.text = receivingData.vendorName
                viewBinder.buyer.text =
                    resources.getString(R.string.print_title_1) + "\n" + resources.getString(R.string.print_title_3) + "\n" + resources.getString(
                        R.string.print_title_2
                    )
                viewBinder.title2.text =
                    resources.getString(R.string.direction) + " " + resources.getString(R.string.print_title_3)

                tallyPrintKeys.clear()
                tallyPrintKeys.add(
                    bitmapToString(
                        getBitmapFromView(
                            view, Color.WHITE
                        )
                    )
                )
                runOnUiThread {
                    showPreviewDialog()
                }

            }.execute()
        }
        binding.btnWithquality.setOnClickListener {

            if (isGhana) {
                val receivingData =
                    intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA)
                        ?: VegaReceiving()
                val Weighbridgetype = intent?.getStringExtra("Weighbridgetype")
                val intent = Intent(this, VegaCommonModuleNavigation::class.java)
                var wbDetails: VegaQualityWBDetails? = VegaQualityWBDetails()
                wbDetails?.weighBridgeId = receivingData.weighBridgeId
                wbDetails?.weighBridgeType = receivingData.weighBridgeType
                wbDetails?.grossWeight = receivingData.grossWeight
                wbDetails?.bagWeight = receivingData.bagWeight

                val bundle = Bundle()
                bundle.putParcelable("wbdetails", wbDetails)
                intent.putExtra(Constants.NAV_MODULE, "QUALITY")
                intent.putExtra(Constants.WEIGHBRIDGETYPE, Weighbridgetype)
                intent.putExtra(Constants.NAV_BUNDLE, bundle)

                startActivity(intent)
            } else if (isDOGhana) {
                val receivingData =
                    intent?.getParcelableExtra<DOReceiving>("receivingData") ?: DOReceiving()
                val Weighbridgetype = intent?.getStringExtra("Weighbridgetype")
                val intent = Intent(this, VegaCommonModuleNavigation::class.java)
                var wbDetails: DOQualityWBDetails? = DOQualityWBDetails()
                wbDetails?.weighBridgeId = receivingData.wbId
                wbDetails?.grossWeight = receivingData.grossWeight.toString()
                wbDetails?.bagWeight = receivingData.bagWeight.toString()

                val bundle = Bundle()
                bundle.putParcelable("wbdetails", wbDetails)
                intent.putExtra(Constants.NAV_MODULE, "DOQUALITY")
                intent.putExtra(Constants.WEIGHBRIDGETYPE, Weighbridgetype)
                intent.putExtra(Constants.NAV_BUNDLE, bundle)

                startActivity(intent)
            }

        }
        binding.btnPrint.visibility = if (isPrintNeed) View.VISIBLE else View.GONE
        prepareBitmapForPrinting()
        prepareBitmapForCameroonGrnPrinting()
        changeButtonStyles()

        if(isSecretId){
            prepareCommonSecretSample()
        }

        binding.btnStockPrint.setOnClickListener {
            bagdetails = intent.getParcelableExtra(AppUtils.BAG_DETAILS) ?: VegaStockReconBagDetails()
            auditDetails = intent.getParcelableArrayListExtra(AppUtils.AUDIT_DETAILS) ?: ArrayList()

            DoAsync {
                tallyPrintKeys = stockReconPrintRecipt(bagdetails, auditDetails, this)
                runOnUiThread {
                    showPreviewDialog()
                }
            }.execute()

        }
        if(fromNigeriaCocoaBagMgmt){
            binding.btnPrint.visible()
            binding.btnPrint.text = getString(R.string.print_receipt)
        }
        if(intent?.hasExtra(IVC_GRN_RECEIPT) == true){
            binding.grnReceipt.visible()
            var wbDetails = VegaGrnWeighBridgeId()
            wbDetails = intent?.getParcelableExtra(IVC_GRN_RECEIPT)?: VegaGrnWeighBridgeId()
            binding.grnReceipt.setOnClickListener {
                tallyPrintKeys = generatePeruGrnTallySheet(intent, this)
                runOnUiThread {
                    val gson = GsonUtils()
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                    startActivity(Intent(this, WifiMainActivity::class.java))
                }
            }
        }
        if(intent?.hasExtra(IVC_OFFLOAD_RECEIPT) == true){
            binding.printLotCard.visible()
            binding.grnReceipt.visible()
            binding.grnReceipt.text = getString(R.string.print_receipt)
            var vegaCoCoaReceivingData = VegaCoCoaReceiving()
            vegaCoCoaReceivingData = intent?.getParcelableExtra(IVC_OFFLOAD_RECEIPT)?: VegaCoCoaReceiving()
            binding.printLotCard.setOnClickListener {
                tallyPrintKeys = generateLotCardPrint(intent, this)
                runOnUiThread {
                    val gson = GsonUtils()
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                    startActivity(Intent(this, WifiMainActivity::class.java))
                }
            }
            binding.grnReceipt.setOnClickListener {
                tallyPrintKeys = offloadingPrint(intent, this)
                runOnUiThread {
                    val gson = GsonUtils()
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                    startActivity(Intent(this, WifiMainActivity::class.java))
                }
            }

        }
    }

    fun updateEudrStatusFlag() {
        binding.eudrStatus.llEudrStatus.visible()
        if (eudrStatus.equals("1")) {
            binding.eudrStatus.tvEudrStatusValue.text = Constants.EUDR_QP_VALUE
            binding.eudrStatus.llEudrStatus.setBackground(resources.getDrawable(com.olam.warehouse.login.R.drawable.rounded_corners_green))
            binding.eudrStatus.ivEudrFlag.setImageResource(com.olam.warehouse.login.R.drawable.ic_eudr_complaint_flag)
        } else {
            binding.eudrStatus.tvEudrStatusValue.text = Constants.ATTR_UNKNOWN_QP_VALUE
            binding.eudrStatus.llEudrStatus.setBackground(resources.getDrawable(com.olam.warehouse.login.R.drawable.rounded_corners_red))
            binding.eudrStatus.ivEudrFlag.setImageResource(com.olam.warehouse.login.R.drawable.ic_attr_unknown_flag)
        }

        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("CASH")){
            binding.eudrStatus.llEudrStatus.gone()
        }
    }

    private fun changeButtonStyles() {
        getActionBtnChangedView(binding.btnWHPrint, this, true)
        getActionBtnChangedView(binding.btnPrint, this, true)
        getActionBtnChangedView(binding.grnReceipt, this, true)
        getActionBtnChangedView(binding.invoicePrinting, this, true)
        getActionBtnChangedView(binding.printLotCard, this, true)
        getActionBtnChangedView(binding.btnMtntPrint, this, true)
        getActionBtnChangedView(binding.btnCertificationPrint, this, true)
        getActionBtnChangedView(binding.btnWithholdTaxPrint, this, true)
        getActionBtnChangedView(binding.btnWithpoPrint, this, true)
        getActionBtnChangedView(binding.btnOk, this, true)
    }

    private fun createTicketCardBitMap() {
        tallyPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            val view = LayoutInflater.from(this).inflate(R.layout.item_print_ticket_preview, null)
            val viewBinder = ItemPrintTicketPreviewBinding.bind(view)
            val receivingData =
                intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()
            viewBinder.ivPreview.setImageBitmap(
                getBitmap(
                    receivingData.palletType.toString().trim()
                )
            )
            viewBinder.tvLotValue.text = receivingData.palletType?.trim()
            viewBinder.tvMaterialValue.text = receivingData.erdat?.trim()
            viewBinder.tvGradeValue.text = receivingData.gradeDesc?.trim()
            viewBinder.tvCertificateValue.text = receivingData.certificate?.trim()
            viewBinder.tvWeightValue.text =
                receivingData.netWeight.trim().plus(" ").plus(receivingData.unitsOfMeasure)
                    .trim()
            viewBinder.tvClientValue.text = receivingData.transportVendorName?.trim()
            viewBinder.tvSacksValue.text = receivingData.bagCount?.trim()
            viewBinder.tvEudrStatusValue.text= eudrStatus
            if(receivingData.sourceLotId.isEmpty()){
               viewBinder.tvSrcLotLabel.gone()
               viewBinder.tvSrcLotValue.gone()
            }else{
                viewBinder.tvSrcLotLabel.visible()
                viewBinder.tvSrcLotValue.visible()
                viewBinder.tvSrcLotValue.text= receivingData.sourceLotId
            }
            tallyPrintKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(this, WifiMainActivity::class.java))
            }
        }.execute()
    }
    @SuppressLint("SuspiciousIndentation")
    private fun createTicketCardPileNicaragua(){
        tallyPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            val view = LayoutInflater.from(this).inflate(R.layout.item_print_ticket_preview_nicaragua, null)
            val viewBinder = ItemPrintTicketPreviewNicaraguaBinding.bind(view)

            var pileDetails= VegaNicaPileDetails()
            pileDetails= intent?.getParcelableExtra<VegaNicaPileDetails>(UIUtils.PILE_PRINT_DETAILS) ?: VegaNicaPileDetails()

            viewBinder.ivPreview.setImageBitmap(
                getBitmap(
                    pileDetails.pileNumber.toString().trim()
                )
            )
            if(pileDetails.ticketNumber?.isNotEmpty() == true) {
                viewBinder.tvLotValue.text = pileDetails.ticketNumber?.trim()
            }else viewBinder.tvLot.gone()

            if(pileDetails.receivingDate?.isEmpty() == true){
                viewBinder.tvMaterialValue.gone()
                viewBinder.tvMaterial.gone()
            }

            viewBinder.tvMaterialValue.text = pileDetails.receivingDate?.trim()
            if(pileDetails.gradeDetails?.isEmpty() == true){
                viewBinder.tvGradeValue.gone()
                viewBinder.tvGrade.gone()
            }
            if(pileDetails.certificate?.isEmpty()==true) {
                viewBinder.tvCertificateValue.gone()
                viewBinder.tvCertificate.gone()
            }

            viewBinder.tvGradeValue.text =
                pileDetails.gradeDetails?.trim().plus(" - ").plus(pileDetails.gradeDesc?.trim())

            viewBinder.tvCertificateValue.text = pileDetails.certificate?.trim()
            if(pileDetails.netWeight?.isEmpty() == true){
                viewBinder.tvWeight.gone()
                viewBinder.tvWeightValue.gone()
            }
            viewBinder.tvWeightValue.text = pileDetails.netWeight?.trim().plus(" ").plus(pileDetails.unitsOfMeasure).trim()
            viewBinder.tvSacksValue.text= pileDetails.bagCount

            if (pileDetails.materialName?.contains("toll", true) == true )
                viewBinder.tvClientValue.text = pileDetails.vendor.toString()
            else {
                viewBinder.tvClientValue.gone()
                viewBinder.tvClient.gone()
            }


            viewBinder.tvStLoc.visible()
            viewBinder.tvStLocValue.visible()
            viewBinder.tvStLocValue.text = pileDetails.storageLocation
            tallyPrintKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(this, WifiMainActivity::class.java).putExtra(IS_SET_DEFAULT_SIZE, true))
            }
        }.execute()


    }


    @SuppressLint("SuspiciousIndentation")
    private fun createTicketCardBitMapNicaragua() {
        tallyPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            val view = LayoutInflater.from(this).inflate(R.layout.item_print_ticket_preview_nicaragua, null)
            val viewBinder = ItemPrintTicketPreviewNicaraguaBinding.bind(view)

            val receivingData = intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()

                if(intent.hasExtra(UIUtils.PRINT_FGRN_REQ)){
                    fgrnReq= intent?.getParcelableExtra<VegaCocoaProcessingFgrnPost>(UIUtils.PRINT_FGRN_REQ) ?: VegaCocoaProcessingFgrnPost()
                    if(fgrnReq.qualityDetails?.filter { it.nameCharValue=="NICERTI" }?.isNotEmpty() == true){
                    receivingData.palletType= fgrnReq.qualityDetails?.filter { it.nameCharValue=="NICERTI" }?.get(0)?.qualityParameterValue.toString()}
                    receivingData.erdat= DateUtils.getCurrentDate()
                    receivingData.netWeight= fgrnReq.processingLotDtls.get(0).netWeight.toString()
                    receivingData.unitsOfMeasure= fgrnReq.processingLotDtls.get(0).unitsOfMeasure.toString()
                    receivingData.bagCount= fgrnReq.qualityDetails?.filter { it.nameCharValue=="NISACOS" }?.get(0)?.qualityParameterValue.toString()
                    receivingData.storageLocationCode= fgrnReq.processingLotDtls.get(0).storageLocationCode
                    receivingData.batchNumber= fgrnReq.qualityDetails?.get(0)?.batchNumber
                    receivingData.gradeDesc= fgrnReq.qualityDetails?.filter { it.nameCharValue=="NIPOSITI" }?.get(0)?.qualityParameterValue
                    receivingData.qualityGradeDesc= fgrnReq.qualityParamDesc
                    if(fgrnReq.qualityDetails?.filter { it.nameCharValue=="NIFG0014" }?.isNotEmpty() == true)
                      receivingData.certificate = fgrnReq.qualityDetails?.filter { it.nameCharValue=="NIFG0014" }?.get(0)?.qualityParameterValue
                }

            viewBinder.ivPreview.setImageBitmap(
                getBitmap(
                    receivingData.batchNumber.toString().trim()
                )
            )
            if(receivingData.palletType?.isNotEmpty() == true) {
                viewBinder.tvLotValue.text = receivingData.palletType?.trim()
            }else viewBinder.tvLot.gone()

            if(receivingData.erdat?.isEmpty() == true){
                viewBinder.tvMaterialValue.gone()
                viewBinder.tvMaterial.gone()
            }

            viewBinder.tvMaterialValue.text = receivingData.erdat?.trim()
            if(receivingData.gradeDesc?.isEmpty() == true){
                viewBinder.tvGradeValue.gone()
                viewBinder.tvGrade.gone()
            }
            if(receivingData.certificate?.isEmpty()==true) {
                viewBinder.tvCertificateValue.gone()
                viewBinder.tvCertificate.gone()
            }

            viewBinder.tvGradeValue.text =
                receivingData.gradeDesc?.trim().plus(" - ").plus(receivingData.qualityGradeDesc?.trim())

            viewBinder.tvCertificateValue.text = receivingData.certificate?.trim()
            viewBinder.tvWeightValue.text =
                receivingData.netWeight.trim().plus(" ").plus(receivingData.unitsOfMeasure)
                    .trim()
            if (receivingData.materialName?.contains("tolling", true) == true)
                viewBinder.tvClientValue.text = receivingData.tollingVendorName.toString()
            else {
                viewBinder.tvClientValue.gone()
                viewBinder.tvClient.gone()
            }
            viewBinder.tvSacksValue.text = receivingData.bagCount?.trim()
            viewBinder.tvStLoc.visible()
            viewBinder.tvStLocValue.visible()
            viewBinder.tvStLocValue.text = receivingData.storageLocationCode
            tallyPrintKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(this, WifiMainActivity::class.java).putExtra(IS_SET_DEFAULT_SIZE, true))
            }
        }.execute()
    }

    private fun generateFgrnTallySheetBitMap() {
        tallyPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            val view =
                LayoutInflater.from(this).inflate(R.layout.item_print_fgrn_tallysheet_preview, null)
            val viewBinder = ItemPrintFgrnTallysheetPreviewBinding.bind(view)
            val receivingData = intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA)?: VegaReceiving()
            val batch = if(receivingData.batchNumber?.isNotEmpty() == true) receivingData.batchNumber else fgrnBatch
            viewBinder.ivPreview.setImageBitmap(getBitmap(batch.toString()))
            viewBinder.tvLotValue.text = fgrnBatch
            tallyPrintKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(this, WifiMainActivity::class.java))
            }
        }.execute()
    }

    private fun printLocalSalesTallysheet() {
        tallyPrintKeys.clear()
        val deliveryid = intent?.getStringExtra(UIUtils.DELIVERYID)
        val batchid = intent?.getStringExtra(UIUtils.BATCHID)

        showCustomLoading()
        DoAsync {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.vega_nic_local_sales_tally_sheet_print, null)
            val viewBinder = VegaNicLocalSalesTallySheetPrintBinding.bind(view)
            val receivingData =
                intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()
            viewBinder.deliveryNoPrint.text = receivingData.weighBridgeId
            viewBinder.contractNoValue.text = receivingData.delivery
            viewBinder.dateValue.text = receivingData.createdDate
            viewBinder.netWtValue.text =
                receivingData.netWeight.trim().plus(" ").plus(receivingData.unitsOfMeasure).trim()
            viewBinder.noBagsValue.text = receivingData.bagCount
            viewBinder.sourceValue.text = receivingData.storageLocationCode
            viewBinder.clientValue.text = receivingData.customerNum
            viewBinder.typeofCafeValue.text = receivingData.materialName
            viewBinder.grossQuintalesValue.text = receivingData.grossWeight
            viewBinder.lotsValue.text = receivingData.batchNumber
            viewBinder.observationsValue.text = receivingData.remarks

            tallyPrintKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )

            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(this, WifiMainActivity::class.java))
            }
        }.execute()
    }

    private fun prepareCommonSecretSample(){
        binding.printSecretIDQRCode.visible()
        binding.printSecretIDQRCode.setOnClickListener {
            DoAsync {
                tallyPrintKeys.clear()
                tallyPrintKeys = generateSecretQRCode(intent, this)
                runOnUiThread {
                    showPreviewDialog()
                }
            }.execute()

        }

    }


    private fun prepareBitmapForCameroonGrnPrinting() {
        if (fromCameroonCocoaQA) {
            if (fromNigeriaCocoaQA) {
                binding.grnReceipt.visibility = View.GONE
                binding.printLotCard.visibility = View.VISIBLE
                binding.printLotCard.text = getString(R.string.print_batch_cards)
                binding.btnPrint.visibility = View.GONE
            } else {
                binding.grnReceipt.visibility = View.VISIBLE
                binding.printLotCard.visibility = View.VISIBLE
                binding.btnPrint.visibility = View.GONE
                binding.btnPrint.text= getString(R.string.entry_sheet_txt)

            }

            val receivingData =
                intent?.getParcelableExtra<VegaQualityApproveCameroonWeighBridge>(UIUtils.RECEIVING_DATA)
                    ?: VegaQualityApproveCameroonWeighBridge()

            val vegaBcApprovePostData =
                intent?.getParcelableExtra<VegaQualityApproveCameroonPostData>(UIUtils.QUALITY_DATA)
                    ?: VegaQualityApproveCameroonPostData()


            binding.printLotCard.setOnClickListener {
                createLotCardBitMap()
            }

            binding.btnPrint.setOnClickListener {
             showCustomLoading()
                DoAsync {
                    tallyPrintKeys.clear()
                   tallyPrintKeys= entrySheetCMCocoPrint(intent,this)

                    runOnUiThread {
                        showPreviewDialog()
                    }

                }.execute()
            }


            binding.grnReceipt.setOnClickListener {
                showCustomLoading()
                DoAsync {
                    tallyPrintKeys.clear()
                    tallyPrintKeys= qualityApprovalDDNCMPrintFormat(intent,this)
                    runOnUiThread {
                        showPreviewDialog()
                    }

                }.execute()
            }
        }
    }

    private fun prepareBitmapForPrinting() {
        if (fromNicarguaCoffee) {
            var type = intent?.getStringExtra(UIUtils.NICARAGUA_PRINT_TYPE)

            when (type) {
                UIUtils.NICARAGUA_PRINT_INVOICE_PTBF -> {

                    binding.btnPrint.visibility = View.GONE
                    binding.invoicePrinting.visibility = View.VISIBLE
                    binding.invoicePrinting.setOnClickListener {
                        showCustomLoading()
                        DoAsync {
                            generateInvoice(type)
                            runOnUiThread {
                                showPreviewDialog()
                            }
                        }.execute()
                    }
                    val priceInfo =
                        intent?.getParcelableExtra<VegaNicaraguaInvoicePriceInfo>(UIUtils.INVOICE_PRICE_INFO)
                            ?: VegaNicaraguaInvoicePriceInfo()

                    if (covertToDouble(priceInfo.certificatePremium) > 0) {
                        binding.btnCertificationPrint.visibility = View.VISIBLE


                        binding.btnCertificationPrint.setOnClickListener {
                            showCustomLoading()
                            DoAsync {
                                generateCertificationPremium(type)
                                runOnUiThread {
                                    showPreviewDialog()
                                }
                            }.execute()
                        }
                    }


                    if (covertToDouble(priceInfo.withholdingTax) > 0) {
                        binding.btnWithholdTaxPrint.visibility = View.VISIBLE

                        binding.btnWithholdTaxPrint.setOnClickListener {
                            showCustomLoading()
                            DoAsync {
                                generateWithHoldTax(type)
                                runOnUiThread {
                                    showPreviewDialog()
                                }
                            }.execute()
                        }
                    }
                }
                UIUtils.NICARAGUA_PRINT_FORWARDPO_RECEIPT -> {

                    binding.btnPrint.visibility = View.GONE
                    binding.btnWithpoPrint.visibility = View.VISIBLE
                    binding.invoicePrinting.visibility = View.GONE
                    binding.invoicePrinting.setOnClickListener {
                        showCustomLoading()
                        DoAsync {
                            generateInvoice(type)
                            runOnUiThread {
                                showPreviewDialog()
                            }
                        }.execute()
                    }
                    val priceInfo =
                        intent?.getParcelableExtra<VegaNicaraguaInvoicePriceInfo>(UIUtils.INVOICE_PRICE_INFO)
                            ?: VegaNicaraguaInvoicePriceInfo()

                    if (covertToDouble(priceInfo.certificatePremium) > 0) {
                        binding.btnCertificationPrint.visibility = View.VISIBLE


                        binding.btnCertificationPrint.setOnClickListener {
                            showCustomLoading()
                            DoAsync {
                                generateCertificationPremium(type)
                                runOnUiThread {
                                    showPreviewDialog()
                                }
                            }.execute()
                        }
                    }


                    if (covertToDouble(priceInfo.withholdingTax) > 0) {
                        binding.btnWithholdTaxPrint.visibility = View.VISIBLE

                        binding.btnWithholdTaxPrint.setOnClickListener {
                            showCustomLoading()
                            DoAsync {
                                generateWithHoldTax(type)
                                runOnUiThread {
                                    showPreviewDialog()
                                }
                            }.execute()
                        }
                    }
                }

                UIUtils.NICARAGUA_PRINT_GRN_RECEIPT -> {
                    if (fromCoffeeGrn) {
                        binding.grnReceipt.text = getString(R.string.tally_sheet)
                        binding.grnReceipt.visibility = View.VISIBLE
                        binding.btnPrint.visibility = View.GONE
                    } else {
                        binding.grnReceipt.visibility = View.VISIBLE
                        binding.printLotCard.visibility = View.VISIBLE
                        binding.btnPrint.visibility = View.GONE
                    }
                    val receivingData =
                        intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA)
                            ?: VegaReceiving()
                    val priceInfo =
                        intent?.getParcelableExtra<VegaNicaraguaInvoicePriceInfo>(UIUtils.INVOICE_PRICE_INFO)
                            ?: VegaNicaraguaInvoicePriceInfo()

                    if (receivingData.grnType.equals("spot") || receivingData.grnType.equals(
                            getString(R.string.fixed)
                        )
                    ) {
                        binding.invoicePrinting.visibility = View.VISIBLE
                        if (covertToDouble(priceInfo.certificatePremium) > 0) {
                            binding.btnCertificationPrint.visibility = View.VISIBLE

                            binding.btnCertificationPrint.setOnClickListener {
                                showCustomLoading()
                                DoAsync {
                                    generateCertificationPremium(type)
                                    runOnUiThread {
                                        showPreviewDialog()
                                    }
                                }.execute()
                            }
                        }
                        if (covertToDouble(priceInfo.withholdingTax) > 0) {
                            binding.btnWithholdTaxPrint.visibility = View.VISIBLE

                            binding.btnWithholdTaxPrint.setOnClickListener {
                                showCustomLoading()
                                DoAsync {
                                    generateWithHoldTax(type)
                                    runOnUiThread {
                                        showPreviewDialog()
                                    }
                                }.execute()
                            }
                        }

                    }
                    val bagsData =
                        intent?.getParcelableArrayListExtra<VegaNicaraguaWeighmentBagMaterial>(
                            UIUtils.BAGS_DATA
                        )

                    if(intent?.hasExtra(UIUtils.LOT_DETAIL) == true) {
                        splitLotItems =
                            intent?.getParcelableArrayListExtra<VegaCoffeeLot>(UIUtils.LOT_DETAIL)
                                ?: ArrayList()
                        receivingData_splitlot =
                            intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()
                        val merged =  intent?.getBooleanExtra(UIUtils.MERGED, false) ?: false
                        if (merged) {
                            binding.btSplitLot.visible()
                            isSplitdlot = true
                        }
                        else{
                            isSplitdlot = false
                            binding.btSplitLot.gone()
                        }
                        binding.btSplitLot.setOnClickListener {
                            VegaCommonSplitLotNavigation.dynamicStart?.let {
                                it.putExtra(UIUtils.LOT_DETAIL, splitLotItems)
                                it.putExtra(UIUtils.RECEIVING_DATA, receivingData)
                                startActivity(it)
                            }
                        }
                    }

                    binding.invoicePrinting.setOnClickListener {
                        showCustomLoading()
                        DoAsync {
                            generateInvoice(type)
                            runOnUiThread {
                                showPreviewDialog()
                            }
                        }.execute()
                    }
                    binding.printLotCard.setOnClickListener {
                        createLotCardBitMap()
                    }
                    binding.grnReceipt.setOnClickListener {

                        if (fromCoffeeGrn) {
                            showCustomLoading()
                            DoAsync {
                                val view =
                                    LayoutInflater.from(this)
                                        .inflate(
                                            R.layout.vega_nicaragua_mtnr_tallysheet_print,
                                            null
                                        )
                                val viewBinder = VegaNicaraguaMtnrTallysheetPrintBinding.bind(view)
                                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoRp)
                                viewBinder.tvmtnrDocSequence.text =
                                    receivingData.mtnrDocSequence.toString()
                                viewBinder.tvmtnrDocSequenceRp.text =
                                    receivingData.mtnrDocSequence.toString()
                                viewBinder.origenValue.text =
                                    receivingData.origin.plus(" - ")
                                        .plus(receivingData.supplierName)
                                viewBinder.origenValueRp.text =
                                    receivingData.origin.plus(" - ")
                                        .plus(receivingData.supplierName)

                                viewBinder.tvmtnrDeliverynumber.text = receivingData.delivery
                                viewBinder.tvmtnrDeliverynumberRp.text = receivingData.delivery

                                if (receivingData.materialName!!.contains("Tolling", true)) {
                                    viewBinder.vendorValue.text = receivingData.tollingVendorName
                                    viewBinder.vendorValueRp.text = receivingData.tollingVendorName
                                }

                                viewBinder.origenValue.text = receivingData.origin.plus(" - ")
                                    .plus(receivingData.supplierName)
                                viewBinder.origenValueRp.text = receivingData.origin.plus(" - ")
                                    .plus(receivingData.supplierName)

                                viewBinder.dateandHourValue.text = DateUtils.getDate(
                                    Calendar.getInstance().timeInMillis,
                                    "dd-MMM-yyyy hh:mm:ss"
                                )
                                viewBinder.dateandHourValueRp.text = DateUtils.getDate(
                                    Calendar.getInstance().timeInMillis,
                                    "dd-MMM-yyyy hh:mm:ss"
                                )

                                viewBinder.tvRemarksCopy.text = receivingData.remarks
                                viewBinder.tvRemarksCopyRp.text = receivingData.remarks

                                viewBinder.tvSLossCopy.text = receivingData.tareWeight1
                                viewBinder.tvSLossCopyRp.text = receivingData.tareWeight1


                                viewBinder.truckNoValue.text =
                                    receivingData.transportVendorCode.plus(" - ")
                                        .plus(receivingData.transportVendorName)
                                viewBinder.truckNoValueRp.text =
                                    receivingData.transportVendorCode.plus(" - ")
                                        .plus(receivingData.transportVendorName)

                                viewBinder.recibidoValue.text =
                                    receivingData.plantId.plus(" - ").plus(receivingData.plantName)
                                viewBinder.recibidoValueRp.text =
                                    receivingData.plantId.plus(" - ").plus(receivingData.plantName)

                                viewBinder.conductorValue.text = receivingData.driverName
                                viewBinder.conductorValueRp.text = receivingData.driverName

                                viewBinder.locationValue.text = receivingData.vehicleNumber
                                viewBinder.locationValueRp.text = receivingData.vehicleNumber

                                if (receivingData.certificate!!.length > 0) {
                                    viewBinder.certificationValue.text = receivingData.certificate
                                    viewBinder.certificationValueRp.text = receivingData.certificate
                                }
                                viewBinder.tvBagValue.append(receivingData.bagCount)
                                viewBinder.tvBagValueRp.append(receivingData.bagCount)


                                viewBinder.tvTareWeightValue.text = receivingData.tareWeight
                                viewBinder.tvTareWeightValueRp.text = receivingData.tareWeight
                                /* if (receivingData.materialName?.contains("tolling", true) == true)
                                     viewBinder.tvTareWeightValue.text = receivingData.tareWeight
                                 else
                                     viewBinder.tvTareWeightValue.append(
                                         (if (receivingData.bagCount?.isNotEmpty() == true) receivingData.bagCount?.toInt()
                                             ?: 0 else 0).times(
                                             if (receivingData.tareWeight?.isNotEmpty() == true) receivingData.tareWeight?.toDouble()
                                                 ?: 0.0 else 0.0
                                         ).formatTwoDigits()
                                     )*/
                                viewBinder.tvGrossWeightValue.append(
                                    formatTwoDigString(
                                        receivingData.grossWeight.toString()
                                    )
                                )
                                viewBinder.tvGrossWeightValueRp.append(
                                    formatTwoDigString(
                                        receivingData.grossWeight.toString()
                                    )
                                )

                                viewBinder.tvNetWeightValue.append(formatTwoDigString(receivingData.netWeight))
                                viewBinder.tvNetWeightValueRp.append(
                                    formatTwoDigString(
                                        receivingData.netWeight
                                    )
                                )

                                /* if (receivingData.netWeight.isNotEmpty()) {
                                     var quintel = receivingData.netWeight.toDouble() / 46
                                     viewBinder.tvGrossQQsValue.append(quintel.formatThreeDigits())
                                     viewBinder.tvGrossQQsValueRp.append(quintel.formatThreeDigits())
                                 }
 */
                                viewBinder.tvGrossQQsValue.append(receivingData.batchNumber)
                                viewBinder.tvGrossQQsValueRp.append(receivingData.batchNumber)

                                viewBinder.tvTicketValue.append(receivingData.palletType)
                                viewBinder.tvTicketValueRp.append(receivingData.palletType)
                                if (receivingData.qualityGradeDesc?.isNotEmpty() == true) {
                                    viewBinder.tvQualityGradeValue.append(receivingData.qualityGradeDesc)
                                    viewBinder.tvQualityGradeValueRp.append(receivingData.qualityGradeDesc)
                                } else {
                                    viewBinder.tvQualityGradeValue.append(receivingData.gradeDesc)
                                    viewBinder.tvQualityGradeValueRp.append(receivingData.gradeDesc)
                                }

                                viewBinder.tvMaterialname.append(receivingData.materialName)
                                viewBinder.tvMaterialnameRp.append(receivingData.materialName)


                                when (receivingData.certificate) {
                                    UIUtils.NICERTFD_SBUX -> {
                                        viewBinder.mtnrCertificationLogo.setImageDrawable(
                                            resources.getDrawable(
                                                com.olam.warehouse.presentation.R.drawable.starbucks_logo
                                            )
                                        )
                                        viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                                        viewBinder.mtnrCertificationLogoRp.setImageDrawable(
                                            resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo)
                                        )
                                        viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                                    }
                                    UIUtils.NICERTFD_UTZ -> {
                                        viewBinder.mtnrCertificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                                        viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                                        viewBinder.mtnrCertificationLogoRp.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                                        viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                                    }
                                    UIUtils.NICERTFD_RFA -> {
                                        viewBinder.mtnrCertificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                                        viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                                        viewBinder.mtnrCertificationLogoRp.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                                        viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                                    }
                                    else -> {
                                        viewBinder.mtnrCertificationLogo.visibility = View.GONE
                                        viewBinder.mtnrCertificationLogoRp.visibility = View.GONE
                                    }
                                }

                                tallyPrintKeys.clear()
                                tallyPrintKeys.add(
                                    bitmapToString(
                                        getBitmapFromView(
                                            view, Color.WHITE
                                        )
                                    )
                                )
                                runOnUiThread {
                                    showPreviewDialog()
                                }
                            }.execute()
                        } else {
                            showCustomLoading()
                            DoAsync {
                                val view =
                                    LayoutInflater.from(this)
                                        .inflate(R.layout.vega_nicaragua_grn_receipt_printing, null)
                                val viewBinder = VegaNicaraguaGrnReceiptPrintingBinding.bind(view)
                                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoCopy)
                                UIUtils.setOlamLogoDynamically(viewBinder.certificationLogoCopy)
                                viewBinder.sapVendorName.text = receivingData.supplierName
                                viewBinder.taxId.text = receivingData.taxId
                                viewBinder.taxIdCopy.text = receivingData.taxId
                                viewBinder.dateAndHour.text =
                                    DateUtils.getDate(
                                        Calendar.getInstance().timeInMillis,
                                        "dd-MMM-yyyy hh:mm:ss"
                                    )
                                viewBinder.buyingUnit.text = receivingData.plantName

                                if (receivingData.grnNumber?.isNotEmpty() == true) {
                                    viewBinder.grnNumber.text = receivingData.grnNumber
                                    viewBinder.grnNumberCopy.text = receivingData.grnNumber
                                } else {
                                    viewBinder.grnNumber.text = ""
                                    viewBinder.grnNumberCopy.text = ""
                                }

                                viewBinder.receiveNumber.text = receivingData.palletType
                                viewBinder.receiveNumberCopy.text = receivingData.palletType

                                when (receivingData.grnType) {
                                    getString(R.string.ptbf) -> {
                                        viewBinder.note.visibility = View.VISIBLE
                                        viewBinder.notecopy.visibility = View.VISIBLE
                                        viewBinder.note.text =
                                            Html.fromHtml(
                                                "<b>" + getString(R.string.nota) + "</b>".plus(
                                                    getString(R.string.grn_receipt_nota)
                                                )
                                            )
                                                .toString()
                                        viewBinder.notecopy.text =
                                            Html.fromHtml(
                                                "<b>" + getString(R.string.nota) + "</b>".plus(
                                                    getString(R.string.grn_receipt_nota)
                                                )
                                            )
                                                .toString()

                                        viewBinder.typeOfPurchaseCopy.text =
                                            getString(R.string.s_ptbf)
                                        viewBinder.typeOfPurchase.text = getString(R.string.s_ptbf)
                                    }
                                    getString(R.string.spot) -> {
                                        viewBinder.note.visibility = View.GONE
                                        viewBinder.notecopy.visibility = View.GONE
                                        viewBinder.typeOfPurchaseCopy.text =
                                            getString(R.string.s_spot)
                                        viewBinder.typeOfPurchase.text = getString(R.string.s_spot)
                                    }

                                    getString(R.string.spot_tolling) -> {
                                        viewBinder.note.visibility = View.GONE
                                        viewBinder.notecopy.visibility = View.GONE
                                        viewBinder.typeOfPurchaseCopy.text =
                                            getString(R.string.s_tolling)
                                        viewBinder.typeOfPurchase.text =
                                            getString(R.string.s_tolling)
                                    }

                                    getString(R.string.fixed) -> {
                                        viewBinder.note.visibility = View.GONE
                                        viewBinder.notecopy.visibility = View.GONE
                                        viewBinder.typeOfPurchaseCopy.text =
                                            getString(R.string.s_fixed)
                                        viewBinder.typeOfPurchase.text = getString(R.string.s_fixed)
                                    }
                                }

                                if (receivingData.certificate!!.length > 0) {
                                    viewBinder.certification.text = receivingData.certificate
                                    viewBinder.certificationCopy.text = receivingData.certificate
                                }


                                viewBinder.sapVendorNameCopy.text = receivingData.supplierName
                                viewBinder.dateAndHourCopy.text =
                                    DateUtils.getDate(
                                        Calendar.getInstance().timeInMillis,
                                        "dd-MMM-yyyy hh:mm:ss"
                                    )
                                viewBinder.buyingUnitCopy.text = receivingData.plantName

                                when (receivingData.certificate) {
                                    UIUtils.NICERTFD_SBUX -> {
                                        viewBinder.certificationLogo.setImageDrawable(
                                            resources.getDrawable(
                                                com.olam.warehouse.presentation.R.drawable.starbucks_logo
                                            )
                                        )
                                        viewBinder.certificationLogo.visibility = View.VISIBLE
                                        viewBinder.certificationLogoCopy.setImageDrawable(
                                            resources.getDrawable(
                                                com.olam.warehouse.presentation.R.drawable.starbucks_logo
                                            )
                                        )
                                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                                    }
                                    UIUtils.NICERTFD_UTZ -> {
                                        viewBinder.certificationLogo.setImageDrawable(
                                            resources.getDrawable(
                                                com.olam.warehouse.presentation.R.drawable.utz_logo
                                            )
                                        )
                                        viewBinder.certificationLogo.visibility = View.VISIBLE
                                        viewBinder.certificationLogoCopy.setImageDrawable(
                                            resources.getDrawable(
                                                com.olam.warehouse.presentation.R.drawable.utz_logo
                                            )
                                        )
                                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                                    }
                                    UIUtils.NICERTFD_RFA -> {
                                        viewBinder.certificationLogo.setImageDrawable(
                                            resources.getDrawable(
                                                com.olam.warehouse.presentation.R.drawable.rfa_logo
                                            )
                                        )
                                        viewBinder.certificationLogo.visibility = View.VISIBLE
                                        viewBinder.certificationLogoCopy.setImageDrawable(
                                            resources.getDrawable(
                                                com.olam.warehouse.presentation.R.drawable.rfa_logo
                                            )
                                        )
                                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                                    }
                                    else -> {
                                        viewBinder.certificationLogo.visibility = View.GONE
                                        viewBinder.certificationLogoCopy.visibility = View.GONE
                                    }
                                }


                                viewBinder.tvMaterialCopy.append(receivingData.materialName)
                                viewBinder.tvBagsQuantityCopy.append(receivingData.bagCount)
                                if (fromCoffeeGrn) {
                                    viewBinder.tvTareWeightCopy.text = receivingData.tareWeight
                                } else {
                                    if (receivingData.materialName?.contains(
                                            "tolling",
                                            true
                                        ) == true
                                    )
                                        viewBinder.tvTareWeightCopy.text = receivingData.tareWeight
                                    else
                                        viewBinder.tvTareWeightCopy.append(
                                            (if (receivingData.bagCount?.isNotEmpty() == true) receivingData.bagCount?.toInt()
                                                ?: 0 else 0).times(
                                                if (receivingData.tareWeight?.isNotEmpty() == true) receivingData.tareWeight?.toDouble()
                                                    ?: 0.0 else 0.0
                                            ).formatTwoDigits()
                                        )
                                }
                                viewBinder.tvGrossWeightCopy.append(formatTwoDigString(receivingData.grossWeight.toString()))
                                viewBinder.tvNetWeightCopy.append(formatTwoDigString(receivingData.netWeight))
                                viewBinder.tvUOMCopy.append(receivingData.unitsOfMeasure)
                                viewBinder.tvBatchNumberCopy.append(receivingData.batchNumber)
                                viewBinder.tvQualityGradeCopy.append(receivingData.gradeDesc)

                                viewBinder.tvMaterial.append(receivingData.materialName)
                                viewBinder.tvBagsQuantity.append(receivingData.bagCount)
                                if (receivingData.materialName?.contains("tolling", true) == true)
                                    viewBinder.tvTareWeight.text = receivingData.tareWeight
                                else
                                    viewBinder.tvTareWeight.append(
                                        (if (receivingData.bagCount?.isNotEmpty() == true) receivingData.bagCount?.toInt()
                                            ?: 0 else 0).times(
                                            if (receivingData.tareWeight?.isNotEmpty() == true) receivingData.tareWeight?.toDouble()
                                                ?: 0.0 else 0.0
                                        ).formatTwoDigits()
                                    )
                                viewBinder.tvGrossWeight.append(formatTwoDigString(receivingData.grossWeight.toString()))
                                viewBinder.tvNetWeight.append(formatTwoDigString(receivingData.netWeight))
                                viewBinder.tvUOM.append(receivingData.unitsOfMeasure)
                                viewBinder.tvBatchNumber.append(receivingData.batchNumber)
                                viewBinder.tvQualityGrade.append(receivingData.gradeDesc)

                                tallyPrintKeys.clear()
                                tallyPrintKeys.add(
                                    bitmapToString(
                                        getBitmapFromView(
                                            view, Color.WHITE
                                        )
                                    )
                                )
                                runOnUiThread {
                                    showPreviewDialog()
                                }
                            }.execute()
                        }
                    }
                }
                UIUtils.NICARAGUA_PRINT_MTNT_RECEIPT -> {
                    binding.btnMtntPrint.visible()
                    val req = intent?.getBooleanExtra(UIUtils.PRINT_TALLY_SHEET, false)
                    if (req == true){
                        binding.btnMtntPrint.text = getString(R.string.tally_sheet)
                    }
                    val mtnt = intent?.getParcelableExtra<VegaNicaraguaMtnt>(UIUtils.MTNT_DATA)
                        ?: VegaNicaraguaMtnt()
                    val lot =
                        intent?.getParcelableArrayListExtra<VegaNicDispatchLots>(UIUtils.MTNT_OUTPUT_DATA)
                            ?: ArrayList()
                    val bags =
                        intent?.getParcelableArrayListExtra<VegaNicaraguaWeighmentBagMaterial>(
                            UIUtils.BAGS_DATA
                        )
                            ?: ArrayList()

                    binding.btnMtntPrint.setOnClickListener {
                        DoAsync {
                            var view = LayoutInflater.from(this)
                                .inflate(R.layout.vega_nicaragua_mtnt_receipt_printing, null)
                            var viewBinder = VegaNicaraguaMtntReceiptPrintingBinding.bind(view)
                            UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                            UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoCopy)
                            if(nicaraguaMtntObdNumber.isNotEmpty()) {
                                viewBinder.tvOBDNoValue.text = nicaraguaMtntObdNumber
                                viewBinder.tvOBDNoValueCopy.text = nicaraguaMtntObdNumber


                                //val obdNumber = "8490021690"
                                val obdNumber = nicaraguaMtntObdNumber
                                val writer = MultiFormatWriter()
                                val matrix: BitMatrix =
                                    writer.encode(obdNumber, BarcodeFormat.QR_CODE, 350, 350)
                                val encoder = BarcodeEncoder()
                                val bitmap: Bitmap = encoder.createBitmap(matrix)
                                viewBinder.ivOBDNoQRCode.setImageBitmap(bitmap)
                                viewBinder.ivOBDNoQRCodeCopy.setImageBitmap(bitmap)
                            }

                            var storageLoss = 0.0
                            var weight: String = "0"
                            var editedweight: String = "0"
                            if (lot.size > 1) {
                                lot.filter { it.isMergedLot == false }.forEach { it1 ->
                                    weight =
                                        (java.lang.Double.valueOf(weight) + java.lang.Double.valueOf(
                                            it1.weight.toString()
                                        )).toString()
                                    if (mtnt.mergedNetWeight.isNullOrEmpty())
                                        editedweight =
                                            (java.lang.Double.valueOf(editedweight) + java.lang.Double.valueOf(
                                                it1.editedWeight.toString()
                                            )).toString()



                                    if (it1.isEndLot == true ||lot.any { it.isMergedLot == true }) {
                                        if (mtnt.mergedNetWeight.isNullOrEmpty())
                                            storageLoss = storageLoss + covertToDouble(
                                                calculateStorageLoss(
                                                    it1.weight.toString(),
                                                    it1.editedWeight.toString()
                                                )
                                            )
                                        else
                                            storageLoss =
                                                covertToDouble(
                                                    calculateStorageLoss(
                                                        weight,
                                                        mtnt.mergedNetWeight.toString()
                                                    )
                                                )
                                    }
                                }

                            } else {
                                lot.forEach { item1 ->
                                    if (item1.isEndLot == true) {
                                        val loss =
                                            calculateStorageLoss(
                                                item1.weight.toString(),
                                                item1.editedWeight.toString()
                                            )
                                        if (loss.isNotEmpty()) storageLoss += loss.toDouble()
                                    }
                                }
                            }

                            viewBinder.tvmtntDocSequence.text = mtnt.mtntDocSequence
                            viewBinder.tvmtntDocSequenceCopy.text = mtnt.mtntDocSequence
                            viewBinder.tvSendingPlant.text =
                                mtnt.plantId.plus(" - ").plus(getPlantDetails().plantName)
                            viewBinder.tvDestPlant.text = mtnt.sendingPlant
                            viewBinder.tvTranVendorName.text = mtnt.transportVendor
                            viewBinder.tvTransVendorCode.text = mtnt.transportVendorID
                            viewBinder.tvTruckNo.text = mtnt.vehicleNumber
                            viewBinder.tvDriverName.text = mtnt.driverName
                            viewBinder.tvCertification.text = mtnt.certification
                            viewBinder.tvRemarks.text = mtnt.remarks
                            viewBinder.tvSLoss.text =
                                storageLoss.formatTwoDigits().toString().plus(" ").plus(getString(R.string.kg))
                            /*  viewBinder.tvNoOfLots.text =
                                  lot.filter {
                                      it.isMergedLot == false
                                      ismergedlot == it.isMergedLot
                                  }.map { it.batchNumber }
                                      .toString().replace("[", "").replace("]", "")
  */

                            viewBinder.tvNoOfLots.text = mtnt.batchNumber
                            viewBinder.tvNoOfLotsCopy.text = mtnt.batchNumber


                            if (lot.size > 1 && !fromNicarguaUpcountry) {
                                viewBinder.tvlotheader.text = getString(R.string.merge_lot)
                                viewBinder.tvlotheaderCopy.text = getString(R.string.merge_lot)
                            }
                            if (mtnt.erdat?.isNotEmpty() == true)
                            /* viewBinder.tvDate.text = mtnt.erdat.let { it1 ->
                                 it1?.let { it2 ->
                                     DateUtils.getUTCDateTimeNicaragua(
                                         it2,
                                         this
                                     )
                                 }
                             }*/
                                viewBinder.tvDate.text =
                                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                            viewBinder.tvMaterial.text = mtnt.materialName
                            if (mtnt.materialName!!.contains("Tolling", ignoreCase = true)) {
                                viewBinder.tvvendorName.text = mtnt.vendorName
                                viewBinder.tvvendorNameCopy.text = mtnt.vendorName
                            }
                            viewBinder.tvQualityGrade.text = mtnt.qulityGradeDesc
                            viewBinder.tvUOM.text = mtnt.unitsOfMeasure
                            viewBinder.tvBagsQuantity.text =
                                bags.sumOf { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }
                                    .toString()
                            viewBinder.tvGrossWeight.text =
                                bags.sumOf { if (it.grossWeight.isNotEmpty() == true) it.grossWeight.toDouble() else 0.0 }
                                    .formatTwoDigits()
                            viewBinder.tvNetWeight.text =
                                bags.sumOf { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                                    .formatTwoDigits()
                            viewBinder.tvTareWeight.text = bags.sumOf {
                                (if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() else 0.0)?.times(
                                    if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0
                                )
                                    ?: 0.0
                            }.formatTwoDigits()

                            //Copy Print
                            viewBinder.tvSendingPlantCopy.text =
                                mtnt.plantId.plus(" - ").plus(getPlantDetails().plantName)
                            viewBinder.tvDestPlantCopy.text = mtnt.sendingPlant
                            viewBinder.tvTranVendorNameCopy.text = mtnt.transportVendor
                            viewBinder.tvTransVendorCodeCopy.text = mtnt.transportVendorID
                            viewBinder.tvTruckNoCopy.text = mtnt.vehicleNumber
                            viewBinder.tvDriverNameCopy.text = mtnt.driverName
                            viewBinder.tvCertificationCopy.text = mtnt.certification
                            viewBinder.tvRemarksCopy.text = mtnt.remarks
                            viewBinder.tvSLossCopy.text =
                                storageLoss.formatTwoDigits().toString().plus(" ").plus(getString(R.string.kg))
                            /*  viewBinder.tvNoOfLotsCopy.text =
                                  lot.filter { it.isMergedLot == false }.map { it.batchNumber }
                                      .toString().replace("[", "").replace("]", "")
                            */
                            viewBinder.tvDateCopy.text =
                                DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                            /* mtnt.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTimeNicaragua(
                                it2,
                                this
                            )
                        }
                    }*/
                            when (mtnt.certification) {
                                UIUtils.NICERTFD_SBUX -> {
                                    viewBinder.mtntCertificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                                    viewBinder.mtntCertificationLogo.visibility = View.VISIBLE
                                    viewBinder.mtntCertificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                                    viewBinder.mtntCertificationLogoCopy.visibility = View.VISIBLE
                                }
                                UIUtils.NICERTFD_UTZ -> {
                                    viewBinder.mtntCertificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                                    viewBinder.mtntCertificationLogo.visibility = View.VISIBLE
                                    viewBinder.mtntCertificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                                    viewBinder.mtntCertificationLogoCopy.visibility = View.VISIBLE
                                }
                                UIUtils.NICERTFD_RFA -> {
                                    viewBinder.mtntCertificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                                    viewBinder.mtntCertificationLogo.visibility = View.VISIBLE
                                    viewBinder.mtntCertificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                                    viewBinder.mtntCertificationLogoCopy.visibility = View.VISIBLE
                                }
                                else -> {
                                    viewBinder.mtntCertificationLogo.visibility = View.GONE
                                    viewBinder.mtntCertificationLogoCopy.visibility = View.GONE
                                }
                            }
                            viewBinder.tvMaterialCopy.text = mtnt.materialName
                            viewBinder.tvQualityGradeCopy.text = mtnt.qulityGradeDesc
                            viewBinder.tvUOMCopy.text = mtnt.unitsOfMeasure
                            viewBinder.tvBagsQuantityCopy.text =
                                bags.sumOf { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }
                                    .toString()
                            viewBinder.tvGrossWeightCopy.text =
                                bags.sumOf { if (it.grossWeight.isNotEmpty() == true) it.grossWeight.toDouble() else 0.0 }
                                    .formatTwoDigits()
                            viewBinder.tvNetWeightCopy.text =
                                bags.sumOf { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                                    .formatTwoDigits()
                            viewBinder.tvTareWeightCopy.text = bags.sumOf {
                                (if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() else 0.0)?.times(
                                    if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0
                                )
                                    ?: 0.0
                            }.formatTwoDigits()

                            tallyPrintKeys.clear()
                            tallyPrintKeys.add(
                                bitmapToString(
                                    getBitmapFromView(
                                        view, Color.WHITE
                                    )
                                )
                            )
                            runOnUiThread {
                                showPreviewDialog()
                            }

                        }.execute()
                    }
                }
            }
        }
    }

    private fun showLotPrintExitDialog() {
        MaterialDialog(this).show {
            message(R.string.print_lot_message)
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { moveToHomePage() },
                { dismiss() })
        }
    }
    private fun showSplitLotExitDialog() {
        MaterialDialog(this).show {
            message(R.string.split_lot_message)
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { moveToHomePage() },
                { dismiss() })
        }
    }

    private fun generateCertificationPremium(type: String) {
        val priceInfo =
            intent?.getParcelableExtra<VegaNicaraguaInvoicePriceInfo>(UIUtils.INVOICE_PRICE_INFO)
                ?: VegaNicaraguaInvoicePriceInfo()
        val view = LayoutInflater.from(this)
            .inflate(R.layout.vega_nicaragua_certificate_premium_receipt_printing, null)
        val viewBinder = VegaNicaraguaCertificatePremiumReceiptPrintingBinding.bind(view)
        UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
        UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoCopy)

        when (type) {
            UIUtils.NICARAGUA_PRINT_INVOICE_PTBF -> {
                val vendorData =
                    intent?.getParcelableExtra<VegaVendor>(UIUtils.VENDOR_DATA) ?: VegaVendor()
                val grnData =
                    intent?.getParcelableExtra<GrnDetails>(UIUtils.GRN_DATA) ?: GrnDetails()
                val inventoryInfo =
                    intent?.getParcelableExtra<VegaNicaraguaInvoiceGrnInventoryModal>(UIUtils.BAGS_DATA)
                        ?: VegaNicaraguaInvoiceGrnInventoryModal()

                viewBinder.buyingUnit.text = inventoryInfo.plantName
                viewBinder.buyingUnitCopy.text = inventoryInfo.plantName
                viewBinder.sapVendorName.text = vendorData.vendorName
                viewBinder.sapVendorNameCopy.text = vendorData.vendorName
                viewBinder.dateAndHour.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.dateAndHourCopy.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.identificationNum.text = vendorData.taxNumber
                viewBinder.identificationNumCopy.text = vendorData.taxNumber
                viewBinder.certification.text = inventoryInfo.certification
                viewBinder.certificationCopy.text = inventoryInfo.certification

                viewBinder.typeOfPurchase.text = getString(R.string.s_ptbf)
                viewBinder.typeOfPurchaseCopy.text = getString(R.string.s_ptbf)

                viewBinder.qualityGrade.text = priceInfo.qualityGradeDesc
                viewBinder.qualityGradeCopy.text = priceInfo.qualityGradeDesc
                viewBinder.paidCertification.text = priceInfo.certificatePremium
                viewBinder.paidCertificationCopy.text = priceInfo.certificatePremium
                viewBinder.netWeightPerKG.text = grnData.grnQty
                viewBinder.netWeightPerKGCopy.text = grnData.grnQty

                when (inventoryInfo.certification) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                        viewBinder.certificationLogoCopy.visibility = View.GONE
                    }
                }
                viewBinder.batchList.text = grnData.batchNumber
                viewBinder.batchListCopy.text = grnData.batchNumber
            }
            UIUtils.NICARAGUA_PRINT_GRN_RECEIPT -> {
                val receivingData =
                    intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA)
                        ?: VegaReceiving()
                viewBinder.buyingUnit.text = receivingData.plantName
                viewBinder.buyingUnitCopy.text = receivingData.plantName
                viewBinder.sapVendorName.text = receivingData.supplierName
                viewBinder.sapVendorNameCopy.text = receivingData.supplierName
                viewBinder.dateAndHour.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.dateAndHourCopy.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.identificationNum.text = receivingData.taxId
                viewBinder.identificationNumCopy.text = receivingData.taxId
                viewBinder.certification.text = receivingData.certificate
                viewBinder.certificationCopy.text = receivingData.certificate

                viewBinder.typeOfPurchase.text = getString(R.string.s_ptbf)
                viewBinder.typeOfPurchaseCopy.text = getString(R.string.s_ptbf)

                viewBinder.qualityGrade.text = priceInfo.qualityGradeDesc
                viewBinder.qualityGradeCopy.text = priceInfo.qualityGradeDesc
                viewBinder.paidCertification.text = priceInfo.certificatePremium
                viewBinder.paidCertificationCopy.text = priceInfo.certificatePremium
                viewBinder.netWeightPerKG.text = formatTwoDigString(receivingData.netWeight)
                viewBinder.netWeightPerKGCopy.text = formatTwoDigString(receivingData.netWeight)


                when (receivingData.grnType) {

                    getString(R.string.spot) -> {

                        viewBinder.typeOfPurchase.text = getString(R.string.s_spot)
                        viewBinder.typeOfPurchaseCopy.text = getString(R.string.s_spot)
                    }

                    getString(R.string.spot_tolling) -> {

                        viewBinder.typeOfPurchase.text = getString(R.string.s_tolling)
                        viewBinder.typeOfPurchaseCopy.text = getString(R.string.s_tolling)
                    }

                    getString(R.string.fixed) -> {
                        viewBinder.typeOfPurchase.text = getString(R.string.s_fixed)
                        viewBinder.typeOfPurchaseCopy.text = getString(R.string.s_fixed)
                    }
                }

                viewBinder.qualityGrade.text = priceInfo.qualityGradeDesc
                viewBinder.qualityGradeCopy.text = priceInfo.qualityGradeDesc
                viewBinder.paidCertification.text = priceInfo.certificatePremium
                viewBinder.paidCertificationCopy.text = priceInfo.certificatePremium
                viewBinder.netWeightPerKG.text = formatTwoDigString(receivingData.netWeight)
                viewBinder.netWeightPerKGCopy.text = formatTwoDigString(receivingData.netWeight)

                when (receivingData.certificate) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                        viewBinder.certificationLogoCopy.visibility = View.GONE
                    }
                }

                viewBinder.batchList.text = receivingData.batchNumber
                viewBinder.batchListCopy.text = receivingData.batchNumber


            }
        }


        tallyPrintKeys.clear()
        tallyPrintKeys.add(
            bitmapToString(
                getBitmapFromView(
                    view, Color.WHITE
                )
            )
        )
    }

    private fun generateWithHoldTax(type: String) {
        val priceInfo =
            intent?.getParcelableExtra<VegaNicaraguaInvoicePriceInfo>(UIUtils.INVOICE_PRICE_INFO)
                ?: VegaNicaraguaInvoicePriceInfo()
        val view = LayoutInflater.from(this)
            .inflate(R.layout.vega_nicaragua_withold_tax_receipt_printing, null)
        val viewBinder = VegaNicaraguaWitholdTaxReceiptPrintingBinding.bind(view)
        UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
        UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoCopy)
        UIUtils.setOlamLogoDynamically(viewBinder.certificationLogoCopy)

        when (type) {
            UIUtils.NICARAGUA_PRINT_INVOICE_PTBF -> {
                val vendorData =
                    intent?.getParcelableExtra<VegaVendor>(UIUtils.VENDOR_DATA) ?: VegaVendor()
                val grnData =
                    intent?.getParcelableExtra<GrnDetails>(UIUtils.GRN_DATA) ?: GrnDetails()
                val inventoryInfo =
                    intent?.getParcelableExtra<VegaNicaraguaInvoiceGrnInventoryModal>(UIUtils.BAGS_DATA)
                        ?: VegaNicaraguaInvoiceGrnInventoryModal()

                viewBinder.vendorName.text = vendorData.vendorName
                viewBinder.vendorId.text = vendorData.taxNumber
                viewBinder.consecutiveNumber.text =
                    "N° " + (intent?.getStringExtra(SUB_TITLE))!!.split(":")[1]
                viewBinder.withHoldTax.text = priceInfo.withholdingTax
                viewBinder.grossAmount.text = priceInfo.grossValue
                viewBinder.dateAndHour.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.withHoldTaxPercentage.text = String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    (covertToDouble(priceInfo.withholdingTax) * 100) / covertToDouble(priceInfo.grossValue)
                ) + "%"


                viewBinder.vendorName.text = vendorData.vendorName
                viewBinder.vendorIdCopy.text = vendorData.taxNumber
                viewBinder.consecutiveNumberCopy.text =
                    "N° " + (intent?.getStringExtra(SUB_TITLE))!!.split(":")[1]
                viewBinder.withHoldTaxCopy.text = priceInfo.withholdingTax
                viewBinder.grossAmountCopy.text = priceInfo.grossValue
                viewBinder.dateAndHourCopy.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.withHoldTaxPercentageCopy.text = String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    (covertToDouble(priceInfo.withholdingTax) * 100) / covertToDouble(priceInfo.grossValue)
                ) + "%"


                when (inventoryInfo.certification) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                        viewBinder.certificationLogoCopy.visibility = View.GONE
                    }
                }
            }
            UIUtils.NICARAGUA_PRINT_GRN_RECEIPT -> {
                val receivingData =
                    intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA)
                        ?: VegaReceiving()

                viewBinder.vendorName.text = receivingData.supplierName
                viewBinder.vendorId.text = receivingData.taxId
                viewBinder.consecutiveNumber.text = "N° " + receivingData.invoiceNumber
                // viewBinder.invoiceNumber.text=receivingData.invoiceNumber
                viewBinder.withHoldTax.text = priceInfo.withholdingTax
                viewBinder.grossAmount.text = priceInfo.grossValue
                viewBinder.dateAndHour.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.withHoldTaxPercentage.text = String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    (covertToDouble(priceInfo.withholdingTax) * 100) / covertToDouble(priceInfo.grossValue)
                ) + "%"


                viewBinder.vendorNameCopy.text = receivingData.supplierName
                viewBinder.vendorIdCopy.text = receivingData.taxId
                viewBinder.consecutiveNumberCopy.text = "N° " + receivingData.invoiceNumber
                // viewBinder.invoiceNumber.text=receivingData.invoiceNumber
                viewBinder.withHoldTaxCopy.text = priceInfo.withholdingTax
                viewBinder.grossAmountCopy.text = priceInfo.grossValue
                viewBinder.dateAndHourCopy.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.withHoldTaxPercentageCopy.text = String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    (covertToDouble(priceInfo.withholdingTax) * 100) / covertToDouble(priceInfo.grossValue)
                ) + "%"


                when (receivingData.certificate) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                        viewBinder.certificationLogoCopy.visibility = View.GONE
                    }
                }
            }
        }


        tallyPrintKeys.clear()
        tallyPrintKeys.add(
            bitmapToString(
                getBitmapFromView(
                    view, Color.WHITE
                )
            )
        )
    }

    private fun generateInvoice(type: String) {
        val priceInfo =
            intent?.getParcelableExtra<VegaNicaraguaInvoicePriceInfo>(UIUtils.INVOICE_PRICE_INFO)
                ?: VegaNicaraguaInvoicePriceInfo()
        val view =
            LayoutInflater.from(this).inflate(R.layout.vega_nicargua_invoice_print_reciept, null)
        val viewBinder = VegaNicarguaInvoicePrintRecieptBinding.bind(view)
        UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
        UIUtils.setOlamLogoDynamically(viewBinder.certificationLogo)
        when (type) {
            UIUtils.NICARAGUA_PRINT_INVOICE_PTBF -> {
                val vendorData =
                    intent?.getParcelableExtra<VegaVendor>(UIUtils.VENDOR_DATA) ?: VegaVendor()
                val grnData =
                    intent?.getParcelableExtra<GrnDetails>(UIUtils.GRN_DATA) ?: GrnDetails()
                val inventoryInfo =
                    intent?.getParcelableExtra<VegaNicaraguaInvoiceGrnInventoryModal>(UIUtils.BAGS_DATA)
                        ?: VegaNicaraguaInvoiceGrnInventoryModal()
                viewBinder.vendorName.text = vendorData.vendorName
                viewBinder.materialName.text = grnData.materialName
                viewBinder.identificationNum.text = vendorData.taxNumber

                viewBinder.plantName.text = inventoryInfo.plantName
                viewBinder.qualityGrade.text = priceInfo.qualityGradeDesc
                viewBinder.tvBagsQuantity.text = inventoryInfo.bagCount
                viewBinder.tvGrossWeight.text =
                    formatTwoDigString(inventoryInfo.grossWeight?.trim().toString())
                viewBinder.tvTareWeight.text = inventoryInfo.tareWeight?.trim().toString()
                /*(if (inventoryInfo.bagCount?.isNotEmpty() == true) inventoryInfo.bagCount?.toInt()
                    ?: 0 else 0).times(
                    if (inventoryInfo.tareWeight?.trim()?.isNotEmpty() == true) inventoryInfo.tareWeight?.trim()
                        ?.toDouble() ?: 0.0 else 0.0
                ).formatTwoDigits()*/
                viewBinder.tvBagNetWeight.text = inventoryInfo.netWeight?.trim()
                viewBinder.tvGrossPricetKg.text = priceInfo.grossValuePerKg
                viewBinder.tvBagNetWeightQQS.text = priceInfo.netWeightQQs
                viewBinder.tvtotalAmount.text = priceInfo.grossValue
                viewBinder.coffeeSettlement.text =
                    (intent?.getStringExtra(SUB_TITLE))!!.split(":")[1]
                viewBinder.sAPMIRONum.text = ""
                viewBinder.note.text = Html.fromHtml(
                    getString(R.string.s_invoice_note_1) + " <b>" + vendorData.vendorName + "</b> " + getString(
                        R.string.s_invoice_note_2
                    )
                ).toString()

                when (inventoryInfo.certification) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                    }
                }
                viewBinder.batchList.text = grnData.charg
            }
            UIUtils.NICARAGUA_PRINT_GRN_RECEIPT -> {
                val receivingData =
                    intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA)
                        ?: VegaReceiving()
                viewBinder.vendorName.text = receivingData.supplierName
                viewBinder.materialName.text = receivingData.materialName
                viewBinder.identificationNum.text = receivingData.taxId

                when (receivingData.certificate) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                    }
                }


                viewBinder.plantName.text = receivingData.plantName
                viewBinder.qualityGrade.text = receivingData.gradeDesc
                viewBinder.tvBagsQuantity.text = receivingData.bagCount
                viewBinder.tvGrossPricetKg.text = priceInfo.grossValuePerKg
                viewBinder.tvtotalAmount.text = priceInfo.grossValue
                viewBinder.tvGrossWeight.text =
                    formatTwoDigString(receivingData.grossWeight.toString())
                if (receivingData.materialName?.contains("tolling", true) == true)
                    viewBinder.tvTareWeight.text = receivingData.tareWeight
                else
                    viewBinder.tvTareWeight.text =
                        (if (receivingData.bagCount?.isNotEmpty() == true) receivingData.bagCount?.toInt()
                            ?: 0 else 0).times(
                            if (receivingData.tareWeight?.isNotEmpty() == true) receivingData.tareWeight?.toDouble()
                                ?: 0.0 else 0.0
                        ).formatTwoDigits()
                viewBinder.tvBagNetWeight.text = formatTwoDigString(receivingData.netWeight)
                viewBinder.coffeeSettlement.text = receivingData.invoiceNumber
                viewBinder.sAPMIRONum.text = ""
                viewBinder.batchList.text = receivingData.batchNumber

                viewBinder.note.text = Html.fromHtml(
                    getString(R.string.s_invoice_note_1) + " <b>" + receivingData.supplierName + " </b>" + getString(
                        R.string.s_invoice_note_2
                    )
                ).toString()

            }

        }

        viewBinder.tvBagNetWeightQQS.text = priceInfo.netWeightQQs

        viewBinder.currency.text = priceInfo.currency

        viewBinder.dateOfPay.text = DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy")


        viewBinder.TvTotalPrice.text = priceInfo.totalPrice

        viewBinder.TvCertificatePremium.text = priceInfo.certificatePremium
        viewBinder.TvHumidityPremium.text = priceInfo.humidityPremium
        viewBinder.TvVolumePremium.text = priceInfo.volumePremium

        viewBinder.TvHumidityDiscount.text = priceInfo.humidityDiscounting
        viewBinder.TvQualityDiscount.text = priceInfo.qualityDiscounting

        viewBinder.TvGrossPrice.text = priceInfo.grossValue

        viewBinder.TVexportIncentive.text = priceInfo.exportnCentives

        viewBinder.tvWitrHoldingTax.text = priceInfo.withholdingTax
        viewBinder.tvBankCommission.text = priceInfo.bankCommission
        viewBinder.tvNationalStockExchange.text = priceInfo.NSExchangeRate
        viewBinder.tvFTDC.text = priceInfo.FTDC
        viewBinder.subTotalDeduction.text = priceInfo.totalDduction
        if(priceInfo.advanceLineItemDetails.isNotEmpty()){
            var advanceSummary: Double = 0.0
            var interestSummary: Double = 0.0
            var commissionSummary: Double = 0.0
            var legalExpenseSummary: Double = 0.0
            var totalAdvanceSummary: Double = 0.0
            var currencyDevaluationSummary: Double = 0.0

            var advanceSummary15: Double = 0.0
            var interestSummary15: Double = 0.0
            var commissionSummary15: Double = 0.0
            var legalExpenseSummary15: Double = 0.0
            var totalAdvanceSummary15: Double = 0.0
            var currencyDevaluationSummary15: Double = 0.0
            priceInfo.advanceLineItemDetails.forEach {
                if(it.documentNumber?.startsWith("13", true) == true){
                    advanceSummary += covertToDouble(it.advanceKnockAmount)
                    interestSummary += covertToDouble(it.interestAmount)
                    commissionSummary += covertToDouble(it.commissionAmount)
                    legalExpenseSummary += covertToDouble(it.legalExpenseAmount)
                    currencyDevaluationSummary += covertToDouble(it.currencyDevaluationAmount)
                    totalAdvanceSummary += covertToDouble(it.totalAdvanceKnockAmount)
                }
                if(it.documentNumber?.startsWith("15", true) == true){
                    advanceSummary15 += covertToDouble(it.advanceKnockAmount)
                    interestSummary15 += covertToDouble(it.interestAmount)
                    commissionSummary15 += covertToDouble(it.commissionAmount)
                    legalExpenseSummary15 += covertToDouble(it.legalExpenseAmount)
                    currencyDevaluationSummary15 += covertToDouble(it.currencyDevaluationAmount)
                    totalAdvanceSummary15 += covertToDouble(it.totalAdvanceKnockAmount)
                }
            }

            viewBinder.tvAdvances.text = advanceSummary.toString()
            viewBinder.tvInterest.text = interestSummary.toString()
            viewBinder.tvAdvanceCommission.text = commissionSummary.toString()
            viewBinder.tvAdvanceLegalExpense.text = legalExpenseSummary.toString()
            viewBinder.tvMaintenanceValue.text = currencyDevaluationSummary.toString()
            viewBinder.tvTotal13.text = totalAdvanceSummary.toString()

            viewBinder.tvAdvances15.text = advanceSummary15.toString()
            viewBinder.tvInterest15.text = interestSummary15.toString()
            viewBinder.tvAdvanceCommission15.text = commissionSummary15.toString()
            viewBinder.tvAdvanceLegalExpense15.text = legalExpenseSummary15.toString()
            viewBinder.tvMaintenanceValue15.text = currencyDevaluationSummary15.toString()
            viewBinder.tvTotal15.text = totalAdvanceSummary15.toString()

            viewBinder.tvSubTotalAdvanceDeductions.text = (totalAdvanceSummary.plus(totalAdvanceSummary15)).toString()

            viewBinder.advanceLayout.isVisible = !(advanceSummary <= 0 && advanceSummary15 <=0)
            viewBinder.interestLayout.isVisible = !(interestSummary <= 0 && interestSummary15 <=0)
            viewBinder.commissionLayout.isVisible = !(commissionSummary <= 0 && commissionSummary15 <=0)
            viewBinder.legalExpenceLayout.isVisible = !(legalExpenseSummary <= 0 && legalExpenseSummary15 <=0)
            viewBinder.maintainanceLayout.isVisible = !(currencyDevaluationSummary <= 0 && currencyDevaluationSummary15 <=0)
            viewBinder.totalAdvanceLayout.isVisible = !(totalAdvanceSummary <= 0 && totalAdvanceSummary15 <=0)
            viewBinder.adTotalLayout.isVisible = !(totalAdvanceSummary <= 0 && totalAdvanceSummary15 <=0)
            if(totalAdvanceSummary <= 0){
                viewBinder.tvAdvanFor13.gone()
                viewBinder.tvAdvances.gone()
                viewBinder.tvInterest.gone()
                viewBinder.tvAdvanceCommission.gone()
                viewBinder.tvAdvanceLegalExpense.gone()
                viewBinder.tvMaintenanceValue.gone()
                viewBinder.tvTotal13.gone()
            }

            if(totalAdvanceSummary15 <= 0){
                viewBinder.tvAdvanFor15.gone()
                viewBinder.tvAdvances15.gone()
                viewBinder.tvInterest15.gone()
                viewBinder.tvAdvanceCommission15.gone()
                viewBinder.tvAdvanceLegalExpense15.gone()
                viewBinder.tvMaintenanceValue15.gone()
                viewBinder.tvTotal15.gone()
            }

        }else{
            viewBinder.advanceHeaderLayout.gone()
            viewBinder.advanceLayout.gone()
            viewBinder.interestLayout.gone()
            viewBinder.commissionLayout.gone()
            viewBinder.legalExpenceLayout.gone()
            viewBinder.maintainanceLayout.gone()
            viewBinder.totalAdvanceLayout.gone()
            viewBinder.adTotalLayout.gone()
        }



        if (covertToDouble(priceInfo.volumePremium) > 0) {
            viewBinder.volumePremiumLayout.visibility = View.VISIBLE
        } else {
            viewBinder.volumePremiumLayout.visibility = View.GONE
        }

        if (covertToDouble(priceInfo.certificatePremium) > 0) {
            viewBinder.certificatePremiumLayout.visibility = View.VISIBLE
        } else {
            viewBinder.certificatePremiumLayout.visibility = View.GONE
        }

        if (covertToDouble(priceInfo.humidityPremium) > 0) {
            viewBinder.humidityPremiumLayout.visibility = View.VISIBLE
        } else {
            viewBinder.humidityPremiumLayout.visibility = View.GONE
        }

        if (covertToDouble(priceInfo.humidityDiscounting) > 0) {
            viewBinder.humidityDiscountLayout.visibility = View.VISIBLE
        } else {
            viewBinder.humidityDiscountLayout.visibility = View.GONE
        }

        if (covertToDouble(priceInfo.qualityDiscounting) > 0) {
            viewBinder.qualityDiscountLayout.visibility = View.VISIBLE
        } else {
            viewBinder.qualityDiscountLayout.visibility = View.GONE
        }


        if (!(viewBinder.qualityDiscountLayout.isVisible || viewBinder.humidityDiscountLayout.isVisible)) {
            viewBinder.discountHeader.visibility = View.GONE
        }

        if (!(viewBinder.humidityPremiumLayout.isVisible || viewBinder.certificatePremiumLayout.isVisible || viewBinder.volumePremiumLayout.isVisible)) {
            viewBinder.othersHeader.visibility = View.GONE
        }

        /*if (covertToDouble(priceInfo.advanceSummary) > 0) {

            if (covertToDouble(priceInfo.advanceInterestSummary) > 0) {
                viewBinder.interestLayout.visibility = View.VISIBLE
            } else {
                viewBinder.interestLayout.visibility = View.GONE
            }

            if (covertToDouble(priceInfo.advanceCommissionSummary) > 0) {
                viewBinder.commissionLayout.visibility = View.VISIBLE
            } else {
                viewBinder.commissionLayout.visibility = View.GONE
            }

            if (covertToDouble(priceInfo.advanceLegalExpenseSummary) > 0) {
                viewBinder.legalExpenceLayout.visibility = View.VISIBLE
            } else {
                viewBinder.legalExpenceLayout.visibility = View.GONE
            }
            if (covertToDouble(priceInfo.advanceMaintainceSummary) > 0) {
                viewBinder.maintainanceLayout.visibility = View.VISIBLE
            } else {
                viewBinder.maintainanceLayout.visibility = View.GONE
            }
            if (covertToDouble(priceInfo.totalAdvanceSummary) > 0) {
                viewBinder.totalAdvanceLayout.visibility = View.VISIBLE
            } else {
                viewBinder.totalAdvanceLayout.visibility = View.GONE
            }

        } else {

            viewBinder.advanceLayout.visibility = View.GONE
            viewBinder.commissionLayout.visibility = View.GONE
            viewBinder.interestLayout.visibility = View.GONE
            viewBinder.legalExpenceLayout.visibility = View.GONE
            viewBinder.maintainanceLayout.visibility = View.GONE
            viewBinder.totalAdvanceLayout.visibility = View.GONE
        }*/
        val nPayment = (if (priceInfo.netPayment?.isNotEmpty() == true) priceInfo.netPayment?.toDouble()
            ?: 0.0 else 0.0).minus(
            (if (priceInfo.totalAdvanceSummary?.isNotEmpty() == true) priceInfo.totalAdvanceSummary?.toDouble()
                ?: 0.0 else 0.0)
        )
        viewBinder.tvNetPayment.text = nPayment.formatTwoDigits()

        tallyPrintKeys.clear()
        tallyPrintKeys.add(
            bitmapToString(
                getBitmapFromView(
                    view, Color.WHITE
                )
            )
        )
    }

    private fun calculateStorageLoss(weight: String, editedWeight: String): String {
        val lossValue =
            (if (weight.isNotEmpty()) weight.toDouble() else 0.0).minus(if (editedWeight.isNotEmpty()) editedWeight.toDouble() else 0.0)
                .formatTwoDigits()
        return lossValue
    }

    private fun createLotCardBitMap() {
        showCustomLoading()
        DoAsync {
            tallyPrintKeys = ticketPrint(intent, this)
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(this, WifiMainActivity::class.java))
            }
        }.execute()
    }

    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }

    private fun moveToHomePage() {
        val currentKey = getCurrentKey()
        val offloading = intent?.getBooleanExtra("offloading", false)
        val quality = intent?.getBooleanExtra("nicaragua_quality", false)
        if (currentKey.split("_")[1].contains("NI") && (offloading == true || quality == true)) {
            finish()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun printQualityParams(){
        showCustomLoading()
        DoAsync {
            tallyPrintKeys = printForAnyLotQualityParams(intent, this)
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(this, WifiMainActivity::class.java))
            }
        }.execute()
    }

    private fun showPreviewDialog() {
        showCustomLoading()
        DoAsync {
            runOnUiThread {
                val gson = GsonUtils()
                if (tallyPrintKeysNew.size > 0 && fromCameroonCocoafgrn) {
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeysNew))
                } else {
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                }
                startActivity(Intent(this, WifiMainActivity::class.java))
            }
        }.execute()
        /*val list = mutableListOf<String>()
        list.addAll(tallyPrintKeys)
        val dialogFragment =
            PrintPreviewDialogFragment(list)
        dialogFragment.show(supportFragmentManager, "signature")*/
    }

    private fun showGRNDocPreviewDialog() {
        val list = mutableListOf<String>()
        list.addAll(grnDoc)
//        val dialogFragment = PrintPreviewDialogFragment(list)
        val dialogFragment = PrintPreviewFragment(list, "grn")
        dialogFragment.show(supportFragmentManager, "signature")
    }

    private fun showWHReceiptPreviewDialog() {
        val list = mutableListOf<String>()
        list.addAll(whList)
//        val dialogFragment = PrintPreviewDialogFragment(list)
        val dialogFragment = PrintPreviewFragment(list, "receipt")
        dialogFragment.show(supportFragmentManager, "signature")
    }


    private fun setScaledBitmap(imagePath: String): Bitmap? {
        try {
            val imageViewWidth = 347
            val imageViewHeight = 413

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = min(a = bitmapWidth / imageViewWidth, b = bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            val decodedString: ByteArray = Base64.decode(imagePath, Base64.DEFAULT)
            val decodedByte =
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, bmOptions)
            return decodedByte
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun printerModule() {
//        initBt()
        createLotCardBitMap()
        /*val list = mutableListOf<VegaCoffeeSalesLots>()
        list.addAll(lotList)
        val dialogFragment = PrintLoCardPreviewDialogFragment(list)
        dialogFragment.show(supportFragmentManager, "signature")*/
    }

    private fun initBt() {
        // Initialize the BluetoothService to perform bluetooth connections
        for (i in bitmapKey) {
            bitmapList.add(setScaledBitmap(i))
        }

        when {
            mBTAdapter.isEnabled -> {
                getPairedDevices()
                startChatService()
            }
            else -> startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), UIUtils.REQUEST_ENABLE_BT
            )
        }
    }

    fun startChatService() {
        mChatService = BluetoothService(mHandler!!)
        if (mChatService != null) {
            if (mChatService?.mState == Constants.STATE_NONE) {
                mChatService?.start()
            }
        }
    }

    private fun formatTwoDigString(str: String): String {
        if (str.isEmpty() || !str.contains(".") || !str.contains(",")) return str
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return strFormat.format(this).replace(",", "")
    }

    private fun getPairedDevices() {
        val pairedMac = PreferenceHelper.get(UIUtils.BT_MAC, "")
        if (pairedMac.isBlank()) {
            showPairedDeviceDialog()
        } else {
            showLoading()
            DoAsync {
                try {
                    val device = mBTAdapter.getRemoteDevice(pairedMac)
                    mBTSocket = device.createRfcommSocketToServiceRecord(mUUID)
                    mBTSocket?.let {
                        if (it.isConnected) it.close()
                        mChatService!!.connect(device, true)
                    }
                } catch (e: Exception) {
                    Log.d("SuccessActivity", e.message ?: "")
                    hideLoading()
                    mBTSocket?.close()
                }
            }.execute()
        }
    }

    private fun showPairedDeviceDialog() {
        mPairedDevices.addAll(mBTAdapter.bondedDevices)
        val pairedDeviceName = mBTAdapter.bondedDevices.map { it.name }
        MaterialDialog(this).show {
            title(text = getString(R.string.please_select_the_printer))
            listItemsSingleChoice(items = pairedDeviceName) { dialog, index, text ->
                if (mPairedDevices.isNotEmpty()) {
                    PreferenceHelper.save(UIUtils.BT_MAC, mPairedDevices[index].address)
                    getPairedDevices()
                }
            }
            getMetirialCustomView(this, getString(R.string.ok), "", { dismiss() }, { dismiss() })
        }
    }


    private fun sendImage(bitmap: Bitmap) {
        if (mChatService!!.mState !== Constants.STATE_CONNECTED) {
            Toast.makeText(this, "Not connected with any device", Toast.LENGTH_SHORT).show()
            return
        }

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos) //bm is the bitmap object
        val b = baos.toByteArray()
        Toast.makeText(this, b.size.toString(), Toast.LENGTH_SHORT).show()
        mChatService!!.write(b)
    }


    override fun onDestroy() {
        super.onDestroy()
        mChatService?.stop()
    }


    private var mHandler: Handler? = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                    Constants.STATE_CONNECTED -> {
                        hideLoading()
                        for (i in bitmapList) {
                            sendImage(i!!)
                        }
                    }

                    Constants.STATE_NONE -> {

                    }
                }
                Constants.MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    val writeMessage = String(writeBuf)
                }
                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                }
                Constants.MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    hideLoading()
                    val mConnectedDeviceName = msg.data.getString(Settings.Global.DEVICE_NAME)
                    if (null != this) {
                        Toast.makeText(
                            this@SuccessActivity, "Connected to "
                                    + mConnectedDeviceName, Toast.LENGTH_SHORT
                        ).show()
                        hideLoading()
                    }
                }
                Constants.MESSAGE_TOAST -> if (null != this) {
                    Toast.makeText(
                        this@SuccessActivity, msg.data.getString(Constants.TOAST),
                        Toast.LENGTH_SHORT
                    ).show()
                    hideLoading()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            getPairedDevices()
            startChatService()
        }
    }

    // fun hideLoading() = progressBar.dialog?.dismiss()

    private var bitmapKey = mutableListOf<String>()
    private var mChatService: BluetoothService? = null

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private val mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bitmapList = ArrayList<Bitmap?>()
    private val progressBar = CustomProgressBar()

    fun covertToDouble(value: String?): Double {
        if (value != null && value.length > 0) {
            val str = value.format(Locale.ENGLISH).replace(",", ".")
            return str.toDouble()
        }
        return 0.00
    }

}
