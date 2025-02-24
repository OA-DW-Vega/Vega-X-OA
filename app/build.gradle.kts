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
    id(GradlePluginId.SONAR)
    id(GradlePluginId.KTLINT_GRADLE)
    id(GradlePluginId.SAFE_ARGS)

}
var dymic = "\"OGA\""
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



    //############ This is for Only Development

    //IVC Cotton Port & Ginning Warehouse
    //setDynamicFeatures(getCottonDynamicFeatureModules())

    //TOGO Cotton Port & Ginning Warehouse
    //setDynamicFeatures(getTogoCottonDynamicFeatureModules())

    //Nigeria Sesame Features
    setDynamicFeatures(getNigeriaSesameDynamicFeatureModules())

    //Nigeria Sesame and Cameroon Features
    //setDynamicFeatures(ModuleDependency.getSesameAndCameroonDynamicFeatureModules().toMutableSet())



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
    debugImplementation(LibraryDependency.LEAK_CANARY)

    api(LibraryDependency.SUPPORT_CONSTRAINT_LAYOUT)
    api(LibraryDependency.RECYCLER_VIEW)
    api(LibraryDependency.FRAGMENT_KTX)
    api(LibraryDependency.FIREBASE_MSG)
    implementation(LibraryDependency.CRASLYTICS)
    implementation(LibraryDependency.ANALYTICS)
    implementation(platform(LibraryDependency.FIREBASE_BOM))


    addTestDependencies()
}

apply(mapOf("plugin" to GradlePluginId.GOOGLE_SERVICE))

fun BaseFlavor.buildConfigFieldFromGradleProperty(gradlePropertyName: String) {
    val propertyValue = project.properties[gradlePropertyName] as? String
    checkNotNull(propertyValue) { "Gradle property $gradlePropertyName is null" }

    val androidResourceName = "GRADLE_${gradlePropertyName.toSnakeCase()}".toUpperCase()
    buildConfigField("String", androidResourceName, propertyValue)
}

/*fun getDynamicFeatureModuleNames() = ModuleDependency.getDynamicFeatureModules()
    .map { it.replace(":feature_", "") }
    .toSet()*/

fun String.toSnakeCase() = this.split(Regex("(?=[A-Z])")).joinToString("_") { it.toLowerCase() }

fun DefaultConfig.buildConfigField(name: String, value: Set<String>) {
    // Generates String that holds Java String Array code
    val strValue =
        value.joinToString(prefix = "{", separator = ",", postfix = "}", transform = { "\"$it\"" })
    buildConfigField("String[]", name, strValue)
}


fun getCottonDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OGA-IV-COTT\""
    return ModuleDependency.getPortWarehouseDynamicFeatureModules().toMutableSet()
}

fun getTogoCottonDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OGA-TG-COTT\""
    return ModuleDependency.getPortWarehouseDynamicFeatureModules().toMutableSet()
}

fun getNigeriaSesameDynamicFeatureModules(): MutableSet<String> {
    dymic = "\"OGA-NG-SESA\""
    return ModuleDependency.getNigeriaSesameDynamicFeatureModules().toMutableSet()
}


fun getClientID(entity: String, envir: String): String{
    return  when(envir){
        BuildType.DEBUG -> BuildConfig.devKeyClientId_oga
        BuildType.SIT -> BuildConfig.sitKeyClientId_oga
        BuildType.UAT -> BuildConfig.uatKeyClientId_oga
        else -> BuildConfig.prodKeyClientId_oga
    }
}

fun getClientSecret(entity: String, envir: String): String{
    return when(envir){
        BuildType.DEBUG -> BuildConfig.devKeyClientSecret_oga
        BuildType.SIT -> BuildConfig.sitKeyClientSecret_oga
        BuildType.UAT -> BuildConfig.uatKeyClientSecret_oga
        else -> BuildConfig.prodKeyClientSecret_oga
    }

}
