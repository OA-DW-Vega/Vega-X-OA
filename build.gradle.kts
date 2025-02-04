import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id(GradlePluginId.DETEKT)
    id(GradlePluginId.KTLINT_GRADLE)
    id(GradlePluginId.GRADLE_VERSION_PLUGIN)
    id(GradlePluginId.KOTLIN_JVM) apply false
    id(GradlePluginId.KOTLIN_ANDROID) apply false
    id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS) apply false
    id(GradlePluginId.ANDROID_APPLICATION) apply false
    id(GradlePluginId.ANDROID_DYNAMIC_FEATURE) apply false
    id(GradlePluginId.ANDROID_LIBRARY) apply false
    id(GradlePluginId.SAFE_ARGS) apply false
    id(GradlePluginId.GOOGLE_SERVICE) apply false
    id(GradlePluginId.FABRIC) apply false
    // id(GradlePluginId.DYNATRACE) apply false
}

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.4.20"
    repositories {
        google()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
    }


    dependencies {
        classpath(GradleOldWayPlugins.ANDROID_GRADLE)
        classpath(GradleOldWayPlugins.GOOGLE_SERVICE)
        classpath(GradleOldWayPlugins.KOTLIN_GRADLE)
        classpath(GradleOldWayPlugins.FABRIC)
        //classpath(GradleOldWayPlugins.DYNATRACE)
        classpath(GradleOldWayPlugins.SONAR)
        classpath(GradleOldWayPlugins.SCENEFORM)

    }
}

/*apply(plugin = "com.dynatrace.instrumentation")
configure<com.dynatrace.tools.android.dsl.DynatraceExtension> {
    configurations {
        create("debug") {
            enabled(false)
            variantFilter("debug")
            *//*autoStart {
                applicationId("b908cd2b-550b-43cf-a26e-cb90d12ecc88")
                beaconUrl("https://eok851.dynatrace-managed.com:9999/mbeacon/8f745a3a-ca48-40b4-aec4-a663c087c19f")
            }*//*
        }
        create("sit") {
            enabled(false)
            variantFilter("sit")
        }
        create("uat") {
//            enabled(false)
            variantFilter("uat")
            autoStart {
                applicationId("986e79de-a0f1-4df1-8570-37ca4e6b60c1")
                beaconUrl("https://eok851.dynatrace-managed.com:9999/mbeacon/82203a30-a89c-4afb-8009-c475586c3488")
            }
        }
        create("release") {
            //enabled(false)
            variantFilter("release")
            autoStart {
                applicationId("b908cd2b-550b-43cf-a26e-cb90d12ecc88")
                beaconUrl("https://eok851.dynatrace-managed.com:9999/mbeacon/8f745a3a-ca48-40b4-aec4-a663c087c19f")
            }
        }
    }
}*/

// all projects = root project + sub projects
allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://jitpack.io")
    }

    // We want to apply ktlint at all project level because it also checks build gradle files
    apply(plugin = GradlePluginId.KTLINT_GRADLE)

    // Ktlint configuration for sub-projects
    ktlint {
        version.set(CoreVersion.KTLINT)
        verbose.set(true)
        android.set(true)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
        filter {
            exclude { element -> element.file.path.contains("generated/") }
        }
    }

}


subprojects {
    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }

    apply(plugin = GradlePluginId.DETEKT)

    detekt {
        toolVersion = "1.1.1"
        input = files("src/main/java")
        config = files("${project.rootDir}/detekt.yml")
        parallel = true
        reports {
            xml {
                enabled = true // Enable/Disable XML report (default: true)
                destination =
                    file("$buildDir/reports/detekt.xml") // Path where XML report will be stored (default: `build/reports/detekt/detekt.xml`)
            }
            html {
                enabled = true // Enable/Disable HTML report (default: true)
                destination =
                    file("$buildDir/reports/detekt.html") // Path where HTML report will be stored (default: `build/reports/detekt/detekt.html`)
            }
            txt {
                enabled = true // Enable/Disable TXT report (default: true)
                destination =
                    file("$buildDir/reports/detekt.txt") // Path where TXT report will be stored (default: `build/reports/detekt/detekt.txt`)
            }

        }
    }
    /*pluginManager.withPlugin("com.android.library") {
        dependencies {
            "implementation"(com.dynatrace.tools.android.DynatracePlugin.agentDependency())
        }
    }*/
    //apply(plugin = GradlePluginId.SONAR)

}

// JVM target applied to all Kotlin tasks across all sub-projects
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

tasks {
    // Gradle versions plugin configuration
    "dependencyUpdates"(DependencyUpdatesTask::class) {
        resolutionStrategy {
            componentSelection {
                all {
                    // Do not show pre-release version of library in generated dependency report
                    val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview")
                        .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                        .any { it.matches(candidate.version) }
                    if (rejected) {
                        reject("Release candidate")
                    }
                }
            }
        }
    }
}
