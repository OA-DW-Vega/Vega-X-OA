/**
 * Created by SangiliPandian C on 06-11-2019.
 */
object GradlePluginVersion {
    const val ANDROID_GRADLE = "3.6.0"
    const val KTLINT_GRADLE = "9.1.1"
    const val DETEKT = "1.1.1"
    const val GRADLE_VERSION_PLUGIN = "0.27.0"
    const val KOTLIN = CoreVersion.KOTLIN
    const val SAFE_ARGS = CoreVersion.NAVIGATION
    const val FABRIC = CoreVersion.FABRIC
    //const val DYNATRACE = CoreVersion.DYNATRACE
    const val GOOGLE_SERVICE = CoreVersion.GOOGLE_SERVICE
    const val SONAR = "2.7"
    const val SCENEFORM = "1.15.0"
}

object GradlePluginId {
    const val DETEKT = "io.gitlab.arturbosch.detekt"
    const val KTLINT_GRADLE = "org.jlleitschuh.gradle.ktlint"
    const val ANDROID_APPLICATION = "com.android.application"
    const val ANDROID_DYNAMIC_FEATURE = "com.android.dynamic-feature"
    const val ANDROID_LIBRARY = "com.android.library"
    const val KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
    const val KOTLIN_ANDROID = "org.jetbrains.kotlin.android"
    const val KOTLIN_ANDROID_EXTENSIONS = "org.jetbrains.kotlin.android.extensions"
    const val GRADLE_VERSION_PLUGIN = "com.github.ben-manes.versions"
    const val SAFE_ARGS = "androidx.navigation.safeargs.kotlin"
    const val KAPT = "org.jetbrains.kotlin.kapt"
    const val FABRIC = "io.fabric"
    const val SONAR = "org.sonarqube"

    //const val DYNATRACE = "com.dynatrace.instrumentation"
    const val GOOGLE_SERVICE = "com.google.gms.google-services"
    const val ARSCENEFORM = "com.google.ar.sceneform.plugin"
}

object GradleOldWayPlugins {
    const val ANDROID_GRADLE =
        "com.android.tools.build:gradle:${GradlePluginVersion.ANDROID_GRADLE}"
    const val KOTLIN_GRADLE =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${GradlePluginVersion.KOTLIN}"
    const val SAFE_ARGS =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${GradlePluginVersion.SAFE_ARGS}"
    const val GOOGLE_SERVICE =
        "com.google.gms:google-services:${GradlePluginVersion.GOOGLE_SERVICE}"
    const val FABRIC = "io.fabric.tools:gradle:${GradlePluginVersion.FABRIC}"

    //const val DYNATRACE = "com.dynatrace.tools.android:gradle-plugin:${GradlePluginVersion.DYNATRACE}"
    const val SONAR =
        "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${GradlePluginVersion.SONAR}"
    const val SCENEFORM = "com.google.ar.sceneform:plugin:${GradlePluginVersion.SCENEFORM}"
}
