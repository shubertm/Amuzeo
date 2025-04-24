import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.gradle.ktlint)
}

val properties = Properties()
val propertiesFile = rootProject.file("local.properties")

if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

val signingKeyStorePass: String = properties.getProperty("key.store.pass")
val keyPass: String = properties.getProperty("key.pass")
val amuzeoKeyAlias: String = properties.getProperty("key.alias")

android {
    namespace = "com.infbyte.amuzeo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.infbyte.amuzeo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("amuzeo_release_keystore.jks")
            storePassword = System.getenv("SIGNING_KEYSTORE_PASS") ?: signingKeyStorePass
            keyAlias = System.getenv("KEY_ALIAS") ?: amuzeoKeyAlias
            keyPassword = System.getenv("KEY_PASS") ?: keyPass

            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders.putAll(
                arrayOf(
                    "appIcon" to "@mipmap/amuzeo_debug",
                    "appRoundIcon" to "@mipmap/amuzeo_debug_round",
                    "appName" to "@string/app_name_debug",
                ),
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders.putAll(
                arrayOf(
                    "appIcon" to "@mipmap/amuzeo",
                    "appRoundIcon" to "@mipmap/amuzeo_round",
                    "appName" to "@string/app_name",
                ),
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.navigation)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    implementation(libs.io.insert.koin)
    implementation(libs.io.insert.koin.android)

    implementation(libs.io.coil)
    implementation(libs.io.coil.video)
    implementation(libs.io.coil.compose)

    implementation(libs.com.infbyte.amuze)

    implementation(libs.dev.arkbuilders.arklib)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.preBuild.dependsOn("ktlintCheck")

tasks.ktlintCheck.dependsOn("ktlintFormat")
