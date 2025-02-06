package com.olam.warehouse.navigation.features

import android.content.Intent
import com.olam.warehouse.navigation.loadIntentOrNull

/**
 * Created by Baskaran Kannan on 12/17/2019.
 */

object DPFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.dpivccashew.ui.DPIVCHomeActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object DOReceivingFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.odreceiving.ui.DOReceivingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object DOQualityFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.odquality.ui.DOQualityActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}


//Vega

object VegaIndiaCoffeeDispatchFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.dispatchindiacoffee.ui.VegaDispatchActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGhanaCocoaDispatchFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaCocoaDispatchMtntActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndiaCoffeeInventoryFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventoryindiacoffee.ui.VegaIndiaCoffeeInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndiaCoffeeQualityApprovalFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.VegaQualityApproveIndiaCoffeeActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaNigeriaQualityApprovalFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.qualityapprovenigeria.ui.VegaQualityApproveNigeriaActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndiaCoffeePpqFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.ppqindiacoffee.ui.VegaIndiaCoffeePpqActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndiaCoffeeProcessingFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingindiacoffee.ui.VegaProcessingIndiaCoffeeActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaEcuadorCocoaProcessingFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingecuador.ui.VegaEcuadorProcessingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndiaCoffeeGRNFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.grnindiacoffee.ui.VegaIndiaCoffeeGrnActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndiaCoffeeOffloadingFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.offloadingindiacoffee.ui.VegaIndiaCoffeeOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndiaCoffeeWeighmentFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeReceivingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndiaCoffeeQualityFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.qualityindiacoffee.ui.VegaIndiaCoffeeQualityActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaReceivingFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.receiving.ui.VegaReceivingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaQualityFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.quality.ui.VegaQualityActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaApproveNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.approve.ui.VegaApproveActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaApproveGhanaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.approveghana.ui.VegaGhanaGrnActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaQualityApproveCameroonNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.qualityapprovecameroon.ui.VegaQualityApproveCameroonActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.offloading.ui.VegaOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaCoffeeOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndoCoffeeOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.offloadingindo.ui.VegaIndoCoffeeOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}


object VegaCoCoaOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaCoffeeWeighmentNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeWeighmentActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaDispatchNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.dispatch.ui.VegaDispatchActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}


object VegaDispatchCocoNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaDispatchMtntCoffeeNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeDispatchMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaDispatchMtntSesameNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameDispatchMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaDispatchMtntGhanaNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaDispatchMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaDispatchMtntCameroonNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntcameroon.ui.VegaCameroonDispatchMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}
object VegaDispatchMtntEcuadorNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.dispatchecuador.ui.VegaEcuadorDispatchActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaGhanaCocoaDispatchMtntNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaCocoaDispatchMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaExportSalesCoffeeNavigation : DynamicFeature<Intent> {

    const val FEATURE =
        "com.olam.warehouse.vegax.exportsalescoffee.ui.VegaCoffeeExportSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaIndoCoffeeSalesCoffeeNavigation : DynamicFeature<Intent> {

    const val FEATURE =
        "com.olam.warehouse.vegax.exportsalesindo.ui.VegaIndoCoffeeExportSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaNigeriaCocoaExportSalesCoffeeNavigation : DynamicFeature<Intent> {

    const val FEATURE =
        "com.olam.warehouse.vegax.exportsalesnigeria.ui.VegaNigeriaExportSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaExportSalesCameroonNavigation : DynamicFeature<Intent> {

    const val FEATURE =
        "com.olam.warehouse.vegax.exportsalescameroon.ui.VegaCameroonExportSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}
object VegaExportSalesEcuadorNavigation : DynamicFeature<Intent> {

    const val FEATURE =
        "com.olam.warehouse.vegax.exportsalesecuador.ui.VegaEcuadorCocoaExportSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaThirdPartySalesCoffeeNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaCocoaThirdPartySalesNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.thirdpartycocoa.ui.VegaCocoaThirdPartyActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaDispatchSalesCocoNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.salescocoa.ui.VegaCocoaDispatchSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaDispatchSalesCameroonCocoNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.localsalescameroon.ui.VegaCameroonSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}
object VegaDispatchSalesEcuadorCocoNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.localsalesecuador.ui.VegaEcuadorCocoaSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaSalesCoffeeNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.salescoffee.ui.VegaCoffeeSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaProcessingNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processing.ui.VegaProcessingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaProcessingCocoaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingcocoa.ui.VegaProcessingCocoaActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaCameroonProcessingCocoaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingcameroon.ui.VegaCameroonProcessingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGhanaProcessingNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingghana.ui.VegaGhanaProcessingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaProcessingCoffeeNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingcoffee.ui.VegaCoffeeProcessingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaProcessingIndoNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingindo.ui.VegaProcessingIndoActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaProcessingNigeriaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingnigeria.ui.VegaNigeriaProcessingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaProcessingSesameNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.processingsesame.ui.VegaSesameProcessingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGateEntryNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.gateentry.ui.VegaGateEntryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGateEntryCameroonNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.gateentrycameroon.ui.VegaGateEntryCameroonActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaSecretIdCameroonNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.secretid.ui.VegaCameroonSecretIdActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaCommonSecretIdNavigation: DynamicFeature<Intent>{
    private const val DPIVC = "com.olam.warehouse.vegax.secretidcommon.ui.VegaSecretIdActivity"

    override val dynamicStart: Intent? get() = DPIVC.loadIntentOrNull()
}

object VegaCommonShipmentNavigation: DynamicFeature<Intent>{
    private const val DPIVC = "com.olam.warehouse.vegax.shipment.ui.VegaShipmentActivity"
    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaSecretIdNigeriaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.secretidnigeria.ui.VegaNigeriaSecretIdActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGateEntryGhanaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.gateentryghana.ui.VegaGateEntryGhanaActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGateEntryGhanaCocoaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.gateentryghanacocoa.ui.VegaGateEntryGhanaCocoaActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGateEntryCoffeeNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.gateentrycoffee.ui.VegaCoffeeGateEntryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGateEntryNigeriaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.gateentrynigeria.ui.VegaGateEntryNigeriaActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGateEntryApprovalNigeriaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.gateentryapprovalnigeria.ui.VegaGateEntryApprovalNigeriaActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}


object VegaInventoryNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventory.ui.VegaInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaInventoryCocoNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventorycocoa.ui.VegaCocoaInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaInventoryIndoCoffeeNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventoryindo.ui.VegaIndoCoffeeInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}


object VegaNigeriaInventoryCocoNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventorynigeria.ui.VegaNigeriaInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaCameroonInventoryCocoNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventorycameroon.ui.VegaCameroonInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaInventoryCoffeeNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventorycoffee.ui.VegaCoffeeInventoryActivity"
    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaInventorySesameNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventorysesame.ui.VegaNigeriaSesameInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}
object VegaInventoryGhanaCocoaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventoryghanacocoa.ui.VegaGhanaCocoaInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaInventoryGhanaNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventoryghana.ui.VegaGhanaInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaSweepingsCocoNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.sweepingcocoa.ui.VegaCocoaSweepingsActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaPchNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.pch.ui.VegaPchActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaOfflineNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.vegaoffline.ui.VegaCreateItemActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaEcuadorOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.offloadingecuador.ui.VegaEcuadorOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

//object VegaEcuadorCocoaProcessingFeatureNavigation : DynamicFeature<Intent> {
//
//    private const val DPIVC = "com.olam.warehouse.vegax.processingecuadorcocoa.ui.VegaProcessingEcuadorCocoaActivity"
//
//    override val dynamicStart: Intent?
//        get() = DPIVC.loadIntentOrNull()
//}

object VegaNigeriaOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCameroonCocoaOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaGhanaCashewOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaGhanaCocoOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaCocoaOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNigeriaSesameOffloadingNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.offloadingsesame.ui.VegaSesameOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaEcuadorQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.qualityecuador.ui.VegaEcuadorQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCameroonCocoaQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.qualitycameroon.ui.VegaCameroonQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaGhanaCashewQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.ghanaquality.ui.VegaGhanaQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNigeriaQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.qualitynigeria.ui.VegaNigeriaQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNigeriaSesameQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.qualitysesame.ui.VegaNigeriaSesameQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCoffeeQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.qualitycoffee.ui.VegaCoffeeQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaIndoCoffeeQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.qualityindo.ui.VegaIndoCoffeeQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaEcuadorGrnNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.grnecuador.ui.VegaEcuadorGrnActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaIndoCoffeeGrnNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.grnindo.ui.VegaIndoCoffeeGrnActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNigeriaGrnNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.grnnigeria.ui.VegaNigeriaGrnActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}


object VegaCoffeeGrnNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.grncoffee.ui.VegaCoffeeGRNActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCameroonCocoaGrnNavigation : DynamicFeature<Intent> {

//    private const val DPEC = "com.olam.warehouse.vegax.grncameroon.ui.VegaCameroonGrnActivity"
    private const val DPEC = "com.olam.warehouse.vegax.bcapprovecameroon.ui.VegaBcApproveCameroonActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNigeriaSesameGrnNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.grnsesame.ui.VegaNigeriaSesameGrnActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaEcuadorApproveNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.bcapproveecuador.ui.VegaEcuadorBcApproveActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaEcuadorInventoryNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.inventoryecuador.ui.VegaEcuadorInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaEcuadorDispatchNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.dispatchecuador.ui.VegaEcuadorDispatchActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaNigeriaDispatchNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaDispatchMtntActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaIndoCoffeeMtntNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.mtntindo.ui.VegaIndoCoffeeDispatchActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGhanaMtntNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.mtntghanacashew.ui.VegaGhanaMtntActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCoffeePpqNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.ppqcoffee.ui.VegaCoffeePpqActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaIndoCoffeePpqNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.ppqindo.ui.VegaIndoCoffeePpqActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCameroonContainerManagementNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.containermanagement.ui.VegaCameroonContainerManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaNigeriaContainerManagementNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.containermanagementnigeria.ui.VegaNigeriaContainerManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaIndiaCoffeeBagIssueNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.bagissueindiacoffee.ui.VegaIndiaCoffeeBagIssueActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCoffeePileNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.coffeepile.ui.VegaCoffeePileManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaIndiaCoffeePileNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.pileindiacoffee.ui.VegaIndiaCoffeePileManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaSesamePileNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.pilesesame.ui.VegaSesamePileManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaEcuadorPileNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.pileecuador.ui.VegaEcuadorPileManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNigeriaPileNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.pilemanagementnigeria.ui.VegaNigeriaPileManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaSesamePpqNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.ppqsesame.ui.VegaSesamePpqActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCameroonPpqNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.ppqcameroon.ui.VegaCameroonPpqActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaWhMovementFgToFgFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.fgtofg.ui.VegaWHMovementActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaWhMovementProductionToFgFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.productiontofg.ui.VegaProductionHomeActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaWhMovementAcknowledgementFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.acknowledgement.ui.VegaAcknowledgementHomeActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaWhMovementDeliveryPickingFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.deliverypicking.ui.DeliveryPickingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaWhMovementDownloadMasterFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.downloadmaster.ui.DownloadMasterActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaNicaraguaGrnNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCocoaMtntNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNicaraguaReportNavigation : DynamicFeature<Intent> {

    private const val RECON = "com.olam.warehouse.vegax.reconcilnicaragua.ui.VegaNicaraguaReconcilActivity"

    override val dynamicStart: Intent?
        get() = RECON.loadIntentOrNull()
}

object VegaNicaraguaInvoiceNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.invoicenicaragua.ui.VegaNicaraguaInvoiceActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCocoaMTNRNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaGhanaReceivingNavigation : DynamicFeature<Intent> {

//    private const val DPEC = "com.olam.warehouse.vegax.receivingghana.ui.VegaGhanaReceivingActivity"
    private const val DPEC = "com.olam.warehouse.vegax.receivingghanacash.ui.VegaReceivingGhanaActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaGhanaQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.qualityghana.ui.VegaGhanaQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}


object VegaNicaraguaMtntNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaNicaraguaAdvanceNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.advanceniicaragua.ui.VegaNicaraguaAdvanceActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaNicaraguaForwordPONavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.forwardponicaragua.ui.VegaNicaraguaForwardPOActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaNicaraguaInventoryNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.inventorynicaragua.ui.VegaNicaraguaInventoryActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}
object VegaCottonGinningDispatchNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.ginningwarehouse.ui.ui.dispatch.GinningDispatchActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaCottonGinningIncomingLOTsNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.GinningSelectIncomingLotTypeActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaCottonGinningDryingNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.ginningwarehouse.ui.ui.drying.GinningDryingActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaCottonGinningPileNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.ginningwarehouse.ui.ui.pile.GinningPileManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaCottonGinningInventoryNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.ginningwarehouse.ui.ui.inventory.GinningInventoryActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaCottonGinningInProgressNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.ginningwarehouse.ui.ui.ginninginprogress.GinningInprogressActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}
object VegaCottonPortInventoryNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.portwarehouse.ui.inventory.InventoryActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCottonPortPileNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.portwarehouse.ui.pile.PortPileManagementActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCottonGinningBaleDetailNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.ginningwarehouse.ui.ui.inventory.GinningBaleDetailActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCottonPortBaleDetailNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.portwarehouse.ui.inventory.BaleDetailActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCottonGinningScanLotDetailsActivityNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.ginningwarehouse.ui.ui.scan.ScanLotDetailsActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCottonPortIncomingLOTsNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.portwarehouse.ui.incoming.PortIncomingMtnActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCottonPortDispatchNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.portwarehouse.ui.dispatch.DispatchSelectionActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCottonPortIncomingMtnSelectionNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.portwarehouse.ui.incoming.offline.PortIncomingMtnSelectionActivityOffline"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNicaraguaOffloadingNavigation : DynamicFeature<Intent> {
    private const val DPIVC =
        "com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadingActivity"
    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

// Lot Quality Nigeria Cocoa

object VegaNigeriaCocoaLotQualityNavigation : DynamicFeature<Intent> {

    private const val DPEC =
        "com.olam.warehouse.vegax.lotqualitynigeria.ui.VegaCocoaLotQualityActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaDispatchNicaraguaNavigation : DynamicFeature<Intent> {


    private const val DPEC =
        "com.olam.warehouse.vegax.localsalescameroon.ui.VegaCameroonSalesActivity"
    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaDispatchNigeriaLocalSalesNavigation : DynamicFeature<Intent> {


    private const val DPEC =
        "com.olam.warehouse.vegax.localsalesnigeria.ui.VegaNigeriaSalesActivity"
    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNicaraguaCoffeeProcessingFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC =
        "com.olam.warehouse.vegax.processingindiacoffee.ui.VegaProcessingIndiaCoffeeActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaNicaraguaNavigationExportSales : DynamicFeature<Intent> {

    const val FEATURE =
        "com.olam.warehouse.vegax.exportsalescameroon.ui.VegaCameroonExportSalesActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaNicaraguaNavigationContainerMangaement : DynamicFeature<Intent> {

    const val FEATURE =
        "com.olam.warehouse.vegax.containermanagement.ui.VegaCameroonContainerManagementActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaNicaraguaTicketNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}
object VegaNicaraguaMtnrReprintNavigation : DynamicFeature<Intent> {

    const val FEATURE = "com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntActivity"

    override val dynamicStart: Intent?
        get() = FEATURE.loadIntentOrNull()
}

object VegaNigeriaCocoaBagIssueNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.bagissuenigeriacocoa.ui.VegaNigeriaCocoaBagIssueActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaNigeriaWeighmentFeatureNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.nigeriaweighment.ui.VegaNigeriaReceivingActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGhanaCocoaHistoryTransactionsNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.VegaHistoryTransactionsGhanaCocoaActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}
object VegaDummyQualityNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.dummyquality.ui.VegaCommonDummyQualityActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaGhanaCashHistoryTransactionsNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaGhanaCashewTransHistoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaCommonSplitLotNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.warehouse.vegax.splitlot.ui.VegaCommonSplitLotActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaCommonQualityOfAnyLot : DynamicFeature<Intent> {
    private const val DPIVC = "com.olam.warehouse.vegax.qualityofanylot.ui.VegaAnyLotQualityActivity"
    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

object VegaCommonReportsNavigation : DynamicFeature<Intent> {
    private const val DPIVC = "com.olam.warehouse.vegax.reports.ui.ReportsActivity"
    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}


object VegaCameroonCocoaQualityApprovalNavigation : DynamicFeature<Intent> {

    private const val DPEC = "com.olam.warehouse.vegax.qualityapprovecameroon.ui.VegaQualityApproveCameroonActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaCoffeeStockNavigation : DynamicFeature<Intent>{
    private const val DPEC = "com.olam.warehouse.vegax.stockrecon.ui.bagaudit.VegaStockReconActivity"

    override val dynamicStart: Intent?
    get() = DPEC.loadIntentOrNull()
}

object VegaNotificationConfig : DynamicFeature<Intent>{
    private const val DPEC = "com.olam.warehouse.vegax.notificationconfig.ui.VegaNotificationConfigActivity"

    override val dynamicStart: Intent?
        get() = DPEC.loadIntentOrNull()
}

object VegaTransactionsHistoryNavigation : DynamicFeature<Intent> {

    private const val DPIVC = "com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHistoryActivity"

    override val dynamicStart: Intent?
        get() = DPIVC.loadIntentOrNull()
}

