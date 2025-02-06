pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://jitpack.io")
        maven ( url = "https://maven.google.com/")
        maven(url = uri("https://plugins.gradle.org/m2/"))
    }

    val detekt: String by settings
    val detektv: String by settings
    val ktlintgradle: String by settings
    val ktlintgradlev: String by settings
    val gradleversionplugin: String by settings
    val gradleversionpluginv: String by settings
    val kotlinjvm: String by settings
    val kotlin: String by settings
    val kotlinandroid: String by settings
    val androidapplication: String by settings
    val androidgradlev: String by settings
    val androidlibrary: String by settings
    val androiddynamicfeature: String by settings
    val safeargs: String by settings
    val safeargsv: String by settings
    val googleservice: String by settings
    val googleservicev: String by settings
//    val parcelize: String by settings
//    val parcelizev: String by settings
//    val kotlinandroidextensions: String by settings
//    val fabric: String by settings
//    val fabricv: String by settings
//    val dynatrace: String by settings
//    val dynatracev: String by settings
    val googleserviceold: String by settings
    val androidgradle: String by settings
    val safeargsold: String by settings

    plugins {
        id(detekt) version detektv
        id(ktlintgradle) version ktlintgradlev
        id(gradleversionplugin) version gradleversionpluginv
        id(kotlinjvm) version kotlin
        id(kotlinandroid) version kotlin
//        id(parcelize) version parcelizev
        //id(kotlinandroidextensions) version kotlin
        id(androidapplication) version androidgradlev
        id(androidlibrary) version androidgradlev
        id(androiddynamicfeature) version androidgradlev
        id(safeargs) version safeargsv
        id(googleservice) version googleservicev
        id("com.android.dynamic-feature") version "8.5.2"
        id("org.jetbrains.kotlin.android") version "1.9.23"
        //id(fabric) version fabricv
        //id(dynatrace) version dynatracev
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                androidapplication,
                androidlibrary,
               // googleservice -> useModule(googleserviceold)
                //fabric -> useModule(fabricold)
                //dynatrace -> useModule(dynatraceold)
                androiddynamicfeature -> useModule(androidgradle)
                safeargs -> useModule(safeargsold)
            }
        }
    }


}

//DB Encryption
val artifactory_user: String by settings
val artifactory_password: String by settings
val artifactory_context_url: String by settings
val artifactory_dev_repo_key: String by settings


@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://jitpack.io")
        maven(url = uri("https://plugins.gradle.org/m2/"))
        maven(url = uri("https://maven.google.com"))
        maven {
            url = uri("$artifactory_context_url$artifactory_dev_repo_key")
            credentials {
                username = artifactory_user
                password = artifactory_password
            }   
        }
    }
}



include(
    ":app",
    ":navigation",
    ":common:login",
    ":common:master",
    ":common:presentation",
//    ":InAppLib",
      ":feature_vega:receiving",
      ":feature_vega:quality",
      ":feature_vega:approve",
      ":feature_vega:offloading",
      ":feature_vega:dispatch",
      ":feature_vega:processing",
      ":feature_vega:gateentry",
      ":feature_vega:inventory",
      ":feature_do:odquality",
      ":feature_do:odreceiving",
      ":feature_vega_cocoa:mtntcocoa",
      ":feature_vega_cocoa:salescocoa",
      ":feature_vega_cocoa:inventorycocoa",
      ":feature_vega_cocoa:sweepingcocoa",
      ":feature_vega_cocoa:thirdpartysalescocoa",
      ":feature_vega_cocoa:offloadingcocoa",
      ":feature_vega_cocoa:processingcocoa",
      ":feature_vega_ecuador:offloadingecuador",
      ":feature_vega_ecuador:qualityecuador",
      ":feature_vega_ecuador:grnecuador",
      ":feature_vega_ecuador:dispatchecuador",
      ":feature_vega_ecuador:inventoryecuador",
      ":feature_vega_coffee:processingcoffee",
      ":feature_vega_coffee:inventorycoffee",
      ":feature_vega_coffee:qualitycoffee",
      ":feature_vega_coffee:ppqcoffee",
      ":feature_vega_coffee:salescoffee",
      ":feature_vega_coffee:exportsalescoffee",
      ":feature_vega_coffee:mtntcoffee",
      ":feature_vega_coffee:thirdpartysalescoffee",
      ":feature_vega_coffee:offloadingcoffee",
      ":feature_vega_coffee:weighmentcoffee",
      ":feature_vega_coffee:grncoffee",
      ":feature_vega_coffee:pilecoffee",
      ":feature_vega_coffee:gateentrycoffee",
      ":feature_vega_nicaragua:grnnicaragua",
      ":feature_vega_nicaragua:invoicenicaragua",
      ":feature_vega_nicaragua:reconcilnicaragua",
      ":feature_vega_nicaragua:advancenicaragua",
      ":feature_vega_nicaragua:forwardponicaragua",
      ":feature_vega_nicaragua:mtntnicaragua",
      ":feature_vega_nicaragua:inventorynicaragua",
      ":feature_vega_nigeria:gateentrynigeria",
      ":feature_vega_nigeria:inventorynigeria",
      ":feature_vega_nigeria:grnnigeria",
      ":feature_vega_nigeria:offloadingnigeria",
      ":feature_vega_nigeria:qualitynigeria",
      ":feature_vega_nigeria:dispatchnigeria",
      ":feature_vega_nigeria:secretidnigeria",
      ":feature_vega_nigeria:processingnigeria",
      ":feature_vega_nigeria:qualityapprovenigeria",
      ":feature_vega_nigeria:localsalesnigeria",
      ":feature_vega_nigeria:exportsalesnigeria",
      ":feature_vega_nigeria:containermanagementnigeria",
      ":feature_vega_nigeria:bcapprovenigeria",
      ":feature_vega_nigeria:gateentryapprovalnigeria",
      ":feature_vega_nigeria:lotqualitynigeria",
      ":feature_vega_nigeria:pilemanagementnigeria",
      ":feature_vega_nigeria:nigeriaweighment",
      ":feature_vega_nigeria_sesame:grnsesame",
      ":feature_vega_nigeria_sesame:inventorysesame",
      ":feature_vega_nigeria_sesame:qualitysesame",
      ":feature_vega_nigeria_sesame:mtntsesame",
      ":feature_vega_nigeria_sesame:processingsesame",
      ":feature_vega_nigeria_sesame:offloadingsesame",
      ":feature_vega_nigeria_sesame:ppqsesame",
      ":feature_vega_nigeria_sesame:pilesesame",
      ":feature_vega_cameroon_cocoa:offloadingcameroon",
      ":feature_vega_cameroon_cocoa:qualitycameroon",
      ":feature_vega_cameroon_cocoa:grncameroon",
      ":feature_vega_cameroon_cocoa:inventorycameroon",
      ":feature_vega_cameroon_cocoa:processingcameroon",
      ":feature_vega_cameroon_cocoa:mtntcameroon",
      ":feature_vega_cameroon_cocoa:ppqcameroon",
      ":feature_vega_cameroon_cocoa:localsalescameroon",
      ":feature_vega_cameroon_cocoa:qualityapprovecameroon",
      ":feature_vega_cameroon_cocoa:gateentrycameroon",
      ":feature_vega_cameroon_cocoa:containermanagement",
      ":feature_vega_cameroon_cocoa:exportsalescameroon",
      ":feature_vega_cameroon_cocoa:bcapprovecameroon",
      ":feature_vega_cameroon_cocoa:secretid",
      ":feature_vega_ghana:gateentryghana",
      ":feature_vega_ghana:receivingghanacash",
      ":feature_vega_ghana:approveghana",
      ":feature_vega_ghana:ghanaquality",
      ":feature_vega_ghana:offloadingghana",
      ":feature_vega_ghana:processingghana",
      ":feature_vega_ghana:mtntghana",
      ":feature_vega_ghana:inventoryghana",
      ":feature_vega_cotton:ginningwarehouse",
      ":feature_vega_cotton:portwarehouse",
      ":feature_vega_indo_coffee:offloadingindo",
      ":feature_vega_indo_coffee:qualityindo",
      ":feature_vega_indo_coffee:grnindo",
      ":feature_vega_indo_coffee:processingindo",
      ":feature_vega_indo_coffee:mtntindo",
      ":feature_vega_indo_coffee:exportsalesindo",
      ":feature_vega_indo_coffee:ppqindo",
      ":feature_vega_indo_coffee:inventoryindo",
      ":feature_vega_ecuador:bcapproveecuador",
      ":feature_vega_india_coffee:weighment",
      ":feature_vega_india_coffee:offloadingindiacoffee",
      ":feature_vega_india_coffee:qualityindiacoffee",
      ":feature_vega_india_coffee:grnindiacoffee",
      ":feature_vega_india_coffee:processingindiacoffee",
      ":feature_vega_india_coffee:ppqindiacoffee",
      ":feature_vega_india_coffee:inventoryindiacoffee",
      ":feature_vega_india_coffee:bagissueindiacoffee",
      ":feature_vega_india_coffee:pileindiacoffee",
      ":feature_vega_india_coffee:dispatchindiacoffee",
      ":feature_vega_india_coffee:qualityapproveindiacoffee",
      ":feature_vega_ghana_cocoa:offloadinggrnghanacocoa",
      ":feature_vega_ghana_cocoa:mtntghanacocoa",
      ":feature_vega_ghana_cocoa:gateentryghanacocoa",
      ":feature_vega_ghana_cocoa:inventoryghanacocoa",
      ":feature_vega_ghana_cocoa:historytransactionsghanacocoa",
//      ":feature_vega_ar:createmapar",
      ":feature_vega_nigeria:bagissuenigeriacocoa",
      ":feature_vega_ecuador:processingecuador",
      ":feature_vega_ecuador:pileecuador",
      ":feature_vega_ecuador:exportsalesecuador",
      ":feature_vega_ecuador:localsalesecuador",
      ":feature_vega_common:dummyquality",
      ":feature_vega_common:splitlot",
      ":feature_vega_ghana:transhistoryghanacashew",
      ":feature_vega_common:secretidcommon",
      ":feature_vega_common:qualityofanylot",
      ":feature_vega_common:reports",
      ":feature_vega_common:stockrecon",
      ":feature_vega_common:notificationconfig",
      ":feature_vega_common:shipment",
      ":feature_vega_common:transactionhistory"
)
/*ModuleDependency.DOQUALITY,
ModuleDependency.DORECEIVING,*/
rootProject.buildFileName = "build.gradle.kts"
