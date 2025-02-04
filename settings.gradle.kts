pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://jitpack.io")
        google()
    }

    plugins {
        id(GradlePluginId.DETEKT) version GradlePluginVersion.DETEKT
        id(GradlePluginId.KTLINT_GRADLE) version GradlePluginVersion.KTLINT_GRADLE
        id(GradlePluginId.GRADLE_VERSION_PLUGIN) version GradlePluginVersion.GRADLE_VERSION_PLUGIN
        id(GradlePluginId.KOTLIN_JVM) version GradlePluginVersion.KOTLIN
        id(GradlePluginId.KOTLIN_ANDROID) version GradlePluginVersion.KOTLIN
        id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS) version GradlePluginVersion.KOTLIN
        id(GradlePluginId.ANDROID_APPLICATION) version GradlePluginVersion.ANDROID_GRADLE
        id(GradlePluginId.ANDROID_LIBRARY) version GradlePluginVersion.ANDROID_GRADLE
        id(GradlePluginId.ANDROID_DYNAMIC_FEATURE) version GradlePluginVersion.ANDROID_GRADLE
        id(GradlePluginId.SAFE_ARGS) version GradlePluginVersion.SAFE_ARGS
        id(GradlePluginId.GOOGLE_SERVICE) version GradlePluginVersion.GOOGLE_SERVICE
        id(GradlePluginId.FABRIC) version GradlePluginVersion.FABRIC
        //id(GradlePluginId.DYNATRACE) version GradlePluginVersion.DYNATRACE
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                GradlePluginId.ANDROID_APPLICATION,
                GradlePluginId.ANDROID_LIBRARY,
                GradlePluginId.GOOGLE_SERVICE -> useModule(GradleOldWayPlugins.GOOGLE_SERVICE)
                GradlePluginId.FABRIC -> useModule(GradleOldWayPlugins.FABRIC)
                //GradlePluginId.DYNATRACE -> useModule(GradleOldWayPlugins.DYNATRACE)
                GradlePluginId.ANDROID_DYNAMIC_FEATURE -> useModule(GradleOldWayPlugins.ANDROID_GRADLE)
                GradlePluginId.SAFE_ARGS -> useModule(GradleOldWayPlugins.SAFE_ARGS)
            }
        }
    }
}
include(
    ModuleDependency.APP,
    ModuleDependency.LOGIN,
    ModuleDependency.MASTER,
    ModuleDependency.PRESENTATION,
    ModuleDependency.NAVIGATION,
    ModuleDependency.RECEIVING,
    ModuleDependency.QUALITY,
    ModuleDependency.APPROVE,
    ModuleDependency.OFFLOADING,
    ModuleDependency.DISPATCH,
    ModuleDependency.PROCESSING,
    ModuleDependency.GATEENTRY,
    ModuleDependency.INVENTORY,
    ModuleDependency.ODQUALITY,
    ModuleDependency.ODRECEIVING,
    ModuleDependency.VEGACOCOAMTNT,
    ModuleDependency.VEGACOCOASALES,
    ModuleDependency.VEGACOCOAINVENTORY,
    ModuleDependency.VEGACOCOASWEEPING,
    ModuleDependency.VEGACOCOATHIRDPARTY,
    ModuleDependency.VEGACOCOAOFFLOADING,
    ModuleDependency.VEGACOCOAPROCESSING,
    ModuleDependency.ECUADOROFFLOADING,
    ModuleDependency.ECUADORQUALITY,
    ModuleDependency.ECUADORGRN,
    ModuleDependency.ECUADORDISPATCH,
    ModuleDependency.ECUADORINVENTORY,
    ModuleDependency.COFFEEPROCESSING,
    ModuleDependency.COFFEEINVENTORY,
    ModuleDependency.COFFEEQUALITY,
    ModuleDependency.COFFEEPPQ,
    ModuleDependency.COFFEESALES,
    ModuleDependency.COFFEEGATEENTRY,
    ModuleDependency.COFFEEEXPORTSALES,
    ModuleDependency.COFFEEMTNT,
    ModuleDependency.COFFEETHIRDPARTY,
    ModuleDependency.COFFEEOFFLOADING,
    ModuleDependency.COFFEEWEIGHMENT,
    ModuleDependency.COFFEEGRN,
    ModuleDependency.COFFEEPILE,
    //ModuleDependency.WHMOVEMENTFGTOFG,
    //ModuleDependency.WHMOVEMENTPRODUCTIONTOFG,
    //ModuleDependency.ACKKNOWLEDGEMENT,
    //ModuleDependency.DELIVERYPICKING,
    ModuleDependency.NICARAGUAGRN,
    ModuleDependency.NICARAGUAINVOICE,
    ModuleDependency.NICARAGUARECONCIL,
    ModuleDependency.NICARAGUAADVANCE,
    ModuleDependency.NICARAGUAFORWARDPO,
    ModuleDependency.NICARAGUAMTNT,
    ModuleDependency.NICARAGUAINVENTORY,
    ModuleDependency.GATEENTRYNIGERIA,
    ModuleDependency.INVENTORYNIGERIA,
    ModuleDependency.GRNNIGERIA,
    ModuleDependency.OFFLOADINGNIGERIA,
    ModuleDependency.QUALITYNIGERIA,
    ModuleDependency.DISPATCHNIGERIA,
    ModuleDependency.SECRETIDNIGERIA,
    ModuleDependency.PROCESSINGNIGERIA,
    ModuleDependency.QUALITYAPPROVENIGERIA,
    ModuleDependency.LOCALSALESNIGERIA,
    ModuleDependency.EXPORTSALESNIGERIA,
    ModuleDependency.CONTAINERMANAGEMENTNIGERIA,
    ModuleDependency.BCAPPROVENIGERIA,
    ModuleDependency.GATEENTRYAPPROVALNIGERIA,
    ModuleDependency.NIGERIALOTQUALITY,
    ModuleDependency.NIGERIAPILEMANAGEMENT,
    ModuleDependency.GRNNIGERIASESAME,
    ModuleDependency.INVENTORYNIGERIASESAME,
    ModuleDependency.QUALITYNIGERIASESAME,
    ModuleDependency.MTNTNIGERIASESAME,
    ModuleDependency.PROCESSINGSESAME,
    ModuleDependency.OFFLOADINGSESAME,
    ModuleDependency.PPQSESAME,
    ModuleDependency.PILESESAME,
//    ModuleDependency.GHANARECEIVING,
//    ModuleDependency.GHANAQUALITY,
    ModuleDependency.CAMEROONOFFLOADING,
    ModuleDependency.CAMEROONQUALITY,
    ModuleDependency.CAMEROONGRN,
    ModuleDependency.CAMEROONINVENTORY,
    ModuleDependency.CAMEROONPROCESSING,
    ModuleDependency.CAMEROONMTNT,
    ModuleDependency.CAMEROONPPQ,
    ModuleDependency.CAMEROONSALES,
    ModuleDependency.CAMEROONQUALITYAPPROVE,
    ModuleDependency.CAMEROONGATEENTRY,
    ModuleDependency.CAMEROONCONTAINERMANAGEMENT,
    ModuleDependency.CAMEROONEXPORTSALES,
    ModuleDependency.CAMEROONBCAPPROVE,
    ModuleDependency.SECRETID,
    ModuleDependency.GHANAGATEENTRY,
    ModuleDependency.GHANARECEIVINGCASHEW,
    ModuleDependency.GHANAAPPROVE,
    ModuleDependency.QUALITYGHANA,
    ModuleDependency.GHANAOFFLOADING,
    ModuleDependency.PROCESSINGGHANA,
    ModuleDependency.MTNTGHANA,
    ModuleDependency.INVENTORYGHANA,
    ModuleDependency.GINNINGWAREHOUSE,
    ModuleDependency.PORTWAREHOUSE,
    ModuleDependency.INDOOFFLOADING,
    ModuleDependency.INDOQUALITY,
    ModuleDependency.INDOGRN,
    ModuleDependency.INDOPROCESSING,
    ModuleDependency.INDOMTNT,
    ModuleDependency.INDOEXPORTSALES,
    ModuleDependency.INDOPPQ,
    ModuleDependency.INDOINVENTORY,
    ModuleDependency.ECUADORBCAPPROVE,
    ModuleDependency.INAPP,
    ModuleDependency.INDIACOFFEEWEIGHMENT,
    ModuleDependency.INDIACOFFEEOFFLOADING,
    ModuleDependency.INDIACOFFEEQUALITY,
    ModuleDependency.INDIACOFFEEGRN,
    ModuleDependency.INDIACOFFEEPROCESSING,
    ModuleDependency.INDIACOFFEEPPQ,
    ModuleDependency.INDIACOFFEEINVENTORY,
    ModuleDependency.INDIACOFFEEBAGISSUE,
    ModuleDependency.INDIACOFFEEPILE,
    ModuleDependency.INDIACOFFEEDISPATCH,
    ModuleDependency.INVENTORYGHANACOCOA,
    ModuleDependency.INDIACOFFEEQUALITYAPPROVE,
    ModuleDependency.OFFLOADINGGRNGHANACOCOA,
    ModuleDependency.MTNTGHANACOCOA,
    ModuleDependency.GATEENTRYGHANACOCOA,
    ModuleDependency.CREATEMAPAR
//    ModuleDependency.MTNTGHANACASHEW
)
/*ModuleDependency.DOQUALITY,
ModuleDependency.DORECEIVING,*/
rootProject.buildFileName = "build.gradle.kts"
