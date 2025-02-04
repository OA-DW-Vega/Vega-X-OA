import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions


plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS)
    id(GradlePluginId.KAPT)
//    id(GradlePluginId.SONAR)
}


android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)

    defaultConfig {
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)

        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME
        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }

    buildTypes {
        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.dev)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doDev)
            buildConfigField(BuildConfig.type, BuildConfig.baseTruckUrl, BuildConfig.truckDev)
            buildConfigField(BuildConfig.type, BuildConfig.keyClientId, BuildConfig.devKeyClientId)
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

    testOptions {
        unitTests.isReturnDefaultValues = TestOptions.IS_RETURN_DEFAULT_VALUES
    }

    viewBinding.isEnabled = true
}

androidExtensions { isExperimental = true }

/*

sonarqube {
    properties {
        property("sonar.projectName", "VegaX")
        property("sonar.projectKey", "vegax123")
        property("sonar.host.url", "http://localhost:9090/")
        property("sonar.language", "java")
        property("sonar.sources", "src/main/java/")
        property("sonar.java.sources", "src/main/java/")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.login", "admin")
        property("sonar.password", "vegax123#")
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
    api(LibraryDependency.QR_GENERATE)
    implementation(LibraryDependency.BIOMETRIC)
    implementation(LibraryDependency.QRCODE)
    //api(LibraryDependency.PDFVIEWER)
}
