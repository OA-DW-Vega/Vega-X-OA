import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions


plugins {
    id(GradlePluginId.ANDROID_DYNAMIC_FEATURE)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.PARCELIZE)
    //id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS)
    id(GradlePluginId.SAFE_ARGS)
    id(GradlePluginId.SONAR)
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
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.dev)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doDev)
        }

        create(BuildType.SIT) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = true
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.sit)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doSit)
        }

        create(BuildType.UAT) {
            //applicationIdSuffix = AndroidConfig.ID_UAT
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = true
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.uat)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doUat)
        }

        getByName(BuildType.RELEASE) {
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = false
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.prod)
             buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doProd)
        }
        create(BuildType.DEMO) {
            //applicationIdSuffix = AndroidConfig.ID_UAT
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            (this as com.android.build.gradle.internal.dsl.BuildType).isDebuggable = true
            buildConfigField(BuildConfig.type, BuildConfig.baseUrl, BuildConfig.demo)
            buildConfigField(BuildConfig.type, BuildConfig.baseDoUrl, BuildConfig.doUat)
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

    // This "test" source set is a fix for SafeArgs classes not being available when running Unit tests from cmd
    // See: https://issuetracker.google.com/issues/139242292
    sourceSets {
        getByName("test").java.srcDir("${project.rootDir}/app/build/generated/source/navigation-args/debug")
    }

    // Removes the need to mock need to mock classes that may be irrelevant from test perspective
    testOptions {
        unitTests.isReturnDefaultValues = TestOptions.IS_RETURN_DEFAULT_VALUES
    }

   buildFeatures.viewBinding = true
    namespace = "com.olam.warehouse.vegax.processingsesame"
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
    implementation(project(ModuleDependency.APP))
}
