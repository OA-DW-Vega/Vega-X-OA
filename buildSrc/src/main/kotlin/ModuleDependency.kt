import kotlin.reflect.full.memberProperties

/**
 * Created by SangiliPandian C on 06-11-2019.
 */

private const val FEATURE_PREFIX = ":feature_"
private const val FEATURE_OD = ":feature_do:"
private const val FEATURE_NICARAGUA = ":feature_vega_nicaragua"
private const val FEATURE_COCOA = ":feature_vega_cocoa"
private const val FEATURE_COFFEE = ":feature_vega_coffee"
private const val FEATURE_ECUDOR = ":feature_vega_ecuador"
private const val FEATURE_CASHIEW = ":feature_vega:"
private const val FEATURE_NIGERIA_SESAME = ":feature_vega_nigeria_sesame"
private const val FEATURE_NIGERIA_COCOA = ":feature_vega_nigeria"
private const val FEATURE_CAMEROON_COCOA = ":feature_vega_cameroon_cocoa"
private const val FEATURE_GHANA_CASHEW = ":feature_vega_ghana:"
private const val FEATURE_PORT_WAREHOUSE = ":feature_vega_cotton"
private const val FEATURE_INDO_COFFEE = ":feature_vega_indo_coffee"
private const val FEATURE_INDIA_COFFEE = ":feature_vega_india_coffee"
private const val FEATURE_GHANA_COCOA = ":feature_vega_ghana_cocoa"

// "Module" means "project" in terminology of Gradle API. To be specific each "Android module" is a Gradle "subproject"
@Suppress("unused")
object ModuleDependency {
    // All consts are accessed via reflection
    const val APP = ":app"
    const val NAVIGATION = ":navigation"
    const val LOGIN = ":common:login"
    const val MASTER = ":common:master"
    const val PRESENTATION = ":common:presentation"
    const val RECEIVING = ":feature_vega:receiving"
    const val QUALITY = ":feature_vega:quality"
    const val APPROVE = ":feature_vega:approve"
    const val OFFLOADING = ":feature_vega:offloading"
    const val DISPATCH = ":feature_vega:dispatch"
    const val PROCESSING = ":feature_vega:processing"
    const val GATEENTRY = ":feature_vega:gateentry"
    const val INVENTORY = ":feature_vega:inventory"
    const val ODQUALITY = ":feature_do:odquality"
    const val ODRECEIVING = ":feature_do:odreceiving"
    const val VEGACOCOAMTNT = ":feature_vega_cocoa:mtntcocoa"
    const val VEGACOCOASALES = ":feature_vega_cocoa:salescocoa"
    const val VEGACOCOAINVENTORY = ":feature_vega_cocoa:inventorycocoa"
    const val VEGACOCOASWEEPING = ":feature_vega_cocoa:sweepingcocoa"
    const val VEGACOCOATHIRDPARTY = ":feature_vega_cocoa:thirdpartysalescocoa"
    const val VEGACOCOAOFFLOADING = ":feature_vega_cocoa:offloadingcocoa"
    const val VEGACOCOAPROCESSING = ":feature_vega_cocoa:processingcocoa"
    const val ECUADOROFFLOADING = ":feature_vega_ecuador:offloadingecuador"
    const val ECUADORQUALITY = ":feature_vega_ecuador:qualityecuador"
    const val ECUADORGRN = ":feature_vega_ecuador:grnecuador"
    const val ECUADORDISPATCH = ":feature_vega_ecuador:dispatchecuador"
    const val ECUADORINVENTORY = ":feature_vega_ecuador:inventoryecuador"
    const val COFFEEPROCESSING = ":feature_vega_coffee:processingcoffee"
    const val COFFEEINVENTORY = ":feature_vega_coffee:inventorycoffee"
    const val COFFEEQUALITY = ":feature_vega_coffee:qualitycoffee"
    const val COFFEEPPQ = ":feature_vega_coffee:ppqcoffee"
    const val COFFEESALES = ":feature_vega_coffee:salescoffee"
    const val COFFEEEXPORTSALES = ":feature_vega_coffee:exportsalescoffee"
    const val COFFEEMTNT = ":feature_vega_coffee:mtntcoffee"
    const val COFFEETHIRDPARTY = ":feature_vega_coffee:thirdpartysalescoffee"
    const val COFFEEOFFLOADING = ":feature_vega_coffee:offloadingcoffee"
    const val COFFEEWEIGHMENT = ":feature_vega_coffee:weighmentcoffee"
    const val COFFEEGRN = ":feature_vega_coffee:grncoffee"
    const val COFFEEPILE = ":feature_vega_coffee:pilecoffee"
    const val COFFEEGATEENTRY = ":feature_vega_coffee:gateentrycoffee"
    const val NICARAGUAGRN = ":feature_vega_nicaragua:grnnicaragua"
    const val NICARAGUAINVOICE = ":feature_vega_nicaragua:invoicenicaragua"
    const val NICARAGUARECONCIL = ":feature_vega_nicaragua:reconcilnicaragua"
    const val NICARAGUAADVANCE = ":feature_vega_nicaragua:advancenicaragua"
    const val NICARAGUAFORWARDPO = ":feature_vega_nicaragua:forwardponicaragua"
    const val NICARAGUAMTNT = ":feature_vega_nicaragua:mtntnicaragua"
    const val NICARAGUAINVENTORY = ":feature_vega_nicaragua:inventorynicaragua"

    const val GATEENTRYNIGERIA = ":feature_vega_nigeria:gateentrynigeria"
    const val INVENTORYNIGERIA = ":feature_vega_nigeria:inventorynigeria"
    const val GRNNIGERIA = ":feature_vega_nigeria:grnnigeria"
    const val OFFLOADINGNIGERIA = ":feature_vega_nigeria:offloadingnigeria"
    const val QUALITYNIGERIA = ":feature_vega_nigeria:qualitynigeria"
    const val DISPATCHNIGERIA = ":feature_vega_nigeria:dispatchnigeria"
    const val SECRETIDNIGERIA = ":feature_vega_nigeria:secretidnigeria"
    const val PROCESSINGNIGERIA = ":feature_vega_nigeria:processingnigeria"
    const val QUALITYAPPROVENIGERIA = ":feature_vega_nigeria:qualityapprovenigeria"
    const val LOCALSALESNIGERIA = ":feature_vega_nigeria:localsalesnigeria"
    const val EXPORTSALESNIGERIA = ":feature_vega_nigeria:exportsalesnigeria"
    const val CONTAINERMANAGEMENTNIGERIA = ":feature_vega_nigeria:containermanagementnigeria"
    const val BCAPPROVENIGERIA = ":feature_vega_nigeria:bcapprovenigeria"
    const val GATEENTRYAPPROVALNIGERIA = ":feature_vega_nigeria:gateentryapprovalnigeria"
    const val NIGERIALOTQUALITY = ":feature_vega_nigeria:lotqualitynigeria"
    const val NIGERIAPILEMANAGEMENT = ":feature_vega_nigeria:pilemanagementnigeria"
    const val GRNNIGERIASESAME = ":feature_vega_nigeria_sesame:grnsesame"
    const val INVENTORYNIGERIASESAME = ":feature_vega_nigeria_sesame:inventorysesame"
    const val QUALITYNIGERIASESAME = ":feature_vega_nigeria_sesame:qualitysesame"
    const val MTNTNIGERIASESAME = ":feature_vega_nigeria_sesame:mtntsesame"
    const val PROCESSINGSESAME = ":feature_vega_nigeria_sesame:processingsesame"
    const val OFFLOADINGSESAME = ":feature_vega_nigeria_sesame:offloadingsesame"
    const val PPQSESAME = ":feature_vega_nigeria_sesame:ppqsesame"
    const val PILESESAME = ":feature_vega_nigeria_sesame:pilesesame"
    const val GHANARECEIVING = ":feature_vega_ghana_cashew:receivingghana"
//    const val GHANAQUALITY = ":feature_vega_ghana_cashew:qualityghana"
    const val CAMEROONOFFLOADING = ":feature_vega_cameroon_cocoa:offloadingcameroon"
    const val CAMEROONQUALITY = ":feature_vega_cameroon_cocoa:qualitycameroon"
    const val CAMEROONGRN = ":feature_vega_cameroon_cocoa:grncameroon"
    const val CAMEROONINVENTORY = ":feature_vega_cameroon_cocoa:inventorycameroon"
    const val CAMEROONPROCESSING = ":feature_vega_cameroon_cocoa:processingcameroon"
    const val CAMEROONMTNT = ":feature_vega_cameroon_cocoa:mtntcameroon"
    const val CAMEROONPPQ = ":feature_vega_cameroon_cocoa:ppqcameroon"
    const val CAMEROONSALES = ":feature_vega_cameroon_cocoa:localsalescameroon"
    const val CAMEROONQUALITYAPPROVE = ":feature_vega_cameroon_cocoa:qualityapprovecameroon"
    const val CAMEROONGATEENTRY = ":feature_vega_cameroon_cocoa:gateentrycameroon"
    const val CAMEROONCONTAINERMANAGEMENT = ":feature_vega_cameroon_cocoa:containermanagement"
    const val CAMEROONEXPORTSALES = ":feature_vega_cameroon_cocoa:exportsalescameroon"
    const val CAMEROONBCAPPROVE = ":feature_vega_cameroon_cocoa:bcapprovecameroon"
    const val SECRETID = ":feature_vega_cameroon_cocoa:secretid"
    const val GHANAGATEENTRY = ":feature_vega_ghana:gateentryghana"
    const val GHANARECEIVINGCASHEW = ":feature_vega_ghana:receivingghanacash"
    const val GHANAAPPROVE = ":feature_vega_ghana:approveghana"
    const val QUALITYGHANA = ":feature_vega_ghana:ghanaquality"
    const val GHANAOFFLOADING = ":feature_vega_ghana:offloadingghana"
    const val PROCESSINGGHANA = ":feature_vega_ghana:processingghana"
    const val MTNTGHANA = ":feature_vega_ghana:mtntghana"

    //    const val MTNTGHANACASHEW = ":feature_vega_ghana_cashew:mtntghanacashew"
    const val INVENTORYGHANA = ":feature_vega_ghana:inventoryghana"
    const val GINNINGWAREHOUSE = ":feature_vega_cotton:ginningwarehouse"
    const val PORTWAREHOUSE = ":feature_vega_cotton:portwarehouse"
    const val INDOOFFLOADING = ":feature_vega_indo_coffee:offloadingindo"
    const val INDOQUALITY = ":feature_vega_indo_coffee:qualityindo"
    const val INDOGRN = ":feature_vega_indo_coffee:grnindo"
    const val INDOPROCESSING = ":feature_vega_indo_coffee:processingindo"
    const val INDOMTNT = ":feature_vega_indo_coffee:mtntindo"
    const val INDOEXPORTSALES = ":feature_vega_indo_coffee:exportsalesindo"
    const val INDOPPQ = ":feature_vega_indo_coffee:ppqindo"
    const val INDOINVENTORY = ":feature_vega_indo_coffee:inventoryindo"
    const val INAPP = ":InAppLib"
    const val ECUADORBCAPPROVE = ":feature_vega_ecuador:bcapproveecuador"
    const val INDIACOFFEEWEIGHMENT = ":feature_vega_india_coffee:weighment"
    const val INDIACOFFEEOFFLOADING = ":feature_vega_india_coffee:offloadingindiacoffee"
    const val INDIACOFFEEQUALITY = ":feature_vega_india_coffee:qualityindiacoffee"
    const val INDIACOFFEEGRN = ":feature_vega_india_coffee:grnindiacoffee"
    const val INDIACOFFEEPROCESSING = ":feature_vega_india_coffee:processingindiacoffee"
    const val INDIACOFFEEPPQ = ":feature_vega_india_coffee:ppqindiacoffee"
    const val INDIACOFFEEINVENTORY = ":feature_vega_india_coffee:inventoryindiacoffee"
    const val INDIACOFFEEBAGISSUE = ":feature_vega_india_coffee:bagissueindiacoffee"
    const val INDIACOFFEEPILE = ":feature_vega_india_coffee:pileindiacoffee"
    const val INDIACOFFEEDISPATCH = ":feature_vega_india_coffee:dispatchindiacoffee"
    const val INDIACOFFEEQUALITYAPPROVE = ":feature_vega_india_coffee:qualityapproveindiacoffee"
    const val OFFLOADINGGRNGHANACOCOA = ":feature_vega_ghana_cocoa:offloadinggrnghanacocoa"
    const val MTNTGHANACOCOA = ":feature_vega_ghana_cocoa:mtntghanacocoa"
    const val GATEENTRYGHANACOCOA = ":feature_vega_ghana_cocoa:gateentryghanacocoa"
    const val INVENTORYGHANACOCOA = ":feature_vega_ghana_cocoa:inventoryghanacocoa"
    const val CREATEMAPAR = ":feature_vega_ar:createmapar"


    // False positive" function can be private"
    // See: https://youtrack.jetbrains.com/issue/KT-33610
    fun getAllModules() = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()

    fun getDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_PREFIX) }
        .toSet()

    fun getNicaraguaDynamicFeatureModules() = getAllModules()
        .filter {
            it.startsWith(FEATURE_NICARAGUA) || it.startsWith(PILESESAME) || it.startsWith(
                COFFEEQUALITY
            )
                    || it.startsWith(COFFEEOFFLOADING) || it.startsWith(CAMEROONSALES) || it.startsWith(
                INDIACOFFEEPROCESSING
            )
                    || it.startsWith(CAMEROONEXPORTSALES) || it.startsWith(
                CAMEROONCONTAINERMANAGEMENT
            )
                    || it.startsWith(COFFEETHIRDPARTY) || it.startsWith(PPQSESAME) || it.startsWith(
                INDIACOFFEEINVENTORY
            )
        }.toSet()


    fun getODDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_OD) }
        .toSet()

    fun getCocoaDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_COCOA) }
        .toSet()

    fun getIndiaCoffeeDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_INDIA_COFFEE) }
        .toSet()

    fun getGhanaCocoaDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_GHANA_COCOA) }
        .toSet()

    fun getCoffeeDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_COFFEE) }
        .toSet()

    fun getCashewDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_CASHIEW) }
        .toSet()

    fun getEcudorDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_ECUDOR) }
        .toSet()

    fun getNigeriaSesameDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_NIGERIA_SESAME) }
        .toSet()

    fun getNigeriaCocoaDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_NIGERIA_COCOA) }
        .toSet()

    fun getCameroonDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_CAMEROON_COCOA) }
        .toSet()

    fun getSesameAndCameroonDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_CAMEROON_COCOA) || it.startsWith(FEATURE_NIGERIA_SESAME) }
        .toSet()

    fun getGhanaCashewDynamicFeatureModules() = getAllModules()
            .filter {
                it.startsWith(FEATURE_GHANA_CASHEW) || it.startsWith(FEATURE_OD)/* || it.startsWith(
                        FEATURE_CAMEROON_COCOA
                )*/
            }
            .toSet()

    fun getPortWarehouseDynamicFeatureModules() = getAllModules()
            .filter { it.startsWith(FEATURE_PORT_WAREHOUSE) }
            .toSet()

    fun getIndoCoffeeDynamicFeatureModules() = getAllModules()
            .filter { it.startsWith(FEATURE_INDO_COFFEE) }
            .toSet()

    fun getIndoCoffeeAndODDynamicFeatureModules() = getAllModules()
            .filter { it.startsWith(FEATURE_INDO_COFFEE) || it.startsWith(FEATURE_OD) }
            .toSet()
}
