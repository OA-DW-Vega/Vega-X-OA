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
    val googleserviceold: String by settings
    val androidgradle: String by settings
    val safeargsold: String by settings

    plugins {
        id(detekt) version detektv
        id(ktlintgradle) version ktlintgradlev
        id(gradleversionplugin) version gradleversionpluginv
        id(kotlinjvm) version kotlin
        id(kotlinandroid) version kotlin
        id(androidapplication) version androidgradlev
        id(androidlibrary) version androidgradlev
        id(androiddynamicfeature) version androidgradlev
        id(safeargs) version safeargsv
        id(googleservice) version googleservicev
        id("com.android.dynamic-feature") version "8.5.2"
        id("org.jetbrains.kotlin.android") version "1.9.23"

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

      ":feature_vega_nigeria_sesame:grnsesame",
      ":feature_vega_nigeria_sesame:inventorysesame",
      ":feature_vega_nigeria_sesame:qualitysesame",
      ":feature_vega_nigeria_sesame:mtntsesame",
      ":feature_vega_nigeria_sesame:processingsesame",
      ":feature_vega_nigeria_sesame:offloadingsesame",
      ":feature_vega_nigeria_sesame:ppqsesame",
      ":feature_vega_nigeria_sesame:pilesesame",

      ":feature_vega_cotton:ginningwarehouse",
      ":feature_vega_cotton:portwarehouse",

      ":feature_vega_common:dummyquality",
      ":feature_vega_common:splitlot",
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
