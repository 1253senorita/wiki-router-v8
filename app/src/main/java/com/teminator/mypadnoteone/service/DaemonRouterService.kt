package com.terminator.mypadnoteone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.teminator.mypadnoteone.R
class DaemonRouterService : Service() {

    private val routerManager = SocketRouterManager()

    override fun onCreate() {
        super.onCreate()
        Log.d("DaemonRouterService", "데몬 서비스 생성됨")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            "ACTION_START_DAEMON" -> {
                Log.d("DaemonRouterService", "데몬 수동 시작 명령 접수")
                startForegroundServiceWithNotification()
                routerManager.startRouterLoop()
            }
            "ACTION_STOP_DAEMON" -> {
                Log.d("DaemonRouterService", "데몬 수동 중지 명령 접수")
                routerManager.stopRouterLoop()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "daemon_router_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "WIKI-Router Daemon Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // 호환성을 타지 않도록 가장 표준적인 방식으로 Notification 생성
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        val notification = builder
            .setContentTitle("WIKI-Router 데몬 가동 중")
            .setContentText("백그라운드에서 실시간 통신 및 라우팅 루프가 작동 중입니다.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        routerManager.stopRouterLoop()
        Log.d("DaemonRouterService", "데몬 서비스 소멸됨")
    }
}