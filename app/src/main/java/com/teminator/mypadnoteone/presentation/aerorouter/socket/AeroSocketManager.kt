package com.teminator.mypadnoteone.presentation.aerorouter.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class AeroSocketManager {

    private var socket: Socket? = null
    private val TAG = "AeroSocketManager"

    // 💡 핵심 수정: 서버가 HTTP로 구동 중이므로 http:// 10.0.2.2:8080 지정
    private val SERVER_URL = "http://192.168.219.100:8080"

    /**
     * Socket.io 서버 연결 초기화 (HTTP 환경 최적화)
     */
    fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
            }

            socket = IO.socket(SERVER_URL, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket Connected Successfully!")
                onConnected()
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val errorMsg = if (args.isNotEmpty()) args[0].toString() else "Unknown Connection Error"
                Log.e(TAG, "Connection Error: $errorMsg")
                onError(errorMsg)
            }

            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e(TAG, "URI Syntax Exception: ${e.message}")
            onError(e.message ?: "Invalid URL")
        }
    }

    fun requestWikiAuth(userId: String, userPw: String, modeId: String, callback: (Boolean, String) -> Unit) {
        val data = JSONObject().apply {
            put("userId", userId)
            put("userPw", userPw)
            put("modeId", modeId)
        }

        socket?.emit("get_oi", arrayOf<Any>(data)) { args ->
            if (args != null && args.isNotEmpty() && args[0] is JSONObject) {
                val res = args[0] as JSONObject
                val success = res.optBoolean("success", false)
                val payload = res.optJSONObject("payload")
                val text = payload?.optString("text") ?: "응답 없음"
                callback(success, text)
            } else {
                callback(false, "서버 응답 오류")
            }
        }
    }

    fun joinRoom(roomId: String) {
        socket?.emit("join-room", roomId)
        Log.d(TAG, "🏠 방 입장 요청 전송 -> Room ID: [$roomId]")
    }

    fun sendAudioData(audioByteArray: ByteArray) {
        val data = JSONObject().apply {
            put("blob", audioByteArray)
        }
        socket?.emit("sync-audio-file", arrayOf<Any>(data))
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        Log.d(TAG, "Socket Disconnected.")
    }
}