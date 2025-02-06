import com.android.build.gradle.internal.dsl.BaseFlavor
import com.android.build.gradle.internal.dsl.DefaultConfig
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.util.*

plugins {
    id(GradlePluginId.GOOGLE_SERVICE)
    id(GradlePluginId.CRASHLYTICS)
    id(GradlePluginId.ANDROID_APPLICATION)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.PARCELIZE)
    //id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS)
    //id(GradlePluginId.FABRIC)
    id(GradlePluginId.SONAR)
    id(GradlePluginId.KTLINT_GRADLE)
    id(GradlePluginId.SAFE_ARGS)

}
var dymic = "\"OFI\""
android {
    signingConfigs {
        create("release_keystore") {
            val properties = Properties().apply {
                load(rootProject.file("signing.properties").reader())
            }
            storeFile = rootProject.file(properties.getProperty("storeFilePath"))
            storePassword = properties.getProperty("storePassword")
            keyPassword = properties.getProperty("keyPassword")
            keyAlias = properties.getProperty("keyAlias")
        }
        create("debug_keystore") {
            val properties = Properties().apply {
                load(rootProject.file("signing.properties").reader())
            }
            storeFile = rootProject.file(properties.getProperty("storeFilePath"))
            storePassword = properties.getProperty("storePassword")
            keyPassword = properties.getProperty("keyPassword")
            keyAlias = properties.getProperty("keyAlias")
        }
    }
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        applicationId = AndroidConfig.ID
        resValue("string", "app_name", "Vega-X")
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
        buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME
        multiDexEnabled = true
        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }


    //############ Each feature module that is included in settings.gradle.kts is added here as dynamic feature
    //setDynamicFeatures(ModuleDependency.getDynamicFeatureModules().toMutableSet())
    //setDynamicFeatures(ModuleDependency.getDynamicFeatureModulesForGHNGCOTT().toMutableSet())

    //############ This is for Only Development

    //IVC Cotton Port & Ginning Warehouse
    //setDynamicFeatures(getCottonDynamicFeatureModules())

    //TOGO Cotton Port & Ginning Warehouse
    //setDynamicFeatures(getTogoCottonDynamicFeatureModules())

    //OD Features
    //setDynamicFeatures(getODDynamicFeatureModules())

    //Nicaragua Features
    setDynamicFeatures(getNicaraguaDynamicFeatureModules())

    //IVC Cocoa Features
    //setDynamicFeatures(getCocoaDynamicFeatureModules())

    //IVC Coffee Features
    //setDynamicFeatures(getCoffeeDynamicFeatureModules())

    //Ecudor Features
    //setDynamicFeatures(getEcudorDynamicFeatureModules())

    //Nigeria Sesame Features
    //setDynamicFeatures(getNigeriaSesameDynamicFeatureModules())

    //Nigeria Cashew Features
    //setDynamicFeatures(getNigeriaCashewDynamicFeatureModules())

    //Nigeria Cococa Features
    //setDynamicFeatures(getNigeriaCococaDynamicFeatureModules())

    //Cameroon Features
    //setDynamicFeatures(getCameroonDynamicFeatureModules())

    //Nigeria Sesame and Cameroon Features
    //setDynamicFeatures(ModuleDependency.getSesameAndCameroonDynamicFeatureModules().toMutableSet())

    //Ghana Cashew Features
    //setDynamicFeatures(getGhanaCashewDynamicFeatureModules())

//    //Ghana Cocoa Features
//    setDynamicFeatures(getGhanaCocoaDynamicFeatureModules())

    //IVC Cashew Features
    //setDynamicFeatures(getCashewDynamicFeatureModules())

    //Indo Coffee Features
    //setDynamicFeatures(getIndoCoffeeDynamicFeatureModules())

    //india coffee Features
   // setDynamicFeatures(getIndiaCoffeeDynamicFeatureModules())

    //Indo Coffee and OD Features
    //setDynamicFeatures(getIndoCoffeeAndODDynamicFeatureModules())

    //val dymic = "".plus(dynamicFeatures.toString().replace("[","\"").replace("]","\"").replace(":",""))

    buildTypes {
        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug_keystore")
            BuildConfig.entity = dymic
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.dev)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doDev)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, getClientID(dymic, BuildType.DEBUG))
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckDev)
            buildConfigField(BuildConfig.type, BuildConfig.currentOrigin, dymic)
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                getClientSecret(dymic, BuildType.DEBUG)
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKey,
                BuildConfig.devBaseKeycloakUrl
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKeyResetPass,
                BuildConfig.devBaseKeycloakResetPassUrl
            )
        }

        create(BuildType.SIT) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug_keystore")
            BuildConfig.entity = dymic
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.sit)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doSit)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckSit)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, getClientID(dymic, BuildType.SIT))
            buildConfigField(BuildConfig.type, BuildConfig.currentOrigin, dymic)
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                getClientSecret(dymic, BuildType.SIT)
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKey,
                BuildConfig.sitBaseKeycloakUrl
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKeyResetPass,
                BuildConfig.sitBaseKeycloakResetPassUrl
            )
        }

        create(BuildType.UAT) {
            applicationIdSuffix = AndroidConfig.ID_UAT
            resValue("string", "app_name", "Vega-X-UAT")
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug_keystore")
            BuildConfig.entity = dymic
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.uat)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doUat)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckUat)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, getClientID(dymic, BuildType.UAT))
            buildConfigField(BuildConfig.type, BuildConfig.currentOrigin, dymic)
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                getClientSecret(dymic, BuildType.UAT)
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKey,
                BuildConfig.uatBaseKeycloakUrl
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKeyResetPass,
                BuildConfig.uatBaseKeycloakResetPassUrl
            )
        }

        getByName(BuildType.RELEASE) {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release_keystore")
            BuildConfig.entity = dymic
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.prod)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doProd)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckProd)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, getClientID(dymic, BuildType.RELEASE))
            buildConfigField(BuildConfig.type, BuildConfig.currentOrigin, dymic)
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                getClientSecret(dymic, BuildType.RELEASE)
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKey,
                BuildConfig.prodBaseKeycloakUrl
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKeyResetPass,
                BuildConfig.prodBaseKeycloakResetPassUrl
            )
        }

        create(BuildType.DEMO) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug_keystore")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.demo)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doUat)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckUat)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, BuildConfig.uatKeyClientId)
            buildConfigField(BuildConfig.type, BuildConfig.currentOrigin, dymic)
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                BuildConfig.uatKeyClientSecret
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKey,
                BuildConfig.uatBaseKeycloakUrl
            )
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }   
    
    project.configurations.all {
        resolutionStrategy.eachDependency {
            if (this.requested.group == "org.jetbrains.kotlin"
                && (requested.name.startsWith("kotlin-stdlib")) ) {
                this.useVersion("1.9.23")
            }
        }
    }
    gradle.taskGraph.whenReady {
        tasks.onEach { task ->
            if(task.name.contains("uploadCrashlyticsMappingFileRelease")){
                task.enabled = false
            }
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = TestOptions.IS_RETURN_DEFAULT_VALUES
    }
    lint {
        ignoreTestSources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        // "this" is currently lacking a proper type
        // See: https://youtrack.jetbrains.com/issue/KT-31077
        //val options = this as? KotlinJvmOptions
        this.jvmTarget = JavaVersion.VERSION_21.toString()
    }
   buildFeatures.viewBinding = true

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/kotlin-stdlib-common.kotlin_module")
        exclude("META-INF/kotlin-stdlib-jdk7.kotlin_module")
        exclude("META-INF/kotlin-stdlib-jdk8.kotlin_module")
        exclude("META-INF/kotlin-stdlib.kotlin_module")
        exclude("kotlin/annotation/annotation.kotlin_builtins")
        exclude("kotlin/collections/collections.kotlin_builtins")
        exclude("kotlin/coroutines/coroutines.kotlin_builtins")
        exclude("kotlin/internal/internal.kotlin_builtins")
        exclude("kotlin/kotlin.kotlin_builtins")
        exclude("kotlin/ranges/ranges.kotlin_builtins")
        exclude("kotlin/reflect/reflect.kotlin_builtins")
        exclude("com/itextpdf/io/font/cmap_info.txt")
        exclude("com/itextpdf/io/font/cmap/*")
    }
    namespace = "com.olam.warehouse.vegax"
}

//androidExtensions { isExperimental = true }

sonarqube {
    properties {
        property("sonar.projectName", "VegaX")
        property("sonar.projectKey", "VegaX")
        property("sonar.host.url", "https://10.101.32.103:9000/")
        property("sonar.language", "kotlin")
        property("sonar.sources", "src/main/java/")
        property("sonar.java.sources", "src/main/java/")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.login", "sonar-user")
        property("sonar.password", "olam123\$\$")
        property("detekt.sonar.kotlin.config.path", "${project.rootDir}/detekt.yml")
        property("sonar.issuesReport.html.enable", "true")
        property("sonar.issuesReport.console.enable", "true")
        //property("sonar.kotlin.detekt.reportPaths", "$buildDir/reports/detekt.xml")
        property(
            "sonar.exclusions",
            "**/BuildConfig.class,**/R.java,**/R\$*.java,src/main/gen/**/*,src/main/assets/**/*,src/androidTest/java"
        )
    }
}


dependencies {

    api(project(ModuleDependency.NAVIGATION))
    api(project(ModuleDependency.LOGIN))
    api(project(ModuleDependency.PRESENTATION))
    api(project(ModuleDependency.MASTER))
    //api(project(ModuleDependency.INAPP))

    //implementation(LibraryDependency.CRASH_ANALYTICS)
    debugImplementation(LibraryDependency.LEAK_CANARY)

    api(LibraryDependency.SUPPORT_CONSTRAINT_LAYOUT)
    api(LibraryDependency.RECYCLER_VIEW)
    //api(LibraryDependency.MATERIAL)
    api(LibraryDependency.FRAGMENT_KTX)
    api(LibraryDependency.FIREBASE_MSG)
    implementation(LibraryDependency.CRASLYTICS)
    implementation(LibraryDependency.ANALYTICS)
    implementation(platform(LibraryDependency.FIREBASE_BOM))

    //InApp Implenetation
    /*implementation("com.github.danysantiago:sendgrid-android:1") {
        exclude("org.apache.httpcomponents", "httpclient")
    }
    implementation("com.github.tarek360:instacapture:2.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.2.1")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("net.lingala.zip4j", "zip4j", "2.2.6")
    implementation("com.googlecode.libphonenumber", "libphonenumber", "8.11.0")*/
   // implementation("com.github.pratikbutani:MultiSelectSpinner:f47c0dadea")

    addTestDependencies()
}

apply(mapOf("plugin" to GradlePluginId.GOOGLE_SERVICE))

fun BaseFlavor.buildConfigFieldFromGradleProperty(gradlePropertyName: String) {
    val propertyValue = project.properties[gradlePropertyName] as? String
    checkNotNull(propertyValue) { "Gradle property $gradlePropertyName is null" }

    val androidResourceName = "GRADLE_${gradlePropertyName.toSnakeCase()}".toUpperCase()
    buildConfigField("String", androidResourceName, propertyValue)
}

fun getDynamicFeatureModuleNames() = ModuleDependency.getDynamicFeatureModules()
    .map { it.replace(":feature_", "") }
    .toSet()

fun String.toSnakeCase() = this.split(Regex("(?=[A-Z])")).joinToString("_") { it.toLowerCase() }

fun DefaultConfig.buildConfigField(name: String, value: Set<String>) {
    // Generates String that holds Java String Array code
    val strValue =
        value.joinToString(prefix = "{", separator = ",", postfix = "}", transform = { "\"$it\"" })
    buildConfigField("String[]", name, strValue)
}

fun getODDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-OD\""
    return ModuleDependency.getODDynamicFeatureModules().toMutableSet()
}

fun getCottonDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OGA-IV-COTT\""
    return ModuleDependency.getPortWarehouseDynamicFeatureModules().toMutableSet()
}

fun getTogoCottonDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OGA-TG-COTT\""
    return ModuleDependency.getPortWarehouseDynamicFeatureModules().toMutableSet()
}

fun getNicaraguaDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-NI-COFF\""
    return ModuleDependency.getNicaraguaDynamicFeatureModules().toMutableSet()
}

fun getCocoaDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-IV-COCO\""
    return ModuleDependency.getCocoaDynamicFeatureModules().toMutableSet()
}

fun getCoffeeDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-IV-COFF\""
    return ModuleDependency.getCoffeeDynamicFeatureModules().toMutableSet()
}

fun getEcudorDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-EC-COCO\""
    return ModuleDependency.getEcudorDynamicFeatureModules().toMutableSet()
}

fun getNigeriaSesameDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OGA-NG-SESA\""
    return ModuleDependency.getNigeriaSesameDynamicFeatureModules().toMutableSet()
}

fun getNigeriaCashewDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-NG-CASH\""
    return ModuleDependency.getNigeriaCocoaDynamicFeatureModules().toMutableSet()
}

fun getNigeriaCococaDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-NG-COCO\""
    return ModuleDependency.getNigeriaCocoaDynamicFeatureModules().toMutableSet()
}

fun getCameroonDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-CM-COCO\""
    return ModuleDependency.getCameroonDynamicFeatureModules().toMutableSet()
}

fun getGhanaCashewDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-GH-CASH\""
    return ModuleDependency.getGhanaCashewDynamicFeatureModules().toMutableSet()
}

fun getGhanaCocoaDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-GH-COCO\""
    return ModuleDependency.getGhanaCocoaDynamicFeatureModules().toMutableSet()
}

fun getCashewDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-IV-CASH\""
    return ModuleDependency.getCashewDynamicFeatureModules().toMutableSet()
}

fun getIndoCoffeeDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-ID-COFF\""
    return ModuleDependency.getIndoCoffeeDynamicFeatureModules().toMutableSet()
}

fun getIndiaCoffeeDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-IN-COFF\""
    return ModuleDependency.getIndiaCoffeeDynamicFeatureModules().toMutableSet()
}

fun getIndoCoffeeAndODDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-ID-COFF\""
    return ModuleDependency.getIndoCoffeeAndODDynamicFeatureModules().toMutableSet()
}


fun getClientID(entity: String, envir: String): String{
    return when{
        entity.contains("OFI") ->{
            return when(envir){
                BuildType.DEBUG -> BuildConfig.devKeyClientId
                BuildType.SIT -> BuildConfig.sitKeyClientId
                BuildType.UAT -> BuildConfig.uatKeyClientId
                else -> BuildConfig.prodKeyClientId
            }
        }
        else -> {
            return when(envir){
                BuildType.DEBUG -> BuildConfig.devKeyClientId_oga
                BuildType.SIT -> BuildConfig.sitKeyClientId_oga
                BuildType.UAT -> BuildConfig.uatKeyClientId_oga
                else -> BuildConfig.prodKeyClientId_oga
            }
        }
    }
}

fun getClientSecret(entity: String, envir: String): String{
    return when{
        entity.contains("OFI") ->{
            return when(envir){
                BuildType.DEBUG -> BuildConfig.devKeyClientSecret
                BuildType.SIT -> BuildConfig.sitKeyClientSecret
                BuildType.UAT -> BuildConfig.uatKeyClientSecret
                else -> BuildConfig.prodKeyClientSecret
            }
        }
        else -> {
            return when(envir){
                BuildType.DEBUG -> BuildConfig.devKeyClientSecret_oga
                BuildType.SIT -> BuildConfig.sitKeyClientSecret_oga
                BuildType.UAT -> BuildConfig.uatKeyClientSecret_oga
                else -> BuildConfig.prodKeyClientSecret_oga
            }
        }
    }
}
