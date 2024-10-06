plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    androidTarget()
    iosArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", "com.traviswyatt.qd")
            binaryOption("bundleShortVersionString", "0.0.1")
            binaryOption("bundleVersion", "1")
            export(libs.coroutines.core)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.bundles.voyager)
            implementation(libs.datetime)
            implementation(libs.khronicle)
        }

        androidMain.dependencies {
            implementation(libs.androidx.startup)
            implementation(libs.compose.activity)
        }
    }
}

android {
    namespace = "com.traviswyatt.qd"
    compileSdk = libs.versions.android.compile.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.juul.sensortag.android"
        minSdk = libs.versions.android.min.get().toInt()
        targetSdk = libs.versions.android.target.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures.compose = true
}
