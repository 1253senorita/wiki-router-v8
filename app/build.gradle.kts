plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    kotlin("kapt")
    kotlin("plugin.serialization") version "2.1.0"

}

android {
    namespace = "com.teminator.mypadnoteone"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.teminator.mypadnoteone"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)

    // --- 동시성 처리 및 바코드(ZXing) 라이브러리 ---
    implementation(libs.guava.android)
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.google.zxing.core)
    implementation(libs.journeyapps.zxing)

    // --- Socket.io Client ---
    implementation(libs.socket.io.client) {
        exclude(group = "org.json", module = "json")
    }

    // --- 코루틴 비동기 처리 ---
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.firebase.dataconnect)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")


    // --- [추가] KotlinX Serialization (드론 패킷 모델링용) ---
    implementation(libs.kotlinx.serialization.json)

}