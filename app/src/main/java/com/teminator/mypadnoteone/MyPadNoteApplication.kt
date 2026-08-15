package com.teminator.mypadnoteone



import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyPadNoteApplication : Application() {
    // 비워두거나 onCreate를 아예 안 적어도 됩니다.
}