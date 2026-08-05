plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
<<<<<<< Updated upstream
    kotlin("kapt") // 또는 ksp를 사용 중이시라면 ksp 적용

=======
    kotlin("kapt")
    id("com.google.firebase.appdistribution")
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======
firebaseAppDistribution {
    appId = "1:469157320114:android:7237cb5ab58a9bc48e40ab"
    releaseNotes = "WIKI-ROUTER v5.2 Native-WebView Bridge & Barcode Scan Update"
}
>>>>>>> Stashed changes

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

    // ★ Hilt가 Kotlin 2.4+ 메타데이터를 정상 해석하도록 추가 ★

    kapt(libs.hilt.compiler)

    // ★ AndroidX Lifecycle & Activity KTX (repeatOnLifecycle 및 viewModels() 지원) ★
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)


<<<<<<< Updated upstream

=======
    // --- 동시성 처리 및 바코드(ZXing) 라이브러리 ---
    implementation(libs.guava.android)
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.google.zxing.core)
    implementation(libs.journeyapps.zxing)

    // --- [최신 DSL 적용] Socket.io Client (JSON 모듈 exclude 처리 포함) ---
    implementation(libs.socket.io.client) {
        exclude(group = "org.json", module = "json")
    }

    // --- [최신 DSL 적용] 코루틴 비동기 처리 ---
    implementation(libs.kotlinx.coroutines.android)
>>>>>>> Stashed changes
}