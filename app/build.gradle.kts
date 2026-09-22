plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.apn7.shunya"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.apn7.shunya"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Signed with the local debug key so a release build installs over USB without a keystore.
            // Replace with a real signing config before publishing to the Play Store.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    // AGP 9 built-in Kotlin uses this Java target for Kotlin's jvmTarget as well.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // Lint must never block the first build on a fresh machine; run `gradlew lint` explicitly.
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.profileinstaller)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}

// Copy the debug APK to <repo>/apk/shunya-debug.apk after every assembleDebug, for easy sideloading.
val exportDebugApk = tasks.register<Copy>("exportDebugApk") {
    from(layout.buildDirectory.dir("outputs/apk/debug")) { include("*.apk") }
    into(rootProject.layout.projectDirectory.dir("apk"))
    rename { "shunya-debug.apk" }
}
tasks.matching { it.name == "assembleDebug" }.configureEach { finalizedBy(exportDebugApk) }
