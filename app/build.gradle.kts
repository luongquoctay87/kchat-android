plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val hasGoogleServices = file("google-services.json").exists()

/** Production API — safep4y.com (internal APK distribution). */
val kchatProdApiUrl = "https://chat-api.safep4y.com/"
val kchatProdWsUrl = "wss://chat-api.safep4y.com"

if (hasGoogleServices) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.kchat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kchat"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.2.0-phase4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Placeholders — overridden per buildType
        buildConfigField("String", "API_BASE_URL", "\"https://chat-api.example.com/\"")
        buildConfigField("String", "WS_BASE_URL", "\"wss://chat-api.example.com\"")
        buildConfigField("boolean", "USE_FAKE_DATA", "false")
        buildConfigField("boolean", "FCM_ENABLED", hasGoogleServices.toString())
    }

    buildTypes {
        release {
            // Internal APK only — replace with company keystore before Play Store release.
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "API_BASE_URL", "\"$kchatProdApiUrl\"")
            buildConfigField("String", "WS_BASE_URL", "\"$kchatProdWsUrl\"")
            buildConfigField("boolean", "USE_FAKE_DATA", "false")
            buildConfigField("boolean", "FCM_ENABLED", hasGoogleServices.toString())
        }
        debug {
            // Emulator → host machine (k-chat-api :8864)
            //buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8864/\"")
            //buildConfigField("String", "WS_BASE_URL", "\"ws://10.0.2.2:8864\"")
            buildConfigField("String", "API_BASE_URL", "\"https://chat-api-test.tayjava.net/\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://chat-api-test.tayjava.net\"")

            // -Pkchat.useFake=true to force fake repositories while debugging UI
            val useFake = (project.findProperty("kchat.useFake") as String?)?.toBoolean() ?: false
            buildConfigField("boolean", "USE_FAKE_DATA", useFake.toString())
            buildConfigField("boolean", "FCM_ENABLED", hasGoogleServices.toString())
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":data:repository"))
    implementation(project(":data:fake"))
    implementation(project(":data:local"))
    implementation(project(":data:network"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.play.services)
}
