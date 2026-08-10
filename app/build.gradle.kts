plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp) // 👈 ksp 유지
    kotlin("plugin.serialization") version "2.1.0"
    alias(libs.plugins.kotlin.compose)
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
        compose = true

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
    implementation(libs.androidx.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hilt.android)


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

    // Jetpack Compose 관련 의존성들은 이렇게 정확한 alias나 BOM 형태로 선언되어야 합니다.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)

    // 기존 kapt(libs.hilt.compiler) 부분을 아래처럼 ksp로 변경합니다.
    //kapt(libs.hilt.compiler)
    ksp(libs.hilt.compiler)

    // 🔥 톰이 버전 관리를 통한 프래그먼트 KTX 의존성 추가
    implementation(libs.androidx.fragment.ktx)
}