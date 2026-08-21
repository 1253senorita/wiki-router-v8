package com.teminator.mypadnoteone.presentation.aerorouter.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class AeroMultiSocketManager {

    private var socket: Socket? = null
    private val TAG = "AeroMultiSocketManager"

    // 💡 수정 포인트: https -> http 주소로 변경
    private val SERVER_URL = "http://192.168.219.100:8080"

    /**
     * 서버 연결 초기화 (HTTP 환경 최적화)
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

    /**
     * 방 입장 요청
     */
    fun joinRoom(roomId: String) {
        socket?.emit("join-room", roomId)
        Log.d(TAG, "🏠 방 입장 요청 전송 -> Room ID: [$roomId]")
    }

    /**
     * 오디오 데이터 전송
     */
    fun sendAudioData(audioByteArray: ByteArray) {
        val data = JSONObject().apply {
            put("blob", audioByteArray)
        }
        socket?.emit("sync-audio-file", arrayOf<Any>(data))
    }

    /**
     * 연결 해제
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        Log.d(TAG, "Socket Disconnected.")
    }
}