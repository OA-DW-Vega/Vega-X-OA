import com.android.build.gradle.internal.dsl.BaseFlavor
import com.android.build.gradle.internal.dsl.DefaultConfig
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id(GradlePluginId.ANDROID_APPLICATION)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS)
    id(GradlePluginId.FABRIC)
    id(GradlePluginId.SONAR)
    id(GradlePluginId.KTLINT_GRADLE)
    id(GradlePluginId.SAFE_ARGS)
}
var dymic = "\"OFI\""
android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)

    defaultConfig {
        applicationId = AndroidConfig.ID
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME
        multiDexEnabled = true
        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }


    //############ Each feature module that is included in settings.gradle.kts is added here as dynamic feature
    //dynamicFeatures = ModuleDependency.getDynamicFeatureModules().toMutableSet()

    //############ This is for Only Development

    //PortWarehouse 
    //dynamicFeatures = getCottonDynamicFeatureModules()

    //OD Features
    //dynamicFeatures = getODDynamicFeatureModules()

    //Nicaragua Features
    //dynamicFeatures = getNicaraguaDynamicFeatureModules()

    //Cocoa Features
    //dynamicFeatures = getCocoaDynamicFeatureModules()

    //Coffee Features
    //dynamicFeatures = getCoffeeDynamicFeatureModules()

    //Ecudor Features
    //dynamicFeatures = getEcudorDynamicFeatureModules()

    //Nigeria Sesame Features
    //dynamicFeatures = getNigeriaSesameDynamicFeatureModules()

    //Nigeria Cashew Features
    //dynamicFeatures = getNigeriaCashewDynamicFeatureModules()

    //Nigeria Cococa Features
    //dynamicFeatures = getNigeriaCococaDynamicFeatureModules()

    //Cameroon Features
    //dynamicFeatures = getCameroonDynamicFeatureModules()

    //Sesame and Cameroon Features
    //dynamicFeatures = ModuleDependency.getSesameAndCameroonDynamicFeatureModules().toMutableSet()

    //Ghana Cashew Features
    //dynamicFeatures = getGhanaCashewDynamicFeatureModules()

    //Ghana Cocoa Features
    //dynamicFeatures = getGhanaCocoaDynamicFeatureModules()

    //IVC Cashew Features
    //dynamicFeatures = getCashewDynamicFeatureModules()

    //Indo Coffee Features
    //dynamicFeatures = getIndoCoffeeDynamicFeatureModules()

    //india coffee Features
    //dynamicFeatures = getIndiaCoffeeDynamicFeatureModules()

    //Indo Coffee and OD Features
    dynamicFeatures = getIndoCoffeeAndODDynamicFeatureModules()

    //val dymic = "".plus(dynamicFeatures.toString().replace("[","\"").replace("]","\"").replace(":",""))

    buildTypes {
        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.dev)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doDev)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, BuildConfig.devKeyClientId)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckDev)
            buildConfigField(BuildConfig.type, BuildConfig.currentOrigin, dymic)
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                BuildConfig.devKeyClientSecret
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKey,
                BuildConfig.devBaseKeycloakUrl
            )
        }

        create(BuildType.SIT) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.sit)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doSit)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckSit)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, BuildConfig.sitKeyClientId)
            buildConfigField(BuildConfig.type, BuildConfig.currentOrigin, dymic)
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                BuildConfig.sitKeyClientSecret
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKey,
                BuildConfig.sitBaseKeycloakUrl
            )
        }

        create(BuildType.UAT) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.uat)
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

        getByName(BuildType.RELEASE) {
            isDebuggable = false
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.prod)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doProd)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckProd)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, BuildConfig.prodKeyClientId)
            buildConfigField(BuildConfig.type, BuildConfig.currentOrigin, dymic)
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                BuildConfig.prodKeyClientSecret
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.baseUrlKey,
                BuildConfig.prodBaseKeycloakUrl
            )
        }

        testOptions {
            unitTests.isReturnDefaultValues = TestOptions.IS_RETURN_DEFAULT_VALUES
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }


    lintOptions {
        // By default lint does not check test sources, but setting this option means that lint will not even parse them
        isIgnoreTestSources = true
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        // "this" is currently lacking a proper type
        // See: https://youtrack.jetbrains.com/issue/KT-31077
        val options = this as? KotlinJvmOptions
        options?.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    viewBinding.isEnabled = true
}

androidExtensions { isExperimental = true }

sonarqube {
    properties {
        property("sonar.projectName", "VegaX")
        property("sonar.projectKey", "vegax123")
        property("sonar.host.url", "http://localhost:9090/")
        property("sonar.language", "kotlin")
        property("sonar.sources", "src/main/java/")
        property("sonar.java.sources", "src/main/java/")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.login", "admin")
        property("sonar.password", "vegax123#")
        property("detekt.sonar.kotlin.config.path", "${project.rootDir}/detekt.yml")
        property("sonar.issuesReport.html.enable", "true")
        property("sonar.issuesReport.console.enable", "true")
        property("sonar.kotlin.detekt.reportPaths", "$buildDir/reports/detekt.xml")
        property("sonar.kotlin.detekt.reportPaths", "$buildDir/reports/detekt.xml")
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
    api(project(ModuleDependency.INAPP))

    implementation(LibraryDependency.CRASH_ANALYTICS)
    debugImplementation(LibraryDependency.LEAK_CANARY)

    api(LibraryDependency.SUPPORT_CONSTRAINT_LAYOUT)
    api(LibraryDependency.RECYCLER_VIEW)
    api(LibraryDependency.MATERIAL)
    api(LibraryDependency.FRAGMENT_KTX)
    api(LibraryDependency.FIREBASE_MSG)

    //InApp Implenetation
    implementation("com.github.danysantiago:sendgrid-android:1") {
        exclude("org.apache.httpcomponents", "httpclient")
    }
    implementation("com.github.tarek360:instacapture:2.0.1")
    implementation("com.github.bumptech.glide:glide:4.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.2.1")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("net.lingala.zip4j", "zip4j", "2.2.6")
    implementation("com.googlecode.libphonenumber", "libphonenumber", "8.11.0")
    implementation("com.github.pratikbutani:MultiSelectSpinner:f47c0dadea")


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

fun getNicaraguaDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-NI10-COFF\""
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
    dymic = "\"OFI-NG10-SESA\""
    return ModuleDependency.getNigeriaSesameDynamicFeatureModules().toMutableSet()
}

fun getNigeriaCashewDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-NG10-CASH\""
    return ModuleDependency.getNigeriaSesameDynamicFeatureModules().toMutableSet()
}

fun getNigeriaCococaDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-NG10-COCO\""
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
    dymic = "\"OFI-IN10-COFF\""
    return ModuleDependency.getIndiaCoffeeDynamicFeatureModules().toMutableSet()
}

fun getIndoCoffeeAndODDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OFI-ID-COFF\""
    return ModuleDependency.getIndoCoffeeAndODDynamicFeatureModules().toMutableSet()
}


