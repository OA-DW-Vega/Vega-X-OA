import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions


plugins {
    id(GradlePluginId.ANDROID_DYNAMIC_FEATURE)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS)
    id(GradlePluginId.SAFE_ARGS)
    id(GradlePluginId.ARSCENEFORM)
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
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.dev)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doDev)
        }

        create(BuildType.SIT) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.sit)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doSit)
        }

        create(BuildType.UAT) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = true
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.uat)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doUat)
        }

        getByName(BuildType.RELEASE) {
            isDebuggable = false
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.prod)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doProd)
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

    // This "test" source set is a fix for SafeArgs classes not being available when running Unit tests from cmd
    // See: https://issuetracker.google.com/issues/139242292
    sourceSets {
        getByName("test").java.srcDir("${project.rootDir}/app/build/generated/source/navigation-args/debug")
    }

    // Removes the need to mock need to mock classes that may be irrelevant from test perspective
    testOptions {
        unitTests.isReturnDefaultValues = TestOptions.IS_RETURN_DEFAULT_VALUES
    }

    viewBinding.isEnabled = true
}

androidExtensions { isExperimental = true }

dependencies {
    implementation(project(ModuleDependency.APP))
    implementation(LibraryDependency.ARSCENE)
    implementation(LibraryDependency.ARCORE)
    implementation(LibraryDependency.GMS_AUTH)
    implementation(LibraryDependency.LOCATION)
}

sceneform.asset(
    "sampledata/model.obj",
    "default",
    "sampledata/model.sfa",
    "src/main/assets/model"
)


sceneform.asset(
    "sampledata/modelnew.obj",
    "default",
    "sampledata/modelnew.sfa",
    "src/main/assets/modelnew"
)

sceneform.asset(
    "sampledata/modelorg.obj",
    "default",
    "sampledata/modelorg.sfa",
    "src/main/assets/modelorg"
)
