import com.android.build.api.dsl.AndroidSourceSet
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions


plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.PARCELIZE)
    //id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS)
    id(GradlePluginId.KAPT)
//    id(GradlePluginId.SONAR)
}

android {
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK_VERSION
        //targetSdk = AndroidConfig.TARGET_SDK_VERSION

        //versionCode = AndroidConfig.VERSION_CODE
        //versionName = AndroidConfig.VERSION_NAME
        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }

    buildTypes {
        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.dev)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doDev)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckDev)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, getClientID(BuildConfig.entity, BuildType.DEBUG))
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                getClientSecret(BuildConfig.entity, BuildType.DEBUG)
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
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionName,
                "\"${AndroidConfig.VERSION_NAME}\""
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionCode,
                "\"${AndroidConfig.VERSION_CODE}\""
            )
        }

        create(BuildType.SIT) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.sit)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doSit)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckSit)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, getClientID(BuildConfig.entity, BuildType.SIT))
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                getClientSecret(BuildConfig.entity, BuildType.SIT)
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
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionName,
                "\"${AndroidConfig.VERSION_NAME}\""
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionCode,
                "\"${AndroidConfig.VERSION_CODE}\""
            )
        }

        create(BuildType.UAT) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.uat)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doUat)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckUat)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, getClientID(BuildConfig.entity, BuildType.UAT))
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                getClientSecret(BuildConfig.entity, BuildType.UAT)
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
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionName,
                "\"${AndroidConfig.VERSION_NAME}\""
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionCode,
                "\"${AndroidConfig.VERSION_CODE}\""
            )
        }

        getByName(BuildType.RELEASE) {
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = false
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.prod)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doProd)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckProd)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, getClientID(BuildConfig.entity, BuildType.RELEASE))
            buildConfigField(
                BuildConfig.type,
                BuildConfig.keyClientSecret,
                getClientSecret(BuildConfig.entity, BuildType.RELEASE)
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
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionName,
                "\"${AndroidConfig.VERSION_NAME}\""
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionCode,
                "\"${AndroidConfig.VERSION_CODE}\""
            )
        }

        create(BuildType.DEMO) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.demo)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doUat)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckUat)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, BuildConfig.uatKeyClientId)
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
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionName,
                "\"${AndroidConfig.VERSION_NAME}\""
            )
            buildConfigField(
                BuildConfig.type,
                BuildConfig.versionCode,
                "\"${AndroidConfig.VERSION_CODE}\""
            )
        }
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

    testOptions {
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
        unitTests.isReturnDefaultValues = TestOptions.IS_RETURN_DEFAULT_VALUES
    }
    lint {
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
        ignoreTestSources = true
    }

   buildFeatures.viewBinding = true
    sourceSets {
       // getByName("main").res.srcDirs("src/main/oga-res", "src/main/ofi-res", "src/main/res")
        setSourceSet(BuildConfig.entity, getByName("main"))
    }
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
    namespace = "com.olam.warehouse.presentation"
}

//androidExtensions { isExperimental = true }
/*


sonarqube {
    properties {
        property("sonar.projectName", "VegaX")
        property("sonar.projectKey", "VegaX")
        property("sonar.host.url", "https://10.101.32.103:9000/")
        property("sonar.language", "java")
        property("sonar.sources", "src/main/java/")
        property("sonar.java.sources", "src/main/java/")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.login", "sonar-user")
        property("sonar.password", "olam123\$\$")
    }
}
*/


dependencies {
    api(LibraryDependency.KOTLIN)
    api(LibraryDependency.KOTLIN_REFLECT)

    api(project(ModuleDependency.NAVIGATION))

    api(LibraryDependency.KOIN)
    //api(LibraryDependency.KOIN_VIEWMODEL)
    api(LibraryDependency.KOIN_CORE)
    api(LibraryDependency.KOIN_COMPAT)
    api(LibraryDependency.KOIN_COMPOSE)
    api(LibraryDependency.KOIN_KTOR)

    api(LibraryDependency.RETROFIT)
    api(LibraryDependency.RETROFIT_GSON_CONVERTER)
    implementation(LibraryDependency.LOGGING_INTERCEPTOR)
    implementation(LibraryDependency.STETHO)
    implementation(LibraryDependency.STETHO_OK_HTTP)
   // implementation(files("libs/itextpdf-5.4.3.jar"))

    api(LibraryDependency.PLAY_CORE)
    api(LibraryDependency.TIMBER)
    api(LibraryDependency.APP_COMPACT)
    api(LibraryDependency.SUPPORT_CONSTRAINT_LAYOUT)
    api(LibraryDependency.MATERIAL)
    api(LibraryDependency.MATERIAL_DIALOG)
    api(LibraryDependency.MATERIAL_INPUT)
    api(LibraryDependency.CORE_KTX)
    api(LibraryDependency.FRAGMENT_KTX)
    api(LibraryDependency.LIFECYCLE_EXTENSIONS)
    api(LibraryDependency.LIVEDATA_KTX)
    api(LibraryDependency.LIFECYCLE_VIEW_MODEL_KTX)
    api(LibraryDependency.ROOM_RUNTIME)
    api(LibraryDependency.ROOM_KTX)
    kapt(LibraryDependency.ROOM_COMPILER)
    api(LibraryDependency.WORK_RUNTIME)
    api(LibraryDependency.VISION)
    implementation(LibraryDependency.LOCATION)
    implementation(LibraryDependency.COMMON)
    api(LibraryDependency.PERMISSION_DISPATCHER)
    kapt(LibraryDependency.PERMISSION_DISPATCHER_COMPILER)
    api(LibraryDependency.LOTTIE)
    api(LibraryDependency.MOTOMO_SDK)
    api(LibraryDependency.RANGE_SEEKBAR)
    implementation(LibraryDependency.CRYPTO)
    api(LibraryDependency.MPCHART)
    api(LibraryDependency.GLIDE)
    annotationProcessor(LibraryDependency.GLIDE)
//    api("com.google.mlkit:barcode-scanning:17.3.0")
    api(LibraryDependency.QR_GENERATE)
    api(LibraryDependency.BIOMETRIC)
    api(LibraryDependency.QRCODE)
//    api(LibraryDependency.PDFVIEWER)

    implementation(LibraryDependency.ITEXT)
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

fun setSourceSet(entity: String, byName: AndroidSourceSet) {
    when {
        entity.contains("OFI") -> {
            sourceSets {
                byName.res.srcDirs("src/main/ofi-res", "src/main/res")
            }
        }
        entity.contains("OGA") -> {
            sourceSets {
                byName.res.srcDirs("src/main/oga-res", "src/main/res")
            }

        }
        entity.contains("DEMO") -> {
            sourceSets {
                byName.res.srcDirs("src/main/demo-res", "src/main/res")
            }
        }
        else -> {
            sourceSets {
                byName.res.srcDirs("src/main/ofi-res", "src/main/res")
            }
        }
    }
}
