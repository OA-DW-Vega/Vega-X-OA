import kotlin.reflect.full.memberProperties

/**
 * Created by SangiliPandian C on 06-11-2019.
 */

private const val FEATURE_PREFIX = ":feature_vega"
private const val FEATURE_NIGERIA_SESAME = ":feature_vega_nigeria_sesame"
private const val FEATURE_PORT_WAREHOUSE = ":feature_vega_cotton"
private const val FEATURE_COMMON = ":feature_vega_common"
private const val FEATURE_COMMON_DUMMY_QUALITY = ":feature_vega_common_dummy_quality"

// "Module" means "project" in terminology of Gradle API. To be specific each "Android module" is a Gradle "subproject"
@Suppress("unused")
object ModuleDependency {
    // All consts are accessed via reflection
    const val APP = ":app"
    const val NAVIGATION = ":navigation"
    const val LOGIN = ":common:login"
    const val MASTER = ":common:master"
    const val PRESENTATION = ":common:presentation"



    //cotton
    const val GINNINGWAREHOUSE = ":feature_vega_cotton:ginningwarehouse"
    const val PORTWAREHOUSE = ":feature_vega_cotton:portwarehouse"

        //Nigeria sesame
    const val GRNNIGERIASESAME = ":feature_vega_nigeria_sesame:grnsesame"
    const val INVENTORYNIGERIASESAME = ":feature_vega_nigeria_sesame:inventorysesame"
    const val QUALITYNIGERIASESAME = ":feature_vega_nigeria_sesame:qualitysesame"
    const val MTNTNIGERIASESAME = ":feature_vega_nigeria_sesame:mtntsesame"
    const val PROCESSINGSESAME = ":feature_vega_nigeria_sesame:processingsesame"
    const val OFFLOADINGSESAME = ":feature_vega_nigeria_sesame:offloadingsesame"
    const val PPQSESAME = ":feature_vega_nigeria_sesame:ppqsesame"
    const val PILESESAME = ":feature_vega_nigeria_sesame:pilesesame"

    //common
    const val DUMMYQUALITY = ":feature_vega_common:dummyquality"
    const val QUALITYOFANYLOT = ":feature_vega_common:qualityofanylot"
    const val REPORTS = ":feature_vega_common:reports"
    const val SPLITLOT = ":feature_vega_common:splitlot"
    const val COMMON_SECRET_ID = ":feature_vega_common:secretidcommon"
    const val STOCK_RECON = ":feature_vega_common:stockrecon"
    const val NOTIFY_CONFIG = ":feature_vega_common:notificationconfig"
    const val COMMON_SHIPMENT = ":feature_vega_common:shipment"
    const val COMMON_TRANSACTION_HISTORY = ":feature_vega_common:transactionhistory"


    // False positive" function can be private"
    // See: https://youtrack.jetbrains.com/issue/KT-33610
    fun getAllModules() = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()


    fun getNigeriaSesameDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_NIGERIA_SESAME) }
        .toSet()


    fun getPortWarehouseDynamicFeatureModules() = getAllModules()
            .filter { it.startsWith(FEATURE_PORT_WAREHOUSE)}
            .toSet()


}
