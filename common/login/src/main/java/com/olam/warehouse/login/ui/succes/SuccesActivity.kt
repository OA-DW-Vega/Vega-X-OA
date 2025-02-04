package com.olam.warehouse.login.ui.succes

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
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.*
import com.olam.warehouse.login.ui.common.VegaCommonModuleNavigation
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPost
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoiceGrnInventoryModal
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import com.olam.warehouse.presentation.ui.BluetoothService
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.FAILURE
import com.olam.warehouse.presentation.utils.AppUtils.SUB_TITLE
import com.olam.warehouse.presentation.utils.AppUtils.TITLE
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import kotlinx.android.synthetic.main.vega_nicargua_invoice_print_reciept.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
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
    private var fromCameroonCocoaGateEntry = false
    private var fromCameroonCocoaQA = false
    private var fromNigeriaCocoaQA = false
    private var fromCameroonCocoaOffloading = false
    private var fromCameroonCocoafgrn = false
    private var fromCameroonCocoaaddContainer = false
    private var nicaraguaMtntScanObdNumber = false
    private var nicaraguaMtntTruckNo = ""
    private var nicaraguaMtntObdNumber = ""
    private var isGhana = false
    private var tallyPrintKeys = ArrayList<String>()
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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
    }

    override fun onBackPressed() {
        if (fromCoffee) showLotPrintExitDialog() else {
            moveToHomePage()
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
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
        isPrintNeed = intent?.getBooleanExtra(AppUtils.PRINT_ENABLE, false) ?: false
        tallyPrintKeys = intent?.getStringArrayListExtra(AppUtils.TALLY_SHEETS) ?: ArrayList()
        whList = intent?.getStringArrayListExtra(AppUtils.WH_RECEIPT) ?: ArrayList()
        grnDoc = intent?.getStringArrayListExtra(AppUtils.GRN_DOCUMENT) ?: ArrayList()

        fromCoffee = intent?.getBooleanExtra("fromcoffee", false) ?: false
        fromSesame = intent?.getBooleanExtra("fromsesame", false) ?: false
        fromSesameGrn = intent?.getBooleanExtra("fromsesamegrn", false) ?: false
        fromSesameProcessing = intent?.getBooleanExtra("fromsesameprocessing", false) ?: false
        fromNicarguaCoffee = intent?.getBooleanExtra(UIUtils.FROM_NICARAGUA_COFFEE, false) ?: false
        fromCameroonCocoaGateEntry =
            intent?.getBooleanExtra(UIUtils.FROM_CAMEROON_GATEENTRY_COCOA, false) ?: false
        fromCameroonCocoaQA =
            intent?.getBooleanExtra(UIUtils.FROM_CAMEROON_COCOA_QA, false) ?: false
        fromNigeriaCocoaQA = intent?.getBooleanExtra(UIUtils.FROM_NIGERIA_COCOA_QA, false) ?: false
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
        fromCoffeeGrn = intent?.getBooleanExtra(UIUtils.FROM_NIC_MTNR_GRN, false) ?: false
        printTicket = intent?.getBooleanExtra(UIUtils.PRINT_TICKET, false) ?: false
        printFgrnTallySheet = intent?.getBooleanExtra(UIUtils.PRINT_FGRN_TALLYSHEET, false) ?: false
        fgrnBatch = intent?.getStringExtra(UIUtils.DISPATCH_BATCH) ?: "AB21000056"
        printLocalSalesTallySheet =
            intent?.getBooleanExtra(UIUtils.PRINT_LOCAL_SALES_TALLY_SHEET, false) ?: false
        //lotList = intent?.getParcelableExtra<VegaReceiving>(UIUtils.LOT_DETAILS_LOCAL_SALES) ?: VegaCameroonS()


        if (intent?.hasExtra(FAILURE) == true) {
            binding.animationFailView.visible()
            binding.animationView.gone()
        } else {
            binding.animationFailView.gone()
            binding.animationView.visible()
        }
        if (isGhana) {
            binding.btnWithquality.visibility = View.VISIBLE
        }
        binding.tvTitle.text = title
        binding.tvSubTitle.text = subTitle
        binding.btnOk.setOnClickListener { if (fromCoffee) showLotPrintExitDialog() else moveToHomePage() }
        if (printTicket) {
            binding.printTicket.visible()
            binding.btnPrint.visibility = View.GONE
        }
        binding.printTicket.setOnClickListener {
            createTicketCardBitMap()
        }
        if (printFgrnTallySheet) {
            binding.btnPrint.visibility = View.GONE
            binding.printFgrnTallySheet.visible()
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
        if(fromCameroonCocoaaddContainer){
            binding.btnPrint.text = getString(R.string.print_container_id)
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
            } else
                showPreviewDialog()
        }
        if (nicaraguaMtntScanObdNumber) {
            binding.btnObdScan.visibility = View.VISIBLE
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
                separated = datestringformated.split("/").toTypedArray()

                }
             val localDate: LocalDate = LocalDate.of(separated[2].trim().toInt(),separated[1].trim().toInt(),separated[0].trim().toInt())
             val spanishLocale = Locale("es", "ES")
             val dateInSpanish: String =
                 localDate.format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy", spanishLocale))*/
                viewBinder.tvcontractdate.text =DateUtils.convertDateToSpanish(receivingData.docDate.toString())
               var deliverydate=DateUtils.convertDateToSpanish(receivingData.deliveryDate.toString())
                viewBinder.tvdeliveryterm.text =deliverydate.replace("-"," de ")
                viewBinder.tvQuantity.text =pricegrosskg+" "+resources.getString(R.string.qq)+" "+resources.getString(R.string.equivalente)+" "+receivingData.netWeight+" "+resources.getString(R.string.kg)
                viewBinder.tvcontractnumber.text =receivingData.poSequenceNumber
                viewBinder.tvProductName.text =receivingData.materialName
                viewBinder.tvQualityGrade.text =  receivingData.qualityGradeDesc
                viewBinder.tvpricegrosskg.text =resources.getString(R.string.c_doller)+" "+receivingData.pricePerUnit
                viewBinder.tvnetpricexQQPOA.text =resources.getString(R.string.c_doller).plus(" ").plus(receivingData.receiptNetPrice)
                viewBinder.tvplaceOfDelivery.text =resources.getString(R.string.s_buying_unit)+" "+receivingData.plant?.plantName
                viewBinder.tvdata2.text =receivingData.vendorName
                viewBinder.buyer.text =resources.getString(R.string.print_title_1)+"\n"+resources.getString(R.string.print_title_3)+"\n"+resources.getString(R.string.print_title_2)
                viewBinder.title2.text =resources.getString(R.string.direction)+" "+resources.getString(R.string.print_title_3)

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
            val receivingData =
                intent?.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()
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
        }
        binding.btnPrint.visibility = if (isPrintNeed) View.VISIBLE else View.GONE
        prepareBitmapForPrinting()
        prepareBitmapForCameroonGrnPrinting()
        changeButtonStyles()
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
            viewBinder.tvSacksValue.text = receivingData.declaredBagCount?.trim()
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


    private fun generateFgrnTallySheetBitMap() {
        tallyPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            val view =
                LayoutInflater.from(this).inflate(R.layout.item_print_fgrn_tallysheet_preview, null)
            val viewBinder = ItemPrintFgrnTallysheetPreviewBinding.bind(view)
            viewBinder.ivPreview.setImageBitmap(getBitmap(fgrnBatch))
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


    private fun prepareBitmapForCameroonGrnPrinting() {
        if (fromCameroonCocoaQA) {
            if (fromNigeriaCocoaQA) {
                binding.grnReceipt.visibility = View.GONE
                binding.printLotCard.visibility = View.VISIBLE
                binding.btnPrint.visibility = View.GONE
            } else {
                binding.grnReceipt.visibility = View.VISIBLE
                binding.printLotCard.visibility = View.VISIBLE
                binding.btnPrint.visibility = View.GONE
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

            binding.grnReceipt.setOnClickListener {
                showCustomLoading()
                DoAsync {
                    val view =
                        LayoutInflater.from(this).inflate(R.layout.vega_cameroon_cocoa_grn_receipt_printing, null)
                    val viewBinder = VegaCameroonCocoaGrnReceiptPrintingBinding.bind(view)

                    viewBinder.tvDdNo.text = receivingData.grnNumber
                    viewBinder.tvLotNo.text = receivingData.batchNumber
                    viewBinder.tvProduct.text = receivingData.materialName
                    viewBinder.tvSupplier.text = receivingData.supplierName
                    viewBinder.tvDate.text = receivingData.year
                    viewBinder.tvUnit.text = receivingData.plantId
                    viewBinder.tvBeNo.text = receivingData.wbid
                    viewBinder.tvConsentEnglish.text = getString(R.string.consentEnglish)
                    viewBinder.tvConsentForeign.text = getString(R.string.consentForeign)

                    lotList.forEachIndexed { index, item ->
                        viewBinder.tvCleanCocoa.text = item.rsNum
                        viewBinder.tvRcvdAt.text = item.plantId
                        viewBinder.tvTotalRefraction.text = item.meins
                        viewBinder.tvNetWt.text = item.weight
                        viewBinder.tvMtntNo.text = item.deliveryItem
                        viewBinder.tvInspectionLot.text = item.bwart
                        viewBinder.tvTruck.text = item.grade
                        viewBinder.tvmtntWeight.text = item.phase
                        viewBinder.tvGrossWeight.text = item.xchpf
                        viewBinder.tvBagCount.text = item.noOfBags
                        viewBinder.tvBagWeight.text = item.salesType
                        viewBinder.tvPalletWeight.text = item.saleOrderId
                        viewBinder.tvJuteBagCount.text = item.processOrderNo
                        viewBinder.tvNylonBagCount.text = item.rsPos
                        viewBinder.tvPalletCount.text = item.certificate
                    }

                    viewBinder.tvBeanCountRefraction.text = "0"

                        viewBinder.tvOtherDefects.text = "0"
                    vegaBcApprovePostData.qualityDetails.forEach{
                        when(it.nameChar){
                            "B_MOIST" -> viewBinder.tvHumidity.text = it.qualityParameterValue
                            "B_MOULD4" -> {
                                viewBinder.tvMould.text = it.qualityParameterValue
                                mould = it.qualityParameterValue.toString().toDouble()
                            }
                            "B_SL" -> {
                                viewBinder.tvSlaty.text = it.qualityParameterValue
                                slaty = it.qualityParameterValue.toString().toDouble()
                            }
                            "WASTE" -> viewBinder.tvWaste.text = it.qualityParameterValue
                            "B_SMOKY" -> {
                                viewBinder.tvSmoky.text = it.qualityParameterValue
                                when(it.qualityParameterValue){
                                    "IR-AB/PR 003" ->
                                        viewBinder.tvSmoky.text = "Absence"
                                    "IR-AB/PR 004" ->
                                        viewBinder.tvSmoky.text = "Presence"
                                }
                            }
                            "B_CLUSTER1" -> viewBinder.tvCluster.text = it.qualityParameterValue
                            "FLAT" -> viewBinder.tvFlat.text = it.qualityParameterValue
                            "B_BEANCOUNT" -> viewBinder.tvBeanCount.text = it.qualityParameterValue
                            "B_RESIDUE" -> viewBinder.tvResidue.text = it.qualityParameterValue
                            "B_MOIS_REFR" -> viewBinder.tvHumidityRefraction.text = it.qualityParameterValue
                            "B_MOULD_REFR" -> viewBinder.tvMouldRefraction.text = it.qualityParameterValue
                            "B_WAST_REFR" -> viewBinder.tvWasteRefraction.text = it.qualityParameterValue
                            "B_RESIDU_REFR" -> viewBinder.tvResidueRefraction.text = it.qualityParameterValue
                            "B_CLUSTER_REFR" -> viewBinder.tvClusterRefraction.text = it.qualityParameterValue
                            "B_SECONDARY_REFR" -> viewBinder.tvSecondaryRefraction.text = it.qualityParameterValue
                        }
                    }

                    var totalDefects = mould + slaty
                    viewBinder.tvTotalDefects.text = totalDefects.toString()
                    viewBinder.tvBeanCountRefraction.text = ""
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

                                    viewBinder.typeOfPurchaseCopy.text = getString(R.string.s_ptbf)
                                    viewBinder.typeOfPurchase.text = getString(R.string.s_ptbf)
                                }
                                getString(R.string.spot) -> {
                                    viewBinder.note.visibility = View.GONE
                                    viewBinder.notecopy.visibility = View.GONE
                                    viewBinder.typeOfPurchaseCopy.text = getString(R.string.s_spot)
                                    viewBinder.typeOfPurchase.text = getString(R.string.s_spot)
                                }

                                getString(R.string.spot_tolling) -> {
                                    viewBinder.note.visibility = View.GONE
                                    viewBinder.notecopy.visibility = View.GONE
                                    viewBinder.typeOfPurchaseCopy.text =
                                        getString(R.string.s_tolling)
                                    viewBinder.typeOfPurchase.text = getString(R.string.s_tolling)
                                }

                                getString(R.string.fixed) -> {
                                    viewBinder.note.visibility = View.GONE
                                    viewBinder.notecopy.visibility = View.GONE
                                    viewBinder.typeOfPurchaseCopy.text = getString(R.string.s_fixed)
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
                                            R.drawable.starbucks_logo
                                        )
                                    )
                                    viewBinder.certificationLogo.visibility = View.VISIBLE
                                    viewBinder.certificationLogoCopy.setImageDrawable(
                                        resources.getDrawable(
                                            R.drawable.starbucks_logo
                                        )
                                    )
                                    viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                                }
                                UIUtils.NICERTFD_UTZ -> {
                                    viewBinder.certificationLogo.setImageDrawable(
                                        resources.getDrawable(
                                            R.drawable.utz_logo
                                        )
                                    )
                                    viewBinder.certificationLogo.visibility = View.VISIBLE
                                    viewBinder.certificationLogoCopy.setImageDrawable(
                                        resources.getDrawable(
                                            R.drawable.utz_logo
                                        )
                                    )
                                    viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                                }
                                UIUtils.NICERTFD_RFA -> {
                                    viewBinder.certificationLogo.setImageDrawable(
                                        resources.getDrawable(
                                            R.drawable.rfa_logo
                                        )
                                    )
                                    viewBinder.certificationLogo.visibility = View.VISIBLE
                                    viewBinder.certificationLogoCopy.setImageDrawable(
                                        resources.getDrawable(
                                            R.drawable.rfa_logo
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
                                if (receivingData.materialName?.contains("tolling", true) == true)
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
                UIUtils.NICARAGUA_PRINT_MTNT_RECEIPT -> {
                    binding.btnMtntPrint.visible()
                    val req = intent?.getBooleanExtra(UIUtils.PRINT_TALLY_SHEET, false)
                    if (req == true)
                        binding.btnMtntPrint.text = getString(R.string.tally_sheet)
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
                            var storageLoss = 0.0
                            var weight: String = "0"
                            if (lot.size > 1) {
                                lot.filter { it.isMergedLot == false }.forEach { it1 ->
                                    weight =
                                        (java.lang.Double.valueOf(weight) + java.lang.Double.valueOf(
                                            it1.weight.toString()
                                        )).toString()
                                }
                                storageLoss = covertToDouble(
                                    calculateStorageLoss(
                                        weight,
                                        mtnt.mergedNetWeight.toString()
                                    )
                                )
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
                                storageLoss.toString().plus(" ").plus(getString(R.string.kg))
                            viewBinder.tvNoOfLots.text =
                                lot.filter { it.isMergedLot == false }.map { it.batchNumber }
                                    .toString().replace("[", "").replace("]", "")
                            if (mtnt.erdat?.isNotEmpty() == true)
                                viewBinder.tvDate.text = mtnt.erdat.let { it1 ->
                                    it1?.let { it2 ->
                                        DateUtils.getUTCDateTimeNicaragua(
                                            it2,
                                            this
                                        )
                                    }
                                }
                            viewBinder.tvMaterial.text = mtnt.materialName
                            viewBinder.tvQualityGrade.text = mtnt.qulityGradeDesc
                            viewBinder.tvUOM.text = mtnt.unitsOfMeasure
                            viewBinder.tvBagsQuantity.text =
                                bags.sumBy { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }
                                    .toString()
                            viewBinder.tvGrossWeight.text =
                                bags.sumByDouble { if (it.grossWeight.isNotEmpty() == true) it.grossWeight.toDouble() else 0.0 }
                                    .formatTwoDigits()
                            viewBinder.tvNetWeight.text =
                                bags.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                                    .formatTwoDigits()
                            viewBinder.tvTareWeight.text = bags.sumByDouble {
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
                                storageLoss.toString().plus(" ").plus(getString(R.string.kg))
                            viewBinder.tvNoOfLotsCopy.text =
                                lot.filter { it.isMergedLot == false }.map { it.batchNumber }
                                    .toString().replace("[", "").replace("]", "")
                            viewBinder.tvDateCopy.text = mtnt.erdat.let { it1 ->
                                it1?.let { it2 ->
                                    DateUtils.getUTCDateTimeNicaragua(
                                        it2,
                                        this
                                    )
                                }
                            }
                            viewBinder.tvMaterialCopy.text = mtnt.materialName
                            viewBinder.tvQualityGradeCopy.text = mtnt.qulityGradeDesc
                            viewBinder.tvUOMCopy.text = mtnt.unitsOfMeasure
                            viewBinder.tvBagsQuantityCopy.text =
                                bags.sumBy { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }
                                    .toString()
                            viewBinder.tvGrossWeightCopy.text =
                                bags.sumByDouble { if (it.grossWeight.isNotEmpty() == true) it.grossWeight.toDouble() else 0.0 }
                                    .toString()
                            viewBinder.tvNetWeightCopy.text =
                                bags.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                                    .toString()
                            viewBinder.tvTareWeightCopy.text = bags.sumByDouble {
                                (if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() else 0.0)?.times(
                                    if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0
                                )
                                    ?: 0.0
                            }.toString()

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
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
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
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
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
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
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
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
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
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                    }
                }
                viewBinder.batchList.text = grnData.batchNumber
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
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
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

        viewBinder.tvAdvances.text = priceInfo.advanceSummary
        viewBinder.tvInterest.text = priceInfo.advanceInterestSummary
        viewBinder.tvAdvanceCommission.text = priceInfo.advanceCommissionSummary
        viewBinder.tvAdvanceLegalExpense.text = priceInfo.advanceLegalExpenseSummary
        viewBinder.tvMaintenanceValue.text = priceInfo.advanceMaintainceSummary
        viewBinder.tvSubTotalAdvanceDeductions.text = priceInfo.totalAdvanceSummary

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

        if (covertToDouble(priceInfo.advanceSummary) > 0) {

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
        }
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
        tallyPrintKeys.clear()
        showCustomLoading()
        var lotList = intent?.getParcelableArrayListExtra(AppUtils.LOT_CARD)
            ?: ArrayList<VegaCoffeeSalesLots>()
        DoAsync {
            lotList.forEachIndexed { index, item ->
                val view = LayoutInflater.from(this).inflate(R.layout.item_print_lot_card_preview, null)
                val viewBinder = ItemPrintLotCardPreviewBinding.bind(view)
                if (fromCameroonCocoaGateEntry) {
                    viewBinder.tvLot.text = "Sample ID"
                    viewBinder.tvWeight.visibility = View.GONE
                    viewBinder.tvWeightValue.visibility = View.GONE
                }
                if(fromCameroonCocoaaddContainer){
                    viewBinder.tvLot.text = "Container ID"
                    viewBinder.tvWeight.text = "Shipping Line"
                    viewBinder.tvMaterial.text = "Container Size"
                }
                viewBinder.ivPreview.setImageBitmap(getBitmap(item.batchNumber))
                viewBinder.tvLotValue.text = item.batchNumber
                viewBinder.tvMaterialValue.text = item.materialName
                if(!fromCameroonCocoaQA) {
                    if (item.grade?.isNotEmpty() == true) {
                        viewBinder.tvGradeValue.text = item.grade
                        viewBinder.tvGrade.visible()
                        viewBinder.tvGradeValue.visible()
                    }
                    if (item.certificate?.isNotEmpty() == true) {
                        viewBinder.tvCertificateValue.text = item.certificate
                        viewBinder.tvCertificate.visible()
                        viewBinder.tvCertificateValue.visible()
                    }
                }
                viewBinder.tvWeightValue.text = item.editedWeight.plus(" ").plus(item.unitOfMeasure)
/*
            val parent = LinearLayout(this)
            parent.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            parent.orientation = LinearLayout.VERTICAL
            parent.background = ContextCompat.getDrawable(parent.context, R.color.white)

            //children of parent linearlayout
            val iv = ImageView(this)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(10, 16, 10, 0)
            val lpText = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lpText.setMargins(10, 0, 10, 5)
            lpText.gravity = Gravity.CENTER
            iv.layoutParams = lp
            iv.setImageBitmap(getBitmap(item.batchNumber))
            iv.layoutParams.height = 512
            iv.layoutParams.width = 512

            val tv1 = TextView(this)
            tv1.text = getString(R.string.lot_id).plus(" : ").plus(item.batchNumber)
            val tv2 = TextView(this)
            tv2.text = getString(R.string.material).plus(" : ").plus(item.materialName)
            val tv4 = TextView(this)
            if (item.grade!!.isNotEmpty()) {
                tv4.text = getString(R.string.grade).plus(" : ").plus(item.grade)
            }
            val tv5 = TextView(this)
            if (item.certificate!!.isNotEmpty()) {
                tv5.text = getString(R.string.certificate).plus(" : ").plus(item.certificate)
            }
            val tv3 = TextView(this)
            if (!item.editedWeight.isNullOrEmpty())
                tv3.text =
                    getString(R.string.weight).plus(" : ").plus(item.editedWeight?.toDouble()?.formatThreeDigits())
                        .plus(" ").plus(item.unitOfMeasure)
            tv1.layoutParams = lpText
            tv2.layoutParams = lpText
            if (item.grade!!.isNotEmpty()) tv4.layoutParams = lpText
            tv3.layoutParams = lpText
            parent.addView(iv) // lo agregamos al layout
            parent.addView(tv1)
            parent.addView(tv2)
            if (item.grade!!.isNotEmpty()) parent.addView(tv4)
            if (item.certificate!!.isNotEmpty()) parent.addView(tv5)
            parent.addView(tv3)*/
                //bitmapValue.put(index, getBitmapFromView(parent))
                tallyPrintKeys.add(
                    bitmapToString(
                        getBitmapFromView(
                            view, Color.WHITE
                        )
                    )
                )
            }
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
        if (currentKey.split("_")[1].contains("NI") && offloading == true) {
            finish()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun showPreviewDialog() {
        showCustomLoading()
        DoAsync {
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
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

    private val key =
        "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"


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
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
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
