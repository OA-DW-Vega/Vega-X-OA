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

        ////versionCode = AndroidConfig.VERSION_CODE
        ////versionName = AndroidConfig.VERSION_NAME
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
                BuildConfig.baseUrlKeyResetPass,
                BuildConfig.devBaseKeycloakResetPassUrl
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
    namespace = "com.olam.warehouse.login"
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
    api(project(ModuleDependency.PRESENTATION))
    api(project(ModuleDependency.MASTER))
    implementation(LibraryDependency.PREFERENCE)
    api(LibraryDependency.LOTTIE)
    api(LibraryDependency.APPCENTER_SDK_ANALYTICS)
    api(LibraryDependency.APPCENTER_SDK_CRASH)
    api(LibraryDependency.APPCENTER_SDK_DISTRIBUTE)
    api(LibraryDependency.config)

    //api(LibraryDependency.PDFVIEWER)
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
