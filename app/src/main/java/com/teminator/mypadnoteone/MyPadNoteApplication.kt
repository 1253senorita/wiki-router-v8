package com.teminator.mypadnoteone



import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyPadNoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 앱 초기화 로직 필요시 구현
    }
}