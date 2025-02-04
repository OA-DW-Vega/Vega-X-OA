import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions


plugins {
    id(GradlePluginId.ANDROID_DYNAMIC_FEATURE)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS)
    id(GradlePluginId.SAFE_ARGS)
    id("kotlin-android")
    id(GradlePluginId.SONAR)
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
    implementation(project(ModuleDependency.APP))
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
}
