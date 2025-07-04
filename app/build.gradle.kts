import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.gradle.ktlint)
}

val properties = Properties()
val propertiesFile = rootProject.file("local.properties")

if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

val signingKeyStorePass: String? = properties.getProperty("key.store.pass")
val keyPass: String? = properties.getProperty("key.pass")
val amuzeoKeyAlias: String? = properties.getProperty("key.alias")
val localVersionName: String? = properties.getProperty("local.version.name")
val amuzeoVersionCode: Int =
    properties.getProperty("local.version.code")?.toInt()
        ?: System.getenv("RELEASES")?.toInt() ?: 0

val testBannerAdUnitId: String? = properties.getProperty("test.banner.ad.unit.id")
val bannerAdUnitId: String? = properties.getProperty("banner.ad.unit.id")
val admobAppId: String? = properties.getProperty("admob.app.id")

android {
    namespace = "com.infbyte.amuzeo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.infbyte.amuzeo"
        minSdk = 26
        targetSdk = 35
        versionCode = amuzeoVersionCode + 1
        versionName = System.getenv("VERSION_NAME") ?: localVersionName

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
            resValue("string", "banner_ad_unit_id", System.getenv("TEST_BANNER_AD_UNIT_ID") ?: "$testBannerAdUnitId")
            resValue("string", "admob_app_id", System.getenv("ADMOB_APP_ID") ?: "$admobAppId")

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

            resValue("string", "admob_app_id", System.getenv("ADMOB_APP_ID") ?: "$admobAppId")
            resValue("string", "banner_ad_unit_id", System.getenv("BANNER_AD_UNIT_ID") ?: "$bannerAdUnitId")

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
    buildFeatures {
        compose = true
        buildConfig = true
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

    implementation(libs.com.infbyte.amuze)

    implementation(libs.google.mobile.ads)

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
