plugins {
    id("com.android.application") version "8.12.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.dagger.hilt.android") version "2.53.1" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    // 🔥 아래 줄(Compose 플러그인 버전 선언)을 반드시 추가해주어야 합니다!
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
}