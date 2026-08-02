package com.teminator.mypadnoteone.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.teminator.mypadnoteone.service.PttService

class PttJavascriptInterface(
    private val context: Context,
    private val onPttStateChanged: (Boolean) -> Unit = {}
) {

    // 웹에서 토스트 메시지 출력 요청
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 무전 송신 시작/종료 상태 전달 (Web -> Native)
    @JavascriptInterface
    fun setPttActive(isActive: Boolean) {
        onPttStateChanged(isActive)
    }

    // 포그라운드 무전 서비스 시작
    @JavascriptInterface
    fun startPttService() {
        val intent = Intent(context, PttService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // 포그라운드 무전 서비스 종료
    @JavascriptInterface
    fun stopPttService() {
        val intent = Intent(context, PttService::class.java)
        context.stopService(intent)
    }
}